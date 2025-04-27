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

    @FXML private ImageView imageView;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
    @FXML private TextField createurField;
    @FXML private TextField techniqueField;

    private textile currentTextile;
    private TextileService textileService = new TextileService();
    private File selectedImageFile = null;

    public void setTextileDetails(textile textile) {
        this.currentTextile = textile;

        if (textile.getImage() != null && !textile.getImage().isEmpty()) {
            try {
                imageView.setImage(new Image("file:" + textile.getImage()));
            } catch (Exception e) {
                System.err.println("Erreur de chargement de l'image: " + e.getMessage());
            }
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
    public void chooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            try {
                imageView.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de charger l'image sélectionnée");
            }
        }
    }

    @FXML
    public void saveChanges(ActionEvent event) {
        // Validation des champs obligatoires
        if (nomField.getText().isEmpty() || typeField.getText().isEmpty() ||
                descriptionField.getText().isEmpty() || matiereField.getText().isEmpty() ||
                couleurField.getText().isEmpty() || dimensionField.getText().isEmpty() ||
                createurField.getText().isEmpty() || techniqueField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, "Champs manquants", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Création du textile mis à jour
        textile updatedTextile = new textile();
        updatedTextile.setId(currentTextile.getId());
        updatedTextile.setNom(nomField.getText().trim());
        updatedTextile.setType(typeField.getText().trim());
        updatedTextile.setDescription(descriptionField.getText().trim());
        updatedTextile.setMatiere(matiereField.getText().trim());
        updatedTextile.setCouleur(couleurField.getText().trim());
        updatedTextile.setDimension(dimensionField.getText().trim());
        updatedTextile.setCreateur(createurField.getText().trim());
        updatedTextile.setTechnique(techniqueField.getText().trim());
        updatedTextile.setImage(selectedImageFile != null ? selectedImageFile.getAbsolutePath() : currentTextile.getImage());
        updatedTextile.setUserId(currentTextile.getUserId());
        updatedTextile.setCollectionId(currentTextile.getCollectionId());

        // Validation avec Hibernate Validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<textile>> violations = validator.validate(updatedTextile);

        if (!violations.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (ConstraintViolation<textile> violation : violations) {
                errors.append("- ").append(violation.getMessage()).append("\n");
            }
            showAlert(AlertType.ERROR, "Erreur de validation", errors.toString());
            return;
        }

        // Sauvegarde des modifications
        try {
            textileService.update(updatedTextile);
            showAlert(AlertType.INFORMATION, "Succès", "Textile modifié avec succès");

            // Retour à l'écran précédent
            Screen screen = Screen.getPrimary();
            double width = screen.getVisualBounds().getWidth();
            double height = screen.getVisualBounds().getHeight();

            Parent root = FXMLLoader.load(getClass().getResource("/show1.fxml"));
            Scene scene = new Scene(root, width, height);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (SQLException | IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Échec de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancelChanges(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double width = screen.getVisualBounds().getWidth();
            double height = screen.getVisualBounds().getHeight();

            Parent root = FXMLLoader.load(getClass().getResource("/show1.fxml"));
            Scene scene = new Scene(root, width, height);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de revenir à la liste");
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