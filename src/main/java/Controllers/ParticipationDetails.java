package Controllers;

import Models.Participation;
import Models.User;
import Services.ParticipationService;
import Utils.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticipationDetails implements Initializable {

    @FXML
    private FlowPane participationsFlowPane;

    private ParticipationController participationController;
    private ObservableList<Participation> participations;
    private ExecutorService executorService;
    private Map<Integer, Boolean> cancellationStatus;

    public ParticipationDetails() {
        try {
            this.participationController = new ParticipationController();
            this.participations = FXCollections.observableArrayList();
            this.executorService = Executors.newSingleThreadExecutor();
            this.cancellationStatus = new HashMap<>();
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'initialisation du contrôleur de participation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialisation du contrôleur ParticipationDetails");

        // Changer la couleur de fond du FlowPane pour le rendre visible
        participationsFlowPane.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");

        // Ajouter un indicateur visuel initial
        Label loadingLabel = new Label("Chargement des participations...");
        loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
        participationsFlowPane.getChildren().add(loadingLabel);

        // Utiliser un thread séparé pour charger les données
        executorService.submit(this::loadParticipationsInBackground);
    }

    @FXML
    public void refreshParticipations() {
        System.out.println("Rafraîchissement des participations");

        // Afficher un message de chargement
        Platform.runLater(() -> {
            participationsFlowPane.getChildren().clear();
            Label loadingLabel = new Label("Actualisation des données...");
            loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
            participationsFlowPane.getChildren().add(loadingLabel);
        });

        executorService.submit(this::loadParticipationsInBackground);
    }

    private void loadParticipationsInBackground() {
        System.out.println("Chargement des participations en arrière-plan");

        // Récupérer l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Aucun utilisateur connecté dans SessionManager");
            Platform.runLater(() -> {
                participationsFlowPane.getChildren().clear();
                Label noUserLabel = new Label("Aucun utilisateur connecté");
                noUserLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #e74c3c; -fx-font-family: 'Roboto', sans-serif;");
                participationsFlowPane.getChildren().add(noUserLabel);
            });
            return;
        }

        System.out.println("Utilisateur connecté: " + currentUser.getNom() + " (ID: " + currentUser.getId() + ")");

        // Récupérer les participations
        List<Participation> userParticipations = null;
        try {
            userParticipations = participationController.getUserParticipations(currentUser);
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des participations: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> showErrorMessage("Erreur de connexion",
                    "Impossible de récupérer vos participations",
                    "Erreur: " + e.getMessage()));
            return;
        }

        // Vérifier si des participations ont été trouvées
        if (userParticipations == null || userParticipations.isEmpty()) {
            System.out.println("Aucune participation trouvée pour l'utilisateur");
            Platform.runLater(() -> {
                participationsFlowPane.getChildren().clear();
                Label noParticipationsLabel = new Label("Vous n'avez pas encore de participations");
                noParticipationsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-family: 'Roboto', sans-serif;");
                participationsFlowPane.getChildren().add(noParticipationsLabel);
            });
            return;
        }

        System.out.println("Nombre de participations trouvées: " + userParticipations.size());

        // Vérifier les statuts d'annulation en une seule fois
        cancellationStatus.clear();
        try {
            ParticipationService participationService = new ParticipationService();
            for (Participation p : userParticipations) {
                try {
                    boolean canCancel = participationService.canCancelParticipation(p.getId());
                    cancellationStatus.put(p.getId(), canCancel);
                } catch (SQLException e) {
                    System.out.println("Erreur lors de la vérification d'annulation pour ID " + p.getId() + ": " + e.getMessage());
                    // Continuer avec les autres participations
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la vérification des annulations: " + e.getMessage());
        }

        // Référence finale pour le lambda
        final List<Participation> finalParticipations = userParticipations;

        // Mettre à jour l'UI sur le thread JavaFX
        Platform.runLater(() -> {
            try {
                updateUI(finalParticipations);
            } catch (Exception e) {
                System.out.println("Erreur lors de la mise à jour de l'interface: " + e.getMessage());
                e.printStackTrace();
                showErrorMessage("Erreur d'affichage",
                        "Impossible d'afficher vos participations",
                        "Erreur: " + e.getMessage());
            }
        });
    }

    private void updateUI(List<Participation> userParticipations) {
        participationsFlowPane.getChildren().clear();
        participations.clear();
        participations.addAll(userParticipations);

        if (participations.isEmpty()) {
            Label noParticipationsLabel = new Label("Vous n'avez pas encore de participations");
            noParticipationsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-font-family: 'Roboto', sans-serif;");
            participationsFlowPane.getChildren().add(noParticipationsLabel);
            return;
        }

        for (Participation participation : participations) {
            boolean canCancel = cancellationStatus.getOrDefault(participation.getId(), false);
            createParticipationCard(participation, canCancel);
        }
    }

    private void createParticipationCard(Participation participation, boolean canCancel) {
        System.out.println("Création de la carte pour la participation ID: " + participation.getId() + ", annulation possible: " + canCancel);

        // Créer la carte pour chaque participation
        VBox participationCard = new VBox();
        participationCard.setMinWidth(350);
        participationCard.setMaxWidth(350);
        participationCard.setPrefHeight(280);
        participationCard.setAlignment(Pos.TOP_LEFT);
        participationCard.setSpacing(10);
        participationCard.setPadding(new Insets(15));
        participationCard.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Détails de l'événement
        Label titleLabel = new Label(participation.getEvent().getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3436;");
        titleLabel.setWrapText(true);

        // Date et heure
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String eventDateStr = dateFormat.format(participation.getEvent().getDate());
        String eventTimeStr = participation.getEvent().getHeure().toString();

        Label dateTimeLabel = new Label("📅 " + eventDateStr + " à " + eventTimeStr);
        dateTimeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");

        // Localisation
        Label locationLabel = new Label("📍 " + participation.getEvent().getLocalisation());
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");
        locationLabel.setWrapText(true);

        // Statut de la participation
        String statusText = participation.isWaiting() ? "⏳ Sur liste d'attente" : "✅ Inscrit";
        String statusStyle = participation.isWaiting() ?
                "-fx-background-color: #fdcb6e; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;" :
                "-fx-background-color: #00b894; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;";

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle(statusStyle);

        // Indicateur de délai d'annulation
        Label cancellationInfoLabel = new Label();
        if (canCancel) {
            cancellationInfoLabel.setText("✓ Annulation possible (plus de 24h avant l'événement)");
            cancellationInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #00b894;");
        } else {
            cancellationInfoLabel.setText("✗ Annulation impossible (moins de 24h avant l'événement)");
            cancellationInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
        }

        // Bouton d'annulation avec nouvelle logique
        Button cancelButton = new Button("Annuler ma participation");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5;");
        cancelButton.setDisable(!canCancel); // Désactiver le bouton si l'annulation est impossible

        if (canCancel) {
            cancelButton.setOnAction(e -> handleCancelParticipation(participation));
        } else {
            // Style pour le bouton désactivé
            cancelButton.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: #ecf0f1; -fx-padding: 8 15; -fx-background-radius: 5;");
        }

        // Ajouter les éléments à la carte
        participationCard.getChildren().addAll(
                titleLabel,
                dateTimeLabel,
                locationLabel,
                statusLabel,
                new javafx.scene.control.Separator(),
                cancellationInfoLabel,
                cancelButton
        );

        // Ajouter la carte au flowPane
        participationsFlowPane.getChildren().add(participationCard);
    }

    private void handleCancelParticipation(Participation participation) {
        System.out.println("Tentative d'annulation de la participation ID: " + participation.getId());
        // Demander confirmation avant d'annuler
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation d'annulation");
        confirmAlert.setHeaderText("Êtes-vous sûr de vouloir annuler votre participation ?");
        confirmAlert.setContentText("Cette action ne peut pas être annulée.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Utiliser un thread séparé pour l'opération de suppression
            executorService.submit(() -> {
                try {
                    // Procéder à l'annulation
                    boolean success = participationController.deleteParticipation(participation.getId());

                    Platform.runLater(() -> {
                        if (success) {
                            // Montrer un message de succès
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Participation annulée");
                            successAlert.setHeaderText("Votre participation a été annulée avec succès");
                            successAlert.setContentText("Si un participant était en liste d'attente, il a été notifié par SMS.");
                            successAlert.showAndWait();

                            // Recharger la liste des participations
                            refreshParticipations();
                        } else {
                            // Gérer l'échec
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Erreur");
                            errorAlert.setHeaderText("Impossible d'annuler la participation");
                            errorAlert.setContentText("Une erreur s'est produite lors de l'annulation de votre participation.");
                            errorAlert.showAndWait();
                        }
                    });
                } catch (Exception ex) {
                    System.out.println("Erreur lors de l'annulation: " + ex.getMessage());
                    ex.printStackTrace();

                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur");
                        errorAlert.setHeaderText("Erreur de base de données");
                        errorAlert.setContentText("Une erreur s'est produite lors de l'accès à la base de données.");
                        errorAlert.showAndWait();
                    });
                }
            });
        }
    }

    private void showErrorMessage(String title, String header, String content) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(header);
        errorAlert.setContentText(content);
        errorAlert.showAndWait();
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}