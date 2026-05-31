package fr.esgi.phil.kanban.servlet;
import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
@WebServlet(name = "helloServlet", value = {"/index", "/"})
public class HelloServlet extends HttpServlet {
    private String message;
    private TemplateEngine templateEngine = null;
    public void init() {
        System.out.println("Initialisation de la servlet HelloServlet");
        message = "Hello Kanban";
        // On récupère le moteur de template dans le contexte des servlets
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }
    // la méthode doGet ci-dessous sera invoquée par Tomcat
    // Tomcat invoque cette méthode car elle prend en charge toutes
    // les requêtes HTTP utilisant la méthode GET vers l'url index et vers l’url /
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // On crée un context Thymeleaf qui va accueillir les objets Java
        // qui seront envoyés à la vue Thymeleaf
        response.setContentType("text/html;charset=UTF-8");
        Context context = new Context();
        templateEngine.process("hello", context, response.getWriter());
    }
    public void destroy() {
    }
}