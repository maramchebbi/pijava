package Models;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime date;

    // Mix des deux approches
    private int userId;       // Temporaire (sera remplacé par User user plus tard)
    private Oeuvre oeuvre;    // Gardé comme objet

    // Constructeurs
    public Commentaire() {
        this.date = LocalDateTime.now();
        this.userId = 1; // Valeur par défaut temporaire
      //  this.userId = 2; // Valeur par défaut temporaire
    }

    public Commentaire(String contenu, int userId, Oeuvre oeuvre) {
        this.contenu = contenu;
        this.userId = userId;
        this.oeuvre = oeuvre;
        this.date = LocalDateTime.now();
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Oeuvre getOeuvre() {
        return oeuvre;
    }

    public void setOeuvre(Oeuvre oeuvre) {
        this.oeuvre = oeuvre;
    }

    // Méthode utilitaire pour oeuvreId
    public int getOeuvreId() {
        return oeuvre != null ? oeuvre.getId() : 0;
    }

    // Méthode toString()
    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", date=" + date +
                ", userId=" + userId +
                ", oeuvre=" + (oeuvre != null ? oeuvre.getId() : "null") +
                '}';
    }
}