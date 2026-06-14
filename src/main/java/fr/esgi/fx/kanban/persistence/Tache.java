package fr.esgi.fx.kanban.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tache {
    private Long id; // BIGINT corresponds to Long in Java
    private String titre;
    private String description;
    private LocalDateTime dateCreation;

    // Relationships to other entities
    private Colonne colonne;
    private TypeTache typeTache;
    private Utilisateur utilisateur;
}