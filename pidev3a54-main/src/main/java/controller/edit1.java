package controller;

import Models.collection_t;
import Services.CollectionTService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

public class edit1 {


    @FXML
    private TextField nomCField;
    @FXML
    private TextArea descriptionCField;

    private collection_t currentCollection;



    private CollectionTService collectionTService = new CollectionTService();


    public void setCollectionDetails(collection_t collection) {
        this.currentCollection = collection;

        nomCField.setText(collection.getNom());
        descriptionCField.setText(collection.getDescription());
    }

    @FXML
    public void saveChanges(ActionEvent actionEvent)  {
        String updatedNom = nomCField.getText().trim();
        String updatedDescription = descriptionCField.getText().trim();

        if (updatedNom.isEmpty() || updatedDescription.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        currentCollection.setNom(updatedNom);
        currentCollection.setDescription(updatedDescription);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<collection_t>> violations = validator.validate(currentCollection);

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder();
            for (ConstraintViolation<collection_t> violation : violations) {
                errorMessage.append("- ").append(violation.getMessage()).append("\n");
            }
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
            return;
        }

        try {
            if (collectionTService != null) {
                collectionTService.update(currentCollection);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Collection mise à jour avec succès !");

            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour.");
        }
    }

    @FXML
    public void cancelChanges(ActionEvent actionEvent) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
