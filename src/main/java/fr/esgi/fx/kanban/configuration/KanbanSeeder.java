package fr.esgi.fx.kanban.configuration;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.model.Tableau;
import fr.esgi.fx.kanban.model.Utilisateur;
import fr.esgi.fx.kanban.repository.IColonneRepository;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.ITacheRepository;
import fr.esgi.fx.kanban.repository.ITableauRepository;
import fr.esgi.fx.kanban.repository.IUtilisateurRepository;
import fr.esgi.fx.kanban.repository.implementation.ActionRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.ColonneRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TableauRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.TacheRepositoryImpl;
import fr.esgi.fx.kanban.repository.implementation.UtilisateurRepositoryImpl;
import fr.esgi.fx.kanban.service.ITableauService;
import fr.esgi.fx.kanban.service.ITacheService;
import fr.esgi.fx.kanban.service.IUtilisateurService;
import fr.esgi.fx.kanban.service.implementation.EmailServiceImpl;
import fr.esgi.fx.kanban.service.implementation.TableauServiceImpl;
import fr.esgi.fx.kanban.service.implementation.TacheServiceImpl;
import fr.esgi.fx.kanban.service.implementation.UtilisateurServiceImpl;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public final class KanbanSeeder {

    private static final String[] NOMS_COLONNES = {"À faire", "En cours",
            "Terminé"};
    private static final long[] TYPE_TACHE_IDS = {1L, 2L, 3L, 4L};
    private static final int NB_UTILISATEURS = 5;
    private static final int NB_TABLEAUX = 3;
    private static final int NB_TACHES_PAR_COLONNE = 3;

    private final Faker faker = new Faker();

    private final IUtilisateurRepository utilisateurRepository = new
            UtilisateurRepositoryImpl();
    private final ITableauRepository tableauRepository = new
            TableauRepositoryImpl();
    private final IColonneRepository colonneRepository = new
            ColonneRepositoryImpl();
    private final ITacheRepository tacheRepository = new TacheRepositoryImpl();
    private final IActionRepository actionRepository = new
            ActionRepositoryImpl();

    private final IUtilisateurService utilisateurService = new
            UtilisateurServiceImpl(utilisateurRepository);
    private final ITableauService tableauService = new
            TableauServiceImpl(tableauRepository, utilisateurRepository);
    private final ITacheService tacheService = new
            TacheServiceImpl(tacheRepository, actionRepository, utilisateurRepository, new EmailServiceImpl());

    public void seed() {
        if (!utilisateurRepository.findAll().isEmpty()) {
            System.out.println("[SEEDER] Des données existent déjà, seeding ignoré.");
            return;
        }

        List<Utilisateur> utilisateurs = creerUtilisateurs();

        for (int i = 0; i < NB_TABLEAUX; i++) {
            creerTableauAvecContenu(utilisateurs);
        }

        System.out.println("[SEEDER] " + NB_UTILISATEURS + " utilisateurs et "
                + NB_TABLEAUX + " tableaux créés.");
    }

    private List<Utilisateur> creerUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        for (int i = 0; i < NB_UTILISATEURS; i++) {
            String pseudo = faker.internet().username() + i;                    String email = faker.internet().emailAddress();
            Utilisateur utilisateur = utilisateurService.inscrire(pseudo, email, "motdepasse123");
            utilisateurs.add(utilisateur);
        }
        return utilisateurs;
    }

    private void creerTableauAvecContenu(List<Utilisateur> utilisateurs) {
        Utilisateur proprietaire =
                utilisateurs.get(faker.random().nextInt(utilisateurs.size()));
        Tableau tableau = tableauService.creer(faker.superhero().name() + " - Projet", proprietaire.getId());

                Utilisateur contributeur =
                        utilisateurs.get(faker.random().nextInt(utilisateurs.size()));
        if (!contributeur.getId().equals(proprietaire.getId())) {
            tableauService.inviterContributeur(tableau.getId(),
                    contributeur.getPseudo());
        }

        int position = 1;
        for (String nomColonne : NOMS_COLONNES) {
            Colonne colonne = colonneRepository.save(
                    Colonne.builder().name(nomColonne).position(position++).tableauId(tableau.getId()).build()
            );

            for (int i = 0; i < NB_TACHES_PAR_COLONNE; i++) {
                Utilisateur createur =
                        utilisateurs.get(faker.random().nextInt(utilisateurs.size()));
                Long typeId =
                        TYPE_TACHE_IDS[faker.random().nextInt(TYPE_TACHE_IDS.length)];
                tacheService.creer(faker.programmingLanguage().name(),
                        faker.lorem().sentence(), colonne.getId(), typeId, null, createur.getId());
            }
        }
    }
}
