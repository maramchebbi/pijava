package Services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    public static void sendEmail(String to, String subject, String body) {
       /* final String from = "marwen.jnen@gmail.com";  // Ton email
        final String password = "rcjdfeflyxntyjpi";    // Ton mot de passe*/

        final String from = "chebbimaram0@gmail.com";
        final String password = "hlsbbpyoruhcweaf";

        // Paramètres de configuration du serveur SMTP
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465"); // Port SSL
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        try {
            // Créer la session AVEC authentificateur
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });

            // Créer le message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            // Envoyer le message
            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès !");
        } catch (MessagingException e) {
            System.out.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}