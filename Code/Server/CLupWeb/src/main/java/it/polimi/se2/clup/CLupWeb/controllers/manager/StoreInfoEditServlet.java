package it.polimi.se2.clup.CLupWeb.controllers.manager;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
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
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;


@WebServlet(name = "ManagerStoreInfoEditServlet", value = "/dashboard/storeinfoedit")
public class StoreInfoEditServlet  extends HttpServlet {

    private TemplateEngine templateEngine;

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
        Map<String, List<OpeningHourEntity>> openingHourMap = new LinkedHashMap<>();

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
        String path = "/WEB-INF/manager/store_info_edit.html";

        ctx.setVariable("storeCap", storeCap);
        ctx.setVariable("openingHourMap", openingHourMap);

        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer storeCap = null;
        try {
            storeCap = Integer.parseInt(request.getParameter("storeCap"));
        } catch (NullPointerException | NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad store cap value.");
            return;
        }

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");
        int storeId = user.getStore().getStoreId();

        // Store the opening hours.
        try {
            storeService.updateStoreCap(storeCap, storeId, user.getUserId());
        } catch (UnauthorizedException | BadStoreException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        String path = getServletContext().getContextPath() + "/dashboard/storeinfo";
        response.sendRedirect(path);
    }
}
