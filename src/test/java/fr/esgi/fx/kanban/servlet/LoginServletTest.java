package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.service.IUtilisateurService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServletTest {

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
    @Mock
    private HttpSession session;

    private LoginServlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new LoginServlet();
        ServletTestUtils.setField(servlet, "utilisateurService", utilisateurService);
        ServletTestUtils.setField(servlet, "templateEngine", templateEngine);
        ServletTestUtils.setField(servlet, "application", application);
    }

    @Test
    void testDoGet_whenAlreadyLoggedIn_shouldRedirectToDashboard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn("alice");

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/dashboard");
    }

    @Test
    void testDoGet_whenSuccessParameterPresent_shouldRenderSuccessMessage() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getSession(false)).thenReturn(null);
        when(request.getParameter("success")).thenReturn("1");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doGet(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("login"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Inscription réussie ! Vous pouvez vous connecter.",
                contextCaptor.getValue().getVariable("success"));
    }

    @Test
    void testDoPost_whenFieldsMissing_shouldRenderValidationErrors() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getParameter("pseudo")).thenReturn(" ");
        when(request.getParameter("password")).thenReturn("");
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("login"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Le pseudo est requis.", contextCaptor.getValue().getVariable("pseudoError"));
        assertEquals("Le mot de passe est requis.", contextCaptor.getValue().getVariable("passwordError"));
        verifyNoInteractions(utilisateurService);
    }

    @Test
    void testDoPost_whenCredentialsValid_shouldCreateSessionAndRedirect() throws Exception {
        Utilisateur utilisateur = Utilisateur.builder().id(9L).pseudo("alice").build();
        when(request.getContextPath()).thenReturn("/app");
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("password")).thenReturn("password123");
        when(utilisateurService.connecter("alice", "password123")).thenReturn(utilisateur);
        when(request.getSession(true)).thenReturn(session);
        when(application.buildExchange(request, response)).thenReturn(exchange);

        servlet.doPost(request, response);

        verify(session).setAttribute("user", "alice");
        verify(session).setAttribute("userId", 9L);
        verify(response).sendRedirect("/app/dashboard");
    }

    @Test
    void testDoPost_whenCredentialsInvalid_shouldRenderError() throws Exception {
        StringWriter out = new StringWriter();
        when(request.getParameter("pseudo")).thenReturn("alice");
        when(request.getParameter("password")).thenReturn("bad");
        when(utilisateurService.connecter("alice", "bad"))
                .thenThrow(new IllegalArgumentException("Pseudo ou mot de passe incorrect"));
        when(application.buildExchange(request, response)).thenReturn(exchange);
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(request, response);

        ArgumentCaptor<IContext> contextCaptor = ArgumentCaptor.forClass(IContext.class);
        verify(templateEngine).process(eq("login"), contextCaptor.capture(), any(PrintWriter.class));
        assertEquals("Pseudo ou mot de passe incorrect", contextCaptor.getValue().getVariable("error"));
        assertEquals("alice", contextCaptor.getValue().getVariable("pseudo"));
        assertTrue(out.toString().isEmpty());
    }
}



