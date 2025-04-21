package Models;

import java.time.LocalDateTime;

public class Commentaire {
    private int id;
    private String contenu;
    private LocalDateTime date;
    private int userId;       // Clé étrangère vers User
    private int oeuvreId;     // Clé étrangère vers Oeuvre

    // Constructeurs
    public Commentaire() {
        this.date = LocalDateTime.now();
    }

    public Commentaire(String contenu, int userId, int oeuvreId) {
        this.contenu = contenu;
        this.userId = userId;
        this.oeuvreId = oeuvreId;
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

    public int getOeuvreId() {
        return oeuvreId;
    }

    public void setOeuvreId(int oeuvreId) {
        this.oeuvreId = oeuvreId;
    }

    // Méthode toString()
    @Override
    public String toString() {
        return "Commentaire{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", date=" + date +
                ", userId=" + userId +
                ", oeuvreId=" + oeuvreId +
                '}';
    }
}