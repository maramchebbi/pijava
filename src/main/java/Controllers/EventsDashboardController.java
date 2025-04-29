package Controllers;

import Models.Event;
import Models.Participation;
import Services.EventService;
import Services.ParticipationService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import javafx.scene.Parent;

public class EventsDashboardController {

    @FXML
    private TableView<Event> eventsTable;

    @FXML
    private TextField searchField;

    @FXML
    private DatePicker dateFilterPicker;

    @FXML
    private ComboBox<String> locationFilterComboBox;



    @FXML
    private TableColumn<Event, Integer> idColumn;

    @FXML
    private TableColumn<Event, String> thumbnailColumn;

    @FXML
    private TableColumn<Event, String> titleColumn;

    @FXML
    private TableColumn<Event, String> locationColumn;

    @FXML
    private TableColumn<Event, Date> dateColumn;

    @FXML
    private TableColumn<Event, Time> timeColumn;

    @FXML
    private TableColumn<Event, Integer> participantsColumn;

    @FXML
    private TableColumn<Event, Event> actionColumn;

    @FXML
    private Label totalEventsLabel;

    @FXML
    private Pagination eventsPagination;

    @FXML
    private VBox editPanelVBox;

    @FXML
    private Label editPanelTitle;

    @FXML
    private TextField editTitleField;

    @FXML
    private DatePicker editDatePicker;

    @FXML
    private TextField editTimeField;

    @FXML
    private TextField editLocationField;

    @FXML
    private TextField editNbParticipantField;

    @FXML
    private TextField editLatitudeField;

    @FXML
    private TextField editLongitudeField;

    @FXML
    private TextField editImagePathField;

    @FXML
    private ImageView editImagePreview;

    @FXML
    private TableView<Participation> participantsTable;

    @FXML
    private TableColumn<Participation, String> participantNameColumn;

    @FXML
    private TableColumn<Participation, String> participantEmailColumn;

    @FXML
    private VBox reminderNotificationVBox;

    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();

    private ObservableList<Event> masterData = FXCollections.observableArrayList();
    private FilteredList<Event> filteredData;

    private Event currentEditEvent;
    private String originalImagePath;
    private final int ITEMS_PER_PAGE = 10;

    // Dossier où sont stockées les images d'événements
    private final String UPLOAD_DIR = "uploads/";


    @FXML
    public void initialize() {
        // Configurer la taille du TableView pour s'adapter correctement
        eventsTable.setMaxHeight(Double.MAX_VALUE);
        eventsTable.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Platform.runLater(() -> {
            // Initialiser les colonnes de la table
            initializeEventTable();

            // Charger les données
            loadEvents();

            // Initialiser les filtres
            initializeFilters();

            // Initialiser la pagination
            initializePagination();

            // Initialiser la table des participants
            initializeParticipantsTable();

            // Configurer les datepickers
            configureDatePickers();

            // Rafraichir le tableau pour s'assurer que le rendu est correct
            eventsTable.refresh();

            // Forcer un recalcul de la mise en page
            eventsTable.requestLayout();
        });
    }

