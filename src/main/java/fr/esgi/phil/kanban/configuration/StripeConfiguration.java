package fr.esgi.phil.kanban.configuration;

import fr.esgi.phil.kanban.stripe.StripeService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Listener qui initialise le service Stripe au démarrage de l'application.
 * <p>
 * La clé API est lue depuis :
 * <ol>
 *   <li>La variable d'environnement <code>STRIPE_API_KEY</code></li>
 *   <li>Ou la propriété système <code>stripe.api.key</code></li>
 * </ol>
 */
@WebListener
public class StripeConfiguration implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String apiKey = System.getenv("STRIPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("stripe.api.key");
        }
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("[STRIPE] ⚠ Aucune clé API Stripe configurée. "
                    + "Définissez la variable d'environnement STRIPE_API_KEY "
                    + "ou la propriété système -Dstripe.api.key=sk_test_...");
            return;
        }

        StripeService.init(apiKey);
        System.out.println("[STRIPE] Service Stripe initialisé avec succès.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à nettoyer
    }
}

