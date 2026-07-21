package fr.esgi.fx.kanban.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@WebListener
public class SeederConfiguration implements ServletContextListener {

    private static final String APP_ENV_KEY = "APP_ENV";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String appEnv = dotenv.get(APP_ENV_KEY);
        if (appEnv == null || appEnv.isBlank()) {
            appEnv = System.getenv(APP_ENV_KEY);
        }
        if (appEnv == null || appEnv.isBlank()) {
            appEnv = readMavenProfileEnv();
        }

        if (!"dev".equals(appEnv) && !"preprod".equals(appEnv)) {
            System.out.println("[SEEDER] APP_ENV='" + appEnv + "' — seeding désactivé (dev/preprod uniquement).");
            return;
        }

        new KanbanSeeder().seed();
    }

    private static String readMavenProfileEnv() {
        try (InputStream stream = SeederConfiguration.class.getResourceAsStream("/db.properties")) {
            if (stream == null) {
                return null;
            }
            Properties properties = new Properties();
            properties.load(stream);
            String value = properties.getProperty("db.env");
            return (value == null || value.isBlank() || value.contains("${")) ? null : value;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à nettoyer
    }
}