package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Tache;
import java.util.List;
import java.util.Optional;

public interface ITacheRepository {
    Optional<Tache> findById(Long id);
    List<Tache> findAll();
    List<Tache> findByColonneId(Long colonneId);
    Tache save(Tache tache);
    void update(Tache tache);
    void delete(Long id);
}