package fr.esgi.phil.kanban.service;

import fr.esgi.phil.kanban.model.Tache;

public interface ITacheService {
    Tache creer(String name, String description, Long colonneId, Long typeId, Long utilisateurId);
    Tache findById(Long id);
    void deplacer(Long tacheId, Long nouvelleColonneId, Long utilisateurId);
    void supprimer(Long id);
}
