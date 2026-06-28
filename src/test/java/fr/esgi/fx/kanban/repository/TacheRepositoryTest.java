package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Colonne;
import fr.esgi.fx.kanban.persistence.Tache;
import fr.esgi.fx.kanban.persistence.TypeTache;
import fr.esgi.fx.kanban.persistence.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TacheRepositoryTest {

    private TacheRepository tacheRepository;
    private UtilisateurRepository utilisateurRepository;
    private ColonneRepository colonneRepository;
    private TypeTacheRepository typeTacheRepository;

    @BeforeEach
    void setUp() {
        tacheRepository = new TacheRepository();
        utilisateurRepository = new UtilisateurRepositoryImpl();
        colonneRepository = new ColonneRepository();
        typeTacheRepository = new TypeTacheRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        //refresh la base de données après chaques tests
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM tache");
            stmt.executeUpdate("DELETE FROM utilisateur");
        }
    }

    @Test
    void testSaveTache() {
        // 1. Arrange
        // On crée un utilisateur de test
        Utilisateur utilisateur = new Utilisateur(null, "test_user", "test@email.com", "password");
        utilisateurRepository.save(utilisateur);
        Utilisateur savedUser = utilisateurRepository.findByPseudo("test_user");

        // On récupère les données de référence (qui existent grâce à import.sql)
        Colonne colonneAFaire = colonneRepository.findById(1); // "À faire"
        TypeTache typeBug = typeTacheRepository.findById(2); // "Bug"

        // On crée la tâche à sauvegarder
        Tache tacheASauvegarder = new Tache();
        tacheASauvegarder.setTitre("Corriger le bug d'affichage");
        tacheASauvegarder.setDescription("Le bouton est mal aligné sur la page d'accueil.");
        tacheASauvegarder.setColonne(colonneAFaire);
        tacheASauvegarder.setTypeTache(typeBug);
        tacheASauvegarder.setUtilisateur(savedUser);

        // 2. Act
        Tache tacheSauvegardee = tacheRepository.save(tacheASauvegarder);

        // 3. Assert
        // On vérifie que la méthode save a bien retourné un objet avec un ID
        assertNotNull(tacheSauvegardee, "La méthode save aurait dû retourner la tâche sauvegardée.");
        assertNotNull(tacheSauvegardee.getId(), "La tâche sauvegardée devrait avoir un ID non-nul.");

        // Pour être 100% sûr, on la relit depuis la base de données avec son nouvel ID
        Tache tacheRelue = tacheRepository.findById(tacheSauvegardee.getId());
        assertNotNull(tacheRelue, "La tâche aurait dû être retrouvée dans la base de données.");
        assertEquals("Corriger le bug d'affichage", tacheRelue.getTitre());
        assertEquals(savedUser.getId(), tacheRelue.getUtilisateur().getId());
        assertEquals("À faire", tacheRelue.getColonne().getNom());
        assertEquals("Bug", tacheRelue.getTypeTache().getLibelle());

    }

    @Test
    void testFindAll_shouldReturnAllSavedTasks() {
        // 1. Arrange
        // On prépare les données communes
        Colonne colonne = colonneRepository.findById(1);
        TypeTache typeTache = typeTacheRepository.findById(1);

        // On crée et sauvegarde deux tâches
        Tache tache1 = new Tache(null, "Tache 1", "Desc 1", null, colonne, typeTache, null);
        Tache tache2 = new Tache(null, "Tache 2", "Desc 2", null, colonne, typeTache, null);
        tacheRepository.save(tache1);
        tacheRepository.save(tache2);

        // 2. Act
        List<Tache> taches = tacheRepository.findAll();

        // 3. Assert
        assertNotNull(taches);
        assertEquals(2, taches.size(), "La méthode findAll devrait retourner 2 tâches.");
    }

    @Test
    void testUpdateTache_shouldChangeColumn() {
        // 1. Arrange
        // On crée une tâche initiale dans la colonne "À faire"
        Colonne colonneAFaire = colonneRepository.findById(1);
        TypeTache typeTache = typeTacheRepository.findById(1);
        Tache tache = new Tache(null, "Tâche à déplacer", "Desc", null, colonneAFaire, typeTache, null);
        Tache tacheSauvegardee = tacheRepository.save(tache);

        // On récupère la colonne "En cours"
        Colonne colonneEnCours = colonneRepository.findById(2);

        // On modifie la tâche pour la mettre dans la nouvelle colonne
        tacheSauvegardee.setColonne(colonneEnCours);

        // 2. Act
        tacheRepository.update(tacheSauvegardee);

        // 3. Assert
        // On relit la tâche depuis la base pour vérifier le changement
        Tache tacheMiseAJour = tacheRepository.findById(tacheSauvegardee.getId());
        assertNotNull(tacheMiseAJour);
        assertEquals(2, tacheMiseAJour.getColonne().getId(), "L'ID de la colonne aurait dû être 2 (En cours).");
        assertEquals("En cours", tacheMiseAJour.getColonne().getNom());
    }
    
    @Test
    void testDeleteTache_shouldRemoveTaskFromDatabase() {
        // 1. Arrange
        // On crée une tâche à supprimer
        Colonne colonne = colonneRepository.findById(1);
        TypeTache typeTache = typeTacheRepository.findById(1);
        Tache tache = new Tache(null, "Tâche à supprimer", "Desc", null, colonne, typeTache, null);
        Tache tacheSauvegardee = tacheRepository.save(tache);
        
        // On vérifie qu'elle existe bien avant de la supprimer
        assertNotNull(tacheSauvegardee, "La tâche à supprimer doit exister avant la suppression.");
        Long idASupprimer = tacheSauvegardee.getId();
        
        // 2. Act
        tacheRepository.delete(idASupprimer);

        // 3. Assert
        Tache tacheSupprimee = tacheRepository.findById(idASupprimer);
        assertNull(tacheSupprimee, "La tâche aurait dû être supprimée de la base.");
    }
                
}
