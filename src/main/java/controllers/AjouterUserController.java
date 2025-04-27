package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterUserController {

    @FXML
    private TextField EmailTextField;

    @FXML
    private TextField GenreTextField;

    @FXML
    private TextField NomTextField;

    @FXML
    private Label NomErrorLabel;

    @FXML
    private Label PrenomErrorLabel;

    @FXML
    private Label GenreErrorLabel;

    @FXML
    private Label EmailErrorLabel;

    @FXML
    private Label PasswordErrorLabel;

    @FXML
    private TextField PrenomTextField;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private TextField VisiblePasswordField;

    @FXML
    private Button toggleVisibilityBtn;

    private boolean passwordVisible = false;

    @FXML
    private ComboBox<String> GenreComboBox;

    @FXML
    private ComboBox<String> RoleComboBox;

    @FXML
    void ajouterAction(ActionEvent event) {
        clearErrors();

        String nom = NomTextField.getText().trim();
        String prenom = PrenomTextField.getText().trim();
        String genre = GenreComboBox.getValue();
        String role = RoleComboBox.getValue();
        String email = EmailTextField.getText().trim();
        String rawPassword = passwordVisible ? VisiblePasswordField.getText() : PasswordField.getText().trim();

        boolean hasError = false;

        // Validation standard
        if (nom.isEmpty()) {
            NomErrorLabel.setText("Nom requis");
            hasError = true;
        } else if (nom.length() < 2) {
            NomErrorLabel.setText("Min. 2 caractères");
            hasError = true;
        }

        if (prenom.isEmpty()) {
            PrenomErrorLabel.setText("Prénom requis");
            hasError = true;
        } else if (prenom.length() < 2) {
            PrenomErrorLabel.setText("Min. 2 caractères");
            hasError = true;
        }

        if (genre == null || genre.isEmpty()) {
            GenreErrorLabel.setText("Genre requis");
            hasError = true;
        }

        if (role == null || role.isEmpty()) {
            GenreErrorLabel.setText("Rôle requis");
            hasError = true;
        }

        if (email.isEmpty()) {
            EmailErrorLabel.setText("Email requis");
            hasError = true;
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            EmailErrorLabel.setText("Format invalide");
            hasError = true;
        } else {
            UserService userService = new UserService();
            try {
                if (userService.emailExists(email)) {
                    EmailErrorLabel.setText("Email déjà utilisé");
                    hasError = true;
                }
            } catch (SQLException e) {
                EmailErrorLabel.setText("Erreur DB");
                hasError = true;
            }
        }

        if (rawPassword.isEmpty()) {
            PasswordErrorLabel.setText("Mot de passe requis");
            hasError = true;
        } else if (rawPassword.length() < 6) {
            PasswordErrorLabel.setText("Min. 6 caractères");
            hasError = true;
        }

        if (hasError) return;

        // Ajouter l'utilisateur
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        User user = new User(nom, prenom, genre, email, hashedPassword, role,true);
        user.setRole(role); // Assurez-vous que votre modèle `User` accepte le rôle

        UserService userService = new UserService();
        try {
            userService.add(user);
            if (userAddedCallback != null) {
                userAddedCallback.onUserAdded(user);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailUser.fxml"));
            Parent root = loader.load();
            DetailUserController detailUserController = loader.getController();

            detailUserController.setNom(nom);
            detailUserController.setPrenom(prenom);
            detailUserController.setGenre(genre);
            detailUserController.setEmail(email);
            detailUserController.setPassword(hashedPassword);
            detailUserController.setRole(role);

            NomTextField.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void annulerAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur retour arrière : " + e.getMessage());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            VisiblePasswordField.setText(PasswordField.getText());
            VisiblePasswordField.setVisible(true);
            VisiblePasswordField.setManaged(true);
            PasswordField.setVisible(false);
            PasswordField.setManaged(false);
            toggleVisibilityBtn.setText("🙈");
        } else {
            PasswordField.setText(VisiblePasswordField.getText());
            PasswordField.setVisible(true);
            PasswordField.setManaged(true);
            VisiblePasswordField.setVisible(false);
            VisiblePasswordField.setManaged(false);
            toggleVisibilityBtn.setText("👁");
        }
    }

    @FXML
    public void initialize() {
        GenreComboBox.setStyle("-fx-background-color: white;");
        GenreComboBox.setOnAction(event -> {
            String selected = GenreComboBox.getValue();
            if ("Homme".equals(selected)) {
                GenreComboBox.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
            } else if ("Femme".equals(selected)) {
                GenreComboBox.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white;");
            }
        });

        // Init Role ComboBox avec valeurs (value logique)
        RoleComboBox.getItems().addAll("admin", "membre", "artiste");
        RoleComboBox.setPromptText("Sélectionner le rôle");
    }

    private void clearErrors() {
        NomErrorLabel.setText("");
        PrenomErrorLabel.setText("");
        GenreErrorLabel.setText("");
        EmailErrorLabel.setText("");
        PasswordErrorLabel.setText("");
    }
    public interface UserAddedCallback {
        void onUserAdded(User user);
    }

    private UserAddedCallback userAddedCallback;

    public void setOnUserAddedCallback(UserAddedCallback callback) {
        this.userAddedCallback = callback;
    }


}
