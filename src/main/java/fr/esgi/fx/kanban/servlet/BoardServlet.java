package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.viewmodel.ColonneVue;
import fr.esgi.fx.kanban.viewmodel.CommentaireVue;
import fr.esgi.fx.kanban.viewmodel.MembreVue;
import fr.esgi.fx.kanban.viewmodel.TacheVue;
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
import java.util.Map;

@WebServlet(name = "boardServlet", value = {"/board"})
public class BoardServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
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
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Identifiant du tableau (défaut : 1)
        long tableauId = parseId(request.getParameter("id"), 1L);

        String pseudo = (String) session.getAttribute("user");

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable("userInitiales", initiales(pseudo));
        context.setVariable("boardId", tableauId);
        context.setVariable("boardName", boardName(tableauId));
        context.setVariable("boardCouleur", boardCouleur(tableauId));
        context.setVariable("membres", membresDeDemo());

        // Colonnes de démo + tâches créées durant la session
        List<ColonneVue> colonnes = colonnesDeDemo();
        mergerTachesSession(colonnes, session, tableauId);
        context.setVariable("colonnes", colonnes);

        // Bannière de confirmation après création d'une tâche
        context.setVariable("created", "1".equals(request.getParameter("created")));

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("board", context, response.getWriter());
    }

    // TODO : Remplacer par tableauService.findById(...) / colonneService.findByTableau(...).
    private List<ColonneVue> colonnesDeDemo() {
        MembreVue jd = MembreVue.builder().initiales("JD").couleur("#475569").build();
        MembreVue am = MembreVue.builder().initiales("AM").couleur("#378ADD").build();
        MembreVue tl = MembreVue.builder().initiales("TL").couleur("#1D9E75").build();

        return List.of(
                ColonneVue.builder().id(1L).name("À faire").taches(List.of(
                        TacheVue.builder().id(1L).name("Maquetter la page d'accueil")
                                .description("Wireframe + maquette haute fidélité pour desktop et mobile. "
                                        + "Prévoir les états vides et les variantes responsive.")
                                .typeClasse("standard").typeLabel("Standard").assignee(am)
                                .pieceJointeNom("maquette-accueil.fig")
                                .commentaires(List.of(
                                        commentaire("alice.m", "AM", "#378ADD",
                                                "J'ai commencé le wireframe, la maquette suit.", "2 juil. 09:12"),
                                        commentaire("jean.d", "JD", "#475569",
                                                "Pense à valider la palette avec le client.", "2 juil. 10:40")))
                                .build(),
                        TacheVue.builder().id(2L).name("Corriger le bug de connexion")
                                .description("La session expire trop tôt sur Safari : le cookie de session "
                                        + "n'est pas conservé après redirection.")
                                .typeClasse("bug").typeLabel("Bug").assignee(jd)
                                .commentaires(List.of(
                                        commentaire("tom.l", "TL", "#1D9E75",
                                                "Reproduit sur Safari 17, pas sur Chrome.", "1 juil. 16:05"),
                                        commentaire("jean.d", "JD", "#475569",
                                                "C'est lié au SameSite du cookie, je regarde.", "1 juil. 17:20")))
                                .build()
                )).build(),
                ColonneVue.builder().id(2L).name("En cours").taches(List.of(
                        TacheVue.builder().id(3L).name("Intégrer le paiement Stripe")
                                .description("Brancher l'API Stripe et gérer les webhooks de confirmation.")
                                .typeClasse("spike").typeLabel("Spike").assignee(tl)
                                .pieceJointeNom("stripe-flow.pdf")
                                .commentaires(List.of(
                                        commentaire("tom.l", "TL", "#1D9E75",
                                                "POC en cours sur l'environnement de test.", "2 juil. 11:00")))
                                .build(),
                        TacheVue.builder().id(4L).name("Optimiser le chargement des images")
                                .description("Lazy-loading et formats modernes (WebP / AVIF).")
                                .typeClasse("amelio").typeLabel("Amélioration").assignee(am)
                                .commentaires(List.of())
                                .build()
                )).build(),
                ColonneVue.builder().id(3L).name("En revue").taches(List.of(
                        TacheVue.builder().id(5L).name("Revue du formulaire d'inscription")
                                .description("Validation client + serveur à relire avant merge.")
                                .typeClasse("standard").typeLabel("Standard").assignee(jd)
                                .commentaires(List.of(
                                        commentaire("alice.m", "AM", "#378ADD",
                                                "RAS sur le front, je valide.", "2 juil. 08:30"),
                                        commentaire("tom.l", "TL", "#1D9E75",
                                                "Ajoute un test sur l'unicité du pseudo.", "2 juil. 09:15"),
                                        commentaire("jean.d", "JD", "#475569",
                                                "Corrigé, prêt à merger.", "2 juil. 09:50")))
                                .build()
                )).build(),
                ColonneVue.builder().id(4L).name("Terminé").taches(List.of(
                        TacheVue.builder().id(6L).name("Mettre en place la navbar")
                                .description("Logo, notifications et menu utilisateur.")
                                .typeClasse("standard").typeLabel("Standard").assignee(tl)
                                .commentaires(List.of())
                                .build()
                )).build()
        );
    }

    // Ajoute aux colonnes les tâches créées pendant la session (clé "boardId:colonneId").
    @SuppressWarnings("unchecked")
    private void mergerTachesSession(List<ColonneVue> colonnes, HttpSession session, long boardId) {
        Map<String, List<TacheVue>> parColonne =
                (Map<String, List<TacheVue>>) session.getAttribute(TaskNewServlet.SESSION_KEY);
        if (parColonne == null || parColonne.isEmpty()) {
            return;
        }
        for (ColonneVue colonne : colonnes) {
            List<TacheVue> creees = parColonne.get(boardId + ":" + colonne.getId());
            if (creees != null && !creees.isEmpty()) {
                List<TacheVue> combinees = new ArrayList<>(colonne.getTaches());
                combinees.addAll(creees);
                colonne.setTaches(combinees);
            }
        }
    }

    private CommentaireVue commentaire(String auteur, String initiales, String couleur,
                                       String content, String date) {
        return CommentaireVue.builder()
                .auteur(auteur).initiales(initiales).couleur(couleur)
                .content(content).date(date)
                .build();
    }

    private List<MembreVue> membresDeDemo() {
        return List.of(
                MembreVue.builder().initiales("JD").couleur("#475569").build(),
                MembreVue.builder().initiales("AM").couleur("#378ADD").build(),
                MembreVue.builder().initiales("TL").couleur("#1D9E75").build()
        );
    }

    private String boardName(long id) {
        return switch ((int) id) {
            case 2 -> "Application mobile";
            case 3 -> "Campagne marketing";
            default -> "Refonte site vitrine";
        };
    }

    private String boardCouleur(long id) {
        return switch ((int) id) {
            case 2 -> "#1D9E75";
            case 3 -> "#BA7517";
            default -> "#378ADD";
        };
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

    /** "jean.d" -> "JD", "alice" -> "A". */
    private String initiales(String s) {
        if (s == null || s.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[.\\s_-]+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }
}
