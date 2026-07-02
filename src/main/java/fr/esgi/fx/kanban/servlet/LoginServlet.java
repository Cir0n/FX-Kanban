package fr.esgi.phil.kanban.servlet;

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

    @Override
    public void init() {
        System.out.println("hello from LongServlet");
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

        // TODO : Remplacer par userService.authenticate(pseudo, password)
        // Faux utilisateurs pour le développement
        boolean authenticated = ("jean.d".equals(pseudo) && "password123".equals(password))
                || ("alice.m".equals(pseudo) && "password123".equals(password))
                || ("tom.l".equals(pseudo) && "password123".equals(password));

        if (authenticated) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", pseudo);
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            context.setVariable("error", "Pseudo ou mot de passe incorrect.");
            context.setVariable("pseudo", pseudo);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
        }
    }
}