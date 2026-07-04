package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.repository.*;
import fr.esgi.fx.kanban.repository.implementation.*;
import fr.esgi.fx.kanban.service.implementation.*;

/**
 * Point d'assemblage unique de la couche service (les repositories JDBC y sont câblés).
 *
 * <p>Le projet n'utilise pas de conteneur d'injection de dépendances : cette fabrique
 * fournit des singletons paresseux, thread-safe via l'idiome « holder ». Les servlets
 * récupèrent leurs services ici plutôt que de manipuler les repositories directement.
 * Les repositories s'appuient sur {@link ConnectionManager} (connexion par requête),
 * ils sont donc sans état et partageables.
 */
public final class ServiceFactory {

    private ServiceFactory() {}

    // --- Repositories (sans état, partagés) ---
    private static final IUtilisateurRepository UTILISATEUR_REPO = new UtilisateurRepositoryImpl();
    private static final ITableauRepository TABLEAU_REPO = new TableauRepositoryImpl();
    private static final IColonneRepository COLONNE_REPO = new ColonneRepositoryImpl();
    private static final ITacheRepository TACHE_REPO = new TacheRepositoryImpl();
    private static final ICommentaireRepository COMMENTAIRE_REPO = new CommentaireRepositoryImpl();
    private static final ITypeDeTacheRepository TYPE_REPO = new TypeDeTacheRepositoryImpl();
    private static final IActionRepository ACTION_REPO = new ActionRepositoryImpl();

    // --- Services (singletons paresseux) ---
    private static final class Holder {
        static final IUtilisateurService UTILISATEUR = new UtilisateurServiceImpl(UTILISATEUR_REPO);
        static final ITableauService TABLEAU = new TableauServiceImpl(TABLEAU_REPO, UTILISATEUR_REPO);
        static final IColonneService COLONNE = new ColonneServiceImpl(COLONNE_REPO);
        static final ITacheService TACHE = new TacheServiceImpl(TACHE_REPO, ACTION_REPO);
        static final ICommentaireService COMMENTAIRE = new CommentaireServiceImpl(COMMENTAIRE_REPO);
        static final ITypeDeTacheService TYPE = new TypeDeTacheServiceImpl(TYPE_REPO);
        static final IActionService ACTION = new ActionServiceImpl(ACTION_REPO);
    }

    public static IUtilisateurService utilisateurService() {
        return Holder.UTILISATEUR;
    }

    public static ITableauService tableauService() {
        return Holder.TABLEAU;
    }

    public static IColonneService colonneService() {
        return Holder.COLONNE;
    }

    public static ITacheService tacheService() {
        return Holder.TACHE;
    }

    public static ICommentaireService commentaireService() {
        return Holder.COMMENTAIRE;
    }

    public static ITypeDeTacheService typeDeTacheService() {
        return Holder.TYPE;
    }

    public static IActionService actionService() {
        return Holder.ACTION;
    }
}
