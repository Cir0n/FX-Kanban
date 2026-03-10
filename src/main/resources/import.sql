-- 1. la table pour les utilisateurs (Pseudo unique)
CREATE TABLE utilisateur (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             pseudo VARCHAR(50) NOT NULL UNIQUE,
                             email VARCHAR(100) NOT NULL,
                             mot_de_passe VARCHAR(255) NOT NULL
);

-- 2. les colonnes du Kanban (À faire, En cours, Terminé)
CREATE TABLE colonne (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(50) NOT NULL,
                         ordre INT NOT NULL
);

-- Remplissage des colonnes par défaut
INSERT INTO colonne (nom, ordre) VALUES ('À faire', 1);
INSERT INTO colonne (nom, ordre) VALUES ('En cours', 2);
INSERT INTO colonne (nom, ordre) VALUES ('Terminé', 3);

-- 3. Création des types de tâches
CREATE TABLE type_tache (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            libelle VARCHAR(50) NOT NULL
);

-- On remplit les types imposés
INSERT INTO type_tache (libelle) VALUES ('Standard');
INSERT INTO type_tache (libelle) VALUES ('Bug');
INSERT INTO type_tache (libelle) VALUES ('Spike');
INSERT INTO type_tache (libelle) VALUES ('Amélioration');