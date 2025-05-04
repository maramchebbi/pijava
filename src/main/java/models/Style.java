package models;

public class Style {
    private int id;
    private String type_p;
    private String description;
    private String extab; // Chemin du tableau exemplaire (image)

    public Style() {}

    public Style(int id, String type_p, String description, String extab) {
        this.id = id;
        this.type_p = type_p;
        this.description = description;
        this.extab = extab;
    }

    public Style(String type_p, String description, String extab) {
        this.type_p = type_p;
        this.description = description;
        this.extab = extab;
    }

    // Getters
    public int getId() { return id; }
    public String getType() { return type_p; }
    public String getDescription() { return description; }
    public String getExtab() { return extab; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setType(String type_p) { this.type_p = type_p; }
    public void setDescription(String description) { this.description = description; }
    public void setExtab(String extab) { this.extab = extab; }

    @Override
    public String toString() {
        return type_p;
    }
}
