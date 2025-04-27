package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Models.User;
import org.mindrot.jbcrypt.BCrypt;
import Services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> genreComboBox;
    @FXML private ComboBox<String> roleComboBox;  // Add ComboBox for Role
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField VisiblePasswordField;

    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label genreErrorLabel;
    @FXML private Label roleErrorLabel;  // Add Label for Role
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;

    @FXML private Label messageLabel;
    @FXML
    private Button toggleVisibilityBtn;  // Déclaration de votre bouton

    private boolean passwordVisible = false;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        genreComboBox.getItems().addAll("Homme", "Femme");
        roleComboBox.getItems().addAll("membre", "artiste");  // Initialize roles
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

    }

    @FXML
    private void handleRegisterAction(ActionEvent event) {
        clearErrors(); // Clear previous error messages

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String genre = genreComboBox.getValue();
        String role = roleComboBox.getValue();  // Get the selected role
        String email = emailField.getText().trim();
        String rawPassword = passwordVisible ? VisiblePasswordField.getText() : passwordField.getText().trim();

        boolean hasError = false;

        // Validation for nom (last name)
        if (nom.isEmpty()) {
            nomErrorLabel.setText("Nom requis");
            hasError = true;
        } else if (nom.length() < 2) {
            nomErrorLabel.setText("Min. 2 caractères");
            hasError = true;
        }

        // Validation for prenom (first name)
        if (prenom.isEmpty()) {
            prenomErrorLabel.setText("Prénom requis");
            hasError = true;
        } else if (prenom.length() < 2) {
            prenomErrorLabel.setText("Min. 2 caractères");
            hasError = true;
        }

        // Validation for genre (gender)
        if (genre == null || genre.isEmpty()) {
            genreErrorLabel.setText("Genre requis");
            hasError = true;
        }

        // Validation for role
        if (role == null || role.isEmpty()) {
            roleErrorLabel.setText("Rôle requis");
            hasError = true;
        }

        // Validation for email
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email requis");
            hasError = true;
        } else if (!isValidEmail(email)) {
            emailErrorLabel.setText("Format invalide");
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

        // Validation for password
        if (rawPassword.isEmpty()) {
            passwordErrorLabel.setText("Mot de passe requis");
            hasError = true;
        } else if (rawPassword.length() < 6) {
            passwordErrorLabel.setText("Min. 6 caractères");
            hasError = true;
        }

        if (hasError) return;  // Stop if there's any error

        // Register user
        try {
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            User user = new User(nom, prenom, genre, email, hashedPassword, role);  // Pass role to User constructor
            userService.add(user);

            // Redirect to login screen after successful registration
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();
            LoginController loginController = loader.getController();
            loginController.prefillLogin(email, rawPassword);

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Une erreur est survenue lors de l'inscription.", "error");
        }
    }

    private boolean isValidEmail(String email) {
        String regex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return Pattern.matches(regex, email);
    }

    private void clearErrors() {
        nomErrorLabel.setText("");
        prenomErrorLabel.setText("");
        genreErrorLabel.setText("");
        roleErrorLabel.setText("");  // Clear role error
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        messageLabel.setText("");
    }

    private void showMessage(String message, String type) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        if ("error".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: red;");
        } else if ("success".equals(type)) {
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    private void togglePasswordVisibility(ActionEvent event) {
        passwordVisible = !passwordVisible; // Inverse l'état de visibilité du mot de passe

        if (passwordVisible) {
            // Afficher le mot de passe en texte clair et masquer le champ de mot de passe avec des points
            VisiblePasswordField.setText(passwordField.getText()); // Copie le mot de passe dans le champ visible
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            VisiblePasswordField.setVisible(true);
            VisiblePasswordField.setManaged(true);
            toggleVisibilityBtn.setText("🙈");  // Change l'icône pour indiquer l'état actuel
        } else {
            // Masquer le mot de passe en texte clair et afficher le champ de mot de passe avec des points
            passwordField.setText(VisiblePasswordField.getText()); // Copie le mot de passe dans le champ masqué
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            VisiblePasswordField.setVisible(false);
            VisiblePasswordField.setManaged(false);
            toggleVisibilityBtn.setText("👁");  // Change l'icône pour indiquer l'état actuel
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
            System.out.println("Erreur retour arrière : " + e.getMessage());
        }
    }
}
