package models;

public class Reclamation {
    private int id;
    private String option;
    private String description;
    private User user; // Relation avec User

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

    // Getters et setters

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
