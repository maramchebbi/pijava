package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.*;
import javafx.util.Duration;
import models.User;
import service.UserService;
import utils.EmailSender;
import utils.SessionManager;
import utils.SessionStorage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class AfficherUserController {
    @FXML private Button btnHistory;
    @FXML private Button btnNotifications;
    @FXML private StackPane notificationsCenter;
    //@FXML private Circle notificationCountBadge;
    @FXML private StackPane historyIndicator;
    @FXML private ImageView backgroundImage;
    @FXML private Button customizeButton;
    @FXML private StackPane customizePopup;
    @FXML private Button closeCustomizeBtn;
    @FXML private Button chooseImageBtn;
    @FXML private Button resetBackgroundBtn;
    @FXML private Slider opacitySlider;
    @FXML private Text opacityValue;
    // Popup pour les notifications et l'historique
    @FXML private StackPane notificationsPopup;
    @FXML private VBox notificationsContainer;
    @FXML private StackPane historyPopup;
    @FXML private VBox historyContainer;

    // Liste pour stocker les notifications et l'historique
    private List<String> notifications = new ArrayList<>();
    private List<String> history = new ArrayList<>();

    @FXML
    private GridPane userGrid;
    @FXML
    private Button addButton;
    @FXML
    private Button chatButton;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField searchField;
    @FXML
    private StackPane loadingPane;
    @FXML
    private Text pageTitle;
    @FXML
    private Circle userAvatar;
    @FXML
    private HBox searchContainer;

    private double xOffset = 0;
    private double yOffset = 0;
    private boolean wasDragged = false;
    private Timeline chatPulseTimeline;
    @FXML
    private Button translationButton;
    // Déclaration des variables pour les notifications et l'historique
    private List<ActionHistorique> historyEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        setupAnimations();
        Platform.runLater(() -> {
            if (scrollPane != null && scrollPane.getScene() != null) {
                scrollPane.getScene().getRoot().setUserData(this);
            }
        });

        // Afficher les données utilisateur actuelles
        //displayCurrentUserInfo();

        // Charger et afficher la liste des utilisateurs
        loadUsers();

        // Configurer le bouton de chat flottant
        setupChatButton();

        // Configurer le responsive design
        setupResponsiveLayout();

        try {
            originalUserList = new UserService().select();
        } catch (SQLException e) {
            showErrorAlert("Erreur", "Impossible de charger les utilisateurs", e.getMessage());
            e.printStackTrace();
        }

        // Configurer le MenuButton de tri
        setupSortButton();

        // Initialiser les notifications et l'historique
        setupNotificationsAndHistory();

        Platform.runLater(() -> {
            setupNavbarAnimations();
            enhanceCurrentUserDisplay();
        });
        setupCustomization();
}

    private void setupSortButton() {
        // Définir l'action par défaut (tri A-Z)
        sortButton.setText("Trier par");

        // Configurer les actions des MenuItems
        sortByNameAsc.setOnAction(this::handleSortByNameAsc);
        sortByNameDesc.setOnAction(this::handleSortByNameDesc);
        sortByRole.setOnAction(this::handleSortByRole);
        sortByGender.setOnAction(this::handleSortByGender);
    }


    private void setupAnimations() {
        // Animation du titre
        animateTitle();

        // Animation du bouton chat
        setupChatButtonPulse();

        // Effets de survol pour les boutons
        setupHoverEffects();
    }

    private void animateTitle() {
        // Changer progressivement la couleur du titre
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pageTitle.scaleXProperty(), 1)),
                new KeyFrame(Duration.ZERO, new KeyValue(pageTitle.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(200), new KeyValue(pageTitle.scaleXProperty(), 1.05)),
                new KeyFrame(Duration.millis(200), new KeyValue(pageTitle.scaleYProperty(), 1.05)),
                new KeyFrame(Duration.millis(400), new KeyValue(pageTitle.scaleXProperty(), 1)),
                new KeyFrame(Duration.millis(400), new KeyValue(pageTitle.scaleYProperty(), 1))
        );
        timeline.play();
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

    private void setupHoverEffects() {
        // Appliquer des effets de survol aux boutons
        applyHoverEffect(addButton, 1.05);
        applyHoverEffect(chatButton, 1.1);
    }

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

    private void displayCurrentUserInfo() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
           // userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

            // Configurez le cercle de l'avatar avec les initiales
            Text initialText = new Text(currentUser.getPrenom().substring(0, 1) + currentUser.getNom().substring(0, 1));
            initialText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            initialText.setFill(Color.WHITE);

            StackPane avatarPane = new StackPane();
            avatarPane.getChildren().add(initialText);

            // Masquer le bouton "Ajouter" si l'utilisateur n'est pas admin
            if (addButton != null) {
                addButton.setVisible("admin".equalsIgnoreCase(currentUser.getRole()));
            }
        }
    }

    private void loadUsers() {
        // Afficher l'indicateur de chargement
        loadingPane.setVisible(true);

        // Simuler un délai de chargement pour l'animation
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> {
            try {
                UserService userService = new UserService();
                List<User> userList = userService.select();
                User currentUser = SessionManager.getCurrentUser();

                // Configurer la recherche
                setupSearch();

                // Afficher les utilisateurs avec animation
                displayUsersWithAnimation(userList, currentUser);

                // Masquer l'indicateur de chargement
                loadingPane.setVisible(false);
            } catch (SQLException e) {
                showErrorAlert("Erreur de base de données", "Impossible de charger les données utilisateur", e.getMessage());
                e.printStackTrace();
                loadingPane.setVisible(false);
            }
        });
        pause.play();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    @FXML
    private void handleSearchAction(KeyEvent event) {
        filterUsers(searchField.getText());
    }

    private void filterUsers(String searchText) {
        try {
            UserService userService = new UserService();
            List<User> allUsers = userService.select();

            if (searchText == null || searchText.isEmpty()) {
                displayUsersWithAnimation(allUsers, SessionManager.getCurrentUser());
                return;
            }

            // Filtrer les utilisateurs selon le texte de recherche
            List<User> filteredUsers = allUsers.stream()
                    .filter(user ->
                            user.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                                    user.getPrenom().toLowerCase().contains(searchText.toLowerCase()) ||
                                    user.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                                    user.getRole().toLowerCase().contains(searchText.toLowerCase())
                    )
                    .collect(Collectors.toList());

            displayUsersWithAnimation(filteredUsers, SessionManager.getCurrentUser());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayUsersWithAnimation(List<User> userList, User currentUser) {
        userGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int delay = 0;

        // Animation en cascade pour chaque carte
        for (User user : userList) {
            VBox card = createUserCard(user, currentUser);
            card.setOpacity(0);
            card.setTranslateY(20);

            userGrid.add(card, column, row);
            GridPane.setHalignment(card, HPos.CENTER);

            // Animation d'entrée pour chaque carte
            PauseTransition pause = new PauseTransition(Duration.millis(delay));
            pause.setOnFinished(event -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), card);
                slideIn.setFromY(20);
                slideIn.setToY(0);

                ParallelTransition parallelTransition = new ParallelTransition(fadeIn, slideIn);
                parallelTransition.play();
            });
            pause.play();

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }

            delay += 100; // Délai pour effet en cascade
        }
    }

    private VBox createUserCard(User user, User currentUser) {
        // Créer le conteneur de la carte
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPrefWidth(300);
        card.setMinWidth(280);
        card.setMaxWidth(350);
        card.setPrefHeight(220);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        // Icône de profil avec cercle
        Circle profileCircle = new Circle(30);
        profileCircle.setFill(Color.web("#f0f7fd"));
        profileCircle.setStroke(Color.web("#c9e3f9"));
        profileCircle.setStrokeWidth(2);

        Text initialText = new Text(user.getNom().substring(0, 1) + user.getPrenom().substring(0, 1));
        initialText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        initialText.setFill(Color.web("#3498db"));

        StackPane profileStack = new StackPane(profileCircle, initialText);

        // Section d'informations utilisateur
        Text nameText = new Text(user.getNom() + " " + user.getPrenom());
        nameText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameText.setFill(Color.web("#2c3e50"));

        Text emailText = new Text(user.getEmail());
        emailText.setFont(Font.font("Segoe UI", 14));
        emailText.setFill(Color.web("#7f8c8d"));

        Text roleText = new Text(user.getRole());
        roleText.setFont(Font.font("Segoe UI", 13));
        roleText.setFill(Color.web("#95a5a6"));

        // Badge pour utilisateurs vérifiés
        HBox nameRow = new HBox(5);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        nameRow.getChildren().add(nameText);

        if (user.isVerified()) {
            Circle verifiedBadge = new Circle(8);
            verifiedBadge.setFill(Color.web("#2ecc71"));
            nameRow.getChildren().add(verifiedBadge);
        }

        VBox textInfo = new VBox(5);
        textInfo.getChildren().addAll(nameRow, emailText, roleText);

        HBox userInfo = new HBox(15);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        userInfo.getChildren().addAll(profileStack, textInfo);

        // Section boutons
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_LEFT);

        Button detailButton = createButton("Details", "#5dade2", e -> showUserDetails(user));
        actionButtons.getChildren().add(detailButton);

        // Ajouter des boutons d'édition/suppression pour les administrateurs
        if (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
            Button editButton = createButton("Edit", "#2ecc71", e -> editUser(user));
            Button deleteButton = createButton("Delete", "#e74c3c", e -> confirmDeleteUser(user));

            actionButtons.getChildren().addAll(editButton, deleteButton);
        }

        // Ajouter les composants à la carte
        card.getChildren().addAll(userInfo, new Separator(), actionButtons);

        // Ajouter des animations au survol
        addCardHoverEffect(card);

        return card;
    }

    private void addCardHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 15, 0, 0, 5);");

            // Animation de survol
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.03);
            scaleTransition.setToY(1.03);
            scaleTransition.play();
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(52,152,219,0.3), 15, 0, 0, 5);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"));

            // Animation de retour
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), card);
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);
            scaleTransition.play();
        });
    }

    private Button createButton(String text, String color, EventHandler<ActionEvent> action) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 13px; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 6 12;"
        );
        button.setOnAction(action);

        // Ajouter un effet de survol
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0, 0, 2);", ""));
        });

        return button;
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


    private void setupResponsiveLayout() {
        // Adapter la mise en page lors du redimensionnement
        scrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            int columns = width < 800 ? 2 : 3;

            // Recréer la grille avec le nouveau nombre de colonnes
            userGrid.getColumnConstraints().clear();
            for (int i = 0; i < columns; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(100.0 / columns);
                column.setHalignment(HPos.CENTER);
                userGrid.getColumnConstraints().add(column);
            }

            // Rafraîchir l'affichage
            refreshUserGrid();
        });
    }

    private void refreshUserGrid() {
        try {
            List<User> userList = new UserService().select();
            User currentUser = SessionManager.getCurrentUser();
            displayUsersWithAnimation(userList, currentUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Animation d'apparition
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        alert.showAndWait();
    }


    private void showNotification(String title, String message) {
        // Vous pouvez implémenter une animation de notification ici
        System.out.println("[" + title + "] " + message);
    }

    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();
            AjouterUserController controller = loader.getController();
            // Configurer un callback pour être notifié quand un utilisateur est ajouté
            controller.setOnUserAddedCallback(user -> {
                String currentUserName = SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom();
                addHistoryEntry(currentUserName + " a ajouté l'utilisateur " + user.getPrenom() + " " + user.getNom(), currentUserName);
                addNotification("Nouvel utilisateur ajouté: " + user.getPrenom() + " " + user.getNom());
            });

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Ajouter un utilisateur");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void ouvrirModifierMotDePasse() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMotDePasse.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Modifier mon mot de passe");

        // Animation d'ouverture
        root.setOpacity(0);

        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
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
    private void handleAfficherReclamations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Réclamations");
            stage.show();
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

                // Définir le callback pour gérer la suppression du compte
                profileController.setAccountDeletedCallback(() -> {
                    // Rediriger vers la page de login
                    try {
                        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                        Parent loginRoot = loginLoader.load();

                        // Animation de transition
                        Scene currentScene = scrollPane.getScene();
                        Stage stage = (Stage) scrollPane.getScene().getWindow();

                        // Animation de fondu
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScene.getRoot());
                        fadeOut.setFromValue(1);
                        fadeOut.setToValue(0);
                        fadeOut.setOnFinished(e -> {
                            // Changer la scène
                            Scene loginScene = new Scene(loginRoot);
                            stage.setScene(loginScene);
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

                // Création de la nouvelle scène et fenêtre pour le profil
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
    }    @FXML
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

    /**
     * Affiche les détails d'un utilisateur avec une animation professionnelle
     */
    private void showUserDetails(User user) {
        try {
            // Charger la vue de détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailUser.fxml"));
            Parent detailRoot = loader.load();

            // Configurer le contrôleur de détails
            DetailUserController detailUserController = loader.getController();
            detailUserController.setNom(user.getNom());
            detailUserController.setPrenom(user.getPrenom());
            detailUserController.setGenre(user.getGenre());
            detailUserController.setEmail(user.getEmail());
            detailUserController.setPassword(user.getPassword());
            detailUserController.setRole(user.getRole());

            // Obtenir la scène et la fenêtre actuelles
            Scene currentScene = scrollPane.getScene();
            Stage stage = (Stage) scrollPane.getScene().getWindow();

            // Créer un conteneur pour la transition
            StackPane transitionContainer = new StackPane();

            // Ajouter un effet de flou à la vue actuelle
            GaussianBlur blur = new GaussianBlur(0);
            currentScene.getRoot().setEffect(blur);

            // Configurer l'animation d'entrée de la nouvelle vue
            detailRoot.setOpacity(0);
            detailRoot.translateYProperty().set(50);

            // Ajouter les deux vues au conteneur de transition
            transitionContainer.getChildren().addAll(currentScene.getRoot(), detailRoot);
            Scene transitionScene = new Scene(transitionContainer, currentScene.getWidth(), currentScene.getHeight());

            // Animation parallèle pour une transition fluide
            ParallelTransition parallelTransition = new ParallelTransition();

            // Effet de flou progressif sur l'ancienne vue
            Timeline blurTimeline = new Timeline();
            blurTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(blur.radiusProperty(), 10, Interpolator.EASE_BOTH))
            );

            // Fondu entrant pour la nouvelle vue
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), detailRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);

            // Animation de glissement vers le haut
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), detailRoot);
            slideIn.setFromY(50);
            slideIn.setToY(0);
            slideIn.setInterpolator(Interpolator.EASE_OUT);

            // Combiner les animations
            parallelTransition.getChildren().addAll(blurTimeline, fadeIn, slideIn);

            // Exécuter l'animation
            stage.setScene(transitionScene);
            stage.setTitle("Détails de l'utilisateur: " + user.getPrenom() + " " + user.getNom());

            parallelTransition.setOnFinished(evt -> {
                // Remplacer complètement par la nouvelle vue après la transition
                stage.setScene(new Scene(detailRoot, currentScene.getWidth(), currentScene.getHeight()));
                currentScene.getRoot().setEffect(null);
            });

            parallelTransition.play();

        } catch (IOException e) {
            // Animation spéciale pour l'alerte d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de navigation");
            alert.setHeaderText("Impossible d'ouvrir les détails de l'utilisateur");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStyleClass().add("custom-alert");
            dialogPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #f5f5f5); -fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5px;");

            // Animation d'entrée pour l'alerte
            dialogPane.setOpacity(0);
            dialogPane.setScaleX(0.9);
            dialogPane.setScaleY(0.9);

            alert.setOnShown(event -> {
                // Animation combinée pour l'alerte
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), dialogPane);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);

                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(250), dialogPane);
                scaleIn.setFromX(0.9);
                scaleIn.setFromY(0.9);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                scaleIn.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn);
                parallelTransition.play();
            });

            alert.showAndWait();
            e.printStackTrace();
        }
    }

    /**
     * Ouvre la fenêtre d'édition d'un utilisateur avec des animations élégantes
     */
    private void editUser(User user) {
        User currentUserInSession = SessionManager.getCurrentUser();
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès refusé");
            alert.setHeaderText(null);
            alert.setContentText("Seul un administrateur peut modifier un utilisateur.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
            Parent root = loader.load();

            ModifierUserController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier : " + user.getPrenom() + " " + user.getNom());

            // Animation rapide
            root.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(200), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            // Rafraîchir après fermeture
            stage.setOnHiding(e -> refreshUserGrid());

            stage.show();

            // Envoi de notification
            EmailSender.sendLoginNotification(user.getEmail(), user.getPrenom());

        } catch (IOException e) {
            showAnimatedErrorAlert("Erreur", "Impossible d'ouvrir la fenêtre", e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Affiche une boîte de dialogue de confirmation pour supprimer un utilisateur
     * avec des animations et effets visuels avancés
     */
    private void confirmDeleteUser(User user) {
        try {
            // Créer une boîte de dialogue personnalisée au lieu d'utiliser Alert standard
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Créer le contenu de la boîte de dialogue
            VBox dialogRoot = new VBox(20);
            dialogRoot.setAlignment(Pos.CENTER);
            dialogRoot.setPadding(new Insets(30));
            dialogRoot.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 15, 0, 0, 5);");
            dialogRoot.setMaxWidth(450);
            dialogRoot.setMaxHeight(300);

            // Icône d'avertissement avec animation
            StackPane iconContainer = new StackPane();
            Circle warningCircle = new Circle(40);
            warningCircle.setFill(Color.web("#ffeeee"));
            warningCircle.setStroke(Color.web("#e74c3c"));
            warningCircle.setStrokeWidth(3);

            Text exclamation = new Text("!");
            exclamation.setFont(Font.font("Segoe UI", FontWeight.BOLD, 50));
            exclamation.setFill(Color.web("#e74c3c"));

            iconContainer.getChildren().addAll(warningCircle, exclamation);

            // Titre de la boîte de dialogue
            Text titleText = new Text("Confirmer la suppression");
            titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            titleText.setFill(Color.web("#2c3e50"));

            // Message de confirmation
            Text messageText = new Text("Êtes-vous sûr de vouloir supprimer définitivement\nl'utilisateur " + user.getNom() + " " + user.getPrenom() + " ?");
            messageText.setFont(Font.font("Segoe UI", 16));
            messageText.setFill(Color.web("#34495e"));
            messageText.setTextAlignment(TextAlignment.CENTER);

            // Créer la barre de boutons
            HBox buttonBar = new HBox(20);
            buttonBar.setAlignment(Pos.CENTER);

            // Bouton Annuler
            Button cancelButton = new Button("Annuler");
            cancelButton.setPrefWidth(120);
            cancelButton.setPrefHeight(40);
            cancelButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-background-radius: 20;");

            // Effet de survol pour le bouton Annuler
            cancelButton.setOnMouseEntered(e -> {
                cancelButton.setStyle("-fx-background-color: #dfe6e9; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-background-radius: 20;");
            });

            cancelButton.setOnMouseExited(e -> {
                cancelButton.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-background-radius: 20;");
            });

            // Action du bouton Annuler
            cancelButton.setOnAction(e -> {
                // Animation de sortie
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dialogRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);

                ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), dialogRoot);
                scaleOut.setFromX(1.0);
                scaleOut.setFromY(1.0);
                scaleOut.setToX(0.8);
                scaleOut.setToY(0.8);

                ParallelTransition closeTransition = new ParallelTransition(fadeOut, scaleOut);
                closeTransition.setOnFinished(evt -> dialogStage.close());
                closeTransition.play();
            });

            // Bouton Supprimer
            Button deleteButton = new Button("Supprimer");
            deleteButton.setPrefWidth(120);
            deleteButton.setPrefHeight(40);
            deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");

            // Effet de survol pour le bouton Supprimer
            deleteButton.setOnMouseEntered(e -> {
                deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");
            });

            deleteButton.setOnMouseExited(e -> {
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");
            });

            // Animation de pulsation pour le bouton Supprimer
            Timeline pulseTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(deleteButton.scaleXProperty(), 1)),
                    new KeyFrame(Duration.ZERO, new KeyValue(deleteButton.scaleYProperty(), 1)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(deleteButton.scaleXProperty(), 1.05)),
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(deleteButton.scaleYProperty(), 1.05)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(deleteButton.scaleXProperty(), 1)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(deleteButton.scaleYProperty(), 1))
            );
            pulseTimeline.setCycleCount(Timeline.INDEFINITE);
            pulseTimeline.play();

            // Action du bouton Supprimer
            deleteButton.setOnAction(e -> {
                pulseTimeline.stop(); // Arrêter l'animation de pulsation

                // Effet de clic
                ScaleTransition clickEffect = new ScaleTransition(Duration.millis(100), deleteButton);
                clickEffect.setToX(0.9);
                clickEffect.setToY(0.9);
                clickEffect.setOnFinished(evt -> {
                    // Animation de rotation pour l'icône d'avertissement
                    RotateTransition rotateIcon = new RotateTransition(Duration.millis(500), iconContainer);
                    rotateIcon.setByAngle(360);
                    rotateIcon.setCycleCount(1);

                    // Changement de couleur du cercle
                    FillTransition fillTransition = new FillTransition(Duration.millis(500), warningCircle);
                    fillTransition.setFromValue(Color.web("#ffeeee"));
                    fillTransition.setToValue(Color.web("#e0f7fa"));

                    // Animation de l'icône
                    ParallelTransition iconAnimation = new ParallelTransition(rotateIcon, fillTransition);

                    // Supprimer l'utilisateur
                    try {
                        new UserService().delete(user.getId());
                        String currentUserName = SessionManager.getCurrentUser().getPrenom() + " " + SessionManager.getCurrentUser().getNom();
                        addHistoryEntry(currentUserName + " a supprimé l'utilisateur " + user.getPrenom() + " " + user.getNom(), currentUserName);
                        addNotification("Utilisateur supprimé: " + user.getPrenom() + " " + user.getNom());
                        refreshUserGrid();

                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Changer le message
                    messageText.setText("L'utilisateur a été supprimé avec succès!");
                    messageText.setFill(Color.web("#2ecc71"));

                    // Changer le titre
                    titleText.setText("Suppression réussie");

                    // Masquer les boutons inutiles
                    deleteButton.setVisible(false);
                    cancelButton.setText("Fermer");
                    cancelButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");

                    // Effets de survol pour le bouton Fermer
                    cancelButton.setOnMouseEntered(evt2 -> {
                        cancelButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");
                    });

                    cancelButton.setOnMouseExited(evt2 -> {
                        cancelButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-background-radius: 20;");
                    });

                    // Rafraîchir la liste des utilisateurs
                    refreshUserGrid();

                    // Jouer les animations
                    iconAnimation.play();
                });
                clickEffect.play();

            });

            buttonBar.getChildren().addAll(cancelButton, deleteButton);

            // Assembler la boîte de dialogue
            dialogRoot.getChildren().addAll(iconContainer, titleText, messageText, buttonBar);

            // Créer et configurer la scène
            Scene dialogScene = new Scene(dialogRoot);
            dialogScene.setFill(Color.TRANSPARENT);

            dialogStage.setScene(dialogScene);
            dialogStage.initOwner(scrollPane.getScene().getWindow());

            // Préparer l'animation d'entrée
            dialogRoot.setOpacity(0);
            dialogRoot.setScaleX(0.5);
            dialogRoot.setScaleY(0.5);

            // Afficher la boîte de dialogue
            dialogStage.show();

            // Animation d'entrée
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dialogRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), dialogRoot);
            scaleIn.setFromX(0.5);
            scaleIn.setFromY(0.5);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.setInterpolator(Interpolator.EASE_OUT);

            // Animation de l'icône
            RotateTransition rotateIcon = new RotateTransition(Duration.millis(500), iconContainer);
            rotateIcon.setFromAngle(-30);
            rotateIcon.setToAngle(0);
            rotateIcon.setInterpolator(Interpolator.EASE_OUT);

            // Jouer les animations ensemble
            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, scaleIn, rotateIcon);
            parallelTransition.play();

        } catch (Exception ex) {
            // Fallback en cas d'erreur avec la boîte de dialogue personnalisée
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Supprimer l'utilisateur");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + user.getNom() + " " + user.getPrenom() + " ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    new UserService().delete(user.getId());
                    refreshUserGrid();

                    // Animation de confirmation
                    showSuccessNotification("Utilisateur supprimé", "L'utilisateur a été supprimé avec succès");
                } catch (SQLException e) {
                    showErrorAlert("Erreur de suppression", "Impossible de supprimer l'utilisateur", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Affiche une notification de succès animée
     */
    private void showSuccessNotification(String title, String message) {
        // Créer un toast de notification
        StackPane notificationPane = new StackPane();
        notificationPane.setMaxWidth(350);
        notificationPane.setMaxHeight(100);
        notificationPane.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 10; -fx-padding: 15;");
        notificationPane.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.2)));

        // Conteneur du message
        VBox messageContainer = new VBox(5);
        messageContainer.setAlignment(Pos.CENTER_LEFT);

        // Titre et message
        Text titleText = new Text(title);
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Text messageText = new Text(message);
        messageText.setFill(Color.WHITE);
        messageText.setFont(Font.font("Segoe UI", 14));

        messageContainer.getChildren().addAll(titleText, messageText);

        // Icône de succès
        Circle successIcon = new Circle(15);
        successIcon.setFill(Color.WHITE);

        Text checkmark = new Text("✓");
        checkmark.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        checkmark.setFill(Color.web("#2ecc71"));

        StackPane iconPane = new StackPane(successIcon, checkmark);
        iconPane.setPadding(new Insets(0, 10, 0, 0));

        // Assembler la notification
        HBox notificationContent = new HBox(10);
        notificationContent.setAlignment(Pos.CENTER_LEFT);
        notificationContent.getChildren().addAll(iconPane, messageContainer);

        notificationPane.getChildren().add(notificationContent);

        // Ajouter la notification à la scène
        AnchorPane parent = (AnchorPane) scrollPane.getParent();
        parent.getChildren().add(notificationPane);

        // Positionner la notification en bas à droite
        AnchorPane.setBottomAnchor(notificationPane, 20.0);
        AnchorPane.setRightAnchor(notificationPane, 20.0);

        // Préparer l'animation d'entrée
        notificationPane.setOpacity(0);
        notificationPane.setTranslateY(50);

        // Animation d'entrée
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationPane);
        slideIn.setFromY(50);
        slideIn.setToY(0);

        ParallelTransition showAnimation = new ParallelTransition(fadeIn, slideIn);
        showAnimation.play();

        // Animation de sortie après 3 secondes
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notificationPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationPane);
            slideOut.setFromY(0);
            slideOut.setToY(50);

            ParallelTransition hideAnimation = new ParallelTransition(fadeOut, slideOut);
            hideAnimation.setOnFinished(evt -> parent.getChildren().remove(notificationPane));
            hideAnimation.play();
        });
        delay.play();
    }

    /**
     * Affiche une alerte d'erreur avec animation
     */
    private void showAnimatedErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("error-alert");

        // Effet de secousse
        dialogPane.setTranslateX(0);

        alert.setOnShown(event -> {
            // Animation de secousse
            Timeline shakeTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(dialogPane.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(50), new KeyValue(dialogPane.translateXProperty(), -10)),
                    new KeyFrame(Duration.millis(100), new KeyValue(dialogPane.translateXProperty(), 10)),
                    new KeyFrame(Duration.millis(150), new KeyValue(dialogPane.translateXProperty(), -10)),
                    new KeyFrame(Duration.millis(200), new KeyValue(dialogPane.translateXProperty(), 10)),
                    new KeyFrame(Duration.millis(250), new KeyValue(dialogPane.translateXProperty(), -10)),
                    new KeyFrame(Duration.millis(300), new KeyValue(dialogPane.translateXProperty(), 0))
            );
            shakeTimeline.play();
        });

        alert.showAndWait();
    }

    // Ajouter ces champs FXML au contrôleur
    @FXML
    private Button statsButton;
    @FXML
    private MenuButton sortButton;
    @FXML
    private MenuItem sortByNameAsc;
    @FXML
    private MenuItem sortByNameDesc;
    @FXML
    private MenuItem sortByRole;
    @FXML
    private MenuItem sortByGender;
    @FXML
    private StackPane statsPopup;
    @FXML
    private StackPane chartContainer;
    @FXML
    private Text malePercentText;
    @FXML
    private Text femalePercentText;
    @FXML
    private Text totalUsersText;
    @FXML
    private Button closeStatsBtn;

    // Variables pour les statistiques et le tri
    private List<User> originalUserList;
    private double malePercent = 0;
    private double femalePercent = 0;
    private int totalUsers = 0;
    private int maleCount = 0;
    private int femaleCount = 0;

    /**
     * Configurer les boutons de statistiques et de tri
     * (À ajouter à la méthode initialize)
     */
    private void setupStatsAndSortButtons() {
        // Configurer le bouton de statistiques
        setupStatsButton();

        // Configurer le popup des statistiques
        setupStatsPopup();

        // Charger les données originales pour le tri
        try {
            originalUserList = new UserService().select();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configurer le bouton de statistiques avec des animations
     */
    private void setupStatsButton() {
        // Animation du bouton statistiques au survol
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), statsButton);
        scaleIn.setToX(1.05);
        scaleIn.setToY(1.05);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), statsButton);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        statsButton.setOnMouseEntered(e -> scaleIn.play());
        statsButton.setOnMouseExited(e -> scaleOut.play());
    }

    /**
     * Configurer le popup des statistiques
     */
    private void setupStatsPopup() {
        // Rendre le popup invisible initialement
        statsPopup.setVisible(false);
        statsPopup.setOpacity(0);
        statsPopup.setScaleX(0.8);
        statsPopup.setScaleY(0.8);

        // Effet de survol pour le bouton de fermeture
        closeStatsBtn.setOnMouseEntered(e -> {
            closeStatsBtn.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 15; -fx-padding: 0;");
            // Changer la couleur du X en blanc
            Node graphic = closeStatsBtn.getGraphic();
            if (graphic instanceof Text) {
                ((Text) graphic).setFill(Color.WHITE);
            }
        });

        closeStatsBtn.setOnMouseExited(e -> {
            closeStatsBtn.setStyle("-fx-background-color: #f1f2f6; -fx-background-radius: 15; -fx-padding: 0;");
            // Remettre la couleur du X en gris
            Node graphic = closeStatsBtn.getGraphic();
            if (graphic instanceof Text) {
                ((Text) graphic).setFill(Color.web("#95a5a6"));
            }
        });
    }

    /**
     * Gérer l'affichage des statistiques
     */
    @FXML
    private void handleShowStats() {
        // Calculer les statistiques
        calculateGenderStats();

        // Créer le graphique en donut
        createDonutChart();

        // Mettre à jour les textes
        updateStatsText();

        // Afficher le popup avec animation
        statsPopup.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), statsPopup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), statsPopup);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(fadeIn, scaleIn);
        parallel.play();
    }

    /**
     * Fermer le popup des statistiques
     */
    @FXML
    private void handleCloseStats() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), statsPopup);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), statsPopup);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);

        ParallelTransition parallel = new ParallelTransition(fadeOut, scaleOut);
        parallel.setOnFinished(e -> statsPopup.setVisible(false));
        parallel.play();
    }

    /**
     * Calculer les statistiques de genre
     */
    private void calculateGenderStats() {
        try {
            // Récupérer tous les utilisateurs
            List<User> allUsers = new UserService().select();
            totalUsers = allUsers.size();

            // Compter le nombre d'hommes et de femmes
            maleCount = 0;
            femaleCount = 0;

            for (User user : allUsers) {
                if (user.getGenre() != null) {
                    if (user.getGenre().equalsIgnoreCase("homme") ||
                            user.getGenre().equalsIgnoreCase("male") ||
                            user.getGenre().equalsIgnoreCase("m")) {
                        maleCount++;
                    } else if (user.getGenre().equalsIgnoreCase("femme") ||
                            user.getGenre().equalsIgnoreCase("female") ||
                            user.getGenre().equalsIgnoreCase("f")) {
                        femaleCount++;
                    }
                }
            }

            // Calculer les pourcentages
            if (totalUsers > 0) {
                malePercent = (double) maleCount / totalUsers * 100;
                femalePercent = (double) femaleCount / totalUsers * 100;
            } else {
                malePercent = 0;
                femalePercent = 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Impossible de calculer les statistiques", e.getMessage());
        }
    }

    /**
     * Mettre à jour les textes des statistiques
     */
    private void updateStatsText() {
        // Formater les pourcentages avec 1 décimale
        DecimalFormat df = new DecimalFormat("#.#");
        malePercentText.setText(df.format(malePercent) + "%");
        femalePercentText.setText(df.format(femalePercent) + "%");

        // Mettre à jour le nombre total d'utilisateurs
        totalUsersText.setText(String.valueOf(totalUsers));
    }

    /**
     * Créer un graphique en donut pour les statistiques de genre
     */
    private void createDonutChart() {
        // Nettoyer le conteneur de graphique avant d'ajouter un nouveau
        chartContainer.getChildren().clear();

        // Créer les arcs pour le graphique en donut
        Arc maleArc = new Arc();
        maleArc.setCenterX(90);
        maleArc.setCenterY(90);
        maleArc.setRadiusX(75);
        maleArc.setRadiusY(75);
        maleArc.setStartAngle(0);
        maleArc.setLength(malePercent * 3.6); // 3.6 = 360/100 pour convertir le pourcentage en degrés
        maleArc.setType(ArcType.ROUND);
        maleArc.setFill(Color.web("#3498db"));

        Arc femaleArc = new Arc();
        femaleArc.setCenterX(90);
        femaleArc.setCenterY(90);
        femaleArc.setRadiusX(75);
        femaleArc.setRadiusY(75);
        femaleArc.setStartAngle(malePercent * 3.6);
        femaleArc.setLength(femalePercent * 3.6);
        femaleArc.setType(ArcType.ROUND);
        femaleArc.setFill(Color.web("#e74c3c"));

        // Ajouter un effet d'ombre subtil aux arcs
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(2);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));

        maleArc.setEffect(dropShadow);
        femaleArc.setEffect(dropShadow);

        // Créer un cercle blanc au centre pour l'effet "donut"
        Circle innerCircle = new Circle(90, 90, 50);
        innerCircle.setFill(Color.WHITE);

        // Ajouter tous les éléments au conteneur
        Group donutChart = new Group(maleArc, femaleArc, innerCircle);

        // Ajouter une animation de rotation au démarrage
        RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), donutChart);
        rotateTransition.setByAngle(360);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);
        rotateTransition.play();

        chartContainer.getChildren().add(donutChart);
    }

    @FXML
    private void handleSortByNameAsc(ActionEvent event) {
        sortButton.setText("Nom (A-Z)");
        sortUsers(Comparator.comparing(User::getNom).thenComparing(User::getPrenom));
    }

    @FXML
    private void handleSortByNameDesc(ActionEvent event) {
        sortButton.setText("Nom (Z-A)");
        sortUsers(Comparator.comparing(User::getNom).thenComparing(User::getPrenom).reversed());
    }

    @FXML
    private void handleSortByRole(ActionEvent event) {
        sortButton.setText("Rôle");
        sortUsers(Comparator.comparing(User::getRole).thenComparing(User::getNom));
    }

    @FXML
    private void handleSortByGender(ActionEvent event) {
        sortButton.setText("Genre");
        sortUsers(Comparator.comparing(User::getGenre, Comparator.nullsLast(String::compareTo))
                .thenComparing(User::getNom));
    }

    private void sortUsers(Comparator<User> comparator) {
        if (originalUserList == null || originalUserList.isEmpty()) {
            return;
        }

        // Créer une nouvelle liste triée
        List<User> sortedList = new ArrayList<>(originalUserList);
        sortedList.sort(comparator);

        // Animer le tri
        animateSorting(sortedList);
    }


    /**
     * Animer le tri des utilisateurs
     */
    private void animateSorting(List<User> sortedList) {
        // Animation de fondu
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), userGrid);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        fadeOut.setOnFinished(e -> {
            // Afficher la nouvelle liste triée
            displayUsersWithAnimation(sortedList, SessionManager.getCurrentUser());

            // Animation de retour
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), userGrid);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
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

    /**
     * Setup animation effects for the navigation bar elements
     */
    private void setupNavbarAnimations() {
        // Add hover effect to notification indicator
        StackPane notificationIndicator = (StackPane) scrollPane.getScene().lookup("#notificationIndicator");
        if (notificationIndicator != null) {
            notificationIndicator.setOnMouseEntered(e -> {
                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), notificationIndicator);
                scaleUp.setToX(1.1);
                scaleUp.setToY(1.1);
                scaleUp.play();
            });

            notificationIndicator.setOnMouseExited(e -> {
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), notificationIndicator);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });
        }

        // Add subtle pulse animation to notification dot if present
        Circle notificationDot = (Circle) scrollPane.getScene().lookup("#notificationDot");
        if (notificationDot != null) {
            Timeline pulsate = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(notificationDot.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1.5), new KeyValue(notificationDot.opacityProperty(), 0.4)),
                    new KeyFrame(Duration.seconds(3.0), new KeyValue(notificationDot.opacityProperty(), 1.0))
            );
            pulsate.setCycleCount(Timeline.INDEFINITE);
            pulsate.play();
        }
    }

    /**
     * Improve the display of the current user info in the navbar
     */
    private void enhanceCurrentUserDisplay() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
           // userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

            // Set user initials for avatar
            Text userInitials = (Text) scrollPane.getScene().lookup("#userInitials");
            if (userInitials != null && currentUser.getPrenom() != null && currentUser.getNom() != null) {
                String initials = "";
                if (!currentUser.getPrenom().isEmpty()) {
                    initials += currentUser.getPrenom().substring(0, 1);
                }
                if (!currentUser.getNom().isEmpty()) {
                    initials += currentUser.getNom().substring(0, 1);
                }
                userInitials.setText(initials.toUpperCase());
            }

            // You could also add a random color generator for user avatars based on user ID
            // This gives each user a unique avatar color
            Circle userAvatar = (Circle) scrollPane.getScene().lookup("#userAvatar");
            if (userAvatar != null) {
                // Generate consistent color based on user ID
                int userId = currentUser.getId();
                Color avatarColor = Color.hsb((userId * 85) % 360, 0.5, 0.8);
                userAvatar.setFill(avatarColor);
            }
        }
    }
    private void setupNotificationsAndHistory() {
        // S'assurer que les badges et compteurs sont bien initialisés
        //notificationCountBadge.setVisible(false);
        //notificationCount.setVisible(false);

        // Vérifier que les popups sont correctement positionnés
        Platform.runLater(() -> {
            // S'assurer que les popups sont des enfants directs de l'AnchorPane principal
            AnchorPane parent = (AnchorPane) scrollPane.getParent();

            // Vérifier et ajuster le positionnement des popups si nécessaire
            if (notificationsPopup.getParent() != parent) {
                // Si le popup est mal placé, le déplacer au bon endroit
                parent.getChildren().add(notificationsPopup);
                AnchorPane.setTopAnchor(notificationsPopup, 100.0);
                AnchorPane.setRightAnchor(notificationsPopup, 20.0);
            }

            if (historyPopup.getParent() != parent) {
                // Si le popup est mal placé, le déplacer au bon endroit
                parent.getChildren().add(historyPopup);
                AnchorPane.setTopAnchor(historyPopup, 100.0);
                AnchorPane.setRightAnchor(historyPopup, 20.0);
            }
        });

        // Mettre à jour le compteur de notifications
        updateNotificationCount();
    }
    private void loadHistoryFromStorage() {
        // Cette méthode pourrait charger l'historique depuis une base de données
        // Pour l'instant, on laisse vide pour une implémentation future
    }    /**
     * Ajouter une entrée d'historique
     */
    public void addNotification(String message) {
        notifications.add(message);
        updateNotificationCount();

        // Option: sauvegarder dans une base de données (implémentation future)
    }

    /**
     * Ajoute une entrée dans l'historique et sauvegarde dans la base de données
     */
    public void addHistoryEntry(String action, String utilisateur) {
        ActionHistorique entry = new ActionHistorique(action, new Date(), utilisateur);
        historyEntries.add(entry);

        // Option: sauvegarder dans une base de données (implémentation future)
    }


    /**
     * Mettre à jour le compteur de notifications
     */
    private void updateNotificationCount() {
        int count = notifications.size();
        //notificationCount.setText(String.valueOf(count));

        // Afficher/masquer le badge selon s'il y a des notifications
        //notificationCountBadge.setVisible(count > 0);
     //   notificationCount.setVisible(count > 0);

        // Animation si nouvelles notifications
       /* if (count > 0) {
            Timeline pulse = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(notificationCountBadge.scaleXProperty(), 1)),
                    new KeyFrame(Duration.ZERO, new KeyValue(notificationCountBadge.scaleYProperty(), 1)),
                    new KeyFrame(Duration.millis(200), new KeyValue(notificationCountBadge.scaleXProperty(), 1.2)),
                    new KeyFrame(Duration.millis(200), new KeyValue(notificationCountBadge.scaleYProperty(), 1.2)),
                    new KeyFrame(Duration.millis(400), new KeyValue(notificationCountBadge.scaleXProperty(), 1)),
                    new KeyFrame(Duration.millis(400), new KeyValue(notificationCountBadge.scaleYProperty(), 1))
            );
            pulse.setCycleCount(3);
            pulse.play();
        }*/

    }

    /**
     * Afficher le popup des notifications
     */
    @FXML
    private void handleShowNotifications() {
        // Masquer l'historique s'il est visible
        if (historyPopup.isVisible()) {
            hidePopup(historyPopup);
        }

        // Préparer le contenu des notifications
        notificationsContainer.getChildren().clear();

        // Titre et bouton "Marquer tout comme lu"
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Notifications");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAllReadBtn = new Button("Marquer tout comme lu");
        markAllReadBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        markAllReadBtn.setOnAction(e -> {
            notifications.clear();
            updateNotificationCount();
            handleShowNotifications(); // Rafraîchir la vue
        });

        header.getChildren().addAll(title, spacer, markAllReadBtn);

        // Ajouter le header
        notificationsContainer.getChildren().add(header);
        notificationsContainer.getChildren().add(new Separator());

        // Si aucune notification
        if (notifications.isEmpty()) {
            Text emptyText = new Text("Aucune notification");
            emptyText.setStyle("-fx-font-size: 14px; -fx-fill: #95a5a6;");
            emptyText.setWrappingWidth(350);

            notificationsContainer.getChildren().add(emptyText);
        } else {
            // Ajouter chaque notification
            for (String notification : notifications) {
                HBox notifBox = createNotificationItem(notification);
                notificationsContainer.getChildren().add(notifBox);
            }
        }

        // Afficher le popup
        togglePopupVisibility(notificationsPopup);
    }

    /**
     * Afficher le popup de l'historique
     */
    @FXML
    private void handleShowHistory() {
        // Masquer les notifications si visibles
        if (notificationsPopup.isVisible()) {
            hidePopup(notificationsPopup);
        }

        // Préparer le contenu de l'historique
        historyContainer.getChildren().clear();

        // Titre et bouton "Effacer l'historique"
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("Historique des actions");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button clearHistoryBtn = new Button("Effacer l'historique");
        clearHistoryBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        clearHistoryBtn.setOnAction(e -> {
            historyEntries.clear();
            handleShowHistory(); // Rafraîchir la vue
        });

        header.getChildren().addAll(title, spacer, clearHistoryBtn);

        // Ajouter le header
        historyContainer.getChildren().add(header);
        historyContainer.getChildren().add(new Separator());

        // Si aucune entrée d'historique
        if (historyEntries.isEmpty()) {
            Text emptyText = new Text("Aucune action enregistrée");
            emptyText.setStyle("-fx-font-size: 14px; -fx-fill: #95a5a6;");
            emptyText.setWrappingWidth(350);

            historyContainer.getChildren().add(emptyText);
        } else {
            // Ajouter chaque entrée d'historique
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(300);

            VBox entriesBox = new VBox(10);
            entriesBox.setPadding(new Insets(5, 0, 5, 0));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            for (ActionHistorique entry : historyEntries) {
                String formattedDate = dateFormat.format(entry.getDate());
                HBox historyEntryBox = createHistoryItem(entry.getAction(), formattedDate);
                entriesBox.getChildren().add(historyEntryBox);
            }

            scrollPane.setContent(entriesBox);
            historyContainer.getChildren().add(scrollPane);
        }

        // Afficher le popup
        togglePopupVisibility(historyPopup);
    }

    /**
     * Créer un élément de notification
     */
    private HBox createNotificationItem(String message) {
        HBox notifBox = new HBox(10);
        notifBox.setPadding(new Insets(10));
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
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #95a5a6; -fx-font-size: 16px; -fx-font-weight: bold;");

        deleteBtn.setOnMouseEntered(e ->
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 50%;")
        );

        deleteBtn.setOnMouseExited(e ->
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #95a5a6; -fx-font-size: 16px; -fx-font-weight: bold;")
        );

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
    private HBox createHistoryItem(String action, String date) {
        HBox historyBox = new HBox(10);
        historyBox.setPadding(new Insets(10));
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

        Text actionText = new Text(action);
        actionText.setStyle("-fx-font-size: 14px;");
        actionText.setWrappingWidth(270);

        Text dateText = new Text(date);
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
     * Afficher un popup avec animation
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
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(fadeIn, scaleIn);
        parallel.play();
    }

    /**
     * Masquer un popup avec animation
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

    private String currentBackgroundPath = null;

    /**
     * Initialisation des éléments de personnalisation
     * Ajouter cette partie à la méthode initialize()
     */
    private void setupCustomization() {
        // Initialiser le popup à invisible
        customizePopup.setVisible(false);
        customizePopup.setOpacity(0);

        // Configurer le slider d'opacité
        opacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Mettre à jour l'opacité de l'image
            backgroundImage.setOpacity(newVal.doubleValue());

            // Mettre à jour le texte de pourcentage
            int percentage = (int) (newVal.doubleValue() * 100);
            opacityValue.setText(percentage + "%");
        });

        // Configurer l'image d'arrière-plan responsive
        setupResponsiveBackground();

        // Charger l'arrière-plan sauvegardé
        loadSavedBackground();
    }
    /**
     * Chargement de l'arrière-plan enregistré dans les préférences
     */
    private void loadSavedBackground() {
        // Récupérer les préférences utilisateur
        Preferences prefs = Preferences.userNodeForPackage(AfficherUserController.class);

        // Récupérer le chemin de l'image
        String savedPath = prefs.get("background_image", null);

        // Récupérer l'opacité
        double opacity = prefs.getDouble("background_opacity", 0.2);

        if (savedPath != null) {
            try {
                // Charger l'image
                Image image = new Image(new File(savedPath).toURI().toString(), true); // true pour charger en arrière-plan
                backgroundImage.setImage(image);
                currentBackgroundPath = savedPath;

                // Ajuster l'opacité
                backgroundImage.setOpacity(opacity);
                opacitySlider.setValue(opacity);

                // La taille sera ajustée via setupResponsiveBackground()
            } catch (Exception e) {
                System.out.println("Erreur lors du chargement de l'arrière-plan: " + e.getMessage());
            }
        }
    }

    /**
     * Méthode pour ouvrir le popup de personnalisation
     */
    @FXML
    private void handleCustomizeBackground() {
        // Afficher le popup avec animation
        customizePopup.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), customizePopup);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), customizePopup);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        scaleIn.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallel = new ParallelTransition(fadeIn, scaleIn);
        parallel.play();
    }

    /**
     * Méthode pour fermer le popup de personnalisation
     */
    @FXML
    private void handleCloseCustomize() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), customizePopup);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), customizePopup);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.9);
        scaleOut.setToY(0.9);

        ParallelTransition parallel = new ParallelTransition(fadeOut, scaleOut);
        parallel.setOnFinished(e -> customizePopup.setVisible(false));
        parallel.play();

        // Sauvegarder les préférences
        saveBackgroundPreference();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image d'arrière-plan");

        // Configurer les filtres pour images
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Afficher le sélecteur de fichier
        Stage stage = (Stage) customizePopup.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Charger l'image
                Image image = new Image(selectedFile.toURI().toString(), true); // true pour charger en arrière-plan
                backgroundImage.setImage(image);
                currentBackgroundPath = selectedFile.getAbsolutePath();

                // Ajuster la taille de l'image
                updateBackgroundSize();

                // Définir l'opacité
                backgroundImage.setOpacity(opacitySlider.getValue());

                // Animation de transition
                FadeTransition fade = new FadeTransition(Duration.millis(500), backgroundImage);
                fade.setFromValue(0);
                fade.setToValue(opacitySlider.getValue());
                fade.play();

                // Afficher une notification
                showToastNotification("Arrière-plan changé", "L'image a été appliquée avec succès", "✓", "#2ecc71");
            } catch (Exception e) {
                showAnimatedErrorAlert("Erreur", "Impossible de charger l'image", e.getMessage());
            }
        }
    }

    /**
     * Méthode pour réinitialiser l'arrière-plan
     */
    @FXML
    private void handleResetBackground() {
        // Effacer l'image
        backgroundImage.setImage(null);
        currentBackgroundPath = null;

        // Réinitialiser l'opacité
        opacitySlider.setValue(0.2);

        // Sauvegarder les préférences
        saveBackgroundPreference();

        // Afficher une notification
        showToastNotification("Arrière-plan réinitialisé", "Les paramètres par défaut ont été restaurés", "✓", "#3498db");
    }

    /**
     * Sauvegarder les préférences d'arrière-plan
     */
    private void saveBackgroundPreference() {
        Preferences prefs = Preferences.userNodeForPackage(AfficherUserController.class);

        if (currentBackgroundPath != null) {
            prefs.put("background_image", currentBackgroundPath);
        } else {
            prefs.remove("background_image");
        }

        prefs.putDouble("background_opacity", opacitySlider.getValue());
    }
    private void showToastNotification(String title, String message, String iconText, String color) {
        // Récupérer les éléments FXML de la notification
        StackPane toastNotification = (StackPane) scrollPane.getScene().lookup("#toastNotification");
        Text toastTitle = (Text) scrollPane.getScene().lookup("#toastTitle");
        Text toastMessage = (Text) scrollPane.getScene().lookup("#toastMessage");
        Text toastIcon = (Text) scrollPane.getScene().lookup("#toastIcon");
        Circle iconCircle = (Circle) ((StackPane) toastIcon.getParent()).getChildrenUnmodifiable().get(0);

        if (toastNotification != null && toastTitle != null && toastMessage != null && toastIcon != null) {
            // Mettre à jour le contenu
            toastTitle.setText(title);
            toastMessage.setText(message);
            toastIcon.setText(iconText);
            iconCircle.setStyle("-fx-fill: " + color + ";");

            // Afficher avec animation
            toastNotification.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastNotification);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastNotification);
            slideIn.setFromY(50);
            slideIn.setToY(0);

            ParallelTransition showAnimation = new ParallelTransition(fadeIn, slideIn);
            showAnimation.play();

            // Cacher après quelques secondes
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastNotification);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);

                TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastNotification);
                slideOut.setFromY(0);
                slideOut.setToY(50);

                ParallelTransition hideAnimation = new ParallelTransition(fadeOut, slideOut);
                hideAnimation.setOnFinished(evt -> toastNotification.setVisible(false));
                hideAnimation.play();
            });
            delay.play();
        }
    }
    private void setupResponsiveBackground() {
        // Récupérer la scène lorsqu'elle est disponible
        Platform.runLater(() -> {
            Scene scene = backgroundImage.getScene();
            if (scene != null) {
                // Ajuster la taille de l'image lorsque la fenêtre est redimensionnée
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    updateBackgroundSize();
                });

                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    updateBackgroundSize();
                });

                // Configuration initiale
                updateBackgroundSize();
            }
        });
    }
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
    @FXML
    void handleShowTranslation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Translation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            // Afficher une notification de succès
         //   showToast("Info", "Service de traduction ouvert", true);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'ouverture du service de traduction: " + e.getMessage());
            e.printStackTrace();

            // Afficher une notification d'erreur
           // showToast("Erreur", "Impossible d'ouvrir le service de traduction", false);
        }
    }
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
    private Button frontButton;

}