package fr.esgi.fx.kanban.servlet;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import fr.esgi.fx.kanban.configuration.StripeConfiguration;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.IStripeService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "boardNewServlet", value = {"/board/new"})
public class BoardNewServlet extends HttpServlet {

    private static final Logger LOGGER = LogManager.getLogger(BoardNewServlet.class);

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
    private static final String VAR_ERROR             = "error";

    // Données du tableau conservées en session le temps du paiement Stripe.
    private static final String PENDING_NAME    = "pendingBoardName";
    private static final String PENDING_COULEUR = "pendingBoardCouleur";

    // Colonnes créées automatiquement à l'ouverture d'un nouveau tableau.
    private static final List<String> COLONNES_PAR_DEFAUT =
            List.of("À faire", "En cours", "En revue", "Terminé");

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private ITableauService tableauService;
    private IColonneService colonneService;
    private IStripeService stripeService;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        tableauService = ServiceFactory.tableauService();
        colonneService = ServiceFactory.colonneService();
        stripeService = (IStripeService) getServletContext()
                .getAttribute(StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY);
    }

    // Les expressions de lien @{/...} de Thymeleaf 3.1 exigent un WebContext.
    private WebContext newContext(HttpServletRequest request, HttpServletResponse response) {
        IWebExchange exchange = application.buildExchange(request, response);
        return new WebContext(exchange);
    }

    private boolean requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
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

        // Retour depuis Stripe Checkout : on finalise (ou on annule) la création.
        String status = request.getParameter("status");
        if ("success".equals(status)) {
            finaliserApresPaiement(request, response);
            return;
        }
        if ("cancel".equals(status)) {
            annulerPaiement(request, response);
            return;
        }

        afficherFormulaire(request, response, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!requireLogin(request, response)) {
            return;
        }

        String name = request.getParameter("name");
        String couleur = request.getParameter(VAR_COULEUR);
        String trimmedName = name == null ? "" : name.trim();

        // La couleur doit faire partie de la palette proposée.
        if (couleur == null || !COULEURS.contains(couleur)) {
            couleur = COULEURS.getFirst();
        }

        // Validation (alignée avec la validation client de kanban.js)
        String nameError = validerNom(trimmedName);
        if (nameError != null) {
            afficherFormulaireErreur(request, response, name, couleur, nameError, null);
            return;
        }

        // Sans service Stripe configuré, la création payante est impossible.
        if (stripeService == null) {
            afficherFormulaireErreur(request, response, name, couleur, null,
                    "Le paiement est indisponible pour le moment. Réessayez plus tard.");
            return;
        }

        HttpSession session = request.getSession();
        // On mémorise le tableau à créer, la création n'aura lieu qu'après paiement.
        session.setAttribute(PENDING_NAME, trimmedName);
        session.setAttribute(PENDING_COULEUR, couleur);

        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath();
            String successUrl = baseUrl + "/board/new?status=success&session_id={CHECKOUT_SESSION_ID}";
            String cancelUrl = baseUrl + "/board/new?status=cancel";

            Session checkout = stripeService.createCheckoutSession(
                    BOARD_CREATION_PRICE_CENTS, BOARD_CREATION_CURRENCY,
                    "Création du tableau « " + trimmedName + " »", successUrl, cancelUrl);

            response.sendRedirect(checkout.getUrl());
        } catch (StripeException e) {
            LOGGER.error("Échec du démarrage du paiement pour la création du tableau '{}'", trimmedName, e);
            afficherFormulaireErreur(request, response, name, couleur, null,
                    "Impossible de démarrer le paiement : " + e.getMessage());
        }
    }

    /** Vérifie le paiement Stripe puis crée réellement le tableau et ses colonnes. */
    private void finaliserApresPaiement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String sessionId = request.getParameter("session_id");
        String name = session == null ? null : (String) session.getAttribute(PENDING_NAME);
        Long userId = session == null ? null : (Long) session.getAttribute("userId");

        // Plus de tableau en attente (ex. rafraîchissement de la page) : rien à créer.
        if (name == null || userId == null || sessionId == null || stripeService == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // On vérifie côté serveur que la session a bien été réglée.
        boolean paye;
        try {
            Session checkout = stripeService.retrieveSession(sessionId);
            paye = checkout != null && "paid".equals(checkout.getPaymentStatus());
        } catch (StripeException e) {
            LOGGER.error("Impossible de vérifier le paiement Stripe id={} pour le tableau '{}' ; " +
                    "traité comme non payé", sessionId, name, e);
            paye = false;
        }

        if (!paye) {
            afficherFormulaireErreur(request, response, name,
                    (String) session.getAttribute(PENDING_COULEUR), null,
                    "Le paiement n'a pas pu être confirmé, le tableau n'a pas été créé.");
            return;
        }

        // Paiement confirmé : on purge l'attente pour éviter une double création.
        session.removeAttribute(PENDING_NAME);
        session.removeAttribute(PENDING_COULEUR);

        Tableau tableau = tableauService.creer(name, userId, sessionId);
        int position = 1;
        for (String colonne : COLONNES_PAR_DEFAUT) {
            colonneService.creer(colonne, position++, tableau.getId());
        }

        response.sendRedirect(request.getContextPath() + "/board?id=" + tableau.getId() + "&created=1");
    }

    /** Paiement annulé : on oublie le tableau en attente et on revient au formulaire. */
    private void annulerPaiement(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        String name = null;
        String couleur = null;
        if (session != null) {
            name = (String) session.getAttribute(PENDING_NAME);
            couleur = (String) session.getAttribute(PENDING_COULEUR);
            session.removeAttribute(PENDING_NAME);
            session.removeAttribute(PENDING_COULEUR);
        }
        afficherFormulaire(request, response, name, couleur,
                "Paiement annulé : le tableau n'a pas été créé.");
    }

    private String validerNom(String trimmedName) {
        if (trimmedName.isEmpty()) {
            return "Le nom du tableau est requis.";
        }
        if (trimmedName.length() > 60) {
            return "Le nom ne doit pas dépasser 60 caractères.";
        }
        if (!trimmedName.matches(BOARD_NAME_PATTERN)) {
            return "Le nom doit contenir uniquement des caractères alphanumériques.";
        }
        return null;
    }

    private void afficherFormulaire(HttpServletRequest request, HttpServletResponse response,
                                    String name, String couleur, String error) throws IOException {
        afficherFormulaireErreur(request, response, name, couleur, null, error);
    }

    private void afficherFormulaireErreur(HttpServletRequest request, HttpServletResponse response,
                                          String name, String couleur,
                                          String nameError, String error) throws IOException {
        HttpSession session = request.getSession(false);
        String pseudo = session == null ? null : (String) session.getAttribute("user");
        if (couleur == null || !COULEURS.contains(couleur)) {
            couleur = COULEURS.getFirst();
        }

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable(VAR_USER_INITIALES, initiales(pseudo));
        context.setVariable(VAR_COULEURS, COULEURS);
        context.setVariable(VAR_COULEUR, couleur);
        context.setVariable("name", name);
        if (nameError != null) {
            context.setVariable(VAR_NAME_ERROR, nameError);
        }
        if (error != null) {
            context.setVariable(VAR_ERROR, error);
        }

        response.setContentType(CONTENT_TYPE_HTML);
        templateEngine.process(TEMPLATE_BOARD_NEW, context, response.getWriter());
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
