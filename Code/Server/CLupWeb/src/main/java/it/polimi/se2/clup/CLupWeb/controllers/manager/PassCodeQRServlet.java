package it.polimi.se2.clup.CLupWeb.controllers.manager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.entities.UserEntity;
import it.polimi.se2.clup.CLupEJB.services.StoreService;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@WebServlet(name = "ManagerPassCodeQRServlet", value = "/dashboard/defaultpasscode")
public class PassCodeQRServlet extends HttpServlet {

    private static final int QRCODE_IMAGE_HEIGHT = 250;
    private static final int QRCODE_IMAGE_WIDTH = 250;
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

        String defaultPassCode = store.getDefaultPassCode();

        QRCodeWriter qrWriter = new QRCodeWriter();
        BitMatrix bitMatrix;
        try {
            bitMatrix = qrWriter.encode(defaultPassCode, BarcodeFormat.QR_CODE, QRCODE_IMAGE_WIDTH, QRCODE_IMAGE_HEIGHT);
        } catch (WriterException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not display QR code.");
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);

        String base64QRCode = new String(Base64.getEncoder().encode(bos.toByteArray()));

        response.setContentType("text/html");

        ServletContext servletContext = getServletContext();
        WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        String path = "/WEB-INF/manager/default_passcode.html";

        ctx.setVariable("defaultPassCode", defaultPassCode);
        ctx.setVariable("base64QRCode", base64QRCode);

        templateEngine.process(path, ctx, response.getWriter());
    }
}
