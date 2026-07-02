package fr.esgi.fx.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointe {
    private Long id;
    private String nomFichier;
    private String mimeType;
    private byte[] contenu;
    private LocalDateTime createdAt;
    private Long tacheId;
}