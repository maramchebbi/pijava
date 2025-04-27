package Models;

import java.util.List;

public class User {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private String genre;
    private List<String> roles;
    private String password;

    // Constructeurs
    public User() {
    }

    public User(Long id, String email, String nom, String prenom, String genre, List<String> roles, String password) {
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.roles = roles;
        this.password = password;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Méthode toString() pour l'affichage
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre='" + genre + '\'' +
                ", roles=" + roles +
                ", password='" + password + '\'' +
                '}';
    }
}