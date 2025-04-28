package controller;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.stage.Screen;
import javafx.stage.Stage;

import Services.OeuvreService;
import Models.Oeuvre;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.sql.SQLException;

public class Detailco {

    @FXML
    private FlowPane imageContainer;

    @FXML
    private Label collectionTitleLabel;

    @FXML
    private Label collectionDescriptionLabel;

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private ScrollPane scrollPane;

    private final OeuvreService oeuvreService = new OeuvreService();
    private String collectionName = "";
    private int currentCollectionId;

    @FXML
    public void initialize() {
        // Code à exécuter lors de l'initialisation
    }

    // Méthode pour activer le mode plein écran
    public void setFullScreen() {
        if (mainAnchorPane.getScene() != null) {
            Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
            stage.setMaximized(true);
        }
    }

    public void populateImagesByCollection(int collectionId, String name, String description) {
        // Mettre à jour les informations de la collection
        this.collectionName = name;
        this.currentCollectionId = collectionId;
        collectionTitleLabel.setText("Collection: " + name);

        if (description != null && !description.isEmpty()) {
            collectionDescriptionLabel.setText(description);
        } else {
            collectionDescriptionLabel.setText("Explorez les œuvres de cette collection");
        }

        // Activer le mode plein écran
        setFullScreen();

        // Charger les œuvres
        loadOeuvres(collectionId);
    }

    public void populateImagesByCollection(int collectionId) {
        // Ancienne méthode maintenue pour compatibilité
        this.currentCollectionId = collectionId;

        // Activer le mode plein écran
        setFullScreen();

        loadOeuvres(collectionId);
    }

    private void loadOeuvres(int collectionId) {
        try {
            List<Oeuvre> oeuvres = oeuvreService.getByCollectionId(collectionId);
            imageContainer.getChildren().clear();

            if (oeuvres.isEmpty()) {
                VBox emptyContainer = createEmptyMessage();
                imageContainer.getChildren().add(emptyContainer);
                return;
            }

            for (Oeuvre oeuvre : oeuvres) {
                VBox oeuvreContainer = createOeuvreCard(oeuvre);
                imageContainer.getChildren().add(oeuvreContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VBox errorContainer = createErrorMessage(e.getMessage());
            imageContainer.getChildren().add(errorContainer);
        }
    }

    private VBox createOeuvreCard(Oeuvre oeuvre) {
        // Créer un conteneur élégant pour chaque œuvre
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setPrefWidth(220);
        card.setMaxWidth(220);

        // Appliquer un effet d'ombre
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(2.0);
        shadow.setColor(Color.color(0, 0, 0, 0.15));
        card.setEffect(shadow);

        // Conteneur pour l'image avec bordure arrondie
        VBox imageBox = new VBox();
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPrefHeight(180);
        imageBox.setMaxHeight(180);
        imageBox.setCursor(Cursor.HAND);
        imageBox.setOnMouseClicked(e -> handleDetails(oeuvre));

        try {
            String imageUrl = oeuvre.getImage();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Convertir le chemin relatif en URL valide
                if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
                    imageUrl = new File(imageUrl).toURI().toString();
                }

                Image image = new Image(imageUrl, 180, 180, true, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(180);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                // Appliquer une forme ronde aux coins de l'image
                Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
                clip.setArcWidth(16);
                clip.setArcHeight(16);
                imageView.setClip(clip);

                imageBox.getChildren().add(imageView);
            } else {
                Label noImageLabel = new Label("Aucune image");
                noImageLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
                imageBox.getChildren().add(noImageLabel);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Image non disponible");
            errorLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
            imageBox.getChildren().add(errorLabel);
        }

        // Label pour le nom de l'œuvre
        Label nameLabel = new Label(oeuvre.getNom() != null ? oeuvre.getNom() : "Sans titre");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-alignment: center;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);

        // Barre d'actions (icônes)
        HBox actionBar = createActionButtons(oeuvre);
        actionBar.setAlignment(Pos.CENTER);
        actionBar.setSpacing(15);
        actionBar.setPadding(new Insets(8, 0, 0, 0));

        // Ajouter tous les éléments au card
        if (oeuvre.getDescription() != null && !oeuvre.getDescription().isEmpty()) {
            Label descLabel = new Label(truncateText(oeuvre.getDescription(), 60));
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-style: italic;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(200);

            card.getChildren().addAll(imageBox, nameLabel, descLabel, actionBar);
        } else {
            card.getChildren().addAll(imageBox, nameLabel, actionBar);
        }

        return card;
    }

    private HBox createActionButtons(Oeuvre oeuvre) {
        HBox actionBar = new HBox(10);

        Button detailsBtn = createCustomIconButton("/images/show.png", "Détails");
        detailsBtn.setOnAction(e -> handleDetails(oeuvre));

        Button editBtn = createCustomIconButton("/images/edit.png", "Modifier");
        editBtn.setOnAction(e -> handleEdit(oeuvre));

        Button deleteBtn = createCustomIconButton("/images/truc.png", "Supprimer");
        deleteBtn.setOnAction(e -> handleDelete(oeuvre));


        actionBar.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
        return actionBar;
    }
    private Button createCustomIconButton(String iconPath, String tooltipText) {
        Button button = new Button();

        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitHeight(16);
            imageView.setFitWidth(16);

            button.setGraphic(imageView);
            button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4;");
            button.setTooltip(new Tooltip(tooltipText));

            // Effet de survol
            button.setOnMouseEntered(e -> {
                button.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4;");
            });

            button.setOnMouseExited(e -> {
                button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4;");
            });
        } catch (Exception e) {
            // En cas d'erreur de chargement d'image, revenir à l'icône FontAwesome
            System.err.println("Impossible de charger l'icône: " + iconPath);
            return createMinimalIconButton(FontAwesomeIcon.QUESTION, tooltipText);
        }

        return button;
    }

