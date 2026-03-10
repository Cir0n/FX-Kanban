package fr.esgi.fx.kanban.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    private Long id;
    private String pseudo;
    private String email;
    private String motDePasse;
}