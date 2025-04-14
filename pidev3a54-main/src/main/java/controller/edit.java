package controller;

import Models.textile;
import Services.TextileService;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class edit {

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
        String nom = nomField.getText().trim();
        String type = typeField.getText().trim();
        String description = descriptionField.getText().trim();
        String matiere = matiereField.getText().trim();
        String couleur = couleurField.getText().trim();
        String dimension = dimensionField.getText().trim();
        String createur = createurField.getText().trim();
        String technique = techniqueField.getText().trim();

        if (nom.isEmpty() || type.isEmpty() || description.isEmpty() || matiere.isEmpty() ||
                couleur.isEmpty() || dimension.isEmpty() || createur.isEmpty() || technique.isEmpty()) {
            showAlert(AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        String imagePath = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : currentTextile.getImage();
        if (imagePath == null || imagePath.isEmpty()) {
            showAlert(AlertType.ERROR, "Image manquante", "Veuillez choisir une image.");
            return;
        }

        textile updatedTextile = new textile(
                currentTextile.getCollectionId(),
                nom, type, description, matiere, couleur,
                dimension, createur, imagePath, technique,
                currentTextile.getUserId()
        );
        updatedTextile.setId(currentTextile.getId());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<textile>> violations = validator.validate(updatedTextile);

        if (!violations.isEmpty()) {
            StringBuilder errorMessages = new StringBuilder();
            for (ConstraintViolation<textile> violation : violations) {
                errorMessages.append("- ").append(violation.getMessage()).append("\n");
            }
            showAlert(AlertType.ERROR, "Erreur de validation", errorMessages.toString());
            return;
        }

        try {
            textileService.update(updatedTextile);
            refreshUI(updatedTextile);

            showAlert(AlertType.INFORMATION, "Succès", "Le textile a été modifié avec succès.");



        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de modifier le textile.");
            e.printStackTrace();
        }
    }




    private void refreshUI(textile updatedTextile) {
        Image newImage = new Image("file:" + updatedTextile.getImage());
        imageView.setImage(newImage);

        nomField.setText(updatedTextile.getNom());
        typeField.setText(updatedTextile.getType());
        descriptionField.setText(updatedTextile.getDescription());
        matiereField.setText(updatedTextile.getMatiere());
        couleurField.setText(updatedTextile.getCouleur());
        dimensionField.setText(updatedTextile.getDimension());
        createurField.setText(updatedTextile.getCreateur());
        techniqueField.setText(updatedTextile.getTechnique());
    }
    @FXML
    private void cancelChanges(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }



    private void showAlert(AlertType type, String title, String message) {
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
