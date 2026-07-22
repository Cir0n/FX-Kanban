package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UtilisateurRepositoryTest {

    private IUtilisateurRepository repository;
    /**
     * IDs des utilisateurs créés par les tests. Permet un @AfterEach ciblé
     * qui ne supprime que les lignes créées par CE test, évitant toute
     * interférence avec d'autres suites de tests exécutées en parallèle.
     */
    private final List<Long> createdIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        repository = new UtilisateurRepositoryImpl();
        createdIds.clear();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (createdIds.isEmpty()) return;
        String ids = createdIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM utilisateur_tableau WHERE utilisateur_id IN (" + ids + ")");
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id IN (" + ids + ")");
        }
    }

    /** Sauvegarde et enregistre l'ID pour le nettoyage automatique. */
    private Utilisateur saveAndTrack(Utilisateur utilisateur) {
        Utilisateur saved = repository.save(utilisateur);
        createdIds.add(saved.getId());
        return saved;
    }

    @Test
    void testSave_shouldPersistUserAndReturnWithId() {
        String email = "save_test@email.com";
        Utilisateur utilisateur = Utilisateur.builder()
                .pseudo("save_test_pseudo")
                .email(email)
                .password("password123")
                .build();

        Utilisateur saved = saveAndTrack(utilisateur);

        Optional<Utilisateur> found = repository.findByEmail(email);
        assertTrue(found.isPresent(), "L'utilisateur aurait dû être trouvé dans la base.");
        assertEquals("save_test_pseudo", found.get().getPseudo());
        assertNotNull(saved.getId(), "L'ID aurait dû être généré.");
    }

    @Test
    void testFindByEmail_whenUserExists_shouldReturnUser() {
        saveAndTrack(Utilisateur.builder().pseudo("find_me").email("find@email.com").password("password").build());

        Optional<Utilisateur> result = repository.findByEmail("find@email.com");

        assertTrue(result.isPresent(), "L'utilisateur aurait dû être trouvé.");
        assertEquals("find_me", result.get().getPseudo());
        assertEquals("find@email.com", result.get().getEmail());
    }

    @Test
    void testFindByPseudo_whenUserExists_shouldReturnUser() {
        String pseudo = "find_me_by_pseudo";
        saveAndTrack(Utilisateur.builder().pseudo(pseudo).email("find_pseudo@email.com").password("password123").build());

        Optional<Utilisateur> result = repository.findByPseudo(pseudo);

        assertTrue(result.isPresent(), "L'utilisateur aurait dû être trouvé.");
        assertEquals(pseudo, result.get().getPseudo());
    }

    @Test
    void testFindByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<Utilisateur> result = repository.findByEmail("nonexistent_ut@email.com");

        assertTrue(result.isEmpty(), "La méthode aurait dû retourner un Optional vide.");
    }

    @Test
    void testFindAll_whenMultipleUsersExist_shouldReturnAtLeastCreatedUsers() {
        saveAndTrack(Utilisateur.builder().pseudo("ut_user1").email("ut_user1@email.com").password("pass1").build());
        saveAndTrack(Utilisateur.builder().pseudo("ut_user2").email("ut_user2@email.com").password("pass2").build());

        List<Utilisateur> utilisateurs = repository.findAll();

        // On vérifie qu'au moins les 2 utilisateurs créés sont présents (la base peut en contenir d'autres)
        assertTrue(utilisateurs.size() >= 2, "La liste devrait contenir au moins 2 utilisateurs.");
        assertTrue(utilisateurs.stream().anyMatch(u -> "ut_user1".equals(u.getPseudo())));
        assertTrue(utilisateurs.stream().anyMatch(u -> "ut_user2".equals(u.getPseudo())));
    }

    @Test
    void testUpdate_shouldModifyUserInDatabase() {
        Utilisateur saved = saveAndTrack(
                Utilisateur.builder().pseudo("pseudo_original").email("update@email.com").password("pass").build());

        saved.setPseudo("pseudo_modifie");
        repository.update(saved);

        Utilisateur updated = repository.findByEmail("update@email.com").orElseThrow();
        assertEquals("pseudo_modifie", updated.getPseudo(), "Le pseudo aurait dû être mis à jour.");
    }

    @Test
    void testDelete_shouldRemoveUserFromDatabase() {
        Utilisateur saved = saveAndTrack(
                Utilisateur.builder().pseudo("to_delete").email("delete@email.com").password("pass").build());

        repository.delete(saved.getId());
        createdIds.remove(saved.getId()); // déjà supprimé, inutile de le supprimer dans tearDown

        Optional<Utilisateur> deleted = repository.findByEmail("delete@email.com");
        assertTrue(deleted.isEmpty(), "L'utilisateur aurait dû être supprimé de la base.");
    }
}
