package it.polimi.se2.clup.CLupWeb.controllers.manager;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.TicketService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ManagerTicketDeleteServlet", value = "/dashboard/ticketdelete")
public class TicketDeleteServlet extends HttpServlet {

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/TicketService")
    private TicketService ticketService;

    public void init() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve request parameters.
        int ticketId;
        try {
            ticketId = Integer.parseInt(request.getParameter("ticketId"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values.");
            return;
        }

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");

        // Delete ticket.
        try {
            TicketEntity ticket = ticketService.findTicketById(ticketId);

            if (ticket == null) {
                response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, "Ticket not found.");
                return;
            }

            ticketService.deleteTicket(ticketId, user.getUserId());
        } catch (BadTicketException | UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to delete opening hour.");
            return;
        }

        String ctxpath = getServletContext().getContextPath();
        String path = ctxpath + "/dashboard/ticketlist";
        response.sendRedirect(path);
    }
}
