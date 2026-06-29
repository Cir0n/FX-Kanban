package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Commentaire;
import fr.esgi.fx.kanban.repository.ICommentaireRepository;
import fr.esgi.fx.kanban.service.ICommentaireService;

import java.util.List;

public class CommentaireServiceImpl implements ICommentaireService {

    private final ICommentaireRepository commentaireRepository;

    public CommentaireServiceImpl(ICommentaireRepository commentaireRepository) {
        this.commentaireRepository = commentaireRepository;
    }

    @Override
    public Commentaire ajouter(String content, Long tacheId, Long utilisateurId) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Le commentaire ne peut pas être vide");
        }
        Commentaire commentaire = Commentaire.builder()
                .content(content)
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .build();
        return commentaireRepository.save(commentaire);
    }

    @Override
    public List<Commentaire> findByTacheId(Long tacheId) {
        return commentaireRepository.findByTacheId(tacheId);
    }
}
