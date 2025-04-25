package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import Models.Oeuvre;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class detail implements Initializable {

    @FXML private ImageView imageView;
    @FXML private Label nomImageLabel;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField descriptionField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
    @FXML private TextField id_userfield;
    @FXML private TextField categorieField;

    private Oeuvre currentOeuvre;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration initiale si nécessaire
    }

    public void setOeuvreDetails(Oeuvre t) {
        this.currentOeuvre = t;

        // Set the image
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                Image image = new Image("file:" + t.getImage());
                imageView.setImage(image);

                // Ajuster l'image pour qu'elle conserve ses proportions
                imageView.setPreserveRatio(true);

                // Mettre à jour le label sous l'image
                nomImageLabel.setText(t.getNom());
            } catch (Exception e) {
                showAlert(AlertType.WARNING, "Erreur d'Image", "Impossible de charger l'image: " + e.getMessage());
            }
        }

        // Set other artwork details
        nomField.setText(t.getNom());
        typeField.setText(t.getType());
        descriptionField.setText(t.getDescription());
        matiereField.setText(t.getMatiere());
        couleurField.setText(t.getCouleur());
        dimensionField.setText(t.getDimensions());
     //   id_userfield.setText(String.valueOf(t.getUser_id()));
        categorieField.setText(t.getCategorie());
    }

    @FXML
    private void handleOK(ActionEvent event) {
        try {
            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Configuration pour fullscreen
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Maximiser la fenêtre pour utiliser tout l'écran
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de retourner à la vue principale: " + e.getMessage());
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