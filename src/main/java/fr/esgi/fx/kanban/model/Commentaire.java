package fr.esgi.fx.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Commentaire {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long tacheId;
    private Long utilisateurId;
}
