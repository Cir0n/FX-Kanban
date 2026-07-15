package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import fr.esgi.fx.kanban.viewmodel.VueSupport;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "taskEditServlet", value = {"/task/edit"})
public class TaskEditServlet extends HttpServlet {

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
        long taskId = parseLong(request.getParameter("taskId"), 0L);

        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String type = request.getParameter("type");
        String assigneeRaw = request.getParameter("assignee");

        if (taskId == 0L || name == null || name.trim().isEmpty()) {
            redirectWithError(request, response, boardId, "Le nom de la tâche est requis.");
            return;
        }

        Long typeId = VueSupport.typeIdDepuisClasse(type);
        Long assigneeId = (assigneeRaw == null || assigneeRaw.isBlank()) ? null : parseLongOrNull(assigneeRaw);

        try {
            tacheService.modifier(
                    taskId,
                    name.trim(),
                    description == null ? "" : description.trim(),
                    typeId,
                    assigneeId,
                    userId);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&updated=1");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, boardId, e.getMessage());
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response,
                                    long boardId, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&error=" + encoded);
    }

    private Long parseLongOrNull(String raw) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
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
