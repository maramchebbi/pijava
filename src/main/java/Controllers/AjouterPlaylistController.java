package Controllers;

import Models.Playlist;
import Services.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Date;

public class AjouterPlaylistController {

    @FXML
    private TextField titreField;
    @FXML
    private TextField descriptionField;

    @FXML
    private Label titreErrorLabel;

    @FXML
    private Label descriptionErrorLabel;

    @FXML
    private void initialize() {
        // Add real-time validation (prevent numbers)
        addNumberFilter(titreField, titreErrorLabel);
        addNumberFilter(descriptionField, descriptionErrorLabel);
    }

    private void addNumberFilter(TextField textField, Label errorLabel) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            char c = event.getCharacter().charAt(0);
            if (Character.isDigit(c)) {
                event.consume();
                errorLabel.setText("Caractère requis.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });
    }

    @FXML
    void handleAjouterPlaylist() {
        boolean isValid = true;

        // Reset errors
        titreErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);

        // Validate Titre
        if (titreField.getText().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            titreErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Description
        if (descriptionField.getText().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            // Continue with adding playlist logic


            String titre = titreField.getText();
            String description = descriptionField.getText();
            int userId = 1; // change to the actual logged-in user ID
            Date dateCreation = new Date(); // date set automatically

            Playlist playlist = new Playlist(titre, userId, description, dateCreation);
            PlaylistService service = new PlaylistService();

                try {
                    // Attempt to add the playlist using the service
                    service.add(playlist);

                    // After adding, reload the playlists and refresh the left pane
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPlaylist.fxml"));
                    AnchorPane afficherPlaylistsPane = loader.load();

                    // Find the left pane from the current scene
                    AnchorPane leftPane = (AnchorPane) titreField.getScene().lookup("#leftPane");

                    // Clear the existing content in the left pane
                    leftPane.getChildren().clear();

                    // Add the newly loaded playlists content to the left pane
                    leftPane.getChildren().add(afficherPlaylistsPane);

                    AnchorPane.setTopAnchor(afficherPlaylistsPane, 0.0);
                    AnchorPane.setBottomAnchor(afficherPlaylistsPane, 0.0);
                    AnchorPane.setLeftAnchor(afficherPlaylistsPane, 0.0);
                    AnchorPane.setRightAnchor(afficherPlaylistsPane, 0.0);

                } catch (Exception e) {
                    // Show an error alert if something goes wrong
                    showAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                }


            showAlert1(Alert.AlertType.INFORMATION, "Succès", "Playlist ajoutée avec succès !");
        } else {
            showAlert1(Alert.AlertType.ERROR, "Erreur", "Veuillez corriger les champs en rouge.");
        }
    }

private void showAlert1(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }
}
