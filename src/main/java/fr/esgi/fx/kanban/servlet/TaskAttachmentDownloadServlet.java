package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.service.IPieceJointeService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "taskAttachmentDownloadServlet", value = {"/task/attachment"})
public class TaskAttachmentDownloadServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(TaskAttachmentDownloadServlet.class);

    private IPieceJointeService pieceJointeService;

    @Override
    public void init() {
        pieceJointeService = ServiceFactory.pieceJointeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long id = parseLong(request.getParameter("id"), 0L);
        if (id == 0L) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        PieceJointe pieceJointe;
        try {
            pieceJointe = pieceJointeService.findById(id);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Pièce jointe id={} introuvable", id);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String nomEncode = URLEncoder.encode(pieceJointe.getNomFichier(), StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType(pieceJointe.getMimeType());
        response.setContentLengthLong(pieceJointe.getContenu().length);
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + nomEncode);
        response.getOutputStream().write(pieceJointe.getContenu());
    }

    private long parseLong(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
