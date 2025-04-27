package Services;

public class NotificationService {

    public void sendWaitingConfirmation(String email, String eventTitle, String qrCodePath) {
        System.out.println("✉️ Email envoyé à : " + email);
        System.out.println("Sujet : Confirmation d'inscription à " + eventTitle);
        System.out.println("QR Code attaché : " + qrCodePath);
        // Pour un vrai envoi d'email, utiliser JavaMail.
    }
}
