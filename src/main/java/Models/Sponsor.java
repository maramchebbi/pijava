package Models;

public class Sponsor {

    private int id;
    private String nom;
    private String type;
    private String email;
    private String telephone;
    private String siteWeb;
    private String logo;
    private double montant;
    private int eventCount; // Nombre d'événements associés

    // Constructeur
    public Sponsor() {
    }

    public Sponsor(int id, String nom, String type, String email, String telephone, String siteWeb, String logo, double montant) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.email = email;
        this.telephone = telephone;
        this.siteWeb = siteWeb;
        this.logo = logo;
        this.montant = montant;
        this.eventCount = 0;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    @Override
    public String toString() {
        return this.nom;
    }
}