package Controllers;

import Models.Event;
import Services.EventService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.Arrays;
import javafx.animation.ScaleTransition;






public class DetailsEvent {

    @FXML
    private FlowPane eventFlowPane;

    private final EventService eventService = new EventService();

    @FXML
    public void initialize() {
        eventFlowPane.getChildren().clear();
        try {
            List<Event> events = eventService.getAll();
            for (Event event : events) {
                VBox eventCard = new VBox(10);
                eventCard.setPrefWidth(200);
                eventCard.setAlignment(Pos.CENTER);
                eventCard.setPadding(new Insets(15));
                eventCard.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
                );

                ImageView imageView = new ImageView();
                try {
                    File file = new File("uploads/" + event.getImage());
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    imageView.setFitWidth(180);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(true);

                    // Animation Fade
                    FadeTransition fade = new FadeTransition(Duration.seconds(1.2), imageView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();

                } catch (Exception e) {
                    imageView.setImage(null);
                }

                Label titre = new Label("🎯 " + event.getTitre());
                Label localisation = new Label("📍 " + event.getLocalisation());
                Label date = new Label("📅 " + event.getDate());
                Label heure = new Label("⏰ " + event.getHeure());
                Label participants = new Label("👥 " + event.getNbParticipant() + " Participants");

                for (Label label : Arrays.asList(titre, localisation, date, heure, participants)) {
                    label.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: #2d3436;");
                }

                Button modifierBtn = new Button("✏️ Modifier");
                modifierBtn.setStyle(
                        "-fx-background-color: #00b894;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 20;"
                );
                modifierBtn.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
                        Parent root = loader.load();
                        ModifierEvent controller = loader.getController();
                        controller.setEvent(event);
                        controller.setDetailsEventController(this);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                Button supprimerBtn = new Button("🗑️ Supprimer");
                supprimerBtn.setStyle(
                        "-fx-background-color: #d63031;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 20;"
                );
                supprimerBtn.setOnAction(e -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation de suppression");
                        alert.setHeaderText(null);
                        alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
                        if (alert.showAndWait().get() == ButtonType.OK) {
                            eventService.delete(event);
                            loadEvents();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                eventCard.getChildren().addAll(imageView, titre, localisation, date, heure, participants, modifierBtn, supprimerBtn);

                eventCard.setOnMouseEntered(ev -> {
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), eventCard);
                    scaleUp.setToX(1.05);
                    scaleUp.setToY(1.05);
                    scaleUp.play();
                });
                eventCard.setOnMouseExited(ev -> {
                    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), eventCard);
                    scaleDown.setToX(1);
                    scaleDown.setToY(1);
                    scaleDown.play();
                });

                eventFlowPane.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent ajouterRoot = FXMLLoader.load(getClass().getResource("/AjouterEvent.fxml"));
            Stage stage = (Stage) eventFlowPane.getScene().getWindow();
            stage.setScene(new Scene(ajouterRoot));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshEvents() {
        eventFlowPane.getChildren().clear();
        initialize();
    }

    public void loadEvents() {
        eventFlowPane.getChildren().clear();
        try {
            List<Event> events = eventService.getAll();
            for (Event event : events) {
                VBox eventCard = new VBox(10);
                eventCard.setPrefWidth(200);
                eventCard.setAlignment(Pos.CENTER);
                eventCard.setPadding(new Insets(15));
                eventCard.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 15;" +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
                );

                ImageView imageView = new ImageView();
                try {
                    File file = new File("uploads/" + event.getImage());
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    imageView.setFitWidth(180);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(true);

                    // Animation Fade
                    FadeTransition fade = new FadeTransition(Duration.seconds(1.2), imageView);
                    fade.setFromValue(0);
                    fade.setToValue(1);
                    fade.play();

                } catch (Exception e) {
                    imageView.setImage(null);
                }

                Label titre = new Label("🎯 " + event.getTitre());
                Label localisation = new Label("📍 " + event.getLocalisation());
                Label date = new Label("📅 " + event.getDate());
                Label heure = new Label("⏰ " + event.getHeure());
                Label participants = new Label("👥 " + event.getNbParticipant() + " Participants");

                for (Label label : Arrays.asList(titre, localisation, date, heure, participants)) {
                    label.setStyle("-fx-font-family: 'Poppins'; -fx-font-size: 13px; -fx-text-fill: #2d3436;");
                }

                Button modifierBtn = new Button("✏️ Modifier");
                modifierBtn.setStyle(
                        "-fx-background-color: #00b894;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 20;"
                );
                modifierBtn.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierEvent.fxml"));
                        Parent root = loader.load();
                        ModifierEvent controller = loader.getController();
                        controller.setEvent(event);
                        controller.setDetailsEventController(this);
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                Button supprimerBtn = new Button("🗑️ Supprimer");
                supprimerBtn.setStyle(
                        "-fx-background-color: #d63031;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 20;"
                );
                supprimerBtn.setOnAction(e -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation de suppression");
                        alert.setHeaderText(null);
                        alert.setContentText("Voulez-vous vraiment supprimer cet événement ?");
                        if (alert.showAndWait().get() == ButtonType.OK) {
                            eventService.delete(event);
                            loadEvents();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                eventCard.getChildren().addAll(imageView, titre, localisation, date, heure, participants, modifierBtn, supprimerBtn);

                eventCard.setOnMouseEntered(ev -> {
                    ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), eventCard);
                    scaleUp.setToX(1.05);
                    scaleUp.setToY(1.05);
                    scaleUp.play();
                });
                eventCard.setOnMouseExited(ev -> {
                    ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), eventCard);
                    scaleDown.setToX(1);
                    scaleDown.setToY(1);
                    scaleDown.play();
                });

                eventFlowPane.getChildren().add(eventCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterEvent(ActionEvent event) {
        try {
            Parent ajouterEventRoot = FXMLLoader.load(getClass().getResource("/AjouterEvent.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(ajouterEventRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterSponsor(ActionEvent event) {
        try {
            Parent ajouterSponsorRoot = FXMLLoader.load(getClass().getResource("/AjouterSponsor.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(ajouterSponsorRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAfficherSponsors(ActionEvent event) {
        try {
            Parent afficherSponsorsRoot = FXMLLoader.load(getClass().getResource("/DetailsSponsor.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Liste des Sponsors 🎉");
            stage.setScene(new Scene(afficherSponsorsRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAfficherEvenement(ActionEvent event) {
        try {
            Parent afficherEventsRoot = FXMLLoader.load(getClass().getResource("/DeatilsEvent.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Liste des Evenements 🎉");
            stage.setScene(new Scene(afficherEventsRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
