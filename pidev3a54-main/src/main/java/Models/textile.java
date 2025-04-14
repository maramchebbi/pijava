package Models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class textile {
    private int id;
    private Integer collectionId;
    @NotBlank(message = "Le nom ne doit pas être vide")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")// Nullable
    private String nom;

    @NotBlank(message = "Le type ne doit pas être vide")
    private String type;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotBlank(message = "La matière est obligatoire")
    private String matiere;

    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    @NotBlank(message = "La dimension est obligatoire")
    @Pattern(regexp = "^\\d+\\*\\d+$", message = "La dimension doit être au format 'nombre*nombre', par exemple '30*50'")
    private String dimension;

    @NotBlank(message = "Le nom du créateur est obligatoire")
    private String createur;

    @NotBlank(message = "Le chemin de l'image est obligatoire")
    @Pattern(
            regexp = "([^\\s]+(\\.(?i)(jpg|jpeg|png|gif|bmp))$)",
            message = "L'image doit être un fichier avec une extension valide (.jpg, .jpeg, .png, .gif, .bmp)"
    )
    private String image;

    @NotBlank(message = "La technique est obligatoire")
    private String technique;

    private int userId;

    // Constructeur vide
    public textile() {}

    // Constructeur avec tous les champs sauf l'id (utilisé pour l'insertion)
    public textile(Integer collectionId, String nom, String type, String description,
                   String matiere, String couleur, String dimension, String createur,
                   String image, String technique, int userId) {
        this.collectionId = collectionId;
        this.nom = nom;
        this.type = type;
        this.description = description;
        this.matiere = matiere;
        this.couleur = couleur;
        this.dimension = dimension;
        this.createur = createur;
        this.image = image;
        this.technique = technique;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMatiere() {
        return matiere;
    }

    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getCreateur() {
        return createur;
    }

    public void setCreateur(String createur) {
        this.createur = createur;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "textile{" +
                "id=" + id +
                ", collectionId=" + collectionId +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", matiere='" + matiere + '\'' +
                ", couleur='" + couleur + '\'' +
                ", dimension='" + dimension + '\'' +
                ", createur='" + createur + '\'' +
                ", image='" + image + '\'' +
                ", technique='" + technique + '\'' +
                ", userId=" + userId +
                '}';
    }
}
