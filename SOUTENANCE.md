# SOUTENANCE — Cheat Sheet FX-Kanban

> Objectif : pouvoir répondre à une question du jury sans réciter. Chaque section
> explique un choix technique, cite le fichier/la classe concernée, et dit pourquoi
> ce choix a été fait. On suppose les bases connues (HTTP, SQL, sessions, MVC...) ;
> on détaille surtout ce qui est spécifique à ce projet.

---

## 1. Vue d'ensemble de l'architecture

### Le principe : pas de framework, couches écrites à la main

Pas de Spring. Chaque couche (routing, IoC, accès données) est réimplémentée
explicitement pour comprendre ce qu'un framework comme Spring MVC automatise
d'habitude.

```
                         Navigateur (HTML + kanban.js)
                                    │
                                    │ HTTP (formulaires ou fetch())
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  SERVLET (fr.esgi.fx.kanban.servlet)                       │  = Controller
│  BoardServlet, TaskEditServlet, LoginServlet, ...            │
│  - lit requête (params, session)                             │
│  - appelle un service                                        │
│  - rend une vue Thymeleaf ou redirige (302)                   │
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  SERVICE (fr.esgi.fx.kanban.service[.implementation])       │  = logique métier
│  ITacheService / TacheServiceImpl, ...                       │
│  - validation métier, orchestration de plusieurs repositories│
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  REPOSITORY (fr.esgi.fx.kanban.repository[.implementation]) │  = accès données
│  ITacheRepository / TacheRepositoryImpl, ConnectionManager   │
│  - JDBC brut, PreparedStatement, SQL centralisé (Requetes.java)│
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                          Base H2 (fichier kanban_db.mv.db)
```

Chaque couche ne parle qu'à celle du dessous (une Servlet n'écrit jamais de SQL
directement). Au démarrage, 3 `@WebListener` initialisent l'infra avant la première
requête : Thymeleaf, la base H2, Stripe (détails sections 2/3/5).

### Organisation des packages

| Package | Rôle | Exemples |
|---|---|---|
| `configuration/` | Code exécuté une seule fois au démarrage (listeners) | `ThymeleafConfiguration`, `DatabaseConfiguration`, `StripeConfiguration` |
| `model/` | Entités métier, POJO Lombok, ~1:1 avec les tables | `Utilisateur`, `Tableau`, `Colonne`, `Tache`, `Commentaire`, `PieceJointe`, `Action`, `TypeDeTache` |
| `repository/` + `.implementation/` | Interfaces + JDBC brut | `ITacheRepository` / `TacheRepositoryImpl`, `ConnectionManager`, `Requetes.java` |
| `service/` + `.implementation/` | Logique métier | `ITacheService` / `TacheServiceImpl`, `ServiceFactory` |
| `servlet/` | Un servlet par route/fonctionnalité | `BoardServlet`, `TaskEditServlet`, `LoginServlet` |
| `viewmodel/` | DTOs d'affichage, distincts des entités `model/` | `TacheVue`, `ColonneVue`, `CommentaireVue`, `ActionVue`, `PieceJointeVue`, `VueSupport` |

Pourquoi séparer `model/` et `viewmodel/` ? Une `Tache` n'a en base qu'un `typeId`
(Long) ; la vue a besoin d'un libellé ("Bug") et d'une couleur déjà résolus.
`BoardServlet.mapTache()` fait cette conversion — Thymeleaf ne manipule jamais les
entités brutes, seulement des `*Vue`._

`ServiceFactory` construit tous les repositories et services en singletons (pattern
*lazy holder*), et chaque servlet va s'y servir dans son `init()`. C'est
l'équivalent artisanal de l'`ApplicationContext` Spring — un service locator manuel
en l'absence de conteneur IoC.

### Stack technique et pourquoi

