package fr.esgi.fx.kanban.repository;

import fr.esgi.phil.kanban.model.PieceJointe;
import java.util.List;

public interface IPieceJointeRepository {
    List<PieceJointe> findByTacheId(Long tacheId);
    PieceJointe save(PieceJointe pieceJointe);
    void delete(Long id);
}