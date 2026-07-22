package fr.esgi.fx.kanban.servlet;

import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.service.IPieceJointeService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAttachmentDownloadServletTest {

    @Mock
    private IPieceJointeService pieceJointeService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;

    private TaskAttachmentDownloadServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TaskAttachmentDownloadServlet();
        ServletTestUtils.setField(servlet, "pieceJointeService", pieceJointeService);
    }

    @Test
    void testDoGet_whenNotLoggedIn_shouldRedirectToLogin() throws Exception {
        when(request.getContextPath()).thenReturn("/app");
        when(request.getSession(false)).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/app/login");
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoGet_whenIdInvalid_shouldReturn404() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(5L);
        when(request.getParameter("id")).thenReturn("0");

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(pieceJointeService);
    }

    @Test
    void testDoGet_whenAttachmentNotFound_shouldReturn404() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(5L);
        when(request.getParameter("id")).thenReturn("8");
        when(pieceJointeService.findById(8L)).thenThrow(new IllegalArgumentException("missing"));

        servlet.doGet(request, response);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoGet_whenAttachmentFound_shouldWriteBinaryResponse() throws Exception {
        byte[] content = "abc123".getBytes(StandardCharsets.UTF_8);
        PieceJointe pj = PieceJointe.builder()
                .id(8L)
                .nomFichier("spec final.pdf")
                .mimeType("application/pdf")
                .contenu(content)
                .tacheId(2L)
                .build();

        CapturingServletOutputStream outputStream = new CapturingServletOutputStream();
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(5L);
        when(request.getParameter("id")).thenReturn("8");
        when(pieceJointeService.findById(8L)).thenReturn(pj);
        when(response.getOutputStream()).thenReturn(outputStream);

        servlet.doGet(request, response);

        String encoded = URLEncoder.encode("spec final.pdf", StandardCharsets.UTF_8).replace("+", "%20");
        verify(response).setContentType("application/pdf");
        verify(response).setContentLengthLong(content.length);
        verify(response).setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        assertArrayEquals(content, outputStream.bytes());
    }

    private static final class CapturingServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) {
            delegate.write(b);
        }

        byte[] bytes() {
            return delegate.toByteArray();
        }
    }
}


