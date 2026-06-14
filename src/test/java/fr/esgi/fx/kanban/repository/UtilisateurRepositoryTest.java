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
        repository = new UtilisateurRepositoryImpl();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Cette méthode est exécutée APRÈS chaque test
        // On vide la table pour que le test suivant parte d'une base propre
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM utilisateur");
        }
    }

    @Test
    void testSaveUtilisateur() {
        // 1. Arrange
        String emailDeTest = "test@email.com";
        // On crée un nouvel objet Utilisateur avec des données de test
        Utilisateur nouvelUtilisateur = new Utilisateur(null, "test_pseudo", emailDeTest, "password123");

        // 2. Act
        // On appelle la méthode que l'on veut tester
        repository.save(nouvelUtilisateur);

        // 3. Assert
        Utilisateur utilisateurSauvegarde = repository.findByEmail(emailDeTest);

        assertNotNull(utilisateurSauvegarde, "L'utilisateur aurait dû être trouvé dans la base.");
        assertEquals("test_pseudo", utilisateurSauvegarde.getPseudo(), "Le pseudo de l'utilisateur sauvegardé n'est pas correct.");
    }

    @Test
    void testFindByEmail_whenUserExists_shouldReturnUser() {
        // 1. Arrange
        // On insère un utilisateur de test directement dans la base
        Utilisateur utilisateurDeTest = new Utilisateur(null, "find_me", "find@email.com", "password");
        repository.save(utilisateurDeTest);

        // 2. Act
        // On essaie de le retrouver
        Utilisateur utilisateurTrouve = repository.findByEmail("find@email.com");

        // 3. Assert
        assertNotNull(utilisateurTrouve, "L'utilisateur aurait dû être trouvé.");
        assertEquals("find_me", utilisateurTrouve.getPseudo(), "Le pseudo trouvé n'est pas correct.");
        assertEquals("find@email.com", utilisateurTrouve.getEmail(), "L'email trouvé n'est pas correct.");
    }
    @Test
    void testFindByPseudo_whenUserExists_shouldReturnUser() {
        // 1.Arrange
        String pseudoDeTest = "find_me_by_pseudo";
        Utilisateur utilisateurDeTest = new Utilisateur(null, pseudoDeTest, "find_pseudo@email.com", "password123");
        repository.save(utilisateurDeTest);
        
        // 2. Act
        Utilisateur utilisateurTrouve = repository.findByPseudo(pseudoDeTest);
        
        // 3. Assert
        assertNotNull(utilisateurTrouve, "L'utilisateur aurait dû être trouvé.");
        assertEquals(pseudoDeTest, utilisateurTrouve.getPseudo(), "Le pseudo trouvé n'est pas correct.");
        
    }

    @Test
    void testFindByEmail_whenUserDoesNotExist_shouldReturnNull() {
        // 1. Arrange
        // La base est vide grâce à @AfterEach, donc aucun utilisateur avec cet email n'existe.

        // 2. Act
        Utilisateur utilisateurTrouve = repository.findByEmail("nonexistent@email.com");

        // 3. Assert
        assertNull(utilisateurTrouve, "La méthode aurait dû retourner null pour un utilisateur non existant.");
    }

    @Test
    void testFindAll_whenMultipleUsersExist_shouldReturnAllUsers() {
        // 1. Arrange
        // On insère deux utilisateurs
        repository.save(new Utilisateur(null, "user1", "user1@email.com", "pass1"));
        repository.save(new Utilisateur(null, "user2", "user2@email.com", "pass2"));

        // 2. Act
        // On appelle la méthode (qui existe maintenant)
        List<Utilisateur> utilisateurs = repository.findAll();

        // 3. Assert
        // On vérifiera que la liste contient bien 2 utilisateurs
        assertEquals(2, utilisateurs.size(), "La liste devrait contenir 2 utilisateurs.");
    }

    @Test
    void testUpdate_shouldModifyUserInDatabase() {
        // 1. Arrange
        // On insère un utilisateur initial
        String email = "update@email.com";
        repository.save(new Utilisateur(null, "pseudo_original", email, "pass"));

        // On le récupère pour avoir son ID et s'assurer qu'il est bien là
        Utilisateur utilisateurAModifier = repository.findByEmail(email);
        assertNotNull(utilisateurAModifier, "L'utilisateur à modifier doit exister avant la mise à jour.");

        // On modifie une de ses propriétés
        utilisateurAModifier.setPseudo("pseudo_modifie");

        // 2. Act
        // On appelle la méthode de mise à jour (qui n'existe pas encore)
        repository.update(utilisateurAModifier);

        // 3. Assert
        // On récupère à nouveau l'utilisateur depuis la base
        Utilisateur utilisateurMisAJour = repository.findByEmail(email);
        assertEquals("pseudo_modifie", utilisateurMisAJour.getPseudo(), "Le pseudo aurait dû être mis à jour.");
    }

    @Test
    void testDelete_shouldRemoveUserFromDatabase() {
        // 1. Arrange
        // On insère un utilisateur pour avoir quelque chose à supprimer
        String email = "delete@email.com";
        repository.save(new Utilisateur(null, "to_delete", email, "pass"));

        // On le récupère pour avoir son ID et s'assurer qu'il est bien là
        Utilisateur utilisateurASupprimer = repository.findByEmail(email);
        assertNotNull(utilisateurASupprimer, "L'utilisateur à supprimer doit exister avant la suppression.");
        Long idASupprimer = utilisateurASupprimer.getId();

        // 2. Act
        // On appelle la méthode de suppression (qui n'existe pas encore)
        repository.delete(idASupprimer);

        // 3. Assert
        // On essaie de retrouver l'utilisateur. Il ne doit plus exister.
        Utilisateur utilisateurSupprime = repository.findByEmail(email);
        assertNull(utilisateurSupprime, "L'utilisateur aurait dû être supprimé de la base.");
    }
}