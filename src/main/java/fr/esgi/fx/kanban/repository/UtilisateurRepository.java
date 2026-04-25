package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Utilisateur;
import java.sql.*;

public class UtilisateurRepository {

    // Adresse de la base de données H2
    private final String url = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
    //private final String url = "jdbc:h2:mem:kanban;DB_CLOSE_DELAY=-1";
    // Nom d'utilisateur et mot de passe de la base de données
    private final String user = "sa";
    private final String password = "";

    public void save(Utilisateur utilisateur) {
        
        String sql = "INSERT INTO utilisateur (pseudo, email, mot_de_passe) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {


            pstmt.setString(1, utilisateur.getPseudo());
            pstmt.setString(2, utilisateur.getEmail());
            pstmt.setString(3, utilisateur.getMotDePasse());

            pstmt.executeUpdate();
            System.out.println("Utilisateur enregistré avec succès !");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Utilisateur findByEmail(String email) {
        String sql = "SELECT id, pseudo, email, mot_de_passe FROM utilisateur WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
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

        // Si on n'a rien trouvé, on retourne null
        return null;
    }
}