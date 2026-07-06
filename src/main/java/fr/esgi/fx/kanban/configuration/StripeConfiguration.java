package fr.esgi.fx.kanban.configuration;

import fr.esgi.fx.kanban.service.IStripeService;
import fr.esgi.fx.kanban.service.implementation.StripeServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Listener qui initialise le service Stripe au démarrage de l'application.
 * <p>
 * La clé API est lue depuis :
 * <ol>
 *   <li>Le fichier local <code>.env</code> (dev)</li>
 *   <li>Ou la propriété système <code>stripe.api.key</code></li>
 * </ol>
 */
@WebListener
public class StripeConfiguration implements ServletContextListener {

    public static final String STRIPE_SERVICE_CONTEXT_KEY = "stripeService";
    private static final String STRIPE_API_KEY_ENV = "STRIPE_API_KEY";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Dotenv dotenv = Dotenv.load();

        String apiKey = dotenv.get(STRIPE_API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("stripe.api.key");
        }
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[STRIPE] Aucune clé API Stripe configurée. "
                    + "Définissez la variable STRIPE_API_KEY dans .env, "
                    + "ou la propriété système -Dstripe.api.key=sk_test_...");
            return;
        }

        IStripeService stripeService = new StripeServiceImpl(apiKey);
        sce.getServletContext().setAttribute(STRIPE_SERVICE_CONTEXT_KEY, stripeService);
        System.out.println("[STRIPE] Service Stripe initialisé avec succès.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à nettoyer
    }
}
