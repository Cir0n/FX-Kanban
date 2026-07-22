package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.repository.IColonneRepository;
import fr.esgi.fx.kanban.service.implementation.ColonneServiceImpl;
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
class ColonneServiceTest {

    @Mock
    private IColonneRepository colonneRepository;

    private ColonneServiceImpl colonneService;

    @BeforeEach
    void setUp() {
        colonneService = new ColonneServiceImpl(colonneRepository);
    }

    @Test
    void testCreer_whenNameIsBlank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> colonneService.creer(" ", 1, 3L));
    }

    @Test
    void testCreer_whenNameIsValid_shouldSaveColonne() {
        when(colonneRepository.save(any(Colonne.class))).thenAnswer(invocation -> {
            Colonne saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Colonne result = colonneService.creer("Todo", 1, 3L);

        assertEquals(100L, result.getId());
        assertEquals("Todo", result.getName());
        assertEquals(1, result.getPosition());
        assertEquals(3L, result.getTableauId());
        verify(colonneRepository).save(any(Colonne.class));
    }

    @Test
    void testFindByTableauId_shouldDelegate() {
        when(colonneRepository.findByTableauId(8L)).thenReturn(List.of(
                Colonne.builder().id(1L).name("Todo").position(1).tableauId(8L).build(),
                Colonne.builder().id(2L).name("Done").position(2).tableauId(8L).build()
        ));

        List<Colonne> result = colonneService.findByTableauId(8L);

        assertEquals(2, result.size());
        verify(colonneRepository).findByTableauId(8L);
    }
}

