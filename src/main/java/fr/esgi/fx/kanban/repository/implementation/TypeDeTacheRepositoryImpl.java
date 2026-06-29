package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.phil.kanban.model.TypeDeTache;
import fr.esgi.phil.kanban.repository.ConnectionManager;
import fr.esgi.phil.kanban.repository.ITypeDeTacheRepository;
import fr.esgi.phil.kanban.repository.Requetes;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeDeTacheRepositoryImpl implements ITypeDeTacheRepository {

    @Override
    public List<TypeDeTache> findAll() {
        List<TypeDeTache> types = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(Requetes.FIND_ALL_TYPES)) {

            while (rs.next()) {
                types.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des types de tâche", e);
        }
        return types;
    }

    @Override
    public Optional<TypeDeTache> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_TYPE_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche du type de tâche", e);
        }
        return Optional.empty();
    }

    @Override
    public TypeDeTache save(TypeDeTache typeDeTache) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_TYPE, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, typeDeTache.getName());
            stmt.setString(2, typeDeTache.getCouleur());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    typeDeTache.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du type de tâche", e);
        }
        return typeDeTache;
    }

    private TypeDeTache mapRow(ResultSet rs) throws SQLException {
        return TypeDeTache.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .couleur(rs.getString("couleur"))
                .build();
    }
}
