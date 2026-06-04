package fr.esgi.phil.kanban.servlet;

import java.io.IOException;

import fr.esgi.phil.kanban.model.User;
import fr.esgi.phil.kanban.repository.UserRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@WebServlet(name = "SigninServlet", value = {"/signIn"})
public class SigninServlet extends HttpServlet {

    private TemplateEngine templateEngine = null;
    private final UserRepository userRepository = UserRepository.getInstance();

    @Override
    public void init() {
        System.out.println("Initialisation de la servlet SigninServlet");
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    /**
     * GET /signIn — Affiche le formulaire d'inscription.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Context context = new Context();
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("signin", context, response.getWriter());
    }

    /**
     * POST /signIn — Traite le formulaire d'inscription.
     * Valide les champs, vérifie que le pseudo n'existe pas déjà, puis crée le compte.
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Context context = new Context();
        // Conserver les valeurs saisies en cas d'erreur
        context.setVariable("name", name);
        context.setVariable("email", email);

        // Validation : champs obligatoires
        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()
                || confirmPassword == null || confirmPassword.isBlank()) {
            context.setVariable("error", "Veuillez remplir tous les champs.");
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("signin", context, response.getWriter());
            return;
        }

        // Validation : longueur du mot de passe (min 8 caractères)
        if (password.length() < 8) {
            context.setVariable("error", "Le mot de passe doit contenir au moins 8 caractères.");
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("signin", context, response.getWriter());
            return;
        }

        // Validation : confirmation du mot de passe
        if (!password.equals(confirmPassword)) {
            context.setVariable("error", "Les mots de passe ne correspondent pas.");
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("signin", context, response.getWriter());
            return;
        }

        // Validation : pseudo déjà utilisé
        if (userRepository.existsByName(name.trim())) {
            context.setVariable("error", "Ce pseudo est déjà utilisé.");
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("signin", context, response.getWriter());
            return;
        }

        // Création de l'utilisateur
        User user = User.builder()
                .name(name.trim())
                .email(email.trim())
                .password(password) // TODO: hasher le mot de passe (bcrypt)
                .build();

        userRepository.save(user);

        // Redirection vers la page de connexion avec message de succès
        response.sendRedirect(request.getContextPath() + "/logIn?success=1");
    }

    @Override
    public void destroy() {
    }
}