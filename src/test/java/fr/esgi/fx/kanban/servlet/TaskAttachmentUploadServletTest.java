package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.service.IPieceJointeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAttachmentUploadServletTest {

    @Mock
    private IPieceJointeService pieceJointeService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private Part part;

    private TaskAttachmentUploadServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskAttachmentUploadServlet();
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
    void testDoPost_whenTaskIdInvalid_shouldRedirectToBoard() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(6L);
        when(request.getParameter("boardId")).thenReturn("11");
        when(request.getParameter("taskId")).thenReturn("0");

        servlet.doPost(request, response);

        verify(response).sendRedirect("/app/board?id=11");
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoPost_whenPartMissing_shouldRedirectWithError() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(6L);
        when(request.getParameter("boardId")).thenReturn("11");
        when(request.getParameter("taskId")).thenReturn("22");
        when(request.getPart("file")).thenReturn(null);

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Veuillez sélectionner un fichier.", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=11&error=" + encoded);
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoPost_whenValid_shouldUploadAndRedirect() throws Exception {
        byte[] content = "binary-data".getBytes(StandardCharsets.UTF_8);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(6L);
        when(request.getParameter("boardId")).thenReturn("11");
        when(request.getParameter("taskId")).thenReturn("22");
        when(request.getPart("file")).thenReturn(part);
        when(part.getSize()).thenReturn((long) content.length);
        when(part.getSubmittedFileName()).thenReturn("C:/fakepath/spec.png");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(part.getContentType()).thenReturn("image/png");

        servlet.doPost(request, response);

        verify(pieceJointeService).ajouter("spec.png", "image/png", content, 22L, 6L);
        verify(response).sendRedirect("/app/board?id=11&updated=1");
    }

    @Test
    void testDoPost_whenServiceValidationFails_shouldRedirectWithError() throws Exception {
        byte[] content = "binary-data".getBytes(StandardCharsets.UTF_8);
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(6L);
        when(request.getParameter("boardId")).thenReturn("11");
        when(request.getParameter("taskId")).thenReturn("22");
        when(request.getPart("file")).thenReturn(part);
        when(part.getSize()).thenReturn((long) content.length);
        when(part.getSubmittedFileName()).thenReturn("spec.png");
        when(part.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(part.getContentType()).thenReturn("image/png");
        doThrow(new IllegalArgumentException("Fichier invalide"))
                .when(pieceJointeService).ajouter("spec.png", "image/png", content, 22L, 6L);

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Fichier invalide", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=11&error=" + encoded);
    }

    @Test
    void testDoPost_whenPartTooLarge_shouldRedirectWithSizeError() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(6L);
        when(request.getParameter("boardId")).thenReturn("11");
        when(request.getParameter("taskId")).thenReturn("22");
        when(request.getPart("file")).thenThrow(new IllegalStateException("too big"));

        servlet.doPost(request, response);

        String encoded = URLEncoder.encode("Le fichier dépasse la taille maximale autorisée (10 Mo).", StandardCharsets.UTF_8);
        verify(response).sendRedirect("/app/board?id=11&error=" + encoded);
    }
}


