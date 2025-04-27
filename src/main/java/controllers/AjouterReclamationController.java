package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Reclamation;
import models.User;
import service.ReclamationService;
import utils.BadWordsAPI;
import utils.SessionManager;
import utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterReclamationController implements Initializable {

    @FXML
    private ComboBox<String> optionCombo;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label statusLabel;

    @FXML
    private Label previewLabel;

    @FXML
    private HBox previewBox;

    @FXML
    private VBox dropZone;

    private File selectedFile;
    private String uploadedFilePath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ajouter des options à la ComboBox
        optionCombo.getItems().addAll("Technique", "Service", "Facturation", "Autre");

        // Configurer les événements de drag and drop
        setupDragAndDrop();
    }

    private void setupDragAndDrop() {
        // Lorsqu'un fichier est traîné au-dessus de la zone
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                // Changement visuel pour indiquer que la zone accepte le dépôt
                dropZone.setStyle("-fx-border-color: #3b82f6; -fx-border-style: dashed; -fx-border-radius: 4; " +
                        "-fx-border-width: 2; -fx-background-color: #eff6ff; -fx-padding: 15;");
            }
            event.consume();
        });

        // Lorsque le fichier quitte la zone de dépôt
        dropZone.setOnDragExited(event -> {
            // Restaurer le style normal
            dropZone.setStyle("-fx-border-color: #cbd5e1; -fx-border-style: dashed; -fx-border-radius: 4; " +
                    "-fx-border-width: 2; -fx-background-color: #f8fafc; -fx-padding: 15;");
            event.consume();
        });

        // Lorsqu'un fichier est déposé dans la zone
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                if (!files.isEmpty()) {
                    File file = files.get(0); // On prend juste le premier fichier

                    // Vérifier si le fichier est d'un type accepté (image ou PDF)
                    if (FileUtil.isImage(file) || FileUtil.isPDF(file)) {
                        selectedFile = file;
                        updateFilePreview(selectedFile);
                        success = true;
                    } else {
                        showAlert("Type de fichier non supporté",
                                "Veuillez déposer une image (PNG, JPG, GIF) ou un document PDF.");
                    }
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    @FXML
    private void handleUploadFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        // Configurer les filtres pour images et documents
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif");
        FileChooser.ExtensionFilter pdfFilter =
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf");
        fileChooser.getExtensionFilters().addAll(imageFilter, pdfFilter);

        // Afficher le sélecteur de fichier
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            updateFilePreview(selectedFile);
        }
    }

    private void updateFilePreview(File file) {
        if (file != null) {
            previewLabel.setText(file.getName() + " (" + FileUtil.getReadableFileSize(file) + ")");
            previewBox.setVisible(true);
            previewBox.setManaged(true);
        }
    }

    @FXML
    private void handleRemoveFile() {
        selectedFile = null;
        previewBox.setVisible(false);
        previewBox.setManaged(false);
        uploadedFilePath = null;
    }

    @FXML
    private void handleAjouterReclamation(ActionEvent event) {
        String option = optionCombo.getValue();
        String description = descriptionArea.getText().trim();

        // Vérifier si BadWordsAPI est disponible et l'utiliser
        try {
            description = BadWordsAPI.filterBadWords(description);
        } catch (Exception e) {
            // Si la classe ou méthode n'existe pas, continuer sans filtrage
            System.out.println("BadWordsAPI non disponible: " + e.getMessage());
        }

        if (option == null || description.isEmpty()) {
            statusLabel.setText("Veuillez remplir tous les champs !");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            statusLabel.setText("Aucun utilisateur connecté !");
            return;
        }

        // Copier le fichier vers un répertoire d'uploads si un fichier est sélectionné
        if (selectedFile != null) {
            try {
                uploadedFilePath = saveFileToUploadsFolder(selectedFile, currentUser.getId());
            } catch (IOException e) {
                statusLabel.setText("Erreur lors de l'enregistrement du fichier !");
                e.printStackTrace();
                return;
            }
        }

        Reclamation reclamation = new Reclamation(option, description, currentUser);

        // Ajouter le chemin du fichier à la réclamation si un fichier a été uploadé
        if (uploadedFilePath != null) {
            reclamation.setFilePath(uploadedFilePath);
        }

        ReclamationService service = new ReclamationService();

        try {
            service.add(reclamation);
            statusLabel.setText("Réclamation ajoutée avec succès !");

            // Vider les champs après ajout
            optionCombo.setValue(null);
            descriptionArea.clear();
            handleRemoveFile();

            // Redirection vers AfficherReclamation.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Changer la scène pour afficher les réclamations
            stage.setScene(new Scene(root));
            stage.show();

        } catch (SQLException e) {
            statusLabel.setText("Erreur lors de l'ajout !");
            e.printStackTrace();
        } catch (IOException e) {
            statusLabel.setText("Erreur de redirection !");
            e.printStackTrace();
        }
    }

    private String saveFileToUploadsFolder(File file, int userId) throws IOException {
        // Créer un nom de fichier unique basé sur l'ID utilisateur et un timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = userId + "_" + timestamp + "_" + file.getName();

        // Créer le dossier uploads s'il n'existe pas
        Path uploadsDir = Paths.get("uploads");
        if (!Files.exists(uploadsDir)) {
            Files.createDirectories(uploadsDir);
        }

        // Chemin de destination pour le fichier
        Path destination = uploadsDir.resolve(fileName);

        // Copier le fichier vers le dossier uploads
        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Retourner le chemin relatif
        return destination.toString();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur retour arrière : " + e.getMessage());
        }
    }
}
