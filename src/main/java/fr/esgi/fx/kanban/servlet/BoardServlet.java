package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Commentaire;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.TypeDeTache;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.ICommentaireService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ITypeDeTacheService;
import fr.esgi.fx.kanban.service.IUtilisateurService;
import fr.esgi.fx.kanban.service.ServiceFactory;
import fr.esgi.fx.kanban.viewmodel.ColonneVue;
import fr.esgi.fx.kanban.viewmodel.CommentaireVue;
import fr.esgi.fx.kanban.viewmodel.MembreVue;
import fr.esgi.fx.kanban.viewmodel.TacheVue;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "boardServlet", value = {"/board"})
public class BoardServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private ITableauService tableauService;
    private IColonneService colonneService;
    private ITacheService tacheService;
    private ICommentaireService commentaireService;
    private ITypeDeTacheService typeDeTacheService;
    private IUtilisateurService utilisateurService;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
        tableauService = ServiceFactory.tableauService();
        colonneService = ServiceFactory.colonneService();
        tacheService = ServiceFactory.tacheService();
        commentaireService = ServiceFactory.commentaireService();
        typeDeTacheService = ServiceFactory.typeDeTacheService();
        utilisateurService = ServiceFactory.utilisateurService();
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

        long tableauId = parseId(request.getParameter("id"), 1L);
        String pseudo = (String) session.getAttribute("user");

        // Le tableau doit exister ; sinon retour au dashboard.
        Tableau tableau;
        try {
            tableau = tableauService.findById(tableauId);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // Cache local (id utilisateur -> utilisateur) partagé par les assignés et
        // les auteurs de commentaires, pour éviter des requêtes redondantes.
        Map<Long, Utilisateur> utilisateurs = new HashMap<>();

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable("userInitiales", VueSupport.initiales(pseudo));
        context.setVariable("boardId", tableau.getId());
        context.setVariable("boardName", tableau.getName());
        context.setVariable("boardCouleur", VueSupport.couleurTableau(tableau.getId()));
        context.setVariable("membres", membres(tableau.getId()));
        context.setVariable("colonnes", colonnes(tableau.getId(), utilisateurs));
        context.setVariable("created", "1".equals(request.getParameter("created")));

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("board", context, response.getWriter());
    }

    private List<MembreVue> membres(Long tableauId) {
        List<MembreVue> membres = new ArrayList<>();
        for (Utilisateur contributeur : tableauService.findContributeurs(tableauId)) {
            membres.add(avatar(contributeur));
        }
        return membres;
    }

    private List<ColonneVue> colonnes(Long tableauId, Map<Long, Utilisateur> cache) {
        List<ColonneVue> vues = new ArrayList<>();
        for (Colonne colonne : colonneService.findByTableauId(tableauId)) {
            List<TacheVue> taches = new ArrayList<>();
            for (Tache tache : tacheService.findByColonneId(colonne.getId())) {
                taches.add(mapTache(tache, cache));
            }
            vues.add(ColonneVue.builder()
                    .id(colonne.getId())
                    .name(colonne.getName())
                    .taches(taches)
                    .build());
        }
        return vues;
    }

    private TacheVue mapTache(Tache tache, Map<Long, Utilisateur> cache) {
        String typeLabel = typeDeTacheService.findById(tache.getTypeId())
                .map(TypeDeTache::getName)
                .orElse("Standard");

        return TacheVue.builder()
                .id(tache.getId())
                .name(tache.getName())
                .description(tache.getDescription())
                .typeClasse(VueSupport.typeClasse(tache.getTypeId()))
                .typeLabel(typeLabel)
                .assignee(tache.getUtilisateurId() == null ? null
                        : avatar(utilisateur(tache.getUtilisateurId(), cache)))
                .pieceJointeNom(null)
                .commentaires(commentaires(tache.getId(), cache))
                .build();
    }

    private List<CommentaireVue> commentaires(Long tacheId, Map<Long, Utilisateur> cache) {
        List<CommentaireVue> vues = new ArrayList<>();
        for (Commentaire commentaire : commentaireService.findByTacheId(tacheId)) {
            Utilisateur auteur = utilisateur(commentaire.getUtilisateurId(), cache);
            String pseudo = auteur == null ? "?" : auteur.getPseudo();
            vues.add(CommentaireVue.builder()
                    .auteur(pseudo)
                    .initiales(VueSupport.initiales(pseudo))
                    .couleur(VueSupport.couleurAvatar(pseudo))
                    .content(commentaire.getContent())
                    .date(VueSupport.formatDate(commentaire.getCreatedAt()))
                    .build());
        }
        return vues;
    }

    private MembreVue avatar(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }
        return MembreVue.builder()
                .initiales(VueSupport.initiales(utilisateur.getPseudo()))
                .couleur(VueSupport.couleurAvatar(utilisateur.getPseudo()))
                .build();
    }

    /** Récupère un utilisateur via le cache local ; null s'il est introuvable. */
    private Utilisateur utilisateur(Long id, Map<Long, Utilisateur> cache) {
        if (id == null) {
            return null;
        }
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        Utilisateur utilisateur;
        try {
            utilisateur = utilisateurService.findById(id);
        } catch (IllegalArgumentException e) {
            utilisateur = null;
        }
        cache.put(id, utilisateur);
        return utilisateur;
    }

    private long parseId(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
