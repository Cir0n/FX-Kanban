package fr.esgi.phil.kanban.viewmodel;

import lombok.*;
import java.util.List;

/** Représentation d'une colonne d'un tableau Kanban avec ses tâches. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColonneVue {
    private Long id;
    private String name;
    private List<TacheVue> taches;
}
