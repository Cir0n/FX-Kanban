package fr.esgi.fx.kanban.repository;

import fr.esgi.fx.kanban.model.PieceJointe;
import java.util.List;
import java.util.Optional;

public interface IPieceJointeRepository {
    List<PieceJointe> findByTacheId(Long tacheId);
    Optional<PieceJointe> findById(Long id);
    PieceJointe save(PieceJointe pieceJointe);
    void delete(Long id);
}