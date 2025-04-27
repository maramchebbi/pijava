package utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Service professionnel d'envoi d'emails pour l'application Artisfera.
 * Cette classe fournit des méthodes pour envoyer différents types d'emails
 * avec un formatage HTML professionnel.
 */
public class EmailSender {
    // Configuration du logger
    private static final Logger LOGGER = Logger.getLogger(EmailSender.class.getName());
    private static boolean loggerInitialized = false;

    // Configuration email
    private static final String SENDER_EMAIL = "chebbimaram0@gmail.com";
    private static final String SENDER_PASSWORD = "hlsbbpyoruhcweaf";
    private static final String SENDER_NAME = "Artisfera";

    // Couleurs de la charte graphique
    private static final String PRIMARY_COLOR = "#4f46e5";    // Bleu indigo
    private static final String SECONDARY_COLOR = "#10b981";  // Vert émeraude
    private static final String DARK_COLOR = "#1e293b";       // Bleu très foncé
    private static final String LIGHT_COLOR = "#f8fafc";      // Gris très clair

    // Initialisation du logger
    static {
        try {
            if (!loggerInitialized) {
                FileHandler fileHandler = new FileHandler("email_logs.log", true);
                fileHandler.setFormatter(new SimpleFormatter());
                LOGGER.addHandler(fileHandler);
                LOGGER.setLevel(Level.ALL);
                loggerInitialized = true;
            }
        } catch (IOException e) {
            System.err.println("Impossible d'initialiser le logger: " + e.getMessage());
        }
    }

