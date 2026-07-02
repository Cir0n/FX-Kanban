-- 1. Utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    pseudo     VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- 2. Type de tâche
CREATE TABLE IF NOT EXISTS type_tache (
    id      BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(50) NOT NULL,
    couleur VARCHAR(20) NOT NULL
);

MERGE INTO type_tache (id, name, couleur) KEY(id) VALUES (1, 'Standard',     '#3498db');
MERGE INTO type_tache (id, name, couleur) KEY(id) VALUES (2, 'Bug',          '#e74c3c');
MERGE INTO type_tache (id, name, couleur) KEY(id) VALUES (3, 'Spike',        '#9b59b6');
MERGE INTO type_tache (id, name, couleur) KEY(id) VALUES (4, 'Amélioration', '#2ecc71');

-- 3. Tableau
CREATE TABLE IF NOT EXISTS tableau (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by        BIGINT       NOT NULL,
    stripe_session_id VARCHAR(255) NULL,
    FOREIGN KEY (created_by) REFERENCES utilisateur(id)
);

-- 4. Utilisateur - Tableau (Contribue)
CREATE TABLE IF NOT EXISTS utilisateur_tableau (
    utilisateur_id BIGINT NOT NULL,
    tableau_id     BIGINT NOT NULL,
    PRIMARY KEY (utilisateur_id, tableau_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id),
    FOREIGN KEY (tableau_id)     REFERENCES tableau(id)
);

-- 5. Colonne
CREATE TABLE IF NOT EXISTS colonne (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    position   INT         NOT NULL,
    tableau_id BIGINT      NOT NULL,
    FOREIGN KEY (tableau_id) REFERENCES tableau(id)
);

-- 6. Tâche
CREATE TABLE IF NOT EXISTS tache (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    colonne_id     BIGINT       NOT NULL,
    type_id        BIGINT       NOT NULL,
    created_by     BIGINT       NOT NULL,
    utilisateur_id BIGINT       NULL,
    FOREIGN KEY (colonne_id)     REFERENCES colonne(id),
    FOREIGN KEY (type_id)        REFERENCES type_tache(id),
    FOREIGN KEY (created_by)     REFERENCES utilisateur(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

-- 7. Pièce jointe
CREATE TABLE IF NOT EXISTS piece_jointe (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nom_fichier VARCHAR(255) NOT NULL,
    mime_type   VARCHAR(100) NOT NULL,
    contenu     BLOB         NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    tache_id    BIGINT       NOT NULL,
    FOREIGN KEY (tache_id) REFERENCES tache(id)
);

-- 8. Commentaire
CREATE TABLE IF NOT EXISTS commentaire (
    id             BIGINT    AUTO_INCREMENT PRIMARY KEY,
    content        TEXT      NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tache_id       BIGINT    NOT NULL,
    utilisateur_id BIGINT    NOT NULL,
    FOREIGN KEY (tache_id)       REFERENCES tache(id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

-- 9. Action (historique)
CREATE TABLE IF NOT EXISTS action (
    id                BIGINT    AUTO_INCREMENT PRIMARY KEY,
    description       TEXT,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tache_id          BIGINT    NOT NULL,
    utilisateur_id    BIGINT    NOT NULL,
    colonne_source_id BIGINT    NULL,
    colonne_cible_id  BIGINT    NULL,
    FOREIGN KEY (tache_id)          REFERENCES tache(id),
    FOREIGN KEY (utilisateur_id)    REFERENCES utilisateur(id),
    FOREIGN KEY (colonne_source_id) REFERENCES colonne(id),
    FOREIGN KEY (colonne_cible_id)  REFERENCES colonne(id)
);