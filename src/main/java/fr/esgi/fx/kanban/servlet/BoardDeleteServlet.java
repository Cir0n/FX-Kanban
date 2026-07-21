package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "boardDeleteServlet", value = {"/board/delete"})
public class BoardDeleteServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(BoardDeleteServlet.class);

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

        long boardId = parseLong(request.getParameter("boardId"), 0L);
        if (boardId == 0L) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            tableauService.supprimer(boardId);
            response.sendRedirect(request.getContextPath() + "/dashboard?deleted=1");
        } catch (RuntimeException e) {
            LOGGER.error("Échec de la suppression du tableau id={}", boardId, e);
            String encoded = URLEncoder.encode(
                    "Impossible de supprimer le tableau pour le moment.", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/dashboard?error=" + encoded);
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
