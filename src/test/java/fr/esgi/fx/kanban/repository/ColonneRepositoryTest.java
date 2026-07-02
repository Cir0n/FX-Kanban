package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.model.Tableau;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ColonneRepositoryTest {

    private ColonneRepositoryImpl colonneRepository;
    private Long tableauId;
    private Long utilisateurId;

    @BeforeEach
    void setUp() throws SQLException {
        colonneRepository = new ColonneRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepo = new UtilisateurRepositoryImpl();
        Utilisateur user = utilisateurRepo.save(
                Utilisateur.builder().pseudo("col_test_user").email("col_test@test.com").password("pass").build()
        );
        utilisateurId = user.getId();

        TableauRepositoryImpl tableauRepo = new TableauRepositoryImpl();
        Tableau tableau = tableauRepo.save(
                Tableau.builder().name("Tableau Test Colonne").createdBy(utilisateurId).build()
        );
        tableauId = tableau.getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM tableau WHERE id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id = " + utilisateurId);
        }
    }

    @Test
    void testSave_shouldPersistColumnWithGeneratedId() {
        Colonne colonne = Colonne.builder()
                .name("À faire")
                .position(1)
                .tableauId(tableauId)
                .build();

        Colonne saved = colonneRepository.save(colonne);

        assertNotNull(saved.getId(), "La colonne sauvegardée doit avoir un ID.");
        assertEquals("À faire", saved.getName());
        assertEquals(tableauId, saved.getTableauId());
    }

    @Test
    void testFindById_shouldReturnSavedColumn() {
        Colonne colonne = colonneRepository.save(
                Colonne.builder().name("En cours").position(2).tableauId(tableauId).build()
        );

        Optional<Colonne> result = colonneRepository.findById(colonne.getId());

        assertTrue(result.isPresent(), "La colonne doit être trouvée par son ID.");
        assertEquals("En cours", result.get().getName());
    }

    @Test
    void testFindByTableauId_shouldReturnAllColumnsForTableau() {
        colonneRepository.save(Colonne.builder().name("À faire").position(1).tableauId(tableauId).build());
        colonneRepository.save(Colonne.builder().name("En cours").position(2).tableauId(tableauId).build());
        colonneRepository.save(Colonne.builder().name("Terminé").position(3).tableauId(tableauId).build());

        List<Colonne> colonnes = colonneRepository.findByTableauId(tableauId);

        assertEquals(3, colonnes.size(), "Il devrait y avoir 3 colonnes pour ce tableau.");
    }
}
