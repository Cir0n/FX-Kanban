package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.implementation.UtilisateurServiceImpl;
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
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        Utilisateur saved = utilisateurService.inscrire(pseudo, email, "password123");

        verify(utilisateurRepository).save(any(Utilisateur.class));
        assertNotNull(saved.getId());
        // Le mot de passe stocké ne doit pas être en clair — on ne teste pas le format interne
        assertNotEquals("password123", saved.getPassword());
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
        verify(utilisateurRepository, never()).existsByEmail(any(String.class));
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

    // ── Helper pour les tests connecter ──────────────────────────────────────
    /**
     * Crée un utilisateur avec un mot de passe haché via le service (PBKDF2).
     * Centralise la configuration des mocks nécessaires à inscrire(),
     * de sorte que les tests de connecter() n'aient plus à répéter ce setup.
     */
    private Utilisateur prepareUserSubscribed(String pseudo, String email, String password, Long id) {
        when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(false);
        when(utilisateurRepository.existsByEmail(email)).thenReturn(false);
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(id);
            return u;
        });
        return utilisateurService.inscrire(pseudo, email, password);
    }

    @Test
    void testConnecter_whenPseudoDoesNotExist_shouldThrow() {
        when(utilisateurRepository.findByPseudo("absent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.connecter("absent", "password123"));

        verify(utilisateurRepository).findByPseudo("absent");
    }

    @Test
    void testConnecter_whenPasswordIsIncorrect_shouldThrow() {
        Utilisateur registered = prepareUserSubscribed("user_login", "user_login@test.com", "password123", 11L);
        when(utilisateurRepository.findByPseudo("user_login")).thenReturn(Optional.of(registered));

        assertThrows(IllegalArgumentException.class,
                () -> utilisateurService.connecter("user_login", "wrong_password"));
    }

    @Test
    void testConnecter_whenPasswordIsValid_shouldReturnUser() {
        Utilisateur registered = prepareUserSubscribed("user_ok", "user_ok@test.com", "password123", 12L);
        when(utilisateurRepository.findByPseudo("user_ok")).thenReturn(Optional.of(registered));

        Utilisateur connected = utilisateurService.connecter("user_ok", "password123");

        assertEquals(registered.getId(), connected.getId());
        assertEquals("user_ok", connected.getPseudo());
    }

    @Test
    void testFindById_whenUserExists_shouldReturnUser() {
        Utilisateur user = Utilisateur.builder().id(50L).pseudo("find_me").email("find@test.com").password("x").build();
        when(utilisateurRepository.findById(50L)).thenReturn(Optional.of(user));

        Utilisateur found = utilisateurService.findById(50L);

        assertEquals(50L, found.getId());
        assertEquals("find_me", found.getPseudo());
    }

    @Test
    void testFindById_whenUserDoesNotExist_shouldThrow() {
        when(utilisateurRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> utilisateurService.findById(999L));
    }
}
