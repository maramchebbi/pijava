package controller;
import javafx.scene.layout.VBox;
import Models.CeramicCollection;
import Models.Oeuvre;
import Services.CollectionCeramiqueService;
import Services.OeuvreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javafx.scene.text.Text;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AjouterOeuvre {

    @FXML
    private ImageView Artisferaimage;
    @FXML
    private Button galleryButton;

    @FXML
    private ImageView galleryIcon;
    @FXML
    private VBox step1Form;
    @FXML
    private VBox step2Form;
    @FXML
    private Button step1Button;
    @FXML
    private Button step2Button;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField collectionIdField;

    @FXML
    private ComboBox<CeramicCollection> collectionComboBox;

    @FXML
    private TextField couleurField;

    @FXML
    private TextField usertextfield;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField dimensionField;

    @FXML
    private TextField iduserfield;

    @FXML
    private TextField matiereField;

    @FXML
    private TextField nomField;

    @FXML
    private ImageView textileimage;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private ImageView iconview;

    @FXML
    void ajouterOeuvre(ActionEvent event) {
        // Récupération des données depuis les champs FXML
        String nom = nomField.getText();
        String type = typeComboBox.getValue();
        String description = descriptionField.getText();
        String matiere = matiereField.getText();
        String dimensions = dimensionField.getText();
        String couleur = couleurField.getText();
        String categorie = categorieComboBox.getValue();

        int userId;
        int collectionId;
        String imagePath = "";

        // Vérification et conversion des IDs
        CeramicCollection selectedCollection;
        try {
            userId = Integer.parseInt(usertextfield.getText());
            selectedCollection = collectionComboBox.getValue();
            if (selectedCollection == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucune collection sélectionnée !",
                        "Veuillez choisir une collection avant d'ajouter l'œuvre.");
                return;
            }
            collectionId = selectedCollection.getId();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format incorrect !",
                    "Les champs ID utilisateur et ID collection doivent être numériques.");
            return;
        }

        // Récupération du chemin de l'image
        if (textileimage.getImage() != null) {
            Image image = textileimage.getImage();
            imagePath = image.getUrl();
            if (imagePath != null && imagePath.startsWith("file:/")) {
                imagePath = imagePath.replace("file:/", "");
            }
        }

        // Vérification si l'œuvre existe déjà
        try {
            OeuvreService oeuvreService = new OeuvreService();
            if (oeuvreService.isOeuvreNameOrImageExists(nom, imagePath)) {
                // Vérification plus précise pour donner un message d'erreur spécifique
                boolean nomExists = oeuvreService.isOeuvreNameExists(nom);
                boolean imageExists = oeuvreService.isOeuvreImageExists(imagePath);

                if (nomExists && imageExists) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Combinaison existante",
                            "Une œuvre avec CE NOM et CETTE IMAGE existe déjà.");
                } else if (nomExists) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Nom existant",
                            "Une œuvre avec CE NOM existe déjà. Choisissez un nom différent.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Image existante",
                            "Cette IMAGE est déjà utilisée. Choisissez une autre image.");
                }
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur base de données",
                    "Impossible de vérifier l'unicité: " + e.getMessage());
            return;
        }

        // Création et validation de l'objet Oeuvre
        Oeuvre oeuvreObj = new Oeuvre(
                nom, type, description, matiere, couleur, dimensions,
                imagePath, categorie, userId, selectedCollection
        );

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Oeuvre>> violations = validator.validate(oeuvreObj);

        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (ConstraintViolation<Oeuvre> violation : violations) {
                message.append(violation.getMessage()).append("\n");
            }
            showAlert(Alert.AlertType.ERROR, "Attention !", "Format invalide", message.toString());
            return;
        }

        // Tentative d'ajout
        try {
            OeuvreService oeuvreService = new OeuvreService();
            oeuvreService.add(oeuvreObj);

            // Afficher l'alerte
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Œuvre ajoutée",
                    "L'œuvre a été ajoutée avec succès !");

            // Redirection après l'alerte
            Parent root = FXMLLoader.load(getClass().getResource("/show.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (SQLException | IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout",
                    "Impossible d'ajouter l'œuvre: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour afficher des alertes
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void viewoeuvre(ActionEvent actionEvent) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // Create a new scene with the loaded FXML content
            Scene scene = new Scene(root);

            // Set the new scene and show the stage
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            // Handle the exception and print the error message
            System.out.println("Error loading the FXML file: " + e.getMessage());
        }
    }

    public void initialize() {
        // Créer une instance du service des collections
        CollectionCeramiqueService collectionService = new CollectionCeramiqueService(/*connection*/);

        try {
            // Récupérer toutes les collections
            List<CeramicCollection> collections = collectionService.getAll();

            // Convertir la liste en ObservableList pour la ComboBox
            ObservableList<CeramicCollection> collectionObservableList = FXCollections.observableArrayList(collections);

            // Remplir la ComboBox avec la liste des collections
            collectionComboBox.setItems(collectionObservableList);

            // Optional: Spécifier un affichage personnalisé si nécessaire
            collectionComboBox.setCellFactory(param -> new ListCell<CeramicCollection>() {
                @Override
                protected void updateItem(CeramicCollection item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        // Afficher le nom de la collection dans la liste déroulante
                        setText(item.getNom_c());
                    }
                }
            });

            // Optional: Spécifier l'affichage de l'élément sélectionné dans la ComboBox
            collectionComboBox.setButtonCell(new ListCell<CeramicCollection>() {
                @Override
                protected void updateItem(CeramicCollection item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom_c());  // Affiche le nom de la collection sélectionnée
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement des collections");
            alert.setHeaderText("Impossible de récupérer les collections");
            alert.setContentText(e.getMessage());
            alert.show();
        }

        // Initialize type ComboBox
        ObservableList<String> types = FXCollections.observableArrayList(
                "Sculpture",
                "Vase",
                "Poterie",
                "Assiette décorative",
                "bibelot"
        );
        typeComboBox.setItems(types);

        // Initialize category ComboBox
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Céramique florale",
                "Céramique aquatique",
                "Céramique monochrome",
                "Céramique fantastique"
        );
        categorieComboBox.setItems(categories);

        // Load the image from resources folder using a relative path
        Image image = new Image(getClass().getResource("/images/icon.png").toExternalForm());
        iconview.setImage(image);

        // Configuration des étapes
        step1Form.setVisible(true);
        step2Form.setVisible(false);
        step1Button.setDisable(false);
        step2Button.setDisable(true);
        progressBar.setProgress(0.5);

        // Style initial des boutons d'étape
        step1Button.getStyleClass().add("step-button");
        step2Button.getStyleClass().add("step-button-inactive");
    }

    @FXML
    public void uploadimage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Ajouter un filtre pour n'autoriser que les fichiers image
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        // Ouvrir la boîte de dialogue pour sélectionner un fichier
        Stage stage = (Stage) textileimage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Afficher l'image dans l'ImageView
                Image image = new Image(file.toURI().toString());
                textileimage.setImage(image);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors du chargement de l'image");
                alert.setContentText("Impossible de charger l'image sélectionnée : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void goToStep2(ActionEvent event) {
        // Valider les champs de l'étape 1 avant de continuer
        if (validateStep1()) {
            step1Form.setVisible(false);
            step2Form.setVisible(true);
            step1Button.getStyleClass().remove("step-button");
            step1Button.getStyleClass().add("step-button-inactive");
            step2Button.getStyleClass().remove("step-button-inactive");
            step2Button.getStyleClass().add("step-button");
            step2Button.setDisable(false);
            progressBar.setProgress(1.0);
        }
    }

    @FXML
    private void goToStep1(ActionEvent event) {
        step2Form.setVisible(false);
        step1Form.setVisible(true);
        step2Button.getStyleClass().remove("step-button");
        step2Button.getStyleClass().add("step-button-inactive");
        step1Button.getStyleClass().remove("step-button-inactive");
        step1Button.getStyleClass().add("step-button");
        progressBar.setProgress(0.5);
    }

    private boolean validateStep1() {
        if (nomField.getText().isEmpty()) {
            showAlert(AlertType.ERROR, "Erreur", "Champ manquant", "Le nom est obligatoire");
            return false;
        }
        if (typeComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Erreur", "Champ manquant", "Veuillez sélectionner un type");
            return false;
        }
        if (collectionComboBox.getValue() == null) {
            showAlert(AlertType.ERROR, "Erreur", "Champ manquant", "Veuillez sélectionner une collection");
            return false;
        }
        return true;
    }
}