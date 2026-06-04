package fr.esgi.phil.kanban.servlet;

import java.io.IOException;
import java.util.Optional;

import fr.esgi.phil.kanban.model.User;
import fr.esgi.phil.kanban.repository.UserRepository;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@WebServlet(name = "LoginServlet", value = {"/logIn", "/"})
public class LoginServlet extends HttpServlet {

    private TemplateEngine templateEngine = null;
    private final UserRepository userRepository = UserRepository.getInstance();

    @Override
    public void init() {
        System.out.println("Initialisation de la servlet LoginServlet");
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    /**
     * GET /logIn — Affiche le formulaire de connexion.
     * Si l'utilisateur est déjà connecté, redirige vers le tableau.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Si déjà connecté, rediriger vers le tableau
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/tableau/1");
            return;
        }

        Context context = new Context();

        // Récupérer un éventuel message de succès (ex: après inscription)
        String success = request.getParameter("success");
        if (success != null) {
            context.setVariable("success", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        }

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("login", context, response.getWriter());
    }

    /**
     * POST /logIn — Traite le formulaire de connexion.
     * Vérifie les identifiants et crée une session HTTP si valides.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String password = request.getParameter("password");

        Context context = new Context();

        // Validation des champs
        if (name == null || name.isBlank() || password == null || password.isBlank()) {
            context.setVariable("error", "Veuillez remplir tous les champs.");
            context.setVariable("name", name);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
            return;
        }

        // Recherche de l'utilisateur
        Optional<User> optionalUser = userRepository.findByName(name.trim());

        if (optionalUser.isEmpty() || !optionalUser.get().getPassword().equals(password)) {
            context.setVariable("error", "Pseudo ou mot de passe incorrect.");
            context.setVariable("name", name);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
            return;
        }

        // Connexion réussie : créer la session
        User user = optionalUser.get();
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Redirection vers le tableau principal
        response.sendRedirect(request.getContextPath() + "/tableau/1");
    }

    @Override
    public void destroy() {
    }
}