package fr.esgi.phil.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tableau {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long utilisateurId;
}
