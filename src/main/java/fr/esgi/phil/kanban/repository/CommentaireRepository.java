package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Commentaire;
import java.util.List;

public interface CommentaireRepository {
    List<Commentaire> findByTacheId(Long tacheId);
    Commentaire save(Commentaire commentaire);
}