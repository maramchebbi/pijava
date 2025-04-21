package controller;

import Models.CeramicCollection;
import Models.Oeuvre;

import Services.OeuvreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Set;
import java.io.File;
import java.sql.SQLException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

// Pour SQLException
import java.sql.SQLException;
import java.io.IOException;

// Pour les autres éléments fréquemment utilisés avec ces classes
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.event.ActionEvent;
public class edit {

    @FXML
    private ImageView imageView;

    @FXML
    private TextField nomField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField matiereField;

    @FXML
    private TextField couleurField;

    @FXML
    private TextField dimensionField;

    @FXML
    private TextField useridtextfield;

    @FXML
    private TextField categorieField;

//    @FXML
//    private TextField collectionIdField; // Nouveau champ pour l'ID de la collection

    private File selectedImageFile = null;

    private Oeuvre currentOeuvre;
    private final OeuvreService oeuvreService = new OeuvreService();

    public void setOeuvreDetails(Oeuvre oeuvre) {
        this.currentOeuvre = oeuvre;

        if (oeuvre != null) {
            nomField.setText(oeuvre.getNom());
            typeField.setText(oeuvre.getType());
            descriptionField.setText(oeuvre.getDescription());
            matiereField.setText(oeuvre.getMatiere());
            couleurField.setText(oeuvre.getCouleur());
            dimensionField.setText(oeuvre.getDimensions());
            useridtextfield.setText(String.valueOf(oeuvre.getUser_id()));
            categorieField.setText(oeuvre.getCategorie());

            if (oeuvre.getImage() != null && !oeuvre.getImage().isEmpty()) {
                imageView.setImage(new Image("file:" + oeuvre.getImage()));
            } else {
                imageView.setImage(null);
            }
        }
    }

    @FXML
    void newimageaction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers image", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void saveChanges(ActionEvent event) {
        if (currentOeuvre == null) {
            showAlert("Erreur", "Aucune œuvre sélectionnée pour modification.");
            return;
        }

        String nom = nomField.getText().trim();
        String type = typeField.getText().trim();
        String description = descriptionField.getText().trim();
        String matiere = matiereField.getText().trim();
        String couleur = couleurField.getText().trim();
        String dimensions = dimensionField.getText().trim();
        int userId = Integer.parseInt(useridtextfield.getText().trim());
        String categorie = categorieField.getText().trim();

        String imagePath = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : currentOeuvre.getImage();

        Oeuvre updatedOeuvre = new Oeuvre(
                currentOeuvre.getId(),
                nom,
                type,
                description,
                matiere,
                couleur,
                dimensions,
                imagePath,
                categorie,
                userId
        );

        try {
            oeuvreService.update(updatedOeuvre);

            // Afficher l'alerte de succès
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("L'œuvre a été mise à jour avec succès !");

            // Ajouter un bouton OK et attendre le clic
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Charger la vue details.fxml
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
                        Parent root = loader.load();

                        // Passer les données mises à jour
                        detail detailController = loader.getController();
                        detailController.setOeuvreDetails(updatedOeuvre);

                        // Afficher la nouvelle scène
                        Stage stage = (Stage) nomField.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Impossible d'ouvrir la vue des détails");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour.");
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    private void cancelChanges(ActionEvent event) {
        // Simply close the window or navigate back
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void refreshUI(Oeuvre updatedOeuvre) {
        // Mise à jour de l'image
        Image newImage = new Image("file:" + updatedOeuvre.getImage());  // Remplace avec le chemin correct de l'image
        imageView.setImage(newImage);  // imageView est l'ImageView de ton fichier FXML

        // Mise à jour des champs de texte
        nomField.setText(updatedOeuvre.getNom());
        typeField.setText(updatedOeuvre.getType());
        descriptionField.setText(updatedOeuvre.getDescription());
        matiereField.setText(updatedOeuvre.getMatiere());
        couleurField.setText(updatedOeuvre.getCouleur());
        dimensionField.setText(updatedOeuvre.getDimensions());
          // Assure-toi que c'est bien l'attribut correct
        categorieField.setText(updatedOeuvre.getCategorie());
    }
    public void onOeuvreUpdated(Oeuvre updatedOeuvre) {
        // Appelle la méthode pour rafraîchir l'interface
        refreshUI(updatedOeuvre);
    }
}
