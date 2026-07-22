package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Commentaire;
import fr.esgi.fx.kanban.repository.ICommentaireRepository;
import fr.esgi.fx.kanban.service.implementation.CommentaireServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentaireServiceTest {

    @Mock
    private ICommentaireRepository commentaireRepository;

    private CommentaireServiceImpl commentaireService;

    @BeforeEach
    void setUp() {
        commentaireService = new CommentaireServiceImpl(commentaireRepository);
    }

    @Test
    void testAjouter_whenContentIsBlank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> commentaireService.ajouter(" ", 2L, 9L));
    }

    @Test
    void testAjouter_whenContentIsValid_shouldSaveComment() {
        when(commentaireRepository.save(any(Commentaire.class))).thenAnswer(invocation -> {
            Commentaire saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        Commentaire result = commentaireService.ajouter("Mon commentaire", 2L, 9L);

        assertEquals(20L, result.getId());
        assertEquals("Mon commentaire", result.getContent());
        assertEquals(2L, result.getTacheId());
        assertEquals(9L, result.getUtilisateurId());
        verify(commentaireRepository).save(any(Commentaire.class));
    }

    @Test
    void testFindByTacheId_shouldDelegate() {
        when(commentaireRepository.findByTacheId(7L)).thenReturn(List.of(
                Commentaire.builder().id(1L).content("A").tacheId(7L).build(),
                Commentaire.builder().id(2L).content("B").tacheId(7L).build()
        ));

        List<Commentaire> result = commentaireService.findByTacheId(7L);

        assertEquals(2, result.size());
        verify(commentaireRepository).findByTacheId(7L);
    }
}

