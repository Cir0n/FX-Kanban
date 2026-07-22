package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.service.ITableauService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardEditServletTest {

    @Mock
    private ITableauService tableauService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private BoardEditServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new BoardEditServlet();
        ServletTestUtils.setField(servlet, "tableauService", tableauService);
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/login");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenBoardIdInvalid_shouldRedirectToDashboard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("boardId")).thenReturn("0");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/dashboard");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenBoardIdNonNumeric_shouldRedirectToDashboard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("boardId")).thenReturn("abc");

        servlet.doPost(request, response);

        // parseLong("abc") → fallback 0 → redirige vers dashboard
        verify(response).sendRedirect("/app/dashboard");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenValid_shouldRenameAndRedirect() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("boardId")).thenReturn("14");
        when(request.getParameter("name")).thenReturn(" Nouveau nom ");
        when(tableauService.renommer(14L, "Nouveau nom")).thenReturn(Tableau.builder().id(14L).name("Nouveau nom").build());

        servlet.doPost(request, response);

        verify(tableauService).renommer(14L, "Nouveau nom");
        verify(response).sendRedirect("/app/dashboard?updated=1");
    }

    @Test
    void testDoPost_whenValidationFails_shouldRedirectWithEncodedError() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("boardId")).thenReturn("14");
        when(request.getParameter("name")).thenReturn("bad");
        doThrow(new IllegalArgumentException("Nom invalide")).when(tableauService).renommer(14L, "bad");

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Nom invalide", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/dashboard?error=" + encoded);
    }
}

