package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.Tache;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.IEmailService;
import fr.esgi.fx.kanban.service.ITacheService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TacheServiceImpl implements ITacheService {

    private static final Logger LOGGER = LogManager.getLogger(TacheServiceImpl.class);

    private final ITacheRepository tacheRepository;
    private final IActionRepository actionRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IEmailService emailService;

    public TacheServiceImpl(ITacheRepository tacheRepository, IActionRepository actionRepository,
                             IUtilisateurRepository utilisateurRepository, IEmailService emailService) {
        this.tacheRepository = tacheRepository;
        this.actionRepository = actionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
    }

    @Override
    public Tache creer(String name, String description, Long colonneId, Long typeId, Long assigneeId, Long utilisateurId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide");
        }
        Tache tache = Tache.builder()
                .name(name)
                .description(description)
                .colonneId(colonneId)
                .typeId(typeId)
                .utilisateurId(assigneeId)
                .createdBy(utilisateurId)
                .build();
        Tache saved = tacheRepository.save(tache);

        Action action = Action.builder()
                .description("Création de la tâche")
                .tacheId(saved.getId())
                .utilisateurId(utilisateurId)
                .build();
        actionRepository.save(action);

        LOGGER.info("Tâche '{}' créée (id={}) dans la colonne id={} par l'utilisateur id={}",
                name, saved.getId(), colonneId, utilisateurId);

        if (assigneeId != null) {
            notifierAssignation(assigneeId, name);
        }
        return saved;
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

        LOGGER.info("Tâche id={} déplacée de la colonne id={} vers la colonne id={} par l'utilisateur id={}",
                tacheId, ancienneColonneId, nouvelleColonneId, utilisateurId);
    }

    @Override
    public void modifier(Long id, String name, String description, Long typeId, Long assigneId, Long utilisateurId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la tâche ne peut pas être vide");
        }
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tâche introuvable"));

        String ancienName = tache.getName();
        String ancienneDescription = tache.getDescription();
        Long ancienTypeId = tache.getTypeId();
        Long ancienAssigneId = tache.getUtilisateurId();

        tache.setName(name);
        tache.setDescription(description);
        tache.setTypeId(typeId);
        tache.setUtilisateurId(assigneId);
        tacheRepository.update(tache);

        Action action = Action.builder()
                .description(construireDescriptionModification(
                        ancienName, name, ancienneDescription, description, ancienTypeId, typeId,
                        ancienAssigneId, assigneId))
                .tacheId(id)
                .utilisateurId(utilisateurId)
                .build();
        actionRepository.save(action);

        LOGGER.info("Tâche id={} modifiée par l'utilisateur id={} : {}", id, utilisateurId, action.getDescription());

        if (!Objects.equals(ancienAssigneId, assigneId) && assigneId != null) {
            notifierAssignation(assigneId, name);
        }
    }

    private String construireDescriptionModification(String ancienName, String name,
                                                       String ancienneDescription, String description,
                                                       Long ancienTypeId, Long typeId,
                                                       Long ancienAssigneId, Long assigneId) {
        List<String> changements = new ArrayList<>();
        if (!Objects.equals(ancienAssigneId, assigneId)) {
            changements.add(assigneId == null ? "Désassignation" : "Assignation");
        }
        if (!Objects.equals(ancienName, name)) {
            changements.add("Renommage");
        }
        if (!Objects.equals(ancienneDescription, description)) {
            changements.add("Description modifiée");
        }
        if (!Objects.equals(ancienTypeId, typeId)) {
            changements.add("Type modifié");
        }
        return changements.isEmpty() ? "Modification de la tâche" : String.join(", ", changements);
    }

    private void notifierAssignation(Long assigneId, String nomTache) {
        Utilisateur assigne = utilisateurRepository.findById(assigneId).orElse(null);
        if (assigne != null) {
            emailService.envoyerNotificationAssignation(assigne.getEmail(), nomTache);
        } else {
            LOGGER.warn("Utilisateur assigné id={} introuvable, notification d'assignation non envoyée", assigneId);
        }
    }

    @Override
    public void supprimer(Long id) {
        tacheRepository.delete(id);
        LOGGER.info("Tâche id={} supprimée", id);
    }
}
