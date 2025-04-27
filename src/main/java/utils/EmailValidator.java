package utils;

import java.util.regex.Pattern;

/**
 * Utilitaire pour valider les adresses email
 */
public class EmailValidator {

    // Expression régulière pour validation basique d'email
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    /**
     * Valide si une chaîne est une adresse email valide
     * @param email L'adresse email à valider
     * @return true si l'email est valide, false sinon
     */
    public static boolean isValid(String email) {
        if (email == null) {
            return false;
        }
        return pattern.matcher(email).matches();
    }
}