package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.service.ITacheService;

import java.util.List;

public class TacheServiceImpl implements ITacheService {

    private final ITacheRepository tacheRepository;
    private final IActionRepository actionRepository;

    public TacheServiceImpl(ITacheRepository tacheRepository, IActionRepository actionRepository) {
        this.tacheRepository = tacheRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    public Tache creer(String name, String description, Long colonneId, Long typeId, Long utilisateurId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide");
        }
        Tache tache = Tache.builder()
                .name(name)
                .description(description)
                .colonneId(colonneId)
                .typeId(typeId)
                .createdBy(utilisateurId)
                .build();
        return tacheRepository.save(tache);
    }

    @Override
    public Tache findById(Long id) {
        return tacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));
    }

    @Override
    public void deplacer(Long tacheId, Long nouvelleColonneId, Long utilisateurId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));

        Long ancienneColonneId = tache.getColonneId();
        tache.setColonneId(nouvelleColonneId);
        tacheRepository.update(tache);

        Action action = Action.builder()
                .description("Déplacement de la tâche")
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .colonneSourceId(ancienneColonneId)
                .colonneCibleId(nouvelleColonneId)
                .build();
        actionRepository.save(action);
    }

    @Override
    public void supprimer(Long id) {
        tacheRepository.delete(id);
    }
}
