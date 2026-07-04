package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Colonne;
import java.util.List;

public interface IColonneService {
    List<Colonne> findByTableauId(Long tableauId);
    Colonne creer(String name, int position, Long tableauId);
}
