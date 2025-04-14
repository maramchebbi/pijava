package Models;

public class collection_ceramic {
    private int id; // Primary key, auto-increment
    private int user_id;
    private String nom_c;
    private String description_c;

    // Constructor with all fields (except ID if it's auto-generated)
    public collection_ceramic(int user_id, String nom_c, String description_c) {
        this.user_id = user_id;
        this.nom_c = nom_c;
        this.description_c = description_c;
    }

    // Constructor with all fields including ID
    public collection_ceramic(int id, int user_id, String nom_c, String description_c) {
        this.id = id;
        this.user_id = user_id;
        this.nom_c = nom_c;
        this.description_c = description_c;
    }

    public collection_ceramic(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getNom_c() {
        return nom_c;
    }

    public void setNom_c(String nom_c) {
        this.nom_c = nom_c;
    }

    public String getDescription_c() {
        return description_c;
    }

    public void setDescription_c(String description_c) {
        this.description_c = description_c;
    }


    @Override
    public String toString() {
        return "collection_ceramic{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", nom_c='" + nom_c + '\'' +
                ", description_c='" + description_c + '\'' +
                '}';
    }
}
