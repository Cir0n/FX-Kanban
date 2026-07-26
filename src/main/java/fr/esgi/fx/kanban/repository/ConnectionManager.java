package fr.esgi.fx.kanban.repository;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionManager.class);

    // Le schéma n'est plus initialisé ici : le rejouer à chaque connexion (aucun pool,
    // une connexion par requête) était la cause de la lenteur de l'application. Il est
    // désormais exécuté une seule fois au démarrage par DatabaseConfiguration — l'URL
    // par défaut ne doit donc plus porter de clause INIT=RUNSCRIPT.
    private static final String DEFAULT_URL =
            "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";

    private static final String DEFAULT_USER =
            "sa";
    private static final String DEFAULT_PASSWORD =
            "";

    private static final Dotenv DOTENV =
            Dotenv.configure().ignoreIfMissing().load();

    private static final Properties DB_PROPERTIES =
            loadDbProperties();

    // Résolution en cascade (le premier trouvé gagne) : .env local > variable
    // d'environnement système > propriété système > db.properties filtré par le
    // profil Maven actif (-Pdev/-Ppreprod/-Pprod) > valeur par défaut ci-dessus.
    // Permet de faire pointer chaque profil vers un fichier H2 différent sans
    // toucher au code.
    private static final String URL =
            resolve("DB_URL", "db.url", DEFAULT_URL);
    private static final String USER =
            resolve("DB_USER", "db.user", DEFAULT_USER);
    private static final String PASSWORD =
            resolve("DB_PASSWORD", "db.password", DEFAULT_PASSWORD);

    // Reflète le profil Maven actif (dev/preprod/prod) au moment du build ; n'est
    // pas fiable si un .env local force DB_URL vers un autre fichier (voir resolve()).
    private static final String ENV =
            DB_PROPERTIES.getProperty("db.env", "inconnu");

    // Sous Tomcat, le driver H2 (WEB-INF/lib) est chargé par le classloader isolé du
    // webapp ; l'auto-enregistrement via ServiceLoader ne l'expose pas à DriverManager
    // (chargé par le classloader système). On force donc son enregistrement ici.
    static {
        LOGGER.info("Base de données : environnement Maven={}, url={}", ENV, URL);
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.fatal("Driver H2 introuvable sur le classpath", e);
            throw new ExceptionInInitializerError(
                    "Driver H2 introuvable sur le classpath : " + e.getMessage());
        }
    }

    private ConnectionManager() {}

    private static Properties loadDbProperties() {
        Properties properties = new Properties();
        try (InputStream stream =
                     ConnectionManager.class.getResourceAsStream("/db.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ignored) {
            // Couche optionnelle : une erreur de lecture retombe simplement
            // sur les couches suivantes (variables d'env / constante Java).
        }
        return properties;
    }

    private static String resolve(String envKey,
                                  String propertyKey,
                                  String defaultValue) {
        String value = DOTENV.get(envKey);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.isBlank()) {
            value = System.getProperty(envKey);
        }
        if (value == null || value.isBlank()) {
            String filtered = DB_PROPERTIES.getProperty(propertyKey);
            if (filtered != null && !filtered.isBlank() && !filtered.contains("${")) {
                value = filtered;
            }
        }
        return (value == null || value.isBlank())
                ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
