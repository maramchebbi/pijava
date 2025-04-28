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
import javafx.scene.control.TextField;
// Pour TextField
import javafx.scene.control.Label;  // L'import essentiel
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
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
import java.util.Map;               // Interface générale
import java.util.HashMap;           // Implémentation spécifique
import java.util.TreeMap;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

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
    @FXML
    private HBox colorFilterContainer;

    @FXML
    private HBox materialFilterContainer;

    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private ComboBox<String> colorComboBox;
    @FXML
    private ComboBox<String> materialComboBox;
    @FXML
    private HBox filterContainer;

    private String currentColorFilter = null;
    private String currentMaterialFilter = null;

    // Définir vos couleurs avec leurs codes hex pour l'affichage
    private final Map<String, String> colors = Map.of(
            "Rouge", "#FF4040",
            "Bleu", "#4080FF",
            "Vert", "#40FF80",
            "Jaune", "#FFFF40",
            "Orange", "#FF8040",
            "Violet", "#8040FF",
            "Rose", "#FF80B0",
            "Gris", "#B0B0B0",
            "Noir", "#202020",
            "Blanc", "#FFFFFF"
    );

    // Définir vos matières
    private final List<String> materials = List.of(
            "Argile",
            "Céramique",
            "Porcelaine",
            "Verre",
            "Terre cuite",
            "Grès",
            "Faïence"
    );

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

        // Setup the filter UI
        setupFilters();

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
//        Button editBtn = createCustomIconButton("/images/edit.png", "Modifier");
//        editBtn.setOnAction(e -> handleEdit(oeuvre));
        Button editBtn = createCustomIconButton("/images/edit.png", "Modifier");
        editBtn.setOnAction(event -> {
            handleEdit(oeuvre, editBtn);  // Pass both the oeuvre and the button
        });

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

            // Déterminer le type d'objet (vous devez avoir ce champ dans votre modèle)
            String type = oeuvre.getType(); // "Vase", "Sculpture", etc.

            // Extraire les dimensions à partir de votre attribut "dimensions"
            // Par défaut si pas de dimensions spécifiques
            double width = 200;
            double height = 300;
            double depth = 200;

            // Si vous avez un attribut dimensions, vous pouvez essayer de l'analyser
            if (oeuvre.getDimensions() != null && !oeuvre.getDimensions().isEmpty()) {
                try {
                    // Supposons que dimensions soit au format "LxHxP" en cm
                    String[] dims = oeuvre.getDimensions().split("x");
                    if (dims.length >= 2) {
                        width = Double.parseDouble(dims[0]);
                        height = Double.parseDouble(dims[1]);
                        depth = dims.length > 2 ? Double.parseDouble(dims[2]) : width;
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors du parsing des dimensions: " + e.getMessage());
                    // Continuer avec les valeurs par défaut
                }
            }

            // Afficher l'œuvre en 3D avec sa propre image
            viewer.displayArtwork(
                    type,
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
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();

            detail detailController = loader.getController();
            detailController.setOeuvreDetails(oeuvre);

            Scene scene = new Scene(root, screenWidth, screenHeight);

            // Create a new stage instead of using sourceNode
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Détails de l'œuvre");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails");
            e.printStackTrace();
        }
    }
    private void handleEdit(Oeuvre oeuvre, Node sourceNode) {  // Added sourceNode parameter
        try {
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Load the edit view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            // Configure controller
            edit editController = loader.getController();
            editController.setOeuvreDetails(oeuvre);

            // Create new scene
            Scene scene = new Scene(root, screenWidth, screenHeight);

            // Get current stage from the source node
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Modifier l'œuvre");
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger l'interface d'édition");
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

            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            // Effet de transition
            Scene scene = new Scene(root,screenWidth,screenHeight);
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
            // Get screen dimensions
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Load the edit view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddWorkshop.fxml"));
            Parent workshopRoot = loader.load();

            // Configure controller
            AddWorkshop controller = loader.getController();

            // Create new scene
            Scene scene = new Scene(workshopRoot, screenWidth, screenHeight);

            // Get current stage from the source node
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Ajouter Workshop");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'interface de workshop: " + e.getMessage());
        }
    }
    @FXML
    private void affichercollectionaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Collections.fxml"));
            Parent root = loader.load();
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            // Transition fluide
            Scene scene = new Scene(root,screenWidth,screenHeight);
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

        // Default style (inactive)
        button.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333333; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 6 14; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");

        // Hover effect
        button.setOnMouseEntered(e -> {
            if (!button.getStyle().contains("#e60023")) {
                button.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-padding: 6 14; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-border-color: #d0d0d0; -fx-border-width: 1px;");
            }
        });

        // Return to default style when not hovered
        button.setOnMouseExited(e -> {
            if (!button.getStyle().contains("#e60023")) {
                button.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-padding: 6 14; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
            }
        });

        return button;
    }

    private void resetFilterButtonsStyle() {
        for (Node node : filterButtonsContainer.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-padding: 6 14; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
            }
        }
    }
    private void setActiveFilterStyle(Button button) {
        button.setStyle("-fx-background-color: #e60023; -fx-text-fill: white; " +
                "-fx-background-radius: 20; -fx-border-radius: 20; " +
                "-fx-padding: 6 14; -fx-cursor: hand; " +
                "-fx-font-size: 12px; -fx-border-color: #e60023; -fx-border-width: 1px;");
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
        applyAllFilters();

    }
    private void setupTypeFilters() {
        // Make sure the container exists and is cleared
        if (filterButtonsContainer == null) {
            System.err.println("Error: filterButtonsContainer is null");
            return;
        }

        // Clear existing buttons to prevent duplication
        filterButtonsContainer.getChildren().clear();

        // Create "All" filter button
        Button allButton = createFilterButton("Tous");
        setActiveFilterStyle(allButton);
        allButton.setOnAction(e -> {
            resetFilterButtonsStyle();
            setActiveFilterStyle(allButton);
            currentFilter = null;
            applyAllFilters();
        });
        filterButtonsContainer.getChildren().add(allButton);

        // Create filter buttons for each type
        for (String type : types) {
            Button typeButton = createFilterButton(type);
            typeButton.setOnAction(e -> {
                resetFilterButtonsStyle();
                setActiveFilterStyle(typeButton);
                currentFilter = type;
                applyAllFilters();
            });
            filterButtonsContainer.getChildren().add(typeButton);
        }
    }
    private void setupColorFilters() {
        colorFilterContainer.getChildren().clear();

        // Create "All" button for colors with the same styling as other filter buttons
        Button allButton = createFilterButton("Tous");
        setActiveFilterStyle(allButton);
        allButton.setOnAction(e -> {
            resetColorFilterButtonsStyle();
            setActiveFilterStyle(allButton);
            currentColorFilter = null;
            applyAllFilters();
        });
        colorFilterContainer.getChildren().add(allButton);

        // Create color buttons with improved styling
        for (Map.Entry<String, String> entry : colors.entrySet()) {
            String colorName = entry.getKey();
            String colorCode = entry.getValue();

            // Create a circular button for color
            Button colorButton = new Button();
            colorButton.setPrefSize(28, 28);
            colorButton.setMinSize(28, 28);
            colorButton.setMaxSize(28, 28);

            // Default style with white border
            colorButton.setStyle("-fx-background-color: " + colorCode + "; " +
                    "-fx-background-radius: 14; -fx-border-radius: 14; " +
                    "-fx-border-color: white; -fx-border-width: 2; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 1, 0, 0, 1);");

            // Add tooltip with color name
            Tooltip tooltip = new Tooltip(colorName);
            colorButton.setTooltip(tooltip);

            // Configure button action
            colorButton.setOnAction(e -> {
                resetColorFilterButtonsStyle();
                // Active style with prominent border
                colorButton.setStyle("-fx-background-color: " + colorCode + "; " +
                        "-fx-background-radius: 14; -fx-border-radius: 14; " +
                        "-fx-border-color: #e60023; -fx-border-width: 2; " +
                        "-fx-effect: dropshadow(gaussian, rgba(230,0,35,0.3), 4, 0, 0, 0);");
                currentColorFilter = colorName;
                applyAllFilters();
            });

            // Hover effect
            colorButton.setOnMouseEntered(e -> {
                if (!colorButton.getStyle().contains("#e60023")) {
                    colorButton.setStyle("-fx-background-color: " + colorCode + "; " +
                            "-fx-background-radius: 14; -fx-border-radius: 14; " +
                            "-fx-border-color: #e0e0e0; -fx-border-width: 2; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0, 0, 1);");
                }
            });

            colorButton.setOnMouseExited(e -> {
                if (!colorButton.getStyle().contains("#e60023")) {
                    colorButton.setStyle("-fx-background-color: " + colorCode + "; " +
                            "-fx-background-radius: 14; -fx-border-radius: 14; " +
                            "-fx-border-color: white; -fx-border-width: 2; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 1, 0, 0, 1);");
                }
            });

            colorFilterContainer.getChildren().add(colorButton);
        }
    }


    private void setupMaterialFilters() {
        materialFilterContainer.getChildren().clear();

        // Create "All" button for materials
        Button allButton = createFilterButton("Tous");
        setActiveFilterStyle(allButton);
        allButton.setOnAction(e -> {
            resetMaterialFilterButtonsStyle();
            setActiveFilterStyle(allButton);
            currentMaterialFilter = null;
            applyAllFilters();
        });
        materialFilterContainer.getChildren().add(allButton);

        // Create buttons for each material
        for (String material : materials) {
            Button materialButton = createFilterButton(material);
            materialButton.setOnAction(e -> {
                resetMaterialFilterButtonsStyle();
                setActiveFilterStyle(materialButton);
                currentMaterialFilter = material;
                applyAllFilters();
            });
            materialFilterContainer.getChildren().add(materialButton);
        }
    }
    private void resetColorFilterButtonsStyle() {
        for (Node node : colorFilterContainer.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                if (button.getTooltip() == null) {
                    // "All" button
                    button.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333333; " +
                            "-fx-background-radius: 20; -fx-border-radius: 20; " +
                            "-fx-padding: 6 14; -fx-cursor: hand; " +
                            "-fx-font-size: 12px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
                } else {
                    // Color button
                    String colorName = button.getTooltip().getText();
                    String colorCode = colors.get(colorName);
                    button.setStyle("-fx-background-color: " + colorCode + "; " +
                            "-fx-background-radius: 14; -fx-border-radius: 14; " +
                            "-fx-border-color: white; -fx-border-width: 2; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 1, 0, 0, 1);");
                }
            }
        }
    }


    private void resetMaterialFilterButtonsStyle() {
        for (Node node : materialFilterContainer.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333333; " +
                        "-fx-background-radius: 20; -fx-border-radius: 20; " +
                        "-fx-padding: 6 14; -fx-cursor: hand; " +
                        "-fx-font-size: 12px; -fx-border-color: #e0e0e0; -fx-border-width: 1px;");
            }
        }
    }

    // Méthode qui applique tous les filtres (type, couleur et matière) en même temps
    private void applyAllFilters() {
        try {
            List<Oeuvre> allOeuvres = oeuvreService.getAll();
            List<Oeuvre> filteredOeuvres = allOeuvres;

            // Apply type filter if active
            if (currentFilter != null) {
                filteredOeuvres = filteredOeuvres.stream()
                        .filter(oeuvre -> currentFilter.equalsIgnoreCase(oeuvre.getType()))
                        .collect(Collectors.toList());
            }

            // Apply color filter if active
            if (currentColorFilter != null) {
                filteredOeuvres = filteredOeuvres.stream()
                        .filter(oeuvre -> oeuvre.getCouleur() != null &&
                                currentColorFilter.equalsIgnoreCase(oeuvre.getCouleur()))
                        .collect(Collectors.toList());
            }

            // Apply material filter if active
            if (currentMaterialFilter != null) {
                filteredOeuvres = filteredOeuvres.stream()
                        .filter(oeuvre -> oeuvre.getMatiere() != null &&
                                currentMaterialFilter.equalsIgnoreCase(oeuvre.getMatiere()))
                        .collect(Collectors.toList());
            }

            // Apply search filter if active
            String searchTerm = searchField.getText();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchTermLower = searchTerm.toLowerCase();
                filteredOeuvres = filteredOeuvres.stream()
                        .filter(oeuvre ->
                                oeuvre.getNom().toLowerCase().contains(searchTermLower) ||
                                        (oeuvre.getDescription() != null &&
                                                oeuvre.getDescription().toLowerCase().contains(searchTermLower))
                        ).collect(Collectors.toList());
            }

            // Display filtered results
            imageContainer.getChildren().clear();
            displayedOeuvreIds.clear();
            addOeuvresToContainer(filteredOeuvres);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'application des filtres: " + e.getMessage());
        }
    }

    private void setupFilters() {
        // Create a container for filters if it doesn't exist in FXML
        if (filterContainer == null) {
            filterContainer = new HBox(20);
            filterContainer.setPadding(new Insets(10, 15, 10, 15));
            filterContainer.setAlignment(Pos.CENTER);

            // Make sure to add this to your layout
            // Find the parent node that should contain your filters
            VBox mainContainer = (VBox) scrollPane.getParent();
            // Add filterContainer at index 0 (top) of the VBox
            if (!mainContainer.getChildren().contains(filterContainer)) {
                mainContainer.getChildren().add(0, filterContainer);
            }
        } else {
            // Clear existing contents to prevent duplication
            filterContainer.getChildren().clear();
        }

        // Initialize ComboBoxes only if they don't exist
        if (typeComboBox == null) typeComboBox = new ComboBox<>();
        if (colorComboBox == null) colorComboBox = new ComboBox<>();
        if (materialComboBox == null) materialComboBox = new ComboBox<>();

        // Add "All" option and populate with values
        List<String> typeOptions = new ArrayList<>(types);
        typeOptions.add(0, "Tous les types");

        List<String> colorOptions = new ArrayList<>(colors.keySet());
        colorOptions.add(0, "Toutes les couleurs");

        List<String> materialOptions = new ArrayList<>(materials);
        materialOptions.add(0, "Toutes les matières");

        // Set items
        typeComboBox.setItems(FXCollections.observableArrayList(typeOptions));
        colorComboBox.setItems(FXCollections.observableArrayList(colorOptions));
        materialComboBox.setItems(FXCollections.observableArrayList(materialOptions));

        // Select default "All" options
        typeComboBox.getSelectionModel().selectFirst();
        colorComboBox.getSelectionModel().selectFirst();
        materialComboBox.getSelectionModel().selectFirst();

        // Style the ComboBoxes
        String comboStyle = "-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 4; " +
                "-fx-padding: 5; " +
                "-fx-pref-width: 180px;";

        typeComboBox.setStyle(comboStyle);
        colorComboBox.setStyle(comboStyle);
        materialComboBox.setStyle(comboStyle);

        // Add some prompt text
        typeComboBox.setPromptText("Type");
        colorComboBox.setPromptText("Couleur");
        materialComboBox.setPromptText("Matière");

        // Add listeners
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentFilter = newVal.equals("Tous les types") ? null : newVal;
                applyAllFilters();
            }
        });

        colorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentColorFilter = newVal.equals("Toutes les couleurs") ? null : newVal;
                applyAllFilters();
            }
        });

        materialComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentMaterialFilter = newVal.equals("Toutes les matières") ? null : newVal;
                applyAllFilters();
            }
        });

        // Create labels for each filter
        Label typeLabel = new Label("Type:");
        Label colorLabel = new Label("Couleur:");
        Label materialLabel = new Label("Matière:");

        String labelStyle = "-fx-font-weight: bold; -fx-font-size: 13px;";
        typeLabel.setStyle(labelStyle);
        colorLabel.setStyle(labelStyle);
        materialLabel.setStyle(labelStyle);

        // Create HBoxes for each label+combobox pair
        HBox typeBox = new HBox(10, typeLabel, typeComboBox);
        HBox colorBox = new HBox(10, colorLabel, colorComboBox);
        HBox materialBox = new HBox(10, materialLabel, materialComboBox);

        typeBox.setAlignment(Pos.CENTER_LEFT);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        materialBox.setAlignment(Pos.CENTER_LEFT);

        // Add them to the filter container
        filterContainer.getChildren().addAll(typeBox, colorBox, materialBox);

        // Add a reset button
        Button resetButton = new Button("Réinitialiser");
        resetButton.setStyle("-fx-background-color: #f8f8f8; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 4; " +
                "-fx-padding: 6 12;");

        resetButton.setOnAction(e -> {
            typeComboBox.getSelectionModel().selectFirst();
            colorComboBox.getSelectionModel().selectFirst();
            materialComboBox.getSelectionModel().selectFirst();
            currentFilter = null;
            currentColorFilter = null;
            currentMaterialFilter = null;
            applyAllFilters();
        });

        filterContainer.getChildren().add(resetButton);
    }


}