package fr.esgi.phil.kanban.repository.implementation;

import fr.esgi.phil.kanban.model.Commentaire;
import fr.esgi.phil.kanban.repository.ConnectionManager;
import fr.esgi.phil.kanban.repository.ICommentaireRepository;
import fr.esgi.phil.kanban.repository.Requetes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireRepositoryImpl implements ICommentaireRepository {

    @Override
    public Commentaire save(Commentaire commentaire) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_COMMENTAIRE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, commentaire.getContent());
            stmt.setLong(2, commentaire.getTacheId());
            stmt.setLong(3, commentaire.getUtilisateurId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    commentaire.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du commentaire", e);
        }
        return commentaire;
    }

    @Override
    public List<Commentaire> findByTacheId(Long tacheId) {
        List<Commentaire> commentaires = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_COMMENTAIRES_BY_TACHE)) {

            stmt.setLong(1, tacheId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commentaires.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des commentaires", e);
        }
        return commentaires;
    }

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        return Commentaire.builder()
                .id(rs.getLong("id"))
                .content(rs.getString("content"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .tacheId(rs.getLong("tache_id"))
                .utilisateurId(rs.getLong("utilisateur_id"))
                .build();
    }
}