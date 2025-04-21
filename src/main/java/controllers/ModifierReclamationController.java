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

}
