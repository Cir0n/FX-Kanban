package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.repository.ConnectionManager;
import fr.esgi.fx.kanban.repository.IColonneRepository;
import fr.esgi.fx.kanban.repository.Requetes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ColonneRepositoryImpl implements IColonneRepository {

    @Override
    public Colonne save(Colonne colonne) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_COLONNE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, colonne.getName());
            stmt.setInt(2, colonne.getPosition());
            stmt.setLong(3, colonne.getTableauId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    colonne.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde de la colonne", e);
        }
        return colonne;
    }

    @Override
    public Optional<Colonne> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_COLONNE_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de la colonne", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Colonne> findAll() {
        List<Colonne> colonnes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Requetes.FIND_ALL_COLONNES)) {

            while (rs.next()) {
                colonnes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des colonnes", e);
        }
        return colonnes;
    }

    @Override
    public List<Colonne> findByTableauId(Long tableauId) {
        List<Colonne> colonnes = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_COLONNES_BY_TABLEAU)) {

            stmt.setLong(1, tableauId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    colonnes.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des colonnes", e);
        }
        return colonnes;
    }

    private Colonne mapRow(ResultSet rs) throws SQLException {
        return Colonne.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .position(rs.getInt("position"))
                .tableauId(rs.getLong("tableau_id"))
                .build();
    }
}