    /**
     * Configure la session email avec les paramètres SMTP appropriés
     * @return La session configurée
     */
    private static Session configureSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }

    /**
     * Envoie une notification de connexion à l'utilisateur
     * @param toEmail L'email du destinataire
     * @param prenom Le prénom de l'utilisateur
     * @throws MessagingException En cas d'erreur d'envoi
     */
    public static void sendLoginNotification(String toEmail, String prenom) {
        LOGGER.info("Préparation de l'envoi d'une notification de connexion à " + toEmail);

        try {
            // Création de la session
            Session session = configureSession();

            // Préparation du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Nouvelle connexion à votre compte Artisfera");

            // Date et heure formatées
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));

            // Corps HTML du message
            String htmlContent = createLoginEmailTemplate(prenom, dateTime);

            // Configuration du contenu HTML
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);
            LOGGER.info("Email de notification de connexion envoyé avec succès à " + toEmail);

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email de notification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'envoi de l'email de notification", e);
        }
    }

    /**
     * Envoie un email de vérification avec un code
     * @param toEmail L'email du destinataire
     * @param verificationCode Le code de vérification
     * @throws MessagingException En cas d'erreur d'envoi
     */
    public static void sendVerificationEmail(String toEmail, String verificationCode) {
        LOGGER.info("Préparation de l'envoi d'un email de vérification à " + toEmail);

        try {
            // Création de la session
            Session session = configureSession();

            // Préparation du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Vérification de votre compte Artisfera");

            // Corps HTML du message
            String htmlContent = createVerificationEmailTemplate(verificationCode);

            // Configuration du contenu HTML
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);
            LOGGER.info("Email de vérification envoyé avec succès à " + toEmail);

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'envoi de l'email de vérification", e);
        }
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     * @param toEmail L'email du destinataire
     * @param resetCode Le code de réinitialisation
     * @param prenom Le prénom de l'utilisateur
     */
    public static void sendPasswordResetEmail(String toEmail, String resetCode, String prenom) {
        LOGGER.info("Préparation de l'envoi d'un email de réinitialisation de mot de passe à " + toEmail);

        try {
            // Création de la session
            Session session = configureSession();

            // Préparation du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Réinitialisation de votre mot de passe Artisfera");

            // Corps HTML du message
            String htmlContent = createPasswordResetTemplate(prenom, resetCode);

            // Configuration du contenu HTML
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);
            LOGGER.info("Email de réinitialisation de mot de passe envoyé avec succès à " + toEmail);

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'envoi de l'email de réinitialisation", e);
        }
    }

    /**
     * Envoie un email de bienvenue après l'inscription
     * @param toEmail L'email du destinataire
     * @param prenom Le prénom de l'utilisateur
     */
    public static void sendWelcomeEmail(String toEmail, String prenom) {
        LOGGER.info("Préparation de l'envoi d'un email de bienvenue à " + toEmail);

        try {
            // Création de la session
            Session session = configureSession();

            // Préparation du message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Bienvenue sur Artisfera !");

            // Corps HTML du message
            String htmlContent = createWelcomeEmailTemplate(prenom);

            // Configuration du contenu HTML
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html; charset=utf-8");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            message.setContent(multipart);

            // Envoi du message
            Transport.send(message);
            LOGGER.info("Email de bienvenue envoyé avec succès à " + toEmail);

        } catch (Exception e) {
            LOGGER.severe("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'envoi de l'email de bienvenue", e);
        }
    }

    /**
     * Crée le template HTML pour l'email de notification de connexion
     * @param prenom Le prénom de l'utilisateur
     * @param dateTime La date et l'heure de connexion
     * @return Le contenu HTML formaté
     */
    private static String createLoginEmailTemplate(String prenom, String dateTime) {
        return "<!DOCTYPE html>"
                + "<html lang='fr'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <title>Nouvelle connexion</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f8fafc;'>"
                + "    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "        <div style='background-color: " + PRIMARY_COLOR + "; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>"
                + "            <h1 style='color: white; margin: 0;'>Artisfera</h1>"
                + "        </div>"
                + "        <div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>"
                + "            <h2 style='color: " + DARK_COLOR + "; margin-top: 0;'>Nouvelle connexion détectée</h2>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Bonjour <strong>" + prenom + "</strong>,</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Nous avons détecté une nouvelle connexion à votre compte Artisfera.</p>"
                + "            <div style='background-color: " + LIGHT_COLOR + "; border-left: 4px solid " + PRIMARY_COLOR + "; padding: 15px; margin: 20px 0;'>"
                + "                <p style='margin: 0; color: " + DARK_COLOR + ";'><strong>Date et heure :</strong> " + dateTime + "</p>"
                + "            </div>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Si vous n'êtes pas à l'origine de cette connexion, nous vous conseillons de changer immédiatement votre mot de passe et de contacter notre support.</p>"
                + "            <div style='text-align: center; margin-top: 30px;'>"
                + "                <a href='#' style='background-color: " + SECONDARY_COLOR + "; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;'>Changer mon mot de passe</a>"
                + "            </div>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px; margin-top: 30px;'>Si tout vous semble normal, vous pouvez ignorer cet email.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Cordialement,<br>L'équipe Artisfera</p>"
                + "        </div>"
                + "        <div style='text-align: center; padding: 20px; color: #64748b; font-size: 12px;'>"
                + "            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>"
                + "            <p>&copy; " + LocalDateTime.now().getYear() + " Artisfera. Tous droits réservés.</p>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }

    /**
     * Crée le template HTML pour l'email de vérification
     * @param verificationCode Le code de vérification
     * @return Le contenu HTML formaté
     */
    private static String createVerificationEmailTemplate(String verificationCode) {
        return "<!DOCTYPE html>"
                + "<html lang='fr'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <title>Vérification de compte</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f8fafc;'>"
                + "    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "        <div style='background-color: " + PRIMARY_COLOR + "; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>"
                + "            <h1 style='color: white; margin: 0;'>Artisfera</h1>"
                + "        </div>"
                + "        <div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>"
                + "            <h2 style='color: " + DARK_COLOR + "; margin-top: 0;'>Vérification de votre compte</h2>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Merci de vous être inscrit sur Artisfera !</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Pour activer votre compte, veuillez utiliser le code de vérification ci-dessous :</p>"
                + "            <div style='background-color: " + LIGHT_COLOR + "; text-align: center; padding: 20px; margin: 25px 0; border-radius: 4px; border: 1px dashed " + PRIMARY_COLOR + ";'>"
                + "                <p style='font-size: 32px; letter-spacing: 5px; font-weight: bold; margin: 0; color: " + PRIMARY_COLOR + ";'>" + verificationCode + "</p>"
                + "            </div>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Ce code est valable pendant 30 minutes. Veuillez l'entrer dans l'application pour finaliser la création de votre compte.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Si vous n'avez pas demandé ce code, vous pouvez ignorer cet email.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Cordialement,<br>L'équipe Artisfera</p>"
                + "        </div>"
                + "        <div style='text-align: center; padding: 20px; color: #64748b; font-size: 12px;'>"
                + "            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>"
                + "            <p>&copy; " + LocalDateTime.now().getYear() + " Artisfera. Tous droits réservés.</p>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }

    /**
     * Crée le template HTML pour l'email de réinitialisation de mot de passe
     * @param prenom Le prénom de l'utilisateur
     * @param resetCode Le code de réinitialisation
     * @return Le contenu HTML formaté
     */
    private static String createPasswordResetTemplate(String prenom, String resetCode) {
        return "<!DOCTYPE html>"
                + "<html lang='fr'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <title>Réinitialisation de mot de passe</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f8fafc;'>"
                + "    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "        <div style='background-color: " + PRIMARY_COLOR + "; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>"
                + "            <h1 style='color: white; margin: 0;'>Artisfera</h1>"
                + "        </div>"
                + "        <div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>"
                + "            <h2 style='color: " + DARK_COLOR + "; margin-top: 0;'>Réinitialisation de votre mot de passe</h2>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Bonjour <strong>" + prenom + "</strong>,</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte Artisfera.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Voici votre code de réinitialisation :</p>"
                + "            <div style='background-color: " + LIGHT_COLOR + "; text-align: center; padding: 20px; margin: 25px 0; border-radius: 4px; border: 1px dashed " + PRIMARY_COLOR + ";'>"
                + "                <p style='font-size: 32px; letter-spacing: 5px; font-weight: bold; margin: 0; color: " + PRIMARY_COLOR + ";'>" + resetCode + "</p>"
                + "            </div>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Ce code est valable pendant 15 minutes. Veuillez l'utiliser pour définir un nouveau mot de passe.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Si vous n'avez pas demandé cette réinitialisation, veuillez sécuriser votre compte en changeant immédiatement votre mot de passe.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Cordialement,<br>L'équipe Artisfera</p>"
                + "        </div>"
                + "        <div style='text-align: center; padding: 20px; color: #64748b; font-size: 12px;'>"
                + "            <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>"
                + "            <p>&copy; " + LocalDateTime.now().getYear() + " Artisfera. Tous droits réservés.</p>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }

    /**
     * Crée le template HTML pour l'email de bienvenue
     * @param prenom Le prénom de l'utilisateur
     * @return Le contenu HTML formaté
     */
    private static String createWelcomeEmailTemplate(String prenom) {
        return "<!DOCTYPE html>"
                + "<html lang='fr'>"
                + "<head>"
                + "    <meta charset='UTF-8'>"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "    <title>Bienvenue sur Artisfera</title>"
                + "</head>"
                + "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f8fafc;'>"
                + "    <div style='max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "        <div style='background-color: " + PRIMARY_COLOR + "; padding: 20px; text-align: center; border-radius: 8px 8px 0 0;'>"
                + "            <h1 style='color: white; margin: 0;'>Artisfera</h1>"
                + "        </div>"
                + "        <div style='background-color: white; padding: 30px; border-radius: 0 0 8px 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>"
                + "            <h2 style='color: " + DARK_COLOR + "; margin-top: 0;'>Bienvenue dans la communauté Artisfera !</h2>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Bonjour <strong>" + prenom + "</strong>,</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Nous sommes ravis de vous accueillir sur Artisfera, la plateforme dédiée aux artisans et créateurs d'art.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Votre compte est maintenant actif, et vous pouvez commencer à explorer toutes les fonctionnalités :</p>"
                + "            <ul style='color: " + DARK_COLOR + "; font-size: 16px;'>"
                + "                <li>Découvrez les créations d'autres artisans</li>"
                + "                <li>Partagez vos propres œuvres</li>"
                + "                <li>Rejoignez une communauté passionnée</li>"
                + "                <li>Participez à des événements exclusifs</li>"
                + "            </ul>"
                + "            <div style='text-align: center; margin: 30px 0;'>"
                + "                <a href='#' style='background-color: " + SECONDARY_COLOR + "; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; display: inline-block;'>Découvrir Artisfera</a>"
                + "            </div>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Si vous avez des questions, n'hésitez pas à contacter notre équipe de support.</p>"
                + "            <p style='color: " + DARK_COLOR + "; font-size: 16px;'>Cordialement,<br>L'équipe Artisfera</p>"
                + "        </div>"
                + "        <div style='text-align: center; padding: 20px; color: #64748b; font-size: 12px;'>"
                + "            <p>Suivez-nous sur nos réseaux sociaux :</p>"
                + "            <p>Instagram • Facebook • Twitter</p>"
                + "            <p>&copy; " + LocalDateTime.now().getYear() + " Artisfera. Tous droits réservés.</p>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }
}