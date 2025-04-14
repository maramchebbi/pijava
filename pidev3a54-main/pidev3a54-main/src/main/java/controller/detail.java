package controller;

import Services.OeuvreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Models.Oeuvre;

import java.io.IOException;
import java.sql.SQLException;

public class detail {

    @FXML
    private ImageView imageView;

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField matiereField;

    @FXML
    private TextField couleurField;

    @FXML
    private TextField dimensionField;

    @FXML
    private TextField id_userfield;

    @FXML
    private TextField categorieField;

    private Oeuvre currentOeuvre;
    private OeuvreService oeuvreService = new OeuvreService();

    // Method to set the artwork details in the UI
    public void setOeuvreDetails(Oeuvre t) {
        this.currentOeuvre = t;

        // Set the image
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            imageView.setImage(new Image("file:" + t.getImage()));
        } else {
            showAlert(AlertType.WARNING, "No Image", "No image associated with this artwork.");
        }

        // Set other artwork details
        nomField.setText(t.getNom());
        typeField.setText(t.getType());
        descriptionField.setText(t.getDescription());
        matiereField.setText(t.getMatiere());
        couleurField.setText(t.getCouleur());
        dimensionField.setText(t.getDimensions());

        // Convert user_id to String before setting it in the text field
        id_userfield.setText(String.valueOf(t.getUser_id()));

        // Set the category (based on your model, it seems to be the "categorie" field)
        categorieField.setText(t.getCategorie());
    }

    // Method to modify the artwork details
    @FXML
    private void modifierOeuvre(ActionEvent event) {
        try {
            // Load the edit.fxml and get the controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            // Get the controller for edit.fxml
            edit editController = loader.getController();

            // Pass data to the edit controller
            editController.setOeuvreDetails(currentOeuvre);

            // Show the new stage for editing
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "Unable to load edit window.");
        }
    }


    // Method to delete the artwork with confirmation
    @FXML
    public void supprimerOeuvre(ActionEvent actionEvent) {
        if (currentOeuvre == null) {
            showAlert(AlertType.ERROR, "No Artwork Selected", "No artwork selected for deletion.");
            return;
        }

        // Show a confirmation dialog
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this artwork?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Call the service to delete the artwork (via the instance of OeuvreService)
                    oeuvreService.delete(currentOeuvre);  // Corrected the delete method call to use the service instance
                    showAlert(AlertType.INFORMATION, "Deletion Successful", "The artwork has been successfully deleted.");
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Deletion Failed", "Failed to delete the artwork.");
                    e.printStackTrace();
                }
            }
        });
    }

    // Helper method to show alerts
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void initialize() {
        // Make sure the FXML is loaded and the id_userfield is initialized
        if (id_userfield != null) {
            id_userfield.setText("Some default text");
        } else {
            System.out.println("id_userfield is not initialized");
        }
    }





}
