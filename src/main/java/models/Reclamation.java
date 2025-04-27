package models;

public class Reclamation {
    private int id;
    private String option;
    private String description;
    private User user; // Relation avec User
    private String filePath; // Chemin du fichier joint

    public Reclamation() {}

    public Reclamation(String option, String description, User user) {
        this.option = option;
        this.description = description;
        this.user = user;
    }

    public Reclamation(int id, String option, String description, User user) {
        this.id = id;
        this.option = option;
        this.description = description;
        this.user = user;
    }

    // Constructeur avec filePath
    public Reclamation(int id, String option, String description, User user, String filePath) {
        this.id = id;
        this.option = option;
        this.description = description;
        this.user = user;
        this.filePath = filePath;
    }

    // Getters et setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean hasAttachment() {
        return filePath != null && !filePath.isEmpty();
    }
}