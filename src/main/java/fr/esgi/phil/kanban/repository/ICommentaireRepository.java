package fr.esgi.phil.kanban.repository;

import fr.esgi.phil.kanban.model.Commentaire;
import java.util.List;

public interface ICommentaireRepository {
    List<Commentaire> findByTacheId(Long tacheId);
    Commentaire save(Commentaire commentaire);
}