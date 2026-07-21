package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.PieceJointe;
import java.util.List;

public interface IPieceJointeService {
    PieceJointe ajouter(String nomFichier, String mimeType, byte[] contenu, Long tacheId, Long utilisateurId);
    List<PieceJointe> findByTacheId(Long tacheId);
    PieceJointe findById(Long id);
    void supprimer(Long id, Long utilisateurId);
}
