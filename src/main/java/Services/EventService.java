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

        String query = "INSERT INTO event (titre, localisation, date, heure, nb_participant, image) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, event.getTitre());
        ps.setString(2, event.getLocalisation());
        ps.setDate(3, event.getDate());
        ps.setTime(4, event.getHeure());
        ps.setInt(5, event.getNbParticipant());
        ps.setString(6, event.getImage());
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
            events.add(event);
        }

        return events;
    }
}
