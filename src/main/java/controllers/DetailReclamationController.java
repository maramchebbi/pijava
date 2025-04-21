package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import models.Reclamation;

public class DetailReclamationController {

    @FXML
    private Label nomPrenomLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label optionLabel;
    @FXML
    private TextArea descriptionLabel;


    public void setReclamation(Reclamation reclamation) {
        nomPrenomLabel.setText("👤 Nom et prénom : " + reclamation.getUser().getNom() + " " + reclamation.getUser().getPrenom());
        emailLabel.setText("📧 Email : " + reclamation.getUser().getEmail());
        optionLabel.setText("📌 Option : " + reclamation.getOption());
        descriptionLabel.setText("📝 Description : " + reclamation.getDescription());
    }
}
