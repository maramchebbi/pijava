package Controllers;

import Models.Event;
import Models.Sponsor;
import Services.EventService;
import Services.SponsorService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
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
import javafx.scene.layout.VBox;
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
import javafx.util.Duration;
import javafx.scene.control.Button;
import java.math.BigDecimal;

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
    private TextField EventLatitude;

    @FXML
    private TextField EventLongitude;

    @FXML
    private ComboBox<Sponsor> sponsorComboBox;

    @FXML
    private Button mapButton;

    @FXML
    private VBox formCard;

    @FXML private Label errorTitre;
    @FXML private Label errorLocalisation;
    @FXML private Label errorDate;
    @FXML private Label errorHeure;
    @FXML private Label errorNbParticipants;
    @FXML private Label errorSponsor;

    private File selectedFile;
    private final EventService eventService = new EventService();
    private final SponsorService sponsorService = new SponsorService();
    private MapCoordinatePicker mapPicker = new MapCoordinatePicker();

    public AjouterEvent() throws SQLException {
    }

    @FXML
    public void initialize() {
        try {

            // Ajouter des écouteurs pour détecter les changements dans les champs de coordonnées
            EventLatitude.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Champ latitude modifié: " + oldValue + " -> " + newValue);
                // Changer la couleur de fond pour indiquer que les coordonnées ont été mises à jour
                EventLatitude.setStyle("-fx-background-color: #e8f5e9;");
            });

            EventLongitude.textProperty().addListener((observable, oldValue, newValue) -> {
                System.out.println("Champ longitude modifié: " + oldValue + " -> " + newValue);
                // Changer la couleur de fond pour indiquer que les coordonnées ont été mises à jour
                EventLongitude.setStyle("-fx-background-color: #e8f5e9;");
            });
            // Charger les sponsors
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

            // Animations d'entrée
            playEntranceAnimation();

            // Ajouter des transitions sur focus pour les champs
            setupFieldAnimations();

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du chargement des sponsors : " + e.getMessage());
        }
    }

    private void playEntranceAnimation() {
        // Réinitialiser les transformations
        formCard.setOpacity(0);
        formCard.setScaleX(0.95);
        formCard.setScaleY(0.95);
        formCard.setTranslateY(20);

        // Créer les animations
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), formCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.7), formCard);
        scaleIn.setFromX(0.95);
        scaleIn.setFromY(0.95);
        scaleIn.setToX(1);
        scaleIn.setToY(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.7), formCard);
        slideIn.setFromY(20);
        slideIn.setToY(0);

        // Créer une animation parallèle
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(fadeIn, scaleIn, slideIn);

        // Lancer l'animation
        parallelTransition.play();
    }

    private void setupFieldAnimations() {
        // Ajouter des animations sur focus pour tous les champs de texte
        addFocusAnimation(EventTitre);
        addFocusAnimation(EventLocalisation);
        addFocusAnimation(EventHeure);
        addFocusAnimation(EventNbParticipants);
        addFocusAnimation(EventImage);
        addFocusAnimation(EventLatitude);
        addFocusAnimation(EventLongitude);
    }

    private void addFocusAnimation(TextField textField) {
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { // Focus obtenu
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(textField.scaleXProperty(), 1.02),
                                new KeyValue(textField.scaleYProperty(), 1.02)
                        )
                );
                timeline.play();
            } else { // Focus perdu
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(textField.scaleXProperty(), 1.0),
                                new KeyValue(textField.scaleYProperty(), 1.0)
                        )
                );
                timeline.play();
            }
        });
    }

    private void showErrorWithAnimation(Label errorLabel, String errorMessage) {
        errorLabel.setText(errorMessage);

        // Animation pour faire apparaître le message d'erreur
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), errorLabel);
        slide.setFromX(-10);
        slide.setToX(0);

        ParallelTransition transition = new ParallelTransition(fade, slide);
        transition.play();
    }

    @FXML
    public void openMapCoordinatePicker() {
        System.out.println("openMapCoordinatePicker appelé");
        System.out.println("EventLatitude null? " + (EventLatitude == null));
        System.out.println("EventLongitude null? " + (EventLongitude == null));

        // Utiliser un point central par défaut (par exemple, le centre de la Tunisie)
        String defaultLocation = "Tunisie";

        // Animation du bouton
        try {
            Button button = (Button) formCard.lookup("Button[text=\"Sélectionner sur carte\"]");
            if (button != null) {
                animateButtonClick(button);
            }
        } catch (Exception ex) {
            // Ignorer si le bouton n'est pas trouvé
        }

        // Ouvrir le sélecteur de coordonnées
        mapPicker.openMapPicker(EventLatitude, EventLongitude, defaultLocation);

        // Vérifier après l'appel
        System.out.println("Après appel à openMapPicker");
        if (EventLatitude != null && EventLongitude != null) {
            System.out.println("EventLatitude: " + EventLatitude.getText());
            System.out.println("EventLongitude: " + EventLongitude.getText());
        }
    }

    private void animateButtonClick(Button button) {
        if (button == null) return;

        // Animation d'échelle
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);
        scaleDown.play();

        scaleDown.setOnFinished(event -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            scaleUp.play();
        });
    }

    @FXML
    public void ajouterEvent() {
        // Animation du bouton
        try {
            Button button = (Button) formCard.lookup("Button[text=\"Ajouter l'événement\"]");
            if (button != null) {
                animateButtonClick(button);
            }
        } catch (Exception ex) {
            // Ignorer si le bouton n'est pas trouvé
        }

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
            String latitudeStr = EventLatitude.getText();
            String longitudeStr = EventLongitude.getText();

            System.out.println("Coordonnées à l'ajout: lat='" + latitudeStr + "', lon='" + longitudeStr + "'"); // Ajout de log

            boolean hasError = false;

            if (titre == null || titre.isBlank()) {
                showErrorWithAnimation(errorTitre, "Le titre est obligatoire !");
                hasError = true;
            }

            if (localisation == null || localisation.isBlank()) {
                showErrorWithAnimation(errorLocalisation, "La localisation est obligatoire !");
                hasError = true;
            }

            if (date == null) {
                showErrorWithAnimation(errorDate, "La date est obligatoire !");
                hasError = true;
            } else if (date.isBefore(LocalDate.now())) {
                showErrorWithAnimation(errorDate, "La date ne peut pas être dans le passé !");
                hasError = true;
            }

            LocalTime heure = null;
            try {
                heure = LocalTime.parse(heureStr);
            } catch (Exception e) {
                showErrorWithAnimation(errorHeure, "Heure invalide (format : HH:mm:ss) !");
                hasError = true;
            }

            int nbParticipant = 0;
            try {
                nbParticipant = Integer.parseInt(nbParticipantStr);
                if (nbParticipant <= 0) {
                    showErrorWithAnimation(errorNbParticipants, "Le nombre doit être supérieur à zéro !");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                showErrorWithAnimation(errorNbParticipants, "Le nombre de participants doit être un entier !");
                hasError = true;
            }

            BigDecimal latitude = null;
            BigDecimal longitude = null;
            try {
                if (latitudeStr != null && !latitudeStr.isEmpty()) {
                    // Normaliser le format (remplacer la virgule par un point si nécessaire)
                    latitudeStr = latitudeStr.replace(',', '.');
                    latitude = new BigDecimal(latitudeStr);
                    System.out.println("Latitude convertie: " + latitude); // Ajout de log
                }
                if (longitudeStr != null && !longitudeStr.isEmpty()) {
                    // Normaliser le format
                    longitudeStr = longitudeStr.replace(',', '.');
                    longitude = new BigDecimal(longitudeStr);
                    System.out.println("Longitude convertie: " + longitude); // Ajout de log
                }
            } catch (NumberFormatException e) {
                System.out.println("Erreur lors de la conversion des coordonnées: " + e.getMessage()); // Ajout de log
                showAlertWithAnimation(Alert.AlertType.WARNING, "Coordonnées invalides", "Les coordonnées GPS doivent être des nombres valides.");
                hasError = true;
            }

            Sponsor selectedSponsor = sponsorComboBox.getValue();
            if (selectedSponsor == null) {
                showErrorWithAnimation(errorSponsor, "Veuillez sélectionner un sponsor !");
                hasError = true;
            }

            if (hasError) return;

            // Vérifier si une image a été sélectionnée
            if (selectedFile != null && selectedFile.exists()) {
                try {
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    File destination = new File(destinationDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    image = destination.getAbsolutePath(); // Assignation de la valeur à image, pas à EventImage.setText()
                    System.out.println("Chemin d'image: " + image); // Ajout de log

                } catch (IOException e) {
                    showAlertWithAnimation(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'upload de l'image : " + e.getMessage());
                    return;
                }
            } else {
                showAlertWithAnimation(Alert.AlertType.WARNING, "Image manquante", "Veuillez sélectionner une image via le bouton 'Parcourir'.");
                return;
            }

            Event event = new Event();
            event.setTitre(titre);
            event.setLocalisation(localisation);
            event.setDate(java.sql.Date.valueOf(date));
            event.setHeure(java.sql.Time.valueOf(heure));
            event.setNbParticipant(nbParticipant);
            event.setImage(image); // Utilisez la variable mise à jour, pas EventImage.getText()
            event.setLatitude(latitude);
            event.setLongitude(longitude);

            System.out.println("Valeurs assignées à l'événement avant ajout:");  // Ajout de log
            System.out.println("Latitude: " + event.getLatitude());  // Ajout de log
            System.out.println("Longitude: " + event.getLongitude()); // Ajout de log

            eventService.add(event, selectedSponsor.getId());

// Utilisons ce code pour déboguer
            try {
                System.out.println("Tentative d'ajout de l'événement avec coordonnées:");
                System.out.println("Latitude: " + event.getLatitude());
                System.out.println("Longitude: " + event.getLongitude());

                // Exécuter l'appel original
                eventService.add(event, selectedSponsor.getId());

                System.out.println("Événement ajouté avec succès!");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de l'événement:");
                System.err.println("Message: " + e.getMessage());
                e.printStackTrace();

                // Afficher tout de même l'erreur à l'utilisateur
                showErrorWithAnimation(errorTitre, "Erreur SQL : " + e.getMessage());
                return; // Important: arrêter l'exécution pour ne pas naviguer vers une autre page
            }
            System.out.println("✅ Événement ajouté avec le sponsor : " + selectedSponsor.getNom());
            showSuccessAnimation();

            // Délai avant de naviguer
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.2), e -> {
                try {
                    Parent detailsRoot = FXMLLoader.load(getClass().getResource("/EventList.fxml")); // Correction du chemin
                    Stage stage = (Stage) EventTitre.getScene().getWindow();

                    // Animation de transition entre les pages
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), formCard);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event2 -> {
                        Scene detailsScene = new Scene(detailsRoot);
                        stage.setScene(detailsScene);
                        stage.show();
                    });
                    fadeOut.play();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    showAlertWithAnimation(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des détails : " + ex.getMessage());
                }
            }));
            timeline.play();

        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de l'ajout: " + e.getMessage()); // Ajout de log
            showErrorWithAnimation(errorTitre, "Erreur SQL : " + e.getMessage());
        }
    }
    private void showSuccessAnimation() {
        // Animation de confirmation de succès
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), formCard);
        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), formCard);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        scaleUp.setOnFinished(e -> scaleDown.play());
        scaleUp.play();

        // Afficher l'alerte de succès avec animation
        showAlertWithAnimation(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès !");
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

            // Animation pour confirmer la sélection
            TranslateTransition bounce = new TranslateTransition(Duration.millis(100), EventImage);
            bounce.setByY(-5);
            bounce.setCycleCount(2);
            bounce.setAutoReverse(true);
            bounce.play();
        }
    }

    private void showAlertWithAnimation(Alert.AlertType type, String title, String content) {
        // Animation avant d'afficher l'alerte
        ScaleTransition pulse = new ScaleTransition(Duration.millis(100), formCard);
        pulse.setToX(1.02);
        pulse.setToY(1.02);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);

        pulse.setOnFinished(e -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });

        pulse.play();
    }
}