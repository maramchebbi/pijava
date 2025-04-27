package utils;

import java.util.regex.Pattern;

/**
 * Classe utilitaire pour la validation des entrées utilisateur
 */
public class ValidationUtils {

    // Regex pour valider un nom (lettres, espaces, tirets et apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '-]+$");

    // Regex pour valider un email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex pour valider un mot de passe fort (min 8 caractères, au moins 1 majuscule, 1 minuscule, 1 chiffre)
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$"
    );

    /**
     * Vérifie si le nom ou prénom est valide
     * @param name Le nom à valider
     * @return true si le nom est valide, false sinon
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Vérifie si l'email est valide
     * @param email L'email à valider
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Vérifie si le mot de passe est suffisamment fort
     * @param password Le mot de passe à valider
     * @return true si le mot de passe est fort, false sinon
     */
    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Vérifie si une chaîne est vide ou null
     * @param str La chaîne à vérifier
     * @return true si la chaîne est vide ou null, false sinon
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}