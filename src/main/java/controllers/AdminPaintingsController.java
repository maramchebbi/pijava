package controllers;

import controllers.AdminPanelController.Refreshable;
import models.Peinture;
import models.Style;
import service.PeintureService;
import service.StyleService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des peintures dans le back-office
 */
public class AdminPaintingsController implements Refreshable {

    // Services
    private PeintureService peintureService;
    private StyleService styleService;

    // Données
    private ObservableList<Peinture> paintings = FXCollections.observableArrayList();
    private ObservableList<Style> styles = FXCollections.observableArrayList();
    private Peinture currentEditPainting;

    // Composants FXML - Filtre et recherche
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<Style> styleFilterComboBox;

    @FXML
    private ComboBox<String> sortComboBox;

    // Composants FXML - Tableau de peintures
    @FXML
    private TableView<Peinture> paintingsTable;

    @FXML
    private TableColumn<Peinture, Integer> idColumn;

    @FXML
    private TableColumn<Peinture, String> thumbnailColumn;

    @FXML
    private TableColumn<Peinture, String> titleColumn;

    @FXML
    private TableColumn<Peinture, String> styleColumn;

    @FXML
    private TableColumn<Peinture, LocalDate> dateColumn;

    @FXML
    private TableColumn<Peinture, Peinture> actionColumn;

    // Composants FXML - Pagination et statut
    @FXML
    private Pagination paintingsPagination;

    @FXML
    private Label totalPaintingsLabel;

    // Composants FXML - Panneau d'édition
    @FXML
    private VBox editPanelVBox;

    @FXML
    private Label editPanelTitle;

    @FXML
    private TextField editTitleField;

    @FXML
    private ComboBox<Style> editStyleComboBox;

    @FXML
    private DatePicker editDatePicker;

    @FXML
    private TextField editImagePathField;

    @FXML
    private ImageView editImagePreview;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Initialiser les services
        peintureService = new PeintureService();
        styleService = new StyleService();

        // Configurer le tableau
        setupTable();

        // Configurer les filtres
        setupFilters();

        // Configurer le panneau d'édition
        setupEditPanel();

