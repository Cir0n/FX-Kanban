package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.TypeDeTache;
import java.util.List;
import java.util.Optional;

public interface ITypeDeTacheService {
    List<TypeDeTache> findAll();
    Optional<TypeDeTache> findById(Long id);
}