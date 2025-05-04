package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;
import java.io.IOException;
import java.sql.SQLException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Window;
import javafx.util.Duration;
import models.User;
import service.UserService;
import utils.SessionManager;
import utils.SessionStorage;

public class HomeController {
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean wasDragged = false;
    private Timeline chatPulseTimeline;
    private Popup settingsPopup;

    @FXML
    private Button chatButton;

    @FXML
    private AnchorPane contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button reclamationButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button settingsButton;

    private Object currentUser; // Utilisez le type approprié à votre modèle d'utilisateur
    private BooleanProperty isAdmin = new SimpleBooleanProperty(false);

    // Getter pour la propriété isAdmin utilisée dans le binding FXML
    public boolean getIsAdmin() {
        return isAdmin.get();
    }

    public BooleanProperty isAdminProperty() {
        return isAdmin;
    }

    public void initData(Object user) {
        this.currentUser = user;

        // Vérifier si l'utilisateur est un admin
        if (user instanceof User) {
            User u = (User) user;
            isAdmin.set("admin".equalsIgnoreCase(u.getRole()));
        } else {
            isAdmin.set(false);
        }
    }

    @FXML
    private void handleShowSettings(ActionEvent event) {
        if (settingsPopup != null && settingsPopup.isShowing()) {
            settingsPopup.hide();
            return;
        }

        // Créer le popup de paramètres
        settingsPopup = new Popup();
        settingsPopup.setAutoHide(true);
        settingsPopup.setHideOnEscape(true);

        // Créer le contenu du popup
        VBox popupContent = new VBox(10);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        popupContent.setAlignment(Pos.CENTER);
        popupContent.setPrefWidth(200);

        // Créer les boutons pour le popup
        Button profileBtn = createPopupButton("Profile", "image/profile_icon.png", "#8e44ad", event1 -> {
            settingsPopup.hide();
            handleAfficherProfile();
        });

        Button reclamationBtn = createPopupButton("Réclamations", "image/claim_icon.png", "#e67e22", event1 -> {
            settingsPopup.hide();
            handleAfficherFrontReclamation();
        });

        Button logoutBtn = createPopupButton("Déconnexion", "image/logout_icon.webp", "#e74c3c", event1 -> {
            settingsPopup.hide();
            handleLogout(event);
        });

        // Ajouter les boutons au popup
        popupContent.getChildren().addAll(profileBtn, reclamationBtn, logoutBtn);
        settingsPopup.getContent().add(popupContent);

        // Positionner et afficher le popup
        Button sourceButton = (Button) event.getSource();
        Window window = sourceButton.getScene().getWindow();
        double x = window.getX() + sourceButton.localToScene(0, 0).getX() + sourceButton.getScene().getX();
        double y = window.getY() + sourceButton.localToScene(0, 0).getY() + sourceButton.getScene().getY() + sourceButton.getHeight();

        settingsPopup.show(window, x - 150, y + 10);

        // Animation d'entrée
        popupContent.setScaleX(0.5);
        popupContent.setScaleY(0.5);
        popupContent.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), popupContent);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), popupContent);
        fadeTransition.setToValue(1);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    private Button createPopupButton(String text, String iconPath, String color, javafx.event.EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setPrefWidth(170);
        button.setPrefHeight(40);
        button.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-alignment: center-left;");

        // Effet au survol
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-alignment: center-left;")
        );

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: white; -fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-alignment: center-left;")
        );

        try {
            // Charger l'icône
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/" + iconPath)));
            imageView.setFitHeight(20);
            imageView.setFitWidth(20);
            button.setGraphic(imageView);
        } catch (Exception e) {
            System.out.println("Erreur de chargement de l'icône: " + e.getMessage());
        }

        button.setOnAction(action);
        return button;
    }

    @FXML
    private void handleShowUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des utilisateurs - Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible d'ouvrir le dashboard", e.getMessage());
        }
    }

    @FXML
    private void handleAfficherEvent() {
        loadUI("/DeatilsEvent.fxml");
    }

    @FXML
    private void handleAfficherTextile() {
        loadUI("/showtextile.fxml");
    }

    @FXML
    private void handleAfficherCollection() {
        loadUI("/show1.fxml");
    }

    @FXML
    private void handleAfficherCeramique() {
        loadUI("/show.fxml");
    }

    @FXML
    private void handleAfficherSponsor() {
        loadUI("/DetailsSponsor.fxml");
    }

    @FXML
    private void handleAjouterSponsor() {
        loadUI("/AjouterSponsor.fxml");
    }

    @FXML
    private void handleAjouterEvent() {
        loadUI("/AjouterEvent.fxml");
    }

    @FXML
    private void handleModifierEvent() {
        loadUI("/ModifierEvent.fxml");
    }

    @FXML
    private void handleModifierSponsor() {
        loadUI("/ModifierSponsor.fxml");
    }

    @FXML
    private void handleParticipations() {
        loadUI("/ParticipationDetails.fxml");
    }

    @FXML
    private void handleAfficherProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et définir l'utilisateur
            ProfileController profileController = loader.getController();
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                profileController.setUser(currentUser);

                // Configurer le callback pour la suppression du compte
                profileController.setAccountDeletedCallback(() -> {
                    // Rediriger vers la page de login en cas de suppression
                    try {
                        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                        Parent loginRoot = loginLoader.load();

                        // Animation de transition
                        Scene currentScene = contentArea.getScene();
                        Stage stage = (Stage) contentArea.getScene().getWindow();

                        // Animation de fondu
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(e -> {
                            stage.setScene(new Scene(loginRoot));
                            stage.setTitle("Connexion");

                            // Animation d'entrée pour la page de login
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginRoot);
                            fadeIn.setFromValue(0);
                            fadeIn.setToValue(1);
                            fadeIn.play();
                        });
                        fadeOut.play();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorAlert("Erreur", "Impossible de rediriger vers la page de login", e.getMessage());
                    }
                });

                // Créer une nouvelle scène pour le profil dans une fenêtre séparée
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Mon Profil");

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

    @FXML
    private void handleAfficherFrontReclamation() {
        loadUI("/AfficherFrontReclamation.fxml");
    }

    @FXML
    private void handleAfficherRecommendations() {
        try {
            System.out.println("Tentative de chargement de Recommendations.fxml");
            URL recommendationsUrl = getClass().getResource("/Recommendations.fxml");
            if (recommendationsUrl == null) {
                // Essayer d'autres chemins possibles
                recommendationsUrl = getClass().getResource("/Views/Recommendations.fxml");
                if (recommendationsUrl == null) {
                    recommendationsUrl = getClass().getResource("/FXML/Recommendations.fxml");
                    if (recommendationsUrl == null) {
                        recommendationsUrl = getClass().getClassLoader().getResource("Recommendations.fxml");
                        if (recommendationsUrl == null) {
                            throw new IOException("Impossible de trouver le fichier Recommendations.fxml dans les ressources");
                        }
                    }
                }
            }
            System.out.println("URL de la ressource trouvée : " + recommendationsUrl);

            // Charger la vue des recommandations
            FXMLLoader loader = new FXMLLoader(recommendationsUrl);
            Parent recommendationsView = loader.load();

            // Vider le contenu actuel
            contentArea.getChildren().clear();

            // Adapter la vue à la taille du contentArea
            AnchorPane.setTopAnchor(recommendationsView, 0.0);
            AnchorPane.setRightAnchor(recommendationsView, 0.0);
            AnchorPane.setBottomAnchor(recommendationsView, 0.0);
            AnchorPane.setLeftAnchor(recommendationsView, 0.0);

            // Ajouter la vue des recommandations dans le contentArea
            contentArea.getChildren().add(recommendationsView);
            System.out.println("Recommendations.fxml chargé avec succès");
        } catch (IOException e) {
            System.out.println("❌ Erreur lors du chargement des recommandations : " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger la vue des recommandations", e.getMessage());
        }
    }

    private void loadUI(String fxmlPath) {
        try {
            System.out.println("Tentative de chargement de " + fxmlPath);
            URL resourceUrl = getClass().getResource(fxmlPath);

            // Si le chemin direct ne fonctionne pas, essayer d'autres chemins possibles
            if (resourceUrl == null) {
                System.out.println("URL null pour " + fxmlPath + ", essai d'autres chemins...");

                // Essayer sans le slash initial
                String pathWithoutSlash = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
                resourceUrl = getClass().getClassLoader().getResource(pathWithoutSlash);

                // Essayer dans le dossier Views
                if (resourceUrl == null) {
                    String viewsPath = "/Views" + fxmlPath;
                    resourceUrl = getClass().getResource(viewsPath);
                    System.out.println("Essai avec " + viewsPath);
                }

                // Essayer dans le dossier FXML
                if (resourceUrl == null) {
                    String fxmlDirPath = "/FXML" + fxmlPath;
                    resourceUrl = getClass().getResource(fxmlDirPath);
                    System.out.println("Essai avec " + fxmlDirPath);
                }

                // Si toujours null, échec
                if (resourceUrl == null) {
                    throw new IOException("Impossible de trouver le fichier " + fxmlPath + " dans les ressources");
                }
            }

            System.out.println("URL de la ressource trouvée : " + resourceUrl);

            // Utilisez un FXMLLoader pour plus de contrôle
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            try {
                Parent root = loader.load();

                // Vider et ajouter le nouveau contenu
                contentArea.getChildren().clear();

                // Adapter la vue à la taille du contentArea
                AnchorPane.setTopAnchor(root, 0.0);
                AnchorPane.setRightAnchor(root, 0.0);
                AnchorPane.setBottomAnchor(root, 0.0);
                AnchorPane.setLeftAnchor(root, 0.0);

                contentArea.getChildren().add(root);
                System.out.println(fxmlPath + " chargé avec succès");
            } catch (Exception e) {
                System.out.println("Erreur spécifique lors du chargement : " + e.getMessage());
                e.printStackTrace();

                if (loader.getController() != null) {
                    System.out.println("Contrôleur créé : " + loader.getController().getClass().getName());
                } else {
                    System.out.println("Aucun contrôleur n'a été créé");
                }

                showErrorAlert("Erreur de chargement", "Impossible de charger " + fxmlPath, e.getMessage());
                throw e;
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur complète lors du chargement de " + fxmlPath + " : " + e);
            e.printStackTrace();
            showErrorAlert("Erreur de chargement", "Impossible de charger " + fxmlPath, e.getMessage());
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void initialize() {
        setupAnimations();
        setupChatButton();
        // Vérifier le rôle de l'utilisateur connecté
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Si l'utilisateur est admin, afficher le bouton dashboard
            boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
            dashboardButton.setVisible(isAdmin);
        } else {
            // Si pas d'utilisateur connecté, cacher le bouton
            dashboardButton.setVisible(false);
        }

        // Cacher les boutons originaux car ils sont remplacés par le bouton de paramètres
        if (profileButton != null) profileButton.setVisible(false);
        if (reclamationButton != null) reclamationButton.setVisible(false);
        if (logoutButton != null) logoutButton.setVisible(false);
    }

    private void setupAnimations() {
        setupChatButtonPulse();
    }

    private void setupChatButtonPulse() {
        // Animation de pulsation pour le bouton chat
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
    private void setupChatButton() {
        // Animation de pulsation (déjà configurée dans setupChatButtonPulse)

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
    @FXML
    private void handleOpenChat(ActionEvent event) {
        // Vérifier si le chat est déjà ouvert
        for (Window window : Window.getWindows()) {
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
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le chat: " + e.getMessage());
            alert.show();
        }
    }
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

    @FXML
    private void handleLogout(ActionEvent event) {
        // Effacer la session
        SessionStorage.clearSession();
        SessionManager.logout();

        try {
            // Charger la page de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            // Animation de transition
            Scene currentScene = ((javafx.scene.Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();

            // Créer une nouvelle scène
            Scene loginScene = new Scene(root);

            // Animation de fondu
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(loginScene);
                stage.setTitle("Connexion");

                // Animation d'entrée pour la page de login
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de charger la page de connexion", e.getMessage());
        }
    }
    public void loadReclamationsContent() {
        // Utiliser la méthode existante pour charger les réclamations
        handleAfficherFrontReclamation();
    }
}