package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Utilisateur;
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

class UtilisateurRepositoryTest {

    private IUtilisateurRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UtilisateurRepositoryImpl();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM utilisateur");
        }
    }

    @Test
    void testSaveUtilisateur() {
        String email = "test@email.com";
        Utilisateur nouvelUtilisateur = Utilisateur.builder()
                .pseudo("test_pseudo")
                .email(email)
                .password("password123")
                .build();

        repository.save(nouvelUtilisateur);

        Optional<Utilisateur> saved = repository.findByEmail(email);
        assertTrue(saved.isPresent(), "L'utilisateur aurait dû être trouvé dans la base.");
        assertEquals("test_pseudo", saved.get().getPseudo());
    }

    @Test
    void testFindByEmail_whenUserExists_shouldReturnUser() {
        repository.save(Utilisateur.builder().pseudo("find_me").email("find@email.com").password("password").build());

        Optional<Utilisateur> result = repository.findByEmail("find@email.com");

        assertTrue(result.isPresent(), "L'utilisateur aurait dû être trouvé.");
        assertEquals("find_me", result.get().getPseudo());
        assertEquals("find@email.com", result.get().getEmail());
    }

    @Test
    void testFindByPseudo_whenUserExists_shouldReturnUser() {
        String pseudo = "find_me_by_pseudo";
        repository.save(Utilisateur.builder().pseudo(pseudo).email("find_pseudo@email.com").password("password123").build());

        Optional<Utilisateur> result = repository.findByPseudo(pseudo);

        assertTrue(result.isPresent(), "L'utilisateur aurait dû être trouvé.");
        assertEquals(pseudo, result.get().getPseudo());
    }

    @Test
    void testFindByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        Optional<Utilisateur> result = repository.findByEmail("nonexistent@email.com");

        assertTrue(result.isEmpty(), "La méthode aurait dû retourner un Optional vide.");
    }

    @Test
    void testFindAll_whenMultipleUsersExist_shouldReturnAllUsers() {
        repository.save(Utilisateur.builder().pseudo("user1").email("user1@email.com").password("pass1").build());
        repository.save(Utilisateur.builder().pseudo("user2").email("user2@email.com").password("pass2").build());

        List<Utilisateur> utilisateurs = repository.findAll();

        assertEquals(2, utilisateurs.size(), "La liste devrait contenir 2 utilisateurs.");
    }

    @Test
    void testUpdate_shouldModifyUserInDatabase() {
        String email = "update@email.com";
        repository.save(Utilisateur.builder().pseudo("pseudo_original").email(email).password("pass").build());

        Utilisateur toUpdate = repository.findByEmail(email).orElseThrow();
        toUpdate.setPseudo("pseudo_modifie");
        repository.update(toUpdate);

        Utilisateur updated = repository.findByEmail(email).orElseThrow();
        assertEquals("pseudo_modifie", updated.getPseudo(), "Le pseudo aurait dû être mis à jour.");
    }

    @Test
    void testDelete_shouldRemoveUserFromDatabase() {
        String email = "delete@email.com";
        repository.save(Utilisateur.builder().pseudo("to_delete").email(email).password("pass").build());

        Utilisateur toDelete = repository.findByEmail(email).orElseThrow();
        repository.delete(toDelete.getId());

        Optional<Utilisateur> deleted = repository.findByEmail(email);
        assertTrue(deleted.isEmpty(), "L'utilisateur aurait dû être supprimé de la base.");
    }
}
