package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IUtilisateurService;
import fr.esgi.fx.kanban.service.ServiceFactory;
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

@WebServlet(name = "loginServlet", value = {"/login"})
public class LoginServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private IUtilisateurService utilisateurService;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        utilisateurService = ServiceFactory.utilisateurService();
    }

    // Les expressions de lien @{/...} de Thymeleaf 3.1 exigent un WebContext.
    private WebContext newContext(HttpServletRequest request, HttpServletResponse response) {
        IWebExchange exchange = application.buildExchange(request, response);
        return new WebContext(exchange);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Si déjà connecté, rediriger vers le dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        WebContext context = newContext(request, response);

        // Message de succès après inscription
        String success = request.getParameter("success");
        if (success != null) {
            context.setVariable("success", "Inscription réussie ! Vous pouvez vous connecter.");
        }

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("login", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pseudo = request.getParameter("pseudo");
        String password = request.getParameter("password");

        WebContext context = newContext(request, response);
        boolean hasError = false;

        // Validation
        if (pseudo == null || pseudo.trim().isEmpty()) {
            context.setVariable("pseudoError", "Le pseudo est requis.");
            hasError = true;
        }

        if (password == null || password.trim().isEmpty()) {
            context.setVariable("passwordError", "Le mot de passe est requis.");
            hasError = true;
        }

        if (hasError) {
            context.setVariable("pseudo", pseudo);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
            return;
        }

        // Authentification via la couche service (mot de passe haché en base).
        try {
            Utilisateur utilisateur = utilisateurService.connecter(pseudo, password);
            HttpSession session = request.getSession(true);
            session.setAttribute("user", utilisateur.getPseudo());
            session.setAttribute("userId", utilisateur.getId());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (IllegalArgumentException e) {
            context.setVariable("error", e.getMessage());
            context.setVariable("pseudo", pseudo);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
        }
    }
}