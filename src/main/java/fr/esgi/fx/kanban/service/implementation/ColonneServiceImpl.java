package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Colonne;
import fr.esgi.fx.kanban.repository.IColonneRepository;
import fr.esgi.fx.kanban.service.IColonneService;

import java.util.List;

public class ColonneServiceImpl implements IColonneService {

    private final IColonneRepository colonneRepository;

    public ColonneServiceImpl(IColonneRepository colonneRepository) {
        this.colonneRepository = colonneRepository;
    }

    @Override
    public List<Colonne> findByTableauId(Long tableauId) {
        return colonneRepository.findByTableauId(tableauId);
    }

    @Override
    public Colonne creer(String name, int position, Long tableauId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de la colonne ne peut pas être vide");
        }
        Colonne colonne = Colonne.builder()
                .name(name)
                .position(position)
                .tableauId(tableauId)
                .build();
        return colonneRepository.save(colonne);
    }
}
