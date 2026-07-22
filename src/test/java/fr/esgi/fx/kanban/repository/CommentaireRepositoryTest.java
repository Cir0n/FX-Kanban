package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Commentaire;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.CommentaireRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TacheRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentaireRepositoryTest {

    private CommentaireRepositoryImpl commentaireRepository;
    private Long utilisateurId;
    private Long tableauId;
    private Long tacheId;
    private Long autreTacheId;

    @BeforeEach
    void setUp() {
        commentaireRepository = new CommentaireRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepository = new UtilisateurRepositoryImpl();
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("comment_test_user")
                .email("comment_test@test.com")
                .password("pass")
                .build());
        utilisateurId = utilisateur.getId();

        TableauRepositoryImpl tableauRepository = new TableauRepositoryImpl();
        Tableau tableau = tableauRepository.save(Tableau.builder()
                .name("Tableau Test Commentaire")
                .createdBy(utilisateurId)
                .build());
        tableauId = tableau.getId();

        ColonneRepositoryImpl colonneRepository = new ColonneRepositoryImpl();
        Long colonneId = colonneRepository.save(Colonne.builder()
                .name("A faire")
                .position(1)
                .tableauId(tableauId)
                .build()).getId();

        TacheRepositoryImpl tacheRepository = new TacheRepositoryImpl();
        tacheId = tacheRepository.save(Tache.builder()
                .name("Tache commentaire")
                .description("Desc")
                .colonneId(colonneId)
                .typeId(1L)
                .createdBy(utilisateurId)
                .build()).getId();

        autreTacheId = tacheRepository.save(Tache.builder()
                .name("Autre tache commentaire")
                .description("Desc")
                .colonneId(colonneId)
                .typeId(1L)
                .createdBy(utilisateurId)
                .build()).getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM commentaire WHERE tache_id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM tache WHERE id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur_tableau WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM tableau WHERE id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id = " + utilisateurId);
        }
    }

    @Test
    void testSave_shouldPersistCommentWithGeneratedId() {
        Commentaire commentaire = Commentaire.builder()
                .content("Premier commentaire")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .build();

        Commentaire saved = commentaireRepository.save(commentaire);

        assertNotNull(saved.getId(), "Le commentaire sauvegarde devrait avoir un ID.");

        List<Commentaire> commentaires = commentaireRepository.findByTacheId(tacheId);
        assertEquals(1, commentaires.size());
        assertEquals("Premier commentaire", commentaires.getFirst().getContent());
    }

    @Test
    void testFindByTacheId_shouldReturnCommentsForRequestedTaskOnly() {
        commentaireRepository.save(Commentaire.builder()
                .content("Commentaire tache 1")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .build());
        commentaireRepository.save(Commentaire.builder()
                .content("Commentaire tache 2")
                .tacheId(autreTacheId)
                .utilisateurId(utilisateurId)
                .build());

        List<Commentaire> commentairesTache1 = commentaireRepository.findByTacheId(tacheId);
        List<Commentaire> commentairesTache2 = commentaireRepository.findByTacheId(autreTacheId);

        assertEquals(1, commentairesTache1.size());
        assertEquals("Commentaire tache 1", commentairesTache1.getFirst().getContent());
        assertEquals(1, commentairesTache2.size());
        assertEquals("Commentaire tache 2", commentairesTache2.getFirst().getContent());
    }

    @Test
    void testFindByTacheId_whenNoCommentExists_shouldReturnEmptyList() {
        List<Commentaire> commentaires = commentaireRepository.findByTacheId(tacheId);

        assertNotNull(commentaires);
        assertTrue(commentaires.isEmpty(), "Aucun commentaire ne devrait etre retourne.");
    }
}

