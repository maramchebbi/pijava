package controller;

import Models.collection_t;
import Models.textile;
import Services.TextileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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

    private void loadTextiles() {
        imageContainer.getChildren().clear();

        try {
            List<textile> textiles = textileService.getByCollectionId(currentCollection.getId());

            for (textile t : textiles) {
                VBox textileCard = createTextileCard(t);
                imageContainer.getChildren().add(textileCard);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les textiles: " + e.getMessage());
        }
    }

    private VBox createTextileCard(textile t) {
        VBox card = new VBox();
        card.getStyleClass().add("textile-card");
        card.setStyle("-fx-spacing: 0;");

        // Image Section
        StackPane imagePane = new StackPane();
        imagePane.setStyle("-fx-background-radius: 10 10 0 0;");

        Rectangle placeholder = new Rectangle(280, 200);
        placeholder.setStyle("-fx-fill: #f5f5f5; -fx-arc-width: 20; -fx-arc-height: 20;");

        ImageView imageView = new ImageView();
        try {
            Image img = new Image("file:" + t.getImage());
            imageView.setImage(img);
        } catch (Exception e) {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/default_textile.png")));
        }
        imageView.setFitWidth(280);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Rectangle clip = new Rectangle(280, 200);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        imagePane.getChildren().addAll(placeholder, imageView);

        // Info Section
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-padding: 15 20;");

        Label nameLabel = new Label(t.getNom());
        nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #603813;");

        Label descLabel = new Label(t.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #757575; -fx-line-spacing: 1.2;");

        // Properties
        HBox propertiesBox = new HBox(12);
        propertiesBox.setStyle("-fx-padding: 8 0 0 0;");

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

        // Buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setStyle("-fx-padding: 10 0 0 0;");
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #d7c3b6; -fx-text-fill: #603813; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 15;");
        editBtn.setOnAction(event -> handleEdit(t, event)); // Utilisez le paramètre de la lambda

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #603813; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 15;");
        detailsBtn.setOnAction(event -> handleDetails(t, event)); // Utilisez le paramètre de la lambda

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 15; -fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 5 15;");
        deleteBtn.setOnAction(event -> handleDelete(t, event)); // Utilisez le paramètre de la lambda

        buttonsBox.getChildren().addAll(editBtn, detailsBtn, deleteBtn);

        infoBox.getChildren().addAll(nameLabel, descLabel, propertiesBox, buttonsBox);
        card.getChildren().addAll(imagePane, infoBox);

        return card;
    }

    private void handleDetails(textile t,ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detail2.fxml"));
            Parent root = loader.load();

            detail controller = loader.getController();
            controller.setTextileDetails(t);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du textile");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir les détails");
        }
    }

    private void handleEdit(textile t,ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit2.fxml"));
            Parent root = loader.load();

            edit controller = loader.getController();
            controller.setTextileDetails(t);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier le textile");
            stage.show();

            stage.setOnHidden(e -> loadTextiles()); // Rafraîchir après modification
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur");
        }
    }

    private void handleDelete(textile t,ActionEvent event) {
        try {
            textileService.delete(t);
            loadTextiles(); // Rafraîchir la liste
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Textile supprimé avec succès");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le textile");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();
            Scene scene = collectionTitle.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir en arrière: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        try {
            imageContainer.getChildren().clear();

            if (query.isEmpty()) {
                // Si le champ de recherche est vide, afficher tous les textiles de la collection
                loadTextiles();
            } else {
                // Sinon, effectuer une recherche filtrée
                List<textile> textiles = textileService.searchTextiles(query);

                // Filtrer les résultats pour ne garder que ceux de la collection actuelle
                for (textile t : textiles) {
                    if (t.getCollectionId() != null && t.getCollectionId() == currentCollection.getId()) {
                        VBox textileCard = createTextileCard(t);
                        imageContainer.getChildren().add(textileCard);
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de recherche",
                    "Impossible de rechercher les textiles: " + e.getMessage());
        }
    }

}