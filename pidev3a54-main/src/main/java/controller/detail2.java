package controller;

import Models.textile;
import Models.Vote;
import Services.TextileService;
import Services.VoteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class detail2 implements Initializable {

    private textile currentTextile;
    private TextileService textileService = new TextileService();
    private VoteService voteService = new VoteService();
    private int currentUserId = 1; // ID utilisateur simulé

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (voteService == null) {
            voteService = new VoteService();
        }
    }

    public void setTextileDetails(textile t) {
        this.currentTextile = t;
        // Implémentez la logique pour afficher les détails du textile
        // comme dans la méthode originale si vous avez des champs FXML correspondants
    }

    @FXML
    public void handleRate(ActionEvent event) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "Aucun textile sélectionné", "Aucun textile à évaluer.");
            return;
        }

        try {
            Dialog<Integer> ratingDialog = new Dialog<>();
            ratingDialog.setTitle("Évaluer le textile");

            // Configuration de la boîte de dialogue
            VBox mainContent = new VBox(20);
            mainContent.setAlignment(Pos.CENTER);

            // Création des étoiles de notation
            HBox starsContainer = new HBox(15);
            starsContainer.setAlignment(Pos.CENTER);

            ToggleGroup ratingGroup = new ToggleGroup();

            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                ToggleButton starButton = new ToggleButton();
                starButton.setToggleGroup(ratingGroup);
                starButton.setUserData(rating);

                SVGPath starPath = new SVGPath();
                starPath.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
                starButton.setGraphic(starPath);

                starsContainer.getChildren().add(starButton);
            }

            mainContent.getChildren().add(starsContainer);
            ratingDialog.getDialogPane().setContent(mainContent);

            // Boutons de la boîte de dialogue
            ButtonType submitButtonType = new ButtonType("Soumettre", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            ratingDialog.getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);

            // Conversion du résultat
            ratingDialog.setResultConverter(dialogButton -> {
                if (dialogButton == submitButtonType) {
                    ToggleButton selected = (ToggleButton) ratingGroup.getSelectedToggle();
                    if (selected != null) {
                        return (Integer) selected.getUserData();
                    }
                }
                return null;
            });

            // Traitement de la notation
            ratingDialog.showAndWait().ifPresent(rating -> {
                if (rating != null && rating > 0) {
                    try {
                        voteService.addOrUpdateVote(currentTextile.getId(), currentUserId, rating);
                        showAlert(AlertType.INFORMATION, "Évaluation enregistrée",
                                "Votre évaluation a été enregistrée avec succès.");
                    } catch (SQLException e) {
                        showAlert(AlertType.ERROR, "Erreur",
                                "Une erreur est survenue lors de l'enregistrement: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de revenir à la liste: " + e.getMessage());
        }
    }

    @FXML
    public void modifiertextile(ActionEvent event) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "Erreur", "Aucun textile sélectionné");
            return;
        }

        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            edit2 editController = loader.getController();
            editController.setTextileDetails(currentTextile);

            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'éditeur: " + e.getMessage());
        }
    }

    @FXML
    public void supprimerTextile(ActionEvent event) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "Erreur", "Aucun textile sélectionné");
            return;
        }

        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer ce textile ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    textileService.delete(currentTextile);
                    showAlert(AlertType.INFORMATION, "Succès", "Textile supprimé avec succès");
                    handleBack(event); // Retour à la liste après suppression
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur", "Échec de la suppression: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}