package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.TicketMessage;
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

@WebServlet(name = "TicketDetailServlet", value = "/api/ticket_detail")
public class TicketDetailServlet extends HttpServlet {
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
        String requestId = StringEscapeUtils.escapeJava(request.getParameter("ticket_id"));

        if (requestId == null || requestId.isEmpty()) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Missing ticket ID")));
            return;
        }

        int ticketId;
        try {
            ticketId = Integer.parseInt(requestId);
        } catch (Exception e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid ticket ID")));
            return;
        }

        String customerId;
        try {
            customerId = TokenManager.getCustomerId(token);
        } catch (TokenException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        TicketEntity ticketEntity = ticketService.findValidTicketById(ticketId);
        if (ticketEntity == null) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid ticket ID")));
            return;
        }

        if (!ticketEntity.getCustomerId().equals(customerId)) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Unauthorized operation")));
            return;
        }

        out.print(ow.writeValueAsString(new TicketMessage(MessageStatus.OK, "Success", ticketEntity)));
    }

}
