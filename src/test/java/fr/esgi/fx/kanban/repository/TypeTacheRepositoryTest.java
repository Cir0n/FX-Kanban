package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.TypeDeTache;
import fr.esgi.fx.kanban.repository.implementation.TypeDeTacheRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TypeTacheRepositoryTest {

    private TypeDeTacheRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new TypeDeTacheRepositoryImpl();
    }

    @Test
    void testFindAll_shouldReturnInitialFourTaskTypes() {
        List<TypeDeTache> typeTaches = repository.findAll();

        assertNotNull(typeTaches, "La liste ne devrait pas être nulle.");
        assertEquals(4, typeTaches.size(), "Il devrait y avoir 4 types de tâches initiaux.");
    }

    @Test
    void testFindById_shouldReturnCorrectType() {
        Optional<TypeDeTache> result = repository.findById(2L);

        assertTrue(result.isPresent(), "Le type avec l'ID 2 (Bug) doit exister.");
        assertEquals("Bug", result.get().getName());
    }

    @Test
    void testFindById_whenNotExists_shouldReturnEmpty() {
        Optional<TypeDeTache> result = repository.findById(999L);

        assertTrue(result.isEmpty(), "Un ID inexistant doit retourner un Optional vide.");
    }
}
