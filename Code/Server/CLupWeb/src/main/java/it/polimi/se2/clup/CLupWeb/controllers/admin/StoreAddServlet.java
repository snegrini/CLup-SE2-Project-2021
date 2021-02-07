package it.polimi.se2.clup.CLupWeb.controllers.admin;

import it.polimi.se2.clup.CLupEJB.entities.AddressEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

@WebServlet(name = "AdminStoreAddServlet", value = "/dashboard/storeadd")
@MultipartConfig
public class StoreAddServlet extends HttpServlet  {

    private TemplateEngine templateEngine;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/UserService")
    private UserService userService;

    public static final String FROM_STR = "-from-";
    public static final String TO_STR = "-to-";

    public void init() {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        List<String> days = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            days.add(day.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        }
        ctx.setVariable("days", days);

        String path = "/WEB-INF/admin/store_add.html";

        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] days = request.getParameterValues("day[]");

        Map<String, String[]> parameterMap = request.getParameterMap();


        // Save uploaded image
        Part filePart = request.getPart("image"); // Retrieves <input type="file" name="image">

        if (filePart == null || filePart.getSize() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing store logo.");
            return;
        }

        File uploads, file;
        try {
            uploads = new File(getServletContext().getInitParameter("upload.location"));
            file = File.createTempFile("image-", ".png", uploads);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find the folder to upload images. Please fix the specified folder in the web.xml file.");
            return;
        }

        try (InputStream fileContent = filePart.getInputStream()) {
            Files.copy(fileContent, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }


        Map<Integer, List<Time>> ohFromMap = new HashMap<>();
        Map<Integer, List<Time>> ohToMap = new HashMap<>();

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");

        // Check parameters are present and correct
        String storeName = StringEscapeUtils.escapeJava(request.getParameter("store-name"));
        String pec = StringEscapeUtils.escapeJava(request.getParameter("pec"));
        String phone = StringEscapeUtils.escapeJava(request.getParameter("phone"));
        String street = StringEscapeUtils.escapeJava(request.getParameter("address"));
        String stnumber = StringEscapeUtils.escapeJava(request.getParameter("stnumber"));
        String city = StringEscapeUtils.escapeJava(request.getParameter("city"));
        String province = StringEscapeUtils.escapeJava(request.getParameter("province"));
        String postalcode = StringEscapeUtils.escapeJava(request.getParameter("postalcode"));
        String country = StringEscapeUtils.escapeJava(request.getParameter("country"));

        if (storeName == null || storeName.isEmpty() || pec == null || pec.isEmpty() ||
                phone == null || phone.isEmpty() ||
                street == null || street.isEmpty() || stnumber == null || stnumber.isEmpty() ||
                city == null || city.isEmpty() || province == null || province.isEmpty() ||
                postalcode == null || postalcode.isEmpty() || country == null || country.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values.");
            return;
        }

        if (days == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to find opening hour days.");
            return;
        }

        DateFormat sdf = new SimpleDateFormat("HH:mm");

        // Prepare a map of opening hours.
        for (String day : days) {

            List<Time> tempFromOh = new ArrayList<>();
            List<Time> tempToOh = new ArrayList<>();

            for (int i = 1; i <= 2; i++) {
                String fromTimeStr = parameterMap.get(day + FROM_STR + i)[0];
                String toTimeStr = parameterMap.get(day + TO_STR + i)[0];

                if (!fromTimeStr.isEmpty() && !toTimeStr.isEmpty()) {
                    try {
                        tempFromOh.add(new java.sql.Time(sdf.parse(fromTimeStr).getTime()));
                        tempToOh.add(new java.sql.Time(sdf.parse(toTimeStr).getTime()));
                    } catch (IllegalArgumentException | ParseException e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad input time value.");
                        return;
                    }
                }
            }

            // Check if after parsing, no opening hour has been specified.
            if (tempFromOh.isEmpty() || tempToOh.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to find opening hour days.");
                return;
            }

            ohFromMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempFromOh);
            ohToMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempToOh);
        }

        // Add a new store with address, opening hours and credentials.
        StoreEntity store;
        List<Map.Entry<String, String>> genUsers;
        try {
            AddressEntity address = new AddressEntity(street, stnumber, city, province, postalcode, country);

            // Create the store.
            genUsers = storeService.addStore(storeName, pec, phone, file.getName(), address, ohFromMap, ohToMap, user.getUserId());
            store = storeService.findStoreByName(storeName);

        } catch (BadStoreException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        } catch (EJBException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad input data.");
            return;
        }

        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/admin/store_detail.html";

        ctx.setVariable("store", store);
        ctx.setVariable("genUsers", genUsers);

        templateEngine.process(path, ctx, response.getWriter());
    }
}
