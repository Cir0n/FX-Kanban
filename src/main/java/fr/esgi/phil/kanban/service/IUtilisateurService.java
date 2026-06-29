package fr.esgi.phil.kanban.service;

import fr.esgi.phil.kanban.model.Utilisateur;

public interface IUtilisateurService {
    Utilisateur inscrire(String pseudo, String email, String password);
    Utilisateur connecter(String pseudo, String password);
    Utilisateur findById(Long id);
}
