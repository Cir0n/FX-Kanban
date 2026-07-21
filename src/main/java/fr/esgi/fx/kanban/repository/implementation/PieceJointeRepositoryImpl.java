package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.repository.ConnectionManager;
import fr.esgi.fx.kanban.repository.IPieceJointeRepository;
import fr.esgi.fx.kanban.repository.Requetes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PieceJointeRepositoryImpl implements IPieceJointeRepository {

    private static final Logger LOGGER = LogManager.getLogger(PieceJointeRepositoryImpl.class);

    @Override
    public PieceJointe save(PieceJointe pieceJointe) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_PIECE_JOINTE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, pieceJointe.getNomFichier());
            stmt.setString(2, pieceJointe.getMimeType());
            stmt.setBytes(3, pieceJointe.getContenu());
            stmt.setLong(4, pieceJointe.getTacheId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    pieceJointe.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la sauvegarde de la pièce jointe pour la tâche id={}", pieceJointe.getTacheId(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde de la pièce jointe", e);
        }
        return pieceJointe;
    }

    @Override
    public List<PieceJointe> findByTacheId(Long tacheId) {
        List<PieceJointe> pieces = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_PIECES_JOINTES_BY_TACHE)) {

            stmt.setLong(1, tacheId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pieces.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération des pièces jointes de la tâche id={}", tacheId, e);
            throw new RuntimeException("Erreur lors de la récupération des pièces jointes", e);
        }
        return pieces;
    }

    @Override
    public Optional<PieceJointe> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_PIECE_JOINTE_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération de la pièce jointe id={}", id, e);
            throw new RuntimeException("Erreur lors de la récupération de la pièce jointe", e);
        }
        return Optional.empty();
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.DELETE_PIECE_JOINTE)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression de la pièce jointe id={}", id, e);
            throw new RuntimeException("Erreur lors de la suppression de la pièce jointe", e);
        }
    }

    private PieceJointe mapRow(ResultSet rs) throws SQLException {
        return PieceJointe.builder()
                .id(rs.getLong("id"))
                .nomFichier(rs.getString("nom_fichier"))
                .mimeType(rs.getString("mime_type"))
                .contenu(rs.getBytes("contenu"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .tacheId(rs.getLong("tache_id"))
                .build();
    }
}
