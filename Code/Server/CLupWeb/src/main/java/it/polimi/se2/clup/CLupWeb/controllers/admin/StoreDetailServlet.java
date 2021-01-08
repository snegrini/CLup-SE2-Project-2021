package it.polimi.se2.clup.CLupWeb.controllers.admin;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AdminStoreDetailServlet", value = "/dashboard/storedetails")
public class StoreDetailServlet extends HttpServlet {

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestId = request.getParameter("id");

        if (requestId == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        int storeId;
        StoreEntity store = null;
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
}
