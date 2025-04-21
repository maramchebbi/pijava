package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.EmailSender;
import utils.SessionManager;
import utils.SessionStorage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField VisiblePasswordField;  // Champ pour afficher le mot de passe en texte clair

    @FXML
    private Button toggleVisibilityBtn;  // Bouton pour basculer la visibilité

    private boolean passwordVisible = false;  // Variable pour suivre l'état de visibilité du mot de passe

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            UserService userService = new UserService();
            User user = userService.login(email, password);

            if (user != null) {
                // Vérifier d'abord si l'utilisateur est vérifié
                if (!user.isVerified()) {
                    errorLabel.setText("Veuillez vérifier votre adresse e-mail avant de vous connecter.");
                    return;
                }

                // Si l'utilisateur est vérifié, continuer avec la connexion
                SessionManager.setCurrentUser(user);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Bienvenue " + user.getPrenom());
                SessionManager.setCurrentUser(user);
                SessionStorage.saveSession(user); // Sauvegarde la session
                EmailSender.sendLoginNotification(user.getEmail(), user.getPrenom());
                stage.show();
            } else {
                // Si l'utilisateur n'existe pas ou les informations sont incorrectes
                errorLabel.setText("Email ou mot de passe incorrect.");
            }
        } catch (SQLException | IOException e) {
            errorLabel.setText("Erreur lors de la connexion.");
            e.printStackTrace();
        }
    }
    public void handleRegisterLinkAction(ActionEvent event) {
        try {
            // Charger le fichier FXML de la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène pour la page d'inscription
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur lors de la redirection vers la page d'inscription : " + e.getMessage());
        }
    }

    public void prefillLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
        SessionStorage.clearSession(); // Supprime le fichier
        SessionManager.logout(); // Vide la session en RAM
    }
    @FXML
    private TextField visiblePasswordField; // Champ de texte visible temporairement pour afficher le mot de passe

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Afficher en clair
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggleVisibilityBtn.setText("🙈");
        } else {
            // Afficher masqué
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            toggleVisibilityBtn.setText("👁");
        }
    }
    @FXML
    private void handleForgotPasswordLink(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ForgotPassword.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mot de passe oublié");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void initialize() {
        // Lier les propriétés textuelles
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Configurer l'état initial
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
    }


}