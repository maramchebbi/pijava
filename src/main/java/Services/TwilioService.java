package Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Service pour l'envoi de SMS via l'API Twilio
 */
public class TwilioService {
    // Credentials Twilio (à sécuriser dans un fichier de configuration en production)
    public static final String ACCOUNT_SID = "AC391be1b72c82cf26b691b727579de1ba";
    public static final String AUTH_TOKEN = "da627b269950df2792c5efe2ff99ed69";
    public static final String FROM_PHONE = "+18163276480"; // Numéro Twilio validé

    /**
     * Constructeur qui initialise la connexion à Twilio
     */
    public TwilioService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un SMS à un numéro de téléphone spécifié
     * @param to Le numéro de téléphone du destinataire (format international, ex: +21699999999)
     * @param messageBody Le contenu du message à envoyer
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendSms(String to, String messageBody) {
        try {
            System.out.println("Envoi de SMS à " + to + ": " + messageBody);
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(FROM_PHONE),
                    messageBody
            ).create();

            // Vérifier le statut du message
            String status = message.getStatus().toString();
            System.out.println("SMS envoyé avec le statut: " + status);

            // On considère l'envoi réussi si le statut est QUEUED ou SENT
            return status.equals("QUEUED") || status.equals("SENT") || status.equals("DELIVERED");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie un SMS de notification pour les événements à venir
     * @param to Numéro de téléphone du participant
     * @param userName Nom du participant
     * @param eventTitle Titre de l'événement
     * @param eventDateTimeStr Date et heure de l'événement en format texte
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendEventReminder(String to, String userName, String eventTitle, String eventDateTimeStr) {
        String messageBody = "Bonjour " + userName + ", " +
                "Nous vous rappelons que l'événement '" + eventTitle + "' " +
                "aura lieu le " + eventDateTimeStr + ". " +
                "Nous vous attendons!";

        return sendSms(to, messageBody);
    }

    /**
     * Envoie un SMS pour notifier qu'une place s'est libérée
     * @param to Numéro de téléphone du participant
     * @param userName Nom du participant
     * @param eventTitle Titre de l'événement
     * @return true si l'envoi a réussi, false sinon
     */
    public boolean sendWaitingListNotification(String to, String userName, String eventTitle) {
        String messageBody = "Bonjour " + userName + ", " +
                "une place s'est libérée pour l'événement '" + eventTitle + "'. " +
                "Vous êtes maintenant inscrit.";

        return sendSms(to, messageBody);
    }
}