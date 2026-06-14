package fr.esgi.fx.kanban.repository;

public final class Requetes {

    private Requetes() {
    }

    // --- Requetes pour les utilisateurs ---
    public static final String AJOUT_UTILISATEUR = "INSERT INTO utilisateur (pseudo, email, mot_de_passe) VALUES (?, ?, ?)";
    public static final String FIND_UTILISATEUR_BY_EMAIL = "SELECT id, pseudo, email, mot_de_passe FROM utilisateur WHERE email = ?";
    public static final String FIND_UTILISATEUR_BY_PSEUDO = "SELECT id, pseudo, email, mot_de_passe FROM utilisateur WHERE pseudo = ?";
    public static final String FIND_ALL_UTILISATEURS = "SELECT id, pseudo, email, mot_de_passe FROM utilisateur";
    public static final String UPDATE_UTILISATEUR = "UPDATE utilisateur SET pseudo = ?, email = ?, mot_de_passe = ? WHERE id = ?";
    public static final String DELETE_UTILISATEUR = "DELETE FROM utilisateur WHERE id = ?";

    // --- Requetes pour les Colonnes ---
    public static final String FIND_ALL_COLONNES = "SELECT id, nom FROM colonne";
    public static final String FIND_COLONNE_BY_ID = "SELECT id, nom FROM colonne WHERE id = ?";

    // --- Requetes pour les Types de Taches ---
    public static final String FIND_ALL_TYPES_TACHES = "SELECT id, libelle FROM type_tache";
    public static final String FIND_TYPE_TACHE_BY_ID = "SELECT id, libelle FROM type_tache WHERE id = ?";

    // --- Requetes pour les Taches ---
    public static final String AJOUT_TACHE = "INSERT INTO tache (titre, description, id_colonne, id_type_tache, id_utilisateur) VALUES (?, ?, ?, ?, ?)";
    public static final String FIND_TACHE_BY_ID = """
            SELECT t.id, t.titre, t.description, t.date_creation,
                   c.id as id_colonne, c.nom as nom_colonne,
                   tt.id as id_type_tache, tt.libelle as libelle_type_tache,
                   u.id as id_utilisateur, u.pseudo as pseudo_utilisateur, u.email as email_utilisateur
            FROM tache t
            JOIN colonne c ON t.id_colonne = c.id
            JOIN type_tache tt ON t.id_type_tache = tt.id
            LEFT JOIN utilisateur u ON t.id_utilisateur = u.id
            WHERE t.id = ?""";
    public static final String FIND_ALL_TACHES = """
            SELECT t.id, t.titre, t.description, t.date_creation,
                   c.id as id_colonne, c.nom as nom_colonne,
                   tt.id as id_type_tache, tt.libelle as libelle_type_tache,
                   u.id as id_utilisateur, u.pseudo as pseudo_utilisateur, u.email as email_utilisateur
            FROM tache t
            JOIN colonne c ON t.id_colonne = c.id
            JOIN type_tache tt ON t.id_type_tache = tt.id
            LEFT JOIN utilisateur u ON t.id_utilisateur = u.id""";
    public static final String UPDATE_TACHE = """
            UPDATE tache SET titre = ?, description = ?, id_colonne = ?, id_type_tache = ?, id_utilisateur = ?
            WHERE id = ?""";
    public static final String DELETE_TACHE = "DELETE FROM tache WHERE id = ?";
}
