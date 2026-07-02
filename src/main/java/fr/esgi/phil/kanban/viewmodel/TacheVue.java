package fr.esgi.phil.kanban.viewmodel;

import lombok.*;
import java.util.List;

/** Représentation d'une tâche pour l'affichage en carte sur un tableau Kanban. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacheVue {
    private Long id;
    private String name;
    private String description;
    private String typeClasse;   // standard | bug | spike | amelio (suffixe des classes CSS)
    private String typeLabel;    // libellé affiché dans le badge (ex. "Bug")
    private MembreVue assignee;  // membre affecté (avatar), peut être null
    private String pieceJointeNom; // nom du fichier joint, null si aucune pièce jointe
    private List<CommentaireVue> commentaires;
}
