package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Colonne;
import java.util.List;
import java.util.Optional;

public interface ColonneRepository {
    Optional<Colonne> findById(Long id);
    List<Colonne> findByTableauId(Long tableauId);
    Colonne save(Colonne colonne);
}