package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.ActionRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
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

class ActionRepositoryTest {

    private ActionRepositoryImpl actionRepository;
    private Long utilisateurId;
    private Long tableauId;
    private Long colonneIdSource;
    private Long colonneIdCible;
    private Long tacheId;
    private Long autreTacheId;

    @BeforeEach
    void setUp() {
        actionRepository = new ActionRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepository = new UtilisateurRepositoryImpl();
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("action_test_user")
                .email("action_test@test.com")
                .password("pass")
                .build());
        utilisateurId = utilisateur.getId();

        TableauRepositoryImpl tableauRepository = new TableauRepositoryImpl();
        Tableau tableau = tableauRepository.save(Tableau.builder()
                .name("Tableau Test Action")
                .createdBy(utilisateurId)
                .build());
        tableauId = tableau.getId();

        ColonneRepositoryImpl colonneRepository = new ColonneRepositoryImpl();
        colonneIdSource = colonneRepository.save(Colonne.builder()
                .name("A faire")
                .position(1)
                .tableauId(tableauId)
                .build()).getId();
        colonneIdCible = colonneRepository.save(Colonne.builder()
                .name("Fait")
                .position(2)
                .tableauId(tableauId)
                .build()).getId();

        TacheRepositoryImpl tacheRepository = new TacheRepositoryImpl();
        tacheId = tacheRepository.save(Tache.builder()
                .name("Tache action")
                .description("Desc")
                .colonneId(colonneIdSource)
                .typeId(1L)
                .createdBy(utilisateurId)
                .build()).getId();

        autreTacheId = tacheRepository.save(Tache.builder()
                .name("Autre tache action")
                .description("Desc")
                .colonneId(colonneIdSource)
                .typeId(1L)
                .createdBy(utilisateurId)
                .build()).getId();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM action WHERE tache_id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM tache WHERE id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur_tableau WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM tableau WHERE id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id = " + utilisateurId);
        }
    }

    @Test
    void testSave_shouldPersistActionWithGeneratedId() {
        Action action = Action.builder()
                .description("Creation de la tache")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .colonneSourceId(null)
                .colonneCibleId(null)
                .build();

        Action saved = actionRepository.save(action);

        assertNotNull(saved.getId(), "L'action sauvegardee devrait avoir un ID.");

        List<Action> actions = actionRepository.findByTacheId(tacheId);
        assertEquals(1, actions.size(), "Une action devrait etre retrouvee pour la tache.");
        assertNull(actions.getFirst().getColonneSourceId());
        assertNull(actions.getFirst().getColonneCibleId());
    }

    @Test
    void testSave_shouldPersistActionWithSourceAndTargetColumns() {
        actionRepository.save(Action.builder()
                .description("Deplacement")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .colonneSourceId(colonneIdSource)
                .colonneCibleId(colonneIdCible)
                .build());

        List<Action> actions = actionRepository.findByTacheId(tacheId);

        assertEquals(1, actions.size());
        Action reloaded = actions.getFirst();
        assertEquals(colonneIdSource, reloaded.getColonneSourceId());
        assertEquals(colonneIdCible, reloaded.getColonneCibleId());
    }

    @Test
    void testFindByTacheId_shouldFilterByTask() {
        actionRepository.save(Action.builder()
                .description("Action tache 1")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .build());
        actionRepository.save(Action.builder()
                .description("Action tache 2")
                .tacheId(autreTacheId)
                .utilisateurId(utilisateurId)
                .build());

        List<Action> actionsTache1 = actionRepository.findByTacheId(tacheId);
        List<Action> actionsTache2 = actionRepository.findByTacheId(autreTacheId);

        assertEquals(1, actionsTache1.size(), "Une seule action attendue pour la tache 1.");
        assertEquals("Action tache 1", actionsTache1.getFirst().getDescription());
        assertEquals(1, actionsTache2.size(), "Une seule action attendue pour la tache 2.");
        assertEquals("Action tache 2", actionsTache2.getFirst().getDescription());
    }
}

