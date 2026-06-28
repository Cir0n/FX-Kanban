package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurRepositoryImpl implements UtilisateurRepository {

    // Pour sauvegarder dans la base de données
    @Override
    public void save(Utilisateur utilisateur) {

        String sql = Requetes.AJOUT_UTILISATEUR;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setString(1, utilisateur.getPseudo());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getMotDePasse());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
// Pour lire dans la base de données et donner le user qui correspond au email passé en paramètre
    @Override
    public Utilisateur findByEmail(String email) {
        String sql = Requetes.FIND_UTILISATEUR_BY_EMAIL;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si on trouve une ligne, on crée l'objet Utilisateur
                    Long id = rs.getLong("id");
                    String pseudo = rs.getString("pseudo");
                    String motDePasse = rs.getString("mot_de_passe");

                    return new Utilisateur(id, pseudo, email, motDePasse);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Si on n'a rien trouvé HOPE LA > null
        return null;
    }


    @Override
    public Utilisateur findByPseudo(String pseudo) {
        String sql = Requetes.FIND_UTILISATEUR_BY_PSEUDO;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pseudo);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long id = rs.getLong("id");
                    String email = rs.getString("email");
                    String motDePasse = rs.getString("mot_de_passe");

                    return new Utilisateur(id, pseudo, email, motDePasse);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public List<Utilisateur> findAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = Requetes.FIND_ALL_UTILISATEURS;

        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String pseudo = rs.getString("pseudo");
                String email = rs.getString("email");
                String motDePasse = rs.getString("mot_de_passe");

                Utilisateur utilisateur = new Utilisateur(id, pseudo, email, motDePasse);
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;


    }

    @Override
    public void update(Utilisateur utilisateur) {
        String sql = Requetes.UPDATE_UTILISATEUR;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // On définit les valeurs pour les '?' de la requête
            pstmt.setString(1, utilisateur.getPseudo());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getMotDePasse());
            pstmt.setLong(4, utilisateur.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id) {
        String sql = Requetes.DELETE_UTILISATEUR; // Utilisation de la constante

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}