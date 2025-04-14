package Controllers;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;

public class ModifierEvent {

    @FXML private TextField titreField;
    @FXML private TextField localisationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField;
    @FXML private TextField nbParticipantsField;
    @FXML private ImageView imageView;

    private Event event;
    private File selectedFile;
    private DetailsEvent detailsEventController;

    private final EventService eventService = new EventService();


    public void setEvent(Event event) {
        this.event = event;
        titreField.setText(event.getTitre());
        localisationField.setText(event.getLocalisation());
        datePicker.setValue(event.getDate().toLocalDate());
        heureField.setText(event.getHeure().toString());
        nbParticipantsField.setText(String.valueOf(event.getNbParticipant()));

        File file = new File("uploads/" + event.getImage());
        if(file.exists()) {
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void choisirImage() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void modifierEvent() {
        event.setTitre(titreField.getText());
        event.setLocalisation(localisationField.getText());
        event.setDate(java.sql.Date.valueOf(datePicker.getValue()));
        event.setHeure(java.sql.Time.valueOf(heureField.getText()));
        event.setNbParticipant(Integer.parseInt(nbParticipantsField.getText()));

        if (selectedFile != null) {
            event.setImage(selectedFile.getName());
        }

        try {
            eventService.update(event);
            if (detailsEventController != null) {
                detailsEventController.loadEvents();
            }
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setDetailsEventController(DetailsEvent controller) {
        this.detailsEventController = controller;
    }

}
