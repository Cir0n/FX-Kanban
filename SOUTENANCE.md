# SOUTENANCE — Cheat Sheet FX-Kanban

> Objectif de ce document : pouvoir répondre à une question du jury sans réciter.
> Chaque section explique un concept **en langage simple**, montre **où il vit dans
> le code** (fichier/classe précis), et dit pourquoi ce choix a été fait. Les mots
> techniques sont expliqués la première fois qu'ils apparaissent. À lire, pas à
> apprendre par cœur.

---

## 1. Vue d'ensemble de l'architecture

### Le principe : pas de framework, tout est écrit à la main

Un **framework** (comme Spring) est une boîte à outils toute faite qui fait
automatiquement plein de choses à notre place (router les requêtes, créer les
objets, etc.). Ici, **on n'utilise pas Spring** : chaque brique est écrite
explicitement. C'est plus long à écrire, mais ça permet de comprendre ce qui se
passe vraiment "sous le capot".

L'application est découpée en 3 couches qui se passent le relais, comme une chaîne :

```
                         Navigateur (page HTML + kanban.js)
                                    │
                                    │ Requête HTTP (le navigateur demande une page
                                    │ ou envoie un formulaire)
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  SERVLET (fr.esgi.fx.kanban.servlet)                       │  = le "réceptionniste"
│  BoardServlet, TaskEditServlet, LoginServlet, ...           │
│  - lit ce que le navigateur a envoyé (paramètres, session)  │
│  - demande au Service de faire le travail                   │
│  - renvoie une page (via Thymeleaf) ou redirige ailleurs     │
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  SERVICE (fr.esgi.fx.kanban.service[.implementation])       │  = les règles métier
│  ITacheService / TacheServiceImpl, ...                      │
│  - vérifie que les règles sont respectées (nom pas vide...)  │
│  - fait travailler plusieurs Repository ensemble si besoin    │
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌───────────────────────────────────────────────────────────┐
│  REPOSITORY (fr.esgi.fx.kanban.repository[.implementation]) │  = l'accès aux données
│  ITacheRepository / TacheRepositoryImpl, ConnectionManager   │
│  - écrit/lit dans la base de données en SQL                  │
└───────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                          Base H2 (fichier kanban_db.mv.db)
```

Chaque couche ne parle qu'à la couche juste en dessous : une Servlet n'écrit jamais
de SQL elle-même, elle demande toujours à un Service.

Avant même qu'une seule requête n'arrive, 3 morceaux de code s'exécutent une seule
fois **au démarrage de l'application** pour tout préparer : le moteur de pages
(Thymeleaf), la base de données (H2) et le paiement (Stripe). Détails sections 2/3/5.

### Organisation des dossiers (packages)

| Dossier | Rôle en une phrase | Exemples |
|---|---|---|
| `configuration/` | Code qui s'exécute **une seule fois, au démarrage** | `ThymeleafConfiguration`, `DatabaseConfiguration`, `StripeConfiguration` |
| `model/` | Les "fiches" qui représentent les données telles qu'elles sont en base | `Utilisateur`, `Tableau`, `Colonne`, `Tache`, `Commentaire`, `PieceJointe`, `Action`, `TypeDeTache` |
| `repository/` + `.implementation/` | Le code qui va lire/écrire dans la base de données | `ITacheRepository` / `TacheRepositoryImpl`, `ConnectionManager`, `Requetes.java` |
| `service/` + `.implementation/` | Les règles métier (ce qui est permis ou non) | `ITacheService` / `TacheServiceImpl`, `ServiceFactory` |
| `servlet/` | Les points d'entrée : une classe par page ou par action possible | `BoardServlet`, `TaskEditServlet`, `LoginServlet` |
| `viewmodel/` | Des "fiches" préparées **spécialement pour l'affichage** | `TacheVue`, `ColonneVue`, `CommentaireVue`, `ActionVue`, `PieceJointeVue`, `VueSupport` (petites fonctions de mise en forme) |

Pourquoi deux types de "fiches" (`model/` et `viewmodel/`) pour la même chose ? Une
tâche en base a juste un `typeId` (un simple nombre, ex. `2`). Mais à l'écran, on
veut afficher un badge "Bug" avec une couleur. `BoardServlet.mapTache()` fait cette
traduction : Thymeleaf (le moteur qui affiche les pages) ne voit jamais les fiches
brutes de la base, seulement des fiches déjà "habillées" pour l'affichage.

