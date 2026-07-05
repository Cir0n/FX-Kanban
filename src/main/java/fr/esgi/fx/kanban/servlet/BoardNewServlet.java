package fr.esgi.fx.kanban.servlet;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import fr.esgi.fx.kanban.configuration.StripeConfiguration;
import fr.esgi.fx.kanban.service.IStripeService;
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
    private static final long BOARD_CREATION_PRICE_CENTS = 500L;
    private static final String BOARD_CREATION_CURRENCY = "eur";
    private static final String BOARD_NAME_PATTERN = "^[a-zA-Z0-9]+$";

    private static final String TEMPLATE_BOARD_NEW    = "board-new";
    private static final String CONTENT_TYPE_HTML     = "text/html;charset=UTF-8";
    private static final String VAR_USER_INITIALES    = "userInitiales";
    private static final String VAR_COULEURS          = "couleurs";
    private static final String VAR_COULEUR           = "couleur";
    private static final String VAR_NAME_ERROR        = "nameError";

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private IStripeService stripeService;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        stripeService = (IStripeService) getServletContext().getAttribute(StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY);
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
        context.setVariable(VAR_USER_INITIALES, initiales(pseudo));
        context.setVariable(VAR_COULEURS, COULEURS);
        context.setVariable(VAR_COULEUR, COULEURS.getFirst());

        response.setContentType(CONTENT_TYPE_HTML);
        templateEngine.process(TEMPLATE_BOARD_NEW, context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireLogin(request, response)) {
            return;
        }

        HttpSession session = request.getSession(false);
        String pseudo = (String) session.getAttribute("user");

        String name = request.getParameter("name");
        String couleur = request.getParameter(VAR_COULEUR);
        String trimmedName = name == null ? "" : name.trim();

        WebContext context = newContext(request, response);
        boolean hasError = false;

        // Validation (alignée avec la validation client de kanban.js)
        if (trimmedName.isEmpty()) {
            context.setVariable(VAR_NAME_ERROR, "Le nom du tableau est requis.");
            hasError = true;
        } else if (trimmedName.length() > 60) {
            context.setVariable(VAR_NAME_ERROR, "Le nom ne doit pas dépasser 60 caractères.");
            hasError = true;
        } else if (!trimmedName.matches(BOARD_NAME_PATTERN)) {
            context.setVariable(VAR_NAME_ERROR, "Le nom doit contenir uniquement des caractères alphanumériques.");
            hasError = true;
        }

        // La couleur doit faire partie de la palette proposée.
        if (couleur == null || !COULEURS.contains(couleur)) {
            couleur = COULEURS.getFirst();
        }

        if (hasError) {
            context.setVariable("user", pseudo);
            context.setVariable(VAR_USER_INITIALES, initiales(pseudo));
            context.setVariable(VAR_COULEURS, COULEURS);
            context.setVariable(VAR_COULEUR, couleur);
            context.setVariable("name", name);
            response.setContentType(CONTENT_TYPE_HTML);
            templateEngine.process(TEMPLATE_BOARD_NEW, context, response.getWriter());
            return;
        }

        if (stripeService == null) {
            context.setVariable("error", "Le service de paiement est indisponible pour le moment.");
            context.setVariable("user", pseudo);
            context.setVariable(VAR_USER_INITIALES, initiales(pseudo));
            context.setVariable(VAR_COULEURS, COULEURS);
            context.setVariable(VAR_COULEUR, couleur);
            context.setVariable("name", name);
            response.setContentType(CONTENT_TYPE_HTML);
            templateEngine.process(TEMPLATE_BOARD_NEW, context, response.getWriter());
            return;
        }

        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath();
            String successUrl = baseUrl + "/stripe/checkout?status=success";
            String cancelUrl = baseUrl + "/stripe/checkout?status=cancel";

            Session checkoutSession = stripeService.createCheckoutSession(
                    BOARD_CREATION_PRICE_CENTS,
                    BOARD_CREATION_CURRENCY,
                    "Creation d'un tableau Kanban",
                    successUrl,
                    cancelUrl
            );

            response.sendRedirect(checkoutSession.getUrl());
        } catch (StripeException e) {
            getServletContext().log("Erreur lors de la creation de la session Stripe", e);
            context.setVariable("error", "Impossible d'initialiser le paiement. Veuillez reessayer.");
            context.setVariable("user", pseudo);
            context.setVariable(VAR_USER_INITIALES, initiales(pseudo));
            context.setVariable(VAR_COULEURS, COULEURS);
            context.setVariable(VAR_COULEUR, couleur);
            context.setVariable("name", name);
            response.setContentType(CONTENT_TYPE_HTML);
            templateEngine.process(TEMPLATE_BOARD_NEW, context, response.getWriter());
        }

        // TODO : Remplacer par tableauService.create(pseudo, name, couleur)
        //  puis rediriger vers /board?id=<idCréé>.
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
        return sb.isEmpty() ? "?" : sb.toString();
    }
}
