package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.EmailService;

import java.io.IOException;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Label statusLabel;

    @FXML
    private void handleSendEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            statusLabel.setText("Veuillez entrer votre email.");
            return;
        }

        // Vérifier si l'email existe dans la base de données
        if (EmailService.getOldPasswordFromDB(email) != null) {
            // Envoie de l'ancien mot de passe par email
            EmailService.sendOldPassword(email);
            statusLabel.setText("Un email vous a été envoyé avec votre ancien mot de passe !");
        } else {
            statusLabel.setText("Email introuvable.");
        }
    }

    public void handleBackToLogin(ActionEvent actionEvent) {  try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showMessage("Erreur lors du retour", "error");
    }
    }

    private void showMessage(String erreurLorsDuRetour, String error) {
    }
}
