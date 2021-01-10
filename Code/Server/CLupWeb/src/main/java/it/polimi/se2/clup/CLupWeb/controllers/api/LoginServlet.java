package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.enums.UserRole;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.TokenMessage;
import it.polimi.se2.clup.CLupEJB.services.UserService;
import it.polimi.se2.clup.CLupEJB.util.TokenManager;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.context.WebContext;

import javax.ejb.EJB;
import javax.persistence.NonUniqueResultException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LoginServlet", value = "/api/login")
public class LoginServlet extends HttpServlet {
    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/UserService")
    private UserService userService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("Forbidden");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String usercode = null;
        String password = null;


        usercode = StringEscapeUtils.escapeJava(request.getParameter("usercode"));
        password = StringEscapeUtils.escapeJava(request.getParameter("password"));

        if (usercode == null || password == null || usercode.isEmpty() || password.isEmpty()) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid login parameters")));
            return;
        }

        UserEntity user = null;
        try {
            user = userService.checkCredentials(usercode, password);
        } catch (CredentialsException | NonUniqueResultException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Could not check credentials")));
            return;
        }

        if (user == null) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Incorrect usercode or password")));
            return;
        }

        if (user.getRole() != UserRole.EMPLOYEE) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "User not authorized")));
            return;
        }

        String token;
        try {
            token = TokenManager.generateEmployeeToken(user.getStore().getStoreId());
        } catch (TokenException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        out.print(ow.writeValueAsString(new TokenMessage(MessageStatus.OK, "Success", token)));
    }

}
