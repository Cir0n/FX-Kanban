package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.persistence.Utilisateur;
import fr.esgi.fx.kanban.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository; // C'est notre faux repository

    private UtilisateurService utilisateurService; // Le service qu'on veut tester

    @BeforeEach
    void setUp() {
        // On instancie le service en lui donnant le FAUX repository
        utilisateurService = new UtilisateurService(utilisateurRepository);
    }

    @Test
    void testCreerUtilisateur_whenEmailDoesNotExist_shouldSaveUserWithCorrectData() {
        // 1. Préparation (Arrange)
        String email = "nouveau@email.com";
        String pseudo = "nouveau_user";

        // On dit à notre FAUX repository quoi faire :
        // "QUAND on t'appellera avec findByEmail, FAIS SEMBLANT de ne rien trouver (retourne null)"
        when(utilisateurRepository.findByEmail(email)).thenReturn(null);

        // 2. Action (Act)
        utilisateurService.creerUtilisateur(pseudo, email, "password123");

        // 3. Vérification (Assert)
        ArgumentCaptor<Utilisateur> utilisateurCaptor = ArgumentCaptor.forClass(Utilisateur.class);
        verify(utilisateurRepository).save(utilisateurCaptor.capture()); // On capture l'objet passé à save()

        Utilisateur utilisateurSauvegarde = utilisateurCaptor.getValue();
        assertEquals(pseudo, utilisateurSauvegarde.getPseudo());
        assertEquals(email, utilisateurSauvegarde.getEmail());
    }

    @Test
    void testCreerUtilisateur_whenEmailAlreadyExists_shouldNotSaveUser() {
        // 1. Préparation (Arrange)
        String emailExistant = "existant@email.com";
        String pseudo = "autre_user";
        Utilisateur utilisateurExistant = new Utilisateur(1L, "ancien_user", emailExistant, "pass");

        // On programme notre FAUX repository :
        // "QUAND on t'appellera avec findByEmail, FAIS SEMBLANT de trouver un utilisateur"
        when(utilisateurRepository.findByEmail(emailExistant)).thenReturn(utilisateurExistant);

        // 2. Action (Act)
        // On tente de créer un utilisateur avec le même email
        utilisateurService.creerUtilisateur(pseudo, emailExistant, "password456");

        // 3. Vérification (Assert)
        // On VÉRIFIE que la méthode save() de notre FAUX repository n'a JAMAIS été appelée.
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    }
}