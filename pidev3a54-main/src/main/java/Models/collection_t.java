package Models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class collection_t {
    private int id;

    @NotBlank(message = "Le nom de la collection est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    private int userId;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 2, max = 6, message = "La descrption doit contenir entre 2 et 6 caractères")
    private String description;

    // Constructeur vide
    public collection_t() {}

    // Constructeur avec tous les champs sauf id (pour insertion)
    public collection_t(String nom, int userId, String description) {
        this.nom = nom;
        this.userId = userId;
        this.description = description;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    //methode tostring
    @Override
    public String toString() {
        return "CollectionT{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                '}';
    }
}
