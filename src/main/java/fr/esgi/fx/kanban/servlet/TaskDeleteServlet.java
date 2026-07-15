package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "taskDeleteServlet", value = {"/task/delete"})
public class TaskDeleteServlet extends HttpServlet {

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

        long boardId = parseLong(request.getParameter("boardId"), 1L);
        long taskId = parseLong(request.getParameter("taskId"), 0L);

        if (taskId == 0L) {
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId);
            return;
        }

        try {
            tacheService.supprimer(taskId);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&deleted=1");
        } catch (RuntimeException e) {
            String encoded = URLEncoder.encode(
                    "Impossible de supprimer la tâche pour le moment.", StandardCharsets.UTF_8);
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
