package fr.esgi.fx.kanban.service;

import fr.esgi.phil.kanban.model.Colonne;
import java.util.List;

public interface IColonneService {
    List<Colonne> findByTableauId(Long tableauId);
}
