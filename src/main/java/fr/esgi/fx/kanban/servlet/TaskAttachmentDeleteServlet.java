package fr.esgi.fx.kanban.servlet;

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

@WebServlet(name = "taskAttachmentDeleteServlet", value = {"/task/attachment/delete"})
public class TaskAttachmentDeleteServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(TaskAttachmentDeleteServlet.class);

    private IPieceJointeService pieceJointeService;

    @Override
    public void init() {
        pieceJointeService = ServiceFactory.pieceJointeService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        long boardId = parseLong(request.getParameter("boardId"), 1L);
        long attachmentId = parseLong(request.getParameter("attachmentId"), 0L);

        if (attachmentId == 0L) {
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId);
            return;
        }

        try {
            pieceJointeService.supprimer(attachmentId, userId);
            LOGGER.info("Pièce jointe id={} supprimée par l'utilisateur id={}", attachmentId, userId);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&updated=1");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Échec de la suppression de la pièce jointe id={} : {}", attachmentId, e.getMessage());
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&error=" + encoded);
        }
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
