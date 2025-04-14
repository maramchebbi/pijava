package controller;

import Models.collection_ceramic;
import Services.CollectionCService;
import Services.CollectionTService;
import Services.OeuvreService;
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
import java.util.Set;

public class AjouterCollectionCeramic {
    @FXML
    private TextArea descriptionCOField;

    @FXML
    private TextField iduserOfield;

    @FXML
    private TextField nomCOField;

    @FXML

    private final CollectionCService service = new CollectionCService();

    public void AjouterCollectionC(ActionEvent event) {
            try {
                String nom = nomCOField.getText().trim();
                String description = descriptionCOField.getText().trim();
                String userIdStr = iduserOfield.getText().trim();

                // ✅ Étape 1 : Vérification des champs vides
                if (nom.isEmpty() || description.isEmpty() || userIdStr.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                    return;
                }

                // ✅ Étape 2 : Vérification que l’ID utilisateur est un nombre
                int userId;
                try {
                    userId = Integer.parseInt(userIdStr);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "L'ID utilisateur doit être un nombre.");
                    return;
                }

                // ✅ Étape 3 : Créer l’objet collection_ceramic
                collection_ceramic newCollection = new collection_ceramic(userId, nom, description);

                // ✅ Étape 4 : Valider les contraintes si vous utilisez la validation
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                Validator validator = factory.getValidator();
                Set<ConstraintViolation<collection_ceramic>> violations = validator.validate(newCollection);

                if (!violations.isEmpty()) {
                    StringBuilder errorMessage = new StringBuilder();
                    for (ConstraintViolation<collection_ceramic> violation : violations) {
                        errorMessage.append("- ").append(violation.getMessage()).append("\n");
                    }
                    showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
                    return;
                }

                // ✅ Étape 5 : Ajouter à la base de données via service
                service.add(newCollection);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Collection céramique ajoutée avec succès !");
                nomCOField.clear();
                descriptionCOField.clear();
                iduserOfield.clear();

            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite : " + e.getMessage());
            }
        }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.show();
    }



    @FXML
    void ViewCollectionC(ActionEvent event) {
            try {
                // Load the FXML file for showing ceramic collections
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/show_collection.fxml")); // ✅ Make sure this path is correct and exists
                Parent root = loader.load();

                // Get the current stage from the event source
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Create a new scene and set it on the current stage
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                System.out.println("Erreur lors du chargement du fichier FXML : " + e.getMessage());
            }
        }

    }

