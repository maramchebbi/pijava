package Utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String SENDER_EMAIL = "chebbimaram0@gmail.com";
    private static final String SENDER_PASSWORD = "hlsbbpyoruhcweaf";

    public static boolean sendEmail(String to, String subject, String content) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean notifyNewTextile(String email, String textileName, String collectionName, String creator) {
        String subject = "Nouveau textile ajouté: " + textileName;

        String content = "<html><body style='font-family: Arial, sans-serif;'>"
                + "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>"
                + "<h2 style='color: #603813; text-align: center;'>Nouveau textile ajouté</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Un nouveau textile a été ajouté à notre collection :</p>"
                + "<ul>"
                + "<li><strong>Nom du textile:</strong> " + textileName + "</li>"
                + "<li><strong>Collection:</strong> " + collectionName + "</li>"
                + "<li><strong>Ajouté par:</strong> " + creator + "</li>"
                + "</ul>"
                + "<p>Cordialement,<br>L'équipe de Gestion des Textiles</p>"
                + "</div></body></html>";

        return sendEmail(email, subject, content);
    }
}