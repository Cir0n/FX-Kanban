package fr.esgi.fx.kanban.repository;

import fr.esgi.phil.kanban.model.Utilisateur;
import java.util.Optional;

public interface IUtilisateurRepository {
    Optional<Utilisateur> findById(Long id);
    Optional<Utilisateur> findByPseudo(String pseudo);
    boolean existsByPseudo(String pseudo);
    boolean existsByEmail(String email);
    Utilisateur save(Utilisateur utilisateur);
}
