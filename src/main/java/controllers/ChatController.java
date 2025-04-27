package controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import models.Message;
import models.User;
import service.ChatService;
import service.UserService;
import utils.AudioPlayer;
import utils.AudioRecorder;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
public class ChatController {
    private double xOffset = 0;
    private double yOffset = 0;
    @FXML
    private Button emojiButton;

    @FXML private Button voiceButton;
    @FXML private HBox recordingBox;
    @FXML private Circle recordingIndicator;
    @FXML private Label recordingTimeLabel;
    @FXML private Button stopRecordingButton;
    @FXML private Button cancelRecordingButton;

    private AudioRecorder audioRecorder;
    private AudioPlayer audioPlayer;
    private Timeline recordingTimeline;
    private File tempAudioFile;
    private boolean isRecording = false;
    private Map<Integer, AudioPlayer> audioPlayers = new HashMap<>();

    private VBox emojiPickerBox;
    private boolean emojiPickerVisible = false;
    @FXML
    private ListView<HBox> messageListView;

    @FXML
    private StackPane messageListViewContainer;

    @FXML
    private StackPane dropOverlay;

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendButton;

    @FXML
    private ImageView sendIcon;

    @FXML
    private Button imageButton;

    @FXML
    private Button fileButton;

    @FXML
    private HBox attachmentPreviewBox;

    @FXML
    private Label attachmentLabel;

    @FXML
    private Button removeAttachmentButton;

    private final ChatService chatService = new ChatService();
    private User currentUser;
    private Message messageBeingEdited = null;

    // Variables pour les pièces jointes
    private File selectedAttachment = null;
    private String attachmentType = null;
    @FXML private ListView<HBox> mentionSuggestionsList;
    @FXML private VBox mentionSuggestionsBox;
    private List<User> allUsers = new ArrayList<>();
    private boolean mentionMode = false;
    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageTextField.setPromptText("Veuillez vous connecter...");
            messageTextField.setDisable(true);
            sendButton.setDisable(true);
            imageButton.setDisable(true);
            fileButton.setDisable(true);
            return;
        }
        // Configurer la zone de suggestions de mentions
        mentionSuggestionsBox.setVisible(false);
        mentionSuggestionsBox.setManaged(false);

        // Configurer l'écouteur de texte pour détecter le symbole @
        messageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            handleMentionInput(oldValue, newValue);
        });

        // Charger tous les utilisateurs
        try {
            UserService userService = new UserService();
            allUsers = userService.select();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Configurer la cell factory
        messageListView.setCellFactory(lv -> new ListCell<HBox>() {
            @Override
            protected void updateItem(HBox item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    setGraphic(item);
                }
            }
        });

        // Configurer le drag and drop
        setupDragAndDrop();

        // Initialiser le sélecteur d'emojis
        initializeEmojiPicker();

        // Configurer le support des emojis
        configureEmojiSupport();

        try {
            loadMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> messageTextField.requestFocus());
        // Initialiser les outils audio
        audioRecorder = new AudioRecorder();
        audioPlayer = new AudioPlayer();

        // Configurer l'animation de l'indicateur d'enregistrement
        initializeRecordingAnimation();

        // Masquer la zone d'enregistrement au début
        recordingBox.setVisible(false);
        recordingBox.setManaged(false);
    }


    private void setupDragAndDrop() {
        // Make sure the overlay is initially hidden
        dropOverlay.setVisible(false);
        dropOverlay.setOpacity(0);

        // Define a common handler for all components
        EventHandler<DragEvent> dragOverHandler = event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                event.consume();
            }
        };

        EventHandler<DragEvent> dragEnteredHandler = event -> {
            if (event.getDragboard().hasFiles()) {
                updateDropOverlayText(event);
                // Show the overlay with animation
                dropOverlay.setVisible(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), dropOverlay);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();

                // Add style class to indicate drop target
                Node source = (Node) event.getSource();
                source.getStyleClass().add("drag-over");
                event.consume();
            }
        };

        EventHandler<DragEvent> dragExitedHandler = event -> {
            // Only hide if we're not still over a valid drop target
            boolean stillOverValidTarget = false;

            // Check if pointer is still within the messageListViewContainer bounds
            if (messageListViewContainer.getBoundsInLocal().contains(
                    messageListViewContainer.sceneToLocal(event.getSceneX(), event.getSceneY()))) {
                stillOverValidTarget = true;
            }

            if (!stillOverValidTarget) {
                // Animate hiding the overlay
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dropOverlay);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> dropOverlay.setVisible(false));
                fadeOut.play();

                // Remove style class
                Node source = (Node) event.getSource();
                source.getStyleClass().remove("drag-over");
            }
            event.consume();
        };

        EventHandler<DragEvent> dragDroppedHandler = event -> {
            boolean success = false;
            Dragboard db = event.getDragboard();

            if (db.hasFiles()) {
                handleMultiFilesDrop(db.getFiles());
                success = true;
            }

            // Hide overlay with animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dropOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> dropOverlay.setVisible(false));
            fadeOut.play();

            // Remove style class from all potential targets
            messageListViewContainer.getStyleClass().remove("drag-over");
            messageListView.getStyleClass().remove("drag-over");
            messageTextField.getStyleClass().remove("drag-over");

            event.setDropCompleted(success);
            event.consume();
        };

        // Apply handlers to the container
        messageListViewContainer.setOnDragOver(dragOverHandler);
        messageListViewContainer.setOnDragEntered(dragEnteredHandler);
        messageListViewContainer.setOnDragExited(dragExitedHandler);
        messageListViewContainer.setOnDragDropped(dragDroppedHandler);

        // Apply handlers to the list view
        messageListView.setOnDragOver(dragOverHandler);
        messageListView.setOnDragEntered(dragEnteredHandler);
        messageListView.setOnDragExited(dragExitedHandler);
        messageListView.setOnDragDropped(dragDroppedHandler);

        // Apply handlers to the text field
        messageTextField.setOnDragOver(dragOverHandler);
        messageTextField.setOnDragEntered(dragEnteredHandler);
        messageTextField.setOnDragExited(dragExitedHandler);
        messageTextField.setOnDragDropped(dragDroppedHandler);
    }

    private void handleDragOver(DragEvent event) {
        // Accepter seulement si c'est un fichier
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".gif") ||
                name.endsWith(".bmp");
    }

    @FXML
    private void handleSendMessage() {
        String message = messageTextField.getText().trim();
        boolean hasText = !message.isEmpty();
        boolean hasAttachment = selectedAttachment != null;


        // Vérifier s'il y a au moins un message ou une pièce jointe
        if (hasText || hasAttachment) {
            // Animation d'envoi
            animateSendButton(() -> {
                try {
                    if (hasText) {
                        // Trouver les utilisateurs mentionnés
                        Pattern mentionPattern = Pattern.compile("@([A-Za-zÀ-ÖØ-öø-ÿ]+ [A-Za-zÀ-ÖØ-öø-ÿ]+)");
                        Matcher matcher = mentionPattern.matcher(message);

                        while (matcher.find()) {
                            String mentionedName = matcher.group(1); // Prénom Nom sans @

                            // Chercher l'utilisateur mentionné
                            for (User user : allUsers) {
                                String fullName = user.getPrenom() + " " + user.getNom();
                                if (fullName.equals(mentionedName)) {
                                    // Envoyer une notification à l'utilisateur mentionné
                                    sendMentionNotification(user, currentUser.getPrenom() + " " + currentUser.getNom());
                                    break;
                                }
                            }
                        }
                    }
                    if (messageBeingEdited != null) {
                        // Mode édition (sans pièce jointe pour l'instant)
                        chatService.updateMessage(messageBeingEdited.getId(), message);
                        exitEditMode();
                    } else {
                        // Nouveau message
                        if (hasAttachment) {
                            // Envoyer avec pièce jointe
                            chatService.sendMessageWithAttachment(currentUser, message, selectedAttachment, attachmentType);
                            // Réinitialiser la pièce jointe
                            clearAttachment();
                        } else {
                            // Envoyer texte seulement
                            chatService.sendMessage(currentUser, message);
                        }

                        // Ajouter la notification (nouveau code)
                        String senderName = currentUser.getPrenom() + " " + currentUser.getNom();
                        sendMessageNotification(senderName);
                    }
                    messageTextField.clear();
                    loadMessages();

                    // Focus automatique sur le TextField après envoi
                    messageTextField.requestFocus();
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    showAlert("Erreur", "Échec de l'opération: " + e.getMessage());
                }
            });
        }
    }
    private void sendMentionNotification(User mentionedUser, String senderName) {
        // Chercher le contrôleur AfficherUserController pour l'utilisateur connecté
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage) {
                Scene scene = ((Stage) window).getScene();
                if (scene != null && scene.getRoot() != null) {
                    AfficherUserController controller = findController(scene.getRoot());
                    if (controller != null) {
                        controller.addNotification("Vous avez été mentionné par " + senderName);
                        break;
                    }
                }
            }
        }
    }
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        Stage stage = (Stage) imageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedAttachment = selectedFile;
            attachmentType = Message.AttachmentType.IMAGE.toString();
            showAttachmentPreview(selectedFile.getName(), "Image");
        }
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        Stage stage = (Stage) fileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            selectedAttachment = selectedFile;
            attachmentType = Message.AttachmentType.FILE.toString();
            showAttachmentPreview(selectedFile.getName(), "Fichier");
        }
    }

    @FXML
    private void handleRemoveAttachment() {
        clearAttachment();
    }

    private void showAttachmentPreview(String fileName, String type) {
        attachmentLabel.setText(type + " sélectionné: " + fileName);
        attachmentPreviewBox.setVisible(true);
        attachmentPreviewBox.setManaged(true);
    }

    private void clearAttachment() {
        selectedAttachment = null;
        attachmentType = null;
        attachmentPreviewBox.setVisible(false);
        attachmentPreviewBox.setManaged(false);
    }

    private void exitEditMode() {
        messageBeingEdited = null;
        messageTextField.clear();
        sendButton.getStyleClass().remove("edit-mode");
        messageTextField.setPromptText("Écrivez votre message...");
    }

    // Trouvez cette méthode dans votre ChatController.java et remplacez la partie
