package fr.esgi.phil.kanban.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet(name = "registerServlet", value = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = newContext(request, response);
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("register", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pseudo = request.getParameter("pseudo");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        WebContext context = newContext(request, response);
        boolean hasError = false;

        // Validation (alignée avec la validation client de kanban.js)
        if (pseudo == null || pseudo.trim().isEmpty()) {
            context.setVariable("pseudoError", "Le pseudo est requis.");
            hasError = true;
        }

        if (email == null || email.trim().isEmpty()) {
            context.setVariable("emailError", "L'email est requis.");
            hasError = true;
        } else if (!EMAIL_REGEX.matcher(email.trim()).matches()) {
            context.setVariable("emailError", "Format d'email invalide.");
            hasError = true;
        }

        if (password == null || password.length() < 8) {
            context.setVariable("passwordError", "Le mot de passe doit contenir au moins 8 caractères.");
            hasError = true;
        }

        if (confirmPassword == null || !confirmPassword.equals(password)) {
            context.setVariable("confirmError", "Les mots de passe ne correspondent pas.");
            hasError = true;
        }

        if (hasError) {
            // On réaffiche le formulaire en repeuplant les champs non sensibles
            context.setVariable("pseudo", pseudo);
            context.setVariable("email", email);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("register", context, response.getWriter());
            return;
        }

        // TODO : Remplacer par userService.register(pseudo, email, password)
        //  - vérifier l'unicité du pseudo / email
        //  - hacher le mot de passe avant persistance
        boolean pseudoAlreadyUsed = "jean.d".equals(pseudo)
                || "alice.m".equals(pseudo)
                || "tom.l".equals(pseudo);

        if (pseudoAlreadyUsed) {
            context.setVariable("pseudoError", "Ce pseudo est déjà utilisé.");
            context.setVariable("pseudo", pseudo);
            context.setVariable("email", email);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("register", context, response.getWriter());
            return;
        }

        // Inscription réussie : on redirige vers la connexion avec un message de succès
        response.sendRedirect(request.getContextPath() + "/login?success=1");
    }
}
