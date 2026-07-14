package fr.esgi.fx.kanban.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import fr.esgi.fx.kanban.configuration.StripeConfiguration;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.IStripeService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ServiceFactory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.BufferedReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servlet exposant les endpoints de paiement Stripe.
 * <p>
 * POST /stripe/checkout — crée une session Checkout et renvoie l'URL de paiement.
 * <p>
 * Corps JSON attendu :
 * <pre>
 * {
 *   "amount": 1500,
 *   "currency": "eur",
 *   "productName": "Abonnement Premium"
 * }
 * </pre>
 */
@WebServlet(name = "StripeServlet", value = {"/stripe/checkout"})
public class StripeServlet extends HttpServlet {

    // Colonnes créées automatiquement à l'ouverture d'un nouveau tableau.
    private static final List<String> COLONNES_PAR_DEFAUT =
            List.of("À faire", "En cours", "En revue", "Terminé");

    private static final Gson gson = new Gson();
    private IStripeService stripeService;
    private ITableauService tableauService;
    private IColonneService colonneService;

    @Override
    public void init() {
        stripeService = (IStripeService) getServletContext().getAttribute(StripeConfiguration.STRIPE_SERVICE_CONTEXT_KEY);
        tableauService = ServiceFactory.tableauService();
        colonneService = ServiceFactory.colonneService();
    }

    /**
     * POST /stripe/checkout
     * Crée une session Stripe Checkout et renvoie l'URL de redirection.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (stripeService == null) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Le service Stripe n'est pas disponible.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        try {
            // Lecture du corps JSON de la requête
            String body;
            try (BufferedReader reader = request.getReader()) {
                body = reader.lines().collect(Collectors.joining());
            }
            JsonObject json = gson.fromJson(body, JsonObject.class);

            long amount = json.get("amount").getAsLong();
            String currency = json.has("currency") ? json.get("currency").getAsString() : "eur";
            String productName = json.has("productName") ? json.get("productName").getAsString() : "Kanban Premium";

            // Construction des URLs de callback
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath();
            String successUrl = baseUrl + "/stripe/checkout?status=success";
            String cancelUrl = baseUrl + "/stripe/checkout?status=cancel";

            // Création de la session Checkout
            Session session = stripeService.createCheckoutSession(amount, currency, productName, successUrl, cancelUrl);

            // Réponse JSON avec l'URL et l'id de la session
            JsonObject result = new JsonObject();
            result.addProperty("sessionId", session.getId());
            result.addProperty("url", session.getUrl());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (StripeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }

    /**
     * GET /stripe/checkout?status=success|cancel
     * Page de retour après le paiement Stripe.
     * - success : redirection auto vers /dashboard après 3 secondes
     * - cancel : affichage d'un lien vers /dashboard
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String status = request.getParameter("status");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String contextPath = request.getContextPath();

        if ("success".equals(status)) {
            HttpSession session = request.getSession(false);
            String pendingName = session == null ? null : (String) session.getAttribute(BoardNewServlet.SESSION_PENDING_NAME);

            if (session != null && pendingName != null) {
                Long userId = (Long) session.getAttribute("userId");
                String pseudo = (String) session.getAttribute("user");

                // Le paiement est confirmé : on crée le tableau, rattache le créateur
                // comme contributeur, puis génère les colonnes par défaut. La couleur
                // n'est pas persistée (le modèle Tableau n'a pas ce champ) : elle est
                // dérivée de l'id à l'affichage.
                Tableau tableau = tableauService.creer(pendingName, userId);
                tableauService.inviterContributeur(tableau.getId(), pseudo);
                int position = 0;
                for (String nomColonne : COLONNES_PAR_DEFAUT) {
                    colonneService.creer(nomColonne, position++, tableau.getId());
                }

                session.removeAttribute(BoardNewServlet.SESSION_PENDING_NAME);
                session.removeAttribute(BoardNewServlet.SESSION_PENDING_COULEUR);

                response.sendRedirect(contextPath + "/board?id=" + tableau.getId());
                return;
            }

            String dashboardUrl = contextPath + "/dashboard";
            response.getWriter().write(
                    "<html><head>" +
                    "<meta http-equiv=\"refresh\" content=\"3; url=" + dashboardUrl + "\" />" +
                    "</head><body>" +
                    "<h1>Paiement réussi !</h1>" +
                    "<p>Merci pour votre achat. Redirection en cours...</p>" +
                    "</body></html>"
            );
        } else if ("cancel".equals(status)) {
            String dashboardUrl = contextPath + "/dashboard";
            response.getWriter().write(
                    "<html><body>" +
                    "<h1>Paiement annulé</h1>" +
                    "<p>Vous avez annulé le paiement.</p>" +
                    "<a href=\"" + dashboardUrl + "\">Retour au tableau de bord</a>" +
                    "</body></html>"
            );
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("<h1>Requête invalide</h1>");
        }
    }
}
