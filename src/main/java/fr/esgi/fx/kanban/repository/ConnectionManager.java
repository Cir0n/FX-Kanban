package fr.esgi.fx.kanban.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionManager.class);

    // Le schéma n'est plus initialisé ici : le rejouer à chaque connexion (aucun pool,
    // une connexion par requête) était la cause de la lenteur de l'application. Il est
    // désormais exécuté une seule fois au démarrage par DatabaseConfiguration.
    private static final String URL = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // Sous Tomcat, le driver H2 (WEB-INF/lib) est chargé par le classloader isolé du
    // webapp ; l'auto-enregistrement via ServiceLoader ne l'expose pas à DriverManager
    // (chargé par le classloader système). On force donc son enregistrement ici.
    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.fatal("Driver H2 introuvable sur le classpath", e);
            throw new ExceptionInInitializerError(
                    "Driver H2 introuvable sur le classpath : " + e.getMessage());
        }
    }

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}