package Utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "votre_email@gmail.com"; // À remplacer par votre email
    private static final String SMTP_PASSWORD = "votre_mot_de_passe_app"; // À remplacer par votre mot de passe d'application

    public static void sendEmail(String recipientEmail, String subject, String content) {
        // Configuration des propriétés pour l'envoi d'email
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Créer une session avec authentification
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);

            // Envoyer l'email
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendWaitingListNotification(String recipientEmail, String recipientName, String eventTitle) {
        String subject = "Place disponible pour l'événement " + eventTitle;
        String content = "Bonjour " + recipientName + ",\n\n" +
                        "Une place s'est libérée pour l'événement \"" + eventTitle + "\".\n" +
                        "Votre participation a été confirmée. Vous n'êtes plus en liste d'attente.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe de gestion des événements";

        sendEmail(recipientEmail, subject, content);
    }
} 