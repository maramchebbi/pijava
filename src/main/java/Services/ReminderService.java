package Services;

import Models.Event;
import Models.Participation;
import Utils.DataSource;
import Utils.EmailSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean isRunning = false;

    // Démarrer le service de rappels
    public static void startReminderService() {
        if (!isRunning) {
            scheduler.scheduleAtFixedRate(ReminderService::checkUpcomingEvents, 0, 12, TimeUnit.HOURS);
            isRunning = true;
            System.out.println("Service de rappels démarré");
        }
    }

    // Arrêter le service de rappels
    public static void stopReminderService() {
        scheduler.shutdown();
        isRunning = false;
        System.out.println("Service de rappels arrêté");
    }

    // Vérifier les événements à venir et envoyer des rappels
    private static void checkUpcomingEvents() {
        try {
            List<Event> upcomingEvents = getUpcomingEvents();

            for (Event event : upcomingEvents) {
                List<Participation> participants = getParticipants(event.getId());

                for (Participation participant : participants) {
                    sendReminder(event, participant);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des événements à venir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Récupérer les événements qui auront lieu dans les 24 heures
    private static List<Event> getUpcomingEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        Connection conn = DataSource.getDataSource().getConnection();

        // Date actuelle + 24h
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        Date tomorrowDate = Date.from(tomorrow.atZone(ZoneId.systemDefault()).toInstant());

        String query = "SELECT * FROM event WHERE date = ? AND NOT reminded = 1";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setDate(1, new java.sql.Date(tomorrowDate.getTime()));

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Event event = new Event();
            event.setId(rs.getInt("id"));
            event.setTitre(rs.getString("titre"));
            event.setLocalisation(rs.getString("localisation"));
            event.setDate(rs.getDate("date"));
            event.setHeure(rs.getTime("heure"));
            // Autres champs...

            events.add(event);
        }

        return events;
    }

    // Récupérer les participants d'un événement
    private static List<Participation> getParticipants(int eventId) throws SQLException {
        List<Participation> participants = new ArrayList<>();
        Connection conn = DataSource.getDataSource().getConnection();

        String query = "SELECT * FROM participation WHERE event_id  = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Participation participation = new Participation();
            participation.setId(rs.getInt("id"));
            participation.setUserId(rs.getInt("user_id"));
            Event event = new Event();
            event.setId(rs.getInt("event_id"));
            participation.setEvent(event);
            participation.setNomUtilisateur(rs.getString("nom_utilisateur"));
            participation.setEmailUtilisateur(rs.getString("email_utilisateur"));

            participants.add(participation);
        }

        return participants;
    }

    // Dans la méthode sendReminder de ReminderService
    private static void sendReminder(Event event, Participation participant) {
        String subject = "Rappel: Événement " + event.getTitre() + " demain!";

        String message = "Bonjour " + participant.getNomUtilisateur() + ",\n\n" +
                "Nous vous rappelons que l'événement \"" + event.getTitre() + "\" aura lieu demain " +
                "à " + event.getHeure() + " à " + event.getLocalisation() + ".\n\n" +
                "Nous sommes impatients de vous y voir!\n\n" +
                "Cordialement,\n" +
                "L'équipe d'organisation";

        try {
            System.out.println("Tentative d'envoi de rappel à: " + participant.getEmailUtilisateur());
            EmailSender.sendEmail(participant.getEmailUtilisateur(), subject, message);

            // Marquer l'événement comme ayant eu ses rappels envoyés
            markEventReminded(event.getId());

            System.out.println("Rappel envoyé à " + participant.getNomUtilisateur() + " (" + participant.getEmailUtilisateur() + ") pour l'événement " + event.getTitre());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du rappel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Marquer un événement comme ayant eu ses rappels envoyés
    private static void markEventReminded(int eventId) throws SQLException {
        Connection conn = DataSource.getDataSource().getConnection();

        String query = "UPDATE event SET reminded = 1 WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, eventId);

        ps.executeUpdate();
    }
}