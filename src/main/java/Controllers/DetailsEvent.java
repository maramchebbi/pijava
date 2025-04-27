package Controllers;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.Arrays;
import javafx.animation.ScaleTransition;
import Models.User;
import Utils.SessionManager;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import java.math.BigDecimal;

public class DetailsEvent {
    private int currentPage = 0;
    private final int eventsPerPage = Integer.MAX_VALUE; // Changé à Integer.MAX_VALUE pour afficher tous les événements
    private List<Event> allEvents;
    private boolean isGridView = true; // Mode grille par défaut

    // Ajoutez cette constante
    private static final int EVENTS_PER_ROW = 4; // Nombre d'événements par ligne

    @FXML
    private FlowPane eventFlowPane;

    @FXML
    private HBox pageButtonsContainer;

    @FXML
    private Label resultsInfoLabel;

    @FXML
    private StackPane loadingIndicator;

    @FXML
    private Label totalEventsLabel;

    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        // Réinitialiser la pagination
        currentPage = 0;

        // Charger tous les événements
        try {
            // Afficher l'indicateur de chargement si disponible
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(true);
            }

            // Charger les événements
            allEvents = eventService.getAll();

            // Charger la première page
            loadPagedEvents();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements: " + e.getMessage());
        } finally {
            // Cacher l'indicateur de chargement
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(false);
            }
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent ajouterRoot = FXMLLoader.load(getClass().getResource("/AjouterEvent.fxml"));
            Stage stage = (Stage) eventFlowPane.getScene().getWindow();
            stage.setScene(new Scene(ajouterRoot));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshEvents() {
        currentPage = 0;
        allEvents = null; // Force le rechargement
        loadPagedEvents();
    }

    // Méthode publique pour charger les événements (appelée depuis d'autres contrôleurs)
    public void loadEvents() {
        currentPage = 0;
        allEvents = null; // Force le rechargement
        loadPagedEvents();
    }

    @FXML
    private void handleAjouterEvent(ActionEvent event) {
        try {
            Parent ajouterEventRoot = FXMLLoader.load(getClass().getResource("/AjouterEvent.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(ajouterEventRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Méthode de mise à jour du conteneur de boutons de pagination
     * Cette méthode génère dynamiquement les boutons de page
     */
    private void updatePagination() {
        if (pageButtonsContainer != null) {
            pageButtonsContainer.getChildren().clear();

            int totalEvents = allEvents != null ? allEvents.size() : 0;
            int totalPages = (int) Math.ceil((double) totalEvents / eventsPerPage);

            // Si pas de pages, ne rien afficher
            if (totalPages == 0) {
                return;
            }

            // Limiter le nombre de pages affichées (ex: 5 max)
            int maxVisiblePages = 5;
            int startPage = Math.max(0, Math.min(currentPage - maxVisiblePages / 2, totalPages - maxVisiblePages));
            int endPage = Math.min(startPage + maxVisiblePages, totalPages);

            // Bouton pour première page
            if (startPage > 0) {
                Button firstPage = new Button("1");
                firstPage.getStyleClass().add("page-button");
                firstPage.setOnAction(e -> {
                    currentPage = 0;
                    loadPagedEvents();
                });
                pageButtonsContainer.getChildren().add(firstPage);

                // Ajouter des points de suspension si on ne commence pas à la page 1
                if (startPage > 1) {
                    Label ellipsis = new Label("...");
                    ellipsis.getStyleClass().add("ellipsis");
                    pageButtonsContainer.getChildren().add(ellipsis);
                }
            }

            // Ajouter boutons de pages
            for (int i = startPage; i < endPage; i++) {
                final int pageIndex = i;
                Button pageButton = new Button(String.valueOf(i + 1));
                pageButton.getStyleClass().add("page-button");

                // Marquer la page courante
                if (i == currentPage) {
                    pageButton.getStyleClass().add("current-page");
                }

                pageButton.setOnAction(e -> {
                    currentPage = pageIndex;
                    loadPagedEvents();
                });

                pageButtonsContainer.getChildren().add(pageButton);
            }

            // Ajouter bouton pour dernière page si nécessaire
            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    Label ellipsis = new Label("...");
                    ellipsis.getStyleClass().add("ellipsis");
                    pageButtonsContainer.getChildren().add(ellipsis);
                }

                Button lastPage = new Button(String.valueOf(totalPages));
                lastPage.getStyleClass().add("page-button");
                lastPage.setOnAction(e -> {
                    currentPage = totalPages - 1;
                    loadPagedEvents();
                });
                pageButtonsContainer.getChildren().add(lastPage);
            }
        }
    }

    /**
     * Méthode de chargement des événements par page
     * Modifiée pour afficher tous les événements sans pagination
     */
    private void loadPagedEvents() {
        if (eventFlowPane == null) {
            return; // Protection contre les appels avant initialisation complète
        }

        eventFlowPane.getChildren().clear();

        try {
            if (allEvents == null) {
                allEvents = eventService.getAll();
            }

            // Afficher tous les événements sans pagination
            int totalEvents = allEvents.size();

            // Mettre à jour l'étiquette d'information sur les résultats
            if (resultsInfoLabel != null) {
                if (totalEvents == 0) {
                    resultsInfoLabel.setText("Aucun événement trouvé");
                } else {
                    resultsInfoLabel.setText("Affichage de tous les événements (" + totalEvents + ")");
                }
            }

            // Créer les cartes d'événements pour tous les événements
            for (Event event : allEvents) {
                StackPane eventCard = createEventCard(event);
                eventFlowPane.getChildren().add(eventCard);
            }

            // Masquer les boutons de pagination
            if (pageButtonsContainer != null) {
                pageButtonsContainer.getChildren().clear();
                pageButtonsContainer.setVisible(false);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    /**
     * Met à jour l'étiquette d'information sur les résultats affichés
     */
    private void updateResultsInfo(int startIndex, int endIndex, int total) {
        if (resultsInfoLabel != null) {
            if (total == 0) {
                resultsInfoLabel.setText("Aucun événement trouvé");
            } else {
                resultsInfoLabel.setText(String.format("Affichage de %d-%d sur %d événements",
                        startIndex + 1, endIndex, total));
            }
        }
    }

    /**
     * Crée une carte d'événement avec design minimaliste et effet de retournement
     */
    private StackPane createEventCard(Event event) {
        // Code de débogage pour vérifier les coordonnées
        System.out.println("\n=== Infos Événement ===");
        System.out.println("ID: " + event.getId());
        System.out.println("Titre: " + event.getTitre());
        System.out.println("Latitude: " + (event.getLatitude() == null ? "null" : event.getLatitude()));
        System.out.println("Longitude: " + (event.getLongitude() == null ? "null" : event.getLongitude()));
        if (event.getLatitude() != null && event.getLongitude() != null) {
            System.out.println("Latitude == 0 ? " + (event.getLatitude().compareTo(BigDecimal.ZERO) == 0));
            System.out.println("Longitude == 0 ? " + (event.getLongitude().compareTo(BigDecimal.ZERO) == 0));
        }
        System.out.println("========================");

        // Conteneur principal qui permet l'effet de retournement
        StackPane cardContainer = new StackPane();
        cardContainer.setMinWidth(200);
        cardContainer.setMinHeight(250);

        // Face avant de la carte (visible par défaut)
        VBox frontCard = new VBox(10);
        frontCard.setAlignment(Pos.CENTER);
        frontCard.setPadding(new Insets(15));
        frontCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Image de l'événement
        ImageView imageView = new ImageView();
        try {
            File file = new File("uploads/" + event.getImage());
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageView.setFitWidth(170);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setImage(null);
        }

        // Titre et information minimaliste
        Label titre = new Label(event.getTitre());
        titre.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        titre.setWrapText(true);
        titre.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER);
        Label locationIcon = new Label("📍");
        Label location = new Label(event.getLocalisation());
        location.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #555555;");

        // Ajouter le bouton de carte si les coordonnées sont disponibles
        Button mapButton = null;
        if (event.getLatitude() != null && event.getLongitude() != null
                && event.getLatitude().compareTo(BigDecimal.ZERO) != 0
                && event.getLongitude().compareTo(BigDecimal.ZERO) != 0) {
            mapButton = new Button("🗺️");
            mapButton.setStyle(
                    "-fx-background-color: #3498db;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 5;" +
                            "-fx-min-width: 30px;" +
                            "-fx-min-height: 30px;" +
                            "-fx-padding: 2;"
            );

            mapButton.setOnAction(e -> {
                e.consume(); // Empêcher l'événement de se propager
                try {
                    MapViewer mapViewer = new MapViewer(event);
                    mapViewer.showMap();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'afficher la carte: " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            // Ajouter un tooltip
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Voir sur la carte");
            javafx.scene.control.Tooltip.install(mapButton, tooltip);

            // Animation de survol du bouton carte
            final Button finalMapButton = mapButton;
            finalMapButton.setOnMouseEntered(evt -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), finalMapButton);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });

            finalMapButton.setOnMouseExited(evt -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), finalMapButton);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
        }

        // Ajouter les éléments à la boîte de localisation
        locationBox.getChildren().addAll(locationIcon, location);
        if (mapButton != null) {
            locationBox.getChildren().add(mapButton);
        }

        HBox timeBox = new HBox(5);
        timeBox.setAlignment(Pos.CENTER);
        Label timeIcon = new Label("⏰");
        Label time = new Label(event.getHeure().toString());
        time.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #555555;");
        timeBox.getChildren().addAll(timeIcon, time);


        // Bouton participer (design simplifié)
        Button participerBtn = new Button("Participer");
        participerBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 5 15;"
        );
        participerBtn.setOnAction(e -> {
            e.consume(); // Empêcher l'événement de se propager au container
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                try {
                    // Vérifier si l'événement est complet
                    int participantActuels = new Services.ParticipationService().getParticipationCountByEvent(event.getId());
                    if (participantActuels >= event.getNbParticipant()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText(null);
                        alert.setContentText("Cet événement est complet !");
                        alert.showAndWait();
                        return;
                    }

                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Confirmation de participation");
                    dialog.setHeaderText("Confirmer votre numéro de téléphone");
                    dialog.setContentText("Numéro de téléphone :");

                    dialog.showAndWait().ifPresent(input -> {
                        try {
                            int numtel = Integer.parseInt(input);
                            Services.ParticipationService participationService = new Services.ParticipationService();
                            participationService.addParticipation(user.getId(), event.getId(), user.getNom(), user.getEmail(), numtel);

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setHeaderText(null);
                            alert.setContentText("Vous êtes inscrit à cet événement !");
                            alert.showAndWait();
                        } catch (NumberFormatException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Numéro invalide. Veuillez saisir uniquement des chiffres.");
                            alert.showAndWait();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Erreur lors de la participation.");
                            alert.showAndWait();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(null);
                alert.setContentText("Vous devez être connecté pour participer !");
                alert.showAndWait();
            }
        });

        // Groupe de boutons Admin (modifier et supprimer)
        HBox adminButtons = new HBox(5);
        adminButtons.setAlignment(Pos.CENTER);

        Button modifierBtn = new Button("✏️");
        modifierBtn.setStyle(
                "-fx-background-color: #00b894;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;"
        );
        modifierBtn.setOnAction(e -> {
            e.consume(); // Empêcher l'événement de se propager au container
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
                Parent root = loader.load();
                ModifierEvent controller = loader.getController();
                controller.setEvent(event);
                controller.setDetailsEventController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button supprimerBtn = new Button("🗑️");
        supprimerBtn.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;"
        );
        supprimerBtn.setOnAction(e -> {
            e.consume(); // Empêcher l'événement de se propager au container
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    eventService.delete(event);
                    loadPagedEvents();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        adminButtons.getChildren().addAll(modifierBtn, supprimerBtn);

        // Ajouter tous les éléments à la face avant
        frontCard.getChildren().addAll(
                imageView,
                titre,
                locationBox,
                timeBox,
                participerBtn,
                adminButtons
        );

        // Face arrière de la carte (visible après retournement)
        VBox backCard = new VBox(10);
        backCard.setAlignment(Pos.CENTER);
        backCard.setPadding(new Insets(15));
        backCard.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Titre pour la face arrière
        Label detailsTitle = new Label("Détails de l'événement");
        detailsTitle.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        // Informations détaillées
        VBox detailsBox = new VBox(8);
        detailsBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 Date: " + event.getDate());
        Label heureLabel = new Label("⏰ Heure: " + event.getHeure());
        Label lieuLabel = new Label("📍 Lieu: " + event.getLocalisation());
        Label participantsLabel = new Label("👥 Places max: " + event.getNbParticipant());

        // Coordonnées géographiques si disponibles
        Button mapButtonBack = null;
        if (event.getLatitude() != null && event.getLongitude() != null
                && event.getLatitude().compareTo(BigDecimal.ZERO) != 0
                && event.getLongitude().compareTo(BigDecimal.ZERO) != 0) {
            HBox coordsBox = new HBox(10);
            Label coordsLabel = new Label("🌍 Coordonnées: " + event.getLatitude() + ", " + event.getLongitude());
            coordsLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #555555;");

            mapButtonBack = new Button("Voir sur la carte");
            mapButtonBack.setStyle(
                    "-fx-background-color: #3498db;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: normal;" +
                            "-fx-background-radius: 5;" +
                            "-fx-padding: 5 10;"
            );

            mapButtonBack.setOnAction(e -> {
                e.consume(); // Empêcher l'événement de se propager
                try {
                    MapViewer mapViewer = new MapViewer(event);
                    mapViewer.showMap();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'afficher la carte: " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            // Animation de survol du bouton carte
            final Button finalMapButtonBack = mapButtonBack;
            finalMapButtonBack.setOnMouseEntered(evt -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), finalMapButtonBack);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });

            finalMapButtonBack.setOnMouseExited(evt -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), finalMapButtonBack);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });

            coordsBox.getChildren().addAll(coordsLabel);
            detailsBox.getChildren().add(coordsBox);
        }

        // Vérifier si l'événement est complet
        Label statusLabel = new Label();
        try {
            int participantActuels = new Services.ParticipationService().getParticipationCountByEvent(event.getId());
            if (participantActuels >= event.getNbParticipant()) {
                statusLabel.setText("⛔ Statut: Complet (" + participantActuels + "/" + event.getNbParticipant() + ")");
                statusLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                statusLabel.setText("✅ Statut: Places disponibles (" + participantActuels + "/" + event.getNbParticipant() + ")");
                statusLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #2ecc71; -fx-font-weight: bold;");
            }
        } catch (Exception ex) {
            statusLabel.setText("❓ Statut: Inconnu");
            ex.printStackTrace();
        }

        // Appliquer le style à toutes les étiquettes
        for (Label label : Arrays.asList(dateLabel, heureLabel, lieuLabel, participantsLabel)) {
            label.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #555555;");
        }

        Button backButton = new Button("Retourner");
        backButton.setStyle(
                "-fx-background-color: #7f8c8d;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 5 15;"
        );

        detailsBox.getChildren().addAll(dateLabel, heureLabel, lieuLabel, participantsLabel, statusLabel);

        // Ajouter la liste des sponsors si disponible
        if (event.getSponsors() != null && !event.getSponsors().isEmpty()) {
            Label sponsorsLabel = new Label("🏆 Sponsors: " + event.getSponsors().size());
            sponsorsLabel.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #555555;");
            detailsBox.getChildren().add(sponsorsLabel);
        }

        backCard.getChildren().addAll(detailsTitle, detailsBox);

        // Ajouter le bouton de carte avant le bouton Retourner si disponible
        if (mapButtonBack != null) {
            backCard.getChildren().add(mapButtonBack);
        }

        // Ajouter le bouton retourner en dernier
        backCard.getChildren().add(backButton);

        // Au début, la face arrière est invisible
        backCard.setVisible(false);
        backCard.setOpacity(0);

        // Ajouter les deux faces au conteneur
        cardContainer.getChildren().addAll(frontCard, backCard);

        // Animation de survol
        frontCard.setOnMouseEntered(ev -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), frontCard);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();
        });

        frontCard.setOnMouseExited(ev -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), frontCard);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });

        // Effet de retournement au clic
        frontCard.setOnMouseClicked(e -> {
            // Vérifier que le clic n'est pas sur un bouton (géré par leurs propres handlers)
            if (!(e.getTarget() instanceof Button) && !(e.getPickResult().getIntersectedNode() instanceof Button)) {
                flipCard(frontCard, backCard);
            }
        });

        backButton.setOnAction(e -> {
            flipCard(backCard, frontCard);
        });

        return cardContainer;
    }

    /**
     * Méthode pour animer le retournement de carte
     */
    private void flipCard(Node frontSide, Node backSide) {
        // Duration de l'animation
        Duration duration = Duration.millis(600);

        // Animation pour faire disparaître la face actuelle
        FadeTransition fadeOut = new FadeTransition(duration.divide(2), frontSide);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            frontSide.setVisible(false);
            backSide.setVisible(true);

            // Animation pour faire apparaître l'autre face
            FadeTransition fadeIn = new FadeTransition(duration.divide(2), backSide);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        // Effet de rotation pour plus de dynamisme
        RotateTransition rotateOut = new RotateTransition(duration.divide(2), frontSide);
        rotateOut.setAxis(Rotate.Y_AXIS);
        rotateOut.setFromAngle(0);
        rotateOut.setToAngle(90);

        RotateTransition rotateIn = new RotateTransition(duration.divide(2), backSide);
        rotateIn.setAxis(Rotate.Y_AXIS);
        rotateIn.setFromAngle(-90);
        rotateIn.setToAngle(0);
        rotateIn.setDelay(duration.divide(2));

        // Lancer les animations
        fadeOut.play();
        rotateOut.play();
        rotateIn.play();
    }

    @FXML
    public void toggleGridView() {
        if (!isGridView) {
            isGridView = true;
            double cardWidth = (eventFlowPane.getWidth() - (EVENTS_PER_ROW - 1) * eventFlowPane.getHgap() - 40) / EVENTS_PER_ROW;
            cardWidth = Math.max(cardWidth, 180); // Largeur minimale
            refreshView(cardWidth);
        }
    }

    @FXML
    public void toggleListView() {
        if (isGridView) {
            isGridView = false;
            double cardWidth = eventFlowPane.getWidth() - 40; // Largeur complète pour la vue liste
            refreshView(cardWidth);
        }
    }

    private void refreshView(double cardWidth) {
        try {
            List<Event> events = eventService.getAll();
            displayEvents(events, cardWidth);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Méthode manquante ajoutée
    private void displayEvents(List<Event> events, double cardWidth) {
        // Vider le FlowPane
        eventFlowPane.getChildren().clear();

        if (events == null || events.isEmpty()) {
            // Aucun événement à afficher
            Label noEventsLabel = new Label("Aucun événement trouvé");
            noEventsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7F8C8D;");
            eventFlowPane.getChildren().add(noEventsLabel);
            totalEventsLabel.setText("Total: 0 événements");
            return;
        }

        // Mettre à jour le nombre total d'événements
        if (totalEventsLabel != null) {
            totalEventsLabel.setText("Total: " + events.size() + " événements");
        }

        // Créer et ajouter les cartes d'événements
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);

            if (isGridView) {
                // Mode grille - utiliser le design des cartes avec effet de retournement
                StackPane eventCard = createEventCard(event);

                // Ajuster la largeur si nécessaire
                eventCard.setPrefWidth(cardWidth);
                eventCard.setMaxWidth(cardWidth);

                eventFlowPane.getChildren().add(eventCard);
            } else {
                // Mode liste - créer une version horizontale des cartes
                HBox listItem = createEventListItem(event, cardWidth);
                eventFlowPane.getChildren().add(listItem);
            }
        }
    }

    // Méthode supplémentaire pour créer un élément de liste (vue horizontale)
    private HBox createEventListItem(Event event, double width) {
        HBox listItem = new HBox(15);
        listItem.setPadding(new Insets(10));
        listItem.setPrefWidth(width);
        listItem.setMaxWidth(width);
        listItem.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
        );

        // Image à gauche
        ImageView imageView = new ImageView();
        try {
            File file = new File("uploads/" + event.getImage());
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            imageView.setImage(null);
        }

        // Informations au centre
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titre = new Label(event.getTitre());
        titre.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");

        HBox detailsBox = new HBox(10);

        // Créer la box pour localisation avec bouton de carte
        HBox locationContainer = new HBox(5);
        Label localisation = new Label("📍 " + event.getLocalisation());
        localisation.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #2d3436;");
        locationContainer.getChildren().add(localisation);

        // Ajouter bouton de carte si coordonnées disponibles
        if (event.getLatitude() != null && event.getLongitude() != null
                && event.getLatitude().compareTo(BigDecimal.ZERO) != 0
                && event.getLongitude().compareTo(BigDecimal.ZERO) != 0) {
            Button mapButton = new Button("🗺️");
            mapButton.setStyle(
                    "-fx-background-color: #3498db;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 5;" +
                            "-fx-min-width: 25px;" +
                            "-fx-min-height: 25px;" +
                            "-fx-padding: 2;"
            );

            mapButton.setOnAction(e -> {
                e.consume(); // Empêcher l'événement de se propager
                try {
                    MapViewer mapViewer = new MapViewer(event);
                    mapViewer.showMap();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'afficher la carte: " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            // Ajouter un tooltip
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip("Voir sur la carte");
            javafx.scene.control.Tooltip.install(mapButton, tooltip);

            // Animation de survol du bouton carte
            final Button finalMapButton = mapButton;
            finalMapButton.setOnMouseEntered(evt -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), finalMapButton);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });

            finalMapButton.setOnMouseExited(evt -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), finalMapButton);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });

            locationContainer.getChildren().add(mapButton);
        }

        Label time = new Label("⏰ " + event.getHeure());
        time.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 12px; -fx-text-fill: #2d3436;");

        detailsBox.getChildren().addAll(locationContainer, time);
        infoBox.getChildren().addAll(titre, detailsBox);

        // Boutons à droite
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(0, 0, 0, 10));

        // Groupe pour modifier/supprimer
        VBox adminBox = new VBox(5);
        adminBox.setAlignment(Pos.CENTER);

        Button modifierBtn = new Button("✏️");
        modifierBtn.setStyle(
                "-fx-background-color: #00b894;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;"
        );
        modifierBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
                Parent root = loader.load();
                ModifierEvent controller = loader.getController();
                controller.setEvent(event);
                controller.setDetailsEventController(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button supprimerBtn = new Button("🗑️");
        supprimerBtn.setStyle(
                "-fx-background-color: #e74c3c;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 5;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;"
        );
        supprimerBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                try {
                    eventService.delete(event);
                    loadPagedEvents();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        adminBox.getChildren().addAll(modifierBtn, supprimerBtn);

        Button participerBtn = new Button("Participer");
        participerBtn.setStyle(
                "-fx-background-color: #3498db;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: normal;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 5 15;"
        );
        participerBtn.setOnAction(e -> {
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                try {
                    // Vérifier si l'événement est complet
                    int participantActuels = new Services.ParticipationService().getParticipationCountByEvent(event.getId());
                    if (participantActuels >= event.getNbParticipant()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setHeaderText(null);
                        alert.setContentText("Cet événement est complet !");
                        alert.showAndWait();
                        return;
                    }

                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Confirmation de participation");
                    dialog.setHeaderText("Confirmer votre numéro de téléphone");
                    dialog.setContentText("Numéro de téléphone :");

                    dialog.showAndWait().ifPresent(input -> {
                        try {
                            int numtel = Integer.parseInt(input);
                            Services.ParticipationService participationService = new Services.ParticipationService();
                            participationService.addParticipation(user.getId(), event.getId(), user.getNom(), user.getEmail(), numtel);

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setHeaderText(null);
                            alert.setContentText("Vous êtes inscrit à cet événement !");
                            alert.showAndWait();
                        } catch (NumberFormatException ex) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Numéro invalide. Veuillez saisir uniquement des chiffres.");
                            alert.showAndWait();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setHeaderText(null);
                            alert.setContentText("Erreur lors de la participation.");
                            alert.showAndWait();
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(null);
                alert.setContentText("Vous devez être connecté pour participer !");
                alert.showAndWait();
            }
        });

        actionsBox.getChildren().addAll(adminBox, participerBtn);

        // Assembler tous les éléments
        listItem.getChildren().addAll(imageView, infoBox, actionsBox);

        // Animations de survol
        listItem.setOnMouseEntered(ev -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), listItem);
            scaleUp.setToX(1.02);
            scaleUp.setToY(1.02);
            scaleUp.play();
        });
        listItem.setOnMouseExited(ev -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), listItem);
            scaleDown.setToX(1);
            scaleDown.setToY(1);
            scaleDown.play();
        });

        return listItem;
    }
}