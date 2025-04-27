package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import models.User;
import utils.SessionManager;
import utils.SessionStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

public class FrontUserController {

    // Navigation elements
    @FXML private HBox navbarContainer;
    @FXML private Button homeButton;
    @FXML private Button adminButton;
    @FXML private Button translationButton;

    // User profile elements
    @FXML private Circle userAvatar;
    @FXML private Text userInitials;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // Main content
    @FXML private VBox mainContent;
    @FXML private ImageView backgroundImage;

    // Floating chat button
    @FXML private Button chatButton;

    // Variables pour le chat button
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean wasDragged = false;

    // Animation du bouton chat
    private Timeline chatPulseTimeline;

    // Liste pour historique et notifications
    private List<String> notifications = new ArrayList<>();
    private List<ActionHistorique> historyEntries = new ArrayList<>();

    // Préférences pour l'arrière-plan
    private String currentBackgroundPath = null;

    @FXML
    public void initialize() {
        // Configuration de base
        Platform.runLater(() -> {
            // Afficher les infos utilisateur
            displayCurrentUserInfo();

            // Configurer les boutons selon le rôle
            setupNavigation();

            // Configurer le bouton chat flottant
            setupChatButton();

            // Ajouter les animations
            setupAnimations();

            // Charger l'arrière-plan
            loadSavedBackground();

            // Afficher le contenu d'accueil
            showWelcomeContent();
        });
    }

    /**
     * Affiche les informations de l'utilisateur actuel
     */
    private void displayCurrentUserInfo() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Nom et rôle
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            userRoleLabel.setText(currentUser.getRole());

            // Initiales pour l'avatar
            String initials = "";
            if (currentUser.getPrenom() != null && !currentUser.getPrenom().isEmpty()) {
                initials += currentUser.getPrenom().substring(0, 1);
            }
            if (currentUser.getNom() != null && !currentUser.getNom().isEmpty()) {
                initials += currentUser.getNom().substring(0, 1);
            }
            userInitials.setText(initials.toUpperCase());

