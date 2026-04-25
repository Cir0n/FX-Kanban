package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
}