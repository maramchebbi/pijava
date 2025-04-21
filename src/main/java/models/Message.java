package models;

import java.time.LocalDateTime;

public class Message {
    private String contenu;
    private String nom;
    private String email;
    private String role;
    private LocalDateTime date;

    public Message(String contenu, String nom, String email, String role, LocalDateTime date) {
        this.contenu = contenu;
        this.nom = nom;
        this.email = email;
        this.role = role;
        this.date = date;
    }

    // Getters
    public String getContenu() { return contenu; }
    public String getNom() { return nom; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getDate() { return date; }
}