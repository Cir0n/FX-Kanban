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
    public List<Tache> findByColonneId(Long colonneId) {
        return tacheRepository.findByColonneId(colonneId);
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
    public void modifier(Long id, String name, String description, Long typeId, Long assigneId, Long utilisateurId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide");
        }
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));

        tache.setName(name);
        tache.setDescription(description);
        tache.setTypeId(typeId);
        tache.setUtilisateurId(assigneId);
        tacheRepository.update(tache);

        Action action = Action.builder()
                .description("Modification de la tâche")
                .tacheId(id)
                .utilisateurId(utilisateurId)
                .build();
        actionRepository.save(action);
    }

    @Override
    public void supprimer(Long id) {
        tacheRepository.delete(id);
    }
}
