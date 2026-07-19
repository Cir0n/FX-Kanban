package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.ITableauRepository;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.service.ITableauService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class TableauServiceImpl implements ITableauService {

    private static final Logger LOGGER = LogManager.getLogger(TableauServiceImpl.class);

    private final ITableauRepository tableauRepository;
    private final IUtilisateurRepository utilisateurRepository;

    public TableauServiceImpl(ITableauRepository tableauRepository, IUtilisateurRepository utilisateurRepository) {
        this.tableauRepository = tableauRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public Tableau creer(String name, Long utilisateurId) {
        return creer(name, utilisateurId, null);
    }

    @Override
    public Tableau creer(String name, Long utilisateurId, String stripeSessionId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom du tableau ne peut pas être vide");
        }
        Tableau tableau = Tableau.builder()
                .name(name)
                .createdBy(utilisateurId)
                .stripeSessionId(stripeSessionId)
                .build();
        Tableau saved = tableauRepository.save(tableau);
        // Le créateur devient automatiquement contributeur pour retrouver son tableau.
        tableauRepository.addContributeur(saved.getId(), utilisateurId);
        LOGGER.info("Tableau '{}' créé (id={}) par l'utilisateur id={}", name, saved.getId(), utilisateurId);
        return saved;
    }

    @Override
    public Tableau findById(Long id) {
        return tableauRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tableau introuvable"));
    }

    @Override
    public List<Tableau> findAllByContributeur(Long utilisateurId) {
        return tableauRepository.findAllByContributeur(utilisateurId);
    }

    @Override
    public List<Utilisateur> findContributeurs(Long tableauId) {
        return tableauRepository.findContributeurs(tableauId);
    }

    @Override
    public void inviterContributeur(Long tableauId, String pseudo) {
        Utilisateur utilisateur = utilisateurRepository.findByPseudo(pseudo)
                .orElseThrow(() -> {
                    LOGGER.warn("Invitation refusée : aucun utilisateur avec le pseudo '{}'", pseudo);
                    return new IllegalArgumentException("Aucun utilisateur trouvé avec le pseudo : " + pseudo);
                });

        boolean dejaMembre = tableauRepository.findContributeurs(tableauId).stream()
                .anyMatch(contributeur -> contributeur.getId().equals(utilisateur.getId()));
        if (dejaMembre) {
            LOGGER.warn("Invitation refusée : {} est déjà membre du tableau id={}", pseudo, tableauId);
            throw new IllegalArgumentException(pseudo + " est déjà membre de ce tableau");
        }

        tableauRepository.addContributeur(tableauId, utilisateur.getId());
        LOGGER.info("Utilisateur {} (id={}) invité sur le tableau id={}", pseudo, utilisateur.getId(), tableauId);
    }

    @Override
    public void supprimer(Long id) {
        tableauRepository.delete(id);
        LOGGER.info("Tableau id={} supprimé", id);
    }
}
