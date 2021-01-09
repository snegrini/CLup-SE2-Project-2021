package it.polimi.se2.clup.CLupWeb.controllers.manager;

import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadOpeningHourException;
import it.polimi.se2.clup.CLupEJB.services.OpeningHourService;
import it.polimi.se2.clup.CLupEJB.services.StoreService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ManagerOpeningDeleteServlet", value = "/dashboard/ohdelete")
public class OpeningDeleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/OpeningHourService")
    private OpeningHourService ohService;

    public void init() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve request parameters.
        Integer ohId = null;
        try {
            ohId = Integer.parseInt(request.getParameter("ohid"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values.");
            return;
        }

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");

        // Delete opening hour.
        try {
            OpeningHourEntity oh = ohService.findOpeningHourById(ohId);

            if (oh == null) {
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Opening hour not found.");
                return;
            }

            ohService.deleteOpeningHour(ohId, user.getUserId());
        } catch (BadOpeningHourException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete opening hour.");
            return;
        }

        String ctxpath = getServletContext().getContextPath();
        String path = ctxpath + "/dashboard/storeinfo";
        response.sendRedirect(path);

    }
}
