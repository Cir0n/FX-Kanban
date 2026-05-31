package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.TypeDeTache;
import java.util.List;
import java.util.Optional;

public interface TypeDeTacheRepository {
    List<TypeDeTache> findAll();
    Optional<TypeDeTache> findById(Long id);
    TypeDeTache save(TypeDeTache typeDeTache);
}