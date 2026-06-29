package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.phil.kanban.model.PieceJointe;
import fr.esgi.phil.kanban.repository.ConnectionManager;
import fr.esgi.phil.kanban.repository.IPieceJointeRepository;
import fr.esgi.phil.kanban.repository.Requetes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PieceJointeRepositoryImpl implements IPieceJointeRepository {

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
            throw new RuntimeException("Erreur lors de la récupération des pièces jointes", e);
        }
        return pieces;
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.DELETE_PIECE_JOINTE)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
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
