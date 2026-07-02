package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.ITableauRepository;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.implementation.TableauServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableauServiceTest {

        @Mock
        private ITableauRepository tableauRepository;

        @Mock
        private IUtilisateurRepository utilisateurRepository;


        private TableauServiceImpl tableauServiceImpl;

    @BeforeEach
    void setUp() {
        tableauServiceImpl = new TableauServiceImpl(tableauRepository, utilisateurRepository);
    }

    @Test
    void testCreerTableau_whenNameIsBlank_shouldThrowException()
    {
        //Arrange
        String name = "";
        Long  utilisateurId = 1L;

        //Act & Assertt'
        assertThrows(IllegalArgumentException.class,
                () -> tableauServiceImpl.creer(name, utilisateurId));

        // Assert
        verify(tableauRepository, never()).save(any(Tableau.class));
    }

    @Test
    void testCreerTableau_whenNameIsNotNull_shouldReturnTableau() {
        // Arrange
        String name = "test";
        Long utilisateurId = 1L;
        Tableau tableauAttendu = Tableau.builder().id(1L).name(name).createdBy(utilisateurId).build();
        when(tableauRepository.save(any(Tableau.class))).thenReturn(tableauAttendu);

        // Act
        Tableau result = tableauServiceImpl.creer(name, utilisateurId);

        //Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        verify(tableauRepository).save(any(Tableau.class));
    }


    @Test
    void testFindbyId_whenIdIsNull_shouldThrowException()
    {
        //Arrange
        Long id = null;

        //Act

        assertThrows(IllegalArgumentException.class, () -> tableauServiceImpl.findById(null));

        //Assert
        verify(tableauRepository, never()).findById(any(Long.class));
    }

    @Test
    void testFindbyId_whenIdIsNotNull_shouldReturnTableau()
    {
        //Arrange
        Long idTab = 37L;
        String name = "test";
        Long idUser = 2L;
        Tableau tableauAttendu = Tableau.builder().id(idTab).name(name).createdBy(idUser).build();
        when(tableauRepository.findById(idTab)).thenReturn(Optional.of(tableauAttendu));

        //ActByID
        Tableau result = tableauServiceImpl.findById(idTab);

        //Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(idTab, result.getId());
        verify(tableauRepository).findById(any(Long.class));
    }
}