            // Couleur de l'avatar basée sur l'ID utilisateur
            int userId = currentUser.getId();
            Color avatarColor = Color.hsb((userId * 85) % 360, 0.5, 0.8);
            userAvatar.setFill(avatarColor);
        }
    }

    /**
     * Configure la navigation selon le rôle utilisateur
     */
    private void setupNavigation() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Afficher le bouton admin uniquement pour les administrateurs
            boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
            adminButton.setVisible(isAdmin);
        } else {
            adminButton.setVisible(false);
        }
    }

    /**
     * Configure les animations
     */
    private void setupAnimations() {
        // Animation de pulsation pour le bouton chat
        setupChatButtonPulse();

        // Effets de survol pour les boutons
        applyHoverEffect(homeButton, 1.05);
        applyHoverEffect(adminButton, 1.05);
        applyHoverEffect(translationButton, 1.05);
        applyHoverEffect(chatButton, 1.1);
    }

    /**
     * Configure l'animation de pulsation du bouton chat
     */
    private void setupChatButtonPulse() {
        chatPulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(chatButton.scaleXProperty(), 1)),
                new KeyFrame(Duration.ZERO, new KeyValue(chatButton.scaleYProperty(), 1)),
                new KeyFrame(Duration.seconds(1), new KeyValue(chatButton.scaleXProperty(), 1.1)),
                new KeyFrame(Duration.seconds(1), new KeyValue(chatButton.scaleYProperty(), 1.1)),
                new KeyFrame(Duration.seconds(2), new KeyValue(chatButton.scaleXProperty(), 1)),
                new KeyFrame(Duration.seconds(2), new KeyValue(chatButton.scaleYProperty(), 1))
        );
        chatPulseTimeline.setCycleCount(Timeline.INDEFINITE);
        chatPulseTimeline.play();
    }

    /**
     * Applique un effet de survol à un élément
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
     * Configure le bouton chat flottant
     */
    private void setupChatButton() {
        // Configuration du drag and drop
        chatButton.setOnMousePressed(event -> {
            xOffset = event.getSceneX() - chatButton.getLayoutX();
            yOffset = event.getSceneY() - chatButton.getLayoutY();
            wasDragged = false;
            event.consume();
        });

        chatButton.setOnMouseDragged(event -> {
            wasDragged = true;

            double newX = event.getSceneX() - xOffset;
            double newY = event.getSceneY() - yOffset;

            // Limites pour ne pas sortir de l'écran
            newX = Math.max(0, Math.min(newX, chatButton.getParent().getLayoutBounds().getWidth() - chatButton.getWidth()));
            newY = Math.max(0, Math.min(newY, chatButton.getParent().getLayoutBounds().getHeight() - chatButton.getHeight()));

            chatButton.setLayoutX(newX);
            chatButton.setLayoutY(newY);
            event.consume();
        });

        chatButton.setOnMouseReleased(event -> {
            if (wasDragged) {
                snapToEdge(); // Coller au bord le plus proche
            }
            event.consume();
        });

        chatButton.setOnMouseClicked(event -> {
            if (!wasDragged && event.getButton() == MouseButton.PRIMARY) {
                handleOpenChat(new ActionEvent(chatButton, null));
            }
            event.consume();
        });
    }

    /**
     * Colle le bouton chat au bord le plus proche
     */
    private void snapToEdge() {
        double parentWidth = chatButton.getParent().getLayoutBounds().getWidth();
        double buttonWidth = chatButton.getWidth();
        double currentX = chatButton.getLayoutX();
        double currentY = chatButton.getLayoutY();

        // Déterminer le bord le plus proche
        boolean snapToRight = currentX > parentWidth / 2;
        double targetX = snapToRight ? parentWidth - buttonWidth : 0;

        // Garder la position Y actuelle (ou ajuster si nécessaire)
        double targetY = Math.max(20, Math.min(currentY,
                chatButton.getParent().getLayoutBounds().getHeight() - chatButton.getHeight() - 20));

        // Animation avec rebond
        Timeline timeline = new Timeline();

        // Animation principale
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(200),
                        new KeyValue(chatButton.layoutXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY, Interpolator.EASE_OUT))
        );

        // Premier rebond
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(chatButton.layoutXProperty(), snapToRight ? targetX - 25 : targetX + 25, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY + 15, Interpolator.EASE_OUT))
        );

        // Position finale
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(500),
                        new KeyValue(chatButton.layoutXProperty(), targetX, Interpolator.EASE_OUT),
                        new KeyValue(chatButton.layoutYProperty(), targetY, Interpolator.EASE_OUT))
        );

        timeline.play();
    }

    /**
     * Charge l'arrière-plan sauvegardé
     */
    private void loadSavedBackground() {
        // Récupérer les préférences utilisateur
        Preferences prefs = Preferences.userNodeForPackage(FrontUserController.class);

        // Récupérer le chemin de l'image
        String savedPath = prefs.get("background_image", null);

        // Récupérer l'opacité
        double opacity = prefs.getDouble("background_opacity", 0.2);

        if (savedPath != null) {
            try {
                // Charger l'image
                Image image = new Image(new File(savedPath).toURI().toString(), true);
                backgroundImage.setImage(image);
                currentBackgroundPath = savedPath;

                // Ajuster l'opacité
                backgroundImage.setOpacity(opacity);

                // Rendre l'image responsive
                setupResponsiveBackground();
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'arrière-plan: " + e.getMessage());
            }
        }
    }

    /**
     * Configure l'adaptation de l'arrière-plan à la taille de la fenêtre
     */
    private void setupResponsiveBackground() {
        Scene scene = backgroundImage.getScene();
        if (scene != null) {
            // Ajuster la taille de l'image lors du redimensionnement
            scene.widthProperty().addListener((obs, oldVal, newVal) -> updateBackgroundSize());
            scene.heightProperty().addListener((obs, oldVal, newVal) -> updateBackgroundSize());

            // Configuration initiale
            updateBackgroundSize();
        }
    }

    /**
     * Met à jour la taille de l'arrière-plan
     */
    private void updateBackgroundSize() {
        if (backgroundImage.getScene() != null) {
            // Récupère les dimensions actuelles de la fenêtre
            double width = backgroundImage.getScene().getWidth();
            double height = backgroundImage.getScene().getHeight();

            // Définit la taille de l'image pour qu'elle couvre toute la fenêtre
            backgroundImage.setFitWidth(width);
            backgroundImage.setFitHeight(height);
        }
    }

    /**
     * Affiche le contenu d'accueil
     */
    private void showWelcomeContent() {
        mainContent.getChildren().clear();

        // Créer un contenu d'accueil
        VBox welcomeBox = new VBox(20);
        welcomeBox.setAlignment(Pos.CENTER);
        welcomeBox.setPadding(new Insets(50));
        welcomeBox.setStyle("-fx-background-color: rgba(255,255,255,0.8); -fx-background-radius: 15;");

        Text welcomeTitle = new Text("Bienvenue dans l'application");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 30));
        welcomeTitle.setStyle("-fx-fill: #3498db;");

        Text welcomeDescription = new Text("Cette page d'accueil sera enrichie ultérieurement.");
        welcomeDescription.setFont(Font.font("System", 16));
        welcomeDescription.setStyle("-fx-fill: #7f8c8d;");

        // Ajouter quelques boutons d'action fictifs pour montrer l'aspect responsive
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);

        Button action1 = new Button("Action 1");
        action1.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        Button action2 = new Button("Action 2");
        action2.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        Button action3 = new Button("Action 3");
        action3.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");

        actionButtons.getChildren().addAll(action1, action2, action3);

        welcomeBox.getChildren().addAll(welcomeTitle, welcomeDescription, actionButtons);

        // Effet d'ombre pour le panel
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setColor(Color.color(0, 0, 0, 0.2));
        welcomeBox.setEffect(shadow);

        // Ajouter le contenu et lui permettre de grandir
        mainContent.getChildren().add(welcomeBox);
        VBox.setVgrow(welcomeBox, Priority.ALWAYS);

        // Animation d'entrée
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), welcomeBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    /*
     * Gestionnaires d'événements
     */

    @FXML
    private void handleHomeButton(ActionEvent event) {
        showWelcomeContent();
    }

    @FXML
    private void handleAdminButton(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();

        // Vérifier si l'utilisateur est admin
        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            showAlert("Accès refusé", "Seul un administrateur peut accéder au back-office.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();

            // Animation de transition
            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Fondu sortant
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                // Changer la scène
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Administration - Liste des utilisateurs");

                // Fondu entrant
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();

            // Ajouter à l'historique
            String userName = currentUser.getPrenom() + " " + currentUser.getNom();
            addHistoryEntry("Accès au back-office (Administration)", userName);

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la page d'administration: " + e.getMessage());
        }
    }

    @FXML
    private void handleTranslationButton(ActionEvent event) {
        try {
            // Charger la vue de traduction
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Translation.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage translationStage = new Stage();
            Scene scene = new Scene(root);

            translationStage.setTitle("Service de Traduction");
            translationStage.setScene(scene);

            // Animation d'ouverture
            root.setScaleX(0.8);
            root.setScaleY(0.8);
            root.setOpacity(0);

            translationStage.show();

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

            // Ajouter à l'historique
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                String userName = currentUser.getPrenom() + " " + currentUser.getNom();
                addHistoryEntry("Ouverture du service de traduction", userName);
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le service de traduction: " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenChat(ActionEvent event) {
        // Vérifier si le chat est déjà ouvert
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window instanceof Stage && "Chat en ligne".equals(((Stage) window).getTitle())) {
                window.requestFocus();
                return;
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
            Parent root = loader.load();

            // Créer le stage sans bordure
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            // Créer la scène avec un fond transparent
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setTitle("Chat en ligne");

            // Afficher la fenêtre
            stage.show();

            // Positionner en bas à droite de l'écran
            positionChatWindowBottomRight(stage);

            // Animation d'ouverture
            root.setScaleX(0.3);
            root.setScaleY(0.3);
            root.setOpacity(0);

            // Animation d'ouverture
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), root);
            scaleIn.setFromX(0.3);
            scaleIn.setFromY(0.3);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
            parallelTransition.setInterpolator(Interpolator.EASE_OUT);
            parallelTransition.play();

            // Ajouter à l'historique
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser != null) {
                String userName = currentUser.getPrenom() + " " + currentUser.getNom();
                addHistoryEntry("Ouverture du chat en ligne", userName);
            }

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le chat: " + e.getMessage());
        }
    }

    /**
     * Positionne la fenêtre de chat en bas à droite de l'écran
     */
    private void positionChatWindowBottomRight(Stage stage) {
        // Obtenir les dimensions de l'écran
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();

        // Attendre que le stage soit complètement chargé pour obtenir ses dimensions réelles
        stage.setOnShown(e -> {
            // Calculer la position pour que la fenêtre soit en bas à droite
            double rightPosition = screenBounds.getMaxX() - stage.getWidth() - 20;  // 20px de marge
            double bottomPosition = screenBounds.getMaxY() - stage.getHeight() - 50; // 50px de marge

            // Positionner la fenêtre
            stage.setX(rightPosition);
            stage.setY(bottomPosition);
        });
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionStorage.clearSession();
        SessionManager.logout();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            // Animation de transition
            Scene currentScene = ((Node) event.getSource()).getScene();

            // Créer une nouvelle scène avec animation
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

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
    @FXML
    private void handleVoirMonProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                profileController.setUser(currentUser);

                // Créer la nouvelle fenêtre pour le profil
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

                // Ajouter à l'historique
                String userName = currentUser.getPrenom() + " " + currentUser.getNom();
                addHistoryEntry("Consultation du profil", userName);
            }
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    /*
     * Méthodes utilitaires
     */

    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Ajoute une notification
     */
    public void addNotification(String message) {
        notifications.add(message);
    }

    /**
     * Ajoute une entrée dans l'historique
     */
    public void addHistoryEntry(String action, String utilisateur) {
        ActionHistorique entry = new ActionHistorique(action, new Date(), utilisateur);
        historyEntries.add(entry);
    }

    /**
     * Classe interne pour les entrées d'historique
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
}