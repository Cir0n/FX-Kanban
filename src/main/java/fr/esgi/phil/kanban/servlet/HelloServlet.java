package fr.esgi.phil.kanban.servlet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Date;

/**
 * Le contr�leur traite des requ�tes HTTP
 */
@WebServlet(name = "helloServlet", value = {"/index"}, loadOnStartup = 1)
public class HelloServlet extends HttpServlet {

    private TemplateEngine templateEngine = null;

    public void init() {
        IO.println("Initialisation de la servlet HelloServlet");

        // On r�cup�re le moteur de template dans le contexte des servlets
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
        Context context= new Context();
        context.setVariable("name", "Philippe");
        templateEngine.process("login", context, response.getWriter());
    }



    // la m�thode doGet ci-dessous sera invoqu�e par Tomcat
    // Tomcat invoque cette m�thode car elle prend en charge toutes
    // les requ�tes HTTP utilisant la m�thode GET vers l'url index
    //public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //    System.out.println(new Date() + " requete recue");
        // On cr�e un context Thymeleaf qui va accueillir des objets Java
        // qui seront envoy�s � la vue Thymeleaf
    //    Context context = new Context();

        // On ajoute dans le contexte une variable message qui contient "Il fait beau !"
    //    context.setVariable("message", "Il fait beau !");

        // On ajoute dans le contexte une variable articles qui est une liste de strings
    //    context.setVariable("articles", articles);

        // On invoque la m�thode process qui formule la r�ponse qui sera renvoy�e au navigateur
    //    templateEngine.process("hello", context, response.getWriter());
    //}

    // la m�thode doPost ci-dessous sera invoqu�e par Tomcat
    // Tomcat invoque cette m�thode car elle prend en charge toutes
    // les requ�tes HTTP utilisant la m�thode POST vers l'url index

}