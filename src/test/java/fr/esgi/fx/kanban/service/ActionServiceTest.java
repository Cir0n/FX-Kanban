package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.service.implementation.ActionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionServiceTest {

    @Mock
    private IActionRepository actionRepository;

    private ActionServiceImpl actionService;

    @BeforeEach
    void setUp() {
        actionService = new ActionServiceImpl(actionRepository);
    }

    @Test
    void testEnregistrer_shouldBuildAndSaveAction() {
        when(actionRepository.save(any(Action.class))).thenAnswer(invocation -> {
            Action saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Action result = actionService.enregistrer("Creation", 4L, 9L, null, 12L);

        assertEquals(1L, result.getId());
        assertEquals("Creation", result.getDescription());
        assertEquals(4L, result.getTacheId());
        assertEquals(9L, result.getUtilisateurId());
        assertEquals(12L, result.getColonneCibleId());
    }

    @Test
    void testFindByTacheId_shouldDelegate() {
        when(actionRepository.findByTacheId(5L)).thenReturn(List.of(
                Action.builder().id(10L).description("A1").tacheId(5L).build(),
                Action.builder().id(11L).description("A2").tacheId(5L).build()
        ));

        List<Action> result = actionService.findByTacheId(5L);

        assertEquals(2, result.size());
        verify(actionRepository).findByTacheId(5L);
    }
}

