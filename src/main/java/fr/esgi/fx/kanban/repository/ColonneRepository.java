package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Colonne;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ColonneRepository {

    private final String url = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
    private final String user = "sa";
    private final String password = "";

    public List<Colonne> findAll() {
        List<Colonne> colonnes = new ArrayList<>();
        String sql = Requetes.FIND_ALL_COLONNES;
        

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String nom = rs.getString("nom");

                Colonne colonne = new Colonne(id, nom);
                colonnes.add(colonne);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return colonnes;
    }
    
    public Colonne findById(Integer id) {
        String sql = Requetes.FIND_COLONNE_BY_ID;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    return new Colonne(id, nom);
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    
        }
}