// qui crée les options de message (boutons Modifier/Supprimer)

    // Modifiez la méthode loadMessages pour inclure la gestion des messages vocaux:
    private void loadMessages() throws SQLException {
        messageListView.getItems().clear();
        List<Message> messages = chatService.getAllMessages();

        for (Message msg : messages) {
            // Création du conteneur HBox pour l'alignement
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5, 10, 5, 10));

            // Si c'est le message de l'utilisateur courant, aligner à droite avec options
            if (msg.getEmail().equals(currentUser.getEmail())) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);

                // Créer les contrôles pour le message
                VBox messageVBox = new VBox(5);

                // Ajouter le contenu du message (texte)
                if (msg.getContenu() != null && !msg.getContenu().isEmpty()) {
                    TextFlow textFlow = createMessageBubble(msg);
                    messageVBox.getChildren().add(textFlow);
                }

                // Ajouter la pièce jointe si présente
                if (msg.hasAttachment()) {
                    Node attachmentNode;
                    if (msg.isAudioAttachment()) {
                        attachmentNode = createAudioMessageNode(msg);
                    } else if (msg.isImageAttachment()) {
                        attachmentNode = createAttachmentNode(msg);
                    } else {
                        attachmentNode = createAttachmentNode(msg);
                    }

                    if (attachmentNode != null) {
                        messageVBox.getChildren().add(attachmentNode);
                    }
                }

                // Ajouter les options de modification et suppression
                HBox optionsBox = createMessageOptions(msg);
                messageVBox.getChildren().add(optionsBox);

                messageContainer.getChildren().add(messageVBox);
            } else {
                messageContainer.setAlignment(Pos.CENTER_LEFT);

                // Créer le conteneur pour le message
                VBox vbox = new VBox(5);

                // Ajouter le nom de l'expéditeur
                Text senderName = new Text(msg.getNom() + ":\n");
                senderName.setStyle("-fx-font-weight: bold");
                vbox.getChildren().add(senderName);

                // Ajouter le contenu du message (texte)
                if (msg.getContenu() != null && !msg.getContenu().isEmpty()) {
                    vbox.getChildren().add(createMessageBubble(msg));
                }

                // Ajouter la pièce jointe si présente
                if (msg.hasAttachment()) {
                    Node attachmentNode;
                    if (msg.isAudioAttachment()) {
                        attachmentNode = createAudioMessageNode(msg);
                    } else if (msg.isImageAttachment()) {
                        attachmentNode = createAttachmentNode(msg);
                    } else {
                        attachmentNode = createAttachmentNode(msg);
                    }

                    if (attachmentNode != null) {
                        vbox.getChildren().add(attachmentNode);
                    }
                }

                messageContainer.getChildren().add(vbox);
            }

            messageListView.getItems().add(messageContainer);
        }

        // Scroll to the bottom
        messageListView.scrollTo(messageListView.getItems().size() - 1);
    }

    private Node createAttachmentNode(Message msg) {
        if (msg.isImageAttachment()) {
            try {
                // Créer une vignette pour l'image
                ImageView imageView = new ImageView(new Image("file:" + msg.getAttachmentPath()));
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);

                // Ajouter un effet de survol pour ouvrir l'image en grand
                imageView.setOnMouseClicked(e -> openAttachment(msg));

                // Ajouter un menu contextuel pour télécharger l'image
                ContextMenu contextMenu = new ContextMenu();
                MenuItem downloadItem = new MenuItem("Télécharger");
                downloadItem.setOnAction(event -> downloadAttachment(msg));
                contextMenu.getItems().add(downloadItem);

                imageView.setOnContextMenuRequested(event -> {
                    contextMenu.show(imageView, event.getScreenX(), event.getScreenY());
                });

                return imageView;
            } catch (Exception e) {
                e.printStackTrace();
                // En cas d'erreur, afficher un lien textuel
                return createAttachmentLink(msg);
            }
        } else {
            // Pour les fichiers non-image, créer un lien cliquable
            return createAttachmentLink(msg);
        }
    }

    private Node createAttachmentLink(Message msg) {
        HBox fileBox = new HBox(5);
        fileBox.setAlignment(Pos.CENTER_LEFT);

        // Icône en fonction du type de fichier
        ImageView fileIcon = new ImageView();
        fileIcon.setFitHeight(24);
        fileIcon.setFitWidth(24);

        if (msg.isImageAttachment()) {
            fileIcon.setImage(new Image(getClass().getResourceAsStream("/image/image-icon.png")));
        } else {
            fileIcon.setImage(new Image(getClass().getResourceAsStream("/image/file-icon.png")));
        }

        // Label avec le nom du fichier
        Label fileNameLabel = new Label(msg.getAttachmentName());
        fileNameLabel.setStyle("-fx-text-fill: #0084ff; -fx-underline: true;");

        // Action pour ouvrir le fichier
        fileBox.setOnMouseClicked(e -> openAttachment(msg));
        fileBox.setCursor(javafx.scene.Cursor.HAND);

        // Ajouter un menu contextuel pour télécharger le fichier
        ContextMenu contextMenu = new ContextMenu();
        MenuItem downloadItem = new MenuItem("Télécharger");
        downloadItem.setOnAction(event -> downloadAttachment(msg));
        MenuItem openItem = new MenuItem("Ouvrir");
        openItem.setOnAction(event -> openAttachment(msg));
        contextMenu.getItems().addAll(openItem, downloadItem);

        fileBox.setOnContextMenuRequested(event -> {
            contextMenu.show(fileBox, event.getScreenX(), event.getScreenY());
        });

        fileBox.getChildren().addAll(fileIcon, fileNameLabel);
        return fileBox;
    }

    private void openAttachment(Message msg) {
        try {
            File file = new File(msg.getAttachmentPath());
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showAlert("Erreur", "Le fichier n'existe plus ou a été déplacé.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
        }
    }

    // Nouvelle méthode pour télécharger un fichier attaché
    private void downloadAttachment(Message msg) {
        try {
            // Obtenir le fichier source
            File sourceFile = new File(msg.getAttachmentPath());
            if (!sourceFile.exists()) {
                showAlert("Erreur", "Le fichier n'existe plus ou a été déplacé.");
                return;
            }

            // Créer un FileChooser pour que l'utilisateur puisse choisir où enregistrer
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer " + msg.getAttachmentName());
            fileChooser.setInitialFileName(msg.getAttachmentName());

            // Définir le filtre approprié en fonction du type de fichier
            if (msg.isImageAttachment()) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
                );
            } else {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
                );
            }

            // Afficher la boîte de dialogue
            Stage stage = (Stage) messageListView.getScene().getWindow();
            File destFile = fileChooser.showSaveDialog(stage);

            if (destFile != null) {
                // Copier le fichier
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Animation de confirmation
                showSuccessAlert("Téléchargement réussi", "Le fichier a été téléchargé avec succès.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec du téléchargement: " + e.getMessage());
        }
    }

    private void handleEditMessage(Message message) {
        // Ne pas permettre l'édition des messages avec pièces jointes pour simplifier
        if (message.hasAttachment()) {
            showAlert("Information", "L'édition des messages avec pièces jointes n'est pas prise en charge.");
            return;
        }

        messageBeingEdited = message;
        messageTextField.setText(message.getContenu());
        messageTextField.requestFocus();
        messageTextField.setPromptText("Modifier votre message...");
        sendButton.getStyleClass().add("edit-mode");
    }

    private void handleDeleteMessage(Message message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le message");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce message ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                chatService.deleteMessage(message.getId());
                loadMessages();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec de la suppression du message");
            }
        }
    }

    private TextFlow createMessageBubble(Message msg) {
        // Analyser le contenu du message pour trouver les mentions
        String messageContent = msg.getContenu();
        TextFlow textFlow = new TextFlow();

        // Regex pour trouver les mentions (@Prénom Nom)
        Pattern mentionPattern = Pattern.compile("@([A-Za-zÀ-ÖØ-öø-ÿ]+ [A-Za-zÀ-ÖØ-öø-ÿ]+)");
        Matcher matcher = mentionPattern.matcher(messageContent);

        int lastEnd = 0;

        // Construire le TextFlow en mettant en évidence les mentions
        while (matcher.find()) {
            // Ajouter le texte avant la mention
            if (matcher.start() > lastEnd) {
                Text regularText = new Text(messageContent.substring(lastEnd, matcher.start()));
                setupTextStyle(regularText, msg);
                textFlow.getChildren().add(regularText);
            }

            // Ajouter la mention avec un style spécial
            Text mentionText = new Text(matcher.group());
            setupTextStyle(mentionText, msg);
            mentionText.setStyle(mentionText.getStyle() + "-fx-font-weight: bold; -fx-underline: true;");
            textFlow.getChildren().add(mentionText);

            lastEnd = matcher.end();
        }

        // Ajouter le reste du texte
        if (lastEnd < messageContent.length()) {
            Text regularText = new Text(messageContent.substring(lastEnd));
            setupTextStyle(regularText, msg);
            textFlow.getChildren().add(regularText);
        }

        // Style de la bulle
        textFlow.setPadding(new Insets(8));
        textFlow.setMaxWidth(300);

        if (msg.getEmail().equals(currentUser.getEmail())) {
            textFlow.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 15;");
        } else if ("admin".equalsIgnoreCase(msg.getRole())) {
            textFlow.setStyle("-fx-background-color: #ff3b30; -fx-background-radius: 15;");
        } else {
            textFlow.setStyle("-fx-background-color: #34c759; -fx-background-radius: 15;");
        }

        return textFlow;
    }
    private void setupTextStyle(Text text, Message msg) {
        if (msg.getEmail().equals(currentUser.getEmail())) {
            text.setFill(Color.WHITE);
        } else if ("admin".equalsIgnoreCase(msg.getRole())) {
            text.setFill(Color.WHITE);
        } else {
            text.setFill(Color.WHITE);
        }
    }

    private void animateSendButton(Runnable onFinished) {
        // 1. Animation de translation vers le haut
        TranslateTransition translateUp = new TranslateTransition(Duration.millis(100), sendIcon);
        translateUp.setByY(-20);
        translateUp.setInterpolator(Interpolator.EASE_OUT);

        // 2. Légère rotation pendant la descente
        RotateTransition rotate = new RotateTransition(Duration.millis(200), sendIcon);
        rotate.setByAngle(15);
        rotate.setAutoReverse(true);
        rotate.setCycleCount(2);

        // 3. Retour avec effet de rebond
        TranslateTransition translateDown = new TranslateTransition(Duration.millis(300), sendIcon);
        translateDown.setByY(20);
        translateDown.setInterpolator(Interpolator.EASE_BOTH);

        // Séquence complète
        SequentialTransition sequence = new SequentialTransition(
                translateUp,
                new ParallelTransition(rotate, translateDown)
        );

        // Callback à la fin de l'animation
        sequence.setOnFinished(e -> onFinished.run());

        sequence.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCloseChat(ActionEvent event) {
        // Récupérer la fenêtre actuelle
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();

        // Créer une animation de fondu
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // Réduire légèrement la fenêtre pour un effet plus élégant
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), stage.getScene().getRoot());
        scaleDown.setFromX(1.0);
        scaleDown.setFromY(1.0);
        scaleDown.setToX(0.9);
        scaleDown.setToY(0.9);

        // Exécuter les animations en parallèle
        ParallelTransition closeAnimation = new ParallelTransition(fadeOut, scaleDown);
        closeAnimation.setOnFinished(e -> stage.close()); // Fermer après l'animation

        // Désactiver l'interaction pendant l'animation
        stage.getScene().getRoot().setDisable(true);

        closeAnimation.play();
    }

    @FXML
    public void handleKeyPress(KeyEvent event) {
        // Si le mode mention est actif et que les suggestions sont visibles
        if (mentionMode && mentionSuggestionsBox.isVisible()) {
            switch (event.getCode()) {
                case UP:
                    // Déplacer la sélection vers le haut
                    int currentIndex = mentionSuggestionsList.getSelectionModel().getSelectedIndex();
                    if (currentIndex > 0) {
                        mentionSuggestionsList.getSelectionModel().select(currentIndex - 1);
                        mentionSuggestionsList.scrollTo(currentIndex - 1);
                        event.consume();
                    }
                    break;
                case DOWN:
                    // Déplacer la sélection vers le bas
                    currentIndex = mentionSuggestionsList.getSelectionModel().getSelectedIndex();
                    if (currentIndex < mentionSuggestionsList.getItems().size() - 1) {
                        mentionSuggestionsList.getSelectionModel().select(currentIndex + 1);
                        mentionSuggestionsList.scrollTo(currentIndex + 1);
                        event.consume();
                    }
                    break;
                case TAB:
                case ENTER:
                    // Sélectionner l'utilisateur en surbrillance
                    HBox selectedItem = mentionSuggestionsList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        User selectedUser = (User) selectedItem.getUserData();
                        selectMentionedUser(selectedUser);
                        event.consume();
                    }
                    break;
                case ESCAPE:
                    // Fermer les suggestions
                    mentionMode = false;
                    hideMentionSuggestions();
                    event.consume();
                    break;
                default:
                    // Pour les autres touches, continuer l'événement normalement
                    break;
            }
        } else if (event.getCode() == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                // Permettre une nouvelle ligne avec Shift+Enter
                return;
            }
            if (!messageTextField.getText().trim().isEmpty() || selectedAttachment != null) {
                handleSendMessage();
                event.consume();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            if (messageBeingEdited != null) {
                // Annuler l'édition avec Escape
                exitEditMode();
                event.consume();
            } else if (selectedAttachment != null) {
                // Annuler la sélection de pièce jointe avec Escape
                clearAttachment();
                event.consume();
            }
        } else if (event.getCode() == KeyCode.AT) {
            // Gérer la frappe manuelle du symbole @ pour activer le mode mention
            // Ceci est techniquement redondant avec le listener de texte, mais offre
            // une rétroaction plus immédiate
            Platform.runLater(() -> {
                mentionMode = true;
                showMentionSuggestions("");
            });
        }
    }    // Méthode améliorée pour le drag dropped
    private void handleDragDropped(DragEvent event) {
        boolean success = false;
        Dragboard db = event.getDragboard();

        if (db.hasFiles()) {
            // Gérer la liste de fichiers
            handleMultiFilesDrop(db.getFiles());
            success = true;
        }

        // Cacher l'overlay avec animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), dropOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> dropOverlay.setVisible(false));
        fadeOut.play();

        event.setDropCompleted(success);
        event.consume();
    }

    // Cette méthode doit également être vérifiée dans votre code
    private void handleMultiFilesDrop(List<File> files) {
        if (files.isEmpty()) return;

        // Taking the first file for now (you could enhance this to handle multiple files)
        File file = files.get(0);

        // Detect file type
        if (isImageFile(file)) {
            selectedAttachment = file;
            attachmentType = Message.AttachmentType.IMAGE.toString();
            showAttachmentPreview(file.getName(), "Image");
        } else {
            selectedAttachment = file;
            attachmentType = Message.AttachmentType.FILE.toString();
            showAttachmentPreview(file.getName(), "Fichier");
        }

        // Visual feedback
        bounceAnimation(messageTextField);

        // Focus on the text field
        Platform.runLater(() -> messageTextField.requestFocus());

        // Log for debugging
        System.out.println("Fichier reçu: " + file.getName());
    }
    // Animation for visual feedback
    private void bounceAnimation(Node node) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();
    }

    // Cette méthode met à jour le texte de l'overlay
    private void updateDropOverlayText(DragEvent event) {
        Dragboard db = event.getDragboard();
        Label overlayLabel = (Label) dropOverlay.getChildren().get(0);

        if (db.hasFiles()) {
            int fileCount = db.getFiles().size();

            if (fileCount == 1) {
                File file = db.getFiles().get(0);
                if (isImageFile(file)) {
                    overlayLabel.setText("Déposez l'image ici");
                    overlayLabel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/image/image-icon.jpg"))));
                } else {
                    overlayLabel.setText("Déposez le fichier ici");
                    overlayLabel.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/image/file-icon.png"))));
                }
            } else {
                overlayLabel.setText("Déposez les fichiers ici");
                overlayLabel.setGraphic(null);
            }
        }
    }
    /**
     * Gère les événements de la barre de titre et permet le déplacement de la fenêtre
     */
    private void setupWindowControls() {
        // Récupérer la barre de titre
        HBox titleBar = (HBox) ((BorderPane) messageListView.getScene().getRoot()).getTop();

        // Gérer les événements de la souris pour le drag de la fenêtre
        titleBar.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        titleBar.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Stage stage = (Stage) messageListView.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });

        // Double-clic pour maximiser/restaurer
        titleBar.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                handleMaximizeWindow(null);
            }
        });
    }

    /**
     * Méthode pour minimiser la fenêtre
     */
    @FXML
    private void handleMinimizeWindow(ActionEvent event) {
        Stage stage = (Stage) messageListView.getScene().getWindow();
        stage.setIconified(true);
    }

    /**
     * Méthode pour maximiser ou restaurer la taille de la fenêtre
     */
    @FXML
    private void handleMaximizeWindow(ActionEvent event) {
        Stage stage = (Stage) messageListView.getScene().getWindow();

        if (stage.isMaximized()) {
            stage.setMaximized(false);
            // Mettre à jour l'icône du bouton
            Button maximizeButton = (Button) ((BorderPane) messageListView.getScene().getRoot())
                    .lookup(".maximize-button");
            if (maximizeButton != null) {
                maximizeButton.setText("□");
            }
        } else {
            stage.setMaximized(true);
            // Mettre à jour l'icône du bouton
            Button maximizeButton = (Button) ((BorderPane) messageListView.getScene().getRoot())
                    .lookup(".maximize-button");
            if (maximizeButton != null) {
                maximizeButton.setText("❐");
            }
        }
    }
    private void positionChatWindowBottomRight() {
        Platform.runLater(() -> {
            Stage stage = (Stage) messageListView.getScene().getWindow();

            // Obtenir les dimensions de l'écran
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();

            // Calculer la position en bas à droite
            double rightPosition = screenBounds.getMaxX() - stage.getWidth() - 20;  // 20px de marge
            double bottomPosition = screenBounds.getMaxY() - stage.getHeight() - 50; // 50px de marge

            // Positionner la fenêtre
            stage.setX(rightPosition);
            stage.setY(bottomPosition);

            // Enlever la barre de titre du système
            stage.initStyle(StageStyle.UNDECORATED);
        });
    }

    /**
     * Méthode pour configurer le stage principal
     * À appeler depuis la classe principale (avant de montrer la scène)
     */
    public static void configureStage(Stage stage) {
        // Enlever la barre de titre du système
        stage.initStyle(StageStyle.UNDECORATED);

        // Style de bordure personnalisé
        stage.initStyle(StageStyle.TRANSPARENT);

        // Définir une taille initiale
        stage.setWidth(400);
        stage.setHeight(600);

        // Autres configurations
        stage.setResizable(true);
        stage.setAlwaysOnTop(false);
    }
    /**
     * Crée les options du message (boutons Modifier et Supprimer colorés)
     * À ajouter à votre ChatController.java sans modifier le reste
     */
    private HBox createMessageOptions(Message msg) {
        HBox optionsBox = new HBox(5);
        optionsBox.setAlignment(Pos.CENTER_RIGHT);
        optionsBox.getStyleClass().add("message-options");

        // Bouton Modifier (VERT)
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("message-option-button", "edit-button");
        editButton.setOnAction(e -> handleEditMessage(msg));

        // Bouton Supprimer (ROUGE)
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("message-option-button", "delete-button");
        deleteButton.setOnAction(e -> handleDeleteMessage(msg));

        optionsBox.getChildren().addAll(editButton, deleteButton);
        return optionsBox;
    }
    // Méthode pour initialiser le sélecteur d'émojis
    private void initializeEmojiPicker() {
        emojiPickerBox = new VBox();
        emojiPickerBox.getStyleClass().add("emoji-picker");
        emojiPickerBox.setVisible(false);
        emojiPickerBox.setManaged(false);

        // Création du tableau d'émojis (grille)
        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        emojiGrid.setPadding(new Insets(10));

        // Liste d'émojis courants - ajout de plus d'emojis populaires
        String[] emojis = {
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
                "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
                "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
                "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "👏", "🙌", "👐",
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "💔", "❣️", "💕",
                "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶",
                "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥",
                "💯", "💪", "🔥", "✨", "🌟", "💫", "💬", "🗯️", "💭", "🕊️"
        };

        int col = 0;
        int row = 0;
        final int COLS = 10; // 10 émojis par ligne

        for (String emoji : emojis) {
            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 20px; -fx-padding: 5;");
            emojiLabel.setCursor(Cursor.HAND);

            // Ajouter un gestionnaire de clic pour insérer l'émoji SANS fermer le picker
            emojiLabel.setOnMouseClicked(e -> {
                messageTextField.appendText(emoji);
                messageTextField.requestFocus();
                messageTextField.positionCaret(messageTextField.getText().length());
                // Ne plus appeler toggleEmojiPicker() ici pour garder le picker ouvert
            });

            // Highlight on hover
            emojiLabel.setOnMouseEntered(e ->
                    emojiLabel.setStyle("-fx-font-size: 20px; -fx-padding: 5; -fx-background-color: -fx-medium-gray; -fx-background-radius: 5;"));
            emojiLabel.setOnMouseExited(e ->
                    emojiLabel.setStyle("-fx-font-size: 20px; -fx-padding: 5;"));

            emojiGrid.add(emojiLabel, col, row);

            col++;
            if (col >= COLS) {
                col = 0;
                row++;
            }
        }

        // Ajouter un bouton pour fermer le panneau d'emojis
        HBox controlsBox = new HBox();
        controlsBox.setAlignment(Pos.CENTER_RIGHT);
        controlsBox.setPadding(new Insets(5, 10, 5, 10));

        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("emoji-close-button");
        closeButton.setOnAction(e -> toggleEmojiPicker());
        controlsBox.getChildren().add(closeButton);

        emojiPickerBox.getChildren().addAll(emojiGrid, controlsBox);

        // Ajouter à la scène
        // Trouvez où insérer le sélecteur d'émojis dans votre disposition
        VBox inputContainer = (VBox) messageTextField.getParent().getParent();
        inputContainer.getChildren().add(0, emojiPickerBox); // Insérer au début
    }

    // 2. Assurez-vous que le messageTextField accepte les emojis
