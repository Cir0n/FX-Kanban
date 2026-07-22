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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskNewServletTest {

    @Mock
    private ITacheService tacheService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskNewServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskNewServlet();
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
    void testDoPost_whenInvalidPayload_shouldRedirectToBoard() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(8L);
        when(request.getParameter("boardId")).thenReturn("3");
        when(request.getParameter("colonneId")).thenReturn("0");
        when(request.getParameter("name")).thenReturn(" ");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/board?id=3");
    }

    @Test
    void testDoPost_whenValid_shouldCreateTaskAndRedirect() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(8L);
        when(request.getParameter("boardId")).thenReturn("3");
        when(request.getParameter("colonneId")).thenReturn("4");
        when(request.getParameter("name")).thenReturn("Ma tache");
        when(request.getParameter("description")).thenReturn("Desc");
        when(request.getParameter("type")).thenReturn(null);
        when(request.getParameter("assignee")).thenReturn("");

        servlet.doPost(request, response);

        verify(tacheService).creer(eq("Ma tache"), eq("Desc"), eq(4L), any(), isNull(), eq(8L));
        verify(response).sendRedirect("/app/board?id=3&created=1");
    }
}

