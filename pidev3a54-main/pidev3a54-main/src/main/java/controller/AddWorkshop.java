package controller;

import Models.Workshops;
import Services.WorkshopService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.SQLException;

public class AddWorkshop {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private Label videoLabel;

    private File selectedVideoFile;
    private WorkshopService workshopService = new WorkshopService(); // Initialize service

    @FXML
    private void handleBrowseVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une vidéo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers vidéo", "*.mp4", "*.avi", "*.mov")
        );

        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedVideoFile = file;
            videoLabel.setText(file.getName());
        } else {
            videoLabel.setText("Aucune vidéo sélectionnée");
        }
    }

    @FXML
    private void handleAddWorkshop() {
        String titre = titreField.getText();
        String description = descriptionField.getText();

        if (titre.isEmpty() || description.isEmpty() || selectedVideoFile == null) {
            showAlert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs et sélectionner une vidéo.");
            return;
        }

        try {
            // Create and save workshop
            Workshops workshop = new Workshops();
            workshop.setTitre(titre);
            workshop.setDescription(description);
            workshop.setVideo(selectedVideoFile.getAbsolutePath());

            workshopService.add(workshop); // Actually save to database

            // Clear fields
            titreField.clear();
            descriptionField.clear();
            videoLabel.setText("Aucune vidéo sélectionnée");
            selectedVideoFile = null;

            showAlert(Alert.AlertType.INFORMATION, "Workshop ajouté avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}