package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.viewmodel.TableauVue;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServletTest {

    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private JakartaServletWebApplication application;
    @Mock
    private IServletWebExchange exchange;
    @Mock
    private ITableauService tableauService;
    @Mock
    private IColonneService colonneService;
    @Mock
    private ITacheService tacheService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private DashboardServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DashboardServlet();
        ServletTestUtils.setField(servlet, "templateEngine", templateEngine);
        ServletTestUtils.setField(servlet, "application", application);
        ServletTestUtils.setField(servlet, "tableauService", tableauService);
        ServletTestUtils.setField(servlet, "colonneService", colonneService);
        ServletTestUtils.setField(servlet, "tacheService", tacheService);
    }

    @Test
    void testDoGet_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoGet_whenLoggedIn_shouldRenderDashboardWithComputedBoardSummaries() throws Exception {
        StringWriter out = new StringWriter();
        Tableau tableau = Tableau.builder().id(12L).name("Projet X").createdBy(8L).build();
        Colonne todo = Colonne.builder().id(100L).name("Todo").position(1).tableauId(12L).build();
        Colonne done = Colonne.builder().id(101L).name("Done").position(2).tableauId(12L).build();
        Utilisateur membre = Utilisateur.builder().id(77L).pseudo("jean.dupont").build();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(8L);
        when(session.getAttribute("user")).thenReturn("alice.martin");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));
        when(tableauService.findAllByContributeur(8L)).thenReturn(List.of(tableau));
        when(colonneService.findByTableauId(12L)).thenReturn(List.of(todo, done));
        when(tacheService.findByColonneId(100L)).thenReturn(List.of(Tache.builder().id(1L).build()));
        when(tacheService.findByColonneId(101L)).thenReturn(List.of(
                Tache.builder().id(2L).build(),
                Tache.builder().id(3L).build()));
        when(tableauService.findContributeurs(12L)).thenReturn(List.of(membre));

        servlet.doGet(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("dashboard"), contextCaptor.capture(), any(PrintWriter.class));

        assertEquals("alice.martin", contextCaptor.getValue().getVariable("user"));
        assertEquals("AM", contextCaptor.getValue().getVariable("userInitiales"));
        @SuppressWarnings("unchecked")
        List<TableauVue> tableaux = (List<TableauVue>) contextCaptor.getValue().getVariable("tableaux");
        assertNotNull(tableaux);
        assertEquals(1, tableaux.size());
        assertEquals("Projet X", tableaux.getFirst().getName());
        assertEquals(3, tableaux.getFirst().getNbTaches());
        assertEquals(1, tableaux.getFirst().getMembres().size());
        assertEquals("JD", tableaux.getFirst().getMembres().getFirst().getInitiales());
    }
}



