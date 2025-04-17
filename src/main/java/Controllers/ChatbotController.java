package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class ChatbotController {

    @FXML
    private VBox chatArea;

    @FXML
    private TextField inputField;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    public void handleSendMessage() {
        String message = inputField.getText();
        if (message.trim().isEmpty()) return;

        addBubble("Vous : " + message, true);
        inputField.clear();

        new Thread(() -> {
            try {
                String reply = sendMessageToChatbot(message);
                Platform.runLater(() -> addBubble("Bot : " + reply, false));
            } catch (IOException e) {
                Platform.runLater(() -> addBubble("Erreur : " + e.getMessage(), false));
            }
        }).start();
    }


    private String sendMessageToChatbot(String message) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("python", "chatbot/chat.py");
        builder.redirectErrorStream(true);

        Process process = builder.start();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(message + "\n");
        writer.flush();
        writer.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder replyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            replyBuilder.append(line).append("\n");
        }
        reader.close();

        return replyBuilder.toString().trim();
    }

    private void addBubble(String message, boolean isUser) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setPadding(new Insets(10));
        label.setMaxWidth(300);

        // Déterminer les couleurs du fond et du texte
        String backgroundColor = isUser ? "#d1e7dd" : "#f8d7da";
        String textColor = isUser ? "#000000" : "#000000"; // Utiliser une couleur sombre pour le texte

        label.setStyle("-fx-background-color: " + backgroundColor + "; "
                + "-fx-background-radius: 15; "
                + "-fx-font-size: 14; "
                + "-fx-text-fill: " + textColor + ";"); // Assurer une bonne lisibilité

        HBox bubble = new HBox(label);
        bubble.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatArea.getChildren().add(bubble);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }



}
