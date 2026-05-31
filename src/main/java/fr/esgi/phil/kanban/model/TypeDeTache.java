package fr.esgi.phil.kanban.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeDeTache {
    private Long id;
    private String name;
    private String couleur;
}
