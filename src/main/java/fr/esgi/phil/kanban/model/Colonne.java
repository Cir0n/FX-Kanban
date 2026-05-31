package fr.esgi.phil.kanban.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Colonne {
    private Long id;
    private String name;
    private int position;
    private Long tableauId;
}
