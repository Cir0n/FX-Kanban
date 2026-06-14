package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.TypeTache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TypeTacheRepositoryTest {

    private TypeTacheRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TypeTacheRepository();
    }

    @Test
    void testFindAll_shouldReturnInitialFourTaskTypes() {
        // 1. Act
        List<TypeTache> typeTaches = repository.findAll();

        // 2. Assert
        assertNotNull(typeTaches, "La liste ne devrait pas être nulle.");
        assertEquals(4, typeTaches.size(), "Il devrait y avoir 4 types de tâches initiaux.");
    }
}