    private Button createMinimalIconButton(FontAwesomeIcon icon, String tooltipText) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14px");
        iconView.setStyle("-fx-fill: #767676;");

        Button button = new Button();
        button.setGraphic(iconView);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4;");
        button.setTooltip(new Tooltip(tooltipText));

        button.setOnMouseEntered(e -> {
            iconView.setStyle("-fx-fill: #e60023;");
            button.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);");
        });

        button.setOnMouseExited(e -> {
            iconView.setStyle("-fx-fill: #767676;");
            button.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-cursor: hand; -fx-padding: 6; -fx-background-radius: 4;");
        });

        return button;
    }

    private Button createIconButton(String icon, String tooltip) {
        Button button = new Button(icon);
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5px 8px;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #f0f0f0;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 5px 8px;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 5px 8px;" +
                                "-fx-cursor: hand;"
                )
        );

        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        return button;
    }

    private VBox createEmptyMessage() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        Label messageLabel = new Label("Aucune œuvre n'a été ajoutée à cette collection.");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");

        Button addBtn = new Button("+ Ajouter une œuvre");
        addBtn.setStyle(
                "-fx-background-color: #D2B48C;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10px 20px;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;"
        );

        addBtn.setOnAction(e -> handleAddOeuvre());

        container.getChildren().addAll(messageLabel, addBtn);
        return container;
    }

    private VBox createErrorMessage(String error) {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(50));

        Label errorLabel = new Label("Impossible de charger les œuvres");
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cc0000; -fx-font-weight: bold;");

        Label detailLabel = new Label(error);
        detailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        container.getChildren().addAll(errorLabel, detailLabel);
        return container;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }



    private void handleEdit(Oeuvre oeuvre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            edit editController = loader.getController();
            editController.setOeuvreDetails(oeuvre);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'œuvre");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur");
            e.printStackTrace();
        }
    }
    private void handleDetails(Oeuvre oeuvre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();

            detail detailController = loader.getController();
            detailController.setOeuvreDetails(oeuvre);

            // Style modal Pinterest
            Stage stage = new Stage();
            stage.setTitle("Détails de l'œuvre");

            // Effet d'animation pour une ouverture plus fluide
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Inside handleDetails method, replace the scrollPane reference with:
            Stage mainStage = (Stage) mainAnchorPane.getScene().getWindow();
            stage.setX(mainStage.getX() + (mainStage.getWidth() - root.prefWidth(-1)) / 2);
            stage.setY(mainStage.getY() + (mainStage.getHeight() - root.prefHeight(-1)) / 2);

            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails");
            e.printStackTrace();
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void handleDelete(Oeuvre oeuvre) {
        System.out.println("Supprimer l'œuvre: " + oeuvre.getNom());

        // Afficher une confirmation avant suppression
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer l'œuvre \"" + oeuvre.getNom() + "\" ?"
        );

        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette œuvre ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Appeler le service pour supprimer l'œuvre
                    // Passons l'objet Oeuvre et non pas l'ID
                    oeuvreService.delete(oeuvre);

                    // Rafraîchir l'affichage
                    loadOeuvres(currentCollectionId);

                    // Notification de succès
                    showNotification("Œuvre supprimée avec succès");
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlert("Erreur lors de la suppression", e.getMessage());
                }
            }
        });
    }

    private void handleAddOeuvre() {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            System.out.println("Ajouter une nouvelle œuvre à la collection ID: " + currentCollectionId);

            // Charger le fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Add.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = new Stage();

            stage.setTitle("Ajouter une œuvre");
            stage.setScene(scene);

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de add.fxml");
            e.printStackTrace();
        }
    }

    // Utilitaires
    private void showNotification(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION,
                message
        );
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                message
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}