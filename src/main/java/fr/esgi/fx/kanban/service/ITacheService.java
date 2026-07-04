package fr.esgi.fx.kanban.service;

import fr.esgi.fx.kanban.model.Tache;
import java.util.List;

public interface ITacheService {
    Tache creer(String name, String description, Long colonneId, Long typeId, Long utilisateurId);
    Tache findById(Long id);
    List<Tache> findByColonneId(Long colonneId);
    void deplacer(Long tacheId, Long nouvelleColonneId, Long utilisateurId);
    void supprimer(Long id);
}
