package fr.esgi.fx.kanban.viewmodel;

import lombok.*;

/** Représentation d'un membre pour l'affichage (avatar). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembreVue {
    private Long id;
    private String nom;
    private String initiales;
    private String couleur;
}
