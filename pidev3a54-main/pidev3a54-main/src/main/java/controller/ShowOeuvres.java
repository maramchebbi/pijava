package controller;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.util.stream.Collectors;
import javafx.application.Platform;
import java.util.Collections;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TextField; // Pour TextField

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
    private HBox filterButtonsContainer;
    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane scrollPane;

    private final OeuvreService oeuvreService = new OeuvreService();
    private final Random random = new Random();

    private List<String> types = List.of(
            "Sculpture",
            "Vase",
            "Poterie",
            "Assiette décorative",
            "bibelot"
    );
    private String currentFilter = null;

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
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.prefWidthProperty().bind(
                scrollPane.widthProperty().subtract(2)
        );

        // Configuration des filtres par type (AJOUT)
        setupTypeFilters();  // <-- Ajoutez cette ligne avant le chargement initial

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

                // Remove any padding or insets that might cause white space
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                // Bind the FlowPane width to match the ScrollPane viewport width
                imageContainer.prefWidthProperty().bind(
                        scrollPane.widthProperty().subtract(20)
                );

                // Make sure the FlowPane fills the available width
                imageContainer.setMaxWidth(Double.MAX_VALUE);

                // Adjust the column count based on available width
                scrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
                    double width = newVal.doubleValue();
                    int columns = (int) (width / 250);
                    imageContainer.setPrefWrapLength(width - 30);
                });

                // Infinite scroll implementation
                scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.doubleValue() > 0.8 && !isLoading) {
                        loadMoreItems();
                    }
                });

            } catch (Exception e) {
                System.err.println("Error setting up responsive UI:");
                e.printStackTrace();
            }
        });
        setupDynamicSearch();
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
            List<Oeuvre> oeuvres;
            if (currentFilter != null) {
                oeuvres = oeuvreService.getAll().stream()
                        .filter(o -> currentFilter.equalsIgnoreCase(o.getType()))
                        .collect(Collectors.toList());
            } else {
                oeuvres = oeuvreService.getAll();
            }

            imageContainer.getChildren().clear();
            displayedOeuvreIds.clear();
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
    // Méthode commune pour ajouter des œuvres au conteneur
    private void addOeuvresToContainer(List<Oeuvre> oeuvres) {
        // Mélanger la liste pour un affichage aléatoire
        Collections.shuffle(oeuvres);

        for (Oeuvre oeuvre : oeuvres) {
            displayedOeuvreIds.add(oeuvre.getId());

            // Créer un groupe pour chaque image et ses éléments
            StackPane imageGroup = new StackPane();

            // Créer l'image
            ImageView imageView = createImageView(oeuvre);

            // Créer un conteneur vertical pour l'image, le nom et les boutons d'action
            VBox itemContainer = new VBox(5);
            itemContainer.setAlignment(Pos.CENTER);

            // Ajouter l'image
            itemContainer.getChildren().add(imageView);

            // Ajouter le nom de l'œuvre
            Text nomOeuvre = new Text(oeuvre.getNom());
            nomOeuvre.setStyle("-fx-font-size: 12px;");
            itemContainer.getChildren().add(nomOeuvre);

            // Créer et ajouter la barre d'action
            HBox actionBar = createActionBar(oeuvre);
            actionBar.setAlignment(Pos.CENTER);
            itemContainer.getChildren().add(actionBar);

            // Ajouter au conteneur principal
            imageGroup.getChildren().add(itemContainer);
            imageContainer.getChildren().add(imageGroup);
        }
    }
    // Créer l'ImageView avec effets et gestion des événements
    private ImageView createImageView(Oeuvre oeuvre) {
        ImageView imageView = new ImageView();
        String imagePath = oeuvre.getImage();

        // Tailles aléatoires pour un effet Pinterest
        double baseWidth = 200 + random.nextInt(150); // Largeur entre 200 et 350
        double baseHeight = 250 + random.nextInt(200); // Hauteur entre 250 et 450

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image("file:" + imagePath);
                imageView.setImage(image);

                // Définir la taille avec préservation du ratio
                imageView.setFitWidth(baseWidth);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                // Style minimal avec coins légèrement arrondis
                imageView.setStyle("-fx-background-radius: 8;");

                // Effet de survol Pinterest-like
                imageView.setOnMouseEntered(e -> {
                    imageView.setOpacity(0.9);
                    imageView.setCursor(javafx.scene.Cursor.HAND);
                    // Légère élévation au survol
                    DropShadow hoverShadow = new DropShadow();
                    hoverShadow.setRadius(8.0);
                    hoverShadow.setOffsetX(0.0);
                    hoverShadow.setOffsetY(2.0);
                    hoverShadow.setColor(Color.color(0, 0, 0, 0.15));
                    imageView.setEffect(hoverShadow);
                });

                imageView.setOnMouseExited(e -> {
                    imageView.setOpacity(1.0);
                    imageView.setEffect(null);
                });

                // Afficher les détails au clic
                imageView.setOnMouseClicked(e -> handleDetails(oeuvre));

            } catch (Exception e) {
                // Create a placeholder image instead of StackPane
                Image placeholderImage = new Image(getClass().getResourceAsStream("/placeholder.png")); // Make sure you have a placeholder.png in your resources
                imageView.setImage(placeholderImage);
                imageView.setFitWidth(baseWidth);
                imageView.setFitHeight(baseHeight);
                imageView.setPreserveRatio(false);
                imageView.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
            }
        } else {
            // Handle case where imagePath is null or empty
            Image placeholderImage = new Image(getClass().getResourceAsStream("/placeholder.png"));
            imageView.setImage(placeholderImage);
            imageView.setFitWidth(baseWidth);
            imageView.setFitHeight(baseHeight);
            imageView.setPreserveRatio(false);
            imageView.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");
        }

        return imageView;
    }

    // Créer la barre d'action avec les boutons
    // Créer la barre d'action avec les boutons utilisant vos propres icônes
    private HBox createActionBar(Oeuvre oeuvre) {
        HBox actionBar = new HBox(10); // Augmenté l'espacement entre les boutons
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(6, 0, 0, 0));

        // Utiliser vos propres icônes depuis le dossier resources/images
        Button editBtn = createCustomIconButton("/images/edit.png", "Modifier");
        editBtn.setOnAction(e -> handleEdit(oeuvre));

        Button deleteBtn = createCustomIconButton("/images/truc.png", "Supprimer");
        deleteBtn.setOnAction(e -> handleDelete(oeuvre));

        Button view3DBtn = createCustomIconButton("/images/3d.png", "Vue 3D");
        view3DBtn.setOnAction(e -> handle3DView(oeuvre));

        Button commentBtn = createCustomIconButton("/images/comment.png", "Commentaires");
        commentBtn.setOnAction(e -> handleComments(oeuvre));

        actionBar.getChildren().addAll(editBtn, deleteBtn, view3DBtn, commentBtn);

        return actionBar;
    }
    // Dans ShowOeuvres.java
    private void handleComments(Oeuvre oeuvre) {
        try {
            // Créer et utiliser le contrôleur de commentaires
            CommentaireController commentaireController = new CommentaireController();
            commentaireController.showCommentsDialog(oeuvre);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des commentaires: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'affichage des commentaires: " + e.getMessage());
        }
    }

    // Méthode pour créer un bouton avec une icône personnalisée
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

    // Pinterest utilise des boutons très subtils, uniquement avec icône
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


    private void setupDynamicSearch() {
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                performSearch(newValue);
            }
        });
    }

    // Méthode pour effectuer la recherche
    private void performSearch(String searchTerm) {
        try {
            List<Oeuvre> allOeuvres = oeuvreService.getAll();

            // Filtrer par type si un filtre est actif
            if (currentFilter != null) {
                allOeuvres = allOeuvres.stream()
                        .filter(o -> currentFilter.equalsIgnoreCase(o.getType()))
                        .collect(Collectors.toList());
            }

            // Filtrer par terme de recherche si présent
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchTermLower = searchTerm.toLowerCase();
                allOeuvres = allOeuvres.stream()
                        .filter(oeuvre ->
                                oeuvre.getNom().toLowerCase().contains(searchTermLower) ||
                                        (oeuvre.getDescription() != null &&
                                                oeuvre.getDescription().toLowerCase().contains(searchTermLower))
                        ).collect(Collectors.toList());
            }

            imageContainer.getChildren().clear();
            displayedOeuvreIds.clear();
            addOeuvresToContainer(allOeuvres);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }
    private Button createFilterButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 24; -fx-padding: 8 16;");
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("#e60023")) {
                button.setStyle("-fx-background-color: #d0d0d0; -fx-text-fill: #333333; -fx-background-radius: 24; -fx-padding: 8 16;");
            }
        });
        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains("#e60023")) {
                button.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 24; -fx-padding: 8 16;");
            }
        });
        return button;
    }

    private void resetFilterButtonsStyle() {
        for (Node node : filterButtonsContainer.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; -fx-background-radius: 24; -fx-padding: 8 16;");
            }
        }
    }
    private void filterByType(String type) {
        try {
            List<Oeuvre> allOeuvres = oeuvreService.getAll();
            List<Oeuvre> filteredOeuvres = allOeuvres.stream()
                    .filter(oeuvre -> type.equalsIgnoreCase(oeuvre.getType()))
                    .collect(Collectors.toList());

            imageContainer.getChildren().clear();
            displayedOeuvreIds.clear();
            addOeuvresToContainer(filteredOeuvres);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du filtrage par type: " + e.getMessage());
        }
    }
    private void setupTypeFilters() {
        // Create "All" filter button
        Button allButton = createFilterButton("Tous");
        allButton.setStyle("-fx-background-color: #e60023; -fx-text-fill: white; -fx-background-radius: 24; -fx-padding: 8 16;");
        allButton.setOnAction(e -> {
            resetFilterButtonsStyle();
            allButton.setStyle("-fx-background-color: #e60023; -fx-text-fill: white; -fx-background-radius: 24; -fx-padding: 8 16;");
            currentFilter = null;
            populateImages();
        });
        filterButtonsContainer.getChildren().add(allButton);

        // Create filter buttons for each type
        for (String type : types) {
            Button typeButton = createFilterButton(type);
            typeButton.setOnAction(e -> {
                resetFilterButtonsStyle();
                typeButton.setStyle("-fx-background-color: #e60023; -fx-text-fill: white; -fx-background-radius: 24; -fx-padding: 8 16;");
                currentFilter = type;
                filterByType(type);
            });
            filterButtonsContainer.getChildren().add(typeButton);
        }
    }

}