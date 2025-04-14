package Controllers;

import Models.Style;
import Services.StyleService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import javafx.scene.Parent;

import java.awt.*;
import java.io.File;
import java.sql.SQLException;

public class AjouterStyleController {

    private Style styleToEdit; // Variable pour stocker le style à éditer

    @FXML private TextField typeTextField;
    @FXML private TextField decriptionTextField;
    @FXML private TextField tableauexTextField;
    @FXML private ImageView imageView;
    @FXML
    private Label lbDesc;

    @FXML
    private Label lbT;

    @FXML
    private Label lbTex;

    private StyleService styleService;
    private AfficherStyleController parentController;

    public void setParentController(AfficherStyleController parentController) {
        this.parentController = parentController;
    }


    public AjouterStyleController() {
        styleService = new StyleService();
    }

    @FXML
    private void handleParcourirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(typeTextField.getScene().getWindow());

        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            tableauexTextField.setText(imagePath); // Met le chemin dans le champ texte
            imageView.setImage(new Image("file:" + imagePath)); // Affiche l'image
        }
    }

    // Méthode pour définir le style à éditer
    public void setStyleToEdit(Style style) {
        this.styleToEdit = style;

        // Remplir les champs avec les données du style existant
        typeTextField.setText(style.getType());
        decriptionTextField.setText(style.getDescription());
        tableauexTextField.setText(style.getExtab());

        // Si l'image existe, l'afficher dans l'ImageView
        if (style.getExtab() != null && !style.getExtab().isEmpty()) {
            Image image = new Image("file:" + style.getExtab());
            imageView.setImage(image);
        }
    }

    // Méthode pour ajouter un style
    @FXML
    public void handleValider(ActionEvent event) {
        String type = typeTextField.getText();
        String description = decriptionTextField.getText();
        String imageUrl = tableauexTextField.getText();

        boolean valid = true;
        lbT.setText("");
        lbDesc.setText("");
        lbTex.setText("");

        // Contrôle sur le type : minimum 3 caractères, uniquement des lettres
        if (type.isEmpty()) {
            lbT.setText("Type requis.");
            valid = false;
        } else if (type.length() < 3) {
            lbT.setText("Le type doit contenir au moins 3 caractères.");
            valid = false;
        } else if (!type.matches("[a-zA-Z]+")) { // Vérifier que le type contient uniquement des lettres
            lbT.setText("Le type doit contenir uniquement des lettres.");
            valid = false;
        }

        // Contrôle sur la description : minimum 10 caractères
        if (description.isEmpty()) {
            lbDesc.setText("Description requise.");
            valid = false;
        } else if (description.length() < 10) {
            lbDesc.setText("La description doit contenir au moins 10 caractères.");
            valid = false;
        }

        // Contrôle sur l'URL de l'image : non vide
        if (imageUrl.isEmpty()) {
            lbTex.setText("Image requise.");
            valid = false;
        }

        // Si une erreur est présente, on arrête l'exécution
        if (!valid) return;

        Style newStyle = new Style(type, description, imageUrl);

        try {
            if (styleToEdit != null) {
                newStyle.setId(styleToEdit.getId());
                styleService.update(newStyle);
                System.out.println("Style modifié avec succès.");
            } else {
                styleService.add(newStyle);
                System.out.println("Style ajouté avec succès.");
            }

            // 🔁 Recharge dynamique de la grille
            if (parentController != null) {
                parentController.loadStyles();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) typeTextField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erreur lors de l'ajout/édition du style.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur SQL");
            alert.setHeaderText("Impossible d'ajouter/éditer le style");
            alert.setContentText("Une erreur est survenue. Veuillez réessayer.");
            alert.showAndWait();
        }
    }


}

