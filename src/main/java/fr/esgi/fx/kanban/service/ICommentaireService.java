package fr.esgi.fx.kanban.service;

import fr.esgi.phil.kanban.model.Commentaire;
import java.util.List;

public interface ICommentaireService {
    Commentaire ajouter(String content, Long tacheId, Long utilisateurId);
    List<Commentaire> findByTacheId(Long tacheId);
}
