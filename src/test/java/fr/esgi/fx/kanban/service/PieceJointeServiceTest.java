package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.IPieceJointeRepository;
import fr.esgi.fx.kanban.service.implementation.PieceJointeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PieceJointeServiceTest {

    @Mock
    private IPieceJointeRepository pieceJointeRepository;
    @Mock
    private IActionRepository actionRepository;

    private PieceJointeServiceImpl pieceJointeService;

    @BeforeEach
    void setUp() {
        pieceJointeService = new PieceJointeServiceImpl(pieceJointeRepository, actionRepository);
    }

    @Test
    void testAjouter_whenFileNameBlank_shouldThrow() {
        byte[] content = new byte[]{1};

        assertThrows(IllegalArgumentException.class,
                () -> pieceJointeService.ajouter(" ", "text/plain", content, 2L, 3L));

        verifyNoInteractions(pieceJointeRepository, actionRepository);
    }

    @Test
    void testAjouter_whenContentEmpty_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> pieceJointeService.ajouter("f.txt", "text/plain", new byte[0], 2L, 3L));

        verifyNoInteractions(pieceJointeRepository, actionRepository);
    }

    @Test
    void testAjouter_whenContentTooLarge_shouldThrow() {
        byte[] content = new byte[10 * 1024 * 1024 + 1];

        assertThrows(IllegalArgumentException.class,
                () -> pieceJointeService.ajouter("big.bin", "application/octet-stream", content, 2L, 3L));

        verifyNoInteractions(pieceJointeRepository, actionRepository);
    }

    @Test
    void testAjouter_whenValid_shouldSaveAttachmentAndHistoryAction() {
        byte[] content = new byte[]{10, 11, 12};
        when(pieceJointeRepository.save(any(PieceJointe.class))).thenAnswer(invocation -> {
            PieceJointe saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        PieceJointe result = pieceJointeService.ajouter("spec.png", " ", content, 10L, 20L);

        assertEquals(99L, result.getId());
        assertEquals("application/octet-stream", result.getMimeType());
        assertArrayEquals(content, result.getContenu());

        ArgumentCaptor<Action> actionCaptor = ArgumentCaptor.forClass(Action.class);
        verify(actionRepository).save(actionCaptor.capture());
        assertEquals("Ajout de la pièce jointe spec.png", actionCaptor.getValue().getDescription());
        assertEquals(10L, actionCaptor.getValue().getTacheId());
        assertEquals(20L, actionCaptor.getValue().getUtilisateurId());
    }

    @Test
    void testFindByTacheId_shouldDelegate() {
        when(pieceJointeRepository.findByTacheId(5L)).thenReturn(List.of(
                PieceJointe.builder().id(1L).nomFichier("a.txt").build()
        ));

        List<PieceJointe> result = pieceJointeService.findByTacheId(5L);

        assertEquals(1, result.size());
        verify(pieceJointeRepository).findByTacheId(5L);
    }

    @Test
    void testFindById_whenMissing_shouldThrow() {
        when(pieceJointeRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> pieceJointeService.findById(7L));
    }

    @Test
    void testSupprimer_shouldDeleteAttachmentAndCreateHistoryAction() {
        PieceJointe pieceJointe = PieceJointe.builder().id(7L).nomFichier("spec.pdf").tacheId(50L).build();
        when(pieceJointeRepository.findById(7L)).thenReturn(Optional.of(pieceJointe));

        pieceJointeService.supprimer(7L, 33L);

        verify(pieceJointeRepository).delete(7L);
        ArgumentCaptor<Action> actionCaptor = ArgumentCaptor.forClass(Action.class);
        verify(actionRepository).save(actionCaptor.capture());
        assertEquals("Suppression de la pièce jointe spec.pdf", actionCaptor.getValue().getDescription());
        assertEquals(50L, actionCaptor.getValue().getTacheId());
        assertEquals(33L, actionCaptor.getValue().getUtilisateurId());
    }
}

