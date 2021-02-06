package it.polimi.se2.clup.CLupWeb.controllers.manager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import it.polimi.se2.clup.CLupEJB.entities.OpeningHourEntity;
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
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.*;

@WebServlet(name = "ManagerStoreInfoServlet", value = "/dashboard/storeinfo")
public class StoreInfoServlet extends HttpServlet {

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

        int storeCap = store.getStoreCap();

        // Build a custom map of opening hours for thymeleaf templating.
        // The map is composed of the name of the day (e.g. Monday) with all its opening hours.
        // Indeed one day could have more than one opening hour.
        // (e.g.) Monday: 08:00 - 12:00, 14:00 - 18:00.
        List<OpeningHourEntity> openingHourList = store.getOpeningHours();
        Map<String, List<OpeningHourEntity>> openingHourMap = new LinkedHashMap<>();

        for (OpeningHourEntity oh : openingHourList) {
            String dayName = DayOfWeek.of(oh.getWeekDay()).getDisplayName(TextStyle.FULL, Locale.getDefault());

            if (!openingHourMap.containsKey(dayName)) {
                openingHourMap.put(dayName, new ArrayList<>());
            }
            openingHourMap.get(dayName).add(oh);
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
        String path = "/WEB-INF/manager/store_info.html";

        ctx.setVariable("storeCap", storeCap);
        ctx.setVariable("openingHourMap", openingHourMap);
        ctx.setVariable("defaultPassCode", defaultPassCode);
        ctx.setVariable("base64QRCode", base64QRCode);

        templateEngine.process(path, ctx, response.getWriter());
    }
}
