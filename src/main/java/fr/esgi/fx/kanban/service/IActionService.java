package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Action;
import java.util.List;

public interface IActionService {
    Action enregistrer(String action, String description, Long tacheId, Long utilisateurId, Long colonneId);
    List<Action> findByTacheId(Long tacheId);
}