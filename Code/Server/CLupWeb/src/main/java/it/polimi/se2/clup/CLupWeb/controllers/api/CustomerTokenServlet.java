package it.polimi.se2.clup.CLupWeb.controllers.api;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.TokenMessage;
import it.polimi.se2.clup.CLupEJB.util.TokenManager;
import org.apache.commons.text.StringEscapeUtils;

@WebServlet(name = "CustomerTokenServlet", value = "/api/customer_token")
public class CustomerTokenServlet extends HttpServlet {
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

        Gson gson = new GsonBuilder().create();

        String customerId = StringEscapeUtils.escapeJava(request.getParameter("customer_id"));

        if (customerId == null) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, "Missing customer id")));
            return;
        } else if (!(customerId.length() == 64 && customerId.matches("[a-fA-F0-9]+"))) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, "Invalid customer id format")));
            return;
        }

        String token;
        try {
            token = TokenManager.generateCustomerToken(customerId);
        } catch (TokenException e) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        out.print(gson.toJson(new TokenMessage(MessageStatus.OK, "Success", token)));
    }
}