// Ajoutez cette méthode à initialize()
    private void configureEmojiSupport() {
        // S'assurer que le TextField accepte tous les caractères Unicode (y compris les emojis)
        messageTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Aucune restriction - les emojis sont acceptés par défaut
        });
    }

    @FXML
    private void handleEmojiPicker() {
        toggleEmojiPicker();
    }

    private void toggleEmojiPicker() {
        emojiPickerVisible = !emojiPickerVisible;

        if (emojiPickerVisible) {
            // Animer l'ouverture
            emojiPickerBox.setVisible(true);
            emojiPickerBox.setManaged(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), emojiPickerBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), emojiPickerBox);
            scale.setFromY(0.8);
            scale.setToY(1);
            scale.setFromX(0.8);
            scale.setToX(1);

            ParallelTransition transition = new ParallelTransition(fadeIn, scale);
            transition.setOnFinished(e -> positionEmojiPicker());
            transition.play();

            // Ajoutez un gestionnaire d'événements pour fermer le panneau d'emoji
            // quand on clique ailleurs dans l'application
            Platform.runLater(() -> {
                Scene scene = emojiPickerBox.getScene();
                if (scene != null) {
                    scene.setOnMousePressed(event -> {
                        // Si le clic est en dehors du panneau emoji et du bouton emoji, fermer le panneau
                        if (emojiPickerVisible &&
                                !isEventWithinNode(event, emojiPickerBox) &&
                                !isEventWithinNode(event, emojiButton)) {
                            toggleEmojiPicker();
                            // Retirer ce gestionnaire après utilisation
                            scene.setOnMousePressed(null);
                        }
                    });
                }
            });
        } else {
            // Animer la fermeture
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), emojiPickerBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(200), emojiPickerBox);
            scale.setFromY(1);
            scale.setToY(0.8);
            scale.setFromX(1);
            scale.setToX(0.8);

            ParallelTransition transition = new ParallelTransition(fadeOut, scale);
            transition.setOnFinished(e -> {
                emojiPickerBox.setVisible(false);
                emojiPickerBox.setManaged(false);

                // Retirer le gestionnaire d'événements de la scène
                if (emojiPickerBox.getScene() != null) {
                    emojiPickerBox.getScene().setOnMousePressed(null);
                }
            });
            transition.play();
        }
    }
    private void configureEmojiPickerStyles() {
        // Ajouter des styles pour le sélecteur d'emojis si nécessaire
        String emojiPickerStyle =
                ".emoji-picker { -fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5); }" +
                        ".emoji-close-button { -fx-background-color: #f2f2f2; -fx-text-fill: #333; -fx-border-color: #ddd; -fx-border-radius: 3; }" +
                        ".emoji-close-button:hover { -fx-background-color: #e6e6e6; }";

        // Appliquer le style si vous n'avez pas de fichier CSS externe
        // messageListView.getScene().getRoot().getStylesheets().add(emojiPickerStyle); // Décommentez si nécessaire
    }
    private void positionEmojiPicker() {
        // Assurez-vous que le panneau emoji est correctement positionné par rapport au champ texte
        // et qu'il ne dépasse pas des limites de la fenêtre
        Platform.runLater(() -> {
            // Ajouter une petite animation quand le panneau s'ouvre
            if (emojiPickerVisible) {
                // Définir une hauteur maximale pour le panneau d'emojis
                emojiPickerBox.setMaxHeight(300);

                // Ajouter une barre de défilement si nécessaire
                ScrollPane scrollPane = new ScrollPane();
                scrollPane.setContent(emojiPickerBox.getChildren().get(0)); // Supposant que la grille est le premier enfant
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefViewportHeight(250);
                scrollPane.getStyleClass().add("emoji-scroll-pane");

                // Remplacer la grille par le ScrollPane
                if (emojiPickerBox.getChildren().size() > 1) {
                    Node controls = emojiPickerBox.getChildren().get(1); // Sauvegarde des contrôles
                    emojiPickerBox.getChildren().clear();
                    emojiPickerBox.getChildren().addAll(scrollPane, controls);
                }
            }
        });
    }
    private boolean isEventWithinNode(MouseEvent event, Node node) {
        if (node == null) return false;

        Bounds boundsInScene = node.localToScene(node.getBoundsInLocal());
        return boundsInScene.contains(event.getSceneX(), event.getSceneY());
    }

    // 4. Ajout d'une catégorisation des emojis (optionnel - pour une amélioration future)
    private void initializeEmojiPickerWithCategories() {
        emojiPickerBox = new VBox();
        emojiPickerBox.getStyleClass().add("emoji-picker");
        emojiPickerBox.setVisible(false);
        emojiPickerBox.setManaged(false);

        // Créer un TabPane pour les catégories d'emojis
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Catégorie 1: Visages
        Tab facesTab = new Tab("😊");
        GridPane facesGrid = createEmojiGrid(new String[] {
                "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
                "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚"
        });
        facesTab.setContent(facesGrid);

        // Catégorie 2: Gestes
        Tab gesturesTab = new Tab("👍");
        GridPane gesturesGrid = createEmojiGrid(new String[] {
                "👍", "👎", "👌", "✌️", "🤞", "🤟", "🤘", "👏", "🙌", "👐"
        });
        gesturesTab.setContent(gesturesGrid);

        // Catégorie 3: Cœurs
        Tab heartsTab = new Tab("❤️");
        GridPane heartsGrid = createEmojiGrid(new String[] {
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "💔", "❣️", "💕"
        });
        heartsTab.setContent(heartsGrid);

        // Ajouter les onglets au TabPane
        tabPane.getTabs().addAll(facesTab, gesturesTab, heartsTab);

        // Bouton pour fermer le panneau
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("emoji-close-button");
        closeButton.setOnAction(e -> toggleEmojiPicker());

        HBox controlsBox = new HBox();
        controlsBox.setAlignment(Pos.CENTER_RIGHT);
        controlsBox.setPadding(new Insets(5, 10, 5, 10));
        controlsBox.getChildren().add(closeButton);

        emojiPickerBox.getChildren().addAll(tabPane, controlsBox);

        // Ajouter à la scène
        VBox inputContainer = (VBox) messageTextField.getParent().getParent();
        inputContainer.getChildren().add(0, emojiPickerBox);
    }

    // 5. Méthode utilitaire pour créer une grille d'emojis
    private GridPane createEmojiGrid(String[] emojis) {
        GridPane emojiGrid = new GridPane();
        emojiGrid.setHgap(5);
        emojiGrid.setVgap(5);
        emojiGrid.setPadding(new Insets(10));

        int col = 0;
        int row = 0;
        final int COLS = 5; // 5 emojis par ligne pour une meilleure présentation dans les onglets

        for (String emoji : emojis) {
            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 24px; -fx-padding: 5;");
            emojiLabel.setCursor(Cursor.HAND);

            emojiLabel.setOnMouseClicked(e -> {
                messageTextField.appendText(emoji);
                messageTextField.requestFocus();
                messageTextField.positionCaret(messageTextField.getText().length());
            });

            emojiLabel.setOnMouseEntered(e ->
                    emojiLabel.setStyle("-fx-font-size: 24px; -fx-padding: 5; -fx-background-color: -fx-medium-gray; -fx-background-radius: 5;"));
            emojiLabel.setOnMouseExited(e ->
                    emojiLabel.setStyle("-fx-font-size: 24px; -fx-padding: 5;"));

            emojiGrid.add(emojiLabel, col, row);

            col++;
            if (col >= COLS) {
                col = 0;
                row++;
            }
        }

        return emojiGrid;
    }


    /**
     * Initialise l'animation de clignotement pour l'indicateur d'enregistrement
     */
    private void initializeRecordingAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), recordingIndicator);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);

        recordingTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    // Animation de l'indicateur
                    fade.play();
                }),
                new KeyFrame(Duration.seconds(1), e -> {
                    // Mise à jour du chronomètre
                    if (isRecording) {
                        long duration = audioRecorder.getDuration() + 1; // +1 car nous anticipons la seconde suivante
                        updateRecordingTimeLabel(duration);
                    }
                })
        );
        recordingTimeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Met à jour l'affichage du temps d'enregistrement
     */
    private void updateRecordingTimeLabel(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        recordingTimeLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    /**
     * Gère le démarrage de l'enregistrement vocal
     */
    @FXML
    private void handleVoiceRecording() {
        if (currentUser == null) return;

        // Si déjà en train d'enregistrer, ne rien faire
        if (isRecording) return;

        // Démarrer l'enregistrement
        if (audioRecorder.startRecording()) {
            isRecording = true;

            // Afficher la zone d'enregistrement
            recordingBox.setVisible(true);
            recordingBox.setManaged(true);

            // Démarrer l'animation et le chronomètre
            recordingTimeline.play();

            // Désactiver certains contrôles pendant l'enregistrement
            messageTextField.setDisable(true);
            sendButton.setDisable(true);
            fileButton.setDisable(true);
            imageButton.setDisable(true);
            emojiButton.setDisable(true);
        } else {
            showAlert("Erreur", "Impossible de démarrer l'enregistrement. Vérifiez votre microphone.");
        }
    }

    /**
     * Gère l'arrêt de l'enregistrement vocal et son envoi
     */
    @FXML
    private void handleStopRecording() {
        if (!isRecording) return;

        // Arrêter l'enregistrement
        audioRecorder.stopRecording();
        isRecording = false;

        // Arrêter l'animation et le chronomètre
        recordingTimeline.stop();

        try {
            // Créer un fichier temporaire pour l'enregistrement
            tempAudioFile = File.createTempFile("audio_", ".wav");
            tempAudioFile.deleteOnExit();

            // Enregistrer l'audio dans le fichier temporaire
            audioRecorder.saveRecording(tempAudioFile.getAbsolutePath());

            // Envoyer le message vocal
            sendVoiceMessage();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'enregistrement: " + e.getMessage());
        } finally {
            // Réinitialiser l'interface
            resetRecordingUI();
        }
    }

    /**
     * Gère l'annulation de l'enregistrement vocal
     */
    @FXML
    private void handleCancelRecording() {
        if (!isRecording) return;

        // Arrêter l'enregistrement
        audioRecorder.stopRecording();
        isRecording = false;

        // Arrêter l'animation et le chronomètre
        recordingTimeline.stop();

        // Réinitialiser l'interface
        resetRecordingUI();

        showSuccessAlert("Enregistrement annulé", "L'enregistrement a été annulé.");
    }

    /**
     * Réinitialise l'interface après un enregistrement
     */
    private void resetRecordingUI() {
        // Masquer la zone d'enregistrement
        recordingBox.setVisible(false);
        recordingBox.setManaged(false);

        // Réactiver les contrôles
        messageTextField.setDisable(false);
        sendButton.setDisable(false);
        fileButton.setDisable(false);
        imageButton.setDisable(false);
        emojiButton.setDisable(false);

        // Réinitialiser le chronomètre
        recordingTimeLabel.setText("00:00");
    }

    /**
     * Envoie un message vocal à partir du fichier temporaire enregistré
     */
    private void sendVoiceMessage() {
        try {
            if (tempAudioFile != null && tempAudioFile.exists()) {
                // Obtenir la durée de l'enregistrement
                long duration = audioRecorder.getDuration();

                // Texte du message (optionnel)
                String messageText = messageTextField.getText().trim();

                // Envoyer le message vocal
                chatService.sendVoiceMessage(currentUser, messageText, tempAudioFile, duration);

                // Effacer le champ de texte
                messageTextField.clear();

                // Ajouter la notification (nouveau code)
                String senderName = currentUser.getPrenom() + " " + currentUser.getNom();
                sendMessageNotification(senderName);

                // Recharger les messages
                loadMessages();

                showSuccessAlert("Succès", "Message vocal envoyé.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'envoi du message vocal: " + e.getMessage());
        }
    }
    /**
     * Crée un nœud pour afficher un message vocal
     */
    private Node createAudioMessageNode(Message msg) {
        HBox audioBox = new HBox(10);
        audioBox.setAlignment(Pos.CENTER_LEFT);

        // Définir le style en fonction de l'expéditeur
        if (msg.getEmail().equals(currentUser.getEmail())) {
            audioBox.getStyleClass().add("audio-message-bubble");
        } else if ("admin".equalsIgnoreCase(msg.getRole())) {
            audioBox.getStyleClass().addAll("audio-message-bubble", "audio-message-bubble-admin");
        } else {
            audioBox.getStyleClass().addAll("audio-message-bubble", "audio-message-bubble-other");
        }

        // Créer le bouton de lecture
        Button playButton = new Button();
        playButton.getStyleClass().add("play-button");

        // Icône de lecture
        ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream("/image/play-icon.png")));
        playIcon.setFitHeight(24);
        playIcon.setFitWidth(24);
        playButton.setGraphic(playIcon);

        // Créer un label pour la durée
        Label durationLabel = new Label(msg.getFormattedAudioDuration());
        durationLabel.getStyleClass().add("audio-duration");

        // Ajouter une barre de progression pour la lecture
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(150);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        // Ajouter un gestionnaire d'événements pour la lecture
        playButton.setOnAction(e -> {
            toggleAudioPlayback(msg.getId(), new File(msg.getAttachmentPath()), playButton, playIcon, progressBar);
        });

        // Assembler le nœud
        audioBox.getChildren().addAll(playButton, progressBar, durationLabel);

        return audioBox;
    }


    /**
     * Arrête la lecture de tous les autres messages audio
     */
    private void stopAllOtherPlayers(int currentMessageId) {
        for (Map.Entry<Integer, AudioPlayer> entry : audioPlayers.entrySet()) {
            if (entry.getKey() != currentMessageId) {
                AudioPlayer player = entry.getValue();
                if (player.isPlaying()) {
                    player.stop();

                    // Trouver et réinitialiser l'interface de lecture pour ce lecteur
                    resetPlayerUI(entry.getKey());
                }
            }
        }
    }

    /**
     * Réinitialise l'interface d'un lecteur audio spécifique
     */
    private void resetPlayerUI(int messageId) {
        // Parcourir tous les éléments de la liste des messages pour trouver le message avec cet ID
        for (HBox messageContainer : messageListView.getItems()) {
            // Récupérer la VBox qui contient les éléments du message
            VBox messageVBox = null;
            if (messageContainer.getChildren().get(0) instanceof VBox) {
                messageVBox = (VBox) messageContainer.getChildren().get(0);
            }

            if (messageVBox != null) {
                // Chercher un nœud audio qui correspond à ce message
                for (Node node : messageVBox.getChildren()) {
                    if (node instanceof HBox && node.getStyleClass().contains("audio-message-bubble")) {
                        HBox audioBox = (HBox) node;

                        // Vérifier si c'est le bon message audio
                        if (audioBox.getUserData() != null && audioBox.getUserData().equals(messageId)) {
                            // Réinitialiser l'interface
                            for (Node controlNode : audioBox.getChildren()) {
                                if (controlNode instanceof Button) {
                                    Button playButton = (Button) controlNode;
                                    // Changer l'icône
                                    for (Node graphic : playButton.getChildrenUnmodifiable()) {
                                        if (graphic instanceof ImageView) {
                                            ((ImageView) graphic).setImage(
                                                    new Image(getClass().getResourceAsStream("/image/play-icon.png"))
                                            );
                                        }
                                    }
                                } else if (controlNode instanceof ProgressBar) {
                                    ProgressBar progressBar = (ProgressBar) controlNode;
                                    progressBar.setProgress(0);

                                    // Arrêter la timeline si elle existe
                                    if (progressBar.getUserData() instanceof Timeline) {
                                        Timeline timeline = (Timeline) progressBar.getUserData();
                                        timeline.stop();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Méthode pour libérer les ressources lors de la fermeture
     * À appeler lorsque l'application se ferme ou lorsque la fenêtre de chat se ferme
     */
    public void dispose() {
        // Arrêter l'enregistrement s'il est en cours
        if (isRecording) {
            audioRecorder.stopRecording();
            isRecording = false;
        }

        // Arrêter tous les lecteurs audio et libérer les ressources
        for (AudioPlayer player : audioPlayers.values()) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.close();
        }
        audioPlayers.clear();

        // Arrêter toutes les timelines
        if (recordingTimeline != null) {
            recordingTimeline.stop();
        }
    }

    /**
     * Méthode pour vérifier si un son est capturé pendant l'enregistrement
     * Utile pour diagnostiquer les problèmes d'enregistrement
     */
    private boolean isSoundDetected(byte[] buffer) {
        // Calculer le niveau d'amplitude du son
        double sumAmplitude = 0;
        for (int i = 0; i < buffer.length - 1; i += 2) {
            int sample = ((buffer[i] & 0xFF) | (buffer[i + 1] << 8));
            sumAmplitude += Math.abs(sample);
        }
        double averageAmplitude = buffer.length > 0 ? sumAmplitude / (buffer.length / 2) : 0;

        // Définir un seuil pour la détection du son (à ajuster)
        return averageAmplitude > 50;
    }


    /**
     * Méthode améliorée pour basculer entre lecture et pause pour un message audio
     */
    private void toggleAudioPlayback(int messageId, File audioFile, Button playButton, ImageView playIcon, ProgressBar progressBar) {
        // Arrêter d'abord tous les autres lecteurs
        stopAllOtherPlayers(messageId);

        // Obtenir ou créer un lecteur audio pour ce message
        AudioPlayer player = audioPlayers.computeIfAbsent(messageId, id -> {
            AudioPlayer newPlayer = new AudioPlayer();
            boolean success = newPlayer.loadAudio(audioFile);
            if (!success) {
                showAlert("Erreur", "Impossible de charger le fichier audio.");
                return null;
            }
            return newPlayer;
        });

        // Si le lecteur n'a pas pu être créé, sortir
        if (player == null) {
            return;
        }

        if (player.isPlaying()) {
            // Si en train de jouer, mettre en pause
            player.pause();

            try {
                playIcon.setImage(new Image(getClass().getResourceAsStream("/image/play-icon.png")));
            } catch (Exception e) {
                playButton.setText("▶");
            }

            // Arrêter la mise à jour de la barre de progression
            if (progressBar.getUserData() instanceof Timeline) {
                Timeline timeline = (Timeline) progressBar.getUserData();
                timeline.stop();
            }
        } else {
            // Commencer la lecture
            player.play();

            try {
                playIcon.setImage(new Image(getClass().getResourceAsStream("/image/pause-icon.png")));
            } catch (Exception e) {
                playButton.setText("⏸");
            }

            // Configurer la mise à jour de la barre de progression
            long totalDuration = Math.max(1, player.getDurationInSeconds()) * 1000; // en millisecondes, éviter division par zéro

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(100), event -> {
                        double progress = (double) player.getCurrentPosition() / (totalDuration * 1000);
                        progressBar.setProgress(Math.min(1.0, Math.max(0.0, progress))); // Limiter entre 0 et 1

                        // Si la lecture est terminée, réinitialiser
                        if (!player.isPlaying()) {
                            try {
                                playIcon.setImage(new Image(getClass().getResourceAsStream("/image/play-icon.png")));
                            } catch (Exception e) {
                                playButton.setText("▶");
                            }
                            progressBar.setProgress(0);
                            //timeline.stop();
                        }
                    })
            );
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            // Stocker la timeline pour pouvoir l'arrêter plus tard
            progressBar.setUserData(timeline);
        }
    }

    /**
     * Vérifier si un fichier audio est valide avant la lecture
     */
    private boolean isValidAudioFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return false;
        }

        try {
            javax.sound.sampled.AudioSystem.getAudioInputStream(file); // Essayer d'ouvrir le fichier
            return true;
        } catch (Exception e) {
            System.err.println("Fichier audio invalide: " + e.getMessage());
            return false;
        }
    }

    /**
     * Méthode pour nettoyer les ressources non utilisées et optimiser les performances
     */
    private void limitAudioPlayers() {
        // Si plus de 10 lecteurs sont chargés, libérer les plus anciens
        if (audioPlayers.size() > 10) {
            List<Integer> keys = new ArrayList<>(audioPlayers.keySet());
            Collections.sort(keys);

            // Garder seulement les 5 plus récents
            for (int i = 0; i < keys.size() - 5; i++) {
                AudioPlayer player = audioPlayers.get(keys.get(i));
                if (player != null) {
                    player.close();
                    audioPlayers.remove(keys.get(i));
                }
            }
        }
    }

    /**
     * Ajouter des infos de débogage pour le système audio
     */
    private void printAudioInfo() {
        try {
            // Afficher les mixeurs disponibles
            javax.sound.sampled.Mixer.Info[] mixerInfos = javax.sound.sampled.AudioSystem.getMixerInfo();
            System.out.println("Mixers disponibles : " + mixerInfos.length);

            for (javax.sound.sampled.Mixer.Info info : mixerInfos) {
                System.out.println("  " + info.getName() + " - " + info.getDescription());

                javax.sound.sampled.Mixer mixer = javax.sound.sampled.AudioSystem.getMixer(info);
                javax.sound.sampled.Line.Info[] sourceLines = mixer.getSourceLineInfo();
                javax.sound.sampled.Line.Info[] targetLines = mixer.getTargetLineInfo();

                System.out.println("    Source lines: " + sourceLines.length);
                System.out.println("    Target lines: " + targetLines.length);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des informations audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vérifier si l'accès au microphone fonctionne
     */
    private boolean checkMicrophoneAccess() {
        try {
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(44100, 16, 1, true, false);
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);

            if (!javax.sound.sampled.AudioSystem.isLineSupported(info)) {
                System.out.println("Ligne non supportée");
                return false;
            }

            javax.sound.sampled.TargetDataLine line = (javax.sound.sampled.TargetDataLine) javax.sound.sampled.AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Si on arrive ici, c'est que l'accès au micro fonctionne
            line.stop();
            line.close();
            return true;
        } catch (Exception e) {
            System.out.println("Erreur d'accès au microphone : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Méthode à appeler lors de l'initialisation pour vérifier l'état du système audio
     */
    private void setupAudioSystem() {
        // Vérifier l'accès au microphone
        boolean microphoneAccess = checkMicrophoneAccess();
        if (!microphoneAccess) {
            System.err.println("ATTENTION: L'accès au microphone n'est pas disponible!");
            // Désactiver le bouton d'enregistrement vocal
            if (voiceButton != null) {
                voiceButton.setDisable(true);
                // Ajouter une info-bulle pour indiquer pourquoi le bouton est désactivé
                Tooltip tooltip = new Tooltip("Microphone non disponible ou accès refusé");
                Tooltip.install(voiceButton, tooltip);
            }
        }

        // Limiter le nombre de lecteurs audio chargés simultanément
        limitAudioPlayers();
    }
    private void sendMessageNotification(String senderName) {
        // Parcourir toutes les fenêtres ouvertes pour trouver AfficherUserController
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage) {
                Scene scene = ((Stage) window).getScene();
                if (scene != null && scene.getRoot() != null) {
                    // Chercher le contrôleur AfficherUserController
                    AfficherUserController controller = findController(scene.getRoot());
                    if (controller != null) {
                        // Ajouter la notification
                        controller.addNotification("Nouveau message de " + senderName);
                        break;
                    }
                }
            }
        }
    }

    private AfficherUserController findController(Node root) {
        if (root.getUserData() instanceof AfficherUserController) {
            return (AfficherUserController) root.getUserData();
        }

        if (root instanceof Parent) {
            for (Node child : ((Parent) root).getChildrenUnmodifiable()) {
                AfficherUserController controller = findController(child);
                if (controller != null) {
                    return controller;
                }
            }
        }
        return null;
    }
    private void handleMentionInput(String oldValue, String newValue) {
        // Vérifier si l'utilisateur vient de taper @
        if (!oldValue.endsWith("@") && newValue.endsWith("@")) {
            mentionMode = true;
            showMentionSuggestions("");
            return;
        }

        // Si nous sommes déjà en mode mention, vérifier si l'utilisateur a tapé du texte après @
        if (mentionMode) {
            int lastAtIndex = newValue.lastIndexOf('@');
            if (lastAtIndex >= 0) {
                String searchText = newValue.substring(lastAtIndex + 1).toLowerCase();

                // Si l'utilisateur tape un espace après une mention, sortir du mode mention
                if (searchText.contains(" ")) {
                    mentionMode = false;
                    hideMentionSuggestions();
                    return;
                }

                // Sinon, filtrer les suggestions
                showMentionSuggestions(searchText);
            } else {
                // Si @ a été supprimé, fermer les suggestions
                mentionMode = false;
                hideMentionSuggestions();
            }
        }
    }

    /**
     * Affiche une liste de suggestions de mentions filtrée par le texte de recherche
     */
    private void showMentionSuggestions(String searchText) {
        // Vider la liste des suggestions
        mentionSuggestionsList.getItems().clear();

        // Filtrer les utilisateurs par le texte de recherche
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    String fullName = user.getPrenom() + " " + user.getNom();
                    return fullName.toLowerCase().contains(searchText.toLowerCase()) ||
                            user.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                            user.getPrenom().toLowerCase().contains(searchText.toLowerCase());
                })
                .limit(5) // Limiter à 5 suggestions
                .collect(Collectors.toList());

        // Si aucun utilisateur ne correspond, masquer les suggestions
        if (filteredUsers.isEmpty()) {
            hideMentionSuggestions();
            return;
        }

        // Créer un élément pour chaque utilisateur filtré
        for (User user : filteredUsers) {
            HBox userBox = createMentionSuggestionItem(user, searchText); // Mise à jour pour highlight
            mentionSuggestionsList.getItems().add(userBox);
        }

        // Afficher la boîte de suggestions avec animation
        if (!mentionSuggestionsBox.isVisible()) {
            // Préparer l'animation
            mentionSuggestionsBox.setOpacity(0);
            mentionSuggestionsBox.setScaleY(0.8);
            mentionSuggestionsBox.setVisible(true);
            mentionSuggestionsBox.setManaged(true);

            // Animation de fade in et scale
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), mentionSuggestionsBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), mentionSuggestionsBox);
            scaleUp.setFromY(0.8);
            scaleUp.setToY(1);

            ParallelTransition animation = new ParallelTransition(fadeIn, scaleUp);
            animation.play();
        }

        // Positionner la boîte de suggestions près du curseur
        Platform.runLater(() -> {
            positionMentionSuggestions();

            // Sélectionner le premier élément par défaut
            if (!mentionSuggestionsList.getItems().isEmpty()) {
                mentionSuggestionsList.getSelectionModel().select(0);
            }
        });
    }

    /**
     * Masque la boîte de suggestions de mentions
     */
    private void hideMentionSuggestions() {
        if (mentionSuggestionsBox.isVisible()) {
            // Animation de disparition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), mentionSuggestionsBox);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), mentionSuggestionsBox);
            scaleDown.setFromY(1);
            scaleDown.setToY(0.8);

            ParallelTransition animation = new ParallelTransition(fadeOut, scaleDown);
            animation.setOnFinished(e -> {
                mentionSuggestionsBox.setVisible(false);
                mentionSuggestionsBox.setManaged(false);
            });

            animation.play();
        } else {
            mentionSuggestionsBox.setVisible(false);
            mentionSuggestionsBox.setManaged(false);
        }
    }


    /**
     * Crée un élément de suggestion de mention pour un utilisateur
     */
    private HBox createMentionSuggestionItem(User user, String searchText) {
        HBox userBox = new HBox(10);
        userBox.setPadding(new Insets(8));
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setUserData(user);

        // Avatar de l'utilisateur
        Circle avatarCircle = new Circle(15);
        avatarCircle.setFill(Color.web("#3498db"));
        avatarCircle.setStroke(Color.web("#2980b9"));
        avatarCircle.setStrokeWidth(1);

        Text initials = new Text(user.getPrenom().substring(0, 1) + user.getNom().substring(0, 1));
        initials.setFill(Color.WHITE);
        initials.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        StackPane avatar = new StackPane(avatarCircle, initials);

        // Créer un TextFlow pour mettre en surbrillance le texte correspondant
        TextFlow nameFlow = new TextFlow();
        String fullName = user.getPrenom() + " " + user.getNom();

        if (!searchText.isEmpty()) {
            // Mettre en surbrillance le texte correspondant
            int startIndex = fullName.toLowerCase().indexOf(searchText.toLowerCase());
            if (startIndex >= 0) {
                int endIndex = startIndex + searchText.length();

                // Texte avant la correspondance
                if (startIndex > 0) {
                    Text beforeText = new Text(fullName.substring(0, startIndex));
                    beforeText.setStyle("-fx-font-weight: bold;");
                    nameFlow.getChildren().add(beforeText);
                }

                // Texte correspondant (surbrillance)
                Text matchText = new Text(fullName.substring(startIndex, endIndex));
                matchText.setStyle("-fx-font-weight: bold; -fx-fill: #2980b9; -fx-underline: true;");
                nameFlow.getChildren().add(matchText);

                // Texte après la correspondance
                if (endIndex < fullName.length()) {
                    Text afterText = new Text(fullName.substring(endIndex));
                    afterText.setStyle("-fx-font-weight: bold;");
                    nameFlow.getChildren().add(afterText);
                }
            } else {
                // Pas de correspondance exacte, afficher normalement
                Text nameText = new Text(fullName);
                nameText.setStyle("-fx-font-weight: bold;");
                nameFlow.getChildren().add(nameText);
            }
        } else {
            // Pas de texte de recherche, afficher normalement
            Text nameText = new Text(fullName);
            nameText.setStyle("-fx-font-weight: bold;");
            nameFlow.getChildren().add(nameText);
        }

        // Email de l'utilisateur
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        VBox userInfo = new VBox(2);
        userInfo.getChildren().addAll(nameFlow, emailLabel);

        userBox.getChildren().addAll(avatar, userInfo);

        // Effet de survol et sélection
        userBox.setOnMouseEntered(e -> {
            userBox.setStyle("-fx-background-color: #f0f7fd; -fx-background-radius: 5;");
            mentionSuggestionsList.getSelectionModel().select(userBox);
        });

        userBox.setOnMouseExited(e -> {
            if (mentionSuggestionsList.getSelectionModel().getSelectedItem() != userBox) {
                userBox.setStyle("-fx-background-color: transparent;");
            }
        });

        userBox.setOnMouseClicked(e -> selectMentionedUser(user));

        return userBox;
    }
    /**
     * Positionne la boîte de suggestions près du curseur
     */
    private void positionMentionSuggestions() {
        // Positionner la boîte juste en dessous du champ de texte
        Bounds textFieldBounds = messageTextField.localToScene(messageTextField.getBoundsInLocal());
        double sceneX = textFieldBounds.getMinX();
        double sceneY = textFieldBounds.getMaxY();

        // Convertir les coordonnées de la scène vers le système de coordonnées du parent
        Point2D localCoords = mentionSuggestionsBox.getParent().sceneToLocal(sceneX, sceneY);

        mentionSuggestionsBox.setLayoutX(localCoords.getX());
        mentionSuggestionsBox.setLayoutY(localCoords.getY());

        // Limiter la taille
        mentionSuggestionsBox.setMaxHeight(200);
        mentionSuggestionsBox.setMaxWidth(350);

        // Assurer que la box est visible dans la fenêtre
        Bounds bounds = mentionSuggestionsBox.localToScene(mentionSuggestionsBox.getBoundsInLocal());
        double sceneWidth = messageTextField.getScene().getWidth();

        // Ajuster si nécessaire pour éviter de dépasser les bords de la fenêtre
        if (bounds.getMaxX() > sceneWidth) {
            double adjustment = bounds.getMaxX() - sceneWidth + 10; // 10px de marge
            mentionSuggestionsBox.setLayoutX(localCoords.getX() - adjustment);
        }
    }
    private void selectMentionedUser(User user) {
        // Obtenir le texte actuel
        String currentText = messageTextField.getText();
        int lastAtIndex = currentText.lastIndexOf('@');

        if (lastAtIndex >= 0) {
            // Remplacer le texte après @ par le nom de l'utilisateur
            String newText = currentText.substring(0, lastAtIndex) +
                    "@" + user.getPrenom() + " " + user.getNom() + " ";

            messageTextField.setText(newText);
            messageTextField.positionCaret(newText.length());
        }

        // Fermer les suggestions
        mentionMode = false;
        hideMentionSuggestions();
    }


}