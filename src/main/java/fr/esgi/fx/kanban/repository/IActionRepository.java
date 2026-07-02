package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Action;
import java.util.List;

public interface IActionRepository {
    List<Action> findByTacheId(Long tacheId);
    Action save(Action action);
}