package controller;

import Services.TextileService;
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
import Models.textile;

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
    private TextField createurField;

    @FXML
    private TextField techniqueField;

    private textile currentTextile;
    private TextileService textileService = new TextileService();

    public void setTextileDetails(textile t) {
        this.currentTextile = t;

        if (t.getImage() != null && !t.getImage().isEmpty()) {
            imageView.setImage(new Image("file:" + t.getImage()));
        } else {
            showAlert(AlertType.WARNING, "No Image", "No image associated with this textile.");
        }
        nomField.setText(t.getNom());
        typeField.setText(t.getType());
        descriptionField.setText(t.getDescription());
        matiereField.setText(t.getMatiere());
        couleurField.setText(t.getCouleur());
        dimensionField.setText(t.getDimension());
        createurField.setText(t.getCreateur());
        techniqueField.setText(t.getTechnique());
    }

    @FXML
    public void modifiertextile(ActionEvent actionEvent) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "Invalid Textile", "No textile data to modify.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            edit editController = loader.getController();
            editController.setTextileDetails(currentTextile);

            Scene editScene = new Scene(root);
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(editScene);
            currentStage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Loading Error", "An error occurred while trying to load the edit screen.");
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerTextile(ActionEvent actionEvent) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "No Textile Selected", "No textile selected for deletion.");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this textile?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {


            if (response == ButtonType.OK) {
                try {
                    textileService.delete(currentTextile);
                    showAlert(AlertType.INFORMATION, "Deletion Successful", "The textile has been successfully deleted.");
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Deletion Failed", "Failed to delete the textile.");
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
