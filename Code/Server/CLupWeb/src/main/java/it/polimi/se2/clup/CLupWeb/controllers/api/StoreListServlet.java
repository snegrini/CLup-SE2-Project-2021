package it.polimi.se2.clup.CLupWeb.controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.StoreListMessage;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@WebServlet(name = "StoreListServlet", value = "/api/get_stores")
public class StoreListServlet extends HttpServlet {
    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

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
        String filter = StringEscapeUtils.escapeJava(request.getParameter("filter"));

        if (token == null || token.isEmpty()) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Missing token")));
            return;
        } else if (!token.matches("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$")) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Invalid token format")));
            return;
        }

        try {
            TokenManager.getCustomerId(token);
        } catch (TokenException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        List<StoreEntity> stores;
        try {
            if (filter == null) {
                stores = storeService.findAllStores();
            } else {
                stores = storeService.findAllStoresFiltered(filter);
            }
        } catch (BadStoreException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        String uploadLocation = getServletContext().getInitParameter("upload.location");
        for (StoreEntity store : stores) {
            Path fullPath = Path.of(uploadLocation + "/" + store.getImagePath());

            if (!Files.exists(fullPath)) {
                store.setImagePath("");
            } else {
                String encodeBytes = Base64.getEncoder().encodeToString(Files.readAllBytes(fullPath));
                store.setImagePath(encodeBytes);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode storesJson = mapper.valueToTree(new StoreListMessage(MessageStatus.OK, "Success", stores));

        try {
            addUserInQueueToJson(storesJson);
        } catch (BadTicketException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        out.print(storesJson.toPrettyString());
    }

    private void addUserInQueueToJson(JsonNode rootNode) throws BadTicketException {
        JsonNode s = rootNode.get("stores");
        Iterator<JsonNode> nodes = s.elements();

        while (nodes.hasNext()) {
            JsonNode entry = nodes.next();

            int custNum = ticketService.getCustomersQueue(entry.get("storeId").asInt());
            ((ObjectNode) entry).put("customersInQueue", custNum);
        }
    }
}
