package controller;

import Models.Oeuvre;
import Services.OeuvreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class edit implements Initializable {

    @FXML private ImageView imageView;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
 @FXML private TextField useridtextfield;
    @FXML private ComboBox<String> categorieComboBox;

    private File selectedImageFile = null;
    private Oeuvre currentOeuvre;
    private final OeuvreService oeuvreService = new OeuvreService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup initial ComboBox values
        ObservableList<String> types = FXCollections.observableArrayList(
                "Sculpture",
                "Vase",
                "Poterie",
                "Assiette décorative",
                "Bibelot"
        );
        typeComboBox.setItems(types);

        ObservableList<String> categories = FXCollections.observableArrayList(
                "Céramique florale",
                "Céramique aquatique",
                "Céramique monochrome",
                "Céramique fantastique"
        );
        categorieComboBox.setItems(categories);
    }

    public void setOeuvreDetails(Oeuvre oeuvre) {
        this.currentOeuvre = oeuvre;

        if (oeuvre != null) {
            nomField.setText(oeuvre.getNom());
            typeComboBox.setValue(oeuvre.getType());
            descriptionField.setText(oeuvre.getDescription());
            matiereField.setText(oeuvre.getMatiere());
            couleurField.setText(oeuvre.getCouleur());
            dimensionField.setText(oeuvre.getDimensions());
      useridtextfield.setText(String.valueOf(oeuvre.getUser_id()));
            categorieComboBox.setValue(oeuvre.getCategorie());

            if (oeuvre.getImage() != null && !oeuvre.getImage().isEmpty()) {
                try {
                    imageView.setImage(new Image("file:" + oeuvre.getImage()));
                } catch (Exception e) {
                    showAlert(AlertType.WARNING, "Erreur d'image", "Impossible de charger l'image: " + e.getMessage());
                }
            } else {
                imageView.setImage(null);
            }
        }
    }

    @FXML
    void newimageaction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers image", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur", "Impossible de charger l'image sélectionnée: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveChanges(ActionEvent event) {
        if (currentOeuvre == null) {
            showAlert(AlertType.ERROR, "Erreur", "Aucune œuvre sélectionnée pour modification.");
            return;
        }

        // Basic validation
        if (nomField.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Validation", "Le nom de l'œuvre ne peut pas être vide.");
            nomField.requestFocus();
            return;
        }

        if (typeComboBox.getValue() == null) {
            showAlert(AlertType.WARNING, "Validation", "Veuillez sélectionner un type.");
            typeComboBox.requestFocus();
            return;
        }

        if (categorieComboBox.getValue() == null) {
            showAlert(AlertType.WARNING, "Validation", "Veuillez sélectionner une catégorie.");
            categorieComboBox.requestFocus();
            return;
        }

        // Get form values
        String nom = nomField.getText().trim();
        String type = typeComboBox.getValue();
        String description = descriptionField.getText().trim();
        String matiere = matiereField.getText().trim();
        String couleur = couleurField.getText().trim();
        String dimensions = dimensionField.getText().trim();
        String categorie = categorieComboBox.getValue();

        // Parse user ID with validation
        int userId;
        try {
            userId = Integer.parseInt(useridtextfield.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(AlertType.WARNING, "Validation", "L'ID du créateur doit être un nombre valide.");
            useridtextfield.requestFocus();
            return;
        }

        String imagePath = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : currentOeuvre.getImage();

        Oeuvre updatedOeuvre = new Oeuvre(
                currentOeuvre.getId(),
                nom,
                type,
                description,
                matiere,
                couleur,
                dimensions,
                imagePath,

                categorie,
                userId
        );

        // Validate using Jakarta validation if needed
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Oeuvre>> violations = validator.validate(updatedOeuvre);

        if (!violations.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed:\n");
            for (ConstraintViolation<Oeuvre> violation : violations) {
                errorMessage.append("- ").append(violation.getMessage()).append("\n");
            }
            showAlert(AlertType.WARNING, "Validation", errorMessage.toString());
            return;
        }

        try {
            oeuvreService.update(updatedOeuvre);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("L'œuvre a été mise à jour avec succès !");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
                        Parent root = loader.load();

                        detail detailController = loader.getController();
                        detailController.setOeuvreDetails(updatedOeuvre);

                        Stage stage = (Stage) nomField.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.setMaximized(true);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir la vue des détails");
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void cancelChanges(ActionEvent event) {
        // Ask for confirmation before discarding changes
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir annuler les modifications ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Return to detail view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
                    Parent root = loader.load();

                    detail detailController = loader.getController();
                    detailController.setOeuvreDetails(currentOeuvre);

                    Stage stage = (Stage) nomField.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Erreur", "Impossible de retourner à la vue des détails.");
                }
            }
        });
    }
}