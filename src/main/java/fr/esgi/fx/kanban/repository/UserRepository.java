package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository en mémoire pour les utilisateurs.
 * À remplacer par une implémentation avec base de données (JDBC, JPA, etc.).
 */
public class UserRepository {

    private static final UserRepository INSTANCE = new UserRepository();
    private final Map<String, User> users = new ConcurrentHashMap<>();

    private UserRepository() {
    }

    public static UserRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Recherche un utilisateur par nom.
     */
    public Optional<User> findByName(String name) {
        return Optional.ofNullable(users.get(name.toLowerCase()));
    }

    /**
     * Enregistre un nouvel utilisateur.
     *
     * @return true si l'utilisateur a été créé, false si le nom existe déjà
     */
    public boolean save(User user) {
        String key = user.getName().toLowerCase();
        if (users.containsKey(key)) {
            return false;
        }
        users.put(key, user);
        return true;
    }

    /**
     * Vérifie si un nom est déjà utilisé.
     */
    public boolean existsByName(String name) {
        return users.containsKey(name.toLowerCase());
    }
}


