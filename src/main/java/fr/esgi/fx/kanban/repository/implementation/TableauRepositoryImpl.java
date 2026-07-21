package fr.esgi.fx.kanban.repository.implementation;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.ConnectionManager;
import fr.esgi.fx.kanban.repository.ITableauRepository;
import fr.esgi.fx.kanban.repository.Requetes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableauRepositoryImpl implements ITableauRepository {

    private static final Logger LOGGER = LogManager.getLogger(TableauRepositoryImpl.class);

    @Override
    public Tableau save(Tableau tableau) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     Requetes.INSERT_TABLEAU, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tableau.getName());
            stmt.setLong(2, tableau.getCreatedBy());
            stmt.setString(3, tableau.getStripeSessionId());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    tableau.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la sauvegarde du tableau créé par l'utilisateur id={}", tableau.getCreatedBy(), e);
            throw new RuntimeException("Erreur lors de la sauvegarde du tableau", e);
        }
        return tableau;
    }

    @Override
    public Optional<Tableau> findById(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_TABLEAU_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la recherche du tableau id={}", id, e);
            throw new RuntimeException("Erreur lors de la recherche du tableau", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tableau> findAllByContributeur(Long utilisateurId) {
        List<Tableau> tableaux = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_TABLEAUX_BY_CONTRIBUTEUR)) {

            stmt.setLong(1, utilisateurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableaux.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la recherche des tableaux du contributeur id={}", utilisateurId, e);
            throw new RuntimeException("Erreur lors de la recherche des tableaux", e);
        }
        return tableaux;
    }

    @Override
    public void update(Tableau tableau) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.UPDATE_TABLEAU)) {

            stmt.setString(1, tableau.getName());
            stmt.setLong(2, tableau.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la mise à jour du tableau id={}", tableau.getId(), e);
            throw new RuntimeException("Erreur lors de la mise à jour du tableau", e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.DELETE_TABLEAU)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression du tableau id={}", id, e);
            throw new RuntimeException("Erreur lors de la suppression du tableau", e);
        }
    }

    @Override
    public void addContributeur(Long tableauId, Long utilisateurId) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.INSERT_CONTRIBUTEUR)) {

            stmt.setLong(1, utilisateurId);
            stmt.setLong(2, tableauId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de l'ajout du contributeur id={} au tableau id={}", utilisateurId, tableauId, e);
            throw new RuntimeException("Erreur lors de l'ajout du contributeur", e);
        }
    }

    @Override
    public List<Utilisateur> findContributeurs(Long tableauId) {
        List<Utilisateur> contributeurs = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(Requetes.FIND_CONTRIBUTEURS)) {

            stmt.setLong(1, tableauId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    contributeurs.add(Utilisateur.builder()
                            .id(rs.getLong("id"))
                            .pseudo(rs.getString("pseudo"))
                            .email(rs.getString("email"))
                            .password(rs.getString("password"))
                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                            .build());
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la récupération des contributeurs du tableau id={}", tableauId, e);
            throw new RuntimeException("Erreur lors de la récupération des contributeurs", e);
        }
        return contributeurs;
    }

    private Tableau mapRow(ResultSet rs) throws SQLException {
        return Tableau.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .createdBy(rs.getLong("created_by"))
                .stripeSessionId(rs.getString("stripe_session_id"))
                .build();
    }
}
