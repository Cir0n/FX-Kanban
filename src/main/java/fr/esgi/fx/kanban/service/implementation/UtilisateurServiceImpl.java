package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.IUtilisateurService;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.Base64;

public class UtilisateurServiceImpl implements IUtilisateurService {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final IUtilisateurRepository utilisateurRepository;

    public UtilisateurServiceImpl(IUtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Utilisateur inscrire(String pseudo, String email, String password) {
        if (utilisateurRepository.existsByPseudo(pseudo)) {
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé");
        }
        if (utilisateurRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit comporter au moins 8 caractères");
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .pseudo(pseudo)
                .email(email)
                .password(hashPassword(password))
                .createdAt(LocalDateTime.now())
                .build();
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur connecter(String pseudo, String password) {
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> new IllegalArgumentException("Pseudo ou mot de passe incorrect"));
        if (!verifierPassword(password, utilisateur.getPassword())) {
            throw new IllegalArgumentException("Pseudo ou mot de passe incorrect");
        }
        return utilisateur;
    }

    @Override
    public Utilisateur findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }

    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    private boolean verifierPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] actualHash = factory.generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur lors de la vérification du mot de passe", e);
        }
    }
}
