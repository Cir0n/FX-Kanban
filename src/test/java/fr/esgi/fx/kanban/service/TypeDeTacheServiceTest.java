package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.TypeDeTache;
import fr.esgi.fx.kanban.repository.ITypeDeTacheRepository;
import fr.esgi.fx.kanban.service.implementation.TypeDeTacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeDeTacheServiceTest {

    @Mock
    private ITypeDeTacheRepository typeDeTacheRepository;

    private TypeDeTacheServiceImpl typeDeTacheService;

    @BeforeEach
    void setUp() {
        typeDeTacheService = new TypeDeTacheServiceImpl(typeDeTacheRepository);
    }

    @Test
    void testFindAll_shouldDelegate() {
        when(typeDeTacheRepository.findAll()).thenReturn(List.of(
                TypeDeTache.builder().id(1L).name("Standard").couleur("#3498db").build(),
                TypeDeTache.builder().id(2L).name("Bug").couleur("#e74c3c").build()
        ));

        List<TypeDeTache> result = typeDeTacheService.findAll();

        assertEquals(2, result.size());
        verify(typeDeTacheRepository).findAll();
    }

    @Test
    void testFindById_whenFound_shouldReturnType() {
        TypeDeTache type = TypeDeTache.builder().id(3L).name("Spike").couleur("#9b59b6").build();
        when(typeDeTacheRepository.findById(3L)).thenReturn(Optional.of(type));

        Optional<TypeDeTache> result = typeDeTacheService.findById(3L);

        assertTrue(result.isPresent());
        assertEquals("Spike", result.get().getName());
    }

    @Test
    void testFindById_whenNotFound_shouldReturnEmpty() {
        when(typeDeTacheRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<TypeDeTache> result = typeDeTacheService.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindById_whenIdIsNull_shouldReturnEmpty() {
        when(typeDeTacheRepository.findById(null)).thenReturn(Optional.empty());

        Optional<TypeDeTache> result = typeDeTacheService.findById(null);

        assertTrue(result.isEmpty());
    }
}

