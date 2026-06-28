package fr.esgi.phil.kanban.service;

import fr.esgi.phil.kanban.model.Tableau;
import java.util.List;

public interface ITableauService {
    Tableau creer(String name, Long utilisateurId);
    Tableau findById(Long id);
    List<Tableau> findAllByContributeur(Long utilisateurId);
    void inviterContributeur(Long tableauId, String nickname);
    void supprimer(Long id);
}
