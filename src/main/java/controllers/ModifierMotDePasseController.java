package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.SessionManager;

public class ModifierMotDePasseController {

    @FXML
    private PasswordField ancienMotDePasse, nouveauMotDePasse, confirmationMotDePasse;

    @FXML
    private Label messageLabel; // Label pour afficher les messages

    @FXML
    private void modifierMotDePasse() {
        String ancien = ancienMotDePasse.getText();
        String nouveau = nouveauMotDePasse.getText();
        String confirmation = confirmationMotDePasse.getText();

        // Vérification des champs vides
        if (ancien.isEmpty() || nouveau.isEmpty() || confirmation.isEmpty()) {
            messageLabel.setText("Tous les champs sont obligatoires.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        // Vérification des correspondances
        if (!nouveau.equals(confirmation)) {
            messageLabel.setText("Les nouveaux mots de passe ne correspondent pas.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        // Vérification de la longueur du mot de passe
        if (nouveau.length() < 6) {
            messageLabel.setText("Le nouveau mot de passe doit contenir au moins 6 caractères.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        try {
            User user = SessionManager.getCurrentUser();
            UserService service = new UserService();

            boolean result = service.modifierMotDePasse(user.getId(), ancien, nouveau);

            if (result) {
                messageLabel.setText("Mot de passe modifié avec succès !");
                messageLabel.setTextFill(Color.GREEN);
                // Optionnel : Vider les champs après succès
                ancienMotDePasse.clear();
                nouveauMotDePasse.clear();
                confirmationMotDePasse.clear();
            } else {
                messageLabel.setText("Ancien mot de passe incorrect.");
                messageLabel.setTextFill(Color.RED);
            }

        } catch (Exception e) {
            messageLabel.setText("Une erreur est survenue : " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    @FXML
    private void annulerAction(ActionEvent event) {
        Stage stage = (Stage) confirmationMotDePasse.getScene().getWindow();
        stage.close();
    }
}
