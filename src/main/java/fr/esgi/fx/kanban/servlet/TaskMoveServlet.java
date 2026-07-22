package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Déplace une tâche vers une autre colonne (drag & drop du board).
 * <p>
 * Endpoint appelé en fetch/AJAX par board.js : il ne renvoie pas de page,
 * seulement un code HTTP (204 = succès, 4xx = erreur). Le déplacement dans
 * l'affichage est fait côté client de façon optimiste.
 */
@WebServlet(name = "taskMoveServlet", value = {"/task/move"})
public class TaskMoveServlet extends HttpServlet {

    private ITacheService tacheService;

    @Override
    public void init() {
        tacheService = ServiceFactory.tacheService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        Long taskId = parseLong(request.getParameter("taskId"));
        Long colonneId = parseLong(request.getParameter("colonneId"));

        if (taskId == null || colonneId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "taskId et colonneId sont requis");
            return;
        }

        try {
            tacheService.deplacer(taskId, colonneId, userId);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    private Long parseLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
