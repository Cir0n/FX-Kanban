package fr.esgi.fx.kanban.viewmodel;

import lombok.*;

/** Représentation d'une entrée d'historique pour l'affichage dans la modale de détail d'une tâche. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionVue {
    private String description;
    private String auteur;       // pseudo affiché
    private String initiales;    // initiales de l'avatar
    private String couleur;      // couleur de fond de l'avatar (hex)
    private String date;         // date déjà formatée pour l'affichage
}
