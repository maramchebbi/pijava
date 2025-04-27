package controller;

import Models.collection_t;
import Models.textile;
import Services.TextileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AjouterTexile {


    // FXML injections for progress indicators
    @FXML private Circle step1Circle;
    @FXML private Label step1Label;
    @FXML private Circle step2Circle;
    @FXML private Label step2Label;
    @FXML
    private ComboBox<collection_t> collectionIdField;
    @FXML private VBox part1Form;
    @FXML private VBox part2Form;


    // FXML injections for form fields (keep all your existing fields)
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
    @FXML private TextField createurField;
    @FXML private TextField techniquefield;
    @FXML private TextField iduserfield;
    @FXML private Button uploadBtn;
    @FXML private Label imageNameLabel;
    @FXML private ImageView textileimage;

    // Error labels
    @FXML private Label nomError;
    @FXML private Label typeError;
    @FXML private Label descriptionError;
    @FXML private Label matiereError;
    @FXML private Label couleurError;
    @FXML private Label dimensionError;
    @FXML private Label createurError;
    @FXML private Label techniqueError;
    @FXML private Label collectionError;
    @FXML private Label userError;

    // Méthodes de navigation entre les parties du formulaire


    @FXML
    private void handleNextButton() {
        clearErrorMessages();
        boolean isValid = validatePart1();

        if (isValid) {
            part1Form.setVisible(false);
            part2Form.setVisible(true);
            updateProgressIndicator(true);
        }
    }
    @FXML
    private void handlePreviousButton() {
        part1Form.setVisible(true);
        part2Form.setVisible(false);
        updateProgressIndicator(false);
    }

    private boolean validatePart1() {
        boolean isValid = true;

        if (nomField.getText().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            isValid = false;
        }

        if (typeField.getText().isEmpty()) {
            typeError.setText("Le type est obligatoire");
            isValid = false;
        }

        if (descriptionField.getText().isEmpty()) {
            descriptionError.setText("La description est obligatoire");
            isValid = false;
        }

        if (matiereField.getText().isEmpty()) {
            matiereError.setText("La matière est obligatoire");
            isValid = false;
        }

        if (couleurField.getText().isEmpty()) {
            couleurError.setText("La couleur est obligatoire");
            isValid = false;
        }

        return isValid;
    }
    private void updateProgressIndicator(boolean isPart2Active) {
        if (isPart2Active) {
            // Part 2 is active - make step 2 highlighted
            step1Circle.setFill(Color.web("#b29f94"));
            step1Label.setStyle("-fx-text-fill: #603813;");
            step2Circle.setFill(Color.web("#603813"));
            step2Label.setStyle("-fx-text-fill: white;");
        } else {
            // Part 1 is active - make step 1 highlighted
            step1Circle.setFill(Color.web("#603813"));
            step1Label.setStyle("-fx-text-fill: white;");
            step2Circle.setFill(Color.web("#b29f94"));
            step2Label.setStyle("-fx-text-fill: #603813;");
        }
    }
    private void clearErrorMessages() {
        nomError.setText("");
        typeError.setText("");
        descriptionError.setText("");
        matiereError.setText("");
        couleurError.setText("");
        dimensionError.setText("");
        createurError.setText("");
        techniqueError.setText("");
        collectionError.setText("");
        userError.setText("");
    }
    // Méthodes existantes (conservées sans modification)

    @FXML
    public void ajouterTextile(ActionEvent event) {
        clearErrorMessages();

        // Récupération des valeurs
        String nom = nomField.getText().trim();
        String type = typeField.getText().trim();
        String description = descriptionField.getText().trim();
        String matiere = matiereField.getText().trim();
        String dimension = dimensionField.getText().trim();
        String couleur = couleurField.getText().trim();
        String createur = createurField.getText().trim();
        String technique = techniquefield.getText().trim();
        String userIdText = iduserfield.getText().trim();
        collection_t selectedCollection = collectionIdField.getValue();

        // Validation
        boolean isValid = true;

        // Valider dans l'ordre des champs
        if (!validateNom(nom)) isValid = false;
        if (!validateType(type)) isValid = false;
        if (!validateDescription(description)) isValid = false;
        if (!validateMatiere(matiere)) isValid = false;
        if (!validateDimension(dimension)) isValid = false;
        if (!validateCouleur(couleur)) isValid = false;
        if (!validateCreateur(createur)) isValid = false;
        if (!validateTechnique(technique)) isValid = false;

        // Validation ID utilisateur
        try {
            Integer.parseInt(userIdText);
            userError.setText("");
        } catch (NumberFormatException e) {
            userError.setText("L'ID utilisateur doit être un nombre");
            isValid = false;
        }

        // Validation collection
        if (selectedCollection == null) {
            collectionError.setText("Veuillez sélectionner une collection");
            isValid = false;
        } else {
            collectionError.setText("");
        }

        // Validation image
        if (textileimage.getImage() == null) {
            userError.setText("Veuillez sélectionner une image valide");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Si validation OK, procéder à l'ajout
        int userId = Integer.parseInt(userIdText);
        int collectionId = selectedCollection.getId();
        String imagePath = "";

        if (textileimage.getImage() != null) {
            Image image = textileimage.getImage();
            imagePath = image.getUrl();
            if (imagePath != null && imagePath.startsWith("file:/")) {
                imagePath = imagePath.replace("file:/", "");
            }
        }

        textile textileObj = new textile(
                collectionId, nom, type, description,
                matiere, couleur, dimension, createur,
                imagePath, technique, userId
        );

        TextileService textileService = new TextileService();

        try {
            textileService.add(textileObj);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Textile ajouté avec succès !");
            alert.show();

            // Redirection
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (SQLException | IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ajout");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }

    public void viewtextile(ActionEvent actionEvent) {
        try {
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadimage(ActionEvent actionEvent) {
        userError.setText("");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.jpg", "*.jpeg", "*.png", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) textileimage.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                long maxSize = 5 * 1024 * 1024;
                if (file.length() > maxSize) {
                    throw new IOException("La taille de l'image ne doit pas dépasser 5MB");
                }

                String fileName = file.getName().toLowerCase();
                if (!fileName.matches(".*\\.(jpg|jpeg|png|bmp)$")) {
                    throw new IOException("Extension non valide. Formats acceptés: JPG, JPEG, PNG, BMP");
                }

                Image image = new Image(file.toURI().toString());
                textileimage.setImage(image);
                userError.setText("");

            } catch (Exception e) {
                userError.setText("Erreur: " + e.getMessage());
                textileimage.setImage(null);
            }
        } else {
            userError.setText("Aucune image sélectionnée");
        }
    }

    @FXML
    public void initialize() {
        TextileService textileService = new TextileService();

        try {
            List<collection_t> collections = textileService.getAllCollections();
            collectionIdField.getItems().addAll(collections);

            collectionIdField.setCellFactory(lv -> new ListCell<collection_t>() {
                @Override
                protected void updateItem(collection_t item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });

            collectionIdField.setButtonCell(new ListCell<collection_t>() {
                @Override
                protected void updateItem(collection_t item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthodes de validation (conservées sans modification)
    private boolean validateNom(String value) {
        if (value == null || value.isEmpty()) {
            nomError.setText("Le nom est requis");
            return false;
        }
        if (value.length() < 2 || value.length() > 100) {
            nomError.setText("2-100 caractères requis");
            return false;
        }
        nomError.setText("");
        return true;
    }

    private boolean validateType(String value) {
        if (value == null || value.isEmpty()) {
            typeError.setText("Le type est requis");
            return false;
        }
        typeError.setText("");
        return true;
    }

    private boolean validateDescription(String value) {
        if (value == null || value.isEmpty()) {
            descriptionError.setText("La description est requise");
            return false;
        }
        descriptionError.setText("");
        return true;
    }

    private boolean validateMatiere(String value) {
        if (value == null || value.isEmpty()) {
            matiereError.setText("La matière est requise");
            return false;
        }
        matiereError.setText("");
        return true;
    }

    private boolean validateDimension(String value) {
        if (value == null || value.isEmpty()) {
            dimensionError.setText("La dimension est requise");
            return false;
        }
        if (!value.matches("^\\d+\\*\\d+$")) {
            dimensionError.setText("Format: nombre*nombre (ex: 30*50)");
            return false;
        }
        dimensionError.setText("");
        return true;
    }

    private boolean validateCouleur(String value) {
        if (value == null || value.isEmpty()) {
            couleurError.setText("La couleur est requise");
            return false;
        }
        couleurError.setText("");
        return true;
    }

    private boolean validateCreateur(String value) {
        if (value == null || value.isEmpty()) {
            createurError.setText("Le créateur est requis");
            return false;
        }
        createurError.setText("");
        return true;
    }

    private boolean validateTechnique(String value) {
        if (value == null || value.isEmpty()) {
            techniqueError.setText("La technique est requise");
            return false;
        }
        techniqueError.setText("");
        return true;
    }

    }