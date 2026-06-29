package fr.esgi.phil.kanban.model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    private Long id;
    private String pseudo;
    private String email;
    private String password;
    private LocalDateTime createdAt;
}
