package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Colonne;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ColonneRepositoryTest {

    private ColonneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ColonneRepository();
    }

    @Test
    void testFindAll_shouldReturnInitialThreeColumns() {
        // 1. Act
        List<Colonne> colonnes = repository.findAll();

        // 2. Assert
        assertNotNull(colonnes, "La liste ne devrait pas être nulle.");
        assertEquals(3, colonnes.size(), "Il devrait y avoir 3 colonnes initiales.");
    }
}