package fr.esgi.fx.kanban.configuration;

import fr.esgi.fx.kanban.service.IStripeService;
import fr.esgi.fx.kanban.service.implementation.StripeServiceImpl;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger(StripeConfiguration.class);
    public static final String STRIPE_SERVICE_CONTEXT_KEY = "stripeService";
    private static final String STRIPE_API_KEY_ENV = "STRIPE_API_KEY";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // ignoreIfMissing : en déploiement (Tomcat) il n'y a pas de .env, on ne doit
        // pas faire échouer le démarrage de l'application pour autant.
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();

        String apiKey = dotenv.get(STRIPE_API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("stripe.api.key");
        }
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.warn("Aucune clé API Stripe configurée. "
                    + "Définissez la variable STRIPE_API_KEY dans .env, "
                    + "ou la propriété système -Dstripe.api.key=sk_test_...");
            return;
        }

        IStripeService stripeService = new StripeServiceImpl(apiKey);
        sce.getServletContext().setAttribute(STRIPE_SERVICE_CONTEXT_KEY, stripeService);
        LOGGER.info("Service Stripe initialisé avec succès.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à nettoyer
    }
}
