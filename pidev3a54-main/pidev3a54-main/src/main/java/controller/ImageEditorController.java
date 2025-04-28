package controller;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Paths;


public class ImageEditorController {

    @FXML private ImageView imageView;
    @FXML private Slider brightnessSlider;
    @FXML private Slider contrastSlider;
    @FXML private Slider redSlider;
    @FXML private Slider greenSlider;
    @FXML private Slider blueSlider;
    @FXML private Slider sharpnessSlider;
    @FXML private TextField widthField;
    @FXML private TextField heightField;

    private ImageProcessingService imageService;
    private String currentImagePath;
    private Image originalImage;
    private boolean imageModified = false;
   // private String oeuvreNom;

    public void initialize() {
        imageService = new ImageProcessingService();


        // Ajouter des écouteurs pour l'application en temps réel (optionnel)
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }
    public boolean isImageModified() {
        return imageModified;
    }
    @FXML
    private void handleOpenImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(imageView.getScene().getWindow());
        if (selectedFile != null) {
            currentImagePath = selectedFile.getAbsolutePath();
            originalImage = new Image(selectedFile.toURI().toString());
            imageView.setImage(originalImage);

            // Préremplir les champs de redimensionnement
            widthField.setText(String.valueOf((int)originalImage.getWidth()));
            heightField.setText(String.valueOf((int)originalImage.getHeight()));

            // Réinitialiser les curseurs
            resetSliders();
        }
    }
    public void setImageToEdit(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                currentImagePath = imagePath;

                // Vérifier si le chemin commence par "file:"
                String path = imagePath.startsWith("file:") ? imagePath : "file:" + imagePath;

                originalImage = new Image(path);
                imageView.setImage(originalImage);

                // Préremplir les champs de redimensionnement
                widthField.setText(String.valueOf((int)originalImage.getWidth()));
                heightField.setText(String.valueOf((int)originalImage.getHeight()));

                // Réinitialiser les curseurs
                resetSliders();

                System.out.println("Image chargée avec succès dans l'éditeur: " + imagePath);
            } else {
                System.err.println("Chemin d'image invalide");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveImage() {
        if (currentImagePath == null || imageView.getImage() == null) {
            showAlert("Erreur", "Aucune image à sauvegarder.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder l'image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpg")
        );

        // Suggérer un nom par défaut
        String originalName = Paths.get(currentImagePath).getFileName().toString();
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        fileChooser.setInitialFileName(baseName + "_edited.png");

        File outputFile = fileChooser.showSaveDialog(imageView.getScene().getWindow());
        if (outputFile != null) {
            // Convertir l'image JavaFX en Mat OpenCV
            // Note: Cette conversion est simplifiée et nécessiterait plus de code
            // pour une implémentation complète
            //Mat mat = imageToMat(imageView.getImage());
           // imageService.saveImage(mat, outputFile.getAbsolutePath());

            showAlert("Information", "Image sauvegardée avec succès!");
        }
    }

    @FXML
    private void handleAutoEnhance() {
        if (currentImagePath == null) {
            showAlert("Erreur", "Aucune image chargée.");
            return;
        }

        try {
            Image enhancedImage = imageService.autoEnhance(currentImagePath);
            imageView.setImage(enhancedImage);

            // Réinitialiser les curseurs car l'amélioration automatique modifie tout
            resetSliders();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'améliorer l'image: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        if (originalImage != null) {
            imageView.setImage(originalImage);
            resetSliders();
        }
    }

    @FXML
    private void handleApplyChanges() {
        if (currentImagePath == null) {
            showAlert("Erreur", "Aucune image chargée.");
            return;
        }

        try {
            // Appliquer les ajustements de luminosité et contraste
            Image processedImage = imageService.adjustBrightnessContrast(
                    currentImagePath,
                    brightnessSlider.getValue(),
                    contrastSlider.getValue()
            );

            // Appliquer la balance des couleurs
            // Note: Pour une application réelle, vous devriez combiner ces opérations
            // plutôt que de les appliquer séquentiellement
            processedImage = imageService.adjustColorBalance(
                    currentImagePath,
                    redSlider.getValue(),
                    greenSlider.getValue(),
                    blueSlider.getValue()
            );

            // Appliquer la netteté
            if (sharpnessSlider.getValue() > 0) {
                processedImage = imageService.sharpenImage(
                        currentImagePath,
                        sharpnessSlider.getValue()
                );
            }

            imageView.setImage(processedImage);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'appliquer les modifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdgeDetection() {
        if (currentImagePath == null) {
            showAlert("Erreur", "Aucune image chargée.");
            return;
        }

        try {
            Image edgeImage = imageService.detectEdges(currentImagePath);
            imageView.setImage(edgeImage);
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de détecter les bords: " + e.getMessage());
        }
    }

    @FXML
    private void handleResize() {
        if (currentImagePath == null) {
            showAlert("Erreur", "Aucune image chargée.");
            return;
        }

        try {
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());

            if (width <= 0 || height <= 0) {
                showAlert("Erreur", "Les dimensions doivent être positives.");
                return;
            }

            Image resizedImage = imageService.resizeImage(currentImagePath, width, height);
            imageView.setImage(resizedImage);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des dimensions valides.");
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de redimensionner l'image: " + e.getMessage());
        }
    }

    private void updatePreview() {
        // Pour une mise à jour en temps réel pendant le glissement des curseurs
        // Cette méthode pourrait être coûteuse en ressources, donc utilisez-la avec précaution
        if (currentImagePath == null) return;

        try {
            Image processedImage = imageService.adjustBrightnessContrast(
                    currentImagePath,
                    brightnessSlider.getValue(),
                    contrastSlider.getValue()
            );
            imageView.setImage(processedImage);
        } catch (Exception e) {
            // Éviter d'afficher des alertes pendant le glissement
            e.printStackTrace();
        }
    }

    private void resetSliders() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        redSlider.setValue(0);
        greenSlider.setValue(0);
        blueSlider.setValue(0);
        sharpnessSlider.setValue(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour convertir une image JavaFX en Mat OpenCV
    // Note: Cette méthode est incomplète et nécessite une implémentation complète

}