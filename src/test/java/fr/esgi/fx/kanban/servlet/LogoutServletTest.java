package fr.esgi.fx.kanban.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    @Test
    void testDoGet_shouldInvalidateSessionAndRedirect() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        when(request.getSession(false)).thenReturn(session);
        when(request.getContextPath()).thenReturn("/app");

        servlet.doGet(request, response);

        verify(session).invalidate();
        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoGet_whenNoActiveSession_shouldRedirectWithoutInvalidating() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        when(request.getSession(false)).thenReturn(null);
        when(request.getContextPath()).thenReturn("/app");

        servlet.doGet(request, response);

        // Aucune session à invalider — vérifier que session.invalidate() n'est pas appelé
        verify(session, never()).invalidate();
        verify(response).sendRedirect("/app/login");
    }
}

