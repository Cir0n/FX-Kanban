package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.implementation.UtilisateurServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private IUtilisateurRepository utilisateurRepository;

    private UtilisateurServiceImpl utilisateurService;

    @BeforeEach
    void setUp() {
        utilisateurService = new UtilisateurServiceImpl(utilisateurRepository);
    }

    @Test
    void testInscrire_whenEmailAndPseudoFree_shouldSaveUser() {
        String email = "nouveau@email.com";
        String pseudo = "nouveau_user";

        when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(utilisateurRepository.existsByEmail(email)).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        utilisateurService.inscrire(pseudo, email, "password123");

        verify(utilisateurRepository).save(any(Utilisateur.class));
    }

    @Test
    void testInscrire_whenEmailAlreadyExists_shouldThrow() {
        String email = "existant@email.com";
        String pseudo = "autre_user";

        when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(utilisateurRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.inscrire(pseudo, email, "password456"));

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void testInscrire_whenPseudoAlreadyExists_shouldThrow() {
        String pseudo = "pseudo_pris";

        when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.inscrire(pseudo, "libre@email.com", "password789"));

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }

    @Test
    void testInscrire_whenPasswordTooShort_shouldThrow() {
        String pseudo = "user_ok";
        String email = "ok@email.com";

        when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(utilisateurRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.inscrire(pseudo, email, "court"));

        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }
}
