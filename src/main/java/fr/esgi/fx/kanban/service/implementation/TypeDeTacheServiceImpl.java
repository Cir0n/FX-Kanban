package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.TypeDeTache;
import fr.esgi.fx.kanban.repository.ITypeDeTacheRepository;
import fr.esgi.fx.kanban.service.ITypeDeTacheService;

import java.util.List;
import java.util.Optional;

public class TypeDeTacheServiceImpl implements ITypeDeTacheService {

    private final ITypeDeTacheRepository typeDeTacheRepository;

    public TypeDeTacheServiceImpl(ITypeDeTacheRepository typeDeTacheRepository) {
        this.typeDeTacheRepository = typeDeTacheRepository;
    }

    @Override
    public List<TypeDeTache> findAll() {
        return typeDeTacheRepository.findAll();
    }

    @Override
    public Optional<TypeDeTache> findById(Long id) {
        return typeDeTacheRepository.findById(id);
    }
}
