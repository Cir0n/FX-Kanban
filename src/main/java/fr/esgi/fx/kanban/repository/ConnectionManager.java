package fr.esgi.fx.kanban.repository;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final String DEFAULT_URL =
            "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE;INIT=RUNSCRIPT FROM 'classpath:import.sql'";

    private static final String DEFAULT_USER =
            "sa";
    private static final String DEFAULT_PASSWORD =
            "";

    private static final Dotenv DOTENV =
            Dotenv.configure().ignoreIfMissing().load();

    private static final Properties DB_PROPERTIES =
            loadDbProperties();

    private static final String URL =
            resolve("DB_URL", "db.url", DEFAULT_URL);
    private static final String USER =
            resolve("DB_USER", "db.user", DEFAULT_USER);
    private static final String PASSWORD =
            resolve("DB_PASSWORD", "db.password", DEFAULT_PASSWORD);

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

    public static Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(URL,
                USER, PASSWORD);
    }
}
