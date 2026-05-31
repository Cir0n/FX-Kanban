package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Utilisateur;
import java.util.Optional;

public interface    UtilisateurRepository {
    Optional<Utilisateur> findById(Long id);
    Optional<Utilisateur> findByNickname(String nickname);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    Utilisateur save(Utilisateur utilisateur);
}