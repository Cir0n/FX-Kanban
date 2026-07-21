package fr.esgi.fx.kanban.viewmodel;

import lombok.*;

/** Représentation d'une pièce jointe pour l'affichage dans la modale de détail d'une tâche. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointeVue {
    private Long id;
    private String nomFichier;
    private String taille; // déjà formatée pour l'affichage (ex. "128 Ko")
    private String date;
}
