# Architecture du projet FX-Kanban

## Vue d'ensemble

FX-Kanban est une application web Java EE construite **sans framework** (pas de Spring, pas de Spring MVC). Elle repose directement sur les APIs Jakarta EE : Servlet API, Thymeleaf pour le rendu HTML, et un patron Repository pour l'accès aux données.

---

## Pourquoi pas de Controller ?

Dans Spring MVC, un `@Controller` est une surcouche qui intercepte les requêtes HTTP et les délègue automatiquement à des méthodes annotées (`@GetMapping`, `@PostMapping`, etc.). Ici, **on n'utilise pas Spring**, donc ce mécanisme n'existe pas.

À la place, chaque **Servlet joue le rôle de controller**. C'est exactement ce que fait Spring MVC en interne — il n'a fait qu'abstraire et automatiser ce que les Servlets font nativement depuis Java EE 1.

L'avantage de cette approche :
- Zéro dépendance à un framework lourd
- Contrôle total du cycle de vie des requêtes
- Compréhension profonde de ce qui se passe réellement sous le capot

---

## Architecture en couches

```
Requête HTTP
     │
     ▼
┌─────────────────────────────────────┐
│           Servlet Layer             │  ← Point d'entrée HTTP (= Controller)
│  (fr.esgi.phil.kanban.servlet)      │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│           Service Layer             │  ← Logique métier
│  (fr.esgi.phil.kanban.service)      │
└─────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────┐
│         Repository Layer            │  ← Accès aux données (interfaces)
│  (fr.esgi.phil.kanban.repository)   │
└─────────────────────────────────────┘
     │
     ▼
   Base de données
```

---

## Les Servlets : fonctionnement

### Qu'est-ce qu'une Servlet ?

Une Servlet est une classe Java qui reçoit une requête HTTP et produit une réponse HTTP. Elle est gérée par un **Servlet Container** (ici Tomcat) qui l'instancie, la garde en mémoire, et l'appelle à chaque requête.

Cycle de vie d'une Servlet :

```
Déploiement de l'application
        │
        ▼
   container.init()         ← appelé une seule fois au démarrage
        │
        ▼
   [requêtes entrantes]
        │
        ▼
   service(req, res)        ← appelé à chaque requête HTTP
    ├── doGet(req, res)     ← si méthode HTTP = GET
    ├── doPost(req, res)    ← si méthode HTTP = POST
    ├── doPut(req, res)     ← si méthode HTTP = PUT
    └── doDelete(req, res)  ← si méthode HTTP = DELETE
        │
        ▼
   container.destroy()      ← appelé une seule fois à l'arrêt
```

La classe de base est `jakarta.servlet.http.HttpServlet`. On surcharge les méthodes `doGet`, `doPost`, etc. selon les besoins.

### Exemple actuel : `HelloServlet`

```java
@WebServlet(name = "helloServlet", value = {"/index", "/"})
public class HelloServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() {
        // Récupère le moteur Thymeleaf initialisé au démarrage de l'app
        this.templateEngine = (TemplateEngine) getServletContext()
            .getAttribute("templateEngine");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Prépare le contexte Thymeleaf et rend le template HTML
        WebContext ctx = new WebContext(/* ... */);
        templateEngine.process("hello", ctx, response.getWriter());
    }
}
```

Points clés :
- `@WebServlet(value = {"/index", "/"})` — déclare les URLs écoutées par cette Servlet
- `init()` — exécuté une seule fois, sert à récupérer les dépendances depuis le `ServletContext`
- `doGet()` — traite les requêtes GET : récupère les données via les services, puis délègue le rendu à Thymeleaf
- Pas de `doPost()` ici pour l'instant, mais ce sera la méthode à surcharger pour traiter les formulaires

---

## Le routing : comment ça fonctionne ?

### Principe général

Le routing est déclaré directement sur chaque Servlet via l'annotation `@WebServlet`. Quand Tomcat démarre, il scanne toutes les classes annotées et construit une table de correspondance URL → Servlet.

```
GET /           → HelloServlet
GET /index      → HelloServlet
POST /login     → LoginServlet (à créer)
GET /tableau    → TableauServlet (à créer)
...
```

Il n'y a **pas de fichier de routing centralisé** (contrairement à Laravel, Express, ou Spring). Chaque Servlet déclare elle-même les routes qu'elle gère.

