package Controllers;

import Models.Music;
import Services.MusicService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class UpdateMusicController {

    @FXML
    private TextField titreField;

    @FXML
    private TextField artistField;

    @FXML
    private TextField genreField;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField cheminFichierField;

    @FXML
    private TextField photoField;

    @FXML private Label titreErrorLabel;
    @FXML private Label artistErrorLabel;
    @FXML private Label genreErrorLabel;
    @FXML private Label descriptionErrorLabel;
    @FXML private Label cheminErrorLabel;
    @FXML private Label photoErrorLabel;
    // Handle save music
//    @FXML
//    private void handleSaveMusic() {
//        String title = titreField.getText();
//        String artist = artistField.getText();
//        String genre = genreField.getText();
//        String description = descriptionField.getText();
//        String cheminFichier = cheminFichierField.getText();
//        String photo = photoField.getText();
//
//        // Call your service to update the music in the database
//        // Example: musicService.updateMusic(id, title, artist, genre, description);
//
//        System.out.println("Music updated: " + title + " by " + artist);
//        // After saving, you could close the window or reset the form
//    }

    private Music currentMusic;

    public void setMusic(Music music) {
        this.currentMusic = music;
        titreField.setText(music.getTitre());
        artistField.setText(music.getArtistName());
        genreField.setText(music.getGenre());
        descriptionField.setText(music.getDescription());
        cheminFichierField.setText(music.getCheminFichier());
        photoField.setText(music.getPhoto());
    }

    @FXML
    private void initialize() {
        // Only characters allowed
        addCharacterOnlyFilter(titreField, titreErrorLabel);
        addCharacterOnlyFilter(artistField, artistErrorLabel);
        addCharacterOnlyFilter(genreField, genreErrorLabel);
        addCharacterOnlyFilter(descriptionField, descriptionErrorLabel);

        // Disable manual input
        makeTextFieldReadOnly(cheminFichierField);
        makeTextFieldReadOnly(photoField);
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
            }
        });
    }

    private void makeTextFieldReadOnly(TextField textField) {
        textField.setEditable(false);
        textField.setFocusTraversable(false);
    }
        @FXML
        private void handleUpdateMusic() {

            boolean isValid = true;

            // Clear all error messages first
            titreErrorLabel.setVisible(false);
            artistErrorLabel.setVisible(false);
            genreErrorLabel.setVisible(false);
            descriptionErrorLabel.setVisible(false);
            cheminErrorLabel.setVisible(false);
            photoErrorLabel.setVisible(false);

            if (titreField.getText().isEmpty()) {
                titreErrorLabel.setText("Le titre est requis.");
                titreErrorLabel.setVisible(true);
                isValid = false;
            }

            if (artistField.getText().isEmpty()) {
                artistErrorLabel.setText("Le nom de l'artiste est requis.");
                artistErrorLabel.setVisible(true);
                isValid = false;
            }

            if (genreField.getText().isEmpty()) {
                genreErrorLabel.setText("Le genre est requis.");
                genreErrorLabel.setVisible(true);
                isValid = false;
            }

            if (descriptionField.getText().isEmpty()) {
                descriptionErrorLabel.setText("La description est requise.");
                descriptionErrorLabel.setVisible(true);
                isValid = false;
            }

            if (cheminFichierField.getText().isEmpty()) {
                cheminErrorLabel.setText("Le chemin du fichier est requis.");
                cheminErrorLabel.setVisible(true);
                isValid = false;
            }

            if (photoField.getText().isEmpty()) {
                photoErrorLabel.setText("La photo est requise.");
                photoErrorLabel.setVisible(true);
                isValid = false;
            }

            if (isValid) {
                System.out.println("Mise à jour de la musique effectuée.");
                // Proceed with update logic

            currentMusic.setTitre(titreField.getText());
            currentMusic.setArtistName(artistField.getText());
            currentMusic.setGenre(genreField.getText());
            currentMusic.setDescription(descriptionField.getText());
            currentMusic.setCheminFichier(cheminFichierField.getText());
            currentMusic.setPhoto(photoField.getText());

            try {
                new MusicService().update(currentMusic);
                ((Stage) titreField.getScene().getWindow()).close(); // Close window
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        }
    }



