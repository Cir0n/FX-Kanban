package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "boardInviteServlet", value = {"/board/invite"})
public class BoardInviteServlet extends HttpServlet {

    private ITableauService tableauService;

    @Override
    public void init() {
        tableauService = ServiceFactory.tableauService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long boardId = parseLong(request.getParameter("boardId"), 1L);
        String pseudo = request.getParameter("pseudo");
        String trimmedPseudo = pseudo == null ? "" : pseudo.trim();

        if (trimmedPseudo.isEmpty()) {
            redirectWithError(request, response, boardId, "Le pseudo est requis.");
            return;
        }

        try {
            tableauService.inviterContributeur(boardId, trimmedPseudo);
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&invited=1");
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, boardId, e.getMessage());
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
