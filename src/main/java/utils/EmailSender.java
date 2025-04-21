package utils;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import jakarta.mail.*;

public class EmailSender {

    public static void sendLoginNotification(String toEmail, String prenom) {
        final String fromEmail = "chebbimaram0@gmail.com"; // remplace par ton adresse Gmail
        final String password = "hlsbbpyoruhcweaf"; // mot de passe d'application

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        jakarta.mail.Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Connexion à Artisfera");
            message.setText("Bonjour " + prenom + ",\n\nUne connexion a été effectuée à votre compte Artisfera.\n\nSi ce n'était pas vous, veuillez changer votre mot de passe.\n\n- L'équipe Artisfera");

            Transport.send(message);
            System.out.println("Email de notification envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void sendVerificationEmail(String toEmail, String verificationCode) {
        final String fromEmail = "chebbimaram0@gmail.com";
        final String password = "hlsbbpyoruhcweaf";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Vérification de votre compte Artisfera");

            String emailContent = "Bonjour,\n\n"
                    + "Merci de vous être inscrit sur Artisfera.\n\n"
                    + "Votre code de vérification est : " + verificationCode + "\n\n"
                    + "Veuillez entrer ce code dans l'application pour activer votre compte.\n\n"
                    + "Cordialement,\nL'équipe Artisfera";

            message.setText(emailContent);

            Transport.send(message);
            System.out.println("Email de vérification envoyé à " + toEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email de vérification");
        }
    }

}
