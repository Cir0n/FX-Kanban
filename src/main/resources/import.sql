-- 1. la table pour les utilisateurs (Pseudo unique)
CREATE TABLE IF NOT EXISTS utilisateur (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             pseudo VARCHAR(50) NOT NULL UNIQUE,
                             email VARCHAR(100) NOT NULL,
                             mot_de_passe VARCHAR(255) NOT NULL
);

-- 2. les colonnes du Kanban (À faire, En cours, Terminé)
CREATE TABLE IF NOT EXISTS colonne (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(50) NOT NULL,
                         ordre INT NOT NULL
);

-- Remplissage des colonnes par défaut
-- On utilise MERGE pour insérer les données uniquement si elles n'existent pas
MERGE INTO colonne (id, nom, ordre) KEY(id) VALUES (1, 'À faire', 1);
MERGE INTO colonne (id, nom, ordre) KEY(id) VALUES (2, 'En cours', 2);
MERGE INTO colonne (id, nom, ordre) KEY(id) VALUES (3, 'Terminé', 3);

-- 3. Création des types de tâches
CREATE TABLE IF NOT EXISTS type_tache (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            libelle VARCHAR(50) NOT NULL
);

-- On remplit les types imposés
MERGE INTO type_tache (id, libelle) KEY(id) VALUES (1, 'Standard');
MERGE INTO type_tache (id, libelle) KEY(id) VALUES (2, 'Bug');
MERGE INTO type_tache (id, libelle) KEY(id) VALUES (3, 'Spike');
MERGE INTO type_tache (id, libelle) KEY(id) VALUES (4, 'Amélioration');

-- 4. La table principale pour les tâches
CREATE TABLE IF NOT EXISTS tache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_colonne INT NOT NULL,
    id_type_tache INT NOT NULL,
    id_utilisateur BIGINT, --  Peut être NULL si la tâche n'est pas assignée
    FOREIGN KEY (id_colonne) REFERENCES colonne(id),
    FOREIGN KEY (id_type_tache) REFERENCES type_tache(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id)
);
