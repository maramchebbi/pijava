package Models;

import java.util.Date;

public class Participation {
    private int id;
    private int userId; // Attribut userId
    private Event event;
    private String nomUtilisateur;
    private String emailUtilisateur;
    private Date dateParticipation;
    private boolean isWaiting;
    private String qrCode;
    private int numtel;

    // Constructeurs
    public Participation() {}

    public Participation(int id, int userId, Event event, String nomUtilisateur, String emailUtilisateur,
                         Date dateParticipation, boolean isWaiting, String qrCode, int numtel) {
        this.id = id;
        this.userId = userId;
        this.event = event;
        this.nomUtilisateur = nomUtilisateur;
        this.emailUtilisateur = emailUtilisateur;
        this.dateParticipation = dateParticipation;
        this.isWaiting = isWaiting;
        this.qrCode = qrCode;
        this.numtel = numtel;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getEmailUtilisateur() {
        return emailUtilisateur;
    }

    public void setEmailUtilisateur(String emailUtilisateur) {
        this.emailUtilisateur = emailUtilisateur;
    }

    public Date getDateParticipation() {
        return dateParticipation;
    }

    public void setDateParticipation(Date dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setIsWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public int getNumtel() {
        return numtel;
    }

    public void setNumtel(int numtel) {
        this.numtel = numtel;
    }
}