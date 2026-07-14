package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import java.util.List;

public interface ITableauService {
    Tableau creer(String name, Long utilisateurId);
    Tableau findById(Long id);
    List<Tableau> findAllByContributeur(Long utilisateurId);
    List<Utilisateur> findContributeurs(Long tableauId);
    void inviterContributeur(Long tableauId, String nickname);
    void supprimer(Long id);
}
