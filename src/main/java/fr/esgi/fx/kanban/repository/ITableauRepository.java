package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import java.util.List;
import java.util.Optional;

public interface ITableauRepository {
    Optional<Tableau> findById(Long id);
    List<Tableau> findAllByContributeur(Long utilisateurId);
    Tableau save(Tableau tableau);
    void update(Tableau tableau);
    void delete(Long id);
    void addContributeur(Long tableauId, Long utilisateurId);
    List<Utilisateur> findContributeurs(Long tableauId);
}