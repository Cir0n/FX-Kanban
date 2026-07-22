package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Commentaire;
import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.TypeDeTache;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IActionService;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.ICommentaireService;
import fr.esgi.fx.kanban.service.IPieceJointeService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.ITypeDeTacheService;
import fr.esgi.fx.kanban.service.IUtilisateurService;
import fr.esgi.fx.kanban.viewmodel.ColonneVue;
import fr.esgi.fx.kanban.viewmodel.TacheVue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.web.servlet.IServletWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServletTest {

    @Mock private TemplateEngine templateEngine;
    @Mock private JakartaServletWebApplication application;
    @Mock private IServletWebExchange exchange;
    @Mock private ITableauService tableauService;
    @Mock private IColonneService colonneService;
    @Mock private ITacheService tacheService;
    @Mock private ICommentaireService commentaireService;
    @Mock private ITypeDeTacheService typeDeTacheService;
    @Mock private IUtilisateurService utilisateurService;
    @Mock private IActionService actionService;
    @Mock private IPieceJointeService pieceJointeService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    private BoardServlet servlet;

    // ── Données de test réutilisées ──────────────────────────────────────────
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 22, 10, 15);
    private static final Tableau TABLEAU = Tableau.builder().id(5L).name("Board Produit").createdBy(1L).build();
    private static final Colonne COLONNE  = Colonne.builder().id(100L).name("En cours").position(1).tableauId(5L).build();
    private static final Tache   TACHE    = Tache.builder()
            .id(200L).name("Corriger le bug").description("Le drag and drop casse")
            .colonneId(100L).typeId(2L).utilisateurId(50L).build();
    private static final Utilisateur MEMBRE        = Utilisateur.builder().id(50L).pseudo("jean.dupont").build();
    private static final Utilisateur AUTEUR_ACTION = Utilisateur.builder().id(60L).pseudo("alice.martin").build();
    private static final Commentaire COMMENTAIRE   = Commentaire.builder()
            .content("Je regarde ça").createdAt(NOW).tacheId(200L).utilisateurId(50L).build();
    private static final Action ACTION_ANCIENNE = Action.builder()
            .description("Création").createdAt(NOW.minusHours(1)).tacheId(200L).utilisateurId(60L).build();
    private static final Action ACTION_RECENTE  = Action.builder()
            .description("Déplacement").createdAt(NOW).tacheId(200L).utilisateurId(60L).build();

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BoardServlet();
        ServletTestUtils.setField(servlet, "templateEngine",    templateEngine);
        ServletTestUtils.setField(servlet, "application",       application);
        ServletTestUtils.setField(servlet, "tableauService",    tableauService);
        ServletTestUtils.setField(servlet, "colonneService",    colonneService);
        ServletTestUtils.setField(servlet, "tacheService",      tacheService);
        ServletTestUtils.setField(servlet, "commentaireService",commentaireService);
        ServletTestUtils.setField(servlet, "typeDeTacheService",typeDeTacheService);
        ServletTestUtils.setField(servlet, "utilisateurService",utilisateurService);
        ServletTestUtils.setField(servlet, "actionService",     actionService);
        ServletTestUtils.setField(servlet, "pieceJointeService",pieceJointeService);
    }

    /**
     * Configure tous les mocks nécessaires à un appel complet de doGet,
     * exécute le servlet et retourne le contexte Thymeleaf capturé.
     * Chaque test focalisé appelle ce helper puis vérifie un aspect précis.
     */
    private IContext executeFullDoGet(
            String paramCreated, String paramInvited, String paramUpdated,
            String paramDeleted, String paramError) throws Exception {

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(session.getAttribute("user")).thenReturn("owner.user");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("created")).thenReturn(paramCreated);
        when(request.getParameter("invited")).thenReturn(paramInvited);
        when(request.getParameter("updated")).thenReturn(paramUpdated);
        when(request.getParameter("deleted")).thenReturn(paramDeleted);
        when(request.getParameter("error")).thenReturn(paramError);
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(tableauService.findById(5L)).thenReturn(TABLEAU);
        when(tableauService.findContributeurs(5L)).thenReturn(List.of(MEMBRE));
        when(colonneService.findByTableauId(5L)).thenReturn(List.of(COLONNE));
        when(tacheService.findByColonneId(100L)).thenReturn(List.of(TACHE));
        when(typeDeTacheService.findById(2L)).thenReturn(
                Optional.of(TypeDeTache.builder().id(2L).name("Bug").build()));
        when(commentaireService.findByTacheId(200L)).thenReturn(List.of(COMMENTAIRE));
        when(actionService.findByTacheId(200L)).thenReturn(List.of(ACTION_ANCIENNE, ACTION_RECENTE));
        when(pieceJointeService.findByTacheId(200L)).thenReturn(List.of(
                PieceJointe.builder().id(300L).nomFichier("spec.pdf").contenu(new byte[]{1,2,3}).build()));
        when(utilisateurService.findById(50L)).thenReturn(MEMBRE);
        when(utilisateurService.findById(60L)).thenReturn(AUTEUR_ACTION);

        servlet.doGet(request, response);

        ArgumentCaptor<IContext> captor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("board"), captor.capture(), any(PrintWriter.class));
        return captor.getValue();
    }

    // ── Tests de sécurité ───────────────────────────────────────────────────

    @Test
    void testDoGet_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoGet_whenBoardNotFound_shouldRedirectToDashboard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("id")).thenReturn("9");
        when(tableauService.findById(9L)).thenThrow(new IllegalArgumentException("Tableau introuvable"));

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/dashboard");
    }

    // ── Tests focalisés sur le rendu du contexte ────────────────────────────

    @Test
    void testDoGet_shouldPassNotificationFlagsToContext() throws Exception {
        IContext ctx = executeFullDoGet("1", "1", "1", "1", "Boom");

        assertEquals(5L,           ctx.getVariable("boardId"));
        assertEquals("Board Produit", ctx.getVariable("boardName"));
        assertEquals(true,         ctx.getVariable("created"));
        assertEquals(true,         ctx.getVariable("invited"));
        assertEquals(true,         ctx.getVariable("updated"));
        assertEquals(true,         ctx.getVariable("deleted"));
        assertEquals("Boom",       ctx.getVariable("error"));
    }

    @Test
    void testDoGet_shouldMapColumnStructure() throws Exception {
        IContext ctx = executeFullDoGet(null, null, null, null, null);

        @SuppressWarnings("unchecked")
        List<ColonneVue> colonnes = (List<ColonneVue>) ctx.getVariable("colonnes");
        assertNotNull(colonnes);
        assertEquals(1, colonnes.size());
        assertEquals("En cours", colonnes.getFirst().getName());
        assertEquals(1, colonnes.getFirst().getTaches().size());
    }

    @Test
    void testDoGet_shouldMapTaskTypeAndAssigneeInitials() throws Exception {
        IContext ctx = executeFullDoGet(null, null, null, null, null);

        @SuppressWarnings("unchecked")
        List<ColonneVue> colonnes = (List<ColonneVue>) ctx.getVariable("colonnes");
        TacheVue tacheVue = colonnes.getFirst().getTaches().getFirst();

        assertEquals("Corriger le bug", tacheVue.getName());
        assertEquals("Bug",             tacheVue.getTypeLabel());
        assertEquals(50L,               tacheVue.getAssigneeId());
        assertEquals("JD",              tacheVue.getAssignee().getInitiales());
    }

    @Test
    void testDoGet_shouldSortHistoriqueByDateDesc() throws Exception {
        IContext ctx = executeFullDoGet(null, null, null, null, null);

        @SuppressWarnings("unchecked")
        List<ColonneVue> colonnes = (List<ColonneVue>) ctx.getVariable("colonnes");
        TacheVue tacheVue = colonnes.getFirst().getTaches().getFirst();

        assertEquals(2, tacheVue.getHistorique().size());
        // Ordre chronologique inverse : l'action la plus récente est en premier
        assertEquals("Déplacement", tacheVue.getHistorique().getFirst().getDescription());
    }

    @Test
    void testDoGet_shouldMapCommentaireAuteur() throws Exception {
        IContext ctx = executeFullDoGet(null, null, null, null, null);

        @SuppressWarnings("unchecked")
        List<ColonneVue> colonnes = (List<ColonneVue>) ctx.getVariable("colonnes");
        TacheVue tacheVue = colonnes.getFirst().getTaches().getFirst();

        assertEquals(1,            tacheVue.getCommentaires().size());
        assertEquals("jean.dupont",tacheVue.getCommentaires().getFirst().getAuteur());
    }

    @Test
    void testDoGet_shouldExposeContributeurs() throws Exception {
        IContext ctx = executeFullDoGet(null, null, null, null, null);

        List<?> membres = (List<?>) ctx.getVariable("membres");
        // assertEquals produit "expected: 1 but was: X" — plus informatif que assertTrue
        assertEquals(1, membres.size());
    }
}
