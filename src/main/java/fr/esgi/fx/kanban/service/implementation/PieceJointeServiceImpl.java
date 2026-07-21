package fr.esgi.fx.kanban.service.implementation;

import fr.esgi.fx.kanban.model.Action;
import fr.esgi.fx.kanban.model.PieceJointe;
import fr.esgi.fx.kanban.repository.IActionRepository;
import fr.esgi.fx.kanban.repository.IPieceJointeRepository;
import fr.esgi.fx.kanban.service.IPieceJointeService;

import java.util.List;

public class PieceJointeServiceImpl implements IPieceJointeService {

    private static final long TAILLE_MAX_OCTETS = 10L * 1024 * 1024;

    private final IPieceJointeRepository pieceJointeRepository;
    private final IActionRepository actionRepository;

    public PieceJointeServiceImpl(IPieceJointeRepository pieceJointeRepository, IActionRepository actionRepository) {
        this.pieceJointeRepository = pieceJointeRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    public PieceJointe ajouter(String nomFichier, String mimeType, byte[] contenu, Long tacheId, Long utilisateurId) {
        if (nomFichier == null || nomFichier.isBlank()) {
            throw new IllegalArgumentException("Le nom du fichier est requis");
        }
        if (contenu == null || contenu.length == 0) {
            throw new IllegalArgumentException("Veuillez sélectionner un fichier");
        }
        if (contenu.length > TAILLE_MAX_OCTETS) {
            throw new IllegalArgumentException("Le fichier dépasse la taille maximale autorisée (10 Mo)");
        }

        PieceJointe pieceJointe = PieceJointe.builder()
                .nomFichier(nomFichier)
                .mimeType(mimeType == null || mimeType.isBlank() ? "application/octet-stream" : mimeType)
                .contenu(contenu)
                .tacheId(tacheId)
                .build();
        PieceJointe saved = pieceJointeRepository.save(pieceJointe);

        actionRepository.save(Action.builder()
                .description("Ajout de la pièce jointe " + nomFichier)
                .tacheId(tacheId)
                .utilisateurId(utilisateurId)
                .build());

        return saved;
    }

    @Override
    public List<PieceJointe> findByTacheId(Long tacheId) {
        return pieceJointeRepository.findByTacheId(tacheId);
    }

    @Override
    public PieceJointe findById(Long id) {
        return pieceJointeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pièce jointe introuvable"));
    }

    @Override
    public void supprimer(Long id, Long utilisateurId) {
        PieceJointe pieceJointe = findById(id);
        pieceJointeRepository.delete(id);

        actionRepository.save(Action.builder()
                .description("Suppression de la pièce jointe " + pieceJointe.getNomFichier())
                .tacheId(pieceJointe.getTacheId())
                .utilisateurId(utilisateurId)
                .build());
    }
}
