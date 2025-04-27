package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.Reclamation;
import utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DetailReclamationController {

    @FXML
    private Label nomPrenomLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label optionLabel;
    @FXML
    private TextArea descriptionLabel;
    @FXML
    private VBox attachmentBox;
    @FXML
    private Label fileNameLabel;
    @FXML
    private Button downloadButton;
    @FXML
    private ImageView imagePreview;

    private Reclamation currentReclamation;

    public void setReclamation(Reclamation reclamation) {
        this.currentReclamation = reclamation;

        nomPrenomLabel.setText("👤 Nom et prénom : " + reclamation.getUser().getNom() + " " + reclamation.getUser().getPrenom());
        emailLabel.setText("📧 Email : " + reclamation.getUser().getEmail());
        optionLabel.setText("📌 Option : " + reclamation.getOption());
        descriptionLabel.setText("📝 Description : " + reclamation.getDescription());

        // Vérifier si une pièce jointe existe
        if (reclamation.getFilePath() != null && !reclamation.getFilePath().isEmpty()) {
            File file = new File(reclamation.getFilePath());
            if (file.exists()) {
                // Afficher la section pièce jointe
                attachmentBox.setVisible(true);
                attachmentBox.setManaged(true);

                // Afficher le nom du fichier
                fileNameLabel.setText(file.getName());

                // Vérifier si c'est une image et afficher un aperçu
                if (isImageFile(file)) {
                    try {
                        // Créer une image à partir du fichier et l'afficher dans ImageView
                        Image image = new Image(file.toURI().toString());
                        imagePreview.setImage(image);
                        imagePreview.setVisible(true);
                        imagePreview.setManaged(true);

                        // Définir une largeur maximale pour l'aperçu
                        imagePreview.setFitWidth(300);
                        imagePreview.setPreserveRatio(true);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
                        imagePreview.setVisible(false);
                        imagePreview.setManaged(false);
                    }
                } else {
                    // Ce n'est pas une image, masquer l'aperçu
                    imagePreview.setVisible(false);
                    imagePreview.setManaged(false);
                }
            } else {
                // Le fichier n'existe plus sur le disque
                attachmentBox.setVisible(false);
                attachmentBox.setManaged(false);
                imagePreview.setVisible(false);
                imagePreview.setManaged(false);
            }
        } else {
            // Pas de pièce jointe
            attachmentBox.setVisible(false);
            attachmentBox.setManaged(false);
            imagePreview.setVisible(false);
            imagePreview.setManaged(false);
        }
    }

    /**
     * Vérifie si le fichier est une image basée sur son extension
     */
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp") || name.endsWith(".webp");
    }

    @FXML
    private void handleDownloadFile() {
        if (currentReclamation == null || currentReclamation.getFilePath() == null) {
            return;
        }

        File sourceFile = new File(currentReclamation.getFilePath());
        if (!sourceFile.exists()) {
            showAlert("Erreur", "Le fichier n'existe plus sur le serveur.");
            return;
        }

        // Ouvrir un sélecteur de dossier où enregistrer le fichier
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir l'emplacement de téléchargement");

        // Définir un dossier initial (dossier Téléchargements de l'utilisateur)
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome + "/Downloads");
        if (downloadsDir.exists()) {
            directoryChooser.setInitialDirectory(downloadsDir);
        } else {
            directoryChooser.setInitialDirectory(new File(userHome));
        }

        // Afficher le sélecteur de dossier
        Stage stage = (Stage) downloadButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            try {
                // Créer un chemin de destination avec le nom original du fichier
                Path destination = Paths.get(selectedDirectory.getAbsolutePath(), sourceFile.getName());

                // Copier le fichier vers la destination
                Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                showAlert("Succès", "Fichier téléchargé avec succès!\nEmplacement: " + destination.toString());

            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors du téléchargement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}