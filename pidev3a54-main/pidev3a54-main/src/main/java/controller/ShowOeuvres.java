package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.ContentDisplay;
import Models.Oeuvre;
import Services.OeuvreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ArrayList;

public class ShowOeuvres {

    @FXML
    private FlowPane imageContainer;

    @FXML
    private ScrollPane scrollPane;

    private final OeuvreService oeuvreService = new OeuvreService();

    // Pour la pagination et le chargement infini (comme Pinterest)
    private int currentPage = 0;
    private final int itemsPerPage = 20;
    private boolean isLoading = false;

    // Liste pour garder trace des œuvres déjà affichées
    private List<Integer> displayedOeuvreIds = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("Controller initialized!");

        // Vérification des composants UI
        if (imageContainer == null) {
            System.err.println("Error: imageContainer is null!");
            return;
        }

        // Chargement initial des images
        try {
            populateImages();
        } catch (Exception e) {
            System.err.println("Error in populateImages:");
            e.printStackTrace();
        }

        // Configuration du mode plein écran et responsive
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) scrollPane.getScene().getWindow();

                // Layout responsive
                imageContainer.prefWidthProperty().bind(
                        scrollPane.widthProperty().subtract(40)
                );

                // Implémentation du scroll infini façon Pinterest
                scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    // Si on atteint ~80% du scroll et qu'on n'est pas déjà en train de charger
                    if (newValue.doubleValue() > 0.8 && !isLoading) {
                        loadMoreItems();
                    }
                });

            } catch (Exception e) {
                System.err.println("Error setting up responsive UI:");
                e.printStackTrace();
            }
        });
    }

    // Méthode pour simuler le chargement infini de Pinterest
    private void loadMoreItems() {
        isLoading = true;
        currentPage++;

        // Dans une vraie implémentation, vous chargeriez une nouvelle page depuis votre service
        // Pour simuler, on peut ajouter un délai
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simuler un délai réseau

                // Sur le thread UI, ajouter plus d'œuvres
                Platform.runLater(() -> {
                    try {
                        populateMoreImages();
                        isLoading = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Version principale pour le chargement initial
    public void populateImages() {
        try {
            List<Oeuvre> oeuvres = oeuvreService.getAll();
            imageContainer.getChildren().clear();
            displayedOeuvreIds.clear(); // Réinitialiser la liste des IDs affichés

            addOeuvresToContainer(oeuvres);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les œuvres: " + e.getMessage());
        }
    }

    // Version pour ajouter plus d'œuvres lors du scroll (Pinterest infinite scrolling)
    private void populateMoreImages() {
        try {
            List<Oeuvre> allOeuvres = oeuvreService.getAll();
            List<Oeuvre> newOeuvres = new ArrayList<>();

            // Filtrer pour ne garder que les œuvres qui n'ont pas encore été affichées
            for (Oeuvre oeuvre : allOeuvres) {
                if (!displayedOeuvreIds.contains(oeuvre.getId())) {
                    newOeuvres.add(oeuvre);

                    // Limiter à itemsPerPage nouvelles œuvres par chargement
                    if (newOeuvres.size() >= itemsPerPage) {
                        break;
                    }
                }
            }

            // Si nous avons de nouvelles œuvres à ajouter
            if (!newOeuvres.isEmpty()) {
                // Ajouter ces éléments au conteneur existant (sans vider)
                addOeuvresToContainer(newOeuvres);
            } else {
                System.out.println("Toutes les œuvres ont été affichées");
                // Optionnel: afficher un message "Fin du contenu"
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode commune pour ajouter des œuvres au conteneur
    private void addOeuvresToContainer(List<Oeuvre> oeuvres) {
        // Largeurs pour le layout masonry style Pinterest (colonnes variables)
        double[] cardWidths = {240, 240, 240, 240};
        int cardIndex = 0;

        for (Oeuvre oeuvre : oeuvres) {
            // Ajouter l'ID à la liste des œuvres déjà affichées
            displayedOeuvreIds.add(oeuvre.getId());

            // Obtenir la largeur pour cette carte (crée un effet masonry plus naturel)
            double cardWidth = cardWidths[cardIndex % cardWidths.length];
            cardIndex++;

            // Créer le conteneur de carte
            VBox card = createCard(oeuvre, cardWidth);

            // Ajouter la carte au flow pane
            imageContainer.getChildren().add(card);
        }
    }

    // Méthode pour créer une carte Pinterest-style
    private VBox createCard(Oeuvre oeuvre, double cardWidth) {
        // Conteneur principal de la carte
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;");

        // Ombre légère pour l'élévation Pinterest-like
        DropShadow shadow = new DropShadow();
        shadow.setRadius(4.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(1.0);
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        card.setEffect(shadow);
        card.setPrefWidth(cardWidth);

        // Conteneur d'image avec coins arrondis
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-radius: 16 16 0 0;");

        // Image
        ImageView imageView = createImageView(oeuvre, cardWidth, imageContainer);
        imageContainer.getChildren().add(imageView);

        // Section inférieure avec titre et contrôles minimaux (style Pinterest)
        VBox bottomSection = new VBox(8);
        bottomSection.setPadding(new Insets(12));
        bottomSection.setStyle("-fx-background-radius: 0 0 16 16;");

        // Titre - typographie propre et subtile comme Pinterest
        Text title = new Text(oeuvre.getNom());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-fill: #333333;");
        title.setWrappingWidth(cardWidth - 24); // Tenir compte du padding

        // Barre d'action style Pinterest (plus minimaliste)
        HBox actionBar = createActionBar(oeuvre);

        // Ajouter tout à la carte
        bottomSection.getChildren().addAll(title, actionBar);
        card.getChildren().addAll(imageContainer, bottomSection);

        return card;
    }

    // Créer l'ImageView avec effets et gestion des événements
    private ImageView createImageView(Oeuvre oeuvre, double cardWidth, StackPane container) {
        ImageView imageView = new ImageView();
        String imagePath = oeuvre.getImage();

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image("file:" + imagePath);
                imageView.setImage(image);
                imageView.setFitWidth(cardWidth);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageView.setStyle("-fx-background-radius: 16 16 0 0;");

                // Effets de survol pour interaction Pinterest-like
                container.setOnMouseEntered(e -> {
                    imageView.setOpacity(0.9);
                    container.setCursor(javafx.scene.Cursor.HAND);
                });

                container.setOnMouseExited(e -> {
                    imageView.setOpacity(1.0);
                });

                // Afficher les détails au clic (plutôt que d'utiliser un bouton)
                container.setOnMouseClicked(e -> handleDetails(oeuvre));

            } catch (Exception e) {
                // Placeholder d'erreur avec style Pinterest
                StackPane placeholder = new StackPane();
                placeholder.setStyle("-fx-background-color: #f0f0f0; -fx-min-height: 180; -fx-background-radius: 16 16 0 0;");
                Text placeholderText = new Text("No Image");
                placeholderText.setStyle("-fx-fill: #999999; -fx-font-size: 14;");
                placeholder.getChildren().add(placeholderText);
                container.getChildren().add(placeholder);
            }
        }

        return imageView;
    }

    // Créer la barre d'action avec les boutons
    private HBox createActionBar(Oeuvre oeuvre) {
        HBox actionBar = new HBox(8);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(6, 0, 0, 0));

        // Créer des boutons minimalistes avec l'affichage d'icône approprié
        // Pinterest utilise des contrôles très minimalistes - souvent juste des options d'épinglage et de menu
        Button editBtn = createMinimalIconButton(FontAwesomeIcon.EDIT, "Modifier");
        editBtn.setOnAction(e -> handleEdit(oeuvre));

        Button deleteBtn = createMinimalIconButton(FontAwesomeIcon.TRASH, "Supprimer");
        deleteBtn.setOnAction(e -> handleDelete(oeuvre));

        Button view3DBtn = createMinimalIconButton(FontAwesomeIcon.CUBE, "Vue 3D");
        view3DBtn.setOnAction(e -> handle3DView(oeuvre));

        actionBar.getChildren().addAll(editBtn, deleteBtn, view3DBtn);

        return actionBar;
    }

    // Pinterest utilise des boutons très subtils, uniquement avec icône
    private Button createMinimalIconButton(FontAwesomeIcon icon, String tooltipText) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14px");
        iconView.setStyle("-fx-fill: #767676;");

        Button button = new Button();
        button.setGraphic(iconView);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
        button.setTooltip(new Tooltip(tooltipText));

        // Effet de survol plus subtil (style Pinterest)
        button.setOnMouseEntered(e -> {
            iconView.setStyle("-fx-fill: #e60023;");
        });

        button.setOnMouseExited(e -> {
            iconView.setStyle("-fx-fill: #767676;");
        });

        return button;
    }

    // Méthode 3D
    private void handle3DView(Oeuvre oeuvre) {
        try {
            // Créer le viewer 3D
            Artwork3DViewer viewer = new Artwork3DViewer();

            // Récupérer le chemin de l'image exacte de l'œuvre
            String imagePath = oeuvre.getImage();

            // Déterminer les dimensions (vous pouvez les stocker dans votre modèle Oeuvre)
            double width = 200;  // ou oeuvre.getWidth() si disponible
            double height = 300; // ou oeuvre.getHeight()
            double depth = 200;  // ou oeuvre.getDepth()

            // Afficher l'œuvre en 3D avec SA PROPRE image
            viewer.displayArtwork(
                    "vase", // ou oeuvre.getType() si vous avez ce champ
                    width,
                    height,
                    depth,
                    "file:" + imagePath // Préfixe "file:" pour les chemins absolus
            );

            // Configurer la fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(viewer, 850, 650));
            stage.setTitle("Visualisation 3D: " + oeuvre.getNom());
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'afficher la vue 3D: " + e.getMessage());
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

            // Centrer la fenêtre
            Stage mainStage = (Stage) scrollPane.getScene().getWindow();
            stage.setX(mainStage.getX() + (mainStage.getWidth() - root.prefWidth(-1)) / 2);
            stage.setY(mainStage.getY() + (mainStage.getHeight() - root.prefHeight(-1)) / 2);

            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails");
            e.printStackTrace();
        }
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

    private void handleDelete(Oeuvre oeuvre) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'œuvre '" + oeuvre.getNom() + "' ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Modification ici pour utiliser la méthode qui accepte un Oeuvre
                oeuvreService.delete(oeuvre); // Passer l'objet Oeuvre complet

                // Retirer l'ID de l'œuvre supprimée de la liste
                displayedOeuvreIds.remove(Integer.valueOf(oeuvre.getId()));

                showAlert("Succès", "Œuvre supprimée avec succès");
                populateImages(); // Rafraîchir l'affichage
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAjouterOeuvre(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Add.fxml"));
            Parent root = loader.load();

            // Effet de transition
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleWorkshopRedirect(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddWorkshop.fxml")); // adapte le chemin
            Parent workshopRoot = loader.load();

            // Interface modal pour workshop
            Stage stage = new Stage();
            stage.setTitle("Ajouter Workshop");
            stage.setScene(new Scene(workshopRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface de workshop");
        }
    }

    @FXML
    private void affichercollectionaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Collections.fxml"));
            Parent root = loader.load();

            // Transition fluide
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les collections");
            e.printStackTrace();
        }
    }
}