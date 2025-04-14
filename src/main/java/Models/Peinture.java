package Models;

import java.time.LocalDate;

public class Peinture {
    private int id;
    private String titre;
    private LocalDate dateCr;
    private String tableau; // Chemin de l'image
    private Style style;    // Association à un style
    private int userId;     // Utilisateur associé (fixé statiquement pour toi)

    public Peinture() {}

    public Peinture(int id, String titre, LocalDate dateCr, String tableau, Style style, int userId) {
        this.id = id;
        this.titre = titre;
        this.dateCr = dateCr;
        this.tableau = tableau;
        this.style = style;
        this.userId = userId;
    }

    public Peinture(String titre, LocalDate dateCr, String tableau, Style style, int userId) {
        this.titre = titre;
        this.dateCr = dateCr;
        this.tableau = tableau;
        this.style = style;
        this.userId = userId;
    }

    // Getters
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public LocalDate getDateCr() { return dateCr; }
    public String getTableau() { return tableau; }
    public Style getStyle() { return style; }
    public int getUserId() { return userId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setDateCr(LocalDate dateCr) { this.dateCr = dateCr; }
    public void setTableau(String tableau) { this.tableau = tableau; }
    public void setStyle(Style style) { this.style = style; }
    public void setUserId(int userId) { this.userId = userId; }

    @Override
    public String toString() {
        return titre;
    }
}
