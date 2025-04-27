package controller;

import Models.textile;
import Services.TextileQRService;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import com.google.zxing.WriterException;

public class TextileQRCodeController {

    @FXML private ImageView qrCodeImageView;
    @FXML private Label textileTitleLabel;
    @FXML private Label urlLabel;
    @FXML private Button saveButton;
    @FXML private Button printButton;
    @FXML private Button backButton;

    private textile currentTextile;
    private final TextileQRService qrService = new TextileQRService();

    // Initialise le contrôleur
    public void initialize() {
        System.out.println("Contrôleur initialisé");

        // Ajouter des gestionnaires d'événements manuellement
        saveButton.setOnAction(this::handleSaveQRCode);
        printButton.setOnAction(this::handlePrintQRCode);
    }

    public void setTextile(textile textile) {
        this.currentTextile = textile;

        // Mettre à jour uniquement le titre du textile
        textileTitleLabel.setText(textile.getNom());

        // Ne pas afficher l'URL dans le label sous le QR code
        urlLabel.setText("");  // Laisser vide pour ne pas montrer le chemin

        // Générer et afficher le QR code
        try {
            Image qrCodeImage = qrService.generateQRCodeForTextile(textile.getId());
            qrCodeImageView.setImage(qrCodeImage);
        } catch (SQLException | WriterException e) {
            showErrorAlert("Erreur de génération du QR code", e.getMessage());
        }
    }

    // Gestionnaire de clic sur l'icône QR
    @FXML
    private void handleQRCodeClick() {
        if (currentTextile != null) {
            try {
                Image qrImage = qrService.generateQRCodeForTextile(currentTextile.getId());
                qrCodeImageView.setImage(qrImage);
                saveButton.setDisable(false);
                printButton.setDisable(false);
            } catch (SQLException | WriterException e) {
                showError("Erreur", "Échec de génération du QR Code: " + e.getMessage());
            }
        }
    }

    // Sauvegarde le QR Code en fichier PNG
    @FXML
    private void handleSaveQRCode(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le QR Code");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images PNG", "*.png")
        );
        fileChooser.setInitialFileName("qrcode_textile_" + currentTextile.getId() + ".png");

        // Afficher le dialogue de sauvegarde
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                // Convertir l'image JavaFX en BufferedImage pour la sauvegarde
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(qrCodeImageView.getImage(), null);
                ImageIO.write(bufferedImage, "png", file);

                // Afficher un message de succès
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("QR Code sauvegardé");
                alert.setContentText("Le QR Code a été sauvegardé avec succès à l'emplacement : \n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (IOException e) {
                showErrorAlert("Erreur de sauvegarde", "Impossible de sauvegarder le QR code: " + e.getMessage());
            }
        }
    }
    // Imprime le QR Code
    @FXML
    private void handlePrintQRCode(ActionEvent event) {
        try {
            // Créer un PrinterJob
            PrinterJob job = PrinterJob.createPrinterJob();

            if (job != null && job.showPrintDialog(((Node) event.getSource()).getScene().getWindow())) {
                // Créer un noeud pour l'impression qui contient le QR code et les informations
                VBox printNode = new VBox(10);
                printNode.setStyle("-fx-padding: 20; -fx-background-color: white;");

                // Ajouter les éléments
                Label titleLabel = new Label(currentTextile.getNom());
                titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

                ImageView printImageView = new ImageView(qrCodeImageView.getImage());
                printImageView.setFitWidth(250);
                printImageView.setFitHeight(250);
                printImageView.setPreserveRatio(true);

                Label infoLabel = new Label("QR Code pour accès rapide aux informations du textile");
                infoLabel.setStyle("-fx-font-size: 12;");

                Label urlInfoLabel = new Label(urlLabel.getText());
                urlInfoLabel.setStyle("-fx-font-size: 10;");

                printNode.getChildren().addAll(titleLabel, printImageView, infoLabel, urlInfoLabel);

                // Calculer l'échelle pour l'ajuster à la page
                PageLayout pageLayout = job.getPrinter().getDefaultPageLayout();
                double scaleX = pageLayout.getPrintableWidth() / printNode.getBoundsInParent().getWidth();
                double scaleY = pageLayout.getPrintableHeight() / printNode.getBoundsInParent().getHeight();
                double scale = Math.min(scaleX, scaleY);

                // Appliquer l'échelle
                printNode.getTransforms().add(new Scale(scale, scale));

                // Imprimer
                boolean success = job.printPage(printNode);
                if (success) {
                    job.endJob();

                    // Afficher un message de succès
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setHeaderText("QR Code imprimé");
                    alert.setContentText("Le QR Code a été envoyé à l'imprimante avec succès.");
                    alert.showAndWait();
                } else {
                    showErrorAlert("Erreur d'impression", "L'impression a échoué. Vérifiez votre imprimante.");
                }
            }
        } catch (Exception e) {
            showErrorAlert("Erreur d'impression", "Impossible d'imprimer le QR code: " + e.getMessage());
        }
    }
    // Ferme la fenêtre
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Crée un VBox pour l'impression
    private VBox createPrintableNode() {
        VBox printNode = new VBox(10);
        printNode.setStyle("-fx-padding: 20; -fx-alignment: center;");

        ImageView printView = new ImageView(qrCodeImageView.getImage());
        printView.setFitWidth(250);
        printView.setFitHeight(250);
        printView.setPreserveRatio(true);

        printNode.getChildren().addAll(
                new Label("QR Code pour: " + currentTextile.getNom()),
                printView,
                new Label("ID: " + currentTextile.getId())
        );

        return printNode;
    }

    // Met à jour le QR Code
    private void updateQRCode() {
        handleQRCodeClick();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Affiche une alerte
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Affiche une erreur
    private void showError(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }
}