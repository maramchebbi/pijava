package Main;

import Models.Event;
import Services.EventService;

import java.sql.Date;
import java.sql.Time;
import java.math.BigDecimal;
import java.sql.SQLException;

public class Test {
    public static void main(String[] args) {
        EventService es = new EventService();
        try {
            Event newEvent = new Event();
            newEvent.setTitre("Concert Jazz");
            newEvent.setLocalisation("Tunis");
            newEvent.setDate(Date.valueOf("2020-06-20"));
            newEvent.setHeure(Time.valueOf("20:00:00"));
            newEvent.setNbParticipant(150);
            newEvent.setImage("concert.jpg");
            newEvent.setLatitude(new BigDecimal("36.806389"));
            newEvent.setLongitude(new BigDecimal("10.181667"));

            // Ajout
            es.add(newEvent);
            System.out.println("✅ Événement ajouté.");
            System.out.println(es.getAll());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