| Techno | Rôle | Pourquoi ce choix |
|---|---|---|
| Jakarta Servlet API 6.1 | HTTP | Contrainte pédagogique : comprendre le cycle de requête sans framework |
| Thymeleaf 3.1.5 | Rendu HTML côté serveur | Pas de front séparé à builder pour ce périmètre |
| H2 2.3 (mode fichier) | Base de données | Zéro install, démarrage instantané (section 5) |
| Lombok | Génère getters/setters/builder | Moins de boilerplate sur les modèles |
| Log4j2 | Logs (console + fichier `logs/kanban.log`) | Standard, rotation gérée |
| Stripe SDK (`stripe-java`) | Paiement mode test | Rend payante la création d'un tableau, pour la démo |
| Jakarta Mail (Eclipse Angus) | SMTP | Notifications d'assignation de tâche |
| Gson | JSON | Utilisé seulement par `StripeServlet` (échange avec le JS) |
| dotenv-java | Charge `.env` en local | Secrets hors du code (sections 3/4) |
| JUnit 5 + Mockito | Tests | Unitaires (repo mocké) + intégration (H2 réel) |
| Maven (packaging `war`) | Build | Déployable tel quel sur Tomcat |

---

## 2. Servlets et cycle de vie d'une requête HTTP

### Mapping URL → Servlet

Pas de routing centralisé : `web.xml` (`src/main/webapp/WEB-INF/web.xml`) existe
mais est **vide**. Chaque classe porte `@WebServlet` avec les URLs qu'elle gère ;
Tomcat scanne le classpath au démarrage et construit sa table de routes.

| Route | Méthode | Servlet |
|---|---|---|
| `/login` | GET / POST | `LoginServlet` |
| `/register` | GET / POST | `RegisterServlet` |
| `/dashboard` | GET | `DashboardServlet` |
| `/board?id=` | GET | `BoardServlet` |
| `/board/new` | GET / POST | `BoardNewServlet` |
| `/board/edit`, `/board/delete` | POST | `BoardEditServlet`, `BoardDeleteServlet` |
| `/task/new`, `/task/edit`, `/task/delete`, `/task/move` | POST | `TaskNewServlet`, `TaskEditServlet`, `TaskDeleteServlet`, `TaskMoveServlet` |
| `/task/attachment`, `/task/attachment/upload`, `/task/attachment/delete` | GET / POST | `TaskAttachmentDownloadServlet`, `TaskAttachmentUploadServlet`, `TaskAttachmentDeleteServlet` |
| `/stripe/checkout` | GET / POST | `StripeServlet` |
| `/logout` | GET | `LogoutServlet` |

> `TableauServlet` (`/tableau/{tableauId}`) et `TaskServlet` (`/task`) ont été
> supprimés : scaffolds jamais terminés, ils rendaient un template `"hello"`
> supprimé du projet (donc cassés), et n'étaient liés nulle part dans l'UI.
> `TableauServlet` était le seul endroit du projet à utiliser les *URI templates*
> du Servlet 6.0 (`{tableauId}`) — depuis sa suppression, tout passe par des query
> params (`?id=...`).

### Cycle de vie

```
Démarrage Tomcat
      │
      ▼
  init()            ← une seule fois, avant la première requête
      │
      ▼
[requêtes entrantes]
      │
      ▼
  service(req, res) ← à chaque requête (méthode héritée d'HttpServlet)
   ├── doGet()
   ├── doPost()
   └── ...
      │
      ▼
  destroy()          ← une seule fois, à l'arrêt
```

