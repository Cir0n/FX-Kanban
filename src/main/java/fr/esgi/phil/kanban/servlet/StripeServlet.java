package fr.esgi.phil.kanban.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import fr.esgi.phil.kanban.stripe.StripeService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.BufferedReader;
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

    private static final Gson gson = new Gson();
    private StripeService stripeService;

    @Override
    public void init() {
        System.out.println("Initialisation de la servlet StripeServlet");
        stripeService = StripeService.getInstance();
    }

    /**
     * POST /stripe/checkout
     * Crée une session Stripe Checkout et renvoie l'URL de redirection.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String status = request.getParameter("status");
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        if ("success".equals(status)) {
            response.getWriter().write("<h1>Paiement réussi !</h1><p>Merci pour votre achat.</p>");
        } else if ("cancel".equals(status)) {
            response.getWriter().write("<h1>Paiement annulé</h1><p>Vous avez annulé le paiement.</p>");
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("<h1>Requête invalide</h1>");
        }
    }
}
