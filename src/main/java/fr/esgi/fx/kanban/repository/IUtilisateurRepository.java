package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Utilisateur;
import java.util.List;
import java.util.Optional;

public interface IUtilisateurRepository {
    Utilisateur save(Utilisateur utilisateur);
    Optional<Utilisateur> findById(Long id);
    Optional<Utilisateur> findByPseudo(String pseudo);
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAll();
    void update(Utilisateur utilisateur);
    void delete(Long id);
    boolean existsByPseudo(String pseudo);
    boolean existsByEmail(String email);
}
