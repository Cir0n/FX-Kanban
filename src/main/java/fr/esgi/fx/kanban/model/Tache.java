package fr.esgi.fx.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tache {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long colonneId;
    private Long typeId;
    private Long createdBy;
    private Long utilisateurId;
}
