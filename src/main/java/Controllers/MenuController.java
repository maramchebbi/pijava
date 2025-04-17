package Controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;


import java.io.IOException;

public class MenuController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    private void initialize() {
        // Créer un texte de bienvenue
        Text welcomeText = new Text("Bienvenue dans l'interface Artisfera !");
        welcomeText.setFont(Font.font("Segoe UI", 30));  // Utiliser une police moderne et plus grande
        welcomeText.setFill(Color.web("#3498db"));  // Une couleur bleue plus moderne
        welcomeText.setStyle("-fx-font-weight: bold;");  // Mettre le texte en gras

        // Ajouter le texte dans le contentPane
        contentPane.getChildren().add(welcomeText);

        // Centrer le texte dans le AnchorPane
        centerText(welcomeText);

        // Ajouter une animation de fade-in pour rendre l'apparition plus fluide
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), welcomeText);
        fadeIn.setFromValue(0.0);  // Commencer invisible
        fadeIn.setToValue(1.0);    // Devenir visible
        fadeIn.setCycleCount(1);   // Jouer une fois
        fadeIn.setAutoReverse(false); // Ne pas inverser l'animation
        fadeIn.play();
    }

    // Méthode pour centrer dynamiquement le texte dans le AnchorPane
    private void centerText(Text text) {
        // S'assurer que le texte est centré dans le contentPane en calculant la position dynamique
        contentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double x = (newVal.doubleValue() - text.getBoundsInLocal().getWidth()) / 2;
            AnchorPane.setLeftAnchor(text, x);
        });

        contentPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double y = (newVal.doubleValue() - text.getBoundsInLocal().getHeight()) / 2;
            AnchorPane.setTopAnchor(text, y);
        });
    }



    public void goToAjouterPeinture() throws IOException {
        setContent("AjouterPeinture.fxml");
    }

    public void goToAfficherPeintures() throws IOException {
        setContent("AfficherPeinture.fxml");
    }

    public void goToAjouterStyle() throws IOException {
        setContent("AjouterStyle.fxml");
    }

    public void goToAfficherStyles() throws IOException {
        setContent("AfficherStyle.fxml");
    }

    @FXML
    private void goToChatbot() throws IOException {
        setContent("chatbot.fxml");  // Charger la vue du chatbot
    }


    private void setContent(String fxml) throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/" + fxml));
        contentPane.getChildren().setAll(pane);
    }
}
