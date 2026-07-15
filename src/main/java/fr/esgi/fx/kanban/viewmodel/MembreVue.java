package fr.esgi.fx.kanban.viewmodel;

import lombok.*;

/** Représentation d'un membre pour l'affichage (avatar). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembreVue {
    private Long id;
    private String initiales;
    private String couleur; // couleur de fond de l'avatar (hex)
}
