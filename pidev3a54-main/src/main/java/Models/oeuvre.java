package Models;

public class oeuvre {
    private int id;  // Renamed from idPrimaire
    private int ceramicCollectionId;
    private int userId;
    private String nom;
    private String type;
    private String description;
    private String matiere;
    private String couleur;
    private String dimensions;
    private String createur;
    private String image;
    private String categorie;

    public oeuvre() {}

    // Constructor
    public oeuvre(int id, int ceramicCollectionId, int userId, String nom, String type,
                  String description, String matiere, String couleur, String dimensions,
                  String createur, String image, String categorie) {
        this.id = id;
        this.ceramicCollectionId = ceramicCollectionId;
        this.userId = userId;
        this.nom = nom;
        this.type = type;
        this.description = description;
        this.matiere = matiere;
        this.couleur = couleur;
        this.dimensions = dimensions;
        this.createur = createur;
        this.image = image;
        this.categorie = categorie;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCeramicCollectionId() {
        return ceramicCollectionId;
    }

    public void setCeramicCollectionId(int ceramicCollectionId) {
        this.ceramicCollectionId = ceramicCollectionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }


    @Override
    public String toString() {
        return "Oeuvre{" +
                "id=" + id +
                ", ceramicCollectionId=" + ceramicCollectionId +
                ", userId=" + userId +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", matiere='" + matiere + '\'' +
                ", couleur='" + couleur + '\'' +
                ", dimensions='" + dimensions + '\'' +
                ", createur='" + createur + '\'' +
                ", image='" + image + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }

}
