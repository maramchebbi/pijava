package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import models.Message;
import models.User;
import service.ChatService;
import utils.SessionManager;

import java.sql.SQLException;
import java.util.List;

public class ChatController {
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private ListView<HBox> messageListView;

    @FXML
    private TextField messageTextField;

    @FXML
    private Button sendButton;

    private final ChatService chatService = new ChatService();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageTextField.setPromptText("Veuillez vous connecter...");
            messageTextField.setDisable(true);
            sendButton.setDisable(true);
            return;
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

        try {
            loadMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleSendMessage() {
        String content = messageTextField.getText().trim();
        if (!content.isEmpty()) {
            try {
                chatService.sendMessage(currentUser, content);
                messageTextField.clear();
                loadMessages();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages() throws SQLException {
        messageListView.getItems().clear();
        List<Message> messages = chatService.getAllMessages();

        for (Message msg : messages) {
            // Création du contenu du message
            TextFlow textFlow = createMessageBubble(msg);

            // Création du conteneur HBox pour l'alignement
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new Insets(5, 10, 5, 10));

            // Si c'est le message de l'utilisateur courant, aligner à droite
            if (msg.getEmail().equals(currentUser.getEmail())) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageContainer.getChildren().add(textFlow);
            }
            // Sinon aligner à gauche
            else {
                messageContainer.setAlignment(Pos.CENTER_LEFT);

                // Ajouter le nom de l'expéditeur si ce n'est pas l'utilisateur courant
                Text senderName = new Text(msg.getNom() + ":\n");
                senderName.setStyle("-fx-font-weight: bold");

                VBox vbox = new VBox(senderName, textFlow);
                messageContainer.getChildren().add(vbox);
            }

            messageListView.getItems().add(messageContainer);
        }

        // Scroll to the bottom
        messageListView.scrollTo(messageListView.getItems().size() - 1);
    }

    private TextFlow createMessageBubble(Message msg) {
        Text text = new Text(msg.getContenu());
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(8));
        textFlow.setMaxWidth(300); // Limite la largeur des bulles

        // Style des bulles selon l'expéditeur
        if (msg.getEmail().equals(currentUser.getEmail())) {
            // Message de l'utilisateur courant (bleu)
            textFlow.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 15;");
            text.setFill(Color.WHITE);
        } else if ("admin".equalsIgnoreCase(msg.getRole())) {
            // Message de l'admin (rouge)
            textFlow.setStyle("-fx-background-color: #ff3b30; -fx-background-radius: 15;");
            text.setFill(Color.WHITE);
        } else {
            // Message des autres utilisateurs (vert)
            textFlow.setStyle("-fx-background-color: #34c759; -fx-background-radius: 15;");
            text.setFill(Color.WHITE);
        }

        return textFlow;
    }
}