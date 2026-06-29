package fr.esgi.phil.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    private Long id;
    private String description;
    private LocalDateTime createdAt;
    private Long tacheId;
    private Long utilisateurId;
    private Long colonneSourceId;
    private Long colonneCibleId;
}
