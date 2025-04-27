package Controllers;

import Models.Participation;
import Models.Event;
import Models.User;
import Services.*;

import java.sql.SQLException;
import java.util.List;

public class ParticipationController {

    private ParticipationService participationService;
    private EventService eventService;
    private TwilioService twilioService;

    public ParticipationController() throws SQLException {
        this.participationService = new ParticipationService();
        this.eventService = new EventService();
        this.twilioService = new TwilioService();
    }

    public Participation createParticipation(int eventId, User user, int numtel) {
        try {
            Event event = eventService.findById(eventId);
            if (event == null) {
                return null;
            }

            participationService.addParticipation(
                    user.getId(),
                    eventId,
                    user.getNom(),
                    user.getEmail(),
                    numtel
            );

            List<Participation> participations = participationService.getParticipationsByUser(user.getId());
            if (!participations.isEmpty()) {
                return participations.get(participations.size() - 1);
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Participation getParticipation(int id) {
        try {
            List<Participation> participations = participationService.getParticipationsByUser(id);
            if (!participations.isEmpty()) {
                return participations.get(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Supprime une participation si c'est possible (24h avant l'événement)
     * @param id ID de la participation à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteParticipation(int id) {
        try {
            System.out.println("Tentative de suppression de la participation: " + id);

            // Vérification si la participation peut être annulée
            if (!participationService.canCancelParticipation(id)) {
                // On ne fait plus l'envoi de SMS, juste un log
                System.out.println("Annulation impossible - moins de 24h avant l'événement");
                return false;
            }

            // Si l'annulation est possible, on procède
            boolean result = participationService.deleteParticipation(id);
            System.out.println("Résultat de la suppression: " + (result ? "Succès" : "Échec"));
            return result;
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la participation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupère une participation spécifique par son ID
     * @param participationId ID de la participation
     * @return La participation ou null si non trouvée
     */
    public Participation getParticipationById(int participationId) {
        try {
            return participationService.getParticipationById(participationId);
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération de la participation: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Participation> getUserParticipations(User user) {
        try {
            if (user == null) {
                System.out.println("Utilisateur null, impossible de récupérer les participations");
                return null;
            }
            System.out.println("Récupération des participations pour l'utilisateur: " + user.getNom() + " (ID: " + user.getId() + ")");
            return participationService.getParticipationsByUser(user.getId());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des participations: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean verifyTicket(int id, String token) {
        try {
            List<Participation> participations = participationService.getParticipationsByUser(id);
            if (!participations.isEmpty()) {
                Participation participation = participations.get(0);
                String generatedToken = generateToken(participation);
                return generatedToken.equals(token);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateToken(Participation participation) {
        String data = participation.getId() + "_" + participation.getEvent().getId() + "_" + participation.getDateParticipation();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}