package it.polimi.se2.clup.CLupWeb.controllers;

import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.ejb.EJB;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Dashboard", value = "/dashboard")
public class Dashboard extends HttpServlet {

    private TemplateEngine templateEngine;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

    public void init() {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        UserEntity user = (UserEntity) request.getSession().getAttribute("user");

        String path = "/dashboard";

        switch (user.getRole()) {
            case ADMIN:
                path += "/admin";
                break;
            case MANAGER:
                break;
            case EMPLOYEE:
                break;
            default:
                // Should not happen, throw exception?
                break;
        }

        RequestDispatcher dispatcher = getServletContext()
                .getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }
}