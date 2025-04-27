package Models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modèle représentant un événement recommandé
 */
public class RecommendedEvent {

    private final IntegerProperty id;
    private final StringProperty titre;
    private final DoubleProperty similarityScore;

    public RecommendedEvent(int id, String titre, double similarityScore) {
        this.id = new SimpleIntegerProperty(id);
        this.titre = new SimpleStringProperty(titre);
        this.similarityScore = new SimpleDoubleProperty(similarityScore);
    }

    // Getters et setters pour id
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Getters et setters pour titre
    public String getTitre() {
        return titre.get();
    }

    public void setTitre(String titre) {
        this.titre.set(titre);
    }

    public StringProperty titreProperty() {
        return titre;
    }

    // Getters et setters pour similarityScore
    public double getSimilarityScore() {
        return similarityScore.get();
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore.set(similarityScore);
    }

    public DoubleProperty similarityScoreProperty() {
        return similarityScore;
    }

    // Formater le score en pourcentage pour l'affichage
    public String getFormattedScore() {
        return String.format("%.1f%%", similarityScore.get() * 100);
    }

    @Override
    public String toString() {
        return "RecommendedEvent{" +
                "id=" + getId() +
                ", titre='" + getTitre() + '\'' +
                ", similarityScore=" + getSimilarityScore() +
                '}';
    }
}