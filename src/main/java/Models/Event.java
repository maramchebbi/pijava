package Models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String titre;
    private String localisation;
    private Date date;
    private Time heure;
    private int nbParticipant;
    private String image;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<Sponsor> sponsors = new ArrayList<>();


    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", localisation='" + localisation + '\'' +
                ", date=" + date +
                ", heure=" + heure +
                ", nbParticipant=" + nbParticipant +
                ", image='" + image + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", sponsors=" + sponsors +
                '}';
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public Time getHeure() { return heure; }
    public void setHeure(Time heure) { this.heure = heure; }

    public int getNbParticipant() { return nbParticipant; }
    public void setNbParticipant(int nbParticipant) { this.nbParticipant = nbParticipant; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public List<Sponsor> getSponsors() { return sponsors; }
    public void setSponsors(List<Sponsor> sponsors) { this.sponsors = sponsors; }

}
