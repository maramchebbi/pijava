package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import Models.Oeuvre;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class detail implements Initializable {

    @FXML
    private ImageView imageView;

    @FXML
    private Label nomImageLabel;

    // Labels remplaçant les TextField
    @FXML
    private Label nomLabel;
    @FXML
    private Label typeLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label matiereLabel;
    @FXML
    private Label couleurLabel;
    @FXML
    private Label dimensionLabel;
    @FXML
    private Label categorieLabel;

    @FXML
    private Button editImageButton;

    @FXML
    private Button btnView3D;

    @FXML
    private Button backButton;

    private Oeuvre currentOeuvre;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration initiale si nécessaire
    }

//    @FXML
//    private void handleEditImage() {
//        try {
//            Screen screen = Screen.getPrimary();
//            double screenWidth = screen.getVisualBounds().getWidth();
//            double screenHeight = screen.getVisualBounds().getHeight();
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ImageEditor.fxml"));
//            Parent root = loader.load();
//
//
//            ImageEditorController controller = loader.getController();
//            controller.setImageToEdit(currentOeuvre.getImage());
//
//        Stage stage = new Stage();
//        stage.setTitle("Édition d'image - " + currentOeuvre.getNom());
//            stage.setScene(new Scene(root,screenWidth,screenHeight));
//            stage.showAndWait(); // Utiliser showAndWait pour bloquer jusqu'à fermeture
//
//            // Si l'image a été modifiée, vous pourriez la récupérer ici
//            if (controller.isImageModified()) {
//                // Mettre à jour l'image si nécessaire
//                refreshImageDisplay();
//            }
//        } catch (IOException e) {
//            showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur d'image: " + e.getMessage());
//        }
//    }
@FXML
private void handleEditImage(ActionEvent event) { // Ajoutez ActionEvent en paramètre
    try {
        // Charger la nouvelle interface
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ImageEditor.fxml"));
        Parent root = loader.load();

        // Configurer le contrôleur
        ImageEditorController controller = loader.getController();
        controller.setImageToEdit(currentOeuvre.getImage());

        // Récupérer la scène et le stage actuels
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Sauvegarder la taille actuelle
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();

        // Changer la scène du stage existant
        Scene newScene = new Scene(root, currentWidth, currentHeight);
        stage.setScene(newScene);
        stage.setTitle("Édition d'image - " + currentOeuvre.getNom());

        // Configurer le retour (vous devrez implémenter cette méthode dans ImageEditorController)
        controller.setReturnCallback(() -> {
            if (controller.isImageModified()) {
                refreshImageDisplay();
            }
            // Pour revenir à la vue précédente, vous pourriez réutiliser le même mécanisme
        });

    } catch (IOException e) {
        showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur d'image: " + e.getMessage());
    }
}

    private void refreshImageDisplay() {
        // Recharger l'image depuis le disque pour afficher les modifications
        if (currentOeuvre != null && currentOeuvre.getImage() != null) {
            Image updatedImage = new Image("file:" + currentOeuvre.getImage());
            imageView.setImage(updatedImage);
        }
    }

    public void setOeuvreDetails(Oeuvre t) {
        this.currentOeuvre = t;

        // Set the image
        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                Image image = new Image("file:" + t.getImage());
                imageView.setImage(image);

                // Ajuster l'image pour qu'elle conserve ses proportions
                imageView.setPreserveRatio(true);

                // Mettre à jour le label sous l'image
                nomImageLabel.setText(t.getNom());
            } catch (Exception e) {
                showAlert(AlertType.WARNING, "Erreur d'Image", "Impossible de charger l'image: " + e.getMessage());
            }
        }

        // Set other artwork details using Labels instead of TextFields
        nomLabel.setText(t.getNom());
        typeLabel.setText(t.getType());
        descriptionLabel.setText(t.getDescription());
        matiereLabel.setText(t.getMatiere());
        couleurLabel.setText(t.getCouleur());
        dimensionLabel.setText(t.getDimensions());
        categorieLabel.setText(t.getCategorie());
    }

    @FXML
    private void handleOK(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Load the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Configuration pour fullscreen
            Scene scene = new Scene(root, screenWidth, screenHeight);
            stage.setScene(scene);

            // Maximiser la fenêtre pour utiliser tout l'écran
            stage.setMaximized(true);

            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur de Navigation", "Impossible de retourner à la vue principale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Simple overload to maintain backward compatibility
    private void showAlert(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }

    @FXML
    public void afficherViewer3D() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PotteryViewer.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur et lui transmettre l'oeuvre actuelle
            PotteryViewerController controller = loader.getController();
            if (controller != null && currentOeuvre != null) {
                controller.setOeuvre(currentOeuvre);
            }

            // Créer une nouvelle scène
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Visualiseur 3D - " + (currentOeuvre != null ? currentOeuvre.getNom() : ""));

            // Configurez la modalité pour rendre la fenêtre modale
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger le visualiseur 3D: " + e.getMessage());
        }
    }
}