package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.IPieceJointeService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@WebServlet(name = "taskAttachmentUploadServlet", value = {"/task/attachment/upload"})
@MultipartConfig(maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class TaskAttachmentUploadServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(TaskAttachmentUploadServlet.class);

    private IPieceJointeService pieceJointeService;

    @Override
    public void init() {
        pieceJointeService = ServiceFactory.pieceJointeService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        long boardId = parseLong(request.getParameter("boardId"), 1L);
        long taskId = parseLong(request.getParameter("taskId"), 0L);

        if (taskId == 0L) {
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId);
            return;
        }

        try {
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0
                    || filePart.getSubmittedFileName() == null || filePart.getSubmittedFileName().isBlank()) {
                redirectWithError(request, response, boardId, "Veuillez sélectionner un fichier.");
                return;
            }

            String nomFichier = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            byte[] contenu = filePart.getInputStream().readAllBytes();

            pieceJointeService.ajouter(nomFichier, filePart.getContentType(), contenu, taskId, userId);
            LOGGER.info("Pièce jointe '{}' ajoutée à la tâche id={} par l'utilisateur id={}", nomFichier, taskId, userId);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&updated=1");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Échec de l'ajout de pièce jointe pour la tâche id={} : {}", taskId, e.getMessage());
            redirectWithError(request, response, boardId, e.getMessage());
        } catch (IllegalStateException e) {
            LOGGER.warn("Fichier trop volumineux pour la tâche id={}", taskId);
            redirectWithError(request, response, boardId, "Le fichier dépasse la taille maximale autorisée (10 Mo).");
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                    long boardId, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&error=" + encoded);
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
