package fr.esgi.fx.kanban.model;

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
    private Long createdBy;
    private String stripeSessionId;
}
