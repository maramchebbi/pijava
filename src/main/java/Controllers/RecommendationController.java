package Controllers;

import Models.Event;
import Models.RecommendedEvent;
import Services.EventService;
import Services.RecommendationService;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/**
 * Contrôleur pour la vue des recommandations
 */
public class RecommendationController implements Initializable {

    @FXML
    private TextField userIdField;

    @FXML
    private Button getRecommendationsButton;

    @FXML
    private FlowPane recommendationsCardsPane;

    @FXML
    private ProgressBar loadingBar;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox detailsPanel;

    @FXML
    private Label selectedEventTitle;

    @FXML
    private Label selectedEventScore;

    private final RecommendationService recommendationService;
    private final EventService eventService;
    private final ObservableList<RecommendedEvent> recommendations;
    private RecommendedEvent selectedEvent;

    public RecommendationController() {
        this.recommendationService = new RecommendationService();
        this.eventService = new EventService();
        this.recommendations = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser la barre de chargement comme invisible
        loadingBar.setVisible(false);

        // Configurer le panneau de détails
        detailsPanel.setVisible(false);
    }

    /**
     * Affiche un popup avec les détails de l'événement
     */
    private void showEventDetailsPopup(RecommendedEvent recommendedEvent) {
        try {
            // Récupérer les détails complets de l'événement
            Event event = eventService.findById(recommendedEvent.getId());

            if (event != null) {
                // Créer et afficher le popup
                EventDetailPopup popup = new EventDetailPopup(event);
                popup.show();
            } else {
                showError("Impossible de trouver les détails de l'événement.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des détails: " + e.getMessage());
        }
    }

    @FXML
    private void handleGetRecommendations(ActionEvent event) {
        try {
            int userId = Integer.parseInt(userIdField.getText().trim());
            loadRecommendations(userId);
        } catch (NumberFormatException e) {
            showError("Veuillez entrer un ID utilisateur valide.");
        }
    }

    @FXML
    public void showSelectedEventDetails() {
        if (selectedEvent != null) {
            showEventDetailsPopup(selectedEvent);
        } else {
            showError("Veuillez sélectionner un événement dans la liste.");
        }
    }

    private void loadRecommendations(int userId) {
        // Réinitialiser l'interface
        recommendations.clear();
        recommendationsCardsPane.getChildren().clear();
        detailsPanel.setVisible(false);
        selectedEvent = null;

        // Afficher la barre de chargement
        loadingBar.setVisible(true);
        statusLabel.setText("Chargement des recommandations...");

        // Désactiver le bouton pendant le chargement
        getRecommendationsButton.setDisable(true);

        // Charger les recommandations de manière asynchrone
        recommendationService.getRecommendationsAsync(userId)
                .thenAccept(result -> Platform.runLater(() -> {
                    // Mettre à jour l'interface
                    loadingBar.setVisible(false);
                    getRecommendationsButton.setDisable(false);

                    if (result.isEmpty()) {
                        statusLabel.setText("Aucune recommandation trouvée pour cet utilisateur.");
                    } else {
                        recommendations.addAll(result);
                        updateRecommendationCards(result);
                        statusLabel.setText("Recommandations chargées avec succès. Double-cliquez sur un événement pour voir les détails.");
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadingBar.setVisible(false);
                        getRecommendationsButton.setDisable(false);
                        statusLabel.setText("Erreur lors du chargement des recommandations.");
                        showError("Impossible de récupérer les recommandations: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Met à jour l'affichage des recommandations sous forme de cartes
     */
    private void updateRecommendationCards(List<RecommendedEvent> events) {
        recommendationsCardsPane.getChildren().clear();

        for (RecommendedEvent event : events) {
            VBox card = createEventCard(event);
            recommendationsCardsPane.getChildren().add(card);
        }
    }

    /**
     * Crée une carte d'événement
     */
    private VBox createEventCard(RecommendedEvent event) {
        // Création de la carte
        VBox card = new VBox();
        card.getStyleClass().add("event-card");
        card.setSpacing(10);
        card.setPadding(new Insets(15));

        // Partie supérieure avec ID
        HBox topBox = new HBox();
        topBox.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("ID: " + event.getId());
        idLabel.getStyleClass().add("card-id");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBox.getChildren().addAll(idLabel, spacer);

        // Titre
        Label titleLabel = new Label(event.getTitre());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        // Score de pertinence
        HBox scoreBox = new HBox();
        scoreBox.setSpacing(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);

        // Convertir le score en pourcentage (en supposant que le score est entre 0 et 1)
        double scorePercentage = event.getSimilarityScore() * 100;

        Label scoreLabel = new Label(String.format("%.1f%%", scorePercentage));
        scoreLabel.getStyleClass().add("card-score");

        ProgressBar scoreBar = new ProgressBar(event.getSimilarityScore());
        scoreBar.getStyleClass().add("score-bar");
        scoreBar.setPrefWidth(150);

        scoreBox.getChildren().addAll(scoreLabel, scoreBar);

        // Ajout des composants à la carte
        card.getChildren().addAll(topBox, titleLabel, scoreBox);

        // Gestion des événements
        card.setOnMouseClicked(e -> {
            selectEvent(event);
            if (e.getClickCount() == 2) {
                showSelectedEventDetails();
            }
        });

        return card;
    }

    /**
     * Sélectionne un événement et met à jour le panneau de détails
     */
    private void selectEvent(RecommendedEvent event) {
        this.selectedEvent = event;
        detailsPanel.setVisible(true);
        selectedEventTitle.setText(event.getTitre());
        selectedEventScore.setText(event.getFormattedScore());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}