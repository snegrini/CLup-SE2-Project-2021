package it.polimi.se2.clup.CLupWeb.controllers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "ImageServlet", value = "/images/*")
public class ImageServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String filename = request.getPathInfo().substring(1);
        String uploadLocation = getServletContext().getInitParameter("upload.location");

        if (!Files.exists(Path.of(uploadLocation))) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not find the image folder. Please fix the specified folder in the web.xml file.");
            return;
        }

        File file = new File(uploadLocation, filename);

        response.setHeader("Content-Type", getServletContext().getMimeType(filename));
        response.setHeader("Content-Length", String.valueOf(file.length()));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
