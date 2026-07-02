package fr.esgi.fx.kanban.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;



/**
 * Service centralisé pour toutes les interactions avec l'API Stripe.
 * <p>
 * Utilisation :
 * <pre>
 *     StripeService stripeService = StripeService.getInstance();
 *     Session session = stripeService.createCheckoutSession(1500, "eur", "Abonnement Kanban Premium",
 *         "https://monsite.com/success", "https://monsite.com/cancel");
 * </pre>
 */
public class StripeService {

    private static StripeService instance;

    private StripeService(String apiKey) {
        Stripe.apiKey = apiKey;
    }

    /**
     * Initialise le singleton avec la clé API Stripe.
     * À appeler une seule fois au démarrage de l'application.
     *
     * @param apiKey clé secrète Stripe (sk_test_... ou sk_live_...)
     * @return l'instance unique de StripeService
     */
    public static synchronized StripeService init(String apiKey) {
        if (instance == null) {
            instance = new StripeService(apiKey);
        }
        return instance;
    }

    /**
     * Retourne l'instance existante du service.
     *
     * @return l'instance unique de StripeService
     * @throws IllegalStateException si le service n'a pas été initialisé
     */
    public static StripeService getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "StripeService n'a pas été initialisé. Appelez StripeService.init(apiKey) d'abord.");
        }
        return instance;
    }

    // ========================
    // Checkout Session
    // ========================

    /**
     * Crée une session Stripe Checkout (page de paiement hébergée par Stripe).
     *
     * @param amountInCents montant en centimes (ex: 1500 = 15,00 €)
     * @param currency      devise ISO (ex: "eur", "usd")
     * @param productName   nom du produit affiché au client
     * @param successUrl    URL de redirection après paiement réussi
     * @param cancelUrl     URL de redirection si le client annule
     * @return la Session Stripe créée
     * @throws StripeException en cas d'erreur API Stripe
     */
    public Session createCheckoutSession(long amountInCents, String currency, String productName,
                                         String successUrl, String cancelUrl) throws StripeException {

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(productName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        return Session.create(params);
    }

    // ========================
    // Payment Intent
    // ========================

    /**
     * Crée un PaymentIntent pour un paiement côté serveur (intégration custom).
     *
     * @param amountInCents montant en centimes
     * @param currency      devise ISO
     * @param description   description du paiement
     * @return le PaymentIntent Stripe créé
     * @throws StripeException en cas d'erreur API Stripe
     */
    public PaymentIntent createPaymentIntent(long amountInCents, String currency,
                                             String description) throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(currency)
                .setDescription(description)
                .addPaymentMethodType("card")
                .build();

        return PaymentIntent.create(params);
    }

    /**
     * Récupère un PaymentIntent existant par son identifiant.
     *
     * @param paymentIntentId identifiant du PaymentIntent (pi_...)
     * @return le PaymentIntent Stripe
     * @throws StripeException en cas d'erreur API Stripe
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    // ========================
    // Customer
    // ========================

    /**
     * Crée un client Stripe.
     *
     * @param name  nom affiché du client
     * @param email adresse e-mail du client
     * @return le Customer Stripe créé
     * @throws StripeException en cas d'erreur API Stripe
     */
    public Customer createCustomer(String name, String email) throws StripeException {

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(name)
                .setEmail(email)
                .build();

        return Customer.create(params);
    }
}

