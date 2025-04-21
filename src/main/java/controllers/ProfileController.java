package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;
import javafx.scene.control.Button;

import java.io.IOException;

public class ProfileController {

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
    private Button modifierBtn, sauvegarderBtn;

    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        NomDetailTextField.setText(user.getNom());
        PrenomDetailTextField.setText(user.getPrenom());
        GenreDetailTextField.setText(user.getGenre());
        EmailDetailTextField.setText(user.getEmail());
        PasswordDetailTextField.setText(user.getPassword());
        RoleDetailTextField.setText(user.getRole());

        // Rendre tous les champs non éditables par défaut
        NomDetailTextField.setEditable(false);
        PrenomDetailTextField.setEditable(false);
        GenreDetailTextField.setEditable(false);
        EmailDetailTextField.setEditable(false);
        PasswordDetailTextField.setEditable(false);
        RoleDetailTextField.setEditable(false);

        sauvegarderBtn.setVisible(false);
        modifierBtn.setVisible(true);
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        // Rendre seulement certains champs éditables
        NomDetailTextField.setEditable(true);
        PrenomDetailTextField.setEditable(true);
        GenreDetailTextField.setEditable(false);
        EmailDetailTextField.setEditable(false);

        // Garder ceux-ci non modifiables
        PasswordDetailTextField.setEditable(false);
        RoleDetailTextField.setEditable(false);

        // Gérer les boutons
        sauvegarderBtn.setVisible(true);
        modifierBtn.setVisible(false);
    }

    @FXML
    private void handleSauvegarder(ActionEvent event) {
        // Ici tu peux sauvegarder les nouvelles infos dans la base de données
        // Par exemple :
        // userService.update(new User(...))

        // Repasser tous les champs en lecture seule
        NomDetailTextField.setEditable(false);
        PrenomDetailTextField.setEditable(false);
        GenreDetailTextField.setEditable(false);
        EmailDetailTextField.setEditable(false);
        PasswordDetailTextField.setEditable(false);
        RoleDetailTextField.setEditable(false);

        // Afficher à nouveau le bouton Modifier
        sauvegarderBtn.setVisible(false);
        modifierBtn.setVisible(true);

        System.out.println("Profil mis à jour !");
    }

    @FXML
    private void annulerAction(ActionEvent event) {
        Stage stage = (Stage) NomDetailTextField.getScene().getWindow();
        stage.close();
    }
}
