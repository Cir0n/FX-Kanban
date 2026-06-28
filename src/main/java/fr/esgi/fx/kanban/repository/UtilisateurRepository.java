package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.persistence.Utilisateur;

import java.util.List;

public interface UtilisateurRepository {
    void save(Utilisateur utilisateur);
    Utilisateur findByEmail(String email);
    Utilisateur findByPseudo(String pseudo);
    List<Utilisateur> findAll();
    void update(Utilisateur utilisateur);
    void delete(Long id);
}