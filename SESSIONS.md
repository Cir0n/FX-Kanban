# Journal des sessions de travail — FX-Kanban

Projet annuel B3 ESGI — Bachelor AL  
Encadrants : Fx COTE (back-end) · AH (front-end)  
Soutenance : **23 juillet 2026**  
Responsable back-end + front partiel : **Nicolas**

---

## Session 3 — 2026-06-29

**Branche :** `Nico`

### Contexte
Reprise des tests après le gros refactoring qui avait fusionné tous les packages en `fr.esgi.fx.kanban`. Le code ne compilait plus du tout.

### Ce qui a été fait

#### Correction des imports cassés (tout le code main)
Après le merge `[REFACTOR] Fusionne les packages en un seul fr.esgi.fx.kanban`, toutes les implémentations importaient encore des packages inexistants (`fr.esgi.phil.kanban.*`, `fr.esgi.fx2.kanban.*`). Corrigé dans :
- 9 interfaces de repository (`IColonneRepository`, `ITypeDeTacheRepository`, `IUtilisateurRepository`, `ITableauRepository`, `ITacheRepository`, `ICommentaireRepository`, `IActionRepository`, `IPieceJointeRepository`, `IActionRepository`)
- 7 interfaces de service (`IUtilisateurService`, `ITableauService`, `ITacheService`, `ITypeDeTacheService`, `IColonneService`, `ICommentaireService`, `IActionService`)
- 9 implémentations (`ColonneRepositoryImpl`, `TypeDeTacheRepositoryImpl`, `UtilisateurRepositoryImpl`, `TacheRepositoryImpl`, `TableauRepositoryImpl`, `ActionRepositoryImpl`, `CommentaireRepositoryImpl`, `PieceJointeRepositoryImpl`, `UtilisateurServiceImpl`)

#### Méthodes manquantes ajoutées
| Cible | Méthodes ajoutées |
|-------|-------------------|
| `IUtilisateurRepository` + impl | `findByEmail`, `findAll`, `update`, `delete` |
| `IColonneRepository` + impl | `findAll` |
| `ITacheRepository` + impl | `findAll` |
| `Requetes.java` | 7 nouvelles constantes SQL |

#### Tests réécrits (adaptation à la nouvelle archi)
Les anciens tests étaient écrits contre une API obsolète (champs `titre`/`nom`/`libelle`, objets imbriqués dans `Tache`, pas d'`Optional`). Décision prise : adapter les tests à la nouvelle archi, pas l'inverse.

| Fichier test | Changement |
|---|---|
| `ColonneRepositoryTest` | Réécrit — tests `save`, `findById`, `findByTableauId` avec setup user+tableau |
| `TypeTacheRepositoryTest` | Adapté à `TypeDeTacheRepositoryImpl` et modèle `TypeDeTache` |
| `UtilisateurRepositoryTest` | Utilise `Optional`, Builder Lombok |
| `TacheRepositoryTest` | Réécrit — IDs plats, `Optional`, setup complet user+tableau+colonne |
| `UtilisateurServiceTest` | Utilise `IUtilisateurRepository` + `UtilisateurServiceImpl`, 4 cas testés |
| `UtilisateurService.java` (test/) | **Supprimé** (ancienne classe service dans le dossier test) |

#### Fix BDD H2
Les fichiers `kanban_db.mv.db` et `kanban_db.trace.db` contenaient un ancien schéma (colonne `libelle` au lieu de `name`). Supprimés pour forcer la recréation.

### Résultat
**21 tests, 0 échec — BUILD SUCCESS**

### Prochaines étapes
- [ ] Implémenter les `*ServiceImpl` manquants (Tableau, Tache, Colonne…)
- [ ] Brancher les servlets Thymeleaf
- [ ] Ajouter les tests pour les services

---

## Session 2 — 2026-06-28

**Branche :** `Nico`

### Contexte
Reprise du travail après la session 1. Mise en place de ce journal de bord.

### En cours / non commité
- Dossier `src/main/java/fr/esgi/phil/kanban/service/` créé mais non tracké :
  - Interfaces : `IUtilisateurService`, `ITableauService`, `IColonneService`, `ITacheService`, `ICommentaireService`, `ITypeDeTacheService`, `IActionService`
  - Implémentation : `service/implementation/UtilisateurServiceImpl.java`
- `pom.xml` modifié (contenu exact à vérifier)
- `.idea/misc.xml` modifié (config IDE, ignorable)

### Prochaines étapes
- [ ] Commiter les interfaces de service et l'implémentation
- [ ] Implémenter les autres `*ServiceImpl`
- [ ] Créer les implémentations des repositories (`*RepositoryImpl`)
- [ ] Brancher les servlets

---

## Session 1 — 2026-06-01

**Branche :** `Nico`

### Ce qui a été fait

#### Couche modèle (`src/main/java/fr/esgi/phil/kanban/model/`)
| Classe | Description |
|--------|-------------|
| `Utilisateur` | Entité utilisateur |
| `Tableau` | Tableau Kanban |
| `Colonne` | Colonne d'un tableau |
| `TypeDeTache` | Catégorie/type de tâche |
| `Tache` | Tâche dans une colonne |
| `Commentaire` | Commentaire sur une tâche |
| `Action` | Historique des déplacements de tâche (contient `tacheId`) |

#### Couche repository (`src/main/java/fr/esgi/phil/kanban/repository/`)
| Interface | Commit |
|-----------|--------|
| `IUtilisateurRepository` | `ddaf0e0` |
| `ITableauRepository` | `d120e2a` |
| `IColonneRepository` | `ac952c7` |
| `ITacheRepository` | `3220c04` |
| `ICommentaireRepository` | `df32acb` |
| `ITypeDeTacheRepository` | `098facb` |
| `IActionRepository` | `e00592b` |

#### Fixes & refactors
- Correction d'erreurs de compilation (`e7c81c6`)
- Ajout de `tacheId` dans `Action` pour l'historique des déplacements (`b9bce77`)
- Renommage de toutes les interfaces repository avec le préfixe `I` (`775b802`)

---

## Conventions rappel

- Commits : `[FEATURE]`, `[FIX]`, `[REFACTOR]`, `[STYLE]`, `[DOCS]`, `[TEST]`
- Ne pas toucher au script SQL / schéma H2 (périmètre d'un autre membre de l'équipe)
- Périmètre Nicolas : servlets, services, repositories, front Thymeleaf
