package fr.esgi.phil.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    private Long id;
    private String action;
    private String description;
    private LocalDateTime createdAt;
    private Long utilisateurId;
    private Long tacheId;
    private Long colonneId;
}
