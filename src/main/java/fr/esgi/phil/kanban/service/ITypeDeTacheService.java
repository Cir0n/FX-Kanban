package fr.esgi.phil.kanban.service;

import fr.esgi.phil.kanban.model.TypeDeTache;
import java.util.List;
import java.util.Optional;

public interface ITypeDeTacheService {
    List<TypeDeTache> findAll();
    Optional<TypeDeTache> findById(Long id);
}