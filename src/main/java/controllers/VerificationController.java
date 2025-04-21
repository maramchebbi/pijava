package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.UserService;
import utils.EmailSender;

import java.io.IOException;
import java.sql.SQLException;

public class VerificationController {
    @FXML private TextField codeField;
    @FXML private Label messageLabel;

    private String userEmail;
    private UserService userService = new UserService();

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    @FXML
    private void handleVerify() {
        String code = codeField.getText().trim();

        if (code.isEmpty()) {
            showMessage("Veuillez entrer le code", "error");
            return;
        }

        try {
            boolean verified = userService.verifyUser(userEmail, code);
            if (verified) {
                showMessage("Compte vérifié avec succès!", "success");
                redirectToLogin();
            } else {
                showMessage("Code incorrect. Veuillez réessayer.", "error");
            }
        } catch (SQLException e) {
            showMessage("Erreur lors de la vérification", "error");
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleResendCode() {
        try {
            String newCode = generateRandomCode();
            userService.updateVerificationCode(userEmail, newCode);
            EmailSender.sendVerificationEmail(userEmail, newCode);
            showMessage("Nouveau code envoyé à votre email!", "success");
        } catch (SQLException e) {
            showMessage("Erreur lors de l'envoi du code", "error");
            e.printStackTrace();
        }
    }

    private String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); // Code 6 chiffres
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + ("error".equals(type) ? "#e74c3c" : "#2ecc71"));
    }

    private void redirectToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) codeField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Connexion");
        stage.show();
    }
}