### Les deux mécanismes de déclaration

**1. Annotation `@WebServlet` (méthode utilisée dans ce projet)**

```java
@WebServlet(name = "monServlet", value = {"/ma-route", "/autre-route"})
public class MonServlet extends HttpServlet { ... }
```

C'est la méthode moderne (Servlet 3.0+). Aucune configuration XML requise.

**2. `web.xml` (méthode historique)**

```xml
<servlet>
    <servlet-name>monServlet</servlet-name>
    <servlet-class>fr.esgi.phil.kanban.servlet.MonServlet</servlet-class>
</servlet>
<servlet-mapping>
    <servlet-name>monServlet</servlet-name>
    <url-pattern>/ma-route</url-pattern>
</servlet-mapping>
```

Le `web.xml` de ce projet est volontairement vide — toute la configuration passe par les annotations.

### Convention de routing prévue

Pour rester cohérent et maintenable, chaque fonctionnalité aura **sa propre Servlet** :

| Route | Méthode | Servlet | Action |
|-------|---------|---------|--------|
| `/` | GET | `HelloServlet` | Page d'accueil |
| `/inscription` | GET | `AuthServlet` | Affiche le formulaire |
| `/inscription` | POST | `AuthServlet` | Traite l'inscription |
| `/connexion` | GET | `AuthServlet` | Affiche le formulaire |
| `/connexion` | POST | `AuthServlet` | Traite la connexion |
| `/tableau` | GET | `TableauServlet` | Liste des tableaux |
| `/tableau/creer` | POST | `TableauServlet` | Crée un tableau |
| `/tableau/{id}` | GET | `TableauServlet` | Détail d'un tableau |
| `/tache/creer` | POST | `TacheServlet` | Crée une tâche |
| `/tache/{id}/deplacer` | POST | `TacheServlet` | Déplace une tâche |

> **Note** : Les Servlets ne supportent pas nativement les paramètres de chemin (`/tableau/{id}`). Pour extraire l'id, on parse manuellement `request.getPathInfo()`.

### Exemple : Servlet avec route paramétrée

```java
@WebServlet("/tableau/*")
public class TableauServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo(); // ex: "/42"

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /tableau → liste tous les tableaux
        } else {
            Long id = Long.parseLong(pathInfo.substring(1)); // extrait "42"
            // GET /tableau/42 → affiche le tableau avec cet id
        }
    }
}
```

---

## Configuration : `ThymeleafConfiguration`

La classe `ThymeleafConfiguration` est un `ServletContextListener`. Elle s'exécute **une seule fois au démarrage de l'application**, avant que toute Servlet ne traite une requête.

```java
@WebListener
public class ThymeleafConfiguration implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Configure Thymeleaf
        TemplateEngine engine = new TemplateEngine();
        // templates dans /WEB-INF/templates/*.html
        sce.getServletContext().setAttribute("templateEngine", engine);
    }
}
```

Les Servlets récupèrent ensuite le `TemplateEngine` via `getServletContext().getAttribute("templateEngine")` dans leur méthode `init()`.

C'est le pattern **Service Locator** — une alternative manuelle à l'injection de dépendances puisqu'on n'a pas de conteneur IoC (pas de Spring).

---

## Structure des packages

```
fr.esgi.phil.kanban/
├── configuration/        ← Initialisation au démarrage (Thymeleaf, futures DB connections)
├── model/                ← Entités métier (Utilisateur, Tableau, Colonne, Tache, ...)
├── repository/           ← Interfaces d'accès aux données (IUtilisateurRepository, ...)
├── service/              ← Interfaces de logique métier + implémentations (impl/)
└── servlet/              ← Points d'entrée HTTP, un par domaine fonctionnel
```

---

## Stack technique

| Technologie | Rôle | Version |
|-------------|------|---------|
| Jakarta Servlet API | Gestion des requêtes HTTP | 6.1.0 |
| Thymeleaf | Moteur de templates HTML | 3.1.3 |
| Lombok | Réduction du boilerplate Java | 1.18.42 |
| Hibernate Validator | Validation des données | 9.0.1 |
| JUnit Jupiter | Tests unitaires | 5.13.2 |
| Java | Langage | 23 |
| Maven | Build + gestion dépendances | — |
| Tomcat | Servlet Container (déploiement WAR) | — |
