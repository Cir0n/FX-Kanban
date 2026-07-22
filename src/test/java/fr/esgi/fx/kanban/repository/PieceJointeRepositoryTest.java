package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.PieceJointeRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TacheRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PieceJointeRepositoryTest {

    private PieceJointeRepositoryImpl pieceJointeRepository;
    private Long utilisateurId;
    private Long tableauId;
    private Long tacheId;
    private Long autreTacheId;

    @BeforeEach
    void setUp() {
        pieceJointeRepository = new PieceJointeRepositoryImpl();

        UtilisateurRepositoryImpl utilisateurRepository = new UtilisateurRepositoryImpl();
        Utilisateur utilisateur = utilisateurRepository.save(Utilisateur.builder()
                .pseudo("pj_test_user")
                .email("pj_test@test.com")
                .password("pass")
                .build());
        utilisateurId = utilisateur.getId();

        TableauRepositoryImpl tableauRepository = new TableauRepositoryImpl();
        Tableau tableau = tableauRepository.save(Tableau.builder()
                .name("Tableau Test Piece Jointe")
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
                .name("Tache piece jointe")
                .description("Desc")
                .colonneId(colonneId)
                .typeId(1L)
                .createdBy(utilisateurId)
                .build()).getId();

        autreTacheId = tacheRepository.save(Tache.builder()
                .name("Autre tache piece jointe")
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
            stmt.executeUpdate("DELETE FROM piece_jointe WHERE tache_id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM tache WHERE id IN (" + tacheId + "," + autreTacheId + ")");
            stmt.executeUpdate("DELETE FROM colonne WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur_tableau WHERE tableau_id = " + tableauId);
            stmt.executeUpdate("DELETE FROM tableau WHERE id = " + tableauId);
            stmt.executeUpdate("DELETE FROM utilisateur WHERE id = " + utilisateurId);
        }
    }

    @Test
    void testSave_shouldPersistAttachmentWithBinaryContent() {
        byte[] contenu = "contenu binaire".getBytes(StandardCharsets.UTF_8);
        PieceJointe pieceJointe = PieceJointe.builder()
                .nomFichier("spec.txt")
                .mimeType("text/plain")
                .contenu(contenu)
                .tacheId(tacheId)
                .build();

        PieceJointe saved = pieceJointeRepository.save(pieceJointe);

        assertNotNull(saved.getId(), "La piece jointe sauvegardee devrait avoir un ID.");

        List<PieceJointe> pieces = pieceJointeRepository.findByTacheId(tacheId);
        assertEquals(1, pieces.size());
        assertEquals("spec.txt", pieces.getFirst().getNomFichier());
        assertArrayEquals(contenu, pieces.getFirst().getContenu());
    }

    @Test
    void testFindByTacheId_shouldReturnOnlyAttachmentsForRequestedTask() {
        pieceJointeRepository.save(PieceJointe.builder()
                .nomFichier("t1.txt")
                .mimeType("text/plain")
                .contenu("1".getBytes(StandardCharsets.UTF_8))
                .tacheId(tacheId)
                .build());
        pieceJointeRepository.save(PieceJointe.builder()
                .nomFichier("t2.txt")
                .mimeType("text/plain")
                .contenu("2".getBytes(StandardCharsets.UTF_8))
                .tacheId(autreTacheId)
                .build());

        List<PieceJointe> piecesTache1 = pieceJointeRepository.findByTacheId(tacheId);
        List<PieceJointe> piecesTache2 = pieceJointeRepository.findByTacheId(autreTacheId);

        assertEquals(1, piecesTache1.size());
        assertEquals("t1.txt", piecesTache1.getFirst().getNomFichier());
        assertEquals(1, piecesTache2.size());
        assertEquals("t2.txt", piecesTache2.getFirst().getNomFichier());
    }

    @Test
    void testDelete_shouldRemoveAttachment() {
        PieceJointe saved = pieceJointeRepository.save(PieceJointe.builder()
                .nomFichier("to_delete.txt")
                .mimeType("text/plain")
                .contenu("delete".getBytes(StandardCharsets.UTF_8))
                .tacheId(tacheId)
                .build());

        pieceJointeRepository.delete(saved.getId());

        List<PieceJointe> pieces = pieceJointeRepository.findByTacheId(tacheId);
        assertTrue(pieces.isEmpty(), "La piece jointe aurait du etre supprimee.");
    }
}

