package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.TypeTache;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeTacheRepository {

    private final String url = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
    private final String user = "sa";
    private final String password = "";

    public List<TypeTache> findAll() {
        List<TypeTache> typeTaches = new ArrayList<>();
        String sql = Requetes.FIND_ALL_TYPES_TACHES;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String libelle = rs.getString("libelle");

                TypeTache typeTache = new TypeTache(id, libelle);
                typeTaches.add(typeTache);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return typeTaches;
    }

    public TypeTache findById(Integer id) {
        String sql = Requetes.FIND_TYPE_TACHE_BY_ID;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String libelle = rs.getString("libelle");
                    return new TypeTache(id, libelle);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}