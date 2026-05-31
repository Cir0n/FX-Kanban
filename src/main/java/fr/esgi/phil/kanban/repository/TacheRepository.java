package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Tache;
import java.util.List;
import java.util.Optional;

public interface TacheRepository {
    Optional<Tache> findById(Long id);
    List<Tache> findByColonneId(Long colonneId);
    Tache save(Tache tache);
    void update(Tache tache);
    void delete(Long id);
}