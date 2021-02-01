package it.polimi.se2.clup.CLupWeb.controllers.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.BadTicketException;
import it.polimi.se2.clup.CLupEJB.exceptions.TokenException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.messages.EmployeeMessage;
import it.polimi.se2.clup.CLupEJB.messages.Message;
import it.polimi.se2.clup.CLupEJB.messages.TicketListMessage;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import it.polimi.se2.clup.CLupEJB.services.TicketService;
import it.polimi.se2.clup.CLupEJB.util.TokenManager;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "EmployeeHomeServlet", value = "/dashboard/employee")
public class HomeServlet extends HttpServlet {
    private TemplateEngine templateEngine;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/TicketService")
    private TicketService ticketService;

    public void init() {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");
        int storeId = user.getStore().getStoreId();

        StoreEntity store;
        try {
            store = storeService.findStoreById(storeId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find store");
            return;
        }

        int customersInside = store.getCustomersInside();

        int customersQueue;
        try {
            customersQueue = ticketService.getCustomersQueue(storeId);
        } catch (BadTicketException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find queue number.");
            return;
        }

        List<TicketEntity> validTickets = null;
        try {
            validTickets = ticketService.findValidStoreTickets(storeId);
        } catch (BadTicketException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find valid tickets.");
            return;
        }
        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/employee/index.html";

        ctx.setVariable("store", store);
        ctx.setVariable("customersQueue", customersQueue);
        ctx.setVariable("validTickets", validTickets);

        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");
        int storeId = user.getStore().getStoreId();

        StoreEntity store;
        try {
            store = storeService.findStoreById(storeId);
        } catch (Exception e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, e.getMessage())));
            return;
        }

        int customersInside = store.getCustomersInside();

        int customersQueue;
        try {
            customersQueue = ticketService.getCustomersQueue(storeId);
        } catch (Exception e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Could not find queue number.")));
            return;
        }

        List<TicketEntity> validTickets = null;
        try {
            validTickets = ticketService.findValidStoreTickets(storeId);
        } catch (BadTicketException e) {
            out.print(ow.writeValueAsString(new Message(MessageStatus.ERROR, "Could not find valid tickets.")));
            return;
        }

        int storeCap = store.getStoreCap();

        out.print(ow.writeValueAsString(new EmployeeMessage(MessageStatus.OK, "Success", validTickets, customersInside, customersQueue, storeCap)));
    }
}
