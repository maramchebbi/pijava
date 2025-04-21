package Models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Workshops {
    private int id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "Le lien vidéo est obligatoire")
    private String video;

    // Constructeurs
    public Workshops() {}

    public Workshops(String titre, String description, String video) {
        this.titre = titre;
        this.description = description;
        this.video = video;
    }

    public Workshops(int id, String titre, String description, String video) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.video = video;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    // Méthode toString()
    @Override
    public String toString() {
        return "Workshop{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", video='" + video + '\'' +
                '}';
    }
}