package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Action;
import java.util.List;

public interface ActionRepository {
    List<Action> findByTacheId(Long tacheId);
    Action save(Action action);
}