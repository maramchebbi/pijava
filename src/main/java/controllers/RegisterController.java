package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import service.EmailService;
import service.UserService;
import utils.EmailSender;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField VisiblePasswordField;

    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label genreErrorLabel;
    @FXML private Label roleErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;

    @FXML private Label messageLabel;
    @FXML private Button toggleVisibilityBtn;

    private boolean passwordVisible = false;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Homme", "Femme");
        roleComboBox.getItems().addAll("membre", "artiste");

        genreComboBox.setStyle("-fx-background-color: white;");
        genreComboBox.setOnAction(event -> {
            String selected = genreComboBox.getValue();
            if ("Homme".equals(selected)) {
                genreComboBox.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            } else if ("Femme".equals(selected)) {
                genreComboBox.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white;");
            } else {
                genreComboBox.setStyle("-fx-background-color: white; -fx-text-fill: black;");
            }
        });

        VisiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        VisiblePasswordField.setVisible(false);
        VisiblePasswordField.setManaged(false);
    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        clearErrors();

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String genre = genreComboBox.getValue();
        String role = roleComboBox.getValue();
        String email = emailField.getText().trim();
        String rawPassword = passwordVisible ? VisiblePasswordField.getText() : passwordField.getText().trim();

        boolean hasError = validateFields(nom, prenom, genre, role, email, rawPassword);
        if (hasError) return;

        registerUser(nom, prenom, genre, role, email, rawPassword);
    }

    private boolean validateFields(String nom, String prenom, String genre, String role, String email, String password) {
        boolean hasError = false;

        if (nom.isEmpty() || nom.length() < 2) {
            nomErrorLabel.setText(nom.isEmpty() ? "Nom requis" : "Min. 2 caractères");
            hasError = true;
        }

        if (prenom.isEmpty() || prenom.length() < 2) {
            prenomErrorLabel.setText(prenom.isEmpty() ? "Prénom requis" : "Min. 2 caractères");
            hasError = true;
        }

        if (genre == null || genre.isEmpty()) {
            genreErrorLabel.setText("Genre requis");
            hasError = true;
        }

        if (role == null || role.isEmpty()) {
            roleErrorLabel.setText("Rôle requis");
            hasError = true;
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            emailErrorLabel.setText(email.isEmpty() ? "Email requis" : "Format invalide");
            hasError = true;
        } else {
            try {
                if (userService.emailExists(email)) {
                    emailErrorLabel.setText("Email déjà utilisé");
                    hasError = true;
                }
            } catch (SQLException e) {
                emailErrorLabel.setText("Erreur DB");
                hasError = true;
            }
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordErrorLabel.setText(password.isEmpty() ? "Mot de passe requis" : "Min. 6 caractères");
            hasError = true;
        }

        return hasError;
    }

    private void registerUser(String nom, String prenom, String genre, String role, String email, String rawPassword) {
        try {
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            String verificationCode = String.valueOf((int)(Math.random() * 900000) + 100000);

            User user = new User(nom, prenom, genre, email, hashedPassword, role, false);
            user.setVerificationCode(verificationCode);
            userService.add(user);

            // Envoyer le code de vérification
            EmailSender.sendVerificationEmail(email, verificationCode);

            // Rediriger vers la page de vérification
            redirectToVerification(email);
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Erreur lors de l'inscription: " + e.getMessage(), "error");
        }
    }

    private void redirectToVerification(String email) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Verification.fxml"));
        Parent root = loader.load();
        VerificationController controller = loader.getController();
        controller.setUserEmail(email);

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Vérification par Email");
        stage.show();
    }
    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            VisiblePasswordField.setVisible(true);
            VisiblePasswordField.setManaged(true);
            toggleVisibilityBtn.setText("🙈");
            toggleVisibilityBtn.setStyle("-fx-font-size: 20px;");
        } else {
            VisiblePasswordField.setVisible(false);
            VisiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            toggleVisibilityBtn.setText("👁");
            toggleVisibilityBtn.setStyle("-fx-font-size: 20px;");
        }
    }

    @FXML
    public void annulerAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showMessage("Erreur lors du retour", "error");
        }
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", email);
    }

    private void clearErrors() {
        nomErrorLabel.setText("");
        prenomErrorLabel.setText("");
        genreErrorLabel.setText("");
        roleErrorLabel.setText("");
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        messageLabel.setText("");
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setStyle("-fx-text-fill: " + ("error".equals(type) ? "#e74c3c" : "#2ecc71") + ";");
    }
}
