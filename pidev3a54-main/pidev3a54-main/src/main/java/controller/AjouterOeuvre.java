package controller;

import Models.CeramicCollection;
import Models.Oeuvre;
import Services.CollectionCeramiqueService;
import Services.OeuvreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
public class AjouterOeuvre {

        @FXML
        private ImageView Artisferaimage;

        @FXML
        private TextField collectionIdField;

        @FXML
        private ComboBox<CeramicCollection> collectionComboBox;


        @FXML
        private TextField couleurField;

        @FXML
        private TextField usertextfield;

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
        private TextField categorieField;

        @FXML
        private ImageView textileimage;

        @FXML
        private TextField typeField;



        @FXML
        void ajouterOeuvre(ActionEvent event) {
            // Récupération des données depuis les champs FXML
            String nom = nomField.getText();
            String type = typeField.getText();
            String description = descriptionField.getText();
            String matiere = matiereField.getText();
            String dimensions = dimensionField.getText(); // Fix variable name
            String couleur = couleurField.getText();

            String categorie = categorieField.getText();

            int userId;
            int collectionId;
            String imagePath = "";

            // Vérification et conversion des IDs
            CeramicCollection selectedCollection;
            try {
                userId = Integer.parseInt(usertextfield.getText());
                // collectionId = Integer.parseInt(collectionIdField.getText());
                // Corrected collection ID
                selectedCollection = collectionComboBox.getValue();
                if (selectedCollection == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText("Aucune collection sélectionnée !");
                    alert.setContentText("Veuillez choisir une collection avant d'ajouter l'œuvre.");
                    alert.show();
                    return;  // Exit the method if no collection is selected
                }


                collectionId = selectedCollection.getId();

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



            Oeuvre oeuvreObj = new Oeuvre(
                    nom, type, description, matiere, couleur, dimensions, imagePath, categorie, userId, selectedCollection
            );

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();

            Set<ConstraintViolation<Oeuvre>> violations = validator.validate(oeuvreObj);

            if (!violations.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (ConstraintViolation<Oeuvre> violation : violations) {
                    message.append(violation.getMessage()).append("\n");
                }
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Attention !");
                alert.setHeaderText("Vous Avez Definir des mauvais Format  :");
                alert.setContentText(message.toString());
                alert.show();
                return; // On ne continue pas si des erreurs sont détectées
            }
            // Création du service Oeuvre
            OeuvreService oeuvreService = new OeuvreService();

            // Tentative d’ajout avec gestion des exceptions
            try {
                oeuvreService.add(oeuvreObj);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Œuvre ajoutée avec succès !");
                alert.show();

                // ➕ Optionnel : Redirection vers un autre écran ou actualisation de l’interface
                // FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
                // Parent root = loader.load();
                // detail controller = loader.getController();
                // ... transmettre oeuvreObj ou actualiser les données

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur lors de l'ajout");
                alert.setHeaderText("Impossible d'ajouter l'œuvre");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }

        @FXML
        public void viewoeuvre(ActionEvent actionEvent) {
                try {
                        // Load the FXML file
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
                        Parent root = loader.load();

                        // Get the current stage
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                        // Create a new scene with the loaded FXML content
                        Scene scene = new Scene(root);

                        // Set the new scene and show the stage
                        stage.setScene(scene);
                        stage.show();

                } catch (IOException e) {
                        // Handle the exception and print the error message
                        System.out.println("Error loading the FXML file: " + e.getMessage());
                }
        }



        public void initialize() {
                // Créer une instance du service des collections
                CollectionCeramiqueService collectionService = new CollectionCeramiqueService(/*connection*/);

                try {
                        // Récupérer toutes les collections
                        List<CeramicCollection> collections = collectionService.getAll();

                        // Convertir la liste en ObservableList pour la ComboBox
                        ObservableList<CeramicCollection> collectionObservableList = FXCollections.observableArrayList(collections);

                        // Remplir la ComboBox avec la liste des collections
                        collectionComboBox.setItems(collectionObservableList);

                        // Optional: Spécifier un affichage personnalisé si nécessaire
                        collectionComboBox.setCellFactory(param -> new ListCell<CeramicCollection>() {
                                @Override
                                protected void updateItem(CeramicCollection item, boolean empty) {
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                                setText(null);
                                        } else {
                                                // Afficher le nom de la collection dans la liste déroulante
                                                setText(item.getNom_c());
                                        }
                                }
                        });

                        // Optional: Spécifier l'affichage de l'élément sélectionné dans la ComboBox
                        collectionComboBox.setButtonCell(new ListCell<CeramicCollection>() {
                                @Override
                                protected void updateItem(CeramicCollection item, boolean empty) {
                                        super.updateItem(item, empty);
                                        if (empty || item == null) {
                                                setText(null);
                                        } else {
                                                setText(item.getNom_c());  // Affiche le nom de la collection sélectionnée
                                        }
                                }
                        });

                } catch (SQLException e) {
                        e.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur de chargement des collections");
                        alert.setHeaderText("Impossible de récupérer les collections");
                        alert.setContentText(e.getMessage());
                        alert.show();
                }
        }



         @FXML
        public void uploadimage(ActionEvent actionEvent) {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Sélectionner une image");

                        // Ajouter un filtre pour n'autoriser que les fichiers image
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.bmp"
                        );
                        fileChooser.getExtensionFilters().add(extFilter);

                        // Ouvrir la boîte de dialogue pour sélectionner un fichier
                        Stage stage = (Stage) textileimage.getScene().getWindow();
                        File file = fileChooser.showOpenDialog(stage);

                        if (file != null) {
                                try {
                                        // Afficher l’image dans l’ImageView
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

        }















