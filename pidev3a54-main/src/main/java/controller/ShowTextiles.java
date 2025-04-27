package controller;

import Models.collection_t;
import Models.textile;
import Models.Vote;
import Services.CollectionTService;
import Services.TextileService;
import Services.VoteService;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ShowTextiles {

    @FXML private FlowPane imageContainer;
    @FXML private ComboBox<collection_t> collectionComboBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Button themeToggleButton;
    @FXML private SVGPath themeIcon;

    private TextileService textileService;
    private CollectionTService collectionTService;
    private VoteService voteService;

    // Simuler l'ID de l'utilisateur actuel (à remplacer par votre système de connexion)
    private int currentUserId = 1;

    public ShowTextiles() {
        textileService = new TextileService();
        collectionTService = new CollectionTService();
        voteService = new VoteService();
    }

    public void initialize()  {
        System.out.println("ShowController initialized");
        setupSearchField();
        setupSortComboBox();
        populateTextileCards();
        setupComboBox();

    }





    @FXML


    private void setupSearchField() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue.trim().isEmpty()) {
                        // Si le champ de recherche est vide, afficher tous les textiles
                        populateTextileCards();
                    } else {
                        // Sinon, filtrer les textiles selon la recherche
                        searchTextiles(newValue);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur de recherche", "Une erreur est survenue lors de la recherche", Alert.AlertType.ERROR);
                }
            });
        }
    }


    private void setupSortComboBox() {
        if (sortComboBox != null) {
            // Définir les options de tri
            ObservableList<String> sortOptions = FXCollections.observableArrayList(
                    "Note (la plus haute)",
                    "Note (la plus basse)",
                    "Nom (A-Z)",
                    "Nom (Z-A)"
            );
            sortComboBox.setItems(sortOptions);
            sortComboBox.getSelectionModel().selectFirst();

            // Gérer les changements de sélection
            sortComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue != null) {
                        sortTextiles(newValue);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Erreur de tri", "Une erreur est survenue lors du tri", Alert.AlertType.ERROR);
                }
            });
        }
    }

    private void sortTextiles(String sortOption) throws SQLException {
        List<textile> textiles;

        switch (sortOption) {
            case "Note (la plus haute)":
                textiles = getTextilesSortedByRating(true);
                break;
            case "Note (la plus basse)":
                textiles = getTextilesSortedByRating(false);
                break;
            case "Nom (A-Z)":
                textiles = textileService.getSortedTextiles("nom", true);
                break;
            case "Nom (Z-A)":
                textiles = textileService.getSortedTextiles("nom", false);
                break;
            default:
                textiles = textileService.getAll();
                break;
        }

        // Afficher les textiles triés
        imageContainer.getChildren().clear();
        for (textile t : textiles) {
            imageContainer.getChildren().add(createTextileCard(t));
        }
    }

    private List<textile> getTextilesSortedByRating(boolean highestFirst) throws SQLException {
        // Obtenir tous les textiles
        List<textile> textiles = textileService.getAll();

        // Créer une Map pour stocker les notes moyennes
        Map<Integer, Double> ratingsMap = new HashMap<>();

        // Calculer la note moyenne pour chaque textile
        for (textile t : textiles) {
            double avgRating = voteService.getAverageRating(t.getId());
            ratingsMap.put(t.getId(), avgRating);
        }

        // Trier la liste selon les notes moyennes
        textiles.sort((t1, t2) -> {
            double rating1 = ratingsMap.getOrDefault(t1.getId(), 0.0);
            double rating2 = ratingsMap.getOrDefault(t2.getId(), 0.0);

            // Si les notes sont égales, trier par nom
            if (Math.abs(rating1 - rating2) < 0.001) {
                return t1.getNom().compareToIgnoreCase(t2.getNom());
            }

            // Sinon, trier par note (ascendant ou descendant selon le paramètre)
            return highestFirst
                    ? Double.compare(rating2, rating1) // Note la plus haute d'abord
                    : Double.compare(rating1, rating2); // Note la plus basse d'abord
        });

        return textiles;
    }


    private void searchTextiles(String query) throws SQLException {
        // Rechercher les textiles correspondant à la requête
        List<textile> textiles = textileService.searchTextiles(query);
        imageContainer.getChildren().clear();

        for (textile t : textiles) {
            imageContainer.getChildren().add(createTextileCard(t));
        }
    }

    private void setupComboBox() {
        Task<List<collection_t>> loadCollectionsTask = new Task<List<collection_t>>() {
            @Override
            protected List<collection_t> call() throws SQLException {
                return collectionTService.getAll();
            }
        };

        loadCollectionsTask.setOnSucceeded(event -> {
            List<collection_t> collections = loadCollectionsTask.getValue();

            collection_t defaultOption = new collection_t();
            defaultOption.setId(-1);
            defaultOption.setNom("Toutes les collections");

            ObservableList<collection_t> collectionComboBoxItems = FXCollections.observableArrayList();
            collectionComboBoxItems.add(defaultOption);
            collectionComboBoxItems.addAll(collections);

            collectionComboBox.setItems(collectionComboBoxItems);
            collectionComboBox.setCellFactory(param -> new ListCell<collection_t>() {
                @Override
                protected void updateItem(collection_t item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });

            collectionComboBox.setButtonCell(collectionComboBox.getCellFactory().call(null));
            collectionComboBox.getSelectionModel().selectFirst();
        });

        loadCollectionsTask.setOnFailed(event -> {
            loadCollectionsTask.getException().printStackTrace();
            System.out.println("Failed to load collections.");
        });

        new Thread(loadCollectionsTask).start();
    }

    private void populateTextileCards() {
        try {
            List<textile> textiles = textileService.getAll();
            imageContainer.getChildren().clear();

            for (textile t : textiles) {
                imageContainer.getChildren().add(createTextileCard(t));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur de chargement", "Impossible de charger les textiles", Alert.AlertType.ERROR);
        }
    }

    private VBox createTextileCard(textile t) {
        // Carte principale avec bords arrondis et ombre
        VBox card = new VBox();
        card.setUserData(t); // Stocke l'objet textile dans la carte
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                "-fx-border-color: #f0f0f0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-spacing: 0; " +
                "-fx-max-width: 300; " +  // Changé de 280px à 300px
                "-fx-min-width: 300; " +  // Changé de 280px à 300px
                "-fx-alignment: center;"); // Centrer le contenu de la carte

        // Conteneur d'image amélioré avec coins arrondis
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 12 12 0 0; -fx-padding: 10 0 0 0;"); // Ajout de padding en haut

        // Image avec coins arrondis
        ImageView imageView = new ImageView();
        try {
            Image image = new Image("file:" + t.getImage());
            imageView.setImage(image);
            imageView.setFitWidth(300);  // Changé de 280px à 300px
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);

            // Création d'un Rectangle pour le masque de découpage (clip)
            Rectangle clip = new Rectangle(300, 180);  // Changé de 280px à 300px
            clip.setArcWidth(24);
            clip.setArcHeight(24);
            imageView.setClip(clip);

        } catch (Exception e) {
            // Image par défaut si l'image ne peut pas être chargée
            try {
                imageView.setImage(new Image("/images/default-textile.png"));
            } catch (Exception ex) {
                // Si même l'image par défaut ne peut pas être chargée, créer un rectangle coloré
                Rectangle placeholder = new Rectangle(300, 180);  // Changé de 280px à 300px
                placeholder.setFill(Color.web("#e0e0e0"));
                placeholder.setArcWidth(24);
                placeholder.setArcHeight(24);
                imageContainer.getChildren().add(placeholder);

                // Ajouter un texte "Pas d'image" centré
                Label noImageLabel = new Label("Image non disponible");
                noImageLabel.setStyle("-fx-text-fill: #757575; -fx-font-style: italic;");
                imageContainer.getChildren().add(noImageLabel);
            }
        }

        imageContainer.getChildren().add(imageView);

        // Badge de collection redessiné
        Label collectionBadge = new Label("Collection"); // Valeur par défaut
        try {
            Integer collectionId = t.getCollectionId();
            if (collectionId != null) {
                String collectionName = textileService.getCollectionNameById(collectionId);
                collectionBadge.setText(collectionName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du nom de collection: " + e.getMessage());
            // Garder la valeur par défaut
        }

        // Le style reste le même
        collectionBadge.setStyle("-fx-background-color: rgba(96, 56, 19, 0.85); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 5 12; " +
                "-fx-background-radius: 15; " +
                "-fx-font-size: 12; " +
                "-fx-font-weight: bold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        StackPane.setAlignment(collectionBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(collectionBadge, new Insets(12, 12, 0, 0));
        imageContainer.getChildren().add(collectionBadge);

        // Zone de contenu de la carte avec espacement amélioré
        VBox contentBox = new VBox();
        contentBox.setStyle("-fx-spacing: 8; -fx-padding: 20 20 16 20;"); // Augmentation du padding

        // Titre et type dans une même boîte avec des styles améliorés
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setSpacing(10);

        // Nom du textile
        Label nameLabel = new Label(t.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #603813; -fx-font-size: 17px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(190); // Ajusté pour la nouvelle largeur

        // Type de textile comme badge
        Label typeLabel = new Label(t.getType());
        typeLabel.setStyle("-fx-background-color: #f0e6e1; " +
                "-fx-text-fill: #603813; " +
                "-fx-padding: 4 10; " +
                "-fx-background-radius: 12; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold;");

        titleBox.getChildren().addAll(nameLabel, typeLabel);

        // Aperçu de la description avec formatage amélioré
        String description = t.getDescription();
        if (description.length() > 120) {
            description = description.substring(0, 117) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #5c5c5c; -fx-font-size: 13px; -fx-line-spacing: 1.3;");
        descLabel.setWrapText(true);

        // Affichage des évaluations amélioré
        HBox ratingBox = new HBox();
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        ratingBox.setSpacing(5);
        ratingBox.setStyle("-fx-padding: 8 0 5 0;");

        try {
            // Obtenir la note moyenne et le nombre de votes
            double avgRating = voteService.getAverageRating(t.getId());
            int voteCount = voteService.getVoteCount(t.getId());

            // Arrondir la moyenne à l'entier le plus proche
            int roundedRating = (int) Math.round(avgRating);

            // Créer l'affichage des étoiles
            for (int i = 1; i <= 5; i++) {
                SVGPath starPath = new SVGPath();
                starPath.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");

                if (i <= roundedRating) {
                    starPath.setFill(Color.web("#FFC107")); // Doré pour les étoiles remplies
                } else {
                    starPath.setFill(Color.web("#E0E0E0")); // Gris pour les étoiles vides
                }

                // Amélioration de l'effet visuel des étoiles
                starPath.setScaleX(1.1);
                starPath.setScaleY(1.1);
                starPath.setEffect(new javafx.scene.effect.DropShadow(2, 0, 1, Color.rgb(0, 0, 0, 0.2)));

                ratingBox.getChildren().add(starPath);
            }

            // Ajouter le nombre de votes
            Label voteCountLabel = new Label("(" + voteCount + ")");
            voteCountLabel.setStyle("-fx-text-fill: #603813; -fx-font-size: 13px; -fx-padding: 0 0 0 5;");
            ratingBox.getChildren().add(voteCountLabel);

        } catch (SQLException e) {
            System.err.println("Erreur de chargement des évaluations: " + e.getMessage());
            // Ajouter un texte placeholder en cas d'erreur
            Label errorLabel = new Label("Évaluation non disponible");
            errorLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            ratingBox.getChildren().add(errorLabel);
        }

        // Ajouter le contenu à la boîte de contenu
        contentBox.getChildren().addAll(titleBox, descLabel, ratingBox);

        // Séparateur avec style amélioré
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 0 15; -fx-opacity: 0.6;");

        // Boutons d'action modernisés
        HBox buttonsBox = new HBox();
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setSpacing(20);
        buttonsBox.setStyle("-fx-padding: 10 15;");

        // Bouton Détails amélioré
        Button detailsBtn = createActionButton(
                "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z",
                "#603813", "Voir détails");
        detailsBtn.setOnAction(this::handleDetails);

        // Bouton Modifier amélioré
        Button editBtn = createActionButton(
                "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
                "#603813", "Modifier");
        editBtn.setOnAction(this::handleEdit);

        // Bouton Supprimer amélioré
        Button deleteBtn = createActionButton(
                "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z",
                "#603813", "Supprimer");
        deleteBtn.setOnAction(this::handleDelete);

        // Bouton Noter amélioré
        Button rateBtn = createActionButton(
                "M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.27 1-4.28-3.32-2.88 4.38-.38L12 6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z",
                "#FFC107", "Noter");
        rateBtn.setOnAction(event -> handleRate(event, t));

        // Bouton QR Code amélioré
        Button qrCodeBtn = createActionButton(
                "M3 3h5v5H3zm1 1v3h3V4zm2 2H5V5h1zM3 16h5v5H3zm1 1v3h3v-3zm2 2H5v-1h1z"
                        + "M16 3h5v5h-5zm1 1v3h3V4zm2 2h-1V5h1zM11 11h2v2h-2zm3 0h2v2h-2zm3 0h2v2h-2z"
                        + "M17 14h2v2h-2zm-6-3h2v2h-2zm3 3h2v2h-2z",
                "#603813", "QR Code");

        // Ajouter l'action au bouton QR Code
        qrCodeBtn.setOnAction(event -> handleQRCode(event, t));

        buttonsBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn, rateBtn, qrCodeBtn);

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageContainer, contentBox, separator, buttonsBox);

        // Ajouter un effet de survol amélioré
        card.setOnMouseEntered(event -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(96,56,19,0.25), 15, 0, 0, 6); " +
                    "-fx-border-color: #f0f0f0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-spacing: 0; " +
                    "-fx-max-width: 300; " +  // Changé de 280px à 300px
                    "-fx-min-width: 300; " +  // Changé de 280px à 300px
                    "-fx-alignment: center; " + // Centrer le contenu de la carte
                    "-fx-translate-y: -3; " +
                    "-fx-cursor: hand;");
        });

        card.setOnMouseExited(event -> {
            card.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4); " +
                    "-fx-border-color: #f0f0f0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-spacing: 0; " +
                    "-fx-max-width: 300; " +  // Changé de 280px à 300px
                    "-fx-min-width: 300; " +  // Changé de 280px à 300px
                    "-fx-alignment: center; " + // Centrer le contenu de la carte
                    "-fx-translate-y: 0;");
        });

        return card;
    }
    @FXML
    private void handleRate(ActionEvent event, textile t) {
        try {
            // Create a custom dialog
            Dialog<Integer> ratingDialog = new Dialog<>();
            ratingDialog.setTitle("Évaluer le textile");

            // Make the dialog more attractive
            DialogPane dialogPane = ratingDialog.getDialogPane();
            try {
                URL styleUrl = getClass().getResource("/css_files/style.css");
                if (styleUrl != null) {
                    dialogPane.getStylesheets().add(styleUrl.toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Could not load stylesheet: " + e.getMessage());
            }

            dialogPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;");

            // Create a better layout for the dialog content
            VBox mainContent = new VBox(20);
            mainContent.setAlignment(Pos.CENTER);
            mainContent.setStyle("-fx-padding: 10;");

            // Add textile image and name for context
            if (t.getImage() != null && !t.getImage().isEmpty()) {
                try {
                    ImageView dialogImageView = new ImageView(new Image("file:" + t.getImage()));
                    dialogImageView.setFitHeight(150);
                    dialogImageView.setFitWidth(150);
                    dialogImageView.setPreserveRatio(true);

                    StackPane imageContainer = new StackPane();
                    imageContainer.getChildren().add(dialogImageView);
                    imageContainer.setStyle("-fx-background-color: white; -fx-padding: 5; " +
                            "-fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                    mainContent.getChildren().add(imageContainer);
                } catch (Exception e) {
                    System.err.println("Error loading image in dialog: " + e.getMessage());
                }
            }

            // Add textile name
            Label nameLabel = new Label(t.getNom());
            nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #603813;");
            mainContent.getChildren().add(nameLabel);

            // Add rating prompt
            Label promptLabel = new Label("Comment évaluez-vous ce textile?");
            promptLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666666;");
            mainContent.getChildren().add(promptLabel);

            // Attempt to get the user's existing vote
            int currentRating = 0;
            try {
                Vote userVote = voteService.getUserVote(t.getId(), currentUserId);
                if (userVote != null) {
                    currentRating = userVote.getValue();
                }
            } catch (SQLException e) {
                System.err.println("Error getting user vote: " + e.getMessage());
            }

            // Create star rating buttons
            HBox starsContainer = new HBox(15);
            starsContainer.setAlignment(Pos.CENTER);
            starsContainer.setPadding(new Insets(10, 0, 10, 0));

            ToggleGroup ratingGroup = new ToggleGroup();

            for (int i = 1; i <= 5; i++) {
                final int rating = i;

                ToggleButton starButton = new ToggleButton();
                starButton.setToggleGroup(ratingGroup);
                starButton.setUserData(rating);
                starButton.setPrefSize(45, 45);
                starButton.setMinSize(45, 45);
                starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                        "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");

                // Create star icon
                SVGPath starPath = new SVGPath();
                starPath.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
                starPath.setFill(Color.web("#E0E0E0")); // Default gray

                // Create label for star value
                Label valueLabel = new Label(String.valueOf(i));
                valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #666;");

                // Stack the star and value
                StackPane starStack = new StackPane();
                starStack.getChildren().addAll(starPath, valueLabel);

                starButton.setGraphic(starStack);

                // Add hover effect
                starButton.setOnMouseEntered(e -> {
                    if (!starButton.isSelected()) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFF8E1; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#FFECB3")); // Light gold on hover
                    }
                });

                starButton.setOnMouseExited(e -> {
                    if (!starButton.isSelected()) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#E0E0E0")); // Back to gray
                    }
                });

                // Set initial state based on current rating
                if (i <= currentRating) {
                    starButton.setSelected(true);
                    starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFC107; " +
                            "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                    starPath.setFill(Color.WHITE);
                    valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
                }

                // Add listener for state changes
                starButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFC107; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.WHITE);
                        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
                    } else {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#E0E0E0"));
                        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #666;");
                    }
                });

                starsContainer.getChildren().add(starButton);
            }

            // Add explanation labels for the rating scale
            HBox explanationBox = new HBox();
            explanationBox.setAlignment(Pos.CENTER);
            explanationBox.setSpacing(10);

            Label minLabel = new Label("Pas satisfait");
            minLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label maxLabel = new Label("Très satisfait");
            maxLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            explanationBox.getChildren().addAll(minLabel, spacer, maxLabel);

            mainContent.getChildren().addAll(starsContainer, explanationBox);

            // Add custom buttons
            ButtonType submitButtonType = new ButtonType("Soumettre", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialogPane.getButtonTypes().addAll(submitButtonType, cancelButtonType);

            // Style the buttons
            Button submitButton = (Button) dialogPane.lookupButton(submitButtonType);
            submitButton.setStyle("-fx-background-color: #603813; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5;");

            Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
            cancelButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #666; " +
                    "-fx-background-radius: 5;");

            dialogPane.setContent(mainContent);

            // Convert the result
            ratingDialog.setResultConverter(dialogButton -> {
                if (dialogButton == submitButtonType) {
                    ToggleButton selected = (ToggleButton) ratingGroup.getSelectedToggle();
                    if (selected != null) {
                        return (Integer) selected.getUserData();
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            ratingDialog.showAndWait().ifPresent(rating -> {
                if (rating != null && rating > 0) {
                    try {
                        voteService.addOrUpdateVote(t.getId(), currentUserId, rating);
                        populateTextileCards(); // Refresh to show the new rating
                        showAlert("Succès", "Évaluation enregistrée",
                                "Votre évaluation a été enregistrée avec succès.", Alert.AlertType.INFORMATION);
                    } catch (SQLException e) {
                        showAlert("Erreur", "Erreur d'évaluation",
                                "Une erreur est survenue lors de l'enregistrement de votre évaluation: " + e.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Erreur", "Erreur de notation", "Une erreur est survenue: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Button createActionButton(String svgPath, String color, String tooltipText) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-background-radius: 50%;");

        SVGPath svg = new SVGPath();
        svg.setContent(svgPath);
        svg.setFill(Color.web(color));

        // Rendre l'icône légèrement plus grande
        svg.setScaleX(1.15);
        svg.setScaleY(1.15);

        // Ajouter un effet d'ombre subtil
        DropShadow shadow = new DropShadow();
        shadow.setRadius(2.0);
        shadow.setOffsetY(1.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        svg.setEffect(shadow);

        button.setGraphic(svg);

        // Amélioration de l'infobulle
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: normal;");
        button.setTooltip(tooltip);

        // Ajouter des effets de survol plus élégants
        button.setOnMouseEntered(event -> {
            button.setStyle("-fx-background-color: " + deriveColor(color, 0.1) + "; " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

            // Animation de l'icône
            ScaleTransition st = new ScaleTransition(Duration.millis(150), svg);
            st.setToX(1.3);
            st.setToY(1.3);
            st.play();
        });

        button.setOnMouseExited(event -> {
            button.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-background-radius: 50%;");

            // Animation de retour
            ScaleTransition st = new ScaleTransition(Duration.millis(150), svg);
            st.setToX(1.15);
            st.setToY(1.15);
            st.play();
        });

        // Effet de pression
        button.setOnMousePressed(event -> {
            button.setStyle("-fx-background-color: " + deriveColor(color, 0.2) + "; " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 10;");

            // Animation de pression
            ScaleTransition st = new ScaleTransition(Duration.millis(100), svg);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.setOnMouseReleased(event -> {
            button.setStyle("-fx-background-color: " + deriveColor(color, 0.1) + "; " +
                    "-fx-background-radius: 50%; " +
                    "-fx-padding: 10;");

            // Animation de relâchement
            ScaleTransition st = new ScaleTransition(Duration.millis(100), svg);
            st.setToX(1.3);
            st.setToY(1.3);
            st.play();
        });

        return button;
    }

    // Méthode utilitaire pour dériver une couleur plus claire ou plus foncée
    private String deriveColor(String baseColor, double opacity) {
        try {
            Color color = Color.web(baseColor);
            double red = color.getRed();
            double green = color.getGreen();
            double blue = color.getBlue();

            // Éclaircir la couleur pour les effets de survol
            red = Math.min(1.0, red + (1 - red) * opacity);
            green = Math.min(1.0, green + (1 - green) * opacity);
            blue = Math.min(1.0, blue + (1 - blue) * opacity);

            return String.format("rgba(%d, %d, %d, 0.2)",
                    (int)(red * 255),
                    (int)(green * 255),
                    (int)(blue * 255));
        } catch (Exception e) {
            // En cas d'erreur, retourner une couleur par défaut
            return "rgba(200, 200, 200, 0.2)";
        }
    }

    @FXML
    private void handleDetails(ActionEvent event) {
        textile textile = getTextileFromEvent(event);
        if (textile != null) {
            try {
                // Try different resource paths until we find the file
                URL resourceUrl = null;

                // Option 2: Try with leading slash (from classpath root)
                if (resourceUrl == null) {
                    resourceUrl = getClass().getResource("/details.fxml");
                }

                // If still not found, show error
                if (resourceUrl == null) {
                    System.err.println("Could not find detail.fxml. Make sure the file exists and is properly included in the build.");
                    showAlert("Erreur", "Fichier introuvable",
                            "Le fichier detail.fxml est introuvable. Vérifiez qu'il existe dans le projet.",
                            Alert.AlertType.ERROR);
                    return;
                }

                System.out.println("Found FXML file at: " + resourceUrl);

                // Obtenir les dimensions de l'écran
                Screen screen = Screen.getPrimary();
                double screenWidth = screen.getVisualBounds().getWidth();
                double screenHeight = screen.getVisualBounds().getHeight();

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                Parent root = loader.load();

                detail detailController = loader.getController();
                detailController.setTextileDetails(textile);

                Scene detailScene = new Scene(root, screenWidth, screenHeight);



                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(detailScene);
                currentStage.show();
            } catch (IOException e) {
                showAlert("Erreur", "Erreur de chargement", "Impossible d'ouvrir les détails du textile: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        try {
            textile textileToEdit = getTextileFromEvent(event);

            // Obtenir les dimensions de l'écran
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Charger la vue d'édition
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            // Passer les données au contrôleur d'édition
            edit editController = loader.getController();
            editController.setTextileDetails(textileToEdit);

            // Remplacer la scène actuelle
            Scene scene = new Scene(root, screenWidth, screenHeight);



            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        textile textile = getTextileFromEvent(event);
        if (textile != null) {
            deleteTextile(textile);
        }
    }

    private textile getTextileFromEvent(ActionEvent event) {
        try {
            Button source = (Button) event.getSource();
            HBox buttonsBox = (HBox) source.getParent();
            VBox card = (VBox) buttonsBox.getParent();
            return (textile) card.getUserData();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur de traitement", "Impossible de récupérer le textile", Alert.AlertType.ERROR);
            return null;
        }
    }

    private void deleteTextile(textile textile) {
        // Confirmation Alert with custom styling
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le textile \"" + textile.getNom() + "\" ?");
        confirm.setContentText("Cette action est irréversible et supprimera définitivement le textile.");

        // Custom dialog pane styling
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css_files/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");
        dialogPane.setStyle("-fx-background-color: #f8f5f2;");
        dialogPane.setHeader(createAlertHeader("Suppression", "M20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"));

        // Custom buttons
        ButtonType confirmButton = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(confirmButton, cancelButton);

        // Get the actual buttons to apply styles
        Button btnConfirm = (Button) confirm.getDialogPane().lookupButton(confirmButton);
        Button btnCancel = (Button) confirm.getDialogPane().lookupButton(cancelButton);
        btnConfirm.setStyle("-fx-background-color: #603813; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCancel.setStyle("-fx-background-color: #b29f94; -fx-text-fill: #333; -fx-font-weight: bold;");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == confirmButton) {
            try {
                textileService.delete(textile);

                // Success Alert
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText("Suppression réussie");
                success.setContentText("Le textile \"" + textile.getNom() + "\" a été supprimé avec succès.");

                // Style success alert
                DialogPane successPane = success.getDialogPane();
                successPane.getStylesheets().add(getClass().getResource("/css_files/style.css").toExternalForm());
                successPane.getStyleClass().add("custom-alert");
                successPane.setStyle("-fx-background-color: #f8f5f2;");
                successPane.setHeader(createAlertHeader("Succès", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41L9 16.17z"));

                // Custom OK button
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                success.getButtonTypes().setAll(okButton);
                Button btnOk = (Button) success.getDialogPane().lookupButton(okButton);
                btnOk.setStyle("-fx-background-color: #603813; -fx-text-fill: white; -fx-font-weight: bold;");

                success.showAndWait();
                populateTextileCards();

            } catch (SQLException e) {
                // Error Alert
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Échec de la suppression");
                error.setContentText("Une erreur s'est produite : " + e.getMessage());

                // Style error alert
                DialogPane errorPane = error.getDialogPane();
                errorPane.getStylesheets().add(getClass().getResource("/css_files/style.css").toExternalForm());
                errorPane.getStyleClass().add("custom-alert");
                errorPane.setStyle("-fx-background-color: #f8f5f2;");
                errorPane.setHeader(createAlertHeader("Erreur", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"));

                // Custom OK button
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                error.getButtonTypes().setAll(okButton);
                Button btnErrorOk = (Button) error.getDialogPane().lookupButton(okButton);
                btnErrorOk.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold;");

                error.showAndWait();
            }
        }
    }

    // Helper method to create styled alert headers with icons
    private HBox createAlertHeader(String title, String svgPath) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setStyle("-fx-fill: #603813;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #603813;");

        header.getChildren().addAll(icon, titleLabel);
        return header;
    }

    @FXML
    private void handleAjouterTextile(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ajouter.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);



            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de chargement", "Impossible d'ouvrir l'interface d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void Collection(ActionEvent actionEvent) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);


            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur de chargement", "Impossible d'ouvrir les collections", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleFilter(ActionEvent actionEvent) {
        try {
            collection_t selected = collectionComboBox.getValue();
            if (selected == null || selected.getId() == -1) {
                populateTextileCards();
            } else {
                List<textile> textiles = textileService.getByCollectionId(selected.getId());
                imageContainer.getChildren().clear();

                for (textile t : textiles) {
                    imageContainer.getChildren().add(createTextileCard(t));
                }
            }
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur de filtrage", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleQRCode(ActionEvent event, textile t) {
        try {
            // Obtenir les dimensions de l'écran
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Utilisez un chemin relatif pour charger le fichier FXML
            URL fxmlUrl = getClass().getResource("/textile_qrcode.fxml");

            if (fxmlUrl == null) {
                // Si le fichier n'est pas trouvé, essayez d'autres chemins possibles
                fxmlUrl = getClass().getResource("textile_qrcode.fxml");
            }

            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Récupérer le contrôleur et définir le textile
            TextileQRCodeController controller = loader.getController();
            controller.setTextile(t);

            // Afficher la nouvelle scène
            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // Imprimez la pile d'erreur pour avoir plus de détails
            showAlert("Erreur", "Erreur QR Code",
                    "Impossible d'afficher le QR Code: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }}