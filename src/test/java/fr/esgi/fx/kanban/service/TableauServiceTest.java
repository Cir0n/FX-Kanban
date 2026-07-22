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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableauServiceTest {

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
    void testCreer_whenNameIsBlank_shouldThrowException() {
        String name = "";
        Long utilisateurId = 1L;

        assertThrows(IllegalArgumentException.class,
                () -> tableauServiceImpl.creer(name, utilisateurId));

        verify(tableauRepository, never()).save(any(Tableau.class));
    }

    @Test
    void testCreer_whenNameIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> tableauServiceImpl.creer(null, 1L));

        verify(tableauRepository, never()).save(any(Tableau.class));
    }

    @Test
    void testCreer_whenNameIsValid_shouldSaveAndAddCreatorAsContributeur() {
        when(tableauRepository.save(any(Tableau.class))).thenAnswer(invocation -> {
            Tableau saved = invocation.getArgument(0);
            saved.setId(21L);
            return saved;
        });

        Tableau result = tableauServiceImpl.creer("Tableau 1", 5L, "cs_test_123");

        assertNotNull(result);
        assertEquals(21L, result.getId());
        assertEquals("cs_test_123", result.getStripeSessionId());
        verify(tableauRepository).save(any(Tableau.class));
        verify(tableauRepository).addContributeur(21L, 5L);
    }

    @Test
    void testFindById_whenIdDoesNotExist_shouldThrowException() {
        when(tableauRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> tableauServiceImpl.findById(99L));
    }

    @Test
    void testFindById_whenIdExists_shouldReturnTableau() {
        Long idTab = 37L;
        String name = "test";
        Long idUser = 2L;
        Tableau tableauAttendu = Tableau.builder().id(idTab).name(name).createdBy(idUser).build();
        when(tableauRepository.findById(idTab)).thenReturn(Optional.of(tableauAttendu));

        Tableau result = tableauServiceImpl.findById(idTab);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(idTab, result.getId());
        verify(tableauRepository).findById(idTab);
    }

    @Test
    void testFindAllByContributeur_shouldDelegate() {
        when(tableauRepository.findAllByContributeur(7L)).thenReturn(List.of(
                Tableau.builder().id(1L).name("A").build(),
                Tableau.builder().id(2L).name("B").build()
        ));

        List<Tableau> result = tableauServiceImpl.findAllByContributeur(7L);

        assertEquals(2, result.size());
        verify(tableauRepository).findAllByContributeur(7L);
    }

    @Test
    void testFindContributeurs_shouldDelegate() {
        when(tableauRepository.findContributeurs(10L)).thenReturn(List.of(
                Utilisateur.builder().id(1L).pseudo("u1").build(),
                Utilisateur.builder().id(2L).pseudo("u2").build()
        ));

        List<Utilisateur> result = tableauServiceImpl.findContributeurs(10L);

        assertEquals(2, result.size());
        verify(tableauRepository).findContributeurs(10L);
    }

    @Test
    void testInviterContributeur_whenPseudoNotFound_shouldThrow() {
        when(utilisateurRepository.findByPseudo("unknown")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> tableauServiceImpl.inviterContributeur(3L, "unknown"));

        verify(tableauRepository, never()).addContributeur(any(Long.class), any(Long.class));
    }

    @Test
    void testInviterContributeur_whenAlreadyMember_shouldThrow() {
        Utilisateur invited = Utilisateur.builder().id(50L).pseudo("already_here").build();
        when(utilisateurRepository.findByPseudo("already_here")).thenReturn(Optional.of(invited));
        when(tableauRepository.findContributeurs(3L)).thenReturn(List.of(invited));

        assertThrows(IllegalArgumentException.class,
                () -> tableauServiceImpl.inviterContributeur(3L, "already_here"));

        verify(tableauRepository, never()).addContributeur(any(Long.class), any(Long.class));
    }

    @Test
    void testInviterContributeur_whenValid_shouldAddContributeur() {
        Utilisateur invited = Utilisateur.builder().id(50L).pseudo("new_member").build();
        when(utilisateurRepository.findByPseudo("new_member")).thenReturn(Optional.of(invited));
        when(tableauRepository.findContributeurs(3L)).thenReturn(List.of());

        tableauServiceImpl.inviterContributeur(3L, "new_member");

        verify(tableauRepository).addContributeur(3L, 50L);
    }

    @Test
    void testSupprimer_shouldDelegateToRepository() {
        tableauServiceImpl.supprimer(88L);

        verify(tableauRepository).delete(88L);
    }
}
