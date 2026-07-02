package fr.esgi.fx.kanban.viewmodel;

import lombok.*;
import java.util.List;

/** Représentation d'un tableau Kanban pour l'affichage en carte sur le dashboard. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableauVue {
    private Long id;
    private String name;
    private String couleur;   // couleur de la barre supérieure (hex)
    private int nbTaches;
    private List<MembreVue> membres;
}
