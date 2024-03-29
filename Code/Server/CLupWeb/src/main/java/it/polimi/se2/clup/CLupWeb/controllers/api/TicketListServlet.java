package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.TicketListMessage;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import it.polimi.se2.clup.CLupEJB.util.TokenManager;
import org.apache.commons.text.StringEscapeUtils;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "TicketListServlet", value = "/api/get_tickets")
public class TicketListServlet extends HttpServlet {
    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/TicketService")
    private TicketService ticketService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("Forbidden");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String token = StringEscapeUtils.escapeJava(request.getParameter("token"));

        if (token == null || token.isEmpty()) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Missing token")));
            return;
        } else if (!token.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid token format")));
            return;
        }

        String customerId;
        try {
            customerId = TokenManager.getCustomerId(token);
        } catch (TokenException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        List<TicketEntity> tickets;

        try {
            tickets = ticketService.findCustomerTickets(customerId);
        } catch (BadTicketException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        out.print(ow.writeValueAsString(new TicketListMessage(MessageStatus.OK, "Success", tickets)));
    }
}
