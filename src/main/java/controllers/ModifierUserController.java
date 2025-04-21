package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import service.UserService;

import java.sql.SQLException;

public class ModifierUserController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private CheckBox verifiedCheckBox;
    @FXML private Label messageLabel;

    private User user;
    private Runnable onUserUpdated;

    public void setUser(User user) {
        this.user = user;
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        verifiedCheckBox.setSelected(user.isVerified());
    }

    public void setOnUserUpdated(Runnable callback) {
        this.onUserUpdated = callback;
    }

    @FXML
    private void handleSave() throws SQLException {
        messageLabel.setText("");

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        boolean isVerified = verifiedCheckBox.isSelected();

        // Validation
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-z]{2,6}$")) {
            messageLabel.setText("Adresse email invalide.");
            return;
        }

        // Mise à jour de l'utilisateur
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setVerified(isVerified);

        UserService userService = new UserService();
        userService.update(user);

        if (onUserUpdated != null) {
            onUserUpdated.run();
        }

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void annulerAction(ActionEvent event) {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}