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
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;

public class AjouterCollectionT {

    @FXML
    private TextField nomCField;

    @FXML
    private TextArea descriptionCField;

    @FXML
    private TextField iduserfield;

    private final CollectionTService service = new CollectionTService();


    @FXML
    public void AjouterCollectionT(ActionEvent actionEvent) {
        try {
            String nom = nomCField.getText().trim();
            String description = descriptionCField.getText().trim();
            String userIdStr = iduserfield.getText().trim();

            if (nom.isEmpty() || description.isEmpty() || userIdStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "L'ID utilisateur doit être un nombre.");
                return;
            }

            collection_t newCollection = new collection_t(nom, userId, description);

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<collection_t>> violations = validator.validate(newCollection);

            if (!violations.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (ConstraintViolation<collection_t> violation : violations) {
                    errorMessage.append("- ").append(violation.getMessage()).append("\n");
                }
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", errorMessage.toString());
                return;
            }

            service.add(newCollection);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Collection ajoutée avec succès !");
//            nomCField.clear();
//            descriptionCField.clear();
//            iduserfield.clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

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
    public void ViewCollectionT(ActionEvent actionEvent) {
        try {
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
}

}
