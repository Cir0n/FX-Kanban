package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.repository.ConnectionManager;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.Requetes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActionRepositoryImpl implements IActionRepository {

    private static final Logger LOGGER = LogManager.getLogger(ActionRepositoryImpl.class);

    @Override
    public Action save(Action action) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_ACTION, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, action.getDescription());
            stmt.setLong(2, action.getTacheId());
            stmt.setLong(3, action.getUtilisateurId());
            if (action.getColonneSourceId() != null) {
                stmt.setLong(4, action.getColonneSourceId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }
            if (action.getColonneCibleId() != null) {
                stmt.setLong(5, action.getColonneCibleId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    action.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la sauvegarde de l'action pour la tâche id={}", action.getTacheId(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde de l'action", e);
        }
        return action;
    }

    @Override
    public List<Action> findByTacheId(Long tacheId) {
        List<Action> actions = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_ACTIONS_BY_TACHE)) {

            stmt.setLong(1, tacheId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    actions.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération de l'historique de la tâche id={}", tacheId, e);
            throw new RuntimeException("Erreur lors de la récupération de l'historique", e);
        }
        return actions;
    }

    private Action mapRow(ResultSet rs) throws SQLException {
        long colonneSourceId = rs.getLong("colonne_source_id");
        Long colonneSource = rs.wasNull() ? null : colonneSourceId;

        long colonneCibleId = rs.getLong("colonne_cible_id");
        Long colonneCible = rs.wasNull() ? null : colonneCibleId;

        return Action.builder()
                .id(rs.getLong("id"))
                .description(rs.getString("description"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .tacheId(rs.getLong("tache_id"))
                .utilisateurId(rs.getLong("utilisateur_id"))
                .colonneSourceId(colonneSource)
                .colonneCibleId(colonneCible)
                .build();
    }
}