`ServiceFactory` construit une seule fois chaque Service et chaque Repository (un
seul exemplaire partagé par toute l'application, jamais recréé à chaque requête —
on appelle ça un **singleton**), et chaque Servlet vient s'y servir dans son
`init()`. C'est notre version artisanale de ce qu'un framework comme Spring ferait
automatiquement (on appelle ça l'**injection de dépendances** : fournir à chaque
classe les objets dont elle a besoin, sans qu'elle ait à les fabriquer elle-même).

### Technologies utilisées et pourquoi

| Techno | À quoi ça sert | Pourquoi ce choix |
|---|---|---|
| Jakarta Servlet API 6.1 | Recevoir et répondre aux requêtes HTTP | Contrainte pédagogique : comprendre comment marche une requête web sans qu'un framework le cache |
| Thymeleaf 3.1.5 | Générer les pages HTML côté serveur | Pas besoin de gérer un projet front séparé (React, Vue...) |
| H2 2.3 (fichier) | Base de données | Aucune installation, portable, démarre instantanément (détails section 5) |
| Lombok | Génère automatiquement le code répétitif (getters, setters...) | Moins de lignes à écrire à la main sur les fiches `model/` |
| Log4j2 | Écrit les logs (traces de ce qui se passe) dans la console et dans un fichier | Standard, gère la rotation des fichiers de logs |
| Stripe (SDK `stripe-java`) | Gérer un paiement en ligne factice (mode test) | Rend payante la création d'un tableau, pour la démo |
| Jakarta Mail (Eclipse Angus) | Envoyer des emails | Notifie un utilisateur qu'on lui a assigné une tâche |
| Gson | Convertir des objets Java en JSON et inversement | Utilisé uniquement pour l'échange avec le JavaScript côté paiement |
| dotenv-java | Charger le fichier `.env` en local | Pour ne jamais écrire un mot de passe/clé directement dans le code (sections 3/4) |
| JUnit 5 + Mockito | Tests automatiques | Vérifie que le code fait ce qu'il doit, sans tout retester à la main |
| Maven (paquet `.war`) | Compiler et empaqueter le projet | Le fichier produit se dépose tel quel dans Tomcat |

---

## 2. Servlets et cycle de vie d'une requête HTTP

### Comment une URL retrouve sa Servlet

**Pas de fichier central qui liste les routes.** Le fichier `web.xml`
(`src/main/webapp/WEB-INF/web.xml`) existe mais il est **vide**. À la place, chaque
classe porte directement l'annotation `@WebServlet` qui déclare l'URL qu'elle gère.
Au démarrage, Tomcat (le logiciel qui fait tourner l'application, appelé **serveur
d'application** ou **conteneur de servlets**) parcourt toutes les classes, repère
celles qui ont cette annotation, et construit sa propre table de correspondance.

Quelques routes réelles du projet :

| Route | Méthode | Servlet |
|---|---|---|
| `/login` | GET / POST | `LoginServlet` |
| `/register` | GET / POST | `RegisterServlet` |
| `/dashboard` | GET | `DashboardServlet` |
| `/board?id=` | GET | `BoardServlet` |
| `/board/new` | GET / POST | `BoardNewServlet` |
| `/board/edit` | POST | `BoardEditServlet` |
| `/board/delete` | POST | `BoardDeleteServlet` |
| `/task/new`, `/task/edit`, `/task/delete`, `/task/move` | POST | `TaskNewServlet`, `TaskEditServlet`, `TaskDeleteServlet`, `TaskMoveServlet` |
| `/task/attachment`, `/task/attachment/upload`, `/task/attachment/delete` | GET / POST | `TaskAttachmentDownloadServlet`, `TaskAttachmentUploadServlet`, `TaskAttachmentDeleteServlet` |
| `/stripe/checkout` | GET / POST | `StripeServlet` |
| `/logout` | GET | `LogoutServlet` |

> Deux vieilles routes (`TableauServlet`, `TaskServlet`) ont été supprimées du
> projet : c'étaient des débuts de servlets jamais terminés, qui essayaient
> d'afficher une page qui n'existait plus. Elles n'étaient reliées à aucun lien
> dans l'interface, donc invisibles pour un utilisateur normal — mais un jury qui
> explore le code aurait pu tomber dessus et se poser des questions. Elles ont été
> retirées proprement.

### Le cycle de vie d'une Servlet

Une Servlet n'est **pas recréée à chaque visite** : Tomcat en garde une seule
instance en mémoire, réutilisée pour tous les utilisateurs.

```
Démarrage de Tomcat
      │
      ▼
  init()            ← s'exécute UNE SEULE FOIS, avant la première requête
      │
      ▼
[le serveur tourne, les requêtes arrivent]
      │
      ▼
  service(req, res) ← s'exécute À CHAQUE requête (méthode héritée d'HttpServlet)
   ├── doGet()       si la requête est un GET (consulter une page)
   ├── doPost()      si la requête est un POST (envoyer/modifier des données)
   └── ...
      │
      ▼
  destroy()          ← s'exécute UNE SEULE FOIS, quand le serveur s'arrête
```

`init()` sert à récupérer une bonne fois pour toutes les objets dont la Servlet aura
besoin (le moteur Thymeleaf, les Services) plutôt que de les redemander à chaque
requête — un peu comme un cuisinier qui installe sa cuisine une fois le matin,
plutôt que de tout ressortir à chaque commande.

### GET ou POST : comment on choisit dans ce projet

- **GET** : on veut juste *voir* quelque chose, sans rien changer sur le serveur
  (afficher le tableau, la page de connexion...).
- **POST** : on *envoie* des données qui vont créer, modifier ou supprimer quelque
  chose (créer une tâche, se connecter, uploader un fichier...).
- **Exception** : `/task/move` (déplacer une tâche par glisser-déposer) est appelé
  en POST mais via `fetch()` en JavaScript (`kanban.js`), sans recharger toute la
  page — la carte bouge tout de suite à l'écran, et si le serveur répond une erreur,
  elle revient à sa place.

### Session et cookies : comment le serveur se souvient de vous

Le HTTP est **"sans mémoire"** : par défaut, chaque requête est traitée comme si
c'était la première fois, le serveur ne sait pas qui vous êtes d'une page à
l'autre. Pour résoudre ça, on utilise une **session** : à la connexion, le serveur
crée un petit espace mémoire qui vous est propre, et donne à votre navigateur un
**cookie** (une petite information stockée par le navigateur, ici nommé
`JSESSIONID`, géré automatiquement par Tomcat) qui sert de "badge" pour vous
reconnaître aux requêtes suivantes.

Après une connexion réussie (`LoginServlet.doPost`) :

```java
HttpSession session = request.getSession(true);
session.setAttribute("user", utilisateur.getPseudo());
session.setAttribute("userId", utilisateur.getId());
```

Chaque page qui nécessite d'être connecté commence par le même contrôle :

```java
HttpSession session = request.getSession(false);
if (session == null || session.getAttribute("userId") == null) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
}
```

`LogoutServlet` efface tout avec `session.invalidate()`.

**Limite connue** : ce contrôle vérifie juste "êtes-vous connecté", jamais "avez-vous
le droit sur CE tableau ou CETTE tâche précisément". Voir sections 7/8.

### Exemple concret, ligne par ligne : `TaskEditServlet`

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

1. Déclare que cette classe répond à `/task/edit`, seulement en POST (il n'y a pas
   de `doGet` — donc si on visite cette URL avec un navigateur normal, on obtient
   une erreur "méthode non autorisée").
2. Récupère le Service une seule fois, dans le tiroir partagé (`ServiceFactory`).
3. Garde-fou de session : pas connecté → on renvoie vers `/login`, on arrête tout.
4. Lecture des champs envoyés par le formulaire. Tout arrive sous forme de texte
   brut (`String`) côté Servlet — contrairement à un framework, il n'y a pas de
   conversion automatique en nombre ou en objet, il faut la faire soi-même.
5. Une vérification "de surface" (le champ n'est pas vide) est faite **ici, tout de
   suite** — ça évite de déranger la base de données pour rien si l'information de
   base manque déjà.
6. Le vrai travail (vérifier les règles, écrire en base, garder une trace dans
   l'historique) est délégué au Service — la Servlet ne touche jamais directement
   à la base de données.
7. Une fois l'action réussie, on ne renvoie pas de page directement : on redirige
   le navigateur (code HTTP 302) vers une autre URL, qui elle fera un GET. Ce
   schéma s'appelle **Post/Redirect/Get** : il évite qu'un rafraîchissement (F5)
   ne renvoie le même formulaire une deuxième fois par erreur.
8. Si le Service refuse (ex. nom déjà pris), l'erreur est récupérée ici et
   transformée en message lisible, transporté dans l'URL de redirection
   (`&error=...`, mis sous une forme sûre pour une URL via `URLEncoder`).

### Le trajet complet d'une requête (exemple : modifier une tâche)

```
Navigateur : l'utilisateur clique sur "Enregistrer" dans le formulaire
      │
      ▼
Tomcat route la requête vers TaskEditServlet.doPost
      │
      ▼
TaskEditServlet : vérifie que l'utilisateur est connecté, lit les champs du formulaire
      │
      ▼
ITacheService.modifier(...)  →  ITacheRepository.update(...) (requête SQL UPDATE)
                              →  IActionRepository.save(...) (garde une trace dans l'historique)
      │
      ▼
TaskEditServlet : renvoie "va voir ailleurs" → /board?id=X&updated=1   (HTTP 302)
      │
      ▼
Navigateur : refait automatiquement une nouvelle requête GET /board?id=X&updated=1
      │
      ▼
BoardServlet.doGet : reconstruit toute la page (colonnes, tâches, commentaires...)
      │
      ▼
Thymeleaf transforme les données en HTML
      │
      ▼
Navigateur : reçoit la page complète, le message "La tâche a bien été modifiée." s'affiche
```

---

## 3. Intégration Stripe (mode bac à sable / test)

Stripe est un service externe qui gère les paiements par carte à notre place — on
ne manipule jamais de vrai numéro de carte bancaire nous-mêmes, on redirige
l'utilisateur vers une page hébergée par Stripe.

### Le flux complet (créer un tableau payant)

```
BoardNewServlet.doPost("/board/new")
   │ vérifie le nom, le garde de côté en session le temps du paiement
   ▼
IStripeService.createCheckoutSession(500, "eur", "Création du tableau « X »",
                                      successUrl, cancelUrl)
   │ demande à Stripe de préparer une page de paiement
   ▼
response.sendRedirect(checkout.getUrl())    → l'utilisateur atterrit sur la page Stripe
   │
   ▼ (l'utilisateur paie avec une carte de test)
   │
GET /board/new?status=success&session_id=...   → Stripe renvoie l'utilisateur chez nous
   │
   ▼
finaliserApresPaiement() : on redemande à Stripe "ce paiement est-il vraiment passé ?"
   │ (stripeService.retrieveSession(sessionId), on vérifie le statut "paid")
   ▼
Le tableau est enfin créé, avec 4 colonnes par défaut (À faire / En cours / En revue / Terminé)
   │
   ▼
redirection vers /board?id=X&created=1
```

Si l'utilisateur annule (`status=cancel`), on oublie simplement le tableau en
attente et on réaffiche le formulaire.

**Ce projet n'utilise pas de "webhook"** — un webhook, c'est une notification que
Stripe pourrait envoyer directement à notre serveur pour confirmer un paiement, de
façon fiable et indépendante du navigateur. Ici, la confirmation se fait seulement
au retour de l'utilisateur sur notre page. Ça marche pour une démo, mais c'est
fragile : si l'utilisateur paie puis ferme l'onglet avant d'être redirigé, Stripe a
bien pris l'argent, mais notre application ne le saura jamais et ne créera pas le
tableau.

### Où sont les clés, et comment elles sont protégées

Une **clé API**, c'est un mot de passe secret qui permet à notre application de
"parler" à Stripe en son nom. Elle ne doit jamais apparaître en clair dans le code
(sinon n'importe qui lisant le code source, y compris sur GitHub, pourrait
l'utiliser).

- `StripeConfiguration` (s'exécute une fois au démarrage) lit `STRIPE_API_KEY`
  depuis un fichier `.env` grâce à la librairie `dotenv-java` :
  ```java
  Dotenv dotenv = Dotenv.configure().ignoreIfMissing().ignoreIfMalformed().load();
  String apiKey = dotenv.get("STRIPE_API_KEY");
  if (apiKey == null) apiKey = System.getProperty("stripe.api.key"); // solution de secours au déploiement
  ```
- Le vrai fichier `.env` (`src/main/resources/.env`) est **volontairement exclu du
  suivi Git** (présent dans `.gitignore`, ligne 52) — il n'est jamais envoyé sur
  GitHub. À la place, un fichier `.env.example` (celui-là bien versionné) montre
  la liste des clés attendues, mais avec des valeurs bidons
  (`STRIPE_API_KEY=sk_test_replace_me`).
- La clé n'est lue qu'une fois au démarrage, puis réutilisée — jamais reloguée en
  clair dans les logs.

### Bac à sable (sandbox) vs vraie mise en production

| | Mode actuel (test) | Production |
|---|---|---|
| Clé | Commence par `sk_test_...` | Commence par `sk_live_...` |
| Cartes utilisables | Cartes factices fournies par Stripe (ex. `4242 4242 4242 4242`) | Vraies cartes bancaires |
| Argent | Aucun mouvement réel | Vrai prélèvement |
| Confirmation du paiement | On redemande "et alors ?" au retour du navigateur | À ajouter : un vrai webhook, avec une signature vérifiée, pour être fiable même si le navigateur se ferme |
| Connexion | HTTP local accepté | HTTPS obligatoire |

---

## 4. Envoi d'e-mails via SMTP Gmail

**SMTP**, c'est le protocole standard (la méthode convenue) pour qu'un serveur
envoie un email. Ici, on passe par les serveurs SMTP de Gmail plutôt que par un
service dédié à l'envoi d'emails.

### Configuration utilisée

`EmailServiceImpl` (via la librairie Jakarta Mail) :

```java
props.put("mail.smtp.auth", "true");
props.put("mail.smtp.starttls.enable", "true");   // on chiffre la connexion, port 587
props.put("mail.smtp.host", host);                // smtp.gmail.com
props.put("mail.smtp.port", port);                // 587
```

`STARTTLS` veut dire que la connexion avec le serveur Gmail est chiffrée : personne
ne peut lire en clair les identifiants qui transitent sur le réseau (contrairement
à une connexion non protégée).

### Où sont gérés les identifiants

Même principe que pour Stripe : le nom d'utilisateur et le mot de passe SMTP sont
lus depuis `.env` (jamais écrits en dur dans le code). Si l'information manque, un
simple booléen `configure` passe à `false` :

```java
if (!configure) {
    LOGGER.warn("Configuration SMTP absente ou incomplète, "
               + "les notifications par email sont désactivées.");
}
```

Choix assumé : **l'envoi d'un email ne doit jamais faire planter le reste de
l'application**. Si le SMTP n'est pas configuré, `envoyerNotificationAssignation()`
ne fait simplement rien — assigner une tâche à quelqu'un fonctionne quand même,
juste sans email de notification.

### Exemple concret : notifier une assignation de tâche

Dans `TacheServiceImpl.creer()` et `.modifier()`, si une tâche reçoit un nouvel
assigné :

```java
if (assigneId != null) {
    notifierAssignation(assigneId, name);   // → retrouve l'email de la personne, puis envoie
}
```

Le message envoyé est un texte simple (pas de mise en forme HTML), avec le sujet
*"Vous avez été assigné à une tâche"*. Si l'envoi échoue, l'erreur est juste notée
dans les logs — jamais montrée à l'utilisateur, cohérent avec le principe "un email
raté ne doit jamais bloquer une action".

### Limites du SMTP Gmail

- **Mot de passe d'application obligatoire** : ce n'est pas le vrai mot de passe du
  compte Gmail, mais un mot de passe spécial généré par Google, utilisable
  uniquement par une application tierce (obligatoire dès que la double
  authentification est activée sur le compte, ce qui est presque toujours le cas
  aujourd'hui).
- **Quota limité** : environ 500 emails/jour pour un compte Gmail gratuit — pas
  du tout adapté à un gros volume d'envois.
- Pas de suivi (savoir si l'email a été ouvert, ou s'il a "rebondi").
- Risque d'être classé comme spam si le volume ou le contenu déclenche les filtres
  de Google.
- Pour un vrai produit en production : passer par un service dédié à l'envoi
  d'emails (SendGrid, Mailgun, Amazon SES), avec le domaine correctement configuré
  pour être reconnu comme fiable.

---

## 5. Base de données H2

H2 est une base de données légère, entièrement écrite en Java, qui peut tourner
sans rien installer sur la machine (contrairement à MySQL ou PostgreSQL qui
demandent un vrai serveur séparé).

### Fonctionnement : mode fichier

`ConnectionManager` :

```java
private static final String URL = "jdbc:h2:file:./kanban_db;AUTO_SERVER=TRUE";
```

H2 peut fonctionner en **mode mémoire** (tout est effacé quand on éteint
l'application, comme écrire sur un tableau blanc) ou en **mode fichier** (les
données sont écrites sur le disque, comme un carnet papier, et restent après un
redémarrage). Ce projet utilise le **mode fichier** : un fichier `kanban_db.mv.db`
est créé au premier lancement et conserve toutes les données. `AUTO_SERVER=TRUE`
permet à plusieurs programmes (l'IDE et les tests, par exemple) de se connecter en
même temps à ce fichier.

Il n'y a pas de "console H2" (une page web pour explorer la base à la souris)
activée dans ce projet — pour regarder ce qu'il y a dans la base, on ouvre
directement le fichier avec un outil de base de données (déjà configuré dans
`.idea/dataSources.xml` pour IntelliJ).

Petit détail technique bon à connaître : le pilote (**driver**) qui permet à Java de
parler à H2 doit être chargé explicitement dans un bloc `static {}` de
`ConnectionManager` (`Class.forName("org.h2.Driver")`). Sous Tomcat, le mécanisme
habituel qui trouve automatiquement ce pilote ne fonctionne pas à cause de la façon
dont Tomcat isole le code de chaque application — sans cette ligne, la connexion à
la base échouerait au démarrage.

### Comment la base est créée et remplie

`DatabaseConfiguration` (s'exécute une seule fois, au tout premier démarrage de
l'application) exécute :

```java
stmt.execute("RUNSCRIPT FROM 'classpath:import.sql'");
```

Le fichier `src/main/resources/import.sql` contient les commandes qui créent les 9
tables (`utilisateur`, `type_tache`, `tableau`, `utilisateur_tableau`, `colonne`,
`tache`, `piece_jointe`, `commentaire`, `action`) et qui ajoutent les 4 types de
tâche de base (Standard/Bug/Spike/Amélioration). Ce script est écrit pour être
**rejouable sans risque** (`CREATE TABLE IF NOT EXISTS`, `MERGE INTO`) : si on le
lance deux fois, rien ne casse ni ne se duplique.

> Détail utile si le jury demande "pourquoi une classe spéciale pour ça, pas juste
> l'URL de connexion ?" : au départ, ce script se relançait à **chaque** connexion
> ouverte à la base, ce qui ralentissait énormément l'application (il n'y a pas de
> réserve de connexions déjà prêtes — voir plus bas). Il est maintenant joué une
> seule fois, au tout début.

### Pourquoi H2 pour ce projet — et ses limites à assumer

**Avantages, pour un projet étudiant / une démo** :
- Aucune installation à faire (pas de serveur MySQL/PostgreSQL à monter).
- Démarrage instantané, le fichier de données se déplace avec le projet.
- Largement suffisant pour peu de données et peu d'utilisateurs en même temps.
- Utilise du SQL classique, donc peu de choses à changer si on migre plus tard.

**Limites, à assumer si le jury pousse** :
- Pas de **pool de connexions** (une réserve de connexions déjà ouvertes, prêtes à
  être réutilisées) : chaque appel à la base ouvre puis referme sa propre
  connexion. Ça marche, mais ce n'est pas la manière la plus rapide de faire à
  grande échelle.
- Pas pensée pour beaucoup d'utilisateurs en même temps, ni pour de la haute
  disponibilité (pas de copie de secours automatique).
- Pas d'outil de suivi des changements de structure de la base (pas de
  Flyway/Liquibase) — juste des commandes SQL rejouables à chaque démarrage.
- Passer en production reviendrait à changer l'adresse de connexion et le pilote
  dans `ConnectionManager` (pour PostgreSQL ou MySQL) et ajouter un vrai pool de
  connexions — le reste du code ne change presque pas, car il n'y a pas d'ORM
  propriétaire, juste du SQL standard.

---

## 6. Thymeleaf

### Son rôle

Thymeleaf génère des pages HTML **côté serveur** : la page complète est déjà
construite avant même d'arriver au navigateur, contrairement à une application
"tout en JavaScript" (React, Vue...) où le navigateur assemble la page lui-même.
Les seuls moments où le JavaScript agit tout seul sans recharger la page sont
ciblés, comme le glisser-déposer d'une tâche (`/task/move` appelé en `fetch()`
depuis `kanban.js`).

Le moteur Thymeleaf est construit une seule fois au démarrage
(`ThymeleafConfiguration`), rangé dans un tiroir partagé
(`ServletContext.setAttribute("templateEngine", ...)`), puis chaque Servlet vient
le récupérer dans son `init()` :

```java
templateEngine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
```

### La syntaxe utilisée dans le projet

Thymeleaf ajoute des attributs spéciaux (préfixés `th:`) directement dans le HTML.
Exemples réels tirés de `board.html` / `dashboard.html` :

| Attribut | Exemple réel | Ce que ça fait, en clair |
|---|---|---|
| `th:text` | `<span th:text="${tache.name}">Nom de la tâche</span>` | Remplace le texte affiché par la vraie valeur |
| `th:each` | `<div th:each="tache : ${colonne.taches}">` | Répète le bloc HTML une fois par élément de la liste (boucle) |
| `th:if` | `<div th:if="${!#lists.isEmpty(tache.pieceJointes)}">` | N'affiche ce bloc que si la condition est vraie |
| `th:attr` | `th:attr="data-modal-target='modal-' + ${tache.id}, ..."` | Pose plusieurs attributs HTML calculés en une seule fois |
| `th:href="@{...}"` | `th:href="@{/board(id=${tableau.id})}"` | Construit un lien correct même si l'application n'est pas déployée à la racine du site |
| `th:classappend` | `th:classappend="'badge-' + ${tache.typeClasse}"` | Ajoute une classe CSS calculée, sans effacer les classes déjà présentes |
| `th:field` | *non utilisé* | Pas de remplissage automatique de formulaire : chaque champ `name=` est lu à la main côté Servlet |
| `th:fragment` / `th:replace` | *non utilisé* | La barre de navigation est copiée-collée dans chaque page (`board.html`, `dashboard.html`, `login.html`...) plutôt que d'être écrite une seule fois et réutilisée. C'est une amélioration facile à faire (section 7). |

### Comment les données passent du code Java vers la page

La Servlet prépare des "fiches d'affichage" (`viewmodel/`, jamais les fiches brutes
de la base) et les range dans un contexte :

```java
context.setVariable("colonnes", colonnes(tableau.getId(), utilisateurs));
```

Le template lit ensuite `${colonnes}`. Ça évite que la page HTML ait besoin de
connaître les détails de la base de données (ex. le fait qu'un type de tâche est
juste un numéro en base ; côté page, on ne voit que le libellé et la couleur déjà
calculés).

### Protection automatique contre le XSS

**XSS** (*Cross-Site Scripting*), c'est le fait qu'un utilisateur malveillant écrive
du code (par exemple `<script>...</script>`) dans un champ de formulaire, dans
l'espoir que ce code s'exécute plus tard dans le navigateur d'un autre utilisateur
qui verra cette donnée affichée. Par défaut, `th:text` **échappe** automatiquement
le contenu (transforme les caractères spéciaux pour qu'ils s'affichent tels quels,
au lieu d'être interprétés comme du code) — le projet n'utilise jamais `th:utext`
(la version "non protégée"). Une description de tâche contenant du code JavaScript
s'affiche donc comme du texte inoffensif, jamais exécuté.

---

## 7. Points transverses

### Comment on accède aux données (sans ORM)

Un **ORM** (comme Hibernate/JPA) est un outil qui transforme automatiquement des
lignes de base de données en objets Java, et inversement. Ce projet **n'en utilise
pas** : tout le SQL est écrit à la main, avec du JDBC "brut" (l'API standard de
Java pour parler à une base de données). Chaque entité a une interface
(`ITacheRepository`) et une implémentation (`TacheRepositoryImpl`) — c'est le
**pattern Repository**, qui isole "comment on parle à la base" du reste du code.
Toutes les requêtes SQL du projet sont regroupées dans **un seul fichier**,
`Requetes.java`, sous forme de textes constants, classés par entité.

```java
try (Connection conn = ConnectionManager.getConnection();
     PreparedStatement stmt = conn.prepareStatement(Requetes.INSERT_TACHE, Statement.RETURN_GENERATED_KEYS)) {
    ...
}
```

Un `PreparedStatement` (requête préparée), c'est une requête SQL écrite avec des
"trous" (les `?`) qu'on remplit ensuite avec les vraies valeurs — voir juste en
dessous pourquoi c'est important pour la sécurité.

### Sécurité

- **Injection SQL** : impossible ici. Une injection SQL, c'est quand un
  utilisateur écrit volontairement du code SQL dans un champ de formulaire pour
  détourner la requête d'origine (par exemple pour se connecter sans mot de
  passe). Comme le projet utilise uniquement des requêtes préparées
  (`PreparedStatement`, jamais de texte SQL "collé" avec les valeurs de
  l'utilisateur), ce que la personne tape est toujours traité comme une simple
  donnée, jamais comme une commande.
- **XSS** : voir section 6 — Thymeleaf protège par défaut.
- **Mots de passe** : jamais stockés en clair. Ils sont transformés en une
  "empreinte" (**hash**) impossible à retransformer en mot de passe d'origine,
  avec l'algorithme **PBKDF2WithHmacSHA256** (`UtilisateurServiceImpl`). Un
  **sel** (une valeur aléatoire différente à chaque compte) est ajouté avant le
  calcul, pour que deux personnes avec le même mot de passe n'aient jamais la
  même empreinte stockée — ça empêche aussi les attaques par "dictionnaire
  précalculé". Ce n'est pas l'algorithme le plus moderne (bcrypt/argon2 sont
  aujourd'hui recommandés en premier choix), mais c'est un algorithme standard et
  reconnu, largement suffisant pour ce projet.
- **Upload de fichiers** (pièces jointes) : le nom du fichier envoyé par
  l'utilisateur est nettoyé avant d'être stocké
  (`Paths.get(...).getFileName()` dans `TaskAttachmentUploadServlet`), pour
  empêcher qu'un nom bizarre comme `../../evil.sh` ne pose problème.
- **CSRF** (*Cross-Site Request Forgery*) : **pas de protection** dans ce projet.
  C'est une attaque où un site malveillant fait discrètement envoyer une requête
  par le navigateur d'un utilisateur déjà connecté chez nous (par exemple "supprimer
  ce tableau"), sans qu'il l'ait vraiment voulu. Limite connue, à assumer si le
  jury la soulève.
- **Droits d'accès** : chaque page protégée vérifie juste "êtes-vous connecté",
  jamais "êtes-vous bien membre de CE tableau précis". Ce n'est pas exploité dans
  la démo (il faudrait deviner l'identifiant d'un tableau appartenant à quelqu'un
  d'autre), mais la vérification n'existe pas. Limite connue et cohérente sur
  tout le projet (même logique partout, pas d'oubli isolé).

### Authentification

Un seul système d'inscription/connexion : `RegisterServlet` (`/register`) +
`LoginServlet` (`/login`), avec l'entité `Utilisateur` et un mot de passe haché
(voir ci-dessus). Un tout premier prototype d'inscription, plus rudimentaire (mot
de passe stocké en clair, données jamais sauvegardées en base), a été identifié
puis nettoyé — il n'était relié à aucun lien dans l'interface.

### Organisation Maven

Maven gère la compilation, les dépendances et l'empaquetage (`pom.xml`, produit un
fichier `.war` — le format que Tomcat sait déployer). Java 25. Dépendances clés :

| Dépendance | Portée | Rôle |
|---|---|---|
| `jakarta.servlet-api` | `provided` (fournie par Tomcat, pas embarquée dans le `.war`) | Gérer le HTTP |
| `thymeleaf` | compile | Moteur de templates |
| `h2` | `runtime` | Base de données |
| `stripe-java` | compile | Paiement |
| `dotenv-java` | compile | Lire `.env` |
| `jakarta.mail` (Eclipse Angus) | compile | Envoyer des emails |
| `gson` | compile | JSON pour l'échange avec le paiement |
| `log4j-api` / `log4j-core` | compile | Logs |
| `lombok` | `provided` | Génère le code répétitif à la compilation |
| `junit-jupiter`, `mockito-junit-jupiter` | `test` | Tests automatiques |

À noter : la dépendance `hibernate-validator` est présente dans `pom.xml` mais
**n'est utilisée nulle part** dans le code — toute la validation est écrite à la
main dans les Servlets/Services. C'est une dépendance ajoutée tôt dans le projet et
jamais exploitée, pas un choix d'architecture.

### Déploiement

Le fichier `.war` produit par `mvn clean package` se dépose dans Tomcat (en local,
configuré dans l'IDE — voir `.idea/workspace.xml`, Tomcat 11). Comme `web.xml` est
vide, ajouter une nouvelle page ne demande jamais de modifier un fichier de
configuration : il suffit d'écrire une nouvelle classe avec `@WebServlet`.

### Pourquoi Servlets + Thymeleaf plutôt que Spring Boot

Choix pédagogique assumé : comprendre ce qu'un framework comme Spring fait pour
nous, en le refaisant à la main.

| Ce que ferait Spring | Ce qu'on a écrit nous-mêmes à la place |
|---|---|
| `@Controller` / `@GetMapping` | `HttpServlet` + `@WebServlet` |
| Conteneur qui fabrique et fournit les objets (IoC) | `ServiceFactory` (un tiroir de singletons fait main) |
| Configuration exécutée au démarrage | `ServletContextListener` (`@WebListener`) |
| Intégration automatique du moteur de vue | Câblage manuel de Thymeleaf (`WebContext`) |

**Limites connues de cette approche "tout à la main"** :
- Beaucoup de code se répète d'une Servlet à l'autre (lire un paramètre en nombre,
  vérifier la session, rediriger avec un message d'erreur...) — pas de mécanisme
  commun pour éviter cette répétition.
- Pas de validation automatique des champs de formulaire (pas d'annotations comme
  `@NotBlank` réellement utilisées, malgré `hibernate-validator` présent) — tout
  est vérifié à la main.
- Pas de gestion centralisée des erreurs : chaque Servlet fait son propre
  `try/catch`.
- Pas de **transaction** (un mécanisme qui garantit que plusieurs opérations en
  base réussissent ou échouent toutes ensemble) : quand `TacheServiceImpl.creer()`
  enregistre une tâche PUIS une entrée d'historique, ce sont deux opérations
  séparées. Si la deuxième échoue, la première reste quand même enregistrée — un
  petit risque d'incohérence, acceptable à cette échelle.

**Piste d'amélioration concrète à citer à l'oral** : remplacer le contrôle de
session copié-collé dans chaque Servlet par un `Filter` (un mécanisme Jakarta qui
intercepte les requêtes avant qu'elles n'atteignent la Servlet) unique, appliqué à
toutes les routes protégées.

---

## 8. Questions probables du jury + réponses courtes

**Q1 — "H2 ne tiendrait pas la charge en production, pourquoi l'avoir choisi ?"**
> Aucune installation, portable, démarrage instantané — largement suffisant pour un
> projet étudiant avec peu d'utilisateurs en même temps. Passer à PostgreSQL
> reviendrait à changer l'adresse de connexion et le pilote dans
> `ConnectionManager` ; le reste ne bouge presque pas car il n'y a pas d'ORM
> propriétaire, juste du SQL classique.

**Q2 — "Où sont vos clés Stripe/Gmail, elles ne sont pas dans le code ?"**
> Dans un fichier `.env`, jamais envoyé sur GitHub (explicitement exclu dans
> `.gitignore`). Un fichier `.env.example` montre quelles clés sont attendues,
> avec des valeurs bidons. En déploiement, le code sait aussi lire une variable
> système en remplacement.

**Q3 — "Pourquoi pas Spring Boot ?"**
> Contrainte pédagogique : comprendre comment une requête HTTP est traitée et
> comment les couches s'articulent, sans que le framework le fasse à notre place.
> `ServiceFactory` joue le rôle d'un conteneur qui fournit les objets, les
> `ServletContextListener` remplacent la configuration automatique au démarrage.

**Q4 — "Le paiement Stripe, c'est vraiment fiable ?"**
> C'est une page de paiement hébergée par Stripe, en mode test — aucune donnée de
> carte ne transite par notre serveur. Limite assumée : pas de notification
> automatique (webhook) de Stripe vers nous, la confirmation se fait seulement au
> retour du navigateur. Si l'utilisateur ferme l'onglet juste après avoir payé,
> Stripe a bien encaissé, mais le tableau n'est jamais créé côté application.

**Q5 — "Comment testez-vous l'application ?"**
> Avec JUnit 5 et Mockito. Des tests qui vérifient la logique métier avec une base
> de données simulée (ex. `TacheServiceTest`), et des tests qui vérifient l'accès
> aux données contre la vraie base H2 (ex. `TacheRepositoryTest`). Tous passent
> avec `mvn test`. Il n'y a pas de tests automatiques qui simulent un vrai
> navigateur cliquant sur les pages.

**Q6 — "Comment gérez-vous les erreurs ?"**
> Pas de mécanisme central : chaque Servlet attrape elle-même les erreurs métier
> et redirige avec un message d'erreur glissé dans l'URL, affiché ensuite par la
> page. Les erreurs techniques imprévues (ex. un problème SQL) sont notées dans
> les logs et remontent jusqu'à une page d'erreur générique de Tomcat — pas de
> page d'erreur personnalisée.

**Q7 — "Vous avez pensé à la sécurité (injection SQL, XSS) ?"**
> Oui : toutes les requêtes SQL sont préparées (donc pas d'injection possible),
> Thymeleaf échappe automatiquement ce qu'il affiche (donc pas de XSS), et les
> mots de passe sont hachés avec un sel aléatoire. Limites assumées : pas de
> protection contre le CSRF, pas de vérification fine de qui a le droit de voir
> quel tableau.

**Q8 — "Pourquoi Thymeleaf plutôt qu'une API + un front JavaScript séparé ?"**
> Le rendu se fait côté serveur, donc pas besoin de gérer un projet front en plus
> pour ce périmètre. Les quelques interactions qui doivent être immédiates (comme
> déplacer une tâche) passent par un simple appel `fetch()` ciblé, sans réécrire
> toute l'application en JavaScript.

**Q9 — "Il n'y a pas de code mort ou de doublons dans le projet ?"**
> Il y en a eu : un tout premier prototype d'inscription (mot de passe en clair,
> données jamais sauvegardées) coexistait avec le vrai système (mot de passe
> haché, sauvegardé en base), et deux vieilles pages essayaient d'afficher un
> template qui n'existait plus. Ce code n'était relié à aucun lien visible dans
> l'application — il a été repéré puis supprimé. Aujourd'hui, il n'y a qu'un seul
> chemin d'inscription/connexion : `/register` et `/login`.
