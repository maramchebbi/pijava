package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DetailUserController implements Initializable {

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

    @FXML
    private Text userName;

    @FXML
    private Text userEmail;

    @FXML
    private Text userRole;

    @FXML
    private Text userInitials;

    // Private fields to store user info
    private String nom;
    private String prenom;
    private String email;
    private String role;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Default values will be overridden when setters are called
    }

    /**
     * Updates all UI elements with the current user data
     */
    private void updateUIWithUserData() {
        // Update header displays
        if (userName != null && nom != null && prenom != null) {
            userName.setText(prenom + " " + nom);
        }

        if (userEmail != null && email != null) {
            userEmail.setText(email);
        }

        if (userRole != null && role != null) {
            userRole.setText(role);
        }

        if (userInitials != null && nom != null && prenom != null && !nom.isEmpty() && !prenom.isEmpty()) {
            userInitials.setText(String.valueOf(prenom.charAt(0)) + String.valueOf(nom.charAt(0)));
        }
    }

    // ----- Méthodes pour remplir les champs depuis un autre contrôleur -----

    public void setNom(String nom) {
        this.nom = nom;
        NomDetailTextField.setText(nom);
        updateUIWithUserData();
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
        PrenomDetailTextField.setText(prenom);
        updateUIWithUserData();
    }

    public void setGenre(String genre) {
        GenreDetailTextField.setText(genre);
    }

    public void setEmail(String email) {
        this.email = email;
        EmailDetailTextField.setText(email);
        updateUIWithUserData();
    }

    public void setPassword(String password) {
        PasswordDetailTextField.setText(password);
    }

    public void setRole(String role) {
        this.role = role;
        RoleDetailTextField.setText(role);
        updateUIWithUserData();
    }

    /**
     * Set all user data at once and update the UI
     */
    public void setUserData(String nom, String prenom, String genre, String email, String password, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;

        NomDetailTextField.setText(nom);
        PrenomDetailTextField.setText(prenom);
        GenreDetailTextField.setText(genre);
        EmailDetailTextField.setText(email);
        PasswordDetailTextField.setText(password);
        RoleDetailTextField.setText(role);

        updateUIWithUserData();
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