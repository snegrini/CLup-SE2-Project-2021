package it.polimi.se2.clup.CLupWeb.controllers.manager;

import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.se2.clup.CLupWeb.controllers.admin.StoreAddServlet.FROM_STR;
import static it.polimi.se2.clup.CLupWeb.controllers.admin.StoreAddServlet.TO_STR;

@WebServlet(name = "ManagerOpeningEditServlet", value = "/dashboard/ohedit")
public class OpeningEditServlet extends HttpServlet {

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

    public void init() {
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

        DateFormat sdf = new SimpleDateFormat("HH:mm");

        // Prepare a map of opening hours.
        for (String day : days) {

            List<Time> tempFromOh = new ArrayList<>();
            List<Time> tempToOh = new ArrayList<>();

            for (int i = 1; i <= 2; i++) {
                if (parameterMap.containsKey(day + FROM_STR + i) && parameterMap.containsKey(day + TO_STR + i)) {
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
            }
            ohFromMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempFromOh);
            ohToMap.put(DayOfWeek.valueOf(day.toUpperCase()).getValue(), tempToOh);
        }

        // Store the opening hours.
        try {
            ohService.updateAllOpeningHour(storeId, ohFromMap, ohToMap, user.getUserId());
        } catch (BadOpeningHourException | UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        String path = getServletContext().getContextPath() + "/dashboard/storeinfo";
        response.sendRedirect(path);
    }
}
