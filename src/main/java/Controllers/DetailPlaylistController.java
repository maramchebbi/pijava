package Controllers;

import Models.Playlist;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DetailPlaylistController {

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;

    public void setPlaylistDetails(Playlist playlist) {
        titleLabel.setText("Title: " + playlist.getTitre_p());
        descriptionLabel.setText("Description: " + playlist.getDescription());
        dateLabel.setText("Created on: " + playlist.getDate_creation().toString());
    }
}
