package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Tache;

import java.sql.Connection;
import fr.esgi.fx.kanban.persistence.Colonne;
import fr.esgi.fx.kanban.persistence.TypeTache;
import fr.esgi.fx.kanban.persistence.Utilisateur;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class TacheRepository {

    public Tache save(Tache tache) {
        String sql = Requetes.AJOUT_TACHE;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, tache.getTitre());
            pstmt.setString(2, tache.getDescription());
            //Pour les clès étrangères
            pstmt.setInt(3, tache.getColonne().getId());
            pstmt.setInt(4, tache.getTypeTache().getId());
            //L'utilisateur peut ëtre null (tâche non assignée)
            if (tache.getUtilisateur() != null) {
                pstmt.setLong(5, tache.getUtilisateur().getId());
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }
            //Exécution de la requête
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        tache.setId(generatedKeys.getLong(1));
                        return tache; // On retourne la tâche avec son nouvel ID
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    public Tache findById(Long id) {
        String sql = Requetes.FIND_TACHE_BY_ID;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Tache tache = new Tache();
                    tache.setId(rs.getLong("id"));
                    tache.setTitre(rs.getString("titre"));
                    tache.setDescription(rs.getString("description"));
                    tache.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                    Colonne colonne = new Colonne(rs.getInt("id_colonne"), rs.getString("nom_colonne"));
                    tache.setColonne(colonne);

                    TypeTache typeTache = new TypeTache(rs.getInt("id_type_tache"), rs.getString("libelle_type_tache"));
                    tache.setTypeTache(typeTache);

                    // Gère le cas où l'utilisateur est null (LEFT JOIN)
                    long utilisateurId = rs.getLong("id_utilisateur");
                    if (!rs.wasNull()) {
                        Utilisateur utilisateur = new Utilisateur(utilisateurId, rs.getString("pseudo_utilisateur"), rs.getString("email_utilisateur"), null);
                        tache.setUtilisateur(utilisateur);
                    }

                    return tache;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tache> findAll() {
        List<Tache> taches = new ArrayList<>();
        String sql = Requetes.FIND_ALL_TACHES;

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Tache tache = new Tache();
                tache.setId(rs.getLong("id"));
                tache.setTitre(rs.getString("titre"));
                tache.setDescription(rs.getString("description"));
                tache.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());

                Colonne colonne = new Colonne(rs.getInt("id_colonne"), rs.getString("nom_colonne"));
                tache.setColonne(colonne);

                TypeTache typeTache = new TypeTache(rs.getInt("id_type_tache"), rs.getString("libelle_type_tache"));
                tache.setTypeTache(typeTache);

                long utilisateurId = rs.getLong("id_utilisateur");
                if (!rs.wasNull()) {
                    Utilisateur utilisateur = new Utilisateur(utilisateurId, rs.getString("pseudo_utilisateur"), rs.getString("email_utilisateur"), null);
                    tache.setUtilisateur(utilisateur);
                }
                taches.add(tache);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taches;
    }

    public void update(Tache tache) {
        String sql = Requetes.UPDATE_TACHE;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tache.getTitre());
            pstmt.setString(2, tache.getDescription());
            pstmt.setInt(3, tache.getColonne().getId());
            pstmt.setInt(4, tache.getTypeTache().getId());

            if (tache.getUtilisateur() != null) {
                pstmt.setLong(5, tache.getUtilisateur().getId());
            } else {
                pstmt.setNull(5, Types.BIGINT);
            }

            pstmt.setLong(6, tache.getId()); // Le WHERE

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void delete(Long id) {
        String sql = Requetes.DELETE_TACHE;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}