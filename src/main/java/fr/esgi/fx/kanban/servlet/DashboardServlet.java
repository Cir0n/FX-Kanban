package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import fr.esgi.fx.kanban.viewmodel.MembreVue;
import fr.esgi.fx.kanban.viewmodel.TableauVue;
import fr.esgi.fx.kanban.viewmodel.VueSupport;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "dashboardServlet", value = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private ITableauService tableauService;
    private IColonneService colonneService;
    private ITacheService tacheService;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        tableauService = ServiceFactory.tableauService();
        colonneService = ServiceFactory.colonneService();
        tacheService = ServiceFactory.tacheService();
    }

    // Les expressions de lien @{/...} de Thymeleaf 3.1 exigent un WebContext.
    private WebContext newContext(HttpServletRequest request, HttpServletResponse response) {
        IWebExchange exchange = application.buildExchange(request, response);
        return new WebContext(exchange);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Accès protégé : il faut être connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String pseudo = (String) session.getAttribute("user");
        Long userId = (Long) session.getAttribute("userId");

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable("userInitiales", VueSupport.initiales(pseudo));
        context.setVariable("tableaux", tableauxDe(userId));

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("dashboard", context, response.getWriter());
    }

    /** Tableaux auxquels l'utilisateur contribue, transformés pour l'affichage. */
    private List<TableauVue> tableauxDe(Long userId) {
        List<TableauVue> vues = new ArrayList<>();
        for (Tableau tableau : tableauService.findAllByContributeur(userId)) {
            vues.add(TableauVue.builder()
                    .id(tableau.getId())
                    .name(tableau.getName())
                    .couleur(VueSupport.couleurTableau(tableau.getId()))
                    .nbTaches(compterTaches(tableau.getId()))
                    .membres(membres(tableau.getId()))
                    .build());
        }
        return vues;
    }

    private int compterTaches(Long tableauId) {
        int total = 0;
        for (Colonne colonne : colonneService.findByTableauId(tableauId)) {
            total += tacheService.findByColonneId(colonne.getId()).size();
        }
        return total;
    }

    private List<MembreVue> membres(Long tableauId) {
        List<MembreVue> membres = new ArrayList<>();
        for (Utilisateur contributeur : tableauService.findContributeurs(tableauId)) {
            membres.add(MembreVue.builder()
                    .initiales(VueSupport.initiales(contributeur.getPseudo()))
                    .couleur(VueSupport.couleurAvatar(contributeur.getPseudo()))
                    .build());
        }
        return membres;
    }
}
