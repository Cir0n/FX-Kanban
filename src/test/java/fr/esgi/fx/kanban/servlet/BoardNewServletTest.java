package fr.esgi.fx.kanban.servlet;

import com.stripe.model.checkout.Session;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.service.IColonneService;
import fr.esgi.fx.kanban.service.IStripeService;
import fr.esgi.fx.kanban.service.ITableauService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardNewServletTest {

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
    private IStripeService stripeService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private BoardNewServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BoardNewServlet();
        ServletTestUtils.setField(servlet, "templateEngine", templateEngine);
        ServletTestUtils.setField(servlet, "application", application);
        ServletTestUtils.setField(servlet, "tableauService", tableauService);
        ServletTestUtils.setField(servlet, "colonneService", colonneService);
        ServletTestUtils.setField(servlet, "stripeService", stripeService);
    }

    @Test
    void testDoGet_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoPost_whenNameIsInvalid_shouldRenderFormWithNameError() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(session.getAttribute("user")).thenReturn("jean.dupont");
        when(request.getParameter("name")).thenReturn("Nom invalide !");
        when(request.getParameter("couleur")).thenReturn("#378ADD");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("board-new"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Le nom doit contenir uniquement des caractères alphanumériques.",
                contextCaptor.getValue().getVariable("nameError"));
        assertEquals("Nom invalide !", contextCaptor.getValue().getVariable("name"));
        verifyNoInteractions(tableauService, colonneService, stripeService);
    }

    @Test
    void testDoPost_whenStripeServiceUnavailable_shouldRenderGlobalError() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(session.getAttribute("user")).thenReturn("jean.dupont");
        when(request.getParameter("name")).thenReturn("BoardAlpha");
        when(request.getParameter("couleur")).thenReturn("#378ADD");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));
        ServletTestUtils.setField(servlet, "stripeService", null);

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("board-new"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Le paiement est indisponible pour le moment. Réessayez plus tard.",
                contextCaptor.getValue().getVariable("error"));
    }

    @Test
    void testDoGet_whenPaymentSuccessAndPaid_shouldCreateBoardAndDefaultColumns() throws Exception {
        Session checkout = org.mockito.Mockito.mock(Session.class);
        Tableau tableau = Tableau.builder().id(42L).name("BoardAlpha").createdBy(4L).build();

        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(session.getAttribute("pendingBoardName")).thenReturn("BoardAlpha");
        when(request.getParameter("status")).thenReturn("success");
        when(request.getParameter("session_id")).thenReturn("cs_test_123");
        when(stripeService.retrieveSession("cs_test_123")).thenReturn(checkout);
        when(checkout.getPaymentStatus()).thenReturn("paid");
        when(tableauService.creer("BoardAlpha", 4L, "cs_test_123")).thenReturn(tableau);

        servlet.doGet(request, response);

        verify(session).removeAttribute("pendingBoardName");
        verify(session).removeAttribute("pendingBoardCouleur");
        verify(tableauService).creer("BoardAlpha", 4L, "cs_test_123");
        verify(colonneService).creer("À faire", 1, 42L);
        verify(colonneService).creer("En cours", 2, 42L);
        verify(colonneService).creer("En revue", 3, 42L);
        verify(colonneService).creer("Terminé", 4, 42L);
        verify(response).sendRedirect("/app/board?id=42&boardCreated=1");
    }

    @Test
    void testDoGet_whenPaymentCancelled_shouldClearPendingStateAndRenderError() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(session.getAttribute("user")).thenReturn("jean.dupont");
        when(session.getAttribute("pendingBoardName")).thenReturn("BoardAlpha");
        when(session.getAttribute("pendingBoardCouleur")).thenReturn("#378ADD");
        when(request.getParameter("status")).thenReturn("cancel");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(request, response);

        verify(session).removeAttribute("pendingBoardName");
        verify(session).removeAttribute("pendingBoardCouleur");
        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("board-new"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Paiement annulé : le tableau n'a pas été créé.", contextCaptor.getValue().getVariable("error"));
        assertEquals("BoardAlpha", contextCaptor.getValue().getVariable("name"));
        assertEquals("#378ADD", contextCaptor.getValue().getVariable("couleur"));
        assertNull(contextCaptor.getValue().getVariable("nameError"));
    }
}



