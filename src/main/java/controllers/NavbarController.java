package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import utils.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NavbarController {
    @FXML private Button btnHistory;
    @FXML private Button btnNotifications;
    @FXML private StackPane notificationsCenter;
    @FXML private Circle notificationCountBadge;
    @FXML private Text notificationCount;
    @FXML private StackPane historyIndicator;
    @FXML private Label userNameLabel;
    @FXML private Circle userAvatar;
    @FXML private Text userInitials;
    @FXML private Button btnMonProfil;
    @FXML private Button btnLogout;
    @FXML private HBox navbarContainer;

    // Popups pour les notifications et l'historique
    @FXML private StackPane notificationsPopup;
    @FXML private VBox notificationsContainer;
    @FXML private StackPane historyPopup;
    @FXML private VBox historyContainer;

    // Listes pour stocker les notifications et l'historique
    private List<String> notifications = new ArrayList<>();
    private List<ActionHistorique> historyEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        // Afficher les informations de l'utilisateur actuel
        displayCurrentUserInfo();

        // Configurer les animations de la barre de navigation
        setupNavbarAnimations();

        // Initialiser les popups de notifications et d'historique
        setupNotificationsAndHistory();
    }

    /**
     * Affiche les informations de l'utilisateur connecté
     */
    private void displayCurrentUserInfo() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

            // Configurer l'avatar avec les initiales
            if (userInitials != null) {
                String initials = "";
                if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty()) {
                    initials += currentUser.getPrenom().substring(0, 1);
                }
                if (currentUser.getNom() != null && !currentUser.getNom().isEmpty()) {
                    initials += currentUser.getNom().substring(0, 1);
                }
                userInitials.setText(initials.toUpperCase());
            }

            // Couleur spécifique pour l'avatar basée sur l'ID utilisateur
            if (userAvatar != null) {
                int userId = currentUser.getId();
                Color avatarColor = Color.hsb((userId * 85) % 360, 0.5, 0.8);
                userAvatar.setFill(avatarColor);
            }
        }
    }

    /**
     * Configure les animations de la barre de navigation
     */
    private void setupNavbarAnimations() {
        // Animation de survol pour le bouton de notifications
        if (notificationsCenter != null) {
            applyHoverEffect(notificationsCenter, 1.1);
        }

        // Animation de survol pour le bouton d'historique
        if (historyIndicator != null) {
            applyHoverEffect(historyIndicator, 1.1);
        }

        // Animation du badge de notifications si présent
        if (notificationCountBadge != null) {
            Timeline pulsate = new Timeline(
                    new javafx.animation.KeyFrame(Duration.ZERO,
                            new javafx.animation.KeyValue(notificationCountBadge.opacityProperty(), 1.0)),
                    new javafx.animation.KeyFrame(Duration.seconds(1.5),
                            new javafx.animation.KeyValue(notificationCountBadge.opacityProperty(), 0.4)),
                    new javafx.animation.KeyFrame(Duration.seconds(3.0),
                            new javafx.animation.KeyValue(notificationCountBadge.opacityProperty(), 1.0))
            );
            pulsate.setCycleCount(Timeline.INDEFINITE);
            pulsate.play();
        }
    }

    /**
     * Initialise les popups de notifications et d'historique
     */
    private void setupNotificationsAndHistory() {
        // Vérifie si les popups existent dans le FXML
        if (notificationsPopup != null && historyPopup != null) {
            // Initialiser comme invisibles
            notificationsPopup.setVisible(false);
            historyPopup.setVisible(false);

            // Mettre à jour le compteur de notifications
            updateNotificationCount();
        }
    }

    /**
     * Applique un effet de survol à un nœud
     */
    private void applyHoverEffect(Node node, double scale) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), node);
        scaleUp.setToX(scale);
        scaleUp.setToY(scale);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        node.setOnMouseEntered(e -> scaleUp.play());
        node.setOnMouseExited(e -> scaleDown.play());
    }

    /**
     * Méthode pour afficher le popup des notifications
     */
    @FXML
    public void handleShowNotifications() {
        // Masquer l'historique s'il est visible
        if (historyPopup != null && historyPopup.isVisible()) {
            hidePopup(historyPopup);
        }

        if (notificationsPopup != null) {
            // Préparer le contenu des notifications
            if (notificationsContainer != null) {
                notificationsContainer.getChildren().clear();

                // Ajouter le titre
                Text title = new Text("Notifications");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
                notificationsContainer.getChildren().add(title);

                // Ajouter un séparateur
                javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
                notificationsContainer.getChildren().add(separator);

                // Ajouter les notifications ou un message si vide
                if (notifications.isEmpty()) {
                    Text emptyText = new Text("Aucune notification");
                    emptyText.setStyle("-fx-font-size: 14px; -fx-fill: #95a5a6;");
                    notificationsContainer.getChildren().add(emptyText);
                } else {
                    for (String notification : notifications) {
                        HBox notifBox = createNotificationItem(notification);
                        notificationsContainer.getChildren().add(notifBox);
                    }
                }
            }

            // Afficher le popup
            togglePopupVisibility(notificationsPopup);
        }
    }

    /**
     * Méthode pour afficher le popup de l'historique
     */
    @FXML
    public void handleShowHistory() {
        // Masquer les notifications si visibles
        if (notificationsPopup != null && notificationsPopup.isVisible()) {
            hidePopup(notificationsPopup);
        }

        if (historyPopup != null) {
            // Préparer le contenu de l'historique
            if (historyContainer != null) {
                historyContainer.getChildren().clear();

                // Ajouter le titre
                Text title = new Text("Historique des actions");
                title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
                historyContainer.getChildren().add(title);

                // Ajouter un séparateur
                javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
                historyContainer.getChildren().add(separator);

                // Ajouter les entrées d'historique ou un message si vide
                if (historyEntries.isEmpty()) {
                    Text emptyText = new Text("Aucune action enregistrée");
                    emptyText.setStyle("-fx-font-size: 14px; -fx-fill: #95a5a6;");
                    historyContainer.getChildren().add(emptyText);
                } else {
                    for (ActionHistorique entry : historyEntries) {
                        HBox historyItem = createHistoryItem(entry);
                        historyContainer.getChildren().add(historyItem);
                    }
                }
            }

            // Afficher le popup
            togglePopupVisibility(historyPopup);
        }
    }

    /**
     * Méthode pour afficher le profil de l'utilisateur
     */
    @FXML
    public void handleVoirMonProfil() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                profileController.setUser(currentUser);

                // Création de la nouvelle scène pour le profil
                Stage stage = new Stage();
                Scene scene = new Scene(root);

                stage.setTitle("Mon Profil");
                stage.setScene(scene);

                // Animation d'ouverture
                root.setScaleX(0.8);
                root.setScaleY(0.8);
                root.setOpacity(0);

                stage.show();

                // Animation combinée
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), root);
                scaleIn.setFromX(0.8);
                scaleIn.setFromY(0.8);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);

                ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
                parallelTransition.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le profil", e.getMessage());
        }
    }

    /**
     * Méthode pour se déconnecter
     */
    @FXML
    public void handleLogout() {
        SessionManager.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            // Animation de transition
            Scene currentScene = navbarContainer.getScene();

            // Créer une nouvelle scène avec animation
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = (Stage) navbarContainer.getScene().getWindow();

            // Animation de fondu
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajoute une notification
     */
    public void addNotification(String message) {
        notifications.add(message);
        updateNotificationCount();
    }

    /**
     * Ajoute une entrée dans l'historique
     */
    public void addHistoryEntry(String action, String utilisateur) {
        ActionHistorique entry = new ActionHistorique(action, new Date(), utilisateur);
        historyEntries.add(entry);
    }

    /**
     * Mettre à jour le compteur de notifications
     */
    private void updateNotificationCount() {
        if (notificationCount != null && notificationCountBadge != null) {
            int count = notifications.size();
            notificationCount.setText(String.valueOf(count));

            // Afficher/masquer le badge selon s'il y a des notifications
            notificationCountBadge.setVisible(count > 0);
            notificationCount.setVisible(count > 0);

            // Animation si nouvelles notifications
            if (count > 0) {
                pulseAnimation(notificationCountBadge);
            }
        }
    }

    /**
     * Animation de pulsation pour le badge de notification
     */
    private void pulseAnimation(Node node) {
        Timeline pulse = new Timeline(
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(node.scaleXProperty(), 1)),
                new javafx.animation.KeyFrame(Duration.ZERO,
                        new javafx.animation.KeyValue(node.scaleYProperty(), 1)),
                new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(node.scaleXProperty(), 1.2)),
                new javafx.animation.KeyFrame(Duration.millis(200),
                        new javafx.animation.KeyValue(node.scaleYProperty(), 1.2)),
                new javafx.animation.KeyFrame(Duration.millis(400),
                        new javafx.animation.KeyValue(node.scaleXProperty(), 1)),
                new javafx.animation.KeyFrame(Duration.millis(400),
                        new javafx.animation.KeyValue(node.scaleYProperty(), 1))
        );
        pulse.setCycleCount(3);
        pulse.play();
    }

    /**
     * Créer un élément de notification
     */
    private HBox createNotificationItem(String message) {
        HBox notifBox = new HBox(10);
        notifBox.setPadding(new javafx.geometry.Insets(10));
        notifBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        // Icône de notification
        StackPane iconPane = new StackPane();
        Circle iconBg = new Circle(15);
        iconBg.setFill(Color.web("#3498db"));

        Text iconText = new Text("📣");
        iconText.setFill(Color.WHITE);

        iconPane.getChildren().addAll(iconBg, iconText);

        // Message de notification
        Text messageText = new Text(message);
        messageText.setStyle("-fx-font-size: 14px;");
        messageText.setWrappingWidth(270);

        // Bouton de suppression
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #95a5a6; -fx-font-size: 16px; -fx-font-weight: bold;");

        deleteBtn.setOnAction(e -> {
            notifications.remove(message);
            updateNotificationCount();
            handleShowNotifications(); // Rafraîchir la vue
        });

        notifBox.getChildren().addAll(iconPane, messageText, spacer, deleteBtn);
        return notifBox;
    }

    /**
     * Créer un élément d'historique
     */
    private HBox createHistoryItem(ActionHistorique entry) {
        HBox historyBox = new HBox(10);
        historyBox.setPadding(new javafx.geometry.Insets(10));
        historyBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 5;");

        // Icône d'historique
        StackPane iconPane = new StackPane();
        Circle iconBg = new Circle(15);
        iconBg.setFill(Color.web("#9b59b6"));

        Text iconText = new Text("🕒");
        iconText.setFill(Color.WHITE);

        iconPane.getChildren().addAll(iconBg, iconText);

        // Informations d'action et date
        VBox infoBox = new VBox(5);

        Text actionText = new Text(entry.getAction());
        actionText.setStyle("-fx-font-size: 14px;");
        actionText.setWrappingWidth(270);

        // Formater la date
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDate = dateFormat.format(entry.getDate());

        Text dateText = new Text(formattedDate);
        dateText.setStyle("-fx-font-size: 12px; -fx-fill: #7f8c8d;");

        infoBox.getChildren().addAll(actionText, dateText);
        historyBox.getChildren().addAll(iconPane, infoBox);

        return historyBox;
    }

    /**
     * Basculer la visibilité d'un popup avec animation
     */
    private void togglePopupVisibility(StackPane popup) {
        if (popup.isVisible() && popup.getOpacity() > 0) {
            hidePopup(popup);
        } else {
            showPopup(popup);
        }
    }

    /**
     * Affiche un popup avec animation
     */
    private void showPopup(StackPane popup) {
        popup.setVisible(true);
        popup.setScaleX(0.9);
        popup.setScaleY(0.9);
        popup.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), popup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), popup);
        scaleIn.setFromX(0.9);
        scaleIn.setFromY(0.9);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(fadeIn, scaleIn);
        parallel.play();
    }

    /**
     * Cache un popup avec animation
     */
    private void hidePopup(StackPane popup) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), popup);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), popup);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);

        ParallelTransition parallel = new ParallelTransition(fadeOut, scaleOut);
        parallel.setOnFinished(e -> popup.setVisible(false));
        parallel.play();
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showErrorAlert(String title, String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Animation d'apparition
        javafx.scene.control.DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        alert.showAndWait();
    }

    /**
     * Navigation vers la page des utilisateurs
     */
    @FXML
    public void navigateToUsers() {
        navigateTo("/AfficherUser.fxml", "Liste des utilisateurs");
    }

    /**
     * Navigation vers la page des réclamations
     */
    @FXML
    public void navigateToReclamations() {
        navigateTo("/AfficherReclamation.fxml", "Liste des réclamations");
    }
    @FXML
    public void navigateToPeinture() {
        navigateTo("/AdminPanel.fxml", "Gestion des Peintures");

        // Ajouter à l'historique
        addHistoryEntry("Accès à la page Peinture", SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom());
    }
    /**
     * Méthode générique de navigation
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene currentScene = navbarContainer.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Animation de transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                // Changer la scène
                Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
                stage.setScene(newScene);
                stage.setTitle(title);

                // Animation d'entrée
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur de navigation", "Impossible de charger la page", e.getMessage());
        }
    }

    /**
     * Classe interne pour stocker les entrées d'historique
     */
    public class ActionHistorique {
        private String action;
        private Date date;
        private String utilisateur;

        public ActionHistorique(String action, Date date, String utilisateur) {
            this.action = action;
            this.date = date;
            this.utilisateur = utilisateur;
        }

        public String getAction() {
            return action;
        }

        public Date getDate() {
            return date;
        }

        public String getUtilisateur() {
            return utilisateur;
        }
    }

    @FXML
    private void handleGoToFront(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur actuel au HomeController
            HomeController homeController = loader.getController();
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                homeController.initData(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Accueil");

            // Animation de transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la page d'accueil", e.getMessage());
        }
    }
}