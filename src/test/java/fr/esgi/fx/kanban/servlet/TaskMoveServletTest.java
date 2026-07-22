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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskMoveServletTest {

    @Mock
    private ITacheService tacheService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskMoveServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskMoveServlet();
        ServletTestUtils.setField(servlet, "tacheService", tacheService);
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldReturn401() throws Exception {
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testDoPost_whenMissingParams_shouldReturn400() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("taskId")).thenReturn(null);
        when(request.getParameter("colonneId")).thenReturn("2");

        servlet.doPost(request, response);

        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "taskId et colonneId sont requis");
        verifyNoInteractions(tacheService);
    }

    @Test
    void testDoPost_whenValid_shouldMoveAndReturn204() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(7L);
        when(request.getParameter("taskId")).thenReturn("10");
        when(request.getParameter("colonneId")).thenReturn("2");

        servlet.doPost(request, response);

        verify(tacheService).deplacer(10L, 2L, 7L);
        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}