        // Charger les données
        loadData();
    }

    /**
     * Configure les colonnes du tableau et les cellules personnalisées
     */
    private void setupTable() {
        // Configurer les colonnes standards
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCr"));

        // Formater la date
        dateColumn.setCellFactory(column -> new TableCell<Peinture, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        // Configurer la colonne style avec le nom du style
        styleColumn.setCellValueFactory(cellData -> {
            Style style = cellData.getValue().getStyle();
            return new SimpleStringProperty(style != null ? style.getType() : "");
        });

        // Configurer la colonne thumbnail avec une image miniature
        thumbnailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTableau()));
        thumbnailColumn.setCellFactory(column -> new TableCell<Peinture, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image("file:" + imagePath, true));
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("Image non disponible");
                    }
                }
            }
        });

        // Configurer la colonne d'actions avec des boutons
        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionColumn.setCellFactory(param -> new TableCell<Peinture, Peinture>() {
            private final Button editButton = new Button("✏");
            private final Button deleteButton = new Button("🗑");
            private final HBox buttonsBox = new HBox(10, editButton, deleteButton);

            {
                editButton.getStyleClass().add("admin-edit-btn");
                deleteButton.getStyleClass().add("admin-delete-btn");
                buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);

                editButton.setOnAction(event -> {
                    Peinture peinture = getTableView().getItems().get(getIndex());
                    showEditPanel(peinture);
                });

                deleteButton.setOnAction(event -> {
                    Peinture peinture = getTableView().getItems().get(getIndex());
                    handleDeletePainting(peinture);
                });
            }

            @Override
            protected void updateItem(Peinture peinture, boolean empty) {
                super.updateItem(peinture, empty);
                if (empty || peinture == null) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    /**
     * Configure les filtres et leur comportement
     */
    private void setupFilters() {
        // Configurer le filtre par style
        styleFilterComboBox.setConverter(new StringConverter<Style>() {
            @Override
            public String toString(Style style) {
                return style == null ? "Tous les styles" : style.getType();
            }

            @Override
            public Style fromString(String string) {
                return null; // Non utilisé pour un ComboBox
            }
        });

        // Ajouter un écouteur sur le changement de style
        styleFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterPaintings();
        });

        // Ajouter un écouteur sur le champ de recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPaintings();
        });

        // Configurer les options de tri
        sortComboBox.getItems().addAll(
                "Date (récent → ancien)",
                "Date (ancien → récent)",
                "Titre (A → Z)",
                "Titre (Z → A)"
        );
        sortComboBox.setValue("Date (récent → ancien)");

        // Ajouter un écouteur sur le changement de tri
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            sortPaintings();
        });
    }

    /**
     * Configure le panneau d'édition
     */
    private void setupEditPanel() {
        // Configurer le ComboBox des styles
        editStyleComboBox.setConverter(new StringConverter<Style>() {
            @Override
            public String toString(Style style) {
                return style == null ? "" : style.getType();
            }

            @Override
            public Style fromString(String string) {
                return null; // Non utilisé pour un ComboBox
            }
        });

        // Ajouter un écouteur sur le champ de chemin d'image
        editImagePathField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                try {
                    editImagePreview.setImage(new Image("file:" + newVal, true));
                } catch (Exception e) {
                    editImagePreview.setImage(null);
                }
            } else {
                editImagePreview.setImage(null);
            }
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

            // Configurer le ComboBox des styles
            styleFilterComboBox.getItems().clear();
            styleFilterComboBox.getItems().add(null); // Option "Tous les styles"
            styleFilterComboBox.getItems().addAll(styles);

            editStyleComboBox.getItems().clear();
            editStyleComboBox.getItems().addAll(styles);

            // Charger les peintures
            List<Peinture> paintingsList = peintureService.getAll();
            paintings.clear();
            paintings.addAll(paintingsList);

            // Mettre à jour l'affichage
            paintingsTable.setItems(paintings);
            totalPaintingsLabel.setText(String.valueOf(paintings.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du chargement des données",
                    "Une erreur s'est produite lors du chargement des données: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Filtre les peintures selon les critères actuels
     */
    private void filterPaintings() {
        String searchText = searchField.getText().toLowerCase();
        Style selectedStyle = styleFilterComboBox.getValue();

        ObservableList<Peinture> filteredList = FXCollections.observableArrayList();

        try {
            List<Peinture> allPaintings = peintureService.getAll();

            for (Peinture peinture : allPaintings) {
                boolean matchesSearch = searchText.isEmpty() ||
                        peinture.getTitre().toLowerCase().contains(searchText);

                boolean matchesStyle = selectedStyle == null ||
                        (peinture.getStyle() != null &&
                                peinture.getStyle().getId() == selectedStyle.getId());

                if (matchesSearch && matchesStyle) {
                    filteredList.add(peinture);
                }
            }

            paintings = filteredList;

            // Appliquer le tri
            sortPaintings();

            // Mettre à jour le tableau
            paintingsTable.setItems(paintings);
            totalPaintingsLabel.setText(String.valueOf(paintings.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors du filtrage",
                    "Une erreur s'est produite lors du filtrage des peintures: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Trie les peintures selon le critère sélectionné
     */
    private void sortPaintings() {
        String sortOption = sortComboBox.getValue();

        if (sortOption != null) {
            switch (sortOption) {
                case "Date (récent → ancien)":
                    paintings.sort((p1, p2) -> p2.getDateCr().compareTo(p1.getDateCr()));
                    break;
                case "Date (ancien → récent)":
                    paintings.sort((p1, p2) -> p1.getDateCr().compareTo(p2.getDateCr()));
                    break;
                case "Titre (A → Z)":
                    paintings.sort((p1, p2) -> p1.getTitre().compareToIgnoreCase(p2.getTitre()));
                    break;
                case "Titre (Z → A)":
                    paintings.sort((p1, p2) -> p2.getTitre().compareToIgnoreCase(p1.getTitre()));
                    break;
            }

            paintingsTable.setItems(paintings);
        }
    }

    /**
     * Affiche le panneau d'édition pour une peinture
     */
    private void showEditPanel(Peinture peinture) {
        currentEditPainting = peinture;

        if (peinture != null) {
            // Mode édition
            editPanelTitle.setText("Modifier la peinture");
            editTitleField.setText(peinture.getTitre());
            editDatePicker.setValue(peinture.getDateCr());
            editImagePathField.setText(peinture.getTableau());

            // Sélectionner le style
            if (peinture.getStyle() != null) {
                for (Style style : editStyleComboBox.getItems()) {
                    if (style.getId() == peinture.getStyle().getId()) {
                        editStyleComboBox.setValue(style);
                        break;
                    }
                }
            } else {
                editStyleComboBox.setValue(null);
            }

            // Charger l'image de prévisualisation
            if (peinture.getTableau() != null && !peinture.getTableau().isEmpty()) {
                try {
                    editImagePreview.setImage(new Image("file:" + peinture.getTableau(), true));
                } catch (Exception e) {
                    editImagePreview.setImage(null);
                }
            } else {
                editImagePreview.setImage(null);
            }
        } else {
            // Mode création
            editPanelTitle.setText("Ajouter une nouvelle peinture");
            editTitleField.clear();
            editStyleComboBox.setValue(null);
            editDatePicker.setValue(LocalDate.now());
            editImagePathField.clear();
            editImagePreview.setImage(null);
        }

        // Afficher le panneau
        editPanelVBox.setVisible(true);
        editPanelVBox.setManaged(true);
    }

    /**
     * Ferme le panneau d'édition
     */
    @FXML
    private void closeEditPanel() {
        editPanelVBox.setVisible(false);
        editPanelVBox.setManaged(false);
        currentEditPainting = null;
    }

    /**
     * Gère l'action du bouton d'ajout de peinture
     */
    @FXML
    private void handleAddPainting() {
        showEditPanel(null);
    }

    /**
     * Gère l'action du bouton de parcourir pour l'image
     */
    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) editPanelVBox.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            editImagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * Gère l'action du bouton de sauvegarde des modifications
     */
    @FXML
    private void handleSaveEdits() {
        // Valider les champs
        if (!validateFields()) {
            return;
        }

        try {
            String titre = editTitleField.getText();
            LocalDate dateCr = editDatePicker.getValue();
            Style style = editStyleComboBox.getValue();
            String imagePath = editImagePathField.getText();

            if (currentEditPainting == null) {
                // Ajouter une nouvelle peinture
                Peinture newPainting = new Peinture(titre, dateCr, imagePath, style, 5); // ID utilisateur fixe pour l'exemple
                peintureService.add(newPainting);
                showAlert("Succès", "Peinture ajoutée", "La peinture a été ajoutée avec succès.", Alert.AlertType.INFORMATION);
            } else {
                // Mettre à jour une peinture existante
                currentEditPainting.setTitre(titre);
                currentEditPainting.setDateCr(dateCr);
                currentEditPainting.setStyle(style);
                currentEditPainting.setTableau(imagePath);

                peintureService.update(currentEditPainting);
                showAlert("Succès", "Peinture modifiée", "La peinture a été modifiée avec succès.", Alert.AlertType.INFORMATION);
            }

            // Fermer le panneau d'édition
            closeEditPanel();

            // Recharger les données
            loadData();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la sauvegarde",
                    "Une erreur s'est produite lors de la sauvegarde: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Gère la suppression d'une peinture
     */
    private void handleDeletePainting(Peinture peinture) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette peinture ?");
        confirmAlert.setContentText("Titre: " + peinture.getTitre() + "\nCette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                peintureService.delete(peinture.getId());
                showAlert("Succès", "Peinture supprimée", "La peinture a été supprimée avec succès.", Alert.AlertType.INFORMATION);
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors de la suppression",
                        "Une erreur s'est produite lors de la suppression: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Valide les champs du formulaire
     */
    private boolean validateFields() {
        String titre = editTitleField.getText();
        LocalDate dateCr = editDatePicker.getValue();
        Style style = editStyleComboBox.getValue();
        String imagePath = editImagePathField.getText();

        StringBuilder errors = new StringBuilder();

        if (titre == null || titre.trim().isEmpty()) {
            errors.append("- Le titre est requis.\n");
        } else if (titre.length() < 3) {
            errors.append("- Le titre doit contenir au moins 3 caractères.\n");
        }

        if (dateCr == null) {
            errors.append("- La date de création est requise.\n");
        } else if (dateCr.isAfter(LocalDate.now())) {
            errors.append("- La date de création ne peut pas être dans le futur.\n");
        }

        if (style == null) {
            errors.append("- Le style est requis.\n");
        }

        if (imagePath == null || imagePath.trim().isEmpty()) {
            errors.append("- L'image est requise.\n");
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