package fr.esgi.fx.kanban.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeServletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StripeServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new StripeServlet();
    }

    @Test
    void testDoPost_whenStripeServiceUnavailable_shouldReturn503AndJsonError() throws Exception {
        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        String body = out.toString();
        // Vérifie que la réponse est un JSON avec une clé "error" non vide
        assertTrue(body.contains("\"error\""),
                "Le corps devrait contenir la clé JSON \"error\" mais obtenu : " + body);
        assertTrue(body.startsWith("{") && body.endsWith("}"),
                "Le corps devrait être un objet JSON valide : " + body);
    }

    @Test
    void testDoGet_whenStatusInvalid_shouldReturn400() throws Exception {
        StringWriter out = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(out));
        when(request.getParameter("status")).thenReturn("unknown");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}

