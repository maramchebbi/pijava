package Controllers;

import Models.Music;
import Services.MusicService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class UpdateMusicController {

    @FXML
    private TextField titreTextField;

    @FXML
    private TextField artistTextField;

    @FXML
    private TextField genreTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private TextField cheminFicherTextField;

    @FXML
    private TextField photoTextField;

    @FXML
    private Button parcourirButton;

    @FXML
    private Button parcourirAudioButton;

    private File selectedFile;

    @FXML private Label titreErrorLabel;
    @FXML private Label artistErrorLabel;
    @FXML private Label genreErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label cheminErrorLabel;
    @FXML private Label photoErrorLabel;

    private Music currentMusic;

    public void setMusic(Music music) {
        this.currentMusic = music;
        titreTextField.setText(music.getTitre());
        artistTextField.setText(music.getArtistName());
        genreTextField.setText(music.getGenre());
        descriptionTextField.setText(music.getDescription());
        cheminFicherTextField.setText(music.getCheminFichier());
        photoTextField.setText(music.getPhoto());
    }

    @FXML
    private void handleParcourirAudio(ActionEvent event) {
        // Créer une instance de FileChooser pour sélectionner le fichier audio
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers audio", "*.mp3", "*.wav", "*.flac"));

        // Ouvrir le FileChooser et obtenir le fichier sélectionné
        File audioFile = fileChooser.showOpenDialog(null);

        // Vérifier si un fichier a été sélectionné
        if (audioFile != null) {
            cheminFicherTextField.setText(audioFile.getAbsolutePath()); // Mettre à jour le TextField avec le chemin du fichier sélectionné
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucun fichier sélectionné", "Veuillez sélectionner un fichier audio.");
        }
    }


    @FXML
    private void handleParcourir(ActionEvent event) {
        // Create a FileChooser to select the photo file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");

        // Set filters to only allow image files to be selected
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        // Open the file chooser and get the selected file
        File file = fileChooser.showOpenDialog(parcourirButton.getScene().getWindow());
        if (file != null) {
            // If a file is selected, set the file path to the text field
            photoTextField.setText(file.getAbsolutePath());
        }
    }


    @FXML
    private void initialize() {
        // Only characters allowed
        addCharacterOnlyFilter(titreTextField, titreErrorLabel);
        addCharacterOnlyFilter(artistTextField, artistErrorLabel);
        addCharacterOnlyFilter(genreTextField, genreErrorLabel);
        addCharacterOnlyFilter(descriptionTextField, descriptionErrorLabel);

       //  Disable manual input
        makeTextFieldReadOnly(cheminFicherTextField);
        makeTextFieldReadOnly(photoTextField);
    }

    private void addCharacterOnlyFilter(TextField textField, Label errorLabel) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            char c = event.getCharacter().charAt(0);
            if (Character.isDigit(c)) {
                event.consume(); // block digit
                errorLabel.setText("Caractère requis.");
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
                errorLabel.setText("All good.");
            }
        });
    }

    private void makeTextFieldReadOnly(TextField textField) {
        textField.setEditable(false);
        textField.setFocusTraversable(false);
    }

    @FXML
    private void UpdateMusicButton() {
        boolean isValid = true;
        String cheminFichierPath = null;
        String photoPath = null;

        // Clear all error messages first
        titreErrorLabel.setVisible(false);
        artistErrorLabel.setVisible(false);
        genreErrorLabel.setVisible(false);
        descriptionErrorLabel.setVisible(false);
        cheminErrorLabel.setVisible(false);
        photoErrorLabel.setVisible(false);

        // Validate required fields
        if (titreTextField.getText().isEmpty()) {
            titreErrorLabel.setText("Le titre est requis.");
            titreErrorLabel.setVisible(true);
            isValid = false;
        }

        if (artistTextField.getText().isEmpty()) {
            artistErrorLabel.setText("Le nom de l'artiste est requis.");
            artistErrorLabel.setVisible(true);
            isValid = false;
        }

        if (genreTextField.getText().isEmpty()) {
            genreErrorLabel.setText("Le genre est requis.");
            genreErrorLabel.setVisible(true);
            isValid = false;
        }

        if (descriptionTextField.getText().isEmpty()) {
            descriptionErrorLabel.setText("La description est requise.");
            descriptionErrorLabel.setVisible(true);
            isValid = false;
        }

        if (cheminFicherTextField.getText().isEmpty()) {
            cheminErrorLabel.setText("Le chemin du fichier est requis.");
            cheminErrorLabel.setVisible(true);
            isValid = false;
        }

        if (photoTextField.getText().isEmpty()) {
            photoErrorLabel.setText("La photo est requise.");
            photoErrorLabel.setVisible(true);
            isValid = false;
        }

        // === Handle audio file path (cheminFichier) ===
        String originalChemin = cheminFicherTextField.getText();
        if (!originalChemin.isEmpty()) {
            File sourceAudioFile = new File(originalChemin);
            if (sourceAudioFile.exists()) {
                try {
                    // Create "uploads" directory if it doesn't exist
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    // Create destination audio file and copy
                    File destinationAudio = new File(destinationDir, sourceAudioFile.getName());
                    Files.copy(sourceAudioFile.toPath(), destinationAudio.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    cheminFichierPath = destinationAudio.getAbsolutePath();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la copie de l'audio : " + e.getMessage());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Audio manquant", "Veuillez sélectionner un fichier audio valide via le bouton 'Parcourir'.");
                return;
            }
        }

        // === Handle photo path ===
        String originalPhoto = photoTextField.getText();
        if (!originalPhoto.isEmpty()) {
            File sourcePhotoFile = new File(originalPhoto);
            if (sourcePhotoFile.exists()) {
                try {
                    // Create "uploads" directory if it doesn't exist
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    // Create destination photo file and copy
                    File destinationPhoto = new File(destinationDir, sourcePhotoFile.getName());
                    Files.copy(sourcePhotoFile.toPath(), destinationPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    photoPath = destinationPhoto.getAbsolutePath();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la copie de la photo : " + e.getMessage());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Image manquante", "Veuillez sélectionner une photo valide via le bouton 'Parcourir'.");
                return;
            }
        }

        // If validation passed, update the music details
        if (isValid) {
            System.out.println("Mise à jour de la musique effectuée.");

            currentMusic.setTitre(titreTextField.getText());
            currentMusic.setArtistName(artistTextField.getText());
            currentMusic.setGenre(genreTextField.getText());
            currentMusic.setDescription(descriptionTextField.getText());

            // Only update paths if they are not null
            if (cheminFichierPath != null) {
                currentMusic.setCheminFichier(cheminFichierPath);
            }

            if (photoPath != null) {
                currentMusic.setPhoto(photoPath);
            }

            // Update the music record in the database
            try {
                // Update the music object with changes
                new MusicService().update(currentMusic);

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Musique mise à jour avec succès !");

                // Go back to AfficherMusic.fxml after updating
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherMusic.fxml"));
                AnchorPane afficherMusicPane = loader.load();

                // Important: find the rightPane from the global scene
                AnchorPane rightPane = (AnchorPane) titreTextField.getScene().lookup("#rightPane");

                rightPane.getChildren().clear();
                rightPane.getChildren().add(afficherMusicPane);

                AnchorPane.setTopAnchor(afficherMusicPane, 0.0);
                AnchorPane.setBottomAnchor(afficherMusicPane, 0.0);
                AnchorPane.setLeftAnchor(afficherMusicPane, 0.0);
                AnchorPane.setRightAnchor(afficherMusicPane, 0.0);

            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
            }
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}



