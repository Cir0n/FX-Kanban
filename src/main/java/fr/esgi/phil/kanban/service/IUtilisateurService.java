package fr.esgi.phil.kanban.service;

import fr.esgi.phil.kanban.model.Utilisateur;

public interface IUtilisateurService {
    Utilisateur inscrire(String nickname, String email, String password);
    Utilisateur connecter(String nickname, String password);
    Utilisateur findById(Long id);
}
