package fr.esgi.fx.kanban.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "boardNewServlet", value = {"/board/new"})
public class BoardNewServlet extends HttpServlet {

    // Palette proposée pour la barre de couleur du tableau (alignée sur le design system).
    private static final List<String> COULEURS = List.of(
            "#378ADD", "#1D9E75", "#BA7517", "#E24B4A", "#635BFF", "#475569");

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    // Les expressions de lien @{/...} de Thymeleaf 3.1 exigent un WebContext.
    private WebContext newContext(HttpServletRequest request, HttpServletResponse response) {
        IWebExchange exchange = application.buildExchange(request, response);
        return new WebContext(exchange);
    }

    private boolean requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireLogin(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String pseudo = (String) session.getAttribute("user");

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable("userInitiales", initiales(pseudo));
        context.setVariable("couleurs", COULEURS);
        context.setVariable("couleur", COULEURS.get(0));

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("board-new", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireLogin(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String pseudo = (String) session.getAttribute("user");

        String name = request.getParameter("name");
        String couleur = request.getParameter("couleur");

        WebContext context = newContext(request, response);
        boolean hasError = false;

        // Validation (alignée avec la validation client de kanban.js)
        if (name == null || name.trim().isEmpty()) {
            context.setVariable("nameError", "Le nom du tableau est requis.");
            hasError = true;
        } else if (name.trim().length() > 60) {
            context.setVariable("nameError", "Le nom ne doit pas dépasser 60 caractères.");
            hasError = true;
        }

        // La couleur doit faire partie de la palette proposée.
        if (couleur == null || !COULEURS.contains(couleur)) {
            couleur = COULEURS.get(0);
        }

        if (hasError) {
            context.setVariable("user", pseudo);
            context.setVariable("userInitiales", initiales(pseudo));
            context.setVariable("couleurs", COULEURS);
            context.setVariable("couleur", couleur);
            context.setVariable("name", name);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("board-new", context, response.getWriter());
            return;
        }

        // TODO : Remplacer par tableauService.create(pseudo, name, couleur)
        //  puis rediriger vers /board?id=<idCréé>.
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    /** "jean.d" -> "JD", "alice" -> "A". */
    private String initiales(String s) {
        if (s == null || s.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[.\\s_-]+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }
}
