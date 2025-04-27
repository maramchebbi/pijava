package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import service.TranslationService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TranslationController {

    @FXML
    private ComboBox<String> sourceLanguageComboBox;

    @FXML
    private ComboBox<String> targetLanguageComboBox;

    @FXML
    private TextArea sourceTextArea;

    @FXML
    private TextArea targetTextArea;

    @FXML
    private Button translateButton;

    @FXML
    private Button swapLanguagesButton;

    @FXML
    private Button clearSourceButton;

    @FXML
    private Button pasteButton;

    @FXML
    private Button copyButton;

    @FXML
    private Circle apiStatusIndicator;

    @FXML
    private Label apiStatusLabel;

    private TranslationService translationService;
    private Map<String, String> languageCodes;

    @FXML
    public void initialize() {
        translationService = new TranslationService();
        setupLanguages();

        // Sélectionner les langues par défaut
        sourceLanguageComboBox.setValue("Français");
        targetLanguageComboBox.setValue("Anglais");

        // Ajouter un listener pour traduire automatiquement après un court délai
        sourceTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !newValue.equals(oldValue)) {
                // Traduction automatique après 1 seconde d'inactivité
                CompletableFuture.delayedExecutor(1, java.util.concurrent.TimeUnit.SECONDS)
                        .execute(this::handleTranslate);
            }
        });
    }

    private void setupLanguages() {
        // Configuration des langues et leurs codes
        languageCodes = new HashMap<>();
        languageCodes.put("Français", "fr");
        languageCodes.put("Anglais", "en");
        languageCodes.put("Allemand", "de");
        languageCodes.put("Espagnol", "es");
        languageCodes.put("Italien", "it");
        languageCodes.put("Portugais", "pt");
        languageCodes.put("Russe", "ru");
        languageCodes.put("Japonais", "ja");
        languageCodes.put("Chinois", "zh");
        languageCodes.put("Arabe", "ar");

        // Ajouter les langues aux ComboBox
        sourceLanguageComboBox.getItems().addAll(languageCodes.keySet());
        targetLanguageComboBox.getItems().addAll(languageCodes.keySet());
    }

    @FXML
    void handleTranslate() {
        handleTranslate(null);
    }

    @FXML
    void handleTranslate(ActionEvent event) {
        String sourceText = sourceTextArea.getText().trim();
        if (sourceText.isEmpty()) {
            return;
        }

        // Récupérer les codes de langue
        String sourceLang = languageCodes.get(sourceLanguageComboBox.getValue());
        String targetLang = languageCodes.get(targetLanguageComboBox.getValue());

        if (sourceLang == null || targetLang == null) {
            updateApiStatus(false, "Veuillez sélectionner les langues");
            return;
        }

        // Mise à jour de l'interface pendant la traduction
        translateButton.setDisable(true);
        apiStatusIndicator.setFill(Color.valueOf("#f39c12"));
        apiStatusLabel.setText("API Status: Traduction en cours...");

        // Appel asynchrone à l'API
        new Thread(() -> {
            try {
                String translatedText = translationService.translate(sourceText, sourceLang, targetLang);

                // Mettre à jour l'interface sur le thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    targetTextArea.setText(translatedText);
                    translateButton.setDisable(false);
                    updateApiStatus(true, "Traduction réussie");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    targetTextArea.setText("Erreur de traduction: " + e.getMessage());
                    translateButton.setDisable(false);
                    updateApiStatus(false, "Erreur: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    void handleSwapLanguages(ActionEvent event) {
        // Échanger les langues
        String sourceLanguage = sourceLanguageComboBox.getValue();
        String targetLanguage = targetLanguageComboBox.getValue();

        sourceLanguageComboBox.setValue(targetLanguage);
        targetLanguageComboBox.setValue(sourceLanguage);

        // Échanger les textes
        String sourceText = sourceTextArea.getText();
        String targetText = targetTextArea.getText();

        if (!targetText.isEmpty()) {
            sourceTextArea.setText(targetText);
            targetTextArea.setText("");
            // Traduire automatiquement après échange
            handleTranslate();
        }
    }

    @FXML
    void handleClearSource(ActionEvent event) {
        sourceTextArea.clear();
        targetTextArea.clear();
        updateApiStatus(true, "En attente");
    }

    @FXML
    void handlePaste(ActionEvent event) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            sourceTextArea.setText(clipboard.getString());
        }
    }

    @FXML
    void handleCopy(ActionEvent event) {
        String translatedText = targetTextArea.getText();
        if (!translatedText.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(translatedText);
            Clipboard.getSystemClipboard().setContent(content);

            // Feedback à l'utilisateur
            updateApiStatus(true, "Texte copié dans le presse-papiers");
        }
    }

    @FXML
    void handleReturn(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur de navigation: " + e.getMessage());
        }
    }

    private void updateApiStatus(boolean success, String message) {
        apiStatusIndicator.setFill(success ? Color.valueOf("#2ecc71") : Color.valueOf("#e74c3c"));
        apiStatusLabel.setText("API Status: " + message);
    }
}