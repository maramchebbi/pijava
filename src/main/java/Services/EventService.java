package Services;

import Models.Event;
import Utils.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IService<Event> {
    Connection con;

    public EventService() {
        con = DataSource.getDataSource().getConnection();
    }

    @Override
    public void add(Event event) throws SQLException {
        if (event.getTitre() == null || event.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide.");
        }

        if (event.getLocalisation() == null || event.getLocalisation().trim().isEmpty()) {
            throw new IllegalArgumentException("La région ne peut pas être vide.");
        }

        if (event.getDate() == null) {
            throw new IllegalArgumentException("La date ne peut pas être vide.");
        }

        if (event.getDate().before(new java.sql.Date(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("La date ne peut pas être dans le passé.");
        }

        if (event.getNbParticipant() <= 0) {
            throw new IllegalArgumentException("Le nombre de participants doit être positif.");
        }

        String query = "INSERT INTO event (titre, localisation, date, heure, nb_participant, image) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, event.getTitre());
        ps.setString(2, event.getLocalisation());
        ps.setDate(3, event.getDate());
        ps.setTime(4, event.getHeure());
        ps.setInt(5, event.getNbParticipant());
        ps.setString(6, event.getImage());
        ps.executeUpdate();
    }

    public void add(Event event, int sponsorId) throws SQLException {
        if (event.getTitre() == null || event.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide.");
        }

        if (event.getLocalisation() == null || event.getLocalisation().trim().isEmpty()) {
            throw new IllegalArgumentException("La région ne peut pas être vide.");
        }

        if (event.getDate() == null) {
            throw new IllegalArgumentException("La date ne peut pas être vide.");
        }

        if (event.getDate().before(new java.sql.Date(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("La date ne peut pas être dans le passé.");
        }

        if (event.getNbParticipant() <= 0) {
            throw new IllegalArgumentException("Le nombre de participants doit être positif.");
        }

        // Modifier la requête pour inclure latitude et longitude
        String query = "INSERT INTO event (titre, localisation, date, heure, nb_participant, image, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, event.getTitre());
        ps.setString(2, event.getLocalisation());
        ps.setDate(3, event.getDate());
        ps.setTime(4, event.getHeure());
        ps.setInt(5, event.getNbParticipant());
        ps.setString(6, event.getImage());

        // Ajouter les paramètres pour latitude et longitude
        if (event.getLatitude() != null) {
            ps.setBigDecimal(7, event.getLatitude());
        } else {
            ps.setNull(7, java.sql.Types.DECIMAL);
        }

        if (event.getLongitude() != null) {
            ps.setBigDecimal(8, event.getLongitude());
        } else {
            ps.setNull(8, java.sql.Types.DECIMAL);
        }

        System.out.println("Requête SQL avec coordonnées - Latitude: " + event.getLatitude() + ", Longitude: " + event.getLongitude());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int eventId = rs.getInt(1);

            String relationQuery = "INSERT INTO sponsor_event (event_id, sponsor_id) VALUES (?, ?)";
            PreparedStatement psRelation = con.prepareStatement(relationQuery);
            psRelation.setInt(1, eventId);
            psRelation.setInt(2, sponsorId);
            psRelation.executeUpdate();
        }
    }


    @Override
    public void update(Event event) throws SQLException {
        String query = "UPDATE event SET titre = ?, localisation = ?, date = ?, heure = ?, nb_participant = ?, image = ? WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, event.getTitre());
        stmt.setString(2, event.getLocalisation());
        stmt.setDate(3, event.getDate());
        stmt.setTime(4, event.getHeure());
        stmt.setInt(5, event.getNbParticipant());
        stmt.setString(6, event.getImage());
        stmt.setInt(7, event.getId());

        stmt.executeUpdate();
    }

    @Override
    public void delete(Event event) throws SQLException {
        String query = "DELETE FROM event WHERE id = ?";
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setInt(1, event.getId());
        stmt.executeUpdate();
    }

    @Override
    public List<Event> getAll() throws SQLException {
        String query = "SELECT * FROM event";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        List<Event> events = new ArrayList<>();

        while (rs.next()) {
            Event event = new Event();
            event.setId(rs.getInt("id"));
            event.setTitre(rs.getString("titre"));
            event.setLocalisation(rs.getString("localisation"));
            event.setDate(rs.getDate("date"));
            event.setHeure(rs.getTime("heure"));
            event.setNbParticipant(rs.getInt("nb_participant"));
            event.setImage(rs.getString("image"));

            // Récupérer latitude et longitude
            event.setLatitude(rs.getBigDecimal("latitude"));
            event.setLongitude(rs.getBigDecimal("longitude"));

            // Débogage pour voir ce qui est chargé (maintenant à l'intérieur de la boucle)
            System.out.println("Event ID: " + event.getId());
            System.out.println("Chargement latitude: " + rs.getBigDecimal("latitude"));
            System.out.println("Chargement longitude: " + rs.getBigDecimal("longitude"));

            events.add(event);
        }

        return events;
    }


    public Event findById(int id) {
        String query = "SELECT * FROM event WHERE id = ?";

        try (Connection conn = DataSource.getDataSource().getConnection();
             PreparedStatement pst = conn.prepareStatement(query)) {

            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Event event = new Event();
                    event.setId(rs.getInt("id"));
                    event.setTitre(rs.getString("titre"));
                    event.setLocalisation(rs.getString("localisation"));
                    event.setDate(rs.getDate("date"));
                    event.setHeure(rs.getTime("heure"));
                    event.setNbParticipant(rs.getInt("nb_participant"));
                    event.setImage(rs.getString("image"));

                    // Récupérer latitude et longitude (maintenant que nous savons qu'elles existent)
                    event.setLatitude(rs.getBigDecimal("latitude"));
                    event.setLongitude(rs.getBigDecimal("longitude"));

                    // Débogage
                    System.out.println("Event ID: " + event.getId() + ", Latitude: " + event.getLatitude() + ", Longitude: " + event.getLongitude());

                    return event;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'événement avec ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Retourne null si aucun événement trouvé avec cet ID
    }

}
