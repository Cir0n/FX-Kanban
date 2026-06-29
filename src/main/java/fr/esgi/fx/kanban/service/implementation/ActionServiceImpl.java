package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.service.IActionService;

import java.util.List;

public class ActionServiceImpl implements IActionService {

    private final IActionRepository actionRepository;

    public ActionServiceImpl(IActionRepository actionRepository) {
        this.actionRepository = actionRepository;
    }

    @Override
    public Action enregistrer(String description, Long tacheId, Long utilisateurId, Long colonneSourceId, Long colonneCibleId) {
        Action action = Action.builder()
                .description(description)
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .colonneSourceId(colonneSourceId)
                .colonneCibleId(colonneCibleId)
                .build();
        return actionRepository.save(action);
    }

    @Override
    public List<Action> findByTacheId(Long tacheId) {
        return actionRepository.findByTacheId(tacheId);
    }
}
