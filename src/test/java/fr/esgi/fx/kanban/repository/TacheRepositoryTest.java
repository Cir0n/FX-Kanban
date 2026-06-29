package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TacheRepositoryImpl;
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

class TacheRepositoryTest {

    private TacheRepositoryImpl tacheRepository;
    private Long utilisateurId;
    private Long tableauId;
    private Long colonneId1;
    private Long colonneId2;

    // type_tache IDs 1 (Standard) et 2 (Bug) sont seedés par import.sql

    @BeforeEach
    void setUp() {
        tacheRepository = new TacheRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepo = new UtilisateurRepositoryImpl();
        Utilisateur user = utilisateurRepo.save(
                Utilisateur.builder().pseudo("tache_test_user").email("tache_test@test.com").password("pass").build()
        );
        utilisateurId = user.getId();

        TableauRepositoryImpl tableauRepo = new TableauRepositoryImpl();
        Tableau tableau = tableauRepo.save(
                Tableau.builder().name("Tableau Test Tache").createdBy(utilisateurId).build()
        );
        tableauId = tableau.getId();

        ColonneRepositoryImpl colonneRepo = new ColonneRepositoryImpl();
        colonneId1 = colonneRepo.save(Colonne.builder().name("À faire").position(1).tableauId(tableauId).build()).getId();
        colonneId2 = colonneRepo.save(Colonne.builder().name("En cours").position(2).tableauId(tableauId).build()).getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tache WHERE colonne_id IN (" + colonneId1 + "," + colonneId2 + ")");
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM tableau WHERE id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id = " + utilisateurId);
        }
    }

    @Test
    void testSaveTache() {
        Tache tache = Tache.builder()
                .name("Corriger le bug d'affichage")
                .description("Le bouton est mal aligné sur la page d'accueil.")
                .colonneId(colonneId1)
                .typeId(2L) // Bug
                .createdBy(utilisateurId)
                .build();

        Tache saved = tacheRepository.save(tache);

        assertNotNull(saved, "La méthode save aurait dû retourner la tâche sauvegardée.");
        assertNotNull(saved.getId(), "La tâche sauvegardée devrait avoir un ID non-nul.");

        Optional<Tache> reloaded = tacheRepository.findById(saved.getId());
        assertTrue(reloaded.isPresent(), "La tâche aurait dû être retrouvée dans la base.");
        assertEquals("Corriger le bug d'affichage", reloaded.get().getName());
        assertEquals(colonneId1, reloaded.get().getColonneId());
        assertEquals(2L, reloaded.get().getTypeId());
    }

    @Test
    void testFindAll_shouldReturnAllSavedTasks() {
        tacheRepository.save(Tache.builder().name("Tache 1").description("Desc 1").colonneId(colonneId1).typeId(1L).createdBy(utilisateurId).build());
        tacheRepository.save(Tache.builder().name("Tache 2").description("Desc 2").colonneId(colonneId1).typeId(1L).createdBy(utilisateurId).build());

        List<Tache> taches = tacheRepository.findAll();

        assertNotNull(taches);
        assertEquals(2, taches.size(), "La méthode findAll devrait retourner 2 tâches.");
    }

    @Test
    void testUpdateTache_shouldChangeColumn() {
        Tache tache = tacheRepository.save(
                Tache.builder().name("Tâche à déplacer").description("Desc").colonneId(colonneId1).typeId(1L).createdBy(utilisateurId).build()
        );

        tache.setColonneId(colonneId2);
        tacheRepository.update(tache);

        Tache updated = tacheRepository.findById(tache.getId()).orElseThrow();
        assertEquals(colonneId2, updated.getColonneId(), "La colonne aurait dû être mise à jour.");
    }

    @Test
    void testDeleteTache_shouldRemoveTaskFromDatabase() {
        Tache tache = tacheRepository.save(
                Tache.builder().name("Tâche à supprimer").description("Desc").colonneId(colonneId1).typeId(1L).createdBy(utilisateurId).build()
        );
        Long id = tache.getId();

        tacheRepository.delete(id);

        Optional<Tache> deleted = tacheRepository.findById(id);
        assertTrue(deleted.isEmpty(), "La tâche aurait dû être supprimée de la base.");
    }
}
