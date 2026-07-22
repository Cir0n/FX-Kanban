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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskEditServletTest {

    @Mock
    private ITacheService tacheService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskEditServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskEditServlet();
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
    void testDoPost_whenNameMissing_shouldRedirectWithError() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("boardId")).thenReturn("9");
        when(request.getParameter("taskId")).thenReturn("10");
        when(request.getParameter("name")).thenReturn(" ");

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Le nom de la tâche est requis.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=9&error=" + encoded);
    }

    @Test
    void testDoPost_whenValid_shouldCallServiceAndRedirect() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);
        when(request.getParameter("boardId")).thenReturn("9");
        when(request.getParameter("taskId")).thenReturn("10");
        when(request.getParameter("name")).thenReturn(" Renommer ");
        when(request.getParameter("description")).thenReturn(" Nouvelle description ");
        when(request.getParameter("type")).thenReturn(null);
        when(request.getParameter("assignee")).thenReturn("");

        servlet.doPost(request, response);

        verify(tacheService).modifier(eq(10L), eq("Renommer"), eq("Nouvelle description"), any(), isNull(), eq(1L));
        verify(response).sendRedirect("/app/board?id=9&updated=1");
    }
}

