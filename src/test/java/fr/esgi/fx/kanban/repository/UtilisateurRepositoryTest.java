package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UtilisateurRepositoryTest {

    private UtilisateurRepository repository;

    @BeforeEach
    void setUp() {
        // Cette méthode est exécutée AVANT chaque test
        repository = new UtilisateurRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Cette méthode est exécutée APRÈS chaque test
        // On vide la table pour que le test suivant parte d'une base propre
        try (Connection conn = DriverManager.getConnection("jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE", "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM utilisateur");
        }
    }

    @Test
    void testSaveUtilisateur() {
        // 1. Préparation (Arrange)
        String emailDeTest = "test@email.com";
        // On crée un nouvel objet Utilisateur avec des données de test
        Utilisateur nouvelUtilisateur = new Utilisateur(null, "test_pseudo", emailDeTest, "password123");

        // 2. Action (Act)
        // On appelle la méthode que l'on veut tester
        repository.save(nouvelUtilisateur);

        // 3. Vérification (Assert)
        Utilisateur utilisateurSauvegarde = repository.findByEmail(emailDeTest);

        assertNotNull(utilisateurSauvegarde, "L'utilisateur aurait dû être trouvé dans la base.");
        assertEquals("test_pseudo", utilisateurSauvegarde.getPseudo(), "Le pseudo de l'utilisateur sauvegardé n'est pas correct.");
    }

    @Test
    void testFindByEmail_whenUserExists_shouldReturnUser() {
        // 1. Préparation (Arrange)
        // On insère un utilisateur de test directement dans la base
        Utilisateur utilisateurDeTest = new Utilisateur(null, "find_me", "find@email.com", "password");
        repository.save(utilisateurDeTest);

        // 2. Action (Act)
        // On essaie de le retrouver
        Utilisateur utilisateurTrouve = repository.findByEmail("find@email.com");

        // 3. Vérification (Assert)
        assertNotNull(utilisateurTrouve, "L'utilisateur aurait dû être trouvé.");
        assertEquals("find_me", utilisateurTrouve.getPseudo(), "Le pseudo trouvé n'est pas correct.");
        assertEquals("find@email.com", utilisateurTrouve.getEmail(), "L'email trouvé n'est pas correct.");
    }

    @Test
    void testFindByEmail_whenUserDoesNotExist_shouldReturnNull() {
        // 1. Préparation (Arrange)
        // La base est vide grâce à @AfterEach, donc aucun utilisateur avec cet email n'existe.

        // 2. Action (Act)
        Utilisateur utilisateurTrouve = repository.findByEmail("nonexistent@email.com");

        // 3. Vérification (Assert)
        assertNull(utilisateurTrouve, "La méthode aurait dû retourner null pour un utilisateur non existant.");
    }

    @Test
    void testFindAll_whenMultipleUsersExist_shouldReturnAllUsers() {
        // 1. Préparation (Arrange)
        // On insère deux utilisateurs
        repository.save(new Utilisateur(null, "user1", "user1@email.com", "pass1"));
        repository.save(new Utilisateur(null, "user2", "user2@email.com", "pass2"));

        // 2. Action (Act)
        // On appelle la méthode (qui existe maintenant)
        List<Utilisateur> utilisateurs = repository.findAll();

        // 3. Vérification (Assert)
        // On vérifiera que la liste contient bien 2 utilisateurs
        assertEquals(2, utilisateurs.size(), "La liste devrait contenir 2 utilisateurs.");
    }
}