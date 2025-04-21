package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class DetailUserController {

    @FXML
    private TextField NomDetailTextField;

    @FXML
    private TextField PrenomDetailTextField;

    @FXML
    private TextField GenreDetailTextField;

    @FXML
    private TextField EmailDetailTextField;

    @FXML
    private TextField PasswordDetailTextField;

    @FXML
    private TextField RoleDetailTextField;

    // ----- Méthodes pour remplir les champs depuis un autre contrôleur -----

    public void setNom(String nom) {
        NomDetailTextField.setText(nom);
    }

    public void setPrenom(String prenom) {
        PrenomDetailTextField.setText(prenom);
    }

    public void setGenre(String genre) {
        GenreDetailTextField.setText(genre);
    }

    public void setEmail(String email) {
        EmailDetailTextField.setText(email);
    }

    public void setPassword(String password) {
        PasswordDetailTextField.setText(password);
    }

    public void setRole(String role) {
        RoleDetailTextField.setText(role);
    }

    // ----- Accesseurs optionnels -----

    public TextField getNomDetailTextField() {
        return NomDetailTextField;
    }

    public TextField getPrenomDetailTextField() {
        return PrenomDetailTextField;
    }

    public TextField getGenreDetailTextField() {
        return GenreDetailTextField;
    }

    public TextField getEmailDetailTextField() {
        return EmailDetailTextField;
    }

    public TextField getPasswordDetailTextField() {
        return PasswordDetailTextField;
    }

    public TextField getRoleDetailTextField() {
        return RoleDetailTextField;
    }

    public void annulerAction(ActionEvent event) {
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
}
