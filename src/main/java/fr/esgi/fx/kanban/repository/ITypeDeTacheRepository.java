package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.TypeDeTache;
import java.util.List;
import java.util.Optional;

public interface ITypeDeTacheRepository {
    List<TypeDeTache> findAll();
    Optional<TypeDeTache> findById(Long id);
    TypeDeTache save(TypeDeTache typeDeTache);
}