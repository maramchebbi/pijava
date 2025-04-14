package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Date;


public class DetailMusicController {

    @FXML
    private TextField artistNameTextField;

    @FXML
    private TextField cheminFicherTextField;

    @FXML
    private TextField descriptionTextField;

    @FXML
    private TextField dateTextField;

    @FXML
    private TextField genreTextField;

    @FXML
    private TextField photoTextField;

    @FXML
    private TextField titreTextField;

    public TextField getDateTextField() {
        return dateTextField;
    }

//    public void setDateTextField(TextField dateTextField) {
//        this.dateTextField = dateTextField;
//    }

    public void setDateTextField(String date) {
        this.dateTextField.setText(date);
    }

        public TextField getArtistNameTextField() {
        return artistNameTextField;
    }

    public void setArtistNameTextField(String artistName) {
        this.artistNameTextField.setText(artistName);
    }

    public TextField getCheminFicherTextField() {
        return cheminFicherTextField;
    }

    public void setCheminFicherTextField(String cheminFichier) {
        this.cheminFicherTextField.setText(cheminFichier);
    }



    public TextField getDescriptionTextField() {
        return descriptionTextField;
    }

    public void setDescriptionTextField(String description) {
        this.descriptionTextField.setText(description);
    }

    public TextField getGenreTextField() {
        return genreTextField;
    }

    public void setGenreTextField(String genre) {
        this.genreTextField.setText(genre);
    }

    public TextField getPhotoTextField() {
        return photoTextField;
    }

    public void setPhotoTextField(String photo) {
        this.photoTextField.setText(photo);
    }

    public TextField getTitreTextField() {
        return titreTextField;
    }

    public void setTitreTextField(String titre) {
        this.titreTextField.setText(titre);
    }

}
