package controllers;

import controllers.AdminPanelController.Refreshable;
import models.Style;
import service.IService;
import service.StyleService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour la gestion des styles dans le back-office
 */
public class AdminStylesController implements Refreshable {

    // Service
    private StyleService styleService;

    // Données
    private ObservableList<Style> styles = FXCollections.observableArrayList();
    private Style currentEditStyle;

    // Composants FXML - Filtre et recherche
    @FXML
    private TextField searchStyleField;

    @FXML
    private ComboBox<String> sortStylesComboBox;

    // Composants FXML - Grille des styles
    @FXML
    private GridPane stylesGrid;

    // Composants FXML - Statut
    @FXML
    private Label totalStylesLabel;

    // Composants FXML - Panneau d'édition
    @FXML
    private VBox editStylePanelVBox;

    @FXML
    private Label editStylePanelTitle;

    @FXML
    private TextField editTypeField;

    @FXML
    private TextArea editDescriptionArea;

    @FXML
    private TextField editExImagePathField;

    @FXML
    private ImageView editStyleImagePreview;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Initialiser le service
        styleService = new StyleService();

        // Configurer les filtres
        setupFilters();

        // Charger les données
        loadData();
    }

    /**
     * Configure les filtres et leur comportement
     */
    private void setupFilters() {
        // Configurer les options de tri
        sortStylesComboBox.getItems().addAll(
                "Alphabétique (A → Z)",
                "Alphabétique (Z → A)",
                "Identifiant (croissant)",
                "Identifiant (décroissant)"
        );
        sortStylesComboBox.setValue("Alphabétique (A → Z)");

        // Ajouter un écouteur sur le changement de tri
        sortStylesComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortStyles();
        });

        // Ajouter un écouteur sur le champ de recherche
        searchStyleField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterStyles();
        });
    }

    /**
     * Charge les données initiales
     */
    private void loadData() {
        try {
            // Charger les styles
            List<Style> stylesList = styleService.getAll();
            styles.clear();
            styles.addAll(stylesList);

            // Mettre à jour l'affichage
            updateStylesGrid();
            totalStylesLabel.setText(String.valueOf(styles.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des données",
                    "Une erreur s'est produite lors du chargement des styles: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Filtre les styles selon le texte de recherche
     */
    private void filterStyles() {
        String searchText = searchStyleField.getText().toLowerCase();

        try {
            List<Style> allStyles = styleService.getAll();

            styles.clear();
            for (Style style : allStyles) {
                if (searchText.isEmpty() ||
                        style.getType().toLowerCase().contains(searchText) ||
                        (style.getDescription() != null && style.getDescription().toLowerCase().contains(searchText))) {
                    styles.add(style);
                }
            }

            // Appliquer le tri
            sortStyles();

            // Mettre à jour l'affichage
            updateStylesGrid();
            totalStylesLabel.setText(String.valueOf(styles.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du filtrage",
                    "Une erreur s'est produite lors du filtrage des styles: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Trie les styles selon le critère sélectionné
     */
    private void sortStyles() {
        String sortOption = sortStylesComboBox.getValue();

        if (sortOption != null) {
            switch (sortOption) {
                case "Alphabétique (A → Z)":
                    styles.sort((s1, s2) -> s1.getType().compareToIgnoreCase(s2.getType()));
                    break;
                case "Alphabétique (Z → A)":
                    styles.sort((s1, s2) -> s2.getType().compareToIgnoreCase(s1.getType()));
                    break;
                case "Identifiant (croissant)":
                    styles.sort((s1, s2) -> Integer.compare(s1.getId(), s2.getId()));
                    break;
                case "Identifiant (décroissant)":
                    styles.sort((s1, s2) -> Integer.compare(s2.getId(), s1.getId()));
                    break;
            }

            updateStylesGrid();
        }
    }

    /**
     * Met à jour la grille des styles
     */
    private void updateStylesGrid() {
        // Effacer la grille
        stylesGrid.getChildren().clear();

        // Réinitialiser les contraintes de ligne
        stylesGrid.getRowConstraints().clear();

        int columns = 3; // Nombre de colonnes
        int row = 0;
        int column = 0;

        for (Style style : styles) {
            VBox styleCard = createStyleCard(style);

            stylesGrid.add(styleCard, column, row);

            // Passer à la colonne suivante
            column++;

            // Si on a atteint le nombre de colonnes, passer à la ligne suivante
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Crée une carte pour afficher un style
     */
    private VBox createStyleCard(Style style) {
        VBox card = new VBox();
        card.getStyleClass().add("admin-style-card");
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);

        // Créer l'en-tête avec le type et les boutons d'action
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label typeLabel = new Label(style.getType());
        typeLabel.getStyleClass().add("admin-style-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editButton = new Button("✏");
        editButton.getStyleClass().add("admin-edit-btn");
        editButton.setOnAction(e -> showStyleEditPanel(style));

        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("admin-delete-btn");
        deleteButton.setOnAction(e -> handleDeleteStyle(style));

        header.getChildren().addAll(typeLabel, spacer, editButton, deleteButton);

        // Image d'exemple
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("admin-style-image-container");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);

        if (style.getExtab() != null && !style.getExtab().isEmpty()) {
            try {
                imageView.setImage(new Image("file:" + style.getExtab(), true));
            } catch (Exception e) {
                // En cas d'erreur, afficher un placeholder
                Label noImageLabel = new Label("Image non disponible");
                noImageLabel.getStyleClass().add("admin-no-image-label");
                imageContainer.getChildren().add(noImageLabel);
            }
        } else {
            Label noImageLabel = new Label("Aucune image");
            noImageLabel.getStyleClass().add("admin-no-image-label");
            imageContainer.getChildren().add(noImageLabel);
        }

        if (imageView.getImage() != null) {
            imageContainer.getChildren().add(imageView);
        }

        // Description dans un conteneur avec défilement si nécessaire
        VBox descriptionBox = new VBox();
        descriptionBox.getStyleClass().add("admin-style-description-container");
        descriptionBox.setPadding(new Insets(8));
        descriptionBox.setMaxHeight(60); // Hauteur fixe pour la zone de description

        Text descriptionText = new Text(style.getDescription() != null ? style.getDescription() : "Aucune description");
        descriptionText.getStyleClass().add("admin-style-description");
        descriptionText.setWrappingWidth(180);

        descriptionBox.getChildren().add(descriptionText);

// Assembler la carte
        card.getChildren().addAll(header, imageContainer, descriptionBox);


        return card;
    }

    /**
     * Affiche le panneau d'édition pour un style
     */
    private void showStyleEditPanel(Style style) {
        currentEditStyle = style;

        if (style != null) {
            // Mode édition
            editStylePanelTitle.setText("Modifier le style");
            editTypeField.setText(style.getType());
            editDescriptionArea.setText(style.getDescription());
            editExImagePathField.setText(style.getExtab());

            // Charger l'image de prévisualisation
            if (style.getExtab() != null && !style.getExtab().isEmpty()) {
                try {
                    editStyleImagePreview.setImage(new Image("file:" + style.getExtab(), true));
                } catch (Exception e) {
                    editStyleImagePreview.setImage(null);
                }
            } else {
                editStyleImagePreview.setImage(null);
            }
        } else {
            // Mode création
            editStylePanelTitle.setText("Ajouter un nouveau style");
            editTypeField.clear();
            editDescriptionArea.clear();
            editExImagePathField.clear();
            editStyleImagePreview.setImage(null);
        }

        // Afficher le panneau
        editStylePanelVBox.setVisible(true);
        editStylePanelVBox.setManaged(true);
    }

    /**
     * Ferme le panneau d'édition
     */
    @FXML
    private void closeStyleEditPanel() {
        editStylePanelVBox.setVisible(false);
        editStylePanelVBox.setManaged(false);
        currentEditStyle = null;
    }

    /**
     * Gère l'action du bouton d'ajout de style
     */
    @FXML
    private void handleAddStyle() {
        showStyleEditPanel(null);
    }

    /**
     * Gère l'action du bouton de parcourir pour l'image d'exemple
     */
    @FXML
    private void handleBrowseExImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image d'exemple");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) editStylePanelVBox.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            editExImagePathField.setText(selectedFile.getAbsolutePath());

            // Mettre à jour la prévisualisation
            try {
                editStyleImagePreview.setImage(new Image("file:" + selectedFile.getAbsolutePath(), true));
            } catch (Exception e) {
                editStyleImagePreview.setImage(null);
            }
        }
    }

    /**
     * Gère l'action du bouton de sauvegarde des modifications
     */
    @FXML
    private void handleSaveStyleEdits() {
        // Valider les champs
        if (!validateStyleFields()) {
            return;
        }

        try {
            String type = editTypeField.getText();
            String description = editDescriptionArea.getText();
            String imagePath = editExImagePathField.getText();

            if (currentEditStyle == null) {
                // Ajouter un nouveau style
                Style newStyle = new Style(type, description, imagePath);
                styleService.add(newStyle);
                showAlert("Succès", "Style ajouté", "Le style a été ajouté avec succès.", Alert.AlertType.INFORMATION);
            } else {
                // Mettre à jour un style existant
                currentEditStyle.setType(type);
                currentEditStyle.setDescription(description);
                currentEditStyle.setExtab(imagePath);

                styleService.update(currentEditStyle);
                showAlert("Succès", "Style modifié", "Le style a été modifié avec succès.", Alert.AlertType.INFORMATION);
            }

            // Fermer le panneau d'édition
            closeStyleEditPanel();

            // Recharger les données
            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la sauvegarde",
                    "Une erreur s'est produite lors de la sauvegarde du style: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Gère la suppression d'un style
     */
    private void handleDeleteStyle(Style style) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce style ?");
        confirmAlert.setContentText("Type: " + style.getType() + "\nCette action est irréversible et pourrait affecter les peintures associées.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                styleService.delete(style.getId());
                showAlert("Succès", "Style supprimé", "Le style a été supprimé avec succès.", Alert.AlertType.INFORMATION);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression",
                        "Une erreur s'est produite lors de la suppression du style: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Valide les champs du formulaire de style
     */
    private boolean validateStyleFields() {
        String type = editTypeField.getText();
        String description = editDescriptionArea.getText();
        String imagePath = editExImagePathField.getText();

        StringBuilder errors = new StringBuilder();

        if (type == null || type.trim().isEmpty()) {
            errors.append("- Le type est requis.\n");
        } else if (type.length() < 3) {
            errors.append("- Le type doit contenir au moins 3 caractères.\n");
        } else if (!type.matches("[a-zA-Z ]+")) {
            errors.append("- Le type doit contenir uniquement des lettres et des espaces.\n");
        }

        if (description == null || description.trim().isEmpty()) {
            errors.append("- La description est requise.\n");
        } else if (description.length() < 10) {
            errors.append("- La description doit contenir au moins 10 caractères.\n");
        }

        if (imagePath == null || imagePath.trim().isEmpty()) {
            errors.append("- L'image d'exemple est requise.\n");
        } else {
            File imageFile = new File(imagePath);
            if (!imageFile.exists() || !imageFile.isFile()) {
                errors.append("- Le fichier image n'existe pas.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Erreur de validation", "Veuillez corriger les erreurs suivantes:",
                    errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     */
    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Implémentation de l'interface Refreshable
     */
    @Override
    public void refresh() {
        loadData();
    }
}