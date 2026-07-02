package fr.esgi.fx.kanban.configuration;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
@WebListener
public class ThymeleafConfiguration implements ServletContextListener {
    private JakartaServletWebApplication application;
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initialisation Thymeleaf");
        application = JakartaServletWebApplication.buildApplication(sce.getServletContext());
        TemplateEngine templateEngine = new TemplateEngine();
        WebApplicationTemplateResolver templateResolver = new
                WebApplicationTemplateResolver(application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        // En développement : cache désactivé pour que les modifications de templates
        // soient prises en compte sans redémarrer le serveur.
        // En production, repasser à setCacheable(true) (+ un TTL) pour les performances.
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);
        sce.getServletContext().setAttribute("templateEngine", templateEngine);
    }
}