# LEARNING.md — Base de connaissances personnelle

Référence technique construite au fil du projet FX-Kanban.  
Technologies couvertes : Git, Java / Jakarta EE, Maven, Thymeleaf, H2, SQL.

---

## Sommaire

- [Git](#git)
- [Java & Jakarta EE](#java--jakarta-ee)
- [Maven](#maven)
- [Thymeleaf](#thymeleaf)
- [H2 & SQL](#h2--sql)

---

## Git

### Concepts fondamentaux

| Terme | Définition |
|-------|-----------|
| **remote** | Dépôt distant (ex: GitHub). Le remote par défaut s'appelle `origin`. |
| **branch** | Ligne de développement indépendante. |
| **HEAD** | Pointeur vers le commit courant (là où tu es). |
| **staging area** | Zone intermédiaire entre tes fichiers modifiés et un commit. `git add` y déplace les fichiers. |
| **merge base** | L'ancêtre commun entre deux branches. Sans lui, les branches sont dites "unrelated". |
| **stash** | Pile temporaire pour mettre de côté des modifications non commitées. |

---

### Commandes essentielles

#### Lire l'état du dépôt

```bash
git status                        # État du working directory et de la staging area
git status --short                # Version condensée (M = modified, A = added, ?? = untracked)
git log --oneline                 # Historique compact (1 ligne par commit)
git log --oneline -10             # Les 10 derniers commits seulement
git log --format="%H|%ad|%s" --date=short   # Format personnalisé : hash | date | message
git branch -a                     # Toutes les branches (locales + distantes)
git diff                          # Différences non stagées
git diff --staged                 # Différences stagées (prêtes à commiter)
git diff --name-only              # Noms des fichiers modifiés seulement
git diff --name-only --diff-filter=U  # Fichiers en conflit uniquement (U = Unmerged)
```

#### Travailler avec le remote

```bash
git fetch --all                   # Télécharge les infos distantes SANS modifier le code local
git pull                          # fetch + merge (modifie le code local)
git push                          # Envoie les commits locaux vers le remote
git push -u origin ma-branche     # Push + lie la branche locale à la branche distante
```

> `fetch` est non-destructif : il met à jour ta "carte" du dépôt sans toucher à tes fichiers.  
> `pull` = `fetch` + `merge` : peut créer des conflits.

#### Commiter

```bash
git add fichier.java              # Stage un fichier précis
git add src/main/java/            # Stage tout un dossier
git commit -m "[FEATURE] ..."     # Crée un commit avec message
git diff HEAD origin/main         # Voir ce qui diverge par rapport à main
```

#### Branches

```bash
git checkout -b ma-branche        # Créer et basculer sur une nouvelle branche
git checkout ma-branche           # Basculer sur une branche existante
git merge autre-branche           # Merger une branche dans la branche courante
git branch -d ma-branche          # Supprimer une branche locale (après merge)
```

---

### Le stash

Le stash est une pile temporaire. Utile quand Git refuse une opération à cause de fichiers modifiés.

```bash
git stash                # Met de côté toutes les modifs non commitées
git stash pop            # Récupère et réapplique le dernier stash
git stash drop           # Supprime le dernier stash sans le réappliquer
git stash list           # Liste tous les stashs en attente
```

**Cas typique :** Git refuse un merge ou un checkout parce qu'un fichier est modifié.
```bash
git stash          # → working directory propre
git merge ...      # → opération possible
git stash pop      # → récupère les modifs (peut créer un conflit si le fichier a changé entre-temps)
```

---

### Merger deux branches sans ancêtre commun

Quand deux personnes initialisent chacune leur propre projet Git de leur côté, leurs historiques sont totalement indépendants (pas de `merge base`). Git refuse le merge par défaut.

```bash
git merge --allow-unrelated-histories --no-commit --no-ff origin/rania
```

| Option | Rôle |
|--------|------|
| `--allow-unrelated-histories` | Autorise le merge même sans ancêtre commun |
| `--no-commit` | Fait le merge mais **ne commite pas** → laisse le temps de résoudre les conflits |
| `--no-ff` | Force la création d'un commit de merge explicite (pas de fast-forward) |

> **Fast-forward** : si ta branche est en retard directement sur l'autre, Git peut juste "avancer le pointeur" sans créer de commit de merge. `--no-ff` interdit ça et garde une trace claire dans l'historique.

---

### Résoudre des conflits

Un conflit arrive quand deux branches ont modifié le même endroit d'un fichier. Git insère des marqueurs dans le fichier :

```
<<<<<<< HEAD
    ton code (ta branche)
=======
    leur code (branche mergée)
>>>>>>> origin/rania
```

**Processus de résolution :**

1. Identifier les fichiers en conflit :
   ```bash
   git diff --name-only --diff-filter=U
   ```
2. Ouvrir chaque fichier et choisir ce qu'on garde (supprimer les marqueurs `<<<<`, `====`, `>>>>`)
3. Marquer le conflit comme résolu :
   ```bash
   git add fichier-resolu.java
   ```
4. Quand tous les conflits sont réglés, commiter :
   ```bash
   git commit -m "[MERGE] ..."
   ```

**Raccourci : prendre une version entière sans éditer**

```bash
git checkout --ours fichier.xml    # Garde notre version (branche courante)
git checkout --theirs fichier.xml  # Garde leur version (branche mergée)
git add fichier.xml                # Marquer comme résolu
```

---

### Inspecter les différences entre branches

```bash
git log --oneline HEAD..origin/rania        # Commits présents chez Rania mais pas chez nous
git diff --name-only HEAD origin/rania      # Fichiers qui diffèrent entre les deux branches
git show origin/rania:pom.xml               # Voir un fichier spécifique sur une branche distante
```

---

### Bonnes pratiques de commit (convention du projet)

```
[FEATURE]  → Nouvelle fonctionnalité
[FIX]      → Correction de bug
[REFACTOR] → Restructuration sans changement de comportement
[BUILD]    → Changements de configuration build (pom.xml, etc.)
[DOCS]     → Documentation
[TEST]     → Ajout ou modification de tests
[STYLE]    → Formatage, nommage (pas de logique modifiée)
[MERGE]    → Commit de merge entre branches
```

---

## Java & Jakarta EE

### JDK, JRE et JVM — quelle différence ?

Ces trois termes désignent des couches imbriquées. La relation est simple :

```
┌─────────────────────────────────────┐
│               JDK                   │  ← Ce qu'on installe pour développer
│  ┌───────────────────────────────┐  │
│  │             JRE               │  │  ← Ce qui permet d'exécuter un programme Java
│  │  ┌─────────────────────────┐  │  │
│  │  │          JVM            │  │  │  ← Le moteur qui exécute le bytecode
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

#### JVM — Java Virtual Machine

La JVM est un **moteur d'exécution**. Elle lit et exécute le bytecode Java (les fichiers `.class`).

Le bytecode n'est pas du code machine (il ne parle pas directement au processeur). C'est un langage intermédiaire que la JVM traduit à la volée pour le système sur lequel elle tourne.

C'est pour ça que Java est "portable" : on compile une seule fois, et le même `.class` tourne sur Windows, Linux, Mac — chacun ayant sa propre JVM.

```
MonCode.java  →  [javac]  →  MonCode.class  →  [JVM]  →  exécution
   (source)      (compile)    (bytecode)         (run)
```

#### JRE — Java Runtime Environment

Le JRE = **JVM + les bibliothèques standard** (`java.util`, `java.io`, `java.time`, etc.).

C'est le minimum pour **exécuter** un programme Java déjà compilé. Un utilisateur final qui veut juste lancer ton application n'a besoin que du JRE.

#### JDK — Java Development Kit

Le JDK = **JRE + les outils de développement** :

| Outil | Rôle |
|-------|------|
| `javac` | Le compilateur : transforme `.java` en `.class` |
| `java` | Lance la JVM pour exécuter un programme |
| `jar` | Crée des archives `.jar` / `.war` |
| `javadoc` | Génère la documentation HTML depuis les commentaires |
| `jdb` | Debugger en ligne de commande |

En tant que développeur, on installe toujours le JDK (qui contient le JRE, qui contient la JVM).

#### Lien avec notre projet

```bash
# JAVA_HOME pointe vers le JDK installé
JAVA_HOME="/c/Users/ponsn/.jdks/openjdk-25.0.2-1"

# Maven utilise javac (dans le JDK) pour compiler
# puis java (dans la JRE/JVM) pour lancer les tests
mvn clean install
```

Dans notre `pom.xml`, `maven.compiler.source=25` et `maven.compiler.target=25` disent à `javac` de compiler en ciblant la version 25 de la JVM. Si quelqu'un essaie de compiler avec un JDK 23, Maven refusera.

---

### Architecture en couches (pattern du projet)

```
Requête HTTP
     │
     ▼
Servlet          ← Point d'entrée HTTP (= Controller sans Spring)
     │
     ▼
Service          ← Logique métier (IXxxService / XxxServiceImpl)
     │
     ▼
Repository       ← Accès aux données (IXxxRepository / XxxRepositoryImpl)
     │
     ▼
Base de données (H2)
```

### Pourquoi des interfaces (IXxxService, IXxxRepository) ?

Séparer l'interface de l'implémentation permet de :
- Changer l'implémentation sans toucher au code qui l'utilise (ex: passer de H2 à PostgreSQL)
- Mocker facilement en test (Mockito remplace l'implémentation par un faux)
- Rendre le code plus lisible : l'interface dit **quoi**, l'impl dit **comment**

```java
// Le service ne connaît que l'interface → ne dépend pas de H2
public class TacheServiceImpl implements ITacheService {
    private final ITacheRepository tacheRepository;  // interface, pas l'impl
    ...
}
```

---

## Maven

> *Section à compléter au fil du projet.*

### Structure d'un `pom.xml`

```xml
<properties>
    <maven.compiler.source>25</maven.compiler.source>   <!-- version Java source -->
    <maven.compiler.target>25</maven.compiler.target>   <!-- version Java cible -->
    <lombok.version>1.18.42</lombok.version>            <!-- variable réutilisable -->
</properties>
```

### Scopes de dépendances

| Scope | Disponible à la compilation | Disponible au runtime | Inclus dans le WAR |
|-------|------|------|------|
| *(aucun)* | ✅ | ✅ | ✅ |
| `provided` | ✅ | ❌ | ❌ (fourni par Tomcat) |
| `runtime` | ❌ | ✅ | ✅ |
| `test` | ✅ (tests uniquement) | ✅ (tests uniquement) | ❌ |

> Lombok et `jakarta.servlet-api` sont en `provided` car Tomcat les fournit au moment de l'exécution. Les mettre dans le WAR causerait des conflits.

### Plugin maven-surefire

Plugin qui exécute les tests JUnit lors du `mvn test`.

```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <!-- Nécessaire pour Mockito + Java 25 (ByteBuddy génère du bytecode à la volée) -->
        <argLine>-Dnet.bytebuddy.experimental=true</argLine>
    </configuration>
</plugin>
```

---

### `mvn clean install` et la redirection `2>&1`

#### Les phases Maven

```bash
mvn clean install
```

| Phase | Ce qu'elle fait |
|-------|----------------|
| `clean` | Supprime le dossier `target/` → repart de zéro |
| `install` | Compile → teste → package en WAR → installe dans `~/.m2/` |

On peut les appeler séparément (`mvn clean`, `mvn install`) ou ensemble. En pratique on fait toujours `clean install` pour être sûr de ne pas avoir de résidus d'une compilation précédente.

#### Les deux canaux de sortie : stdout et stderr

Tout programme Linux/bash dispose de deux "tuyaux" de sortie séparés :

```
ton programme
    ├── stdout (canal 1) → messages normaux  → affiché en blanc dans le terminal
    └── stderr (canal 2) → messages d'erreur → affiché en rouge dans le terminal
```

Par défaut, quand on capture la sortie d'une commande, seul **stdout** est récupéré. Les erreurs partent dans **stderr** et sont "perdues" (non capturées).

#### `2>&1` : fusionner les deux canaux

```bash
mvn clean install 2>&1
```

Se lit : **"redirige le canal 2 (stderr) vers le canal 1 (stdout)"**.

Résultat : les deux flux sont fusionnés, on voit tout dans le même endroit.

#### Exemple concret

Imaginons une commande qui écrit sur les deux canaux :

```bash
# Sans 2>&1 : on ne capture que stdout
$ ma_commande > sortie.txt
# → sortie.txt contient : "Compilation OK"
# → l'erreur "JAVA_HOME introuvable" est affichée à l'écran mais PAS dans le fichier

# Avec 2>&1 : on capture tout
$ ma_commande > sortie.txt 2>&1
# → sortie.txt contient : "Compilation OK" ET "JAVA_HOME introuvable"
```

**Ce qui s'est passé dans notre session :**  
Au premier `mvn clean install`, Maven a échoué avec `JAVA_HOME not defined` sur stderr. Sans `2>&1`, l'outil n'aurait pas vu l'erreur. Avec `2>&1`, le message d'erreur a été capturé et on a pu diagnostiquer le problème immédiatement.

---

## Tests unitaires (JUnit 5 + Mockito)

### Principe général : tester en isolation

Un **test unitaire** teste une seule classe, isolée de tout le reste. L'objectif : vérifier que la logique métier d'une classe est correcte, sans avoir besoin d'une base de données, d'un serveur, ou d'une connexion réseau.

Le problème : nos services dépendent de repositories. Comment tester `UtilisateurServiceImpl` sans lancer H2 ?

Réponse : on remplace les dépendances réelles par des **faux objets** (mocks) qu'on contrôle entièrement.

```
Test réel (intégration)          Test unitaire (isolation)
─────────────────────            ──────────────────────────
UtilisateurServiceImpl           UtilisateurServiceImpl
        │                                │
        ▼                                ▼
UtilisateurRepositoryImpl        [Mock] IUtilisateurRepository
        │                         (faux objet qu'on programme)
        ▼
      H2 database
```

---

### `@ExtendWith(MockitoExtension.class)` — pourquoi ?

JUnit 5 fonctionne avec un système d'**extensions**. Par défaut, JUnit crée simplement les instances de ta classe de test et exécute les méthodes annotées `@Test`. Il ne sait pas ce qu'est Mockito.

`@ExtendWith(MockitoExtension.class)` dit à JUnit : **"utilise l'extension Mockito pour cette classe de test"**. Concrètement, cette extension fait deux choses automatiquement :

1. Elle scanne la classe et crée de vrais mocks pour tous les champs annotés `@Mock`
2. Elle réinitialise ces mocks entre chaque test (pour éviter que les interactions d'un test "contaminent" le suivant)

Sans cette annotation, les `@Mock` restent `null` et le test plante immédiatement.

```java
@ExtendWith(MockitoExtension.class)   // ← active Mockito pour cette classe
class UtilisateurServiceTest {

    @Mock
    private IUtilisateurRepository utilisateurRepository;  // ← Mockito crée un faux objet ici
```

---

### Qu'est-ce qu'un mock ?

Un mock est un **faux objet** qui implémente une interface mais ne fait rien par défaut. Quand on appelle une méthode dessus :
- Si on ne l'a pas programmé → retourne `null` (ou `false`, ou `0` selon le type)
- Si on l'a programmé avec `when(...).thenReturn(...)` → retourne ce qu'on a dit

L'intérêt : on contrôle exactement ce que le "faux repository" retourne, sans base de données.

```java
// On programme le mock : "quand on appelle existsByEmail avec cet email, retourne false"
when(utilisateurRepository.existsByEmail(email)).thenReturn(false);

// Plus tard dans le test, quand le service appellera utilisateurRepository.existsByEmail(email),
// il recevra false — comme si la base était vide.
```

---

### Le pattern AAA (Arrange / Act / Assert)

Chaque test est structuré en trois blocs clairs :

| Bloc | Rôle | Question |
|------|------|----------|
| **Arrange** | Préparer les données et programmer les mocks | "Dans quel contexte je suis ?" |
| **Act** | Appeler la méthode qu'on teste | "Qu'est-ce que je fais ?" |
| **Assert** | Vérifier le résultat | "Est-ce que ça s'est passé comme prévu ?" |

**Exemple appliqué — `testInscrire_whenEmailAndPseudoFree_shouldSaveUser` :**

```java
@Test
void testInscrire_whenEmailAndPseudoFree_shouldSaveUser() {

    // ── ARRANGE ──────────────────────────────────────────────────────────
    String email = "nouveau@email.com";
    String pseudo = "nouveau_user";

    // On programme le mock : pseudo et email sont libres
    when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(false);
    when(utilisateurRepository.existsByEmail(email)).thenReturn(false);
    // On programme save() pour retourner l'objet qu'on lui passe
    when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

    // ── ACT ──────────────────────────────────────────────────────────────
    utilisateurService.inscrire(pseudo, email, "password123");

    // ── ASSERT ───────────────────────────────────────────────────────────
    // On vérifie que save() a bien été appelé une fois
    verify(utilisateurRepository).save(any(Utilisateur.class));
}
```

---

### Comment organiser la logique des tests ?

La règle d'or : **un test = un cas précis**. On ne teste pas "ça marche", on teste un scénario.

La convention de nommage utilisée dans le projet :

```
testNomDeLaMethode_contexte_résultatAttendu()
```

Exemples :
```
testInscrire_whenEmailAndPseudoFree_shouldSaveUser      → cas nominal (tout va bien)
testInscrire_whenEmailAlreadyExists_shouldThrow         → email déjà pris
testInscrire_whenPseudoAlreadyExists_shouldThrow        → pseudo déjà pris
testInscrire_whenPasswordTooShort_shouldThrow           → mot de passe trop court
```

Pour décider quels tests écrire, on pense aux **chemins possibles dans le code** :
- Le chemin heureux (nominal) : tout se passe bien → 1 test
- Chaque branche `if` qui peut échouer → 1 test par condition de garde

Dans `UtilisateurServiceImpl.inscrire`, il y a 3 conditions de garde :
```java
if (utilisateurRepository.existsByPseudo(pseudo)) → throw   // → 1 test
if (utilisateurRepository.existsByEmail(email))   → throw   // → 1 test
if (password.length() < 8)                        → throw   // → 1 test
```
Plus le cas nominal → **4 tests au total**, un par chemin possible.

---

### `any()` — matcher générique

`any(Utilisateur.class)` est un **argument matcher** Mockito. Il signifie "n'importe quel objet de type `Utilisateur`".

On l'utilise quand on ne peut pas prédire l'objet exact qui sera passé. Par exemple, dans `inscrire`, le service crée un `Utilisateur` en interne avec un mot de passe hashé aléatoire. On ne peut pas connaître le hash à l'avance, donc on ne peut pas écrire :

```java
// ❌ impossible : on ne connaît pas le hash du mot de passe
verify(utilisateurRepository).save(new Utilisateur(..., "hash_inconnu", ...));

// ✅ correct : on vérifie juste que save() a été appelé avec un Utilisateur quelconque
verify(utilisateurRepository).save(any(Utilisateur.class));
```

`any()` sans argument de classe fonctionne aussi, mais `any(Utilisateur.class)` est plus précis et lisible.

---

### `never()` — vérifier qu'une méthode N'a PAS été appelée

`verify(mock, never()).methode(...)` vérifie que la méthode n'a **jamais** été invoquée pendant le test.

C'est la vérification la plus importante dans les tests d'erreur : quand une condition de garde rejette la requête, on veut s'assurer que le code ne va pas quand même sauvegarder quelque chose.

```java
// On s'assure que save() n'a JAMAIS été appelé
verify(utilisateurRepository, never()).save(any(Utilisateur.class));
```

Sans ce `never()`, le test pourrait passer même si `save()` était appelé par erreur.

---

### Détail complet : `testInscrire_whenPseudoAlreadyExists_shouldThrow()`

```java
@Test
void testInscrire_whenPseudoAlreadyExists_shouldThrow() {

    // ── ARRANGE ──────────────────────────────────────────────────────────
    String pseudo = "pseudo_pris";

    // On programme le mock : ce pseudo EST déjà pris
    when(utilisateurRepository.existsByPseudo(pseudo)).thenReturn(true);
    // Note : on ne programme PAS existsByEmail — le code ne l'atteindra jamais
    // car la vérification du pseudo est en premier dans inscrire()

    // ── ACT + ASSERT (cas d'exception) ───────────────────────────────────
    assertThrows(IllegalArgumentException.class,
            () -> utilisateurService.inscrire(pseudo, "libre@email.com", "password789"));

    // ── ASSERT (side effect) ─────────────────────────────────────────────
    verify(utilisateurRepository, never()).save(any(Utilisateur.class));
}
```

**Ce qui se passe étape par étape :**

1. On prépare un pseudo qui "existe déjà" selon notre mock
2. `assertThrows` appelle `inscrire(...)` et attend qu'une `IllegalArgumentException` soit lancée
   - Si aucune exception n'est lancée → le test **échoue**
   - Si l'exception est lancée → le test **continue**
3. On vérifie ensuite que `save()` n'a jamais été appelé
   - Logique : si le pseudo est pris, on doit s'arrêter AVANT de sauvegarder

**Pourquoi deux assertions séparées ?**

`assertThrows` vérifie le comportement **visible** (l'exception). `verify(..., never())` vérifie le **side effect** (aucune écriture en base). Les deux ensemble garantissent que le rejet est complet : le service ni n'a laissé passer la requête, ni n'a fait d'effet de bord non voulu.

**Pourquoi `existsByEmail` n'est pas programmé dans ce test ?**

Regarde le code de `inscrire` :
```java
if (utilisateurRepository.existsByPseudo(pseudo)) {
    throw new IllegalArgumentException("Ce pseudo est déjà utilisé");  // ← sortie immédiate
}
if (utilisateurRepository.existsByEmail(email)) { ... }  // ← jamais atteint
```

Si le pseudo est pris, le code lève une exception et s'arrête. La ligne `existsByEmail` n'est jamais exécutée. Mockito vérifierait même une erreur si on programmait `existsByEmail` mais qu'il n'était jamais appelé — par défaut il retourne `false` pour un booléen, donc c'est transparent.

---

### Récap visuel

```
@ExtendWith(MockitoExtension.class)
        │
        ├── Active la création automatique des @Mock
        │
@Mock IUtilisateurRepository   ← faux objet (ne touche pas H2)
        │
        │  when(...).thenReturn(...)  ← on programme son comportement (Arrange)
        │
utilisateurService.inscrire(...)     ← on appelle la vraie méthode (Act)
        │
        ├── assertThrows(...)         ← on vérifie l'exception (Assert)
        └── verify(..., never())      ← on vérifie les side effects (Assert)
```

| Élément | Rôle |
|---------|------|
| `@Mock` | Crée un faux objet qui implémente l'interface |
| `when().thenReturn()` | Programme ce que le mock retourne |
| `any(Classe.class)` | Accepte n'importe quel argument de ce type |
| `verify(mock).methode()` | Vérifie que la méthode a été appelée 1 fois |
| `verify(mock, never()).methode()` | Vérifie que la méthode n'a PAS été appelée |
| `assertThrows(Exception.class, () -> ...)` | Vérifie qu'une exception est bien lancée |

---

### TDD — Test Driven Development

#### C'est quoi ?

Le TDD est une méthode de développement où **on écrit le test AVANT le code**. L'idée peut sembler contre-intuitive, mais elle change fondamentalement la façon de concevoir une fonctionnalité.

En TDD classique, on travaille par cycles très courts appelés **Red → Green → Refactor** :

```
    ┌─────────────────────────────────────────────────┐
    │                                                 │
    ▼                                                 │
  RED        Écrire un test qui échoue                │
    │        (le code n'existe pas encore)            │
    ▼                                                 │
 GREEN       Écrire le minimum de code                │
    │        pour faire passer le test                │
    ▼                                                 │
REFACTOR     Nettoyer/améliorer le code               │
    │        sans casser les tests                    │
    └─────────────────────────────────────────────────┘
```

#### Pourquoi RED d'abord ?

Voir le test **échouer en premier** est important pour deux raisons :
1. Ça prouve que le test est bien câblé (si un test passe sans qu'on ait écrit le code, c'est qu'il ne teste rien)
2. Ça te force à penser à l'interface de ta classe **avant** de penser à l'implémentation

#### Exemple concret sur notre projet

Supposons qu'on veuille ajouter une règle : "on ne peut pas créer un tableau avec un nom vide".

**Étape 1 — RED : écrire le test (le code n'existe pas encore)**

```java
@Test
void testCreer_whenNameIsBlank_shouldThrow() {
    // Arrange
    when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(unUtilisateur));

    // Act + Assert
    assertThrows(IllegalArgumentException.class,
            () -> tableauService.creer("", 1L));
}
```

On lance `mvn test` → le test **échoue** (RED). Normal : `TableauServiceImpl.creer` n'a pas encore cette validation.

**Étape 2 — GREEN : écrire le minimum pour passer**

```java
@Override
public Tableau creer(String name, Long utilisateurId) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Le nom du tableau ne peut pas être vide");
    }
    // ... reste du code
}
```

On relance `mvn test` → le test **passe** (GREEN).

**Étape 3 — REFACTOR : améliorer si besoin**

Le code est déjà propre ici, rien à changer. On passe au prochain test.

---

#### TDD vs "tester après"

| | TDD (test d'abord) | Test après |
|---|---|---|
| **Conception** | Forcé de penser à l'interface avant l'impl | On code ce qui nous arrange |
| **Couverture** | Naturellement élevée | Souvent basse (flemme après) |
| **Filets de sécurité** | Présents dès le début | Ajoutés après coup |
| **Confiance au refactor** | Haute | Faible |
| **Vitesse initiale** | Plus lente | Plus rapide |
| **Bugs en production** | Moins | Plus |

---

#### Les 3 lois du TDD (Uncle Bob)

1. **Tu n'écris pas de code de production sans avoir d'abord un test qui échoue**
2. **Tu n'écris pas plus de test que nécessaire pour que le test échoue** (ne pas compiler = échouer)
3. **Tu n'écris pas plus de code de production que nécessaire pour faire passer le test**

Ces règles semblent strictes, mais elles empêchent un défaut classique : écrire plus de code que nécessaire (sur-engineering).

---

#### Quand utiliser le TDD dans ce projet ?

Le TDD est particulièrement adapté pour :
- Les **règles métier** (validations, conditions d'accès)
- Les **cas d'erreur** (que se passe-t-il si l'email est déjà pris ?)
- Le code qu'on hésite à refactoriser (les tests donnent confiance)

Il est moins adapté pour :
- Le **code exploratoire** (on ne sait pas encore ce qu'on veut construire)
- Les **tests d'intégration** complexes avec beaucoup de setup (H2, tableaux, colonnes)

En pratique : utilise le TDD pour les services (logique métier, facile à mocker), et teste après pour les repositories (intégration H2, plus lourd à mettre en place).

---

## Thymeleaf

> *Section à compléter au fil du projet.*

---

## H2 & SQL

> *Section à compléter au fil du projet.*

---

*Mis à jour le 2026-06-28*
