# Mise en place des bonnes pratiques

## GitHub
- Une branche par contributeur/fonctionnalité (`Nico`, `phil`, `will`, `rania`) fusionnée dans `Develop` puis `main`, plutôt que des push directs sur `main`
- Travail via Pull Requests (39 PR mergées sur le dépôt) → possibilité de relecture avant fusion
- Messages de commit normalisés par préfixe : `[FEATURE]`, `[FIX]`, `[REFACTOR]`, `[CLEANUP]`, `[TEST]`, `[DOCS]` — historique lisible, facile à parcourir
- `.gitignore` dédié (build Maven/IDE, fichiers de secrets) pour ne jamais committer ce qui ne doit pas l'être

## Clean Code (Robert C. Martin)
- **Noms qui révèlent l'intention** : méthodes métier nommées comme l'action réelle — `creer()`, `modifier()`, `deplacer()`, `supprimer()` (`TacheServiceImpl`), `renommer()`, `inviterContributeur()` (`TableauServiceImpl`) — se comprennent sans commentaire
- **Fonctions courtes, un seul niveau d'abstraction** : `BoardServlet.doGet()` orchestre à haut niveau (session → tableau → viewmodel → rendu) et délègue chaque détail à une méthode privée ciblée (`colonnes()`, `mapTache()`, `historique()`, `commentaires()`...), toutes < 25 lignes
- **Une responsabilité par classe (SRP)** : 17 servlets, chacune sur une route précise — ex. 3 servlets séparés (Upload/Download/Delete) pour les pièces jointes plutôt qu'un seul "god servlet"
- **Pas de `null`, exceptions plutôt que codes d'erreur** : tous les `findById` des repositories renvoient `Optional<T>` ; la couche service transforme l'absence en `IllegalArgumentException` explicite (`.orElseThrow(...)`) au lieu de laisser un `null` remonter
- **Commentaires qui expliquent le "pourquoi", pas le "quoi"** : ex. `ConnectionManager` documente *pourquoi* le driver H2 doit être enregistré manuellement sous Tomcat (conflit de classloader), pas ce que fait la ligne suivante
- **DRY** : tout le SQL centralisé dans `Requetes.java` (zéro requête écrite en dur ailleurs, vérifié sur les 8 repositories), formatage d'affichage centralisé dans `VueSupport.java` plutôt que dupliqué dans chaque servlet

> **Limite assumée** : le helper `parseLong` est copié-collé dans 11 servlets sur 17 au lieu d'être factorisé — une vraie entorse au DRY, gardée volontairement dans ce document pour montrer qu'on sait aussi identifier ce qui reste à améliorer.

## Sécurité du `.env`
- Aucun secret en dur dans le code : clé API Stripe, identifiants SMTP Gmail
- `.env` exclu du dépôt via `.gitignore` ; `.env.example` versionné comme modèle (valeurs bidons) pour que chacun sache quelles variables renseigner
- Chargement via `dotenv-java`, avec repli sur une variable d'environnement système en déploiement (pas de dépendance dure au fichier `.env`)
- **Limite assumée** : un fichier `.env` n'est pas un vrai coffre-fort à secrets — pas de rotation, pas de chiffrement au repos, pas d'audit d'accès
- **Évolution envisagée** : migrer vers un gestionnaire de secrets dédié (HashiCorp Vault, AWS/Azure Secrets Manager) pour la rotation automatique, le chiffrement et la traçabilité des accès en production
