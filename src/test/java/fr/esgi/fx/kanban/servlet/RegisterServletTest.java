package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IUtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServletTest {

    @Mock
    private IUtilisateurService utilisateurService;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private JakartaServletWebApplication application;
    @Mock
    private IServletWebExchange exchange;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private RegisterServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new RegisterServlet();
        ServletTestUtils.setField(servlet, "utilisateurService", utilisateurService);
        ServletTestUtils.setField(servlet, "templateEngine", templateEngine);
        ServletTestUtils.setField(servlet, "application", application);
    }

    private void stubRender() throws Exception {
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    }

    // -----------------------------------------------------------------------
    // doGet
    // -----------------------------------------------------------------------

    @Test
    void testDoGet_shouldRenderRegisterTemplate() throws Exception {
        stubRender();

        servlet.doGet(request, response);

        verify(templateEngine).process(eq("register"), any(), any(PrintWriter.class));
        verifyNoInteractions(utilisateurService);
    }

    // -----------------------------------------------------------------------
    // doPost – validation des champs
    // -----------------------------------------------------------------------

    @Test
    void testDoPost_whenPseudoBlank_shouldRenderPseudoError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("  ");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("Le pseudo est requis.", ctx.getValue().getVariable("pseudoError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenEmailInvalid_shouldRenderEmailError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("bad-email");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("Format d'email invalide.", ctx.getValue().getVariable("emailError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenEmailBlank_shouldRenderEmailRequiredError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("  ");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("L'email est requis.", ctx.getValue().getVariable("emailError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenPasswordTooShort_shouldRenderPasswordError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("court");
        when(request.getParameter("confirmPassword")).thenReturn("court");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("Le mot de passe doit contenir au moins 8 caractères.",
                ctx.getValue().getVariable("passwordError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenPasswordMismatch_shouldRenderConfirmError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("autreMotDePasse");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("Les mots de passe ne correspondent pas.", ctx.getValue().getVariable("confirmError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenValidationErrors_shouldRepopulateNonSensitiveFields() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("  ");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        // Le pseudo soumis et l'email sont réaffichés (mais pas le mot de passe)
        assertEquals("  ", ctx.getValue().getVariable("pseudo"));
        assertEquals("alice@test.com", ctx.getValue().getVariable("email"));
    }

    // -----------------------------------------------------------------------
    // doPost – chemin nominal
    // -----------------------------------------------------------------------

    @Test
    void testDoPost_whenValid_shouldCallServiceAndRedirectToLogin() throws Exception {
        // Le servlet crée un WebContext avant validation, donc buildExchange est toujours appelé
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");
        when(utilisateurService.inscrire("alice", "alice@test.com", "password123"))
                .thenReturn(Utilisateur.builder().id(1L).pseudo("alice").build());

        servlet.doPost(request, response);

        verify(utilisateurService).inscrire("alice", "alice@test.com", "password123");
        verify(response).sendRedirect("/app/login?success=1");
        verifyNoInteractions(templateEngine);
    }

    // -----------------------------------------------------------------------
    // doPost – exception du service (pseudo ou email déjà pris)
    // -----------------------------------------------------------------------

    @Test
    void testDoPost_whenServiceThrows_shouldRenderPseudoError() throws Exception {
        stubRender();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("email")).thenReturn("alice@test.com");
        when(request.getParameter("password")).thenReturn("password123");
        when(request.getParameter("confirmPassword")).thenReturn("password123");
        when(utilisateurService.inscrire("alice", "alice@test.com", "password123"))
                .thenThrow(new IllegalArgumentException("Ce pseudo est déjà utilisé"));

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> ctx = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("register"), ctx.capture(), any(PrintWriter.class));
        assertEquals("Ce pseudo est déjà utilisé", ctx.getValue().getVariable("pseudoError"));
        assertEquals("alice", ctx.getValue().getVariable("pseudo"));
        assertEquals("alice@test.com", ctx.getValue().getVariable("email"));
        assertNull(ctx.getValue().getVariable("confirmError"));
    }
}
