package controller;

import Models.Workshops;
import Services.WorkshopService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Screen;
import java.io.File;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.application.Platform;

public class AddWorkshop implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private Label videoLabel;
    @FXML private Button backButton;
    @FXML
    private Button backgallery;



    private File selectedVideoFile;
    private WorkshopService workshopService = new WorkshopService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // We'll use Platform.runLater to ensure this runs after the scene is fully initialized
        Platform.runLater(() -> {
            if (titreField.getScene() != null && titreField.getScene().getWindow() != null) {
                setFullScreen((Stage) titreField.getScene().getWindow());
            }
        });
    }
    @FXML
    void backgallery(ActionEvent event) {
        try {
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Load the gallery view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent galleryRoot = loader.load();

            // Create new scene
            Scene scene = new Scene(galleryRoot, screenWidth, screenHeight);

            // Get current stage from the source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la galerie: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setFullScreen(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.setTitle("Ajouter un Workshop");
    }

    @FXML
    void voiraction(ActionEvent event) {

        try {
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Load the gallery view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ShowWorkshops.fxml"));
            Parent root = loader.load();

            // Create new scene
            Scene scene = new Scene(root, screenWidth, screenHeight);

            // Get current stage from the source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la galerie: " + e.getMessage());
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
            videoLabel.setStyle("-fx-text-fill: #2e7d32;"); // Green color for success
        } else {
            videoLabel.setText("Aucune vidéo sélectionnée");
            videoLabel.setStyle("-fx-text-fill: #767676;"); // Reset to default color
        }
    }

    @FXML
    private void handleAddWorkshop() {
        if (!validateInputs()) {
            return;
        }

        try {
            // Create and save workshop
            Workshops workshop = new Workshops();
            workshop.setTitre(titreField.getText());
            workshop.setDescription(descriptionField.getText());
            workshop.setVideo(selectedVideoFile.getAbsolutePath());

            workshopService.add(workshop);

            // Show success message with improved appearance
            showSuccessAlert("Workshop ajouté avec succès!");

            // Clear fields
            resetForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur lors de l'ajout", "Détails: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        StringBuilder errorMessage = new StringBuilder();

        if (titreField.getText().trim().isEmpty()) {
            errorMessage.append("- Le titre est requis\n");
            titreField.setStyle("-fx-border-color: #ff6b6b; -fx-background-color: #fff8f8; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            titreField.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e6dfd5; -fx-border-radius: 5; -fx-background-radius: 5;");
        }

        if (descriptionField.getText().trim().isEmpty()) {
            errorMessage.append("- La description est requise\n");
            descriptionField.setStyle("-fx-border-color: #ff6b6b; -fx-background-color: #fff8f8; -fx-border-radius: 5; -fx-background-radius: 5;");
        } else {
            descriptionField.setStyle("-fx-background-color: #fafafa; -fx-border-color: #e6dfd5; -fx-border-radius: 5; -fx-background-radius: 5;");
        }

        if (selectedVideoFile == null) {
            errorMessage.append("- Une vidéo doit être sélectionnée\n");
        }

        if (errorMessage.length() > 0) {
            showErrorAlert("Validation échouée", "Veuillez corriger les erreurs suivantes:\n" + errorMessage.toString());
            return false;
        }

        return true;
    }

    private void resetForm() {
        titreField.clear();
        descriptionField.clear();
        videoLabel.setText("Aucune vidéo sélectionnée");
        videoLabel.setStyle("-fx-text-fill: #767676;");
        selectedVideoFile = null;
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        styleAlert(alert);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        styleAlert(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #e6dfd5; -fx-border-width: 1px;");
        dialogPane.getStyleClass().add("modern-alert");

        // Add custom styling if CSS is available in your project
        // Stage stage = (Stage) dialogPane.getScene().getWindow();
        // stage.getScene().getStylesheets().add(getClass().getResource("/styles/alerts.css").toExternalForm());
    }






}