package Controllers;

import Models.Playlist;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import Services.PlaylistService;

import java.io.IOException;
import java.sql.SQLException;


public class UpdatePlaylistController {
    @FXML
    private TextField titreField;
    @FXML
    private TextField descriptionField;

    private Playlist currentPlaylist;

    @FXML private Label titreErrorLabel;
    @FXML private Label descriptionErrorLabel;

    @FXML
    private void initialize() {
        // Prevent numbers in text input
        addCharacterOnlyFilter(titreField, titreErrorLabel);
        addCharacterOnlyFilter(descriptionField, descriptionErrorLabel);
    }

    private void addCharacterOnlyFilter(TextField field, Label errorLabel) {
        field.addEventFilter(KeyEvent.KEY_TYPED, event -> {
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

    public void setPlaylist(Playlist playlist) {
        this.currentPlaylist = playlist;
        titreField.setText(playlist.getTitre_p());
        descriptionField.setText(playlist.getDescription());
    }

    @FXML
    private void handleUpdatePlaylist() {
        boolean isValid = true;

        titreErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);

        if (titreField.getText().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            titreErrorLabel.setVisible(true);
            isValid = false;
        }

        if (descriptionField.getText().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        if (isValid) {
            System.out.println("Mise à jour de la playlist effectuée.");
            // Proceed with update logic here


            currentPlaylist.setTitre_p(titreField.getText());
            currentPlaylist.setDescription(descriptionField.getText());
            PlaylistService PlaylistService = new PlaylistService();
            try {
                // Update the playlist object with changes
                PlaylistService.update(currentPlaylist);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Playlist mise à jour avec succès !");

                // Go back to AfficherPlaylist.fxml after updating
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPlaylist.fxml"));
                AnchorPane afficherPlaylistPane = loader.load();

                // Important: find the leftPane from the global scene
                AnchorPane leftPane = (AnchorPane) titreField.getScene().lookup("#leftPane");

                leftPane.getChildren().clear();
                leftPane.getChildren().add(afficherPlaylistPane);

                AnchorPane.setTopAnchor(afficherPlaylistPane, 0.0);
                AnchorPane.setBottomAnchor(afficherPlaylistPane, 0.0);
                AnchorPane.setLeftAnchor(afficherPlaylistPane, 0.0);
                AnchorPane.setRightAnchor(afficherPlaylistPane, 0.0);

            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }

        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
