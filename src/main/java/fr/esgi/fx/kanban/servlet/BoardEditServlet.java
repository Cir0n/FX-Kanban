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

@WebServlet(name = "boardEditServlet", value = {"/board/edit"})
public class BoardEditServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(BoardEditServlet.class);

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
        String name = request.getParameter("name");

        if (boardId == 0L) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            tableauService.renommer(boardId, name == null ? null : name.trim());
            response.sendRedirect(request.getContextPath() + "/dashboard?updated=1");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Échec du renommage du tableau id={} : {}", boardId, e.getMessage());
            String encoded = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
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
