package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.implementation.TacheServiceImpl;
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
class TacheServiceTest {

    @Mock
    private IActionRepository actionRepository;
    @Mock
    private ITacheRepository tacheRepository;
    @Mock
    private IUtilisateurRepository utilisateurRepository;
    @Mock
    private IEmailService emailService;

    private TacheServiceImpl tacheService;

    @BeforeEach
    void setUp() {
        tacheService = new TacheServiceImpl(tacheRepository, actionRepository, utilisateurRepository, emailService);
    }

    @Test
    void testCreer_whenNameIsBlank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tacheService.creer(" ", "test", 2L, 2L, null, 2L));

        verify(tacheRepository, never()).save(any(Tache.class));
        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void testCreer_whenValidAndNoAssignee_shouldSaveTaskAndActionWithoutEmail() {
        when(tacheRepository.save(any(Tache.class))).thenAnswer(invocation -> {
            Tache saved = invocation.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        Tache result = tacheService.creer("Tache 1", "Desc", 2L, 1L, null, 3L);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("Tache 1", result.getName());
        verify(tacheRepository).save(any(Tache.class));
        verify(actionRepository).save(any(Action.class));
        verify(utilisateurRepository, never()).findById(any(Long.class));
        verify(emailService, never()).envoyerNotificationAssignation(any(String.class), any(String.class));
    }

    @Test
    void testCreer_whenAssigneeExists_shouldSendNotification() {
        when(tacheRepository.save(any(Tache.class))).thenAnswer(invocation -> {
            Tache saved = invocation.getArgument(0);
            saved.setId(102L);
            return saved;
        });
        Utilisateur assigne = Utilisateur.builder().id(8L).email("assigne@test.com").build();
        when(utilisateurRepository.findById(8L)).thenReturn(Optional.of(assigne));

        tacheService.creer("Tache assignee", "Desc", 2L, 1L, 8L, 3L);

        verify(emailService).envoyerNotificationAssignation("assigne@test.com", "Tache assignee");
    }

    @Test
    void testFindById_whenMissing_shouldThrow() {
        when(tacheRepository.findById(15L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tacheService.findById(15L));
    }

    @Test
    void testFindByColonneId_shouldDelegate() {
        when(tacheRepository.findByColonneId(9L)).thenReturn(List.of(
                Tache.builder().id(1L).name("A").build(),
                Tache.builder().id(2L).name("B").build()
        ));

        List<Tache> result = tacheService.findByColonneId(9L);

        assertEquals(2, result.size());
        verify(tacheRepository).findByColonneId(9L);
    }

    @Test
    void testDeplacer_whenTacheNotFound_shouldThrow() {
        when(tacheRepository.findById(15L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tacheService.deplacer(15L, 12L, 1L));

        verify(tacheRepository, never()).update(any(Tache.class));
        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void testDeplacer_shouldUpdateTaskAndCreateHistoryAction() {
        Tache existing = Tache.builder().id(30L).name("Move me").colonneId(4L).build();
        when(tacheRepository.findById(30L)).thenReturn(Optional.of(existing));

        tacheService.deplacer(30L, 7L, 99L);

        verify(tacheRepository).update(existing);
        assertEquals(7L, existing.getColonneId());
        verify(actionRepository).save(argThat(action ->
                action.getDescription() != null
                        && Long.valueOf(30L).equals(action.getTacheId())
                        && Long.valueOf(99L).equals(action.getUtilisateurId())
                        && Long.valueOf(4L).equals(action.getColonneSourceId())
                        && Long.valueOf(7L).equals(action.getColonneCibleId())
        ));
    }

    @Test
    void testModifier_whenNameIsBlank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tacheService.modifier(1L, "", "d", 1L, null, 2L));

        verify(tacheRepository, never()).update(any(Tache.class));
    }

    @Test
    void testModifier_whenTacheNotFound_shouldThrow() {
        when(tacheRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> tacheService.modifier(1L, "Nom", "d", 1L, null, 2L));

        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void testModifier_whenAssigneeChanges_shouldUpdateAndNotify() {
        Tache existing = Tache.builder()
                .id(4L).name("Old").description("Old desc").typeId(1L).utilisateurId(null).build();
        when(tacheRepository.findById(4L)).thenReturn(Optional.of(existing));
        Utilisateur assigne = Utilisateur.builder().id(44L).email("new@test.com").build();
        when(utilisateurRepository.findById(44L)).thenReturn(Optional.of(assigne));

        tacheService.modifier(4L, "New", "New desc", 2L, 44L, 12L);

        verify(tacheRepository).update(existing);

        // ArgumentCaptor : diagnostics clairs sur chaque champ en cas d'échec
        ArgumentCaptor<Action> actionCaptor = ArgumentCaptor.forClass(Action.class);
        verify(actionRepository).save(actionCaptor.capture());
        Action capturedAction = actionCaptor.getValue();

        assertTrue(capturedAction.getDescription().contains("Assignation"),
                "La description devrait mentionner l'assignation");
        assertTrue(capturedAction.getDescription().contains("Renommage"),
                "La description devrait mentionner le renommage");
        assertTrue(capturedAction.getDescription().contains("Description"),
                "La description devrait mentionner la modification de description");
        assertTrue(capturedAction.getDescription().contains("Type"),
                "La description devrait mentionner le changement de type");
        assertEquals(4L,  capturedAction.getTacheId(),      "tacheId incorrect");
        assertEquals(12L, capturedAction.getUtilisateurId(), "utilisateurId incorrect");

        verify(emailService).envoyerNotificationAssignation("new@test.com", "New");
    }

    @Test
    void testModifier_whenAssigneeUnchanged_shouldNotNotify() {
        Tache existing = Tache.builder()
                .id(5L)
                .name("Task")
                .description("Desc")
                .typeId(1L)
                .utilisateurId(44L)
                .build();
        when(tacheRepository.findById(5L)).thenReturn(Optional.of(existing));

        tacheService.modifier(5L, "Task", "Desc", 1L, 44L, 12L);

        verify(emailService, never()).envoyerNotificationAssignation(any(String.class), any(String.class));
    }

    @Test
    void testSupprimer_shouldDelegateToRepository() {
        tacheService.supprimer(77L);
        verify(tacheRepository).delete(77L);
    }
}
