package Controllers;

import Models.Event;
import Models.Sponsor;
import Services.EventService;
import Services.SponsorService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class AjouterEvent {

    @FXML
    private TextField EventTitre;

    @FXML
    private TextField EventLocalisation;

    @FXML
    private DatePicker EventDate;

    @FXML
    private TextField EventHeure;

    @FXML
    private TextField EventNbParticipants;

    @FXML
    private TextField EventImage;

    @FXML
    private ComboBox<Sponsor> sponsorComboBox;



    @FXML private Label errorTitre;
    @FXML private Label errorLocalisation;
    @FXML private Label errorDate;
    @FXML private Label errorHeure;
    @FXML private Label errorNbParticipants;
    @FXML private Label errorSponsor;

    private File selectedFile;
    private final EventService eventService = new EventService();
    private final SponsorService sponsorService = new SponsorService();

    public AjouterEvent() throws SQLException {
    }

    @FXML
    public void initialize() {
        try {
            sponsorComboBox.getItems().addAll(sponsorService.getAll());

            sponsorComboBox.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Sponsor sponsor) {
                    return (sponsor != null) ? sponsor.getNom() : "";
                }

                @Override
                public Sponsor fromString(String string) {
                    return null;
                }
            });

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des sponsors : " + e.getMessage());
        }
    }
/
    @FXML
    public void ajouterEvent() {
        // Réinitialiser les messages d'erreur
        errorTitre.setText("");
        errorLocalisation.setText("");
        errorDate.setText("");
        errorHeure.setText("");
        errorNbParticipants.setText("");
        errorSponsor.setText("");

        try {
            String titre = EventTitre.getText();
            String localisation = EventLocalisation.getText();
            LocalDate date = EventDate.getValue();
            String heureStr = EventHeure.getText();
            String nbParticipantStr = EventNbParticipants.getText();
            String image = EventImage.getText();

            boolean hasError = false;

            if (titre == null || titre.isBlank()) {
                errorTitre.setText("Le titre est obligatoire !");
                hasError = true;
            }

            if (localisation == null || localisation.isBlank()) {
                errorLocalisation.setText("La localisation est obligatoire !");
                hasError = true;
            }

            if (date == null) {
                errorDate.setText("La date est obligatoire !");
                hasError = true;
            } else if (date.isBefore(LocalDate.now())) {
                errorDate.setText("La date ne peut pas être dans le passé !");
                hasError = true;
            }

            LocalTime heure = null;
            try {
                heure = LocalTime.parse(heureStr);
            } catch (Exception e) {
                errorHeure.setText("Heure invalide (format : HH:mm:ss) !");
                hasError = true;
            }

            int nbParticipant = 0;
            try {
                nbParticipant = Integer.parseInt(nbParticipantStr);
                if (nbParticipant <= 0) {
                    errorNbParticipants.setText("Le nombre doit être supérieur à zéro !");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                errorNbParticipants.setText("Le nombre de participants doit être un entier !");
                hasError = true;
            }

            Sponsor selectedSponsor = sponsorComboBox.getValue();
            if (selectedSponsor == null) {
                errorSponsor.setText("Veuillez sélectionner un sponsor !");
                hasError = true;
            }

            if (hasError) return;

            if (selectedFile != null && selectedFile.exists()) {
                try {
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    File destination = new File(destinationDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    EventImage.setText(destination.getAbsolutePath());

                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'upload de l'image : " + e.getMessage());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Image manquante", "Veuillez sélectionner une image via le bouton 'Parcourir'.");
                return;
            }

            Event event = new Event();
            event.setTitre(titre);
            event.setLocalisation(localisation);
            event.setDate(java.sql.Date.valueOf(date));
            event.setHeure(java.sql.Time.valueOf(heure));
            event.setNbParticipant(nbParticipant);
            event.setImage(image);

            eventService.add(event, selectedSponsor.getId());

            System.out.println("✅ Événement ajouté avec le sponsor : " + selectedSponsor.getNom());
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
            try {
                Parent detailsRoot = FXMLLoader.load(getClass().getResource("/DeatilsEvent.fxml"));
                Stage stage = (Stage) EventTitre.getScene().getWindow();
                Scene detailsScene = new Scene(detailsRoot);
                stage.setScene(detailsScene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des détails : " + e.getMessage());
            }

        } catch (SQLException e) {
            errorTitre.setText("Erreur SQL : " + e.getMessage());
        }
    }

    @FXML
    void handleParcourir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            EventImage.setText(selectedFile.getName());  // Ici ton TextField
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }




}
