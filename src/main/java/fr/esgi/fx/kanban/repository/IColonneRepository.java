package fr.esgi.fx.kanban.repository;

import fr.esgi.phil.kanban.model.Colonne;
import java.util.List;
import java.util.Optional;

public interface IColonneRepository {
    Optional<Colonne> findById(Long id);
    List<Colonne> findByTableauId(Long tableauId);
    Colonne save(Colonne colonne);
}