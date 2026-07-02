package fr.esgi.fx.kanban.service;


import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.service.implementation.TacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TacheServiceTest {

    @Mock
    IActionRepository actionRepository;
    @Mock
    TacheServiceImpl tacheService;
    @Mock
    ITacheRepository tacheRepository;

    @BeforeEach
    void setUp(){
        tacheService = new TacheServiceImpl(tacheRepository, actionRepository);
    }


    @Test
    void testCreerTache_WhenUserIsNull_ShouldThrow(){

        //Arrange
        String name = null;

        //Act & assert
        assertThrows(IllegalArgumentException.class, () -> tacheService.creer(name, "test", 2l, 2l, 2L));

        //Assert
        verify(tacheRepository, never()).save(any(Tache.class));
    }

    @Test
    void testCreerTache_WhenUserIsNotNull_ShouldReturnTache(){

        //Arrange
        String name = "test";
        Long userId = 3L;
        Long colonneId = 2l;

        Tache tacheAttendu = Tache.builder()
                .name(name)
                .createdBy(userId)
                .colonneId(colonneId)
                .build();
        when(tacheRepository.save(any(Tache.class))).thenReturn(tacheAttendu);

        //Act
        Tache result = tacheService.creer(name, "", colonneId, null, userId);


        //Assert
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(userId, result.getCreatedBy());
        assertEquals(colonneId, result.getColonneId());
        verify(tacheRepository).save(any(Tache.class));
    }
//
//    @Override
//    public void deplacer(Long tacheId, Long nouvelleColonneId, Long utilisateurId) {
//        Tache tache = tacheRepository.findById(tacheId)
//                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));
//
//        Long ancienneColonneId = tache.getColonneId();
//        tache.setColonneId(nouvelleColonneId);
//        tacheRepository.update(tache);
//
//        Action action = Action.builder()
//                .description("Déplacement de la tâche")
//                .tacheId(tacheId)
//                .utilisateurId(utilisateurId)
//                .colonneSourceId(ancienneColonneId)
//                .colonneCibleId(nouvelleColonneId)
//                .build();
//        actionRepository.save(action);
//    }

    @Test
    void testDeplacer_WhenTacheIdIsNotFindable_ShouldThrow(){
        // Arrange
        when(tacheRepository.findById(anyLong())).thenReturn(Optional.empty());

        //Act
        assertThrows(IllegalArgumentException.class, () -> tacheService.deplacer(15L,12L,1L));

        //Assert
        verify(tacheRepository, never()).update(any(Tache.class));
        verify(actionRepository, never()).save(any(Action.class));
    }

    @Test
    void testDeplacer_ShouldUpdateTacheAndUpdateAction(){
        //Arrange


        //Act


        //Assert

    }
}
