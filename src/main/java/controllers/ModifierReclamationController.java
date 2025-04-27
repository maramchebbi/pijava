package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.Reclamation;
import service.ReclamationService;

import java.sql.SQLException;

public class ModifierReclamationController {

    @FXML
    private ComboBox<String> optionCombo;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label statusLabel;

    private Reclamation reclamation; // La réclamation à modifier

    @FXML
    public void initialize() {
        optionCombo.getItems().addAll("Technique", "Service", "Facturation", "Autre");
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;

        // Préremplir les champs
        optionCombo.setValue(reclamation.getOption());
        descriptionArea.setText(reclamation.getDescription());
    }

    @FXML
    private void handleModifierReclamation(ActionEvent event) {
        String option = optionCombo.getValue();
        String description = descriptionArea.getText().trim();

        if (option == null || description.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        reclamation.setOption(option);
        reclamation.setDescription(description);

        try {
            ReclamationService service = new ReclamationService();
            service.update(reclamation);
            statusLabel.setText("Réclamation modifiée avec succès !");

            // ✅ Fermer la fenêtre de modification
            ((Stage) statusLabel.getScene().getWindow()).close();

        } catch (SQLException e) {
            statusLabel.setText("Erreur lors de la modification !");
            e.printStackTrace();
        }
    }
    /**
     * Gère l'action du bouton retour.
     * Ferme la fenêtre actuelle pour revenir à l'écran précédent.
     */
    @FXML
    private void handleBack() {
        // Fermer la fenêtre de modification
        ((Stage) statusLabel.getScene().getWindow()).close();
    }

    /**
     * Gère l'action du bouton annuler.
     * Demande confirmation avant de fermer la fenêtre.
     */
    @FXML
    private void handleCancel() {
        // Afficher une boîte de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Annuler les modifications");
        alert.setContentText("Êtes-vous sûr de vouloir annuler vos modifications ?");

        // Si l'utilisateur clique sur OK, fermer la fenêtre
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ((Stage) statusLabel.getScene().getWindow()).close();
            }
        });
    }


}

