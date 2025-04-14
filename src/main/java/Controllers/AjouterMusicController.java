package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import Models.Music;
import Services.MusicService;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.io.File;


public class AjouterMusicController {

    @FXML
    private TextField titreTextField;

    @FXML
    private TextField artistNameTextField;

    @FXML
    private TextField genreTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private TextField cheminFicherTextField;

    @FXML
    private TextField photoTextField;

    @FXML
    private Button validerTextField;

    @FXML
    private Button parcourirButton;

    @FXML
    private Button parcourirAudioButton;

    private File selectedFile;

    @FXML
    private Label titreErrorLabel;
    @FXML
    private Label artistErrorLabel;
    @FXML
    private Label genreErrorLabel;
    @FXML
    private Label descriptionErrorLabel;
    @FXML
    private Label cheminErrorLabel;
    @FXML
    private Label photoErrorLabel;

    @FXML
    void handleParcourir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(parcourirButton.getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            photoTextField.setText(selectedFile.getName());
        }
    }

    @FXML
    void handleParcourirAudio(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier audio");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers Audio", "*.mp3", "*.wav", "*.aac", "*.ogg")
        );

        File audioFile = fileChooser.showOpenDialog(parcourirAudioButton.getScene().getWindow());
        if (audioFile != null) {
            cheminFicherTextField.setText(audioFile.getAbsolutePath());
        }
    }


    @FXML
    private void initialize() {
        addNumberFilter(titreTextField, titreErrorLabel);
        addNumberFilter(artistNameTextField, artistErrorLabel);
        addNumberFilter(genreTextField, genreErrorLabel);
        addNumberFilter(descriptionTextField, descriptionErrorLabel);
    }

    private void addNumberFilter(TextField textField, Label errorLabel) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            char c = event.getCharacter().charAt(0);
            if (Character.isDigit(c)) {
                event.consume(); // Block the number
                errorLabel.setText("Caractère requis.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false); // Hide error if valid input
            }
        });
    }
    @FXML
    void addMusicButton(ActionEvent event) {

        boolean isValid = true;

        // Clear previous error messages
        clearErrorMessages();

        // Validate Titre
        if (titreTextField.getText().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            titreErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Artist Name
        if (artistNameTextField.getText().isEmpty()) {
            artistErrorLabel.setText("Le nom de l'artiste est requis.");
            artistErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Genre
        if (genreTextField.getText().isEmpty()) {
            genreErrorLabel.setText("Le genre est requis.");
            genreErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Description
        if (descriptionTextField.getText().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Chemin Fichier
        if (cheminFicherTextField.getText().isEmpty()) {
            cheminErrorLabel.setText("Le chemin du fichier est requis.");
            cheminErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Photo
        if (photoTextField.getText().isEmpty()) {
            photoErrorLabel.setText("La photo est requise.");
            photoErrorLabel.setVisible(true);
            isValid = false;
        }

        // If all fields are valid, process the submission
        if (isValid) {

        String titre = titreTextField.getText();
        String artistName = artistNameTextField.getText();
        String genre = genreTextField.getText();
        String description = descriptionTextField.getText();
        String cheminFichierPath = null;
        Date dateSortie = new java.util.Date();  // current system date
        String photoPath = null;

        // Handle audio file path (cheminFichier)
        String originalChemin = cheminFicherTextField.getText();
        if (originalChemin != null && !originalChemin.isEmpty()) {
            File sourceAudioFile = new File(originalChemin);
            if (sourceAudioFile.exists()) {
                try {
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }
                    File destinationAudio = new File(destinationDir, sourceAudioFile.getName());
                    Files.copy(sourceAudioFile.toPath(), destinationAudio.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    cheminFichierPath = destinationAudio.getAbsolutePath();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la copie de l'audio : " + e.getMessage());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Audio manquant", "Veuillez sélectionner un fichier audio via le bouton 'Parcourir'.");
                return;
            }
        }

        // Handle image file (photo)
        if (selectedFile != null && selectedFile.exists()) {
            try {
                File destinationDir = new File("uploads");
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }

                File destination = new File(destinationDir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                photoPath = destination.getAbsolutePath();

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'upload de l'image : " + e.getMessage());
                return;
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Image manquante", "Veuillez sélectionner une image via le bouton 'Parcourir'.");
            return;
        }
            // Créer l'objet Music
            Music music = new Music(titre, 1, artistName, genre, description, dateSortie, cheminFichierPath, photoPath);
            MusicService musicService = new MusicService();

            try {
                musicService.add(music);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Musique ajoutée avec succès !");

//                // Charger la vue de détail
//                try {
//                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailMusic.fxml"));
//                    Parent root = loader.load();
//                    DetailMusicController detailMusicController = loader.getController();
//                    String formattedDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(dateSortie);
//
//                    detailMusicController.setTitreTextField(titre);
//                    detailMusicController.setArtistNameTextField(artistName);
//                    detailMusicController.setGenreTextField(genre);
//                    detailMusicController.setDescriptionTextField(description);
//                    detailMusicController.setDateTextField(formattedDate);
//                    detailMusicController.setCheminFicherTextField(cheminFichierPath);
//                    detailMusicController.setPhotoTextField(photoPath);
//
//                    titreTextField.getScene().setRoot(root);
//
//                } catch (IOException e) {
//                    System.out.println("Erreur lors du chargement du détail: " + e.getMessage());
//                }

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur ajout musique", e.getMessage());
            }
            showAlert1(Alert.AlertType.INFORMATION, "Succès", "La musique a été ajoutée avec succès.");
        } else {
            // Show error alert
            showAlert1(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs requis.");
        }
    }

private void clearErrorMessages() {
    titreErrorLabel.setVisible(false);
    artistErrorLabel.setVisible(false);
    genreErrorLabel.setVisible(false);
    descriptionErrorLabel.setVisible(false);
    cheminErrorLabel.setVisible(false);
    photoErrorLabel.setVisible(false);
}

private void showAlert1(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }


}
