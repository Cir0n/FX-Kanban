package fr.esgi.fx.kanban.servlet;

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
class BoardDeleteServletTest {

    @Mock
    private ITableauService tableauService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private BoardDeleteServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new BoardDeleteServlet();
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
        when(session.getAttribute("userId")).thenReturn(3L);
        when(request.getParameter("boardId")).thenReturn("0");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/dashboard");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenBoardIdNonNumeric_shouldRedirectToDashboard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(3L);
        when(request.getParameter("boardId")).thenReturn("abc");

        servlet.doPost(request, response);

        // parseLong("abc") → fallback 0 → redirige vers dashboard
        verify(response).sendRedirect("/app/dashboard");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenValid_shouldDeleteAndRedirect() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(3L);
        when(request.getParameter("boardId")).thenReturn("12");

        servlet.doPost(request, response);

        verify(tableauService).supprimer(12L);
        verify(response).sendRedirect("/app/dashboard?deleted=1");
    }

    @Test
    void testDoPost_whenServiceFails_shouldRedirectWithEncodedError() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(3L);
        when(request.getParameter("boardId")).thenReturn("12");
        doThrow(new RuntimeException("db down")).when(tableauService).supprimer(12L);

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Impossible de supprimer le tableau pour le moment.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/dashboard?error=" + encoded);
    }
}