    private void initializeEventTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Thumbnail column with images
        thumbnailColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue();
            return new SimpleStringProperty(event.getImage());
        });
        thumbnailColumn.setCellFactory(col -> new TableCell<Event, String>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(60);
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(true);
                setGraphic(imageView);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);

                if (empty || imagePath == null) {
                    imageView.setImage(null);
                } else {
                    try {
                        File file = new File(UPLOAD_DIR + imagePath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString());
                            imageView.setImage(image);
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                }
            }
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("heure"));

        // Participants count column
        participantsColumn.setCellValueFactory(cellData -> {
            Event event = cellData.getValue();
            try {
                int count = participationService.getParticipationCountByEvent(event.getId());
                return new SimpleIntegerProperty(count).asObject();
            } catch (Exception e) {
                return new SimpleIntegerProperty(0).asObject();
            }
        });

        // Status column with colored indicators
        participantsColumn.setCellFactory(col -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);

                if (empty || count == null) {
                    setText("");
                    setStyle("");
                } else {
                    Event event = getTableView().getItems().get(getIndex());
                    int maxParticipants = event.getNbParticipant();

                    setText(count + " / " + maxParticipants);

                    if (count >= maxParticipants) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #d32f2f;");
                    } else if ((double) count / maxParticipants > 0.75) {
                        setStyle("-fx-background-color: #fff9c4; -fx-text-fill: #ff8f00;");
                    } else {
                        setStyle("-fx-background-color: #dcedc8; -fx-text-fill: #388e3c;");
                    }
                }
            }
        });

        // Actions column with edit and delete buttons
        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionColumn.setCellFactory(param -> new TableCell<Event, Event>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            private final Button viewBtn = new Button("👁️ Voir");

            {
                editBtn.getStyleClass().add("admin-edit-btn");
                deleteBtn.getStyleClass().add("admin-delete-btn");
                viewBtn.getStyleClass().add("admin-view-btn");

                // Layout
                HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);
                hbox.setAlignment(Pos.CENTER);
                setGraphic(hbox);

                // Event handlers
                editBtn.setOnAction(event -> {
                    Event currentEvent = getTableView().getItems().get(getIndex());
                    showEditPanel(currentEvent);
                });

                deleteBtn.setOnAction(event -> {
                    Event currentEvent = getTableView().getItems().get(getIndex());
                    handleDeleteEvent(currentEvent);
                });

                viewBtn.setOnAction(event -> {
                    Event currentEvent = getTableView().getItems().get(getIndex());
                    handleViewEvent(currentEvent);
                });
            }

            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
    }

    private void loadEvents() {
        try {
            List<Event> events = eventService.getAll();
            masterData.clear();
            masterData.addAll(events);

            filteredData = new FilteredList<>(masterData, p -> true);

            totalEventsLabel.setText(String.valueOf(masterData.size()));

            // Extraire toutes les localisations pour le filtre
            Set<String> locations = new HashSet<>();
            for (Event event : masterData) {
                locations.add(event.getLocalisation());
            }

            ObservableList<String> locationsList = FXCollections.observableArrayList(locations);
            FXCollections.sort(locationsList);
            locationsList.add(0, "Toutes les localisations");
            locationFilterComboBox.setItems(locationsList);
            locationFilterComboBox.getSelectionModel().selectFirst();

            // S'assurer que le tableau est explicitement lié aux données
            eventsTable.setItems(filteredData);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void initializeFilters() {
        // Filtre de recherche textuelle
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Filtre de date
        dateFilterPicker.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // Filtre de localisation
        locationFilterComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void applyFilters() {
        filteredData.setPredicate(event -> {
            boolean matchesSearch = true;
            boolean matchesDate = true;
            boolean matchesLocation = true;

            // Filtre de recherche
            if (searchField.getText() != null && !searchField.getText().isEmpty()) {
                String searchText = searchField.getText().toLowerCase();
                matchesSearch = event.getTitre().toLowerCase().contains(searchText) ||
                        event.getLocalisation().toLowerCase().contains(searchText);
            }

            // Filtre de date
            if (dateFilterPicker.getValue() != null) {
                LocalDate filterDate = dateFilterPicker.getValue();
                LocalDate eventDate = event.getDate().toLocalDate();
                matchesDate = eventDate.equals(filterDate);
            }

            // Filtre de localisation
            String selectedLocation = locationFilterComboBox.getSelectionModel().getSelectedItem();
            if (selectedLocation != null && !selectedLocation.equals("Toutes les localisations")) {
                matchesLocation = event.getLocalisation().equals(selectedLocation);
            }

            return matchesSearch && matchesDate && matchesLocation;
        });

        updatePagination();
    }

    private void updatePagination() {
        int totalItems = filteredData.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

        eventsPagination.setPageCount(Math.max(1, pageCount));

        if (eventsPagination.getCurrentPageIndex() >= pageCount) {
            eventsPagination.setCurrentPageIndex(0);
        }

        updatePage(eventsPagination.getCurrentPageIndex());
    }

    private void initializePagination() {
        eventsPagination.setPageFactory(this::createPage);
    }

    private TableView<Event> createPage(int pageIndex) {
        updatePage(pageIndex);
        return eventsTable;
    }

    private void updatePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, filteredData.size());

        ObservableList<Event> pageItems;

        if (fromIndex > toIndex) {
            pageItems = FXCollections.observableArrayList();
        } else {
            pageItems = FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex));
        }

        eventsTable.setItems(pageItems);
    }

    private void initializeParticipantsTable() {
        participantNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomUtilisateur()));
        participantEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmailUtilisateur()));
    }

    private void configureDatePickers() {
        // Format pour les dates (jj/mm/aaaa)
        StringConverter<LocalDate> converter = new StringConverter<>() {
            final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        dateFilterPicker.setConverter(converter);
        editDatePicker.setConverter(converter);
    }

    @FXML
    private void handleAddEvent() {
        clearEditPanel();
        editPanelTitle.setText("Ajouter un nouvel événement");
        currentEditEvent = null;
        showEditPanel(null);
    }

    private void showEditPanel(Event event) {
        currentEditEvent = event;

        if (event != null) {
            // Mode édition
            editPanelTitle.setText("Modifier l'événement #" + event.getId());

            editTitleField.setText(event.getTitre());
            editLocationField.setText(event.getLocalisation());

            // Date
            if (event.getDate() != null) {
                editDatePicker.setValue(event.getDate().toLocalDate());
            }

            // Heure
            if (event.getHeure() != null) {
                editTimeField.setText(new SimpleDateFormat("HH:mm").format(event.getHeure()));
            }

            editNbParticipantField.setText(String.valueOf(event.getNbParticipant()));

            // Coordonnées
            if (event.getLatitude() != null) {
                editLatitudeField.setText(event.getLatitude().toString());
            }

            if (event.getLongitude() != null) {
                editLongitudeField.setText(event.getLongitude().toString());
            }

            // Image
            originalImagePath = event.getImage();
            editImagePathField.setText(originalImagePath);

            try {
                File imageFile = new File(UPLOAD_DIR + originalImagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    editImagePreview.setImage(image);
                } else {
                    editImagePreview.setImage(null);
                }
            } catch (Exception e) {
                editImagePreview.setImage(null);
            }

            // Charger les participants
            loadParticipants(event.getId());

        } else {
            // Mode ajout
            originalImagePath = null;
            editImagePreview.setImage(null);
            participantsTable.getItems().clear();
        }

        editPanelVBox.setVisible(true);
        editPanelVBox.setManaged(true);
    }

    private void loadParticipants(int eventId) {
        try {
            List<Participation> participants = participationService.getParticipationsByEvent(eventId);
            participantsTable.setItems(FXCollections.observableArrayList(participants));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void closeEditPanel() {
        editPanelVBox.setVisible(false);
        editPanelVBox.setManaged(false);
        currentEditEvent = null;
        clearEditPanel();
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     *
     * @param type Le type d'alerte (information, erreur, etc.)
     * @param title Le titre de l'alerte
     * @param content Le contenu du message
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearEditPanel() {
        editTitleField.clear();
        editLocationField.clear();
        editDatePicker.setValue(null);
        editTimeField.clear();
        editNbParticipantField.clear();
        editLatitudeField.clear();
        editLongitudeField.clear();
        editImagePathField.clear();
        editImagePreview.setImage(null);
        participantsTable.getItems().clear();
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(editPanelVBox.getScene().getWindow());
        if (selectedFile != null) {
            editImagePathField.setText(selectedFile.getAbsolutePath());

            try {
                Image image = new Image(selectedFile.toURI().toString());
                editImagePreview.setImage(image);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveEdits() {
        if (!validateForm()) {
            return;
        }

        try {
            Event event;

            if (currentEditEvent != null) {
                // Mode édition
                event = currentEditEvent;
            } else {
                // Mode ajout
                event = new Event();
            }

            // Mettre à jour les champs
            event.setTitre(editTitleField.getText());
            event.setLocalisation(editLocationField.getText());

            // Date
            if (editDatePicker.getValue() != null) {
                event.setDate(java.sql.Date.valueOf(editDatePicker.getValue()));
            }

            // Heure
            try {
                LocalTime time = LocalTime.parse(editTimeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                event.setHeure(Time.valueOf(time.toString()));
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'heure invalide. Utilisez le format HH:MM.");
                return;
            }

            // Nombre de participants
            try {
                event.setNbParticipant(Integer.parseInt(editNbParticipantField.getText()));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de participants doit être un nombre entier.");
                return;
            }

            // Coordonnées GPS
            if (!editLatitudeField.getText().isEmpty()) {
                try {
                    event.setLatitude(new BigDecimal(editLatitudeField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Format de latitude invalide.");
                    return;
                }
            }

            if (!editLongitudeField.getText().isEmpty()) {
                try {
                    event.setLongitude(new BigDecimal(editLongitudeField.getText()));
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Format de longitude invalide.");
                    return;
                }
            }

            // Traitement de l'image
            String imagePath = editImagePathField.getText();
            if (imagePath != null && !imagePath.isEmpty()) {
                // Si le chemin commence par le répertoire d'upload, c'est une image déjà dans le système
                if (!imagePath.startsWith(UPLOAD_DIR)) {
                    // C'est une nouvelle image, on doit la copier
                    File sourceFile = new File(imagePath);
                    if (sourceFile.exists()) {
                        // Générer un nom de fichier unique
                        String fileName = UUID.randomUUID().toString() + "_" + sourceFile.getName();
                        String targetPath = UPLOAD_DIR + fileName;

                        // Copier le fichier
                        Files.copy(sourceFile.toPath(), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);

                        // Mettre à jour le chemin de l'image
                        event.setImage(fileName);
                    }
                } else {
                    // Image déjà dans le système
                    event.setImage(originalImagePath);
                }
            }

            // Enregistrer l'événement
            if (currentEditEvent != null) {
                // Mode édition
                eventService.update(event);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement mis à jour avec succès.");
            } else {
                // Mode ajout
                eventService.add(event);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement ajouté avec succès.");
            }

            // Recharger les données et fermer le panneau d'édition
            loadEvents();
            closeEditPanel();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        if (editTitleField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le titre est obligatoire.");
            return false;
        }

        if (editLocationField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La localisation est obligatoire.");
            return false;
        }

        if (editDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La date est obligatoire.");
            return false;
        }

        if (editTimeField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "L'heure est obligatoire.");
            return false;
        }

        if (editNbParticipantField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nombre de participants est obligatoire.");
            return false;
        }

        return true;
    }

    private void handleDeleteEvent(Event event) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer cet événement ?");
        confirmAlert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                eventService.delete(event);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Événement supprimé avec succès.");
                loadEvents();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleViewEvent(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailsEvent.fxml"));
            Parent root = loader.load();

            DetailsEvent controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Détails de l'événement: " + event.getTitre());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher les détails de l'événement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleShowMap() {
        if (currentEditEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez d'abord enregistrer l'événement.");
            return;
        }

        // Vérifier si les coordonnées sont définies
        if (editLatitudeField.getText().isEmpty() || editLongitudeField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Les coordonnées GPS ne sont pas définies.");
            return;
        }

        try {
            BigDecimal latitude = new BigDecimal(editLatitudeField.getText());
            BigDecimal longitude = new BigDecimal(editLongitudeField.getText());

            // Mettre à jour les coordonnées temporairement
            Event tempEvent = new Event();
            tempEvent.setId(currentEditEvent.getId());
            tempEvent.setTitre(editTitleField.getText());
            tempEvent.setLocalisation(editLocationField.getText());
            tempEvent.setLatitude(latitude);
            tempEvent.setLongitude(longitude);

            // Afficher la carte
            MapViewer mapViewer = new MapViewer(tempEvent);
            mapViewer.showMap();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Coordonnées GPS invalides.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'afficher la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendReminder() {
        if (currentEditEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez d'abord sélectionner un événement.");
            return;
        }

        try {
            List<Participation> participants = participationService.getParticipationsByEvent(currentEditEvent.getId());

            if (participants.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Avertissement", "Aucun participant pour cet événement.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation d'envoi de rappels");
            confirmAlert.setHeaderText("Envoyer des rappels à tous les participants ?");
            confirmAlert.setContentText("Un email sera envoyé aux " + participants.size() + " participants.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Envoyer les rappels en arrière-plan
                new Thread(() -> {
                    int successCount = 0;
                    int errorCount = 0;

                    for (Participation participant : participants) {
                        try {
                            sendReminderEmail(currentEditEvent, participant);
                            successCount++;
                        } catch (Exception e) {
                            errorCount++;
                            e.printStackTrace();
                        }
                    }

                    final int finalSuccessCount = successCount;
                    final int finalErrorCount = errorCount;

                    javafx.application.Platform.runLater(() -> {
                        showReminderNotification(finalSuccessCount, finalErrorCount);
                    });
                }).start();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer les rappels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendReminderEmail(Event event, Participation participant) throws Exception {
        // Simuler l'envoi d'email (à remplacer par votre vrai implémentation)
        System.out.println("Envoi d'un rappel à " + participant.getNomUtilisateur() + " (" + participant.getEmailUtilisateur() + ") pour l'événement " + event.getTitre());

        // Si vous avez un service d'envoi d'email, appelez-le ici
        // EmailSender.sendEmail(participant.getEmailUtilisateur(), "Rappel: " + event.getTitre(), "Contenu du rappel...");
    }

    private void showReminderNotification(int successCount, int errorCount) {
        // Créer une notification
        VBox notification = new VBox(5);
        notification.getStyleClass().add("reminder-notification");
        notification.setPadding(new javafx.geometry.Insets(10));

        Label titleLabel = new Label("Résultat des envois de rappels");
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label successLabel = new Label(successCount + " rappels envoyés avec succès.");
        successLabel.setStyle("-fx-text-fill: #388e3c;");

        VBox content = new VBox(5, titleLabel, successLabel);

        if (errorCount > 0) {
            Label errorLabel = new Label(errorCount + " erreurs lors de l'envoi.");
            errorLabel.setStyle("-fx-text-fill: #d32f2f;");
            content.getChildren().add(errorLabel);
        }

        Button closeBtn = new Button("✖");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #555555;");

        HBox header = new HBox(content, closeBtn);
        javafx.scene.layout.HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_RIGHT);

        notification.getChildren().add(header);

        reminderNotificationVBox.getChildren().add(notification);
        reminderNotificationVBox.setVisible(true);
        reminderNotificationVBox.setManaged(true);

        closeBtn.setOnAction(e -> {
            reminderNotificationVBox.getChildren().remove(notification);

            if (reminderNotificationVBox.getChildren().isEmpty()) {
                reminderNotificationVBox.setVisible(false);
                reminderNotificationVBox.setManaged(false);
            }
        });
    }
}