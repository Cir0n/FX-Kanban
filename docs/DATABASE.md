# Documentation de la Couche de Persistance

Ce document décrit la mise en place de la couche de persistance pour l'application Kanban. Cette partie du projet est responsable de toutes les interactions avec la base de données, de la structure des données et de la logique d'accès à celles-ci.

## 1. Description

La couche de persistance est développée en Java sans framework ORM. Elle utilise :

- **JDBC** pour la communication avec la base de données.
- Une base de données **H2** en mode fichier.
- Un modèle de conception **Repository** pour séparer la logique d'accès aux données du reste de l'application. Chaque entité métier (`Utilisateur`, `Tache`, etc.) possède son propre Repository.
- Une gestion centralisée des connexions via une classe `ConnectionManager`.

## 2. Prérequis

Pour compiler et exécuter cette partie du projet, les outils suivants sont nécessaires :

- **Java 25** (ou une version supérieure)
- **Apache Maven** 3.8+

## 3. Base de Données (H2)

### Lancement et Compilation

Pour compiler le projet et créer l'archive `.war` déployable, utilisez l'une des commandes suivantes à la racine du projet :

- **Avec Maven installé globalement :** `mvn clean package`
- **Avec le Maven Wrapper (recommandé) :**
  - Sur Linux/macOS : `./mvnw clean package`
  - Sur Windows : `.\mvnw.cmd clean package`

### Création Automatique

La base de données H2 est configurée pour se créer et s'initialiser automatiquement.

- Le fichier de la base de données (`kanban_db.mv.db`) est créé à la racine du projet lors de la première connexion.
- La configuration de la connexion est centralisée dans la classe `ConnectionManager.java`. L'URL de connexion contient le paramètre suivant :
  `INIT=RUNSCRIPT FROM 'classpath:import.sql'`

Ce paramètre force H2 à exécuter le script `src/main/resources/import.sql` lors de la toute première connexion à la base. Ce script est responsable de la création des tables et de l'insertion des données initiales.

### Structure de la Base de Données

Le script `import.sql` crée les tables suivantes :

- **`utilisateur`**: Stocke les informations des utilisateurs (pseudo, email, mot de passe).
- **`colonne`**: Stocke les colonnes du tableau Kanban (nom et ordre d'affichage). Les colonnes par défaut (`À faire`, `En cours`, `Terminé`) sont insérées au démarrage.
- **`type_tache`**: Stocke les différents types de tâches possibles (`Standard`, `Bug`, etc.).
- **`tache`**: Table centrale qui stocke les tâches. Elle est liée aux autres tables par des clés étrangères (`id_utilisateur`, `id_colonne`, `id_type_tache`).

Le script utilise les commandes `CREATE TABLE IF NOT EXISTS` et `MERGE INTO` pour être "idempotent", c'est-à-dire qu'il peut être exécuté plusieurs fois sans causer d'erreur.

## 4. Tests

La couche de persistance est entièrement couverte par des tests d'intégration (pour les `Repository`) et des tests unitaires (pour les `Service`).

Pour lancer l'ensemble des tests, exécutez la commande suivante à la racine du projet :

```bash
mvn test
```

Les rapports de test détaillés sont générés dans le dossier `target/surefire-reports`.

## 5. Notes de Configuration Spécifiques

### Lombok

Le projet utilise Lombok pour réduire le code répétitif (getters, setters, constructeurs). La version `1.18.42` est spécifiée dans les propriétés du `pom.xml`.

Pour que Maven puisse compiler le projet correctement, le `maven-compiler-plugin` a été configuré avec un `annotationProcessorPaths` qui active Lombok pendant la phase de compilation.

### Mockito et Java 25

Les tests unitaires des services utilisent Mockito. En raison d'une incompatibilité connue entre la version actuelle de Mockito et Java 25, les tests peuvent échouer avec une `MockitoException`.

Pour résoudre ce problème, le `maven-surefire-plugin` (qui exécute les tests) est configuré dans le `pom.xml` pour lancer la JVM de test avec l'option suivante :

```bash
-Dnet.bytebuddy.experimental=true
```

Cette configuration est nécessaire pour que les tests utilisant Mockito s'exécutent correctement via la ligne de commande Maven.