// EmailService.java
package service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class EmailService {

    private static final String FROM_EMAIL = "chebbimaram0@gmail.com"; // Ton email Gmail
    private static final String APP_PASSWORD = "hlsbbpyoruhcweaf"; // Mot de passe d'application Gmail

    // Méthode pour envoyer l'ancien mot de passe
    public static void sendOldPassword(String toEmail) {
        // Générer un mot de passe temporaire
        String newPassword = generateRandomPassword(10);
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        // Mettre à jour le mot de passe dans la base
        if (updatePasswordInDB(toEmail, hashedNewPassword)) {
            // Configuration de l'envoi d'email
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
         //   props.put("mail.smtp.port", "587");
//
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("Réinitialisation de votre mot de passe");
                message.setText("Voici votre nouveau mot de passe temporaire : " + newPassword);

                Transport.send(message);
                System.out.println("Email envoyé à " + toEmail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Échec de la mise à jour du mot de passe.");
        }
    }

    // Méthode pour récupérer le mot de passe haché depuis la base de données
    public static String getOldPasswordFromDB(String email) {
        String url = "jdbc:mysql://localhost:3306/projetpi";
        String user = "root";
        String password = "";

        String sql = "SELECT password FROM user WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("password");  // Retourne le hachage du mot de passe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // Si aucun mot de passe n'est trouvé
    }

    // Récupère le mot de passe de l'utilisateur (peut être via un formulaire)
    public static String getOriginalPasswordFromUser() {
        // Cette méthode peut être utilisée pour récupérer le mot de passe fourni par l'utilisateur
        return "motDePasseOriginal";  // Remplacer par la logique d'entrée du mot de passe utilisateur
    }
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int)(Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
    public static boolean updatePasswordInDB(String email, String hashedPassword) {
        String url = "jdbc:mysql://localhost:3306/projetpi";
        String user = "root";
        String password = "";

        String sql = "UPDATE user SET password = ? WHERE email = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
