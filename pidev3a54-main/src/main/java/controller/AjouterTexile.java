package controller;

import Models.collection_t;
import Models.textile;
import Services.TextileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterTexile {

        @FXML
        private ImageView Artisferaimage;

        @FXML
        private ComboBox<collection_t> collectionIdField;

        @FXML
        private TextField couleurField;

        @FXML
        private TextField createurField;

        @FXML
        private TextArea descriptionField;

        @FXML
        private TextField dimensionField;

        @FXML
        private TextField iduserfield;

        @FXML
        private TextField matiereField;

        @FXML
        private TextField nomField;

        @FXML
        private TextField techniquefield;

        @FXML
        private ImageView textileimage;

        @FXML
        private TextField typeField;


        @FXML
        void ajouterTextile(ActionEvent event) {
                // Récupération des données depuis les champs FXML
                String nom = nomField.getText().trim();
                String type = typeField.getText().trim();
                String description = descriptionField.getText().trim();
                String matiere = matiereField.getText().trim();
                String dimension = dimensionField.getText().trim();
                String couleur = couleurField.getText().trim();
                String createur = createurField.getText().trim();
                String technique = techniquefield.getText().trim();

                // Validate that fields are not empty
                if (nom.isEmpty() || type.isEmpty() || description.isEmpty() || matiere.isEmpty() ||
                        dimension.isEmpty() || couleur.isEmpty() || createur.isEmpty() || technique.isEmpty() ||
                        iduserfield.getText().trim().isEmpty() || collectionIdField.getValue() == null) {

                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Champs manquants");
                        alert.setContentText("Veuillez remplir tous les champs.");
                        alert.show();
                        return;
                }

                // Vérification et conversion des IDs
                int userId;
                collection_t selectedCollection = collectionIdField.getValue(); // ✅ CORRECT
                int collectionId = selectedCollection.getId();
                String imagePath = "";

                // Ensure the userId and collectionId are valid integers
                try {
                        userId = Integer.parseInt(iduserfield.getText().trim());
                        if (selectedCollection == null) {
                                throw new NumberFormatException("Collection ID is null");
                        }
                } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Format incorrect !");
                        alert.setContentText("Les champs ID utilisateur et ID collection doivent être numériques.");
                        alert.show();
                        return;
                }

                // Récupération du chemin de l’image sélectionnée dans l’ImageView
                if (textileimage.getImage() != null) {
                        Image image = textileimage.getImage();
                        imagePath = image.getUrl();
                        if (imagePath != null && imagePath.startsWith("file:/")) {
                                imagePath = imagePath.replace("file:/", "");
                        }
                }

                // Création de l'objet textile
                textile textileObj = new textile(
                        collectionId, nom, type, description,
                        matiere, couleur, dimension, createur,
                        imagePath, technique, userId
                );

                // Création du service
                TextileService textileService = new TextileService();

                // Tentative d’ajout avec gestion des exceptions
                try {
                        textileService.add(textileObj);

                        // Display success message
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText("Textile ajouté avec succès !");
                        alert.show();

                        // Optionnel : Redirection vers un autre écran ou actualisation de l’interface
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();

                } catch (SQLException | IOException e) { // Catch both SQLException and IOException
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur lors de l'ajout");
                        alert.setHeaderText("Impossible d'ajouter le textile");
                        alert.setContentText(e.getMessage());
                        alert.show();
                }
        }


        public void viewtextile(ActionEvent actionEvent) {
                try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
                        Parent root = loader.load();

                        // njib fel current window
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                        // Create a new content
                        Scene scene = new Scene(root);

                        // Set the new content l mawjoud f scene
                        stage.setScene(scene);
                        stage.show();

                } catch (IOException e) {
                        System.out.println("Error loading the FXML file: " + e.getMessage());
                }
        }


        public void uploadimage(ActionEvent actionEvent) {
                // filechooser bech tekhtar fih l taswyra
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Sélectionner une image");

                // validartions mtaa l 'extension bel filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                        "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.bmp"
                );
                fileChooser.getExtensionFilters().add(extFilter);

                Stage stage = (Stage) textileimage.getScene().getWindow();
                File file = fileChooser.showOpenDialog(stage);

                if (file != null) {
                        try {
                                // thot les images fel image view
                                Image image = new Image(file.toURI().toString());
                                textileimage.setImage(image);
                        } catch (Exception e) {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Erreur");
                                alert.setHeaderText("Erreur lors du chargement de l'image");
                                alert.setContentText("Impossible de charger l'image sélectionnée : " + e.getMessage());
                                alert.showAndWait();
                        }
                }
        }

        @FXML
        public void initialize() {
                TextileService textileService = new TextileService();

                try {
                        // tjib les textiles kol
                        List<collection_t> collections = textileService.getAllCollections();

                        // taffichi les collection fel combobox
                        collectionIdField.getItems().addAll(collections);

                        // bech twary ken nom mtaa textile
                        collectionIdField.setCellFactory(lv -> new ListCell<collection_t>() {
                                @Override
                                protected void updateItem(collection_t item, boolean empty) {
                                        super.updateItem(item, empty);
                                        setText(empty || item == null ? null : item.getNom());
                                }
                        });

                        collectionIdField.setButtonCell(new ListCell<collection_t>() {
                                @Override
                                protected void updateItem(collection_t item, boolean empty) {
                                        super.updateItem(item, empty);
                                        setText(empty || item == null ? null : item.getNom());
                                }
                        });

                } catch (SQLException e) {
                        e.printStackTrace();
                }


        }}


