package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.awt.Desktop;
import javafx.event.ActionEvent;
import Models.Oeuvre;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;

public class ImageEditorController {
    private Oeuvre currentOeuvre;

    @FXML private ImageView imageView;
    @FXML private Slider brightnessSlider;
    @FXML private Slider contrastSlider;
    @FXML private Slider sharpnessSlider;
    @FXML private TextField widthField;
    @FXML private TextField heightField;
    @FXML
    private Button retourbutton;

    private ImageProcessingService imageService;
    private String currentImagePath;
    private Image originalImage;
    private Image processedImage;
    private boolean imageModified = false;
    private File lastSavedFile;
    private Runnable returnCallback;

    public void initialize() {
        imageService = new ImageProcessingService();


        // Ajout d'écouteurs pour l'affichage en temps réel
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
        sharpnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    /**
     * Méthode pour définir une image à éditer à partir d'un chemin
     * @param imagePath Chemin de l'image à éditer
     */
    public void setImageToEdit(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            loadImage(imagePath);
        }
    }

    public boolean isImageModified() {
        return imageModified;
    }

    public void setCurrentOeuvre(Oeuvre oeuvre) {
        this.currentOeuvre = oeuvre;
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
            loadImage(selectedFile.getAbsolutePath());
        }
    }

    public void loadImage(String path) {
        try {
            currentImagePath = path;
            File imageFile = new File(path);
            originalImage = new Image(imageFile.toURI().toString());
            imageView.setImage(originalImage);
            processedImage = originalImage;

            // Préremplir les dimensions
            widthField.setText(String.valueOf((int)originalImage.getWidth()));
            heightField.setText(String.valueOf((int)originalImage.getHeight()));

            // Réinitialiser les curseurs
            resetSliders();

            imageModified = false;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveImage() {
        if (currentImagePath == null || imageView.getImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image à sauvegarder.");
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
        fileChooser.setInitialFileName(baseName + "_édité.png");

        File outputFile = fileChooser.showSaveDialog(imageView.getScene().getWindow());
        if (outputFile != null) {
            try {
                // Utiliser WritableImage pour sauvegarder l'état actuel
                WritableImage writableImage = imageViewToWritableImage(imageView);

                // Sauvegarder l'image
                saveImageToFile(writableImage, outputFile);

                lastSavedFile = outputFile;
                imageModified = false;

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Image sauvegardée avec succès!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder l'image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveAndDownload() {
        // D'abord sauvegarder l'image
        if (currentImagePath == null || imageView.getImage() == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image à télécharger.");
            return;
        }

        // Si l'image n'a pas été sauvegardée récemment, demander où sauvegarder
        if (lastSavedFile == null) {
            handleSaveImage();
            if (lastSavedFile == null) return; // L'utilisateur a annulé
        }

        // Essayer d'ouvrir l'image avec l'application par défaut
        try {
            if (Desktop.isDesktopSupported() && lastSavedFile.exists()) {
                Desktop.getDesktop().open(lastSavedFile);
            } else {
                // Si l'ouverture directe ne fonctionne pas, montrer le fichier dans l'explorateur
                Desktop.getDesktop().browse(lastSavedFile.getParentFile().toURI());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir l'image. L'image a été sauvegardée à: " + lastSavedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleAutoEnhance() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            processedImage = imageService.autoEnhance(currentImagePath);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'améliorer l'image: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        if (originalImage != null) {
            imageView.setImage(originalImage);
            processedImage = originalImage;
            resetSliders();
            imageModified = false;
        }
    }

    @FXML
    private void handleApplyChanges() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            // Appliquer les ajustements
            processedImage = imageService.applyAdjustments(
                    currentImagePath,
                    brightnessSlider.getValue(),
                    contrastSlider.getValue(),
                    sharpnessSlider.getValue()
            );

            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer les modifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdgeDetection() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            processedImage = imageService.detectEdges(currentImagePath);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de détecter les bords: " + e.getMessage());
        }
    }

    @FXML
    private void handleBlackAndWhite() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            processedImage = imageService.convertToBlackAndWhite(currentImagePath);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer le filtre noir et blanc: " + e.getMessage());
        }
    }

    @FXML
    private void handleSepia() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            processedImage = imageService.applySepiaFilter(currentImagePath);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer le filtre sépia: " + e.getMessage());
        }
    }

    @FXML
    private void handleBlur() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            processedImage = imageService.applyBlurFilter(currentImagePath);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'appliquer le filtre de flou: " + e.getMessage());
        }
    }

    @FXML
    private void handleResize() {
        if (currentImagePath == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucune image chargée.");
            return;
        }

        try {
            int width = Integer.parseInt(widthField.getText());
            int height = Integer.parseInt(heightField.getText());

            if (width <= 0 || height <= 0) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Les dimensions doivent être positives.");
                return;
            }

            processedImage = imageService.resizeImage(currentImagePath, width, height);
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez entrer des dimensions valides.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de redimensionner l'image: " + e.getMessage());
        }
    }

    private void updatePreview() {
        // Pour une mise à jour en temps réel pendant le glissement des curseurs
        if (currentImagePath == null) return;

        try {
            processedImage = imageService.previewAdjustments(
                    currentImagePath,
                    brightnessSlider.getValue(),
                    contrastSlider.getValue(),
                    sharpnessSlider.getValue()
            );
            imageView.setImage(processedImage);
            imageModified = true;
        } catch (Exception e) {
            // Éviter d'afficher des alertes pendant le glissement
            e.printStackTrace();
        }
    }

    private void resetSliders() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(0);
        sharpnessSlider.setValue(0);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour convertir ImageView en WritableImage
    private WritableImage imageViewToWritableImage(ImageView view) {
        Image image = view.getImage();

        // Créer une WritableImage
        WritableImage writableImage = new WritableImage(
                (int)image.getWidth(),
                (int)image.getHeight());

        // Obtenir le PixelWriter
        PixelReader pixelReader = image.getPixelReader();
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        // Copier les pixels
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
            }
        }

        return writableImage;
    }

    // Sauvegarder l'image dans un fichier
    private void saveImageToFile(WritableImage image, File file) throws IOException {
        String extension = getFileExtension(file);

        // Créer un snapshot pour sauvegarder
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension, file);
    }

    // Obtenir l'extension du fichier
    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "png"; // Extension par défaut
    }

    // Pour sauvegarder l'image, nous avons besoin d'utiliser JavaFX avec SwingFXUtils
    private static class ImageIO {
        public static void write(java.awt.image.BufferedImage img, String formatName, File output) throws IOException {
            javax.imageio.ImageIO.write(img, formatName, output);
        }
    }

    private static class SwingFXUtils {
        public static java.awt.image.BufferedImage fromFXImage(Image img, java.awt.image.BufferedImage bimg) {
            int width = (int) img.getWidth();
            int height = (int) img.getHeight();

            if (bimg == null) {
                bimg = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }

            PixelReader pr = img.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = pr.getColor(x, y);
                    int argb = (
                            ((int) (color.getOpacity() * 255) << 24) |
                                    ((int) (color.getRed() * 255) << 16) |
                                    ((int) (color.getGreen() * 255) << 8) |
                                    ((int) (color.getBlue() * 255))
                    );
                    bimg.setRGB(x, y, argb);
                }
            }

            return bimg;
        }
    }

    public void setReturnCallback(Runnable callback) {
        this.returnCallback = callback;
    }


    @FXML
    private void handleReturn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
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
            e.printStackTrace();
        }
    }
}