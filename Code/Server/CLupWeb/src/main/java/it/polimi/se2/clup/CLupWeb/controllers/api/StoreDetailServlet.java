package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.StoreMessage;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@WebServlet(name = "StoreDetailServlet", value = "/api/store_detail")
public class StoreDetailServlet extends HttpServlet {
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

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        String token = StringEscapeUtils.escapeJava(request.getParameter("token"));
        String requestId = StringEscapeUtils.escapeJava(request.getParameter("store_id"));

        if (requestId == null || requestId.isEmpty()) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Missing store ID")));
            return;
        }

        int storeId;
        try {
            storeId = Integer.parseInt(requestId);
        } catch (Exception e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid store ID")));
            return;
        }

        try {
            TokenManager.getCustomerId(token);
        } catch (TokenException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        StoreEntity storeEntity = storeService.findStoreById(storeId);
        if (storeEntity == null) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid store ID")));
            return;
        }

        String uploadLocation = getServletContext().getInitParameter("upload.location");
        Path fullPath = Path.of(uploadLocation + "/" + storeEntity.getImagePath());

        if (storeEntity.getImagePath().isEmpty() || !Files.exists(fullPath) || !Files.exists(Path.of(uploadLocation))) {
            storeEntity.setImagePath("");
        } else {
            String encodeBytes = Base64.getEncoder().encodeToString(Files.readAllBytes(fullPath));
            storeEntity.setImagePath(encodeBytes);
        }

        // TODO Add estimated time

        out.print(ow.writeValueAsString(new StoreMessage(MessageStatus.OK, "Success", storeEntity)));
    }
}