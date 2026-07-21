package fr.esgi.fx.kanban.viewmodel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Fonctions d'affichage partagées par les servlets pour transformer les objets du
 * modèle en informations présentables (initiales, couleurs d'avatar, libellé de type,
 * date formatée). Regroupé ici pour éviter la duplication entre servlets.
 */
public final class VueSupport {

    private VueSupport() {}

    /** Palette du design system, réutilisée pour les avatars et la barre des tableaux. */
    private static final List<String> PALETTE = List.of(
            "#378ADD", "#1D9E75", "#BA7517", "#E24B4A", "#635BFF", "#475569");

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.FRENCH);

    /** "jean.d" -> "JD", "alice" -> "A", null/vide -> "?". */
    public static String initiales(String pseudo) {
        if (pseudo == null || pseudo.isBlank()) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : pseudo.split("[.\\s_-]+")) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "?" : sb.toString();
    }

    /** Couleur d'avatar déterministe (même pseudo -> même couleur). */
    public static String couleurAvatar(String pseudo) {
        return couleurDepuis(pseudo == null ? "" : pseudo);
    }

    /** Couleur de la barre supérieure d'un tableau, déterministe à partir de son id. */
    public static String couleurTableau(Long tableauId) {
        long id = tableauId == null ? 0L : tableauId;
        return PALETTE.get((int) (Math.floorMod(id, PALETTE.size())));
    }

    private static String couleurDepuis(String seed) {
        int index = Math.floorMod(seed.hashCode(), PALETTE.size());
        return PALETTE.get(index);
    }

    /**
     * Suffixe de classe CSS pour le badge de type de tâche, dérivé de l'id de type
     * (voir import.sql : 1 Standard, 2 Bug, 3 Spike, 4 Amélioration).
     */
    public static String typeClasse(Long typeId) {
        if (typeId == null) {
            return "standard";
        }
        return switch (typeId.intValue()) {
            case 2 -> "bug";
            case 3 -> "spike";
            case 4 -> "amelio";
            default -> "standard";
        };
    }

    /** Opération inverse de {@link #typeClasse(Long)} : classe -> id (voir import.sql). */
    public static Long typeIdDepuisClasse(String classe) {
        if (classe == null) {
            return 1L;
        }
        return switch (classe) {
            case "bug" -> 2L;
            case "spike" -> 3L;
            case "amelio" -> 4L;
            default -> 1L;
        };
    }

    /** Date formatée pour l'affichage (ex. "2 juil. 09:12"), chaîne vide si null. */
    public static String formatDate(LocalDateTime date) {
        return date == null ? "" : date.format(DATE_FMT);
    }

    /** Taille de fichier formatée pour l'affichage (ex. "128 Ko", "1,4 Mo"). */
    public static String formatTaille(long octets) {
        if (octets < 1024) {
            return octets + " o";
        }
        if (octets < 1024 * 1024) {
            return String.format(Locale.FRENCH, "%.0f Ko", octets / 1024.0);
        }
        return String.format(Locale.FRENCH, "%.1f Mo", octets / (1024.0 * 1024));
    }
}
