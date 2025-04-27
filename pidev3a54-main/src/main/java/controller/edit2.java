package controller;

import Models.collection_t;
import Models.textile;
import Services.TextileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class edit2 {

        @FXML
        private ImageView imageView;

        @FXML
        private TextField nomField;

        @FXML
        private TextField typeField;

        @FXML
        private TextArea descriptionField;

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
        private File selectedImageFile = null;

        private textile currentTextile;
        private TextileService textileService = new TextileService();

        public void setTextileDetails(textile textile) {
            this.currentTextile = textile;

            if (textile.getImage() != null && !textile.getImage().isEmpty()) {
                imageView.setImage(new Image("file:" + textile.getImage()));
            } else {
                imageView.setImage(null);
            }

            nomField.setText(textile.getNom());
            typeField.setText(textile.getType());
            descriptionField.setText(textile.getDescription());
            matiereField.setText(textile.getMatiere());
            couleurField.setText(textile.getCouleur());
            dimensionField.setText(textile.getDimension());
            createurField.setText(textile.getCreateur());
            techniqueField.setText(textile.getTechnique());
        }

    @FXML
    private void saveChanges(ActionEvent event) {
        // Validation des champs obligatoires
        if (nomField.getText().isEmpty() || typeField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() || matiereField.getText().isEmpty() ||
                couleurField.getText().isEmpty() || dimensionField.getText().isEmpty() ||
                createurField.getText().isEmpty() || techniqueField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Préparation de l'objet textile modifié
        textile updatedTextile = new textile(
                currentTextile.getCollectionId(),
                nomField.getText().trim(),
                typeField.getText().trim(),
                descriptionField.getText().trim(),
                matiereField.getText().trim(),
                couleurField.getText().trim(),
                dimensionField.getText().trim(),
                createurField.getText().trim(),
                (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : currentTextile.getImage(),
                techniqueField.getText().trim(),
                currentTextile.getUserId()
        );
        updatedTextile.setId(currentTextile.getId());

        try {
            textileService.update(updatedTextile);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Textile modifié avec succès !");

            // Redirection avec plein écran
            redirectToDetails1WithFullScreen(event);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelChanges(ActionEvent event) {
        redirectToDetails1WithFullScreen(event);
    }


    // Nouvelle méthode pour rediriger avec plein écran
    private void redirectToDetails1WithFullScreen(ActionEvent event) {
        try {
            // Obtenir les dimensions de l'écran
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details1.fxml"));
            Parent root = loader.load();

            // Initialiser le controller avec la collection
            detail1 controller = loader.getController();
            collection_t collection = new collection_t();
            collection.setId(currentTextile.getCollectionId());
            controller.setCollection(collection);

            // Créer une nouvelle scène avec les dimensions exactes de l'écran
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, screenWidth, screenHeight);
            stage.setScene(scene);

            // Assurez-vous que la fenêtre est maximisée
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
        private void showAlert(Alert.AlertType type, String title, String message) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        @FXML
        private void chooseImage(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedImageFile = file;
                imageView.setImage(new Image(file.toURI().toString()));
            }
        }
    }


