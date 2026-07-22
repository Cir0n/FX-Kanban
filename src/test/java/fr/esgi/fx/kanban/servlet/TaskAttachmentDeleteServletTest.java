package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.IPieceJointeService;
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
class TaskAttachmentDeleteServletTest {

    @Mock
    private IPieceJointeService pieceJointeService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskAttachmentDeleteServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskAttachmentDeleteServlet();
        ServletTestUtils.setField(servlet, "pieceJointeService", pieceJointeService);
    }

    @Test
    void testDoPost_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/login");
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoPost_whenAttachmentIdInvalid_shouldRedirectToBoard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(request.getParameter("boardId")).thenReturn("3");
        when(request.getParameter("attachmentId")).thenReturn("0");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/board?id=3");
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoPost_whenValid_shouldDeleteAttachmentAndRedirect() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(request.getParameter("boardId")).thenReturn("3");
        when(request.getParameter("attachmentId")).thenReturn("9");

        servlet.doPost(request, response);

        verify(pieceJointeService).supprimer(9L, 4L);
        verify(response).sendRedirect("/app/board?id=3&updated=1");
    }

    @Test
    void testDoPost_whenServiceFails_shouldRedirectWithEncodedError() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(4L);
        when(request.getParameter("boardId")).thenReturn("3");
        when(request.getParameter("attachmentId")).thenReturn("9");
        doThrow(new IllegalArgumentException("Pièce jointe introuvable")).when(pieceJointeService).supprimer(9L, 4L);

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Pièce jointe introuvable", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=3&error=" + encoded);
    }
}

