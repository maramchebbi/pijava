package Services;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import Models.Participation;
import Models.Event;
import Models.User;
import Utils.DataSource;

public class ParticipationService {

    private Connection getConnection() throws SQLException {
        Connection conn = DataSource.getDataSource().getConnection();
        if (conn == null || conn.isClosed()) {
            System.out.println("Connexion échouée ou fermée !");
        } else {
            System.out.println("Connexion établie avec succès !");
        }
        return conn;
    }

    public void addParticipation(int userId, int eventId, String nom, String email, int numtel) throws SQLException {
        String maxQuery = "SELECT nb_participant FROM event WHERE id = ?";
        String countQuery = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND is_waiting = 0";
        String insertQuery = "INSERT INTO participation (user_id, event_id, nom_utilisateur, email_utilisateur, numtel, is_waiting, date_participation) VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection()) {
            int isWaiting = 0;
            int maxParticipants = 0;
            int currentParticipants = 0;

            try (PreparedStatement pstMax = conn.prepareStatement(maxQuery)) {
                pstMax.setInt(1, eventId);
                ResultSet rsMax = pstMax.executeQuery();
                if (rsMax.next()) maxParticipants = rsMax.getInt("nb_participant");
            }

            try (PreparedStatement pstCount = conn.prepareStatement(countQuery)) {
                pstCount.setInt(1, eventId);
                ResultSet rsCount = pstCount.executeQuery();
                if (rsCount.next()) currentParticipants = rsCount.getInt(1);
            }

            if (currentParticipants >= maxParticipants) isWaiting = 1;

            try (PreparedStatement pst = conn.prepareStatement(insertQuery)) {
                pst.setInt(1, userId);
                pst.setInt(2, eventId);
                pst.setString(3, nom);
                pst.setString(4, email);
                pst.setInt(5, numtel);
                pst.setInt(6, isWaiting);
                pst.executeUpdate();
            }
        }
    }

    /**
     * Méthode qui utilise SQL pour calculer la différence de temps
     * @param participationId L'ID de la participation
     * @return true si la participation peut être annulée, false sinon
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public boolean canCancelParticipation(int participationId) throws SQLException {
        // Cette requête calcule directement la différence en heures entre maintenant et l'événement
        String query = "SELECT " +
                "p.id, " +
                "e.titre, " +
                "e.date, " +
                "e.heure, " +
                "TIMESTAMPDIFF(HOUR, NOW(), CONCAT(e.date, ' ', IFNULL(e.heure, '12:00:00'))) AS hours_remaining " +
                "FROM participation p " +
                "JOIN event e ON p.event_id = e.id " +
                "WHERE p.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, participationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Récupérer directement le nombre d'heures restantes
                    int hoursRemaining = rs.getInt("hours_remaining");
                    String eventTitle = rs.getString("e.titre");
                    java.sql.Date eventDate = rs.getDate("e.date");
                    java.sql.Time eventTime = rs.getTime("e.heure");

                    System.out.println("=== Vérification d'annulation pour participation #" + participationId + " ===");
                    System.out.println("Événement: " + eventTitle);
                    System.out.println("Date: " + eventDate);
                    System.out.println("Heure: " + (eventTime != null ? eventTime : "Non spécifiée"));
                    System.out.println("Heures restantes avant l'événement: " + hoursRemaining);

                    // Vérifier si l'annulation est faite au moins 24h avant l'événement
                    boolean canCancel = hoursRemaining >= 24;
                    System.out.println("Annulation possible: " + canCancel + " (" + (canCancel ? "Plus" : "Moins") + " de 24h avant l'événement)");

                    return canCancel;
                } else {
                    System.out.println("Participation ID " + participationId + " non trouvée");
                    return false;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification du délai d'annulation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean deleteParticipation(int participationId) throws SQLException {
        // Vérifier si l'annulation est autorisée (24h avant l'événement)
        if (!canCancelParticipation(participationId)) {
            System.out.println("Annulation refusée - moins de 24h avant l'événement");
            return false; // Annulation refusée - moins de 24h avant l'événement
        }

        String getEventQuery = "SELECT event_id FROM participation WHERE id = ?";
        String deleteQuery = "DELETE FROM participation WHERE id = ?";

        try (Connection conn = getConnection()) {
            int eventId = -1;

            // Récupérer l'ID de l'événement
            try (PreparedStatement getEventStmt = conn.prepareStatement(getEventQuery)) {
                getEventStmt.setInt(1, participationId);
                try (ResultSet eventRs = getEventStmt.executeQuery()) {
                    if (eventRs.next()) {
                        eventId = eventRs.getInt("event_id");
                    }
                }
            }

            // Supprimer la participation
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, participationId);
                int rowsDeleted = deleteStmt.executeUpdate();
                System.out.println("Lignes supprimées: " + rowsDeleted);
            }

            // Si un événement est lié, gérer la liste d'attente
            if (eventId != -1) {
                handleWaitingList(eventId, conn);
            }

            return true; // Annulation réussie
        }
    }

    private void handleWaitingList(int eventId, Connection conn) throws SQLException {
        String findWaitingQuery = "SELECT p.*, e.titre FROM participation p " +
                "JOIN event e ON p.event_id = e.id " +
                "WHERE p.event_id = ? AND p.is_waiting = 1 " +
                "ORDER BY p.date_participation ASC LIMIT 1";

        try (PreparedStatement findWaitingStmt = conn.prepareStatement(findWaitingQuery)) {
            findWaitingStmt.setInt(1, eventId);

            try (ResultSet waitingRs = findWaitingStmt.executeQuery()) {
                if (waitingRs.next()) {
                    int waitingParticipationId = waitingRs.getInt("id");
                    String nom = waitingRs.getString("nom_utilisateur");
                    String email = waitingRs.getString("email_utilisateur");
                    String eventTitre = waitingRs.getString("titre");
                    int numtel = waitingRs.getInt("numtel");

                    // Mise à jour du statut de is_waiting de 1 à 0
                    String updateQuery = "UPDATE participation SET is_waiting = 0 WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, waitingParticipationId);
                        updateStmt.executeUpdate();
                    }

                    // Envoi du SMS à la personne qui n'est plus sur liste d'attente
                    TwilioService smsService = new TwilioService();
                    String msg = "Bonjour " + nom + ", une place s'est libérée pour l'événement '" + eventTitre + "'. Vous êtes maintenant inscrit.";
                    smsService.sendSms("+216" + numtel, msg);
                }
            } catch (SQLException e) {
                System.out.println("Erreur lors de l'exécution de la requête pour la liste d'attente");
                e.printStackTrace();
            }
        }
    }
    /**
     * Méthode corrigée pour récupérer les participations d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des participations de l'utilisateur
     */
    public List<Participation> getParticipationsByUser(int userId) {
        System.out.println("Récupération des participations pour l'utilisateur: " + userId);
        List<Participation> participations = new ArrayList<>();

        // Utiliser try-with-resources pour garantir la fermeture de la connexion
        try (Connection conn = DataSource.getDataSource().getConnection()) {
            // Vérifier si la connexion est valide
            if (conn == null || conn.isClosed()) {
                System.out.println("Connexion nulle ou fermée");
                return participations;
            }

            String query = "SELECT p.*, e.* FROM participation p " +
                    "JOIN event e ON p.event_id = e.id " +
                    "WHERE p.user_id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Participation participation = new Participation();
                        participation.setId(rs.getInt("p.id"));
                        participation.setUserId(rs.getInt("p.user_id"));
                        participation.setNomUtilisateur(rs.getString("p.nom_utilisateur"));
                        participation.setEmailUtilisateur(rs.getString("p.email_utilisateur"));
                        participation.setDateParticipation(rs.getDate("p.date_participation"));
                        participation.setIsWaiting(rs.getBoolean("p.is_waiting"));
                        participation.setQrCode(rs.getString("p.qr_code"));
                        participation.setNumtel(rs.getInt("p.numtel"));

                        // Créer et configurer l'événement associé
                        Event event = new Event();
                        event.setId(rs.getInt("e.id"));
                        event.setTitre(rs.getString("e.titre"));
                        event.setDate(rs.getDate("e.date"));
                        event.setHeure(rs.getTime("e.heure"));
                        event.setLocalisation(rs.getString("e.localisation"));
                        event.setNbParticipant(rs.getInt("e.nb_participant"));
                        event.setImage(rs.getString("e.image"));
                        event.setLatitude(rs.getBigDecimal("e.latitude"));
                        event.setLongitude(rs.getBigDecimal("e.longitude"));

                        participation.setEvent(event);
                        participations.add(participation);
                    }
                }
            }

            System.out.println("Nombre de participations trouvées: " + participations.size());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des participations: " + e.getMessage());
            e.printStackTrace();
        }

        return participations;
    }
    /**
     * Récupère une participation spécifique par son ID
     * @param participationId ID de la participation
     * @return La participation ou null si non trouvée
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public Participation getParticipationById(int participationId) throws SQLException {
        System.out.println("Récupération de la participation avec ID: " + participationId);
        Participation participation = null;
        String query = "SELECT p.*, e.* FROM participation p " +
                "JOIN event e ON p.event_id = e.id " +
                "WHERE p.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, participationId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                participation = new Participation();
                participation.setId(rs.getInt("p.id"));
                participation.setUserId(rs.getInt("p.user_id"));
                participation.setNomUtilisateur(rs.getString("p.nom_utilisateur"));
                participation.setEmailUtilisateur(rs.getString("p.email_utilisateur"));
                participation.setDateParticipation(rs.getDate("p.date_participation"));
                participation.setIsWaiting(rs.getBoolean("p.is_waiting"));
                participation.setQrCode(rs.getString("p.qr_code"));
                participation.setNumtel(rs.getInt("p.numtel"));

                // Créer et configurer l'événement associé
                Event event = new Event();
                event.setId(rs.getInt("e.id"));
                event.setTitre(rs.getString("e.titre"));
                event.setDate(rs.getDate("e.date"));
                event.setHeure(rs.getTime("e.heure"));
                event.setLocalisation(rs.getString("e.localisation"));
                event.setNbParticipant(rs.getInt("e.nb_participant"));
                event.setImage(rs.getString("e.image"));
                event.setLatitude(rs.getBigDecimal("e.latitude"));
                event.setLongitude(rs.getBigDecimal("e.longitude"));

                participation.setEvent(event);
                System.out.println("Participation trouvée avec ID: " + participation.getId());
            } else {
                System.out.println("Aucune participation trouvée avec ID: " + participationId);
            }
        }
        return participation;
    }

    /**
     * Récupère le nombre de participants pour un événement spécifique
     * @param eventId L'identifiant de l'événement
     * @return Le nombre de participants (hors liste d'attente)
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public int getParticipationCountByEvent(int eventId) throws SQLException {
        String query = "SELECT COUNT(*) FROM participation WHERE event_id = ? AND is_waiting = 0";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        }

        return count;
    }

    /**
     * Récupère le nombre total de participants (y compris liste d'attente) pour un événement spécifique
     * @param eventId L'identifiant de l'événement
     * @return Le nombre total de participants (y compris liste d'attente)
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public int getTotalParticipationCountByEvent(int eventId) throws SQLException {
        String query = "SELECT COUNT(*) FROM participation WHERE event_id = ?";
        int count = 0;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }
        }

        return count;
    }

    /**
     * Récupère toutes les participations
     * @return Liste de toutes les participations
     * @throws SQLException En cas d'erreur d'accès à la base de données
     */
    public List<Participation> getAllParticipations() throws SQLException {
        System.out.println("Récupération de toutes les participations");
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT p.*, e.* FROM participation p " +
                "JOIN event e ON p.event_id = e.id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Participation participation = new Participation();
                participation.setId(rs.getInt("p.id"));
                participation.setUserId(rs.getInt("p.user_id"));
                participation.setNomUtilisateur(rs.getString("p.nom_utilisateur"));
                participation.setEmailUtilisateur(rs.getString("p.email_utilisateur"));
                participation.setDateParticipation(rs.getDate("p.date_participation"));
                participation.setIsWaiting(rs.getBoolean("p.is_waiting"));
                participation.setQrCode(rs.getString("p.qr_code"));
                participation.setNumtel(rs.getInt("p.numtel"));

                // Créer et configurer l'événement associé
                Event event = new Event();
                event.setId(rs.getInt("e.id"));
                event.setTitre(rs.getString("e.titre"));
                event.setDate(rs.getDate("e.date"));
                event.setHeure(rs.getTime("e.heure"));
                event.setLocalisation(rs.getString("e.localisation"));
                event.setNbParticipant(rs.getInt("e.nb_participant"));
                event.setImage(rs.getString("e.image"));
                event.setLatitude(rs.getBigDecimal("e.latitude"));
                event.setLongitude(rs.getBigDecimal("e.longitude"));

                participation.setEvent(event);
                participations.add(participation);
            }

            System.out.println("Nombre total de participations: " + participations.size());
        }
        return participations;
    }
}