package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.repository.ConnectionManager;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.repository.Requetes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TacheRepositoryImpl implements ITacheRepository {

    private static final Logger LOGGER = LogManager.getLogger(TacheRepositoryImpl.class);

    @Override
    public Tache save(Tache tache) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_TACHE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tache.getName());
            stmt.setString(2, tache.getDescription());
            stmt.setLong(3, tache.getColonneId());
            stmt.setLong(4, tache.getTypeId());
            stmt.setLong(5, tache.getCreatedBy());
            if (tache.getUtilisateurId() != null) {
                stmt.setLong(6, tache.getUtilisateurId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tache.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la sauvegarde de la tâche dans la colonne id={}", tache.getColonneId(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde de la tâche", e);
        }
        return tache;
    }

    @Override
    public Optional<Tache> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_TACHE_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la recherche de la tâche id={}", id, e);
            throw new RuntimeException("Erreur lors de la recherche de la tâche", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tache> findAll() {
        List<Tache> taches = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Requetes.FIND_ALL_TACHES)) {

            while (rs.next()) {
                taches.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération des tâches", e);
            throw new RuntimeException("Erreur lors de la récupération des tâches", e);
        }
        return taches;
    }

    @Override
    public List<Tache> findByColonneId(Long colonneId) {
        List<Tache> taches = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_TACHES_BY_COLONNE)) {

            stmt.setLong(1, colonneId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la recherche des tâches de la colonne id={}", colonneId, e);
            throw new RuntimeException("Erreur lors de la recherche des tâches", e);
        }
        return taches;
    }

    @Override
    public void update(Tache tache) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.UPDATE_TACHE)) {

            stmt.setString(1, tache.getName());
            stmt.setString(2, tache.getDescription());
            stmt.setLong(3, tache.getColonneId());
            stmt.setLong(4, tache.getTypeId());
            if (tache.getUtilisateurId() != null) {
                stmt.setLong(5, tache.getUtilisateurId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.setLong(6, tache.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la mise à jour de la tâche id={}", tache.getId(), e);
            throw new RuntimeException("Erreur lors de la mise à jour de la tâche", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.DELETE_TACHE)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression de la tâche id={}", id, e);
            throw new RuntimeException("Erreur lors de la suppression de la tâche", e);
        }
    }

    private Tache mapRow(ResultSet rs) throws SQLException {
        long utilisateurId = rs.getLong("utilisateur_id");
        return Tache.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .colonneId(rs.getLong("colonne_id"))
                .typeId(rs.getLong("type_id"))
                .createdBy(rs.getLong("created_by"))
                .utilisateurId(rs.wasNull() ? null : utilisateurId)
                .build();
    }
}
