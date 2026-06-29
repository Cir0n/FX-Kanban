package fr.esgi.phil.kanban.repository;

public final class Requetes {

    private Requetes() {}

    // --- Utilisateur ---
    public static final String INSERT_UTILISATEUR =
            "INSERT INTO utilisateur (pseudo, email, password) VALUES (?, ?, ?)";
    public static final String FIND_UTILISATEUR_BY_ID =
            "SELECT id, pseudo, email, password, created_at FROM utilisateur WHERE id = ?";
    public static final String FIND_UTILISATEUR_BY_PSEUDO =
            "SELECT id, pseudo, email, password, created_at FROM utilisateur WHERE pseudo = ?";
    public static final String EXISTS_UTILISATEUR_BY_PSEUDO =
            "SELECT COUNT(*) FROM utilisateur WHERE pseudo = ?";
    public static final String EXISTS_UTILISATEUR_BY_EMAIL =
            "SELECT COUNT(*) FROM utilisateur WHERE email = ?";

    // --- Tableau ---
    public static final String INSERT_TABLEAU =
            "INSERT INTO tableau (name, created_by, stripe_session_id) VALUES (?, ?, ?)";
    public static final String FIND_TABLEAU_BY_ID =
            "SELECT id, name, created_at, created_by, stripe_session_id FROM tableau WHERE id = ?";
    public static final String FIND_TABLEAUX_BY_CONTRIBUTEUR = """
            SELECT t.id, t.name, t.created_at, t.created_by, t.stripe_session_id
            FROM tableau t
            JOIN utilisateur_tableau ut ON t.id = ut.tableau_id
            WHERE ut.utilisateur_id = ?""";
    public static final String DELETE_TABLEAU =
            "DELETE FROM tableau WHERE id = ?";
    public static final String INSERT_CONTRIBUTEUR =
            "INSERT INTO utilisateur_tableau (utilisateur_id, tableau_id) VALUES (?, ?)";
    public static final String FIND_CONTRIBUTEURS = """
            SELECT u.id, u.pseudo, u.email, u.password, u.created_at
            FROM utilisateur u
            JOIN utilisateur_tableau ut ON u.id = ut.utilisateur_id
            WHERE ut.tableau_id = ?""";

    // --- Colonne ---
    public static final String INSERT_COLONNE =
            "INSERT INTO colonne (name, position, tableau_id) VALUES (?, ?, ?)";
    public static final String FIND_COLONNE_BY_ID =
            "SELECT id, name, position, tableau_id FROM colonne WHERE id = ?";
    public static final String FIND_COLONNES_BY_TABLEAU =
            "SELECT id, name, position, tableau_id FROM colonne WHERE tableau_id = ? ORDER BY position";

    // --- Type de tâche ---
    public static final String FIND_ALL_TYPES =
            "SELECT id, name, couleur FROM type_tache";
    public static final String FIND_TYPE_BY_ID =
            "SELECT id, name, couleur FROM type_tache WHERE id = ?";
    public static final String INSERT_TYPE =
            "INSERT INTO type_tache (name, couleur) VALUES (?, ?)";

    // --- Tâche ---
    public static final String INSERT_TACHE =
            "INSERT INTO tache (name, description, colonne_id, type_id, created_by, utilisateur_id) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String FIND_TACHE_BY_ID =
            "SELECT id, name, description, created_at, colonne_id, type_id, created_by, utilisateur_id FROM tache WHERE id = ?";
    public static final String FIND_TACHES_BY_COLONNE =
            "SELECT id, name, description, created_at, colonne_id, type_id, created_by, utilisateur_id FROM tache WHERE colonne_id = ?";
    public static final String UPDATE_TACHE =
            "UPDATE tache SET name = ?, description = ?, colonne_id = ?, type_id = ?, utilisateur_id = ? WHERE id = ?";
    public static final String DELETE_TACHE =
            "DELETE FROM tache WHERE id = ?";

    // --- Commentaire ---
    public static final String INSERT_COMMENTAIRE =
            "INSERT INTO commentaire (content, tache_id, utilisateur_id) VALUES (?, ?, ?)";
    public static final String FIND_COMMENTAIRES_BY_TACHE =
            "SELECT id, content, created_at, tache_id, utilisateur_id FROM commentaire WHERE tache_id = ? ORDER BY created_at";

    // --- Pièce jointe ---
    public static final String INSERT_PIECE_JOINTE =
            "INSERT INTO piece_jointe (nom_fichier, mime_type, contenu, tache_id) VALUES (?, ?, ?, ?)";
    public static final String FIND_PIECES_JOINTES_BY_TACHE =
            "SELECT id, nom_fichier, mime_type, contenu, created_at, tache_id FROM piece_jointe WHERE tache_id = ?";
    public static final String DELETE_PIECE_JOINTE =
            "DELETE FROM piece_jointe WHERE id = ?";

    // --- Action ---
    public static final String INSERT_ACTION =
            "INSERT INTO action (description, tache_id, utilisateur_id, colonne_source_id, colonne_cible_id) VALUES (?, ?, ?, ?, ?)";
    public static final String FIND_ACTIONS_BY_TACHE =
            "SELECT id, description, created_at, tache_id, utilisateur_id, colonne_source_id, colonne_cible_id FROM action WHERE tache_id = ? ORDER BY created_at";
}