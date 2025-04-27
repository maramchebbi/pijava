package controller;

import Models.collection_t;
import Models.textile;
import Services.TextileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class detail1 {
    @FXML private FlowPane imageContainer;
    @FXML private Label collectionTitle;
    @FXML private TextField searchField;



    private collection_t currentCollection;
    private TextileService textileService = new TextileService();

    public void setCollection(collection_t collection) {
        this.currentCollection = collection;
        collectionTitle.setText(collection.getNom());
        loadTextiles();
    }


    @FXML
    private void initialize() {
        // Ajouter un écouteur pour la recherche dynamique
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue == null || newValue.trim().isEmpty()) {
                        loadTextiles();
                        return;
                    }

                    String query = newValue.trim().toLowerCase();

                    // Si la collection n'est pas encore définie, on ne peut pas filtrer
                    if (currentCollection == null) {
                        return;
                    }

                    // Récupérer et filtrer les textiles de la collection actuelle
                    List<textile> allTextiles = textileService.getByCollectionId(currentCollection.getId());
                    List<textile> filteredTextiles = allTextiles.stream()
                            .filter(t ->
                                    t.getNom().toLowerCase().contains(query) ||
                                            t.getDescription().toLowerCase().contains(query) ||
                                            t.getMatiere().toLowerCase().contains(query) ||
                                            t.getType().toLowerCase().contains(query) ||
                                            t.getCouleur().toLowerCase().contains(query))
                            .collect(Collectors.toList());

                    // Mettre à jour l'affichage
                    imageContainer.getChildren().clear();

                    if (filteredTextiles.isEmpty() && !query.isEmpty()) {
                        Label noResultsLabel = new Label("Aucun résultat trouvé pour : " + query);
                        noResultsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #757575; -fx-padding: 20;");
                        imageContainer.getChildren().add(noResultsLabel);
                    } else {
                        for (textile t : filteredTextiles) {
                            VBox card = createTextileCard(t);
                            imageContainer.getChildren().add(card);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Ne pas afficher d'alerte pendant la saisie pour éviter de perturber l'utilisateur
                }
            });
        }
    }

    // Assurez-vous que votre méthode loadTextiles() est complète
    private void loadTextiles() {
        imageContainer.getChildren().clear();

        try {
            List<textile> textiles = textileService.getByCollectionId(currentCollection.getId());

            if (textiles.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Aucun textile trouvé dans cette collection");
                return;
            }

            for (textile t : textiles) {
                VBox card = createTextileCard(t);
                imageContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement des textiles: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private VBox createTextileCard(textile t) {
        // Création de la carte principale
        VBox card = new VBox();
        card.getStyleClass().add("textile-card");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        card.setSpacing(0);
        card.setUserData(t); // Stockage ESSENTIEL de l'objet textile

        // ===== SECTION IMAGE =====
        StackPane imagePane = new StackPane();
        imagePane.setStyle("-fx-background-radius: 10 10 0 0;");

        // Rectangle de fond
        Rectangle placeholder = new Rectangle(280, 200);
        placeholder.setStyle("-fx-fill: #f5f5f5; -fx-arc-width: 20; -fx-arc-height: 20;");

        // ImageView
        ImageView imageView = new ImageView();
        try {
            if (t.getImage() != null && !t.getImage().isEmpty()) {
                imageView.setImage(new Image("file:" + t.getImage()));
            } else {
                imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_textile.png")));
            }
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_textile.png")));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Clip pour les coins arrondis
        Rectangle clip = new Rectangle(280, 200);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        imagePane.getChildren().addAll(placeholder, imageView);

        // ===== SECTION INFORMATIONS =====
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-padding: 15 20;");

        // Nom
        Label nameLabel = new Label(t.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #603813;");

        // Description
        Label descLabel = new Label(t.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #757575; -fx-line-spacing: 1.2;");

        // ===== SECTION PROPRIÉTÉS =====
        HBox propertiesBox = new HBox(12);
        propertiesBox.setStyle("-fx-padding: 8 0 0 0;");

        // Matière
        HBox matiereBox = new HBox(5);
        matiereBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath matiereIcon = new SVGPath();
        matiereIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm4.59-12.42L10 14.17l-2.59-2.58L6 13l4 4 8-8z");
        matiereIcon.setStyle("-fx-fill: #907a6c;");
        matiereIcon.setScaleX(0.7);
        matiereIcon.setScaleY(0.7);
        Label matiereLabel = new Label(t.getMatiere());
        matiereLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #907a6c;");
        matiereBox.getChildren().addAll(matiereIcon, matiereLabel);

        // Dimension
        HBox dimensionBox = new HBox(5);
        dimensionBox.setAlignment(Pos.CENTER_LEFT);
        SVGPath dimensionIcon = new SVGPath();
        dimensionIcon.setContent("M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z");
        dimensionIcon.setStyle("-fx-fill: #907a6c;");
        dimensionIcon.setScaleX(0.7);
        dimensionIcon.setScaleY(0.7);
        Label dimensionLabel = new Label(t.getDimension());
        dimensionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #907a6c;");
        dimensionBox.getChildren().addAll(dimensionIcon, dimensionLabel);

        propertiesBox.getChildren().addAll(matiereBox, dimensionBox);

        // ===== SECTION BOUTONS =====
        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        // Bouton Modifier
        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #d7c3b6; -fx-text-fill: #603813; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 15;");
        editBtn.setOnAction(this::handleEdit);

        // Bouton Supprimer
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 15;");
        deleteBtn.setOnAction(this::handleDelete);

        buttonsBox.getChildren().addAll(editBtn, deleteBtn);

        // ===== ASSEMBLAGE FINAL =====
        infoBox.getChildren().addAll(nameLabel, descLabel, propertiesBox, buttonsBox);
        card.getChildren().addAll(imagePane, infoBox);

        return card;
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Ajoutez les méthodes manquantes dans detail1.java
    @FXML
    private void handleEdit(ActionEvent event) {
        try {
            textile textileToEdit = getTextileFromEvent(event);

            if (textileToEdit == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de récupérer les informations du textile");
                return;
            }
            // Obtenir les dimensions de l'écran
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit2.fxml"));
            Parent root = loader.load();

            edit2 controller = loader.getController();
            controller.setTextileDetails(textileToEdit);

            // Création d'une nouvelle scène en plein écran
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root,screenWidth,screenHeight);
            stage.setScene(scene);

            // Configuration du plein écran
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir l'éditeur: " + e.getMessage());
            e.printStackTrace();
        }
    }




    @FXML
    private void handleDelete(ActionEvent event) {
        try {
            // Récupérer le textile avec la même logique
            textile textileToDelete = getTextileFromEvent(event);

            if (textileToDelete == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun textile sélectionné ou textile introuvable");

                return;
            }

            // Confirmation de suppression
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText("Supprimer " + textileToDelete.getNom() + "?");
            confirm.setContentText("Cette action est irréversible.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                textileService.delete(textileToDelete);
                loadTextiles(); // Recharger l'affichage
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Textile supprimé avec succès");

            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Échec de la suppression: " + e.getMessage());

        }
    }


    private textile getTextileFromEvent(ActionEvent event) {
        try {
            // 1. Récupérer le bouton source de l'événement
            Button source = (Button) event.getSource();

            // 2. Remonter à la HBox parente des boutons
            HBox buttonsBox = (HBox) source.getParent();

            // 3. Remonter au VBox parent (infoBox)
            VBox infoBox = (VBox) buttonsBox.getParent();

            // 4. Remonter au VBox parent principal (la carte)
            VBox card = (VBox) infoBox.getParent();

            // 5. Récupérer l'objet textile stocké dans le userData de la carte
            return (textile) card.getUserData();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de récupérer les informations du textile: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    @FXML
    private void handleSearch(ActionEvent event) {
        try {
            String query = searchField.getText().trim().toLowerCase();

            if (query.isEmpty()) {
                // Si la recherche est vide, recharger tous les textiles
                loadTextiles();
                return;
            }

            // D'abord, récupérer tous les textiles de la collection actuelle
            List<textile> allTextiles = textileService.getByCollectionId(currentCollection.getId());

            // Filtrer les textiles qui correspondent à la recherche
            List<textile> filteredTextiles = allTextiles.stream()
                    .filter(t ->
                            t.getNom().toLowerCase().contains(query) ||
                                    t.getDescription().toLowerCase().contains(query) ||
                                    t.getMatiere().toLowerCase().contains(query) ||
                                    t.getType().toLowerCase().contains(query) ||
                                    t.getCouleur().toLowerCase().contains(query))
                    .collect(Collectors.toList());

            // Mettre à jour l'affichage
            imageContainer.getChildren().clear();

            if (filteredTextiles.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Recherche",
                        "Aucun textile ne correspond à votre recherche dans cette collection.");
                return;
            }

            for (textile t : filteredTextiles) {
                VBox card = createTextileCard(t);
                imageContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleBack() {
        try {
            // Obtenir les dimensions de l'écran
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) collectionTitle.getScene().getWindow();
            stage.setScene(new Scene(root,screenWidth,screenHeight));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir en arrière: " + e.getMessage());
        }
    }
}