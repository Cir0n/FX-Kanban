package fr.esgi.phil.kanban.servlet;

import java.io.IOException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "TaskServlet", value = {"/task"})
public class TaskServlet extends HttpServlet {
    private String message;
    private TemplateEngine templateEngine = null;

    @Override
    public void init() {
        System.out.println("Initialisation de la servlet TaskServlet");
        message = "Hello Kanban";
        // On récupère le moteur de template dans le contexte des servlets
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }

    // la méthode doGet ci-dessous sera invoquée par Tomcat
    // Tomcat invoque cette méthode car elle prend en charge toutes
    // les requêtes HTTP utilisant la méthode GET vers l'url signIn
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // On crée un context Thymeleaf qui va accueillir les objets Java
        // qui seront envoyés à la vue Thymeleaf
        Context context = new Context();
        // On invoque la méthode process qui formule la réponse qui sera renvoyée au navigateur
        templateEngine.process("hello", context, response.getWriter());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    }

    @Override
    public void destroy() {
    }
}