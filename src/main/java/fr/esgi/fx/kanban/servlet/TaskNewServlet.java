package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import fr.esgi.fx.kanban.viewmodel.VueSupport;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet(name = "taskNewServlet", value = {"/task/new"})
public class TaskNewServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(TaskNewServlet.class);

    private ITacheService tacheService;

    @Override
    public void init() {
        tacheService = ServiceFactory.tacheService();
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
        long colonneId = parseLong(request.getParameter("colonneId"), 0L);
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String type = request.getParameter("type");

        // Le nom et la colonne cible sont requis (la validation client de kanban.js
        // empêche déjà l'envoi d'un nom vide).
        if (name == null || name.trim().isEmpty() || colonneId == 0L) {
            LOGGER.warn("Création de tâche refusée pour le tableau id={} : nom ou colonne manquant", boardId);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId);
            return;
        }

        Long typeId = VueSupport.typeIdDepuisClasse(type);

        // Persistance via la couche service. L'assigné n'est pas transmis ici :
        // le service positionne le créateur, l'assignation se fait ultérieurement.
        tacheService.creer(
                name.trim(),
                description == null ? "" : description.trim(),
                colonneId,
                typeId,
                userId);

        response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&created=1");
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
