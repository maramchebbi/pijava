package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.Reclamation;
import models.User;
import service.ReclamationService;
import utils.BadWordsAPI;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class AjouterReclamationController {

    @FXML
    private ComboBox<String> optionCombo;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Ajouter des options à la ComboBox
        optionCombo.getItems().addAll("Technique", "Service", "Facturation", "Autre");
    }

    @FXML
    private void handleAjouterReclamation(ActionEvent event) {
        String option = optionCombo.getValue();
        String description = descriptionArea.getText().trim();
        description = BadWordsAPI.filterBadWords(description); // Appel API pour censurer

        if (option == null || description.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            statusLabel.setText("Aucun utilisateur connecté !");
            return;
        }

        Reclamation reclamation = new Reclamation(option, description, currentUser);
        ReclamationService service = new ReclamationService();

        try {
            service.add(reclamation);
            statusLabel.setText("Réclamation ajoutée avec succès !");

            // Vider les champs après ajout
            optionCombo.setValue(null);
            descriptionArea.clear();

            // Redirection vers AfficherReclamation.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Changer la scène pour afficher les réclamations
            stage.setScene(new Scene(root));
            stage.show();

        } catch (SQLException e) {
            statusLabel.setText("Erreur lors de l'ajout !");
            e.printStackTrace();
        } catch (IOException e) {
            statusLabel.setText("Erreur de redirection !");
            e.printStackTrace();
        }
    }

    public void handleRetour(ActionEvent event) {
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                System.out.println("Erreur retour arrière : " + e.getMessage());
            }
        }
    }
}