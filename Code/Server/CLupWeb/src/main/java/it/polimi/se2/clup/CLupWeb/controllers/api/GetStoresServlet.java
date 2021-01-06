package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.StoreListMessage;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
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

@WebServlet(name = "GetStoresServlet", value = "/api/get_stores")
public class GetStoresServlet extends HttpServlet {
    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

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

        String token = StringEscapeUtils.escapeJava(request.getParameter("token"));
        String filter = StringEscapeUtils.escapeJava(request.getParameter("filter"));

        if (token == null) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, "Missing token")));
            return;
        } else if (!token.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, "Invalid token format")));
            return;
        }

        try {
            TokenManager.getCustomerId(token);
        } catch (TokenException e) {
            out.print(gson.toJson(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        List<StoreEntity> stores;
        if (filter == null) {
            stores = storeService.findAllStores();
        } else {
            stores = storeService.findAllStoresFiltered(filter);
        }

        out.print(gson.toJson(new StoreListMessage(MessageStatus.OK, "Success", stores)));
    }
}
