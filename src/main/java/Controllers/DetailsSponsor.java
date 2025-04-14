package Controllers;

import Models.Sponsor;
import Services.SponsorService;
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

public class DetailsSponsor {

    @FXML
    private FlowPane sponsorFlowPane;

    private final SponsorService sponsorService = new SponsorService();

    public DetailsSponsor() throws SQLException {
    }

    @FXML
    public void initialize() {
        loadSponsors();
    }

    private void refreshSponsors() {
        sponsorFlowPane.getChildren().clear();
        initialize();
    }

    public void loadSponsors() {
        sponsorFlowPane.getChildren().clear();
        try {
            List<Sponsor> sponsors = sponsorService.getAll();
            for (Sponsor sponsor : sponsors) {
                VBox sponsorCard = new VBox(10);
                sponsorCard.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-padding: 10; -fx-background-radius: 10;");
                sponsorCard.setPrefWidth(200);

                ImageView imageView = new ImageView();
                try {
                    File file = new File("uploads/" + sponsor.getLogo());
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                    imageView.setFitWidth(180);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(true);
                } catch (Exception e) {
                    imageView.setImage(null);
                }

                Label nom = new Label("Nom : " + sponsor.getNom());
                Label type = new Label("Type : " + sponsor.getType());
                Label email = new Label("Email : " + sponsor.getEmail());
                Label telephone = new Label("Téléphone : " + sponsor.getTelephone());
                Label siteWeb = new Label("Site Web : " + sponsor.getSiteWeb());
                Label montant = new Label("Montant : " + sponsor.getMontant());

                Button modifierBtn = new Button("Modifier");
                modifierBtn.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierSponsor.fxml"));
                        Parent root = loader.load();
                        ModifierSponsor controller = loader.getController();
                        controller.setSponsor(sponsor);

                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));

                        stage.setOnHiding(event -> {
                            loadSponsors();
                        });

                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });



                Button supprimerBtn = new Button("Supprimer");
                supprimerBtn.setOnAction(e -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmation de suppression");
                        alert.setHeaderText(null);
                        alert.setContentText("Voulez-vous vraiment supprimer ce sponsor ?");
                        if (alert.showAndWait().get() == ButtonType.OK) {
                            sponsorService.delete(sponsor);
                            loadSponsors();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                sponsorCard.getChildren().addAll(imageView, nom, type, email, telephone, siteWeb, montant, modifierBtn, supprimerBtn);
                sponsorFlowPane.getChildren().add(sponsorCard);
            }
        } catch (SQLException e) {
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
    private void handleRetour(ActionEvent event) {
        try {
            Parent ajouterRoot = FXMLLoader.load(getClass().getResource("/DetailsSponsor.fxml"));
            Stage stage = (Stage) sponsorFlowPane.getScene().getWindow();
            stage.setScene(new Scene(ajouterRoot));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAfficherSponsor(ActionEvent event) {
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

