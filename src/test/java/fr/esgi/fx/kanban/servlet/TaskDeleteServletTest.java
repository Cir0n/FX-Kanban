package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.ITacheService;
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
class TaskDeleteServletTest {

    @Mock
    private ITacheService tacheService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskDeleteServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskDeleteServlet();
        ServletTestUtils.setField(servlet, "tacheService", tacheService);
        when(request.getContextPath()).thenReturn("/app");
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/login");
    }

    @Test
    void testDoPost_whenTaskIdInvalid_shouldRedirectToBoard() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("boardId")).thenReturn("5");
        when(request.getParameter("taskId")).thenReturn("0");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/board?id=5");
        verifyNoInteractions(tacheService);
    }

    @Test
    void testDoPost_whenValid_shouldDeleteAndRedirect() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("boardId")).thenReturn("5");
        when(request.getParameter("taskId")).thenReturn("9");

        servlet.doPost(request, response);

        verify(tacheService).supprimer(9L);
        verify(response).sendRedirect("/app/board?id=5&deleted=1");
    }

    @Test
    void testDoPost_whenServiceFails_shouldRedirectWithEncodedError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("boardId")).thenReturn("5");
        when(request.getParameter("taskId")).thenReturn("9");
        doThrow(new RuntimeException("db error")).when(tacheService).supprimer(9L);

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Impossible de supprimer la tâche pour le moment.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=5&error=" + encoded);
    }
}

