package fr.esgi.fx.kanban.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeTache {
    private Integer id;
    private String libelle;
}