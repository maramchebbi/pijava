package Controllers;

import Models.Event;
import Models.User;
import Services.ParticipationService;
import Utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Pop-up animé pour afficher les détails d'un événement
 */
public class EventDetailPopup {

    private final Stage dialogStage;
    private final VBox contentBox;
    private final Event event;

    public EventDetailPopup(Event event) {
        this.event = event;
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.setTitle("Détails de l'événement");

        contentBox = createContent();

        // Fond semi-transparent
        contentBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        Scene scene = new Scene(contentBox);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
    }

    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setMinWidth(400);
        content.setMaxWidth(600);

        // Protection contre un événement null
        if (event == null) {
            Label errorLabel = new Label("⚠️ Détails de l'événement non disponibles");
            errorLabel.setFont(Font.font("Poppins", FontWeight.BOLD, 16));
            errorLabel.setStyle("-fx-text-fill: #e74c3c;");

            Button closeButton = new Button("Fermer");
            closeButton.setStyle(
                    "-fx-background-color: #3498db; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;"
            );
            closeButton.setOnAction(e -> close());

            content.getChildren().addAll(errorLabel, closeButton);
            return content;
        }

        // Titre de l'événement
        Label titleLabel = new Label("🎯 " + (event.getTitre() != null ? event.getTitre() : "Sans titre"));
        titleLabel.setFont(Font.font("Poppins", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");

        // Image de l'événement
        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Charger l'image avec gestion d'erreur améliorée
        try {
            if (event.getImage() != null && !event.getImage().isEmpty()) {
                File file = new File("uploads/" + event.getImage());
                if (file.exists()) {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);

                    // Animation de fondu pour l'image
                    FadeTransition fade = new FadeTransition(Duration.seconds(1.2), imageView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();
                } else {
                    // Essayer de charger une image par défaut depuis différents chemins possibles
                    try {
                        Image defaultImage = new Image("file:uploads/default.jpg");
                        imageView.setImage(defaultImage);
                    } catch (Exception e2) {
                        try {
                            Image defaultImage = new Image("file:default.jpg");
                            imageView.setImage(defaultImage);
                        } catch (Exception e3) {
                            System.err.println("Impossible de charger l'image par défaut");
                            // Plutôt que de planter, ne pas afficher d'image
                            imageView.setFitHeight(0);
                            imageView.setVisible(false);
                        }
                    }
                }
            } else {
                // Même approche pour l'image par défaut
                try {
                    Image defaultImage = new Image("file:uploads/default.jpg");
                    imageView.setImage(defaultImage);
                } catch (Exception e2) {
                    try {
                        Image defaultImage = new Image("file:default.jpg");
                        imageView.setImage(defaultImage);
                    } catch (Exception e3) {
                        System.err.println("Impossible de charger l'image par défaut");
                        // Plutôt que de planter, ne pas afficher d'image
                        imageView.setFitHeight(0);
                        imageView.setVisible(false);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            e.printStackTrace();
            // Ne pas afficher d'image plutôt que de planter
            imageView.setFitHeight(0);
            imageView.setVisible(false);
        }

        // Informations sur l'événement
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 15;");

        // Localisation avec gestion des nulls
        String locationText = event.getLocalisation() != null ? event.getLocalisation() : "Non spécifiée";
        HBox locationBox = createInfoRow("📍 Lieu:", locationText);

        // Date et heure avec gestion des nulls
        String formattedDate = event.getDate() != null ? new SimpleDateFormat("dd/MM/yyyy").format(event.getDate()) : "Non spécifiée";
        HBox dateBox = createInfoRow("📅 Date:", formattedDate);

        String formattedTime = event.getHeure() != null ? event.getHeure().toString() : "Non spécifiée";
        HBox timeBox = createInfoRow("⏰ Heure:", formattedTime);

        // Nombre de participants
        HBox participantsBox = createInfoRow("👥 Participants:", String.valueOf(event.getNbParticipant()));

        infoBox.getChildren().addAll(locationBox, dateBox, timeBox, participantsBox);

        // Conteneur pour les boutons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        // Bouton pour participer
        Button participateButton = new Button("✅ Participer");
        participateButton.setStyle(
                "-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;"
        );
        participateButton.setOnAction(e -> handleParticipate());

        // Bouton de fermeture
        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20;"
        );
        closeButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(participateButton, closeButton);

        content.getChildren().addAll(titleLabel, imageView, infoBox, buttonBox);
        return content;
    }

    private void handleParticipate() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Confirmation de participation");
            dialog.setHeaderText("Confirmer votre numéro de téléphone");
            dialog.setContentText("Numéro de téléphone :");

            dialog.showAndWait().ifPresent(input -> {
                try {
                    int numtel = Integer.parseInt(input);
                    ParticipationService participationService = new ParticipationService();
                    participationService.addParticipation(user.getId(), event.getId(), user.getNom(), user.getEmail(), numtel);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText("Vous êtes inscrit à cet événement !");
                    alert.showAndWait();
                    close();
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Numéro invalide. Veuillez saisir uniquement des chiffres.");
                    alert.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de la participation.");
                    alert.showAndWait();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(null);
            alert.setContentText("Vous devez être connecté pour participer !");
            alert.showAndWait();
        }
    }

    private HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Poppins", FontWeight.BOLD, 14));
        labelNode.setStyle("-fx-text-fill: #34495e;");

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("Poppins", 14));
        valueNode.setStyle("-fx-text-fill: #2c3e50;");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    public void show() {
        dialogStage.show();

        // Animation d'apparition
        contentBox.setScaleX(0.7);
        contentBox.setScaleY(0.7);
        contentBox.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), contentBox);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), contentBox);
        fadeTransition.setToValue(1.0);

        SequentialTransition sequentialTransition = new SequentialTransition(scaleTransition, fadeTransition);
        sequentialTransition.play();
    }

    public void close() {
        // Animation de disparition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentBox);
        fadeOut.setToValue(0);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), contentBox);
        scaleOut.setToX(0.7);
        scaleOut.setToY(0.7);

        SequentialTransition closeTransition = new SequentialTransition(fadeOut, scaleOut);
        closeTransition.setOnFinished(e -> dialogStage.close());
        closeTransition.play();
    }

    /**
     * Affiche le popup avec une animation d'entrée
     */
    public void showWithAnimation() {
        // D'abord afficher le popup
        dialogStage.show();

        // Configurer l'état initial (réduit et transparent)
        dialogStage.setOpacity(0.0);

        // Récupérer la racine de la scène pour l'animer
        VBox root = (VBox) dialogStage.getScene().getRoot();
        root.setScaleX(0.8);
        root.setScaleY(0.8);

        // Animation de mise à l'échelle
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(250), root);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        // Animation de fondu
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(250), root);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        // Jouer les animations
        scaleTransition.play();
        fadeTransition.play();
    }

    /**
     * Ferme le popup avec une animation de sortie
     */
    private void closeWithAnimation() {
        // Récupérer la racine de la scène pour l'animer
        VBox root = (VBox) dialogStage.getScene().getRoot();

        // Animation de mise à l'échelle
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), root);
        scaleTransition.setToX(0.8);
        scaleTransition.setToY(0.8);

        // Animation de fondu
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), root);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);

        // Fermer le popup à la fin de l'animation
        fadeTransition.setOnFinished(e -> dialogStage.close());

        // Jouer les animations
        scaleTransition.play();
        fadeTransition.play();
    }
}