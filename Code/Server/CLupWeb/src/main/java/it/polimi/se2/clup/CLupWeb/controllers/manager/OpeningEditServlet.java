package it.polimi.se2.clup.CLupWeb.controllers.manager;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;

import static it.polimi.se2.clup.CLupWeb.controllers.admin.StoreAddServlet.FROM_STR;
import static it.polimi.se2.clup.CLupWeb.controllers.admin.StoreAddServlet.TO_STR;

@WebServlet(name = "ManagerOpeningEditServlet", value = "/dashboard/ohedit")
public class OpeningEditServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

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
        UserEntity user = (UserEntity) request.getSession().getAttribute("user");
        int storeId = user.getStore().getStoreId();

        StoreEntity store;
        try {
            store = storeService.findStoreById(storeId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find store");
            return;
        }

        int storeCap = store.getStoreCap();

        // Build a custom map of opening hours for thymeleaf templating.
        // The map is composed of the name of the day (e.g. Monday) with all its opening hours.
        // Indeed one day could have more than one opening hour.
        // (e.g.) Monday: 08:00 - 12:00, 14:00 - 18:00.
        List<OpeningHourEntity> openingHourList = store.getOpeningHours();
        Map<String, List<OpeningHourEntity>> openingHourMap = new HashMap<>();

        // Prepare map with all the week days.
        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, Locale.getDefault());
            openingHourMap.put(dayName, new ArrayList<>());
        }

        // Fill in the map with the actual data.
        for (OpeningHourEntity oh : openingHourList) {
            String dayName = DayOfWeek.of(oh.getWeekDay()).getDisplayName(TextStyle.FULL, Locale.getDefault());
            openingHourMap.get(dayName).add(oh);
        }

        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/manager/oh_edit.html";

        ctx.setVariable("openingHourMap", openingHourMap);

        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] days = request.getParameterValues("day[]");

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<Integer, List<Time>> ohFromMap = new HashMap<>();
        Map<Integer, List<Time>> ohToMap = new HashMap<>();

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");
        int storeId = user.getStore().getStoreId();

        if (days == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to find opening hour days.");
            return;
        }

        // Prepare a map of opening hours.
        for (String day : days) {

            List<Time> tempFromOh = new ArrayList<>();
            List<Time> tempToOh = new ArrayList<>();

            for (int i = 1; i <= 2; i++) {
                String fromTimeStr = parameterMap.get(day + FROM_STR + i)[0];
                String toTimeStr = parameterMap.get(day + TO_STR + i)[0];

                if (!fromTimeStr.isEmpty() && !toTimeStr.isEmpty()) {
                    try {
                        tempFromOh.add(Time.valueOf(fromTimeStr));
                        tempToOh.add(Time.valueOf(toTimeStr));
                    } catch (IllegalArgumentException e) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad input time value.");
                        return;
                    }
                }

            }
            ohFromMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempFromOh);
            ohToMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempToOh);
        }

        // Store the opening hours.
        try {
            ohService.updateAllOpeningHour(storeId, ohFromMap, ohToMap, user.getUserId());
        } catch (BadOpeningHourException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        String path = getServletContext().getContextPath() + "/dashboard/storeinfo";
        response.sendRedirect(path);
    }
}
