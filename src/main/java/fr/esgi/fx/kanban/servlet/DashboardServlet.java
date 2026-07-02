package fr.esgi.phil.kanban.servlet;

import fr.esgi.phil.kanban.viewmodel.MembreVue;
import fr.esgi.phil.kanban.viewmodel.TableauVue;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "dashboardServlet", value = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() {
        templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    // Les expressions de lien @{/...} de Thymeleaf 3.1 exigent un WebContext.
    private WebContext newContext(HttpServletRequest request, HttpServletResponse response) {
        IWebExchange exchange = application.buildExchange(request, response);
        return new WebContext(exchange);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Accès protégé : il faut être connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String pseudo = (String) session.getAttribute("user");

        WebContext context = newContext(request, response);
        context.setVariable("user", pseudo);
        context.setVariable("userInitiales", initiales(pseudo));
        context.setVariable("tableaux", tableauxDeDemo());

        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("dashboard", context, response.getWriter());
    }

    // TODO : Remplacer par tableauService.findByUtilisateur(...) une fois la couche service prête.
    private List<TableauVue> tableauxDeDemo() {
        return List.of(
                TableauVue.builder()
                        .id(1L).name("Refonte site vitrine").couleur("#378ADD").nbTaches(12)
                        .membres(List.of(
                                MembreVue.builder().initiales("JD").couleur("#475569").build(),
                                MembreVue.builder().initiales("AM").couleur("#378ADD").build(),
                                MembreVue.builder().initiales("TL").couleur("#1D9E75").build()))
                        .build(),
                TableauVue.builder()
                        .id(2L).name("Application mobile").couleur("#1D9E75").nbTaches(8)
                        .membres(List.of(
                                MembreVue.builder().initiales("AM").couleur("#378ADD").build(),
                                MembreVue.builder().initiales("TL").couleur("#1D9E75").build()))
                        .build(),
                TableauVue.builder()
                        .id(3L).name("Campagne marketing").couleur("#BA7517").nbTaches(5)
                        .membres(List.of(
                                MembreVue.builder().initiales("JD").couleur("#475569").build()))
                        .build()
        );
    }

    /** "jean.d" -> "JD", "alice" -> "A". */
    private String initiales(String s) {
        if (s == null || s.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("[.\\s_-]+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }
}
