package Controllers;

import Models.Playlist;
import Services.PlaylistService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

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
                service.add(playlist);
                // showAlert("Succès", "Playlist ajoutée avec succès !");
            } catch (Exception e) {
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
