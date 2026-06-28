package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.persistence.Utilisateur;
import fr.esgi.fx.kanban.repository.UtilisateurRepository;

public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public Utilisateur creerUtilisateur(String pseudo, String email, String motDePasse) {
        //Règle métier : vérifier si l'email est déjà utilisé
        if (utilisateurRepository.findByEmail(email) != null) {
            return null;
        }

        Utilisateur nouvelUtilisateur = new Utilisateur(null, pseudo, email, motDePasse);
        utilisateurRepository.save(nouvelUtilisateur);
        return nouvelUtilisateur;

    }
}
