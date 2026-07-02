package fr.esgi.phil.kanban.servlet;

import fr.esgi.phil.kanban.viewmodel.MembreVue;
import fr.esgi.phil.kanban.viewmodel.TacheVue;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(name = "taskNewServlet", value = {"/task/new"})
public class TaskNewServlet extends HttpServlet {

    /** Attribut de session : tâches créées, groupées par clé "boardId:colonneId". */
    static final String SESSION_KEY = "tachesParColonne";

    private static final Set<String> TYPES_VALIDES = Set.of("standard", "bug", "spike", "amelio");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long boardId = parseLong(request.getParameter("boardId"), 1L);
        long colonneId = parseLong(request.getParameter("colonneId"), 0L);
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String type = request.getParameter("type");
        String assignee = request.getParameter("assignee");

        // Le nom est requis (la validation client de kanban.js empêche déjà l'envoi vide).
        if (name == null || name.trim().isEmpty() || colonneId == 0L) {
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId);
            return;
        }

        String typeClasse = TYPES_VALIDES.contains(type) ? type : "standard";

        TacheVue tache = TacheVue.builder()
                .id(System.currentTimeMillis())
                .name(name.trim())
                .description(description == null ? "" : description.trim())
                .typeClasse(typeClasse)
                .typeLabel(libelleType(typeClasse))
                .assignee(membre(assignee))
                .pieceJointeNom(null)
                .commentaires(new ArrayList<>())
                .build();

        // TODO : Remplacer par tacheService.create(...) (persistance en base).
        //  Stockage en session en attendant la couche service.
        stockerEnSession(session, boardId, colonneId, tache);

        response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&created=1");
    }

    @SuppressWarnings("unchecked")
    private void stockerEnSession(HttpSession session, long boardId, long colonneId, TacheVue tache) {
        Map<String, List<TacheVue>> parColonne =
                (Map<String, List<TacheVue>>) session.getAttribute(SESSION_KEY);
        if (parColonne == null) {
            parColonne = new ConcurrentHashMap<>();
            session.setAttribute(SESSION_KEY, parColonne);
        }
        parColonne.computeIfAbsent(boardId + ":" + colonneId, k -> new ArrayList<>()).add(tache);
    }

    private MembreVue membre(String initiales) {
        if (initiales == null || initiales.isBlank()) {
            return null;
        }
        return switch (initiales) {
            case "AM" -> MembreVue.builder().initiales("AM").couleur("#378ADD").build();
            case "TL" -> MembreVue.builder().initiales("TL").couleur("#1D9E75").build();
            case "JD" -> MembreVue.builder().initiales("JD").couleur("#475569").build();
            default -> null;
        };
    }

    private String libelleType(String typeClasse) {
        return switch (typeClasse) {
            case "bug" -> "Bug";
            case "spike" -> "Spike";
            case "amelio" -> "Amélioration";
            default -> "Standard";
        };
    }

    private long parseLong(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
