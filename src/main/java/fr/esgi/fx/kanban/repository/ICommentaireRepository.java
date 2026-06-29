package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.Commentaire;
import java.util.List;

public interface ICommentaireRepository {
    List<Commentaire> findByTacheId(Long tacheId);
    Commentaire save(Commentaire commentaire);
}