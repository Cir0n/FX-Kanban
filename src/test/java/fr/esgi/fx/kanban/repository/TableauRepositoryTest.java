package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TableauRepositoryTest {

    private TableauRepositoryImpl tableauRepository;
    private Long createurId;
    private Long contributeurId;
    private Long autreContributeurId;
    private Long tableauId;
    private Long autreTableauId;

    @BeforeEach
    void setUp() {
        tableauRepository = new TableauRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepository = new UtilisateurRepositoryImpl();

        Utilisateur createur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("tab_createur")
                .email("tab_createur@test.com")
                .password("pass")
                .build());
        createurId = createur.getId();

        Utilisateur contributeur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("tab_contrib")
                .email("tab_contrib@test.com")
                .password("pass")
                .build());
        contributeurId = contributeur.getId();

        Utilisateur autreContributeur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("tab_contrib_2")
                .email("tab_contrib_2@test.com")
                .password("pass")
                .build());
        autreContributeurId = autreContributeur.getId();

        tableauId = tableauRepository.save(Tableau.builder()
                .name("Tableau principal")
                .createdBy(createurId)
                .stripeSessionId("cs_test_123")
                .build()).getId();

        autreTableauId = tableauRepository.save(Tableau.builder()
                .name("Tableau secondaire")
                .createdBy(createurId)
                .build()).getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM utilisateur_tableau WHERE tableau_id IN (" + tableauId + "," + autreTableauId + ")");
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id IN (" + tableauId + "," + autreTableauId + ")");
            stmt.executeUpdate("DELETE FROM tableau WHERE id IN (" + tableauId + "," + autreTableauId + ")");
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id IN (" + createurId + "," + contributeurId + "," + autreContributeurId + ")");
        }
    }

    @Test
    void testFindById_shouldReturnSavedTableau() {
        Optional<Tableau> result = tableauRepository.findById(tableauId);

        assertTrue(result.isPresent(), "Le tableau devrait etre retrouve par ID.");
        assertEquals("Tableau principal", result.get().getName());
        assertEquals(createurId, result.get().getCreatedBy());
        assertEquals("cs_test_123", result.get().getStripeSessionId());
    }

    @Test
    void testAddContributeur_andFindAllByContributeur_shouldReturnAssignedTableaux() {
        tableauRepository.addContributeur(tableauId, contributeurId);

        List<Tableau> tableaux = tableauRepository.findAllByContributeur(contributeurId);

        assertEquals(1, tableaux.size(), "Le contributeur devrait avoir un seul tableau assigne.");
        assertEquals(tableauId, tableaux.getFirst().getId());
    }

    @Test
    void testFindContributeurs_shouldReturnAllContributeursForTableau() {
        tableauRepository.addContributeur(tableauId, contributeurId);
        tableauRepository.addContributeur(tableauId, autreContributeurId);

        List<Utilisateur> contributeurs = tableauRepository.findContributeurs(tableauId);

        assertEquals(2, contributeurs.size(), "Le tableau devrait avoir 2 contributeurs.");
        assertTrue(contributeurs.stream().anyMatch(u -> u.getId().equals(contributeurId)));
        assertTrue(contributeurs.stream().anyMatch(u -> u.getId().equals(autreContributeurId)));
    }

    @Test
    void testDelete_shouldRemoveTableau() {
        tableauRepository.delete(autreTableauId);

        Optional<Tableau> deleted = tableauRepository.findById(autreTableauId);
        assertTrue(deleted.isEmpty(), "Le tableau supprime ne devrait plus exister.");
    }
}

