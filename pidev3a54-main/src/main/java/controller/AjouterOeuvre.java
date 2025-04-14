package controller;

import Models.oeuvre;
import Services.OeuvreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterOeuvre {


    @FXML
    private ImageView Artisferaimage;

    @FXML
    private TextField categoriefield;

    @FXML
    private ComboBox<?> collectionIdOField;

    @FXML
    private TextField couleurOField;

    @FXML
    private TextField createurOField;

    @FXML
    private TextArea descriptionOField;

    @FXML
    private TextField dimensionOField;

    @FXML
    private TextField iduserOfield;

    @FXML
    private TextField matiereOField;

    @FXML
    private TextField nomOField;

    @FXML
    private ImageView oeuvreImageView;

    @FXML
    private TextField typeOField;

    @FXML
    void ajouterOeuvre(ActionEvent event) {
        // Retrieve data from FXML fields
        String nom = nomOField.getText().trim();
        String type = typeOField.getText().trim();
        String description = descriptionOField.getText().trim();
        String matiere = matiereOField.getText().trim();
        String couleur = couleurOField.getText().trim();
        String dimension = dimensionOField.getText().trim();
        String createur = createurOField.getText().trim();
        String categorie = categoriefield.getText().trim();

        // Validate that fields are not empty
        if (nom.isEmpty() || type.isEmpty() || description.isEmpty() || matiere.isEmpty() ||
                couleur.isEmpty() || dimension.isEmpty() || createur.isEmpty() || categorie.isEmpty() ||
                iduserOfield.getText().trim().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Champs manquants");
            alert.setContentText("Veuillez remplir tous les champs.");
            alert.show();
            return;
        }

        // Retrieve user ID
        int userId;
        try {
            userId = Integer.parseInt(iduserOfield.getText().trim());
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Format incorrect !");
            alert.setContentText("L'ID utilisateur doit être numérique.");
            alert.show();
            return;
        }

        // Retrieve the image path from the ImageView
        String imagePath = "";
        if (Artisferaimage.getImage() != null) {
            Image image = Artisferaimage.getImage();
            imagePath = image.getUrl();
            if (imagePath != null && imagePath.startsWith("file:/")) {
                imagePath = imagePath.replace("file:/", "");
            }
        }

        // Create the Oeuvre object
        oeuvre oeuvreObj = new oeuvre(
                0, // Assuming 0 for ID, it will be auto-generated in the DB
                0, // ceramicCollectionId (default, you can adjust as needed)
                userId, nom, type, description, matiere, couleur, dimension, createur, imagePath, categorie
        );

        // Create the Oeuvre service
        OeuvreService oeuvreService = new OeuvreService();

        // Attempt to add the Oeuvre with exception handling
        try {
            oeuvreService.add(oeuvreObj);

            // Display success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Oeuvre ajoutée avec succès !");
            alert.show();

            // Optionally, redirect to another screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/showOeuvre.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (SQLException | IOException e) { // Catch both SQLException and IOException
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur lors de l'ajout");
            alert.setHeaderText("Impossible d'ajouter l'oeuvre");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    @FXML
    void uploadimageO(ActionEvent event) {
        // Create a FileChooser to choose the image file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image pour l'oeuvre");

        // Add a filter for image files only
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Open the file dialog
        Stage stage = (Stage) oeuvreImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Display the image in the ImageView (assuming you have an ImageView for the oeuvre image)
                Image image = new Image(file.toURI().toString());
                oeuvreImageView.setImage(image); // Assuming you have an ImageView named oeuvreImageView

                // Save the image path or the file to the database
                String imagePath = file.getAbsolutePath(); // You can choose to store the path or handle it differently

                // Create a new oeuvre object (you can get the other fields from the UI, such as name, type, etc.)
                oeuvre nouvelleOeuvre = new oeuvre(0, 2, 1, "Nom Oeuvre", "Type", "Description", "Matière", "Couleur",
                        "Dimensions", "Createur", imagePath, "Categorie");

                // Add the new oeuvre to the database (assuming you have a service like OeuvreService)
                OeuvreService oeuvreService = new OeuvreService();
                oeuvreService.add(nouvelleOeuvre);

            } catch (Exception e) {
                // Show an error message if something goes wrong
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors du chargement de l'image");
                alert.setContentText("Impossible de charger l'image sélectionnée : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    void viewoeuvre(ActionEvent event) {
        }

    }


