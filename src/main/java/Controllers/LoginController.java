package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Models.User;
import Services.UserService;
import Utils.SessionManager;
import Utils.SessionStorage;

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
        System.out.println("enaa houni1");

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs !");
            return;
        }
        try {
            System.out.println(email);
            UserService userService = new UserService();
            User user = userService.login(email, password);
               System.out.println("userr is "+user);
            if (user != null) {
                System.out.println("ena houni 2");
                // Connexion réussie ✅
                SessionManager.setCurrentUser(user);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
                System.out.println("ena houni 3");

                Parent root = loader.load();
                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Bienvenue " + user.getPrenom());
                SessionManager.setCurrentUser(user);
                SessionStorage.saveSession(user); // <--- Sauvegarde la session
                stage.show();
                System.out.println("enaa houni");
            } else {
                errorLabel.setText("Email ou mot de passe incorrect.");
            }

        } catch (SQLException | IOException e) {
            System.out.println(e.getMessage());
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
        passwordVisible = !passwordVisible; // Inverse l'état de visibilité

        if (passwordVisible) {
            // Afficher le mot de passe en texte clair et masquer le champ de mot de passe avec des points
            visiblePasswordField.setText(passwordField.getText()); // Copie le texte du champ de mot de passe
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            toggleVisibilityBtn.setText("🙈"); // Change l'icône en "🙈" (cacher)
        } else {
            // Masquer le mot de passe en texte clair et afficher le champ de mot de passe avec des points
            passwordField.setText(visiblePasswordField.getText()); // Copie le texte du champ visible dans le champ masqué
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            toggleVisibilityBtn.setText("👁"); // Change l'icône en "👁" (voir)
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

    private java.util.function.Consumer<User> loginSuccessHandler;

    public void setLoginSuccessHandler(java.util.function.Consumer<User> handler) {
        this.loginSuccessHandler = handler;
    }


}
