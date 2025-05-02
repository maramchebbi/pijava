package controller;
import Models.CeramicCollection;
import Models.Oeuvre;
import controller.Detailco;
import Services.CollectionCeramiqueService;
import Services.OeuvreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CollectionsController {

    @FXML
    private FlowPane collectionsContainer;

    @FXML
    private ScrollPane scrollPane;

    private ObservableList<CeramicCollection> collectionsList = FXCollections.observableArrayList();
    private CollectionCeramiqueService collectionService;
    private OeuvreService oeuvreService;

    // Image par défaut quand aucune œuvre n'est disponible
    private final String DEFAULT_IMAGE = "/images/2.jpg";

    public CollectionsController() {
        this.collectionService = new CollectionCeramiqueService();
        this.oeuvreService = new OeuvreService();
    }

    @FXML
    public void initialize() {
        // Configuration du conteneur de collections
        collectionsContainer.setHgap(20);
        collectionsContainer.setVgap(20);
        collectionsContainer.setPadding(new Insets(20));
        collectionsContainer.setAlignment(Pos.CENTER);

        // Configuration du scroll pane
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(collectionsContainer);
        scrollPane.getStyleClass().add("edge-to-edge");

        // Chargement des collections
        loadCollectionsData();
    }

    // Charger les collections depuis la base de données
    private void loadCollectionsData() {
        try {
            List<CeramicCollection> collections = collectionService.getAll();
            collectionsList.setAll(collections);

            // Afficher chaque collection comme une carte
            for (CeramicCollection collection : collections) {
                collectionsContainer.getChildren().add(createCollectionCard(collection));
            }

        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Erreur",
                    "Erreur lors de la récupération des collections: " + e.getMessage());
        }
    }

    private VBox createCollectionCard(CeramicCollection collection) {
        // Créer la carte (container)
        VBox card = new VBox();
        card.setAlignment(Pos.CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setPrefHeight(320);
        card.setStyle("-fx-background-color: white; -fx-border-color: #D2B48C; -fx-border-radius: 5;");

        // Ajouter un effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(5);
        card.setEffect(shadow);

        // Créer l'image de la collection
        ImageView imageView = new ImageView();
        imageView.setFitWidth(220);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // Essayer de trouver une image parmi les œuvres de cette collection
        String imagePath = getCollectionImagePath(collection.getId());
        if (imagePath != null && new File(imagePath).exists()) {
            imageView.setImage(new Image("file:" + imagePath));
        } else {
            // Utiliser l'image par défaut si aucune œuvre n'est trouvée
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream(DEFAULT_IMAGE)));
            } catch (Exception e) {
                // Fallback si l'image par défaut n'est pas trouvée
                StackPane placeholder = new StackPane();
                placeholder.setPrefSize(220, 180);
                placeholder.setStyle("-fx-background-color: #F5F5F5;");
                Label noImageLabel = new Label("Aucune image");
                noImageLabel.setStyle("-fx-text-fill: #6B4226;");
                placeholder.getChildren().add(noImageLabel);
                card.getChildren().add(placeholder);
            }
        }

        // Ajouter l'image au conteneur
        if (imageView.getImage() != null) {
            card.getChildren().add(imageView);
        }

        // Titre de la collection
        Label titleLabel = new Label(collection.getNom_c());
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#6B4226"));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        // Description (limitée à 2-3 lignes)
        Label descriptionLabel = new Label(truncateText(collection.getDescription_c(), 100));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setTextAlignment(TextAlignment.CENTER);
        descriptionLabel.setTextFill(Color.web("#666666"));

        // Bouton pour voir les œuvres
        Button viewButton = new Button("Voir les œuvres");
        viewButton.setStyle("-fx-background-color: #D2B48C; -fx-text-fill: #6B4226; " +
                "-fx-background-radius: 3;");
       // viewButton.setOnAction(e -> viewCollectionOeuvres(collection));

        // Zone pour les actions (modifier/supprimer)
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #6B4226;");
        editButton.setOnAction(e -> modifyCollection(collection));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #6B4226;");
        deleteButton.setOnAction(e -> delete(collection));

        actionsBox.getChildren().addAll(editButton, deleteButton);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(titleLabel, descriptionLabel, viewButton, actionsBox);

        // Ajouter un effet au survol
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #FFFAF0; -fx-border-color: #D2B48C; -fx-border-radius: 5;");
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-border-color: #D2B48C; -fx-border-radius: 5;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    // Récupérer le chemin d'une image d'une œuvre dans cette collection
    private String getCollectionImagePath(int collectionId) {
        try {
            // Récupérer les œuvres de cette collection
            List<Oeuvre> oeuvres = oeuvreService.getByCollectionId(collectionId);

            if (oeuvres != null && !oeuvres.isEmpty()) {
                // Option 1: Prendre la première œuvre qui a une image
                for (Oeuvre oeuvre : oeuvres) {
                    if (oeuvre.getImage() != null && !oeuvre.getImage().isEmpty()) {
                        return oeuvre.getImage();
                    }
                }

                // Option 2: Si aucune œuvre n'a d'image, retourner null
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des œuvres: " + e.getMessage());
        }

        return null;
    }

    // Tronquer le texte de la description
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    // Afficher les œuvres d'une collection
//    private void viewCollectionOeuvres(CeramicCollection collection) {
//        try {
//            Screen screen = Screen.getPrimary();
//            double screenWidth = screen.getVisualBounds().getWidth();
//            double screenHeight = screen.getVisualBounds().getHeight();
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailco.fxml"));
//            Parent root = loader.load();
//
//            // Récupérer le contrôleur lié au fichier FXML
//            Detailco controller = loader.getController();
//
//            // Charger les images de la collection sélectionnée
//            controller.populateImagesByCollection(collection.getId());
//
//            // Afficher la nouvelle scène
//            Scene scene = new Scene(root,screenWidth,screenHeight);
//            Stage stage = new Stage();
//            stage.setScene(scene);
//            stage.setTitle("Œuvres de la Collection: " + collection.getNom_c());
//            stage.show();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            showAlert(AlertType.ERROR, "Erreur",
//                    "Impossible d'afficher les œuvres de cette collection.");
//        }
//    }

    // Modifier une collection
    private void modifyCollection(CeramicCollection collection) {
        Dialog<CeramicCollection> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Collection");
        dialog.setHeaderText("Modifier la collection: " + collection.getNom_c());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        dialogPane.setPrefSize(400, 250);

        ButtonType updateButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        Button updateButton = (Button) dialog.getDialogPane().lookupButton(updateButtonType);
        updateButton.setStyle("-fx-background-color: #D2B48C; -fx-text-fill: #6B4226;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #6B4226;");

        TextField nomField = new TextField(collection.getNom_c());
        nomField.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D2B48C; -fx-border-radius: 3;");

        TextArea descriptionField = new TextArea(collection.getDescription_c());
        descriptionField.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D2B48C; -fx-border-radius: 3;");
        descriptionField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 10, 10, 10));

        grid.add(new Label("Nom de la collection:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                collection.setNom_c(nomField.getText());
                collection.setDescription_c(descriptionField.getText());
                return collection;
            }
            return null;
        });

        Optional<CeramicCollection> result = dialog.showAndWait();

        result.ifPresent(updatedCollection -> {
            try {
                collectionService.update(updatedCollection);

                // Rafraîchir l'affichage
                collectionsContainer.getChildren().clear();
                loadCollectionsData();

                showAlert(Alert.AlertType.INFORMATION, "Collection modifiée",
                        "Les informations de la collection ont été mises à jour avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la mise à jour de la collection.");
            }
        });
    }

    // Supprimer une collection
    private void delete(CeramicCollection collection) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la collection");
        alert.setContentText("Voulez-vous vraiment supprimer la collection \"" + collection.getNom_c() + "\" ?\n" +
                "Cette action supprimera également toutes les œuvres associées.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #D2B48C; -fx-text-fill: #6B4226;");
        }

        Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #6B4226;");
        }

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                collectionService.delete(collection);

                // Rafraîchir l'affichage
                collectionsContainer.getChildren().clear();
                loadCollectionsData();

                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                        "La collection \"" + collection.getNom_c() + "\" a été supprimée avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                        "Une erreur est survenue lors de la suppression. Veuillez réessayer.");
            }
        }
    }

    // Afficher une alerte
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");

        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle("-fx-background-color: #D2B48C; -fx-text-fill: #6B4226;");
        }

        alert.showAndWait();
    }

    @FXML
    private void ajoutercoaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCollection.fxml"));
            Parent root = loader.load();
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void oeuvreaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}