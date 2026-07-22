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
class BoardInviteServletTest {

    @Mock
    private ITableauService tableauService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private BoardInviteServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new BoardInviteServlet();
        ServletTestUtils.setField(servlet, "tableauService", tableauService);
        when(request.getContextPath()).thenReturn("/app");
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/login");
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenPseudoIsBlank_shouldRedirectWithError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(10L);
        when(request.getParameter("boardId")).thenReturn("8");
        when(request.getParameter("pseudo")).thenReturn("   ");

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Le pseudo est requis.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=8&error=" + encoded);
        verifyNoInteractions(tableauService);
    }

    @Test
    void testDoPost_whenValid_shouldInviteAndRedirect() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(10L);
        when(request.getParameter("boardId")).thenReturn("8");
        when(request.getParameter("pseudo")).thenReturn("alice");

        servlet.doPost(request, response);

        verify(tableauService).inviterContributeur(8L, "alice");
        verify(response).sendRedirect("/app/board?id=8&invited=1");
    }

    @Test
    void testDoPost_whenServiceThrows_shouldRedirectWithEncodedError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(10L);
        when(request.getParameter("boardId")).thenReturn("8");
        when(request.getParameter("pseudo")).thenReturn("unknown");
        doThrow(new IllegalArgumentException("Aucun utilisateur trouvé avec le pseudo : unknown"))
                .when(tableauService).inviterContributeur(8L, "unknown");

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Aucun utilisateur trouvé avec le pseudo : unknown", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=8&error=" + encoded);
    }
}