Une seule instance de chaque Servlet est réutilisée pour toutes les requêtes (pas
d'instanciation par requête) — `init()` sert donc à récupérer une fois pour toutes
les dépendances (`TemplateEngine`, services via `ServiceFactory`).

### GET / POST et gestion de session

- **GET** : affichage, sans effet de bord.
- **POST** : toute mutation (créer/modifier/supprimer/upload).
- Exception AJAX : `/task/move` est appelé en `fetch()` POST depuis `kanban.js`
  pour le drag & drop, sans recharger la page (mise à jour optimiste côté client,
  rollback si le serveur renvoie une erreur).

`HttpSession` classique, pas de JWT. À la connexion (`LoginServlet.doPost`) :

```java
HttpSession session = request.getSession(true);
session.setAttribute("user", utilisateur.getPseudo());
session.setAttribute("userId", utilisateur.getId());
```

Chaque servlet protégée répète le même garde-fou :

```java
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("userId") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
}
```

`LogoutServlet` fait `session.invalidate()`. **Limite connue** : ce contrôle
vérifie juste "connecté ou non", jamais "droit sur CE tableau/CETTE tâche" — pas de
vérification d'appartenance (section 7/8).

### Exemple ligne par ligne : `TaskEditServlet`

```java
@WebServlet(name = "taskEditServlet", value = {"/task/edit"})   // (1)
public class TaskEditServlet extends HttpServlet {

    private ITacheService tacheService;

    @Override
    public void init() {
        tacheService = ServiceFactory.tacheService();            // (2)
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");  // (3)
            return;
        }

        Long userId = (Long) session.getAttribute("userId");
        long boardId = parseLong(request.getParameter("boardId"), 1L);   // (4)
        long taskId = parseLong(request.getParameter("taskId"), 0L);
        String name = request.getParameter("name");
        // ...

        if (taskId == 0L || name == null || name.trim().isEmpty()) {
            redirectWithError(request, response, boardId, "Le nom de la tâche est requis.");  // (5)
            return;
        }

        try {
            tacheService.modifier(taskId, name.trim(), description, typeId, assigneeId, userId); // (6)
            response.sendRedirect(request.getContextPath() + "/board?id=" + boardId + "&updated=1"); // (7)
        } catch (IllegalArgumentException e) {
            redirectWithError(request, response, boardId, e.getMessage());  // (8)
        }
    }
}
```

1. Route `/task/edit`, seulement en POST (pas de `doGet` surchargé → 405 sinon).
2. Service récupéré une fois via le service locator maison.
3. Garde de session classique.
4. Params lus en `String` bruts — pas de binding automatique façon
   `@RequestParam`, tout est parsé à la main.
5. Validation de surface (champ vide) faite ici, avant d'aller en base.
6. La vraie logique (règles, écriture, historique) est déléguée au service — la
   servlet ne touche jamais un repository directement.
7. Pattern **Post/Redirect/Get** : après un POST réussi, on redirige (302) plutôt
   que de renvoyer du HTML, pour éviter une double soumission au F5.
8. Erreurs métier (`IllegalArgumentException` du service) rattrapées ici,
   transportées en query param encodé (`&error=...` via `URLEncoder`).

### Trajet complet d'une requête (éditer une tâche)

```
Navigateur : soumission du <form th:action="@{/task/edit}" method="post">
      │
      ▼
Tomcat route vers TaskEditServlet.doPost
      │
      ▼
TaskEditServlet : vérifie la session, lit les paramètres
      │
      ▼
ITacheService.modifier(...)  →  ITacheRepository.update(...) (UPDATE SQL)
                              →  IActionRepository.save(...) (historique)
      │
      ▼
TaskEditServlet : sendRedirect("/board?id=X&updated=1")   [HTTP 302]
      │
      ▼
Navigateur : refait un GET /board?id=X&updated=1
      │
      ▼
BoardServlet.doGet : reconstruit tout le viewmodel (colonnes, tâches, commentaires...)
      │
      ▼
Thymeleaf : templateEngine.process("board", context, response.getWriter())
```

---

## 3. Intégration Stripe (mode sandbox/test)

### Flux complet (création d'un tableau payant)

```
BoardNewServlet.doPost("/board/new")
   │ valide le nom, mémorise en session (pendingBoardName/pendingBoardCouleur)
   ▼
IStripeService.createCheckoutSession(500, "eur", "Création du tableau « X »",
                                      successUrl, cancelUrl)
   ▼
response.sendRedirect(checkout.getUrl())     → Checkout hébergé par Stripe
   │
   ▼ (paiement avec une carte de test)
   │
GET /board/new?status=success&session_id=...
   ▼
finaliserApresPaiement() : stripeService.retrieveSession(sessionId)
   │ vérifie checkout.getPaymentStatus() == "paid"
   ▼
tableauService.creer(...) + 4 colonnes par défaut (À faire / En cours / En revue / Terminé)
   ▼
redirect /board?id=X&created=1
```

Annulation : `status=cancel` → `annulerPaiement()` oublie le tableau en attente.

**Pas de webhook Stripe signé.** La confirmation se fait au retour de redirection
(`retrieveSession`), pas via un event asynchrone. Fonctionne pour une démo, mais
fragile : si l'utilisateur ferme l'onglet juste après le paiement, Stripe a bien
encaissé mais le tableau n'est jamais créé côté appli.

### Où sont les clés, comment elles sont protégées

`StripeConfiguration` (`@WebListener`, exécuté une fois au démarrage) lit
`STRIPE_API_KEY` via `dotenv-java` :

```java
Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
String apiKey = dotenv.get("STRIPE_API_KEY");
if (apiKey == null) apiKey = System.getProperty("stripe.api.key"); // fallback déploiement
```

Le fichier `.env` (`src/main/resources/.env`) est explicitement dans `.gitignore`
(ligne 52) — jamais commité. `.env.example` est versionné à la place, avec des
valeurs bidons. La clé est stockée une fois en attribut du `ServletContext`
(`STRIPE_SERVICE_CONTEXT_KEY`), relue par `BoardNewServlet`/`StripeServlet`.

### Sandbox vs production

| | Mode actuel (test) | Production |
|---|---|---|
| Clé | `sk_test_...` | `sk_live_...` |
| Cartes | Cartes de test Stripe (`4242 4242 4242 4242`) | Vraies cartes |
| Confirmation | `retrieveSession()` au retour navigateur | Webhook signé (`Stripe-Signature` + secret), asynchrone et idempotent |
| Transport | HTTP local accepté | HTTPS obligatoire |

---

## 4. Envoi d'e-mails via SMTP Gmail

### Configuration

`EmailServiceImpl` (Jakarta Mail, impl. Eclipse Angus) :

```java
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");   // STARTTLS, pas SSL direct (465)
props.put("mail.smtp.host", host);                // smtp.gmail.com
props.put("mail.smtp.port", port);                // 587
```

Auth via un `Authenticator` qui fournit `username`/`password` lus depuis `.env`
(`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`).

### Identifiants et dégradation gracieuse

Même mécanisme que Stripe : lus une fois via `dotenv-java`, jamais en dur. Si la
config est absente, un booléen `configure` passe à `false` et
`envoyerNotificationAssignation()` devient un no-op silencieux (juste un WARN au
démarrage) :

```java
if (!configure) {
    LOGGER.warn("Configuration SMTP absente ou incomplète, "
               + "les notifications par email sont désactivées.");
}
```

Choix assumé : l'envoi d'email ne doit jamais faire échouer une opération métier —
assigner une tâche fonctionne même sans SMTP configuré.

### Flux : notification d'assignation

Dans `TacheServiceImpl.creer()`/`.modifier()`, si un `assigneeId` est fourni
(nouveau ou changé) :

```java
if (assigneId != null) {
    notifierAssignation(assigneId, name);
}
```

Construit un `MimeMessage` texte brut (sujet fixe *"Vous avez été assigné à une
tâche"*), `Transport.send(message)`. Les échecs (`MessagingException`) sont
loggés, jamais remontés à l'utilisateur.

### Limites du SMTP Gmail

- Mot de passe d'application obligatoire (Gmail refuse l'auth simple dès la 2FA
  activée, quasi systématique aujourd'hui).
- Quota ~500 mails/jour en compte gratuit — pas fait pour du volume.
- Pas de tracking (ouverture, bounce), pas de HTML.
- Risque de classement spam selon volume/contenu.
- En prod : service dédié (SendGrid, Mailgun, SES) avec SPF/DKIM configurés.

---

## 5. Base de données H2

### Mode fichier

```java
private static final String URL = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
```

Mode fichier (pas `jdbc:h2:mem:`) : `kanban_db.mv.db` créé au premier lancement,
données persistées entre redémarrages. `AUTO_SERVER=TRUE` autorise plusieurs
connexions concurrentes sur le même fichier (IDE + tests en parallèle). Pas de
console H2 web activée — exploration via un client JDBC (déjà configuré dans
`.idea/dataSources.xml`).

Le driver H2 est enregistré explicitement dans un bloc `static {}` de
`ConnectionManager` (`Class.forName("org.h2.Driver")`) : sous Tomcat, le
classloader isolé de la webapp empêche l'auto-découverte habituelle du driver par
`DriverManager` (classloader système).

### Initialisation / seed

`DatabaseConfiguration` (`@WebListener`) exécute une seule fois au démarrage :

```java
stmt.execute("RUNSCRIPT FROM 'classpath:import.sql'");
```

`import.sql` : 9 `CREATE TABLE IF NOT EXISTS` + 4 `MERGE INTO type_tache` (seed
idempotent des types Standard/Bug/Spike/Amélioration).

> Avant : le script tournait via la clause `INIT=` de l'URL JDBC, donc à **chaque**
> connexion. Sans pool de connexions (une connexion par appel repository), ça
> ralentissait fortement l'appli. Désormais joué une seule fois, au démarrage.

### Justification et limites

**Pour** : zéro install, portable, démarrage instantané, suffisant pour une démo
avec peu d'utilisateurs simultanés, SQL standard donc peu de migration si besoin.

**Limites à assumer** :
- Pas de pool de connexions (`try-with-resources` par appel repository) — correct
  fonctionnellement, pas optimal en charge.
- Pas pensée pour de la forte concurrence ni de la HA.
- Pas de Flyway/Liquibase, juste des `CREATE TABLE IF NOT EXISTS` rejouables.
- Migration prod = changer URL + driver dans `ConnectionManager` pour
  PostgreSQL/MySQL + ajouter un vrai pool (HikariCP) ; le reste (SQL standard, pas
  d'ORM propriétaire) bouge peu.

---

## 6. Thymeleaf

### Rôle et intégration

Rendu HTML côté serveur (SSR) — pas de SPA. Les interactions dynamiques ciblées
(drag & drop) passent par du `fetch()` (`kanban.js`), pas par une réécriture front
complète.

`ThymeleafConfiguration` (`@WebListener`) construit le `TemplateEngine` une seule
fois au démarrage :

```java
WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
templateResolver.setTemplateMode(TemplateMode.HTML);
templateResolver.setPrefix("/WEB-INF/templates/");
templateResolver.setSuffix(".html");
templateResolver.setCacheable(false);   // dev : templates rechargés sans redémarrer
sce.getServletContext().setAttribute("templateEngine", templateEngine);
```

Chaque servlet le récupère dans `init()` via `getServletContext().getAttribute(...)`
— pattern service locator, faute de conteneur IoC.

Toutes les servlets utilisent `WebContext` (via
`JakartaServletWebApplication.buildApplication(...)`), requis dès qu'un template
utilise `th:href="@{/...}"` (résolution avec context path) — c'est le cas de tous
les templates actuels.

### Syntaxe utilisée dans le projet

Exemples réels tirés de `board.html`/`dashboard.html` :

| Directive | Exemple réel | Effet |
|---|---|---|
| `th:text` | `<span th:text="${tache.name}">Nom de la tâche</span>` | Remplace le texte (échappé HTML) ; le texte statique sert d'aperçu design |
| `th:each` | `<div th:each="tache : ${colonne.taches}">` | Boucle sur `List<TacheVue>` |
| `th:if` | `<div th:if="${!#lists.isEmpty(tache.pieceJointes)}">` | Affichage conditionnel |
| `th:attr` | `th:attr="data-modal-target='modal-' + ${tache.id}, data-task-id=${tache.id}"` | Pose plusieurs attributs dynamiques en une fois |
| `th:href="@{...}"` | `th:href="@{/board(id=${tableau.id})}"` | URL avec query params, tenant compte du context path |
| `th:classappend` | `th:classappend="'badge-' + ${tache.typeClasse}"` | Ajoute une classe CSS calculée sans écraser les classes statiques |
| `th:field` | non utilisé | Pas de binding de formulaire automatique — les `name=` sont posés à la main et relus via `request.getParameter(...)` |
| `th:fragment`/`th:replace` | non utilisé | La navbar est dupliquée dans chaque page (`board.html`, `dashboard.html`, `login.html`...) plutôt que factorisée. Piste d'amélioration évidente (section 7). |

### Du contrôleur vers la vue

```java
context.setVariable("colonnes", colonnes(tableau.getId(), utilisateurs));
```

Le servlet passe des viewmodels, jamais les entités `model/` — la vue ne connaît
pas la structure des tables SQL.

### Échappement / XSS

`th:text` échappe par défaut (jamais `th:utext` dans le projet) — une description
de tâche contenant `<script>` s'affiche comme texte inerte.

---

## 7. Points transverses

### Accès aux données (pas d'ORM)

Pas de JPA/Hibernate (malgré `hibernate-validator` en dépendance — jamais exploité,
ni comme validateur ni comme ORM, ajouté tôt et jamais utilisé). JDBC brut,
pattern Repository (interface + impl). Tout le SQL est centralisé dans
`Requetes.java`, constantes texte groupées par entité. Chaque méthode ouvre sa
propre connexion :

```java
try (Connection conn = ConnectionManager.getConnection();
     PreparedStatement stmt = conn.prepareStatement(Requetes.INSERT_TACHE, Statement.RETURN_GENERATED_KEYS)) {
    ...
}
```

### Sécurité

- **Injection SQL** : impossible en l'état, 100% `PreparedStatement`, aucune
  concaténation de chaîne SQL dans le repo.
- **XSS** : Thymeleaf échappe par défaut (section 6).
- **Mots de passe** : hashés PBKDF2WithHmacSHA256 (65 536 itérations, sel
  aléatoire 16 octets, `UtilisateurServiceImpl.hashPassword/verifierPassword`) —
  pas bcrypt/argon2, mais un algo standard du JDK, suffisant à ce niveau.
- **Upload de fichiers** : nom nettoyé via `Paths.get(...).getFileName()` avant
  stockage (`TaskAttachmentUploadServlet`) pour éviter un `../../evil.sh`.
- **CSRF** : aucune protection (pas de token sur les formulaires POST) — limite
  connue à assumer si le jury la soulève.
- **Autorisation** : vérifie juste "connecté", jamais "appartenance au tableau/à
  la tâche". Pas exploité dans la démo, mais absent. Cohérent sur tout le projet
  (même logique partout, pas un oubli isolé).

### Authentification

Un seul flux : `RegisterServlet`/`LoginServlet`, entité `Utilisateur`, mot de
passe haché, stocké en base H2. Un premier prototype (`SigninServlet`/`User`/
`UserRepository`, mot de passe en clair, stockage en mémoire) a été identifié puis
supprimé — plus aucun lien dans l'UI.

### Organisation Maven

`packaging=war`, Java 25. Dépendances clés :

| Dépendance | Scope | Rôle |
|---|---|---|
| `jakarta.servlet-api` | `provided` | Fournie par Tomcat, pas dans le WAR |
| `thymeleaf` | compile | Templates |
| `h2` | `runtime` | Base de données |
| `stripe-java` | compile | Paiement |
| `dotenv-java` | compile | `.env` |
| `jakarta.mail` (Eclipse Angus) | compile | SMTP |
| `gson` | compile | JSON pour `/stripe/checkout` |
| `log4j-api`/`log4j-core` | compile | Logs |
| `lombok` | `provided` | Boilerplate à la compilation |
| `junit-jupiter`, `mockito-junit-jupiter` | `test` | Tests |

Plugins notables : `maven-war-plugin`, `maven-surefire-plugin` avec
`-Dnet.bytebuddy.experimental=true` (nécessaire pour Mockito sous Java 25, que
byte-buddy ne supporte pas encore officiellement).

### Déploiement

WAR déployé sur Tomcat (config IntelliJ locale, Tomcat 11 — voir
`.idea/workspace.xml`). `web.xml` vide → ajouter une route = juste une nouvelle
classe `@WebServlet`, aucune config à toucher. `mvn clean package` génère le
`.war`.

### Pourquoi Servlets + Thymeleaf plutôt que Spring Boot

Choix pédagogique assumé : reconstruire à la main ce que Spring automatise.

| Spring | Équivalent fait main |
|---|---|
| `@Controller`/`@GetMapping` | `HttpServlet` + `@WebServlet` |
| `ApplicationContext` (IoC) | `ServiceFactory` (singletons lazy holder) |
| Bean lifecycle / config au démarrage | `ServletContextListener` (`@WebListener`) |
| Intégration auto du moteur de vue | Câblage manuel de Thymeleaf (`WebContext`) |

**Limites connues** :
- Code répétitif d'une Servlet à l'autre (`parseLong`, check de session,
  `redirectWithError`) — pas de filtre/intercepteur commun.
- Pas de validation déclarative (`hibernate-validator` présent mais inexploité) —
  tout est écrit à la main.
- Pas de gestion d'exception centralisée — chaque servlet fait son propre
  `try/catch`.
- Pas de transaction : `TacheServiceImpl.creer()` fait un `INSERT tache` puis un
  `INSERT action` séparément — si le 2e échoue, le 1er reste commité. Risque
  mineur, acceptable ici.

**Piste d'amélioration à citer à l'oral** : remplacer le check de session dupliqué
par un `Filter` Jakarta unique sur les routes protégées.

---

## 8. Questions probables du jury + réponses courtes

**Q1 — "H2 ne tiendrait pas la charge en prod, pourquoi l'avoir choisi ?"**
> Zéro install, portable, démarrage instantané — adapté à un projet étudiant avec
> peu d'utilisateurs simultanés. Migration PostgreSQL = changer URL + driver dans
> `ConnectionManager` ; pas d'ORM propriétaire, donc peu d'impact ailleurs.

**Q2 — "Où sont vos clés Stripe/Gmail ?"**
> Dans `.env`, exclu de `.gitignore`, jamais commité. `.env.example` documente les
> clés attendues avec des valeurs bidons. Fallback en variable système au
> déploiement.

**Q3 — "Pourquoi pas Spring Boot ?"**
> Contrainte pédagogique : comprendre le cycle de requête et l'assemblage des
> couches sans que le framework le fasse à notre place. `ServiceFactory` = IoC
> maison, `ServletContextListener` = config au démarrage.

**Q4 — "Le paiement Stripe, c'est fiable ?"**
> Checkout hébergé par Stripe, mode test — aucune donnée carte ne transite par
> notre serveur. Limite assumée : pas de webhook signé, confirmation seulement au
> retour de redirection. Si l'utilisateur ferme l'onglet après paiement, Stripe a
> encaissé mais le tableau n'est jamais créé côté appli.

**Q5 — "Comment testez-vous l'application ?"**
> JUnit 5 + Mockito. Tests unitaires services (repo mocké, ex. `TacheServiceTest`),
> tests d'intégration repositories contre le vrai H2 (ex. `TacheRepositoryTest`).
> Tous verts via `mvn test`. Pas de tests end-to-end HTTP.

**Q6 — "Comment gérez-vous les erreurs ?"**
> Pas de gestionnaire centralisé. Chaque servlet attrape `IllegalArgumentException`
> et redirige avec un message encodé en query param, affiché ensuite côté template
> (`th:if="${error}"`). Erreurs techniques loggées en Log4j2, remontent en
> `RuntimeException` → page d'erreur Tomcat par défaut.

**Q7 — "Sécurité : injection SQL, XSS ?"**
> SQL 100% préparé, pas d'injection possible. Thymeleaf échappe par défaut, pas de
> XSS. Mots de passe hashés PBKDF2 + sel. Limites assumées : pas de CSRF, pas de
> vérification fine des droits d'accès.

**Q8 — "Pourquoi Thymeleaf plutôt qu'une API + front séparé ?"**
> Rendu serveur simple, pas de build front à gérer pour ce périmètre. Les rares
> interactions immédiates (drag & drop) passent par un `fetch()` ciblé.

**Q9 — "Il n'y a pas de code mort dans le projet ?"**
> Il y en a eu : un premier prototype d'inscription (mot de passe en clair, jamais
> persisté) coexistait avec le vrai système (mot de passe haché, persisté), et
> deux servlets rendaient un template supprimé. Non liés dans l'UI, repérés puis
> supprimés. Aujourd'hui : un seul chemin, `/register` + `/login`.
