package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Models.Oeuvre;

import java.io.IOException;

public class detail {

    @FXML private ImageView imageView;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField descriptionField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
    @FXML private TextField id_userfield;
    @FXML private TextField categorieField;

    private Oeuvre currentOeuvre;

    public void setOeuvreDetails(Oeuvre t) {
        this.currentOeuvre = t;

        // Set the image
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                imageView.setImage(new Image("file:" + t.getImage()));
            } catch (Exception e) {
                showAlert(AlertType.WARNING, "Image Error", "Could not load image: " + e.getMessage());
            }
        }

        // Set other artwork details
        nomField.setText(t.getNom());
        typeField.setText(t.getType());
        descriptionField.setText(t.getDescription());
        matiereField.setText(t.getMatiere());
        couleurField.setText(t.getCouleur());
        dimensionField.setText(t.getDimensions());
        id_userfield.setText(String.valueOf(t.getUser_id()));
        categorieField.setText(t.getCategorie());
    }

    @FXML
    private void handleOK(ActionEvent event) {
        try {
            // Load the main view
            Parent root = FXMLLoader.load(getClass().getResource("/show.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error", "Could not return to main view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}