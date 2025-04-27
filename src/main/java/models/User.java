package models;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String nom;
    private String prenom;
    private String genre;
    private String email;
    private String password;
    private String role;
    private boolean isVerified;
    private String verificationCode;
    private boolean googleAccount;
    private boolean facebookAccount;  // Nouvelle propriété pour les comptes Facebook



    public User() {
    }

    public User(int id, String nom, String prenom, String genre, String email, String password, String role, boolean isVerified) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
    }

    public User(String nom, String prenom, String genre, String email, String password, String role, boolean isVerified) {
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isVerified = isVerified;
    }
    public User(int id, String nom, String prenom, String genre, String email, String password,
                String role, boolean isVerified, String verificationCode,
                boolean googleAccount, boolean facebookAccount) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "membre";
        this.isVerified = isVerified;
        this.verificationCode = verificationCode != null ? verificationCode : "";
        this.googleAccount = googleAccount;
        this.facebookAccount = facebookAccount;
    }
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    public boolean isGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(boolean googleAccount) {
        this.googleAccount = googleAccount;
    }

    public boolean isFacebookAccount() {
        return facebookAccount;
    }

    public void setFacebookAccount(boolean facebookAccount) {
        this.facebookAccount = facebookAccount;
    }




    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre='" + genre + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", isVerified=" + isVerified +
                '}';
    }
}
