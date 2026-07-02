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
}
