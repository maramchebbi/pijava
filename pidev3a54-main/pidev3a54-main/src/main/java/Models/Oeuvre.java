package Models;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Oeuvre {
    private int id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le type est obligatoire")
    private String type;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "La matière est obligatoire")
    private String matiere;

    @NotBlank(message = "La couleur est obligatoire")
    private String couleur;

    @NotBlank(message = "Les dimensions sont obligatoires")
    @Pattern(regexp = "\\d+x\\d+x\\d+", message = "Les dimensions doivent être au format hxlxp, par exemple 10x20x30")
    private String dimensions;

    @NotBlank(message = "L'image est obligatoire")
    private String image;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorie;

    // Aucune validation sur fichier3d pour permettre des valeurs null
    private String fichier3d;

    private int user_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceramic_collection_id")
    private CeramicCollection collection;

    // Constructeur par défaut
    public Oeuvre() {
        this.fichier3d = null; // Valeur par défaut explicite
    }

    // Constructeur complet avec le champ fichier3d
    public Oeuvre(String nom, String type, String description, String matiere, String couleur, String dimensions,
                  String image, String categorie, String fichier3d, int user_id, CeramicCollection collection) {
        this.nom = nom;
        this.type = type;
        this.description = description;
        this.matiere = matiere;
        this.couleur = couleur;
        this.dimensions = dimensions;
        this.image = image;
        this.categorie = categorie;
        this.fichier3d = fichier3d;
        this.user_id = user_id;
        this.collection = collection;
    }

    // Constructeur sans le champ fichier3d pour la rétrocompatibilité
    public Oeuvre(String nom, String type, String description, String matiere, String couleur, String dimensions,
                  String image, String categorie, int user_id, CeramicCollection collection) {
        this(nom, type, description, matiere, couleur, dimensions, image, categorie, null, user_id, collection);
    }

    // Constructeur avec id et tous les champs
    public Oeuvre(int id, String nom, String type, String description, String matiere, String couleur,
                  String dimensions, String image, String categorie, String fichier3d, int user_id) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.description = description;
        this.matiere = matiere;
        this.couleur = couleur;
        this.dimensions = dimensions;
        this.image = image;
        this.categorie = categorie;
        this.fichier3d = fichier3d;
        this.user_id = user_id;
    }

    // Constructeur sans le champ fichier3d avec id pour la rétrocompatibilité
    public Oeuvre(int id, String nom, String type, String description, String matiere, String couleur,
                  String dimensions, String image, String categorie, int user_id) {
        this(id, nom, type, description, matiere, couleur, dimensions, image, categorie, null, user_id);
    }



    public boolean hasFichier3D() {
        return fichier3d != null && !fichier3d.isEmpty();
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getFichier3d() {
        return fichier3d;
    }

    public void setFichier3d(String fichier3d) {
        System.out.println("Setting fichier3d to: " + fichier3d);
        this.fichier3d = fichier3d;
    }

    public CeramicCollection getCollection() {
        return collection;
    }

    public void setCollection(CeramicCollection collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        return "Oeuvre{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", matiere='" + matiere + '\'' +
                ", couleur='" + couleur + '\'' +
                ", dimensions='" + dimensions + '\'' +
                ", image='" + image + '\'' +
                ", categorie='" + categorie + '\'' +
                ", fichier3d='" + (hasFichier3D() ? fichier3d : "Non disponible") + '\'' +
                ", user_id=" + user_id +
                ", collection=" + collection +
                '}';
    }
}