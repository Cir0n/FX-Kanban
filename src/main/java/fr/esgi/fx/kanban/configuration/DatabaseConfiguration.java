package fr.esgi.fx.kanban.configuration;

import fr.esgi.fx.kanban.repository.ConnectionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Listener qui initialise le schéma de la base au démarrage de l'application.
 * <p>
 * Le script {@code import.sql} (situé sur le classpath) est rejoué une seule fois
 * ici, via {@code RUNSCRIPT}. Auparavant il était exécuté à chaque ouverture de
 * connexion (clause {@code INIT} de l'URL JDBC), ce qui, sans pool de connexions,
 * ralentissait fortement l'application.
 */
@WebListener
public class DatabaseConfiguration implements ServletContextListener {

    private static final String INIT_SCRIPT = "classpath:import.sql";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("RUNSCRIPT FROM '" + INIT_SCRIPT + "'");
            System.out.println("[DB] Schéma initialisé avec succès (" + INIT_SCRIPT + ").");
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Échec de l'initialisation du schéma de la base : " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à nettoyer
    }
}
