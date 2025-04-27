package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import service.FacebookSimpleAuthService;
import service.GoogleAuthService;
import service.UserService;
import utils.EmailSender;
import utils.SessionManager;
import utils.SessionStorage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private TextField visiblePasswordField;
    @FXML private Button toggleVisibilityBtn;
    @FXML private Button googleLoginBtn;
    @FXML private Button facebookLoginBtn;
    private final GoogleAuthService googleAuthService = new GoogleAuthService();
    private final FacebookSimpleAuthService facebookAuthService = new FacebookSimpleAuthService();

    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        // Lier les propriétés textuelles entre les deux champs
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Configurer l'état initial
        visiblePasswordField.setVisible(false);
        passwordField.setVisible(true);
        googleLoginBtn.setOnAction(this::handleGoogleLogin);
        facebookLoginBtn.setOnAction(this::handleFacebookLogin);
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            // Passage au mode visible
            visiblePasswordField.setText(passwordField.getText());

            // Animation de transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), passwordField);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), visiblePasswordField);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition transition = new ParallelTransition(fadeOut, fadeIn);
            transition.setOnFinished(e -> {
                passwordField.setVisible(false);
                visiblePasswordField.setVisible(true);
            });
            transition.play();

            toggleVisibilityBtn.setText("🙈");
        } else {
            // Retour au mode masqué
            passwordField.setText(visiblePasswordField.getText());

            // Animation de transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), visiblePasswordField);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), passwordField);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition transition = new ParallelTransition(fadeOut, fadeIn);
            transition.setOnFinished(e -> {
                visiblePasswordField.setVisible(false);
                passwordField.setVisible(true);
            });
            transition.play();

            toggleVisibilityBtn.setText("👁");
        }
    }

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
                if (!user.isVerified()) {
                    errorLabel.setText("Veuillez vérifier votre adresse e-mail avant de vous connecter.");
                    return;
                }

                SessionManager.setCurrentUser(user);
                loadMainView(user, event);
            } else {
                errorLabel.setText("Email ou mot de passe incorrect.");
            }
        } catch (SQLException | IOException e) {
            errorLabel.setText("Erreur lors de la connexion.");
            e.printStackTrace();
        }
    }

    private void loadMainView(User user, ActionEvent event) throws IOException {
        // Choisir la vue en fonction du rôle
        String fxmlPath;
        String title;

        if ("admin".equalsIgnoreCase(user.getRole())) {
            // Si l'utilisateur est un administrateur
            fxmlPath = "/AfficherUser.fxml";
            title = "Panneau d'administration";
        } else {
            // Si l'utilisateur est un membre normal
            fxmlPath = "/Home.fxml";
            title = "Espace membre - " + user.getPrenom();
        }

        // Charger la vue appropriée
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Si nous chargeons la vue Home, nous devons initialiser les données utilisateur
        if (!fxmlPath.equals("/AfficherUser.fxml") && loader.getController() instanceof HomeController) {
            HomeController homeController = loader.getController();
            homeController.initData(user);
        }

        // Configurer et afficher la scène
        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);

        // Sauvegarder la session et envoyer une notification d'email
        SessionStorage.saveSession(user);
        EmailSender.sendLoginNotification(user.getEmail(), user.getPrenom());

        // Afficher la fenêtre
        stage.show();
    }
    @FXML
    private void handleRegisterLinkAction(ActionEvent event) {
        navigateToView("/Register.fxml", "Inscription", event);
    }

    @FXML
    private void handleForgotPasswordLink(ActionEvent event) {
        navigateToView("/ForgotPassword.fxml", "Mot de passe oublié", event);
    }

    private void navigateToView(String fxmlPath, String title, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur lors de la redirection: " + e.getMessage());
        }
    }

    public void prefillLogin(String email, String password) {
        emailField.setText(email);
        passwordField.setText(password);
        SessionStorage.clearSession();
        SessionManager.logout();
    }

    private void handleGoogleLogin(ActionEvent event) {
        googleAuthService.startGoogleAuth()
                .thenAccept(user -> {
                    // Sur le thread JavaFX
                    Platform.runLater(() -> {
                        try {
                            // Stocker l'utilisateur en session
                            SessionManager.setCurrentUser(user);
                            // Rediriger vers la page principale
                            loadMainView(user, event);
                        } catch (IOException e) {
                            errorLabel.setText("Erreur lors de la redirection: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(ex -> {
                    // Gérer les erreurs sur le thread JavaFX
                    Platform.runLater(() -> {
                        errorLabel.setText("Erreur d'authentification Google: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
    }

    private void handleFacebookLogin(ActionEvent event) {
        errorLabel.setText("Connexion à Facebook en cours...");

        facebookAuthService.startFacebookAuth()
                .thenAccept(user -> {
                    // Sur le thread JavaFX
                    Platform.runLater(() -> {
                        try {
                            // Stocker l'utilisateur en session
                            SessionManager.setCurrentUser(user);
                            // Rediriger vers la page principale
                            loadMainView(user, event);
                        } catch (IOException e) {
                            errorLabel.setText("Erreur lors de la redirection: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(ex -> {
                    // Gérer les erreurs sur le thread JavaFX
                    Platform.runLater(() -> {
                        errorLabel.setText("Erreur d'authentification Facebook: " + ex.getMessage());
                        ex.printStackTrace();
                    });
                    return null;
                });
    }
}