package fr.esgi.fx.kanban.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@WebServlet(name = "logoutServlet", value = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            LOGGER.info("Déconnexion : {} (id={})", session.getAttribute("user"), session.getAttribute("userId"));
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
