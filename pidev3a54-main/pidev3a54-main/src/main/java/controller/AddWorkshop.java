package controller;

import Models.Workshops;
import Services.WorkshopService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class AddWorkshop {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private Label videoLabel;

    private File selectedVideoFile;
    private WorkshopService workshopService = new WorkshopService(); // Initialize service



    @FXML
    void voiraction(ActionEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowWorkshops.fxml"));
            Parent root = loader.load();

            // Get the current stage - CORRECTED: using 'event' instead of 'actionEvent'
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Create a new scene with the loaded FXML content
            Scene scene = new Scene(root);

            // Set the new scene and show the stage
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println("Error loading the FXML file: " + e.getMessage());
            e.printStackTrace();  // Added for better debugging
        }
    }

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