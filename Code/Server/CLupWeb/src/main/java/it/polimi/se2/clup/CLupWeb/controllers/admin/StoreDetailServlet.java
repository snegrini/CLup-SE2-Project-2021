package it.polimi.se2.clup.CLupWeb.controllers.admin;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.exceptions.BadStoreException;
import it.polimi.se2.clup.CLupEJB.exceptions.CredentialsException;
import it.polimi.se2.clup.CLupEJB.exceptions.UnauthorizedException;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import it.polimi.se2.clup.CLupEJB.services.UserService;
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
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminStoreDetailServlet", value = "/dashboard/storedetails")
public class StoreDetailServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/StoreService")
    private StoreService storeService;

    @EJB(name = "it.polimi.se2.clup.CLupEJB.services/UserService")
    private UserService userService;

    public void init() {
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setSuffix(".html");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestId = request.getParameter("id");

        if (requestId == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        int storeId;
        StoreEntity store;
        try {
            storeId = Integer.parseInt(requestId);
            store = storeService.findStoreById(storeId);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        if (store == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/admin/store_detail.html";

        ctx.setVariable("store", store);

        templateEngine.process(path, ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestId = request.getParameter("id");
        UserEntity user = (UserEntity) request.getSession().getAttribute("user");

        if (requestId == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        int storeId;
        StoreEntity store;
        try {
            storeId = Integer.parseInt(requestId);
            store = storeService.findStoreById(storeId);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        if (store == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        List<Map.Entry<String, String>> genUsers;
        try {
            genUsers = userService.regenerateCredentials(store, user.getUserId());
        } catch (BadStoreException | CredentialsException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        } catch (UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/admin/store_detail.html";

        ctx.setVariable("store", store);
        ctx.setVariable("genUsers", genUsers);

        templateEngine.process(path, ctx, response.getWriter());
    }
}
