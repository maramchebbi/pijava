package Controllers;

import Models.Sponsor;
import Services.SponsorService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DetailsSponsor {

    @FXML
    private VBox sponsorFlowPane;

    @FXML
    private HBox topSponsorsContainer;

    @FXML
    private FlowPane premiumSponsorsBox;

    @FXML
    private Label totalSponsorsLabel;

    @FXML
    private StackPane loadingIndicator;

    private final SponsorService sponsorService = new SponsorService();

    public DetailsSponsor() throws SQLException {
    }

    @FXML
    public void initialize() {
        // Afficher l'indicateur de chargement si disponible
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        // Chargement des données
        loadSponsors();

        // Cacher l'indicateur de chargement
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }
    }

    private void refreshSponsors() {
        if (sponsorFlowPane != null) {
            sponsorFlowPane.getChildren().clear();
        }
        if (premiumSponsorsBox != null) {
            premiumSponsorsBox.getChildren().clear();
        }
        if (topSponsorsContainer != null) {
            topSponsorsContainer.getChildren().clear();
        }
        loadSponsors();
    }

    public void loadSponsors() {
        // Vérifier si les composants FXML sont bien injectés
        if (sponsorFlowPane == null) {
            System.err.println("ERREUR: sponsorFlowPane est null" );
            return;
        }

        sponsorFlowPane.getChildren().clear();
        if (premiumSponsorsBox != null) {
            premiumSponsorsBox.getChildren().clear();
        }
        if (topSponsorsContainer != null) {
            topSponsorsContainer.getChildren().clear();
        }

        try {
            // Charger les top 3 sponsors
            if (topSponsorsContainer != null) {
                loadTop3Sponsors();
            }

            List<Sponsor> sponsors = sponsorService.getAll();
            int totalSponsors = sponsors.size();
            int premiumCount = 0;

            // Mettre à jour le compteur total
            if (totalSponsorsLabel != null) {
                totalSponsorsLabel.setText("Total: " + totalSponsors + " sponsors" );
            }

            // Traiter tous les sponsors
            for (Sponsor sponsor : sponsors) {
                // Vérifier si c'est un sponsor premium
                boolean isPremium = sponsor.getMontant() > 5000;

                // Ajouter à la liste des sponsors premium si applicable
                if (isPremium && premiumSponsorsBox != null) {
                    createPremiumSponsorBadge(sponsor);
                    premiumCount++;
                }

                // Ajouter à la liste principale dans tous les cas
                createSponsorListItem(sponsor, isPremium);
            }

            // Si aucun sponsor premium n'a été trouvé
            if (premiumCount == 0 && premiumSponsorsBox != null) {
                Label noSponsorLabel = new Label("Aucun sponsor premium pour le moment" );
                noSponsorLabel.setStyle("-fx-text-fill: #7F8C8D;" );
                premiumSponsorsBox.getChildren().add(noSponsorLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();

            // Afficher une alerte en cas d'erreur
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement" );
            alert.setHeaderText(null);
            alert.setContentText("Impossible de charger les sponsors: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadTop3Sponsors() {
        try {
            // Récupérer les top 3 sponsors
            List<Sponsor> topSponsors = sponsorService.getTop3Sponsors();

            if (topSponsors.isEmpty()) {
                Label noDataLabel = new Label("Aucun sponsor avec événements pour le moment" );
                noDataLabel.setStyle("-fx-text-fill: #7F8C8D;" );
                topSponsorsContainer.getChildren().add(noDataLabel);
                return;
            }

            // Styles pour les médailles
            String[] medalColors = {"#FFD700", "#C0C0C0", "#CD7F32"};
            String[] rankings = {"1", "2", "3"};

            // Limiter à 3 sponsors maximum
            int count = Math.min(topSponsors.size(), 3);

            for (int i = 0; i < count; i++) {
                Sponsor sponsor = topSponsors.get(i);

                // Créer la carte du sponsor
                VBox sponsorCard = new VBox();
                sponsorCard.setAlignment(Pos.CENTER);
                sponsorCard.setSpacing(10);
                sponsorCard.setPadding(new Insets(15));
                sponsorCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;" );
                sponsorCard.setEffect(new javafx.scene.effect.DropShadow(5, Color.rgb(0, 0, 0, 0.2)));
                sponsorCard.setPrefWidth(200);

                // Badge de position
                StackPane badge = new StackPane();
                Circle circle = new Circle(20);
                circle.setFill(Color.web(medalColors[i]));
                Label rankLabel = new Label(rankings[i]);
                rankLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;" );
                badge.getChildren().addAll(circle, rankLabel);

                // Logo du sponsor
                ImageView logoView = new ImageView();
                try {
                    File file = new File("uploads/" + sponsor.getLogo());
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString());
                        logoView.setImage(image);
                    } else {
                        logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
                    }
                } catch (Exception e) {
                    try {
                        logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
                    } catch (Exception ex) {
                        // Ignorer si l'image par défaut n'est pas disponible
                    }
                }

                logoView.setFitHeight(80);
                logoView.setFitWidth(80);
                logoView.setPreserveRatio(true);

                // Nom du sponsor
                Label nameLabel = new Label(sponsor.getNom());
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;" );

                // Nombre d'événements
                Label eventsCountLabel = new Label(sponsor.getEventCount() + " événements" );
                eventsCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;" );

                // Ajouter tous les éléments à la carte
                sponsorCard.getChildren().addAll(badge, logoView, nameLabel, eventsCountLabel);

                // Ajouter la carte au conteneur
                topSponsorsContainer.getChildren().add(sponsorCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des top sponsors" );
            errorLabel.setStyle("-fx-text-fill: #E74C3C;" );
            topSponsorsContainer.getChildren().add(errorLabel);
        }
    }

    private void createPremiumSponsorBadge(Sponsor sponsor) {
        // Créer un badge pour le sponsor premium
        HBox badge = new HBox();
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setSpacing(10);
        badge.setPadding(new Insets(5, 10, 5, 10));
        badge.setStyle("-fx-background-color: #EBF5FB; -fx-background-radius: 15; " +
                "-fx-border-color: #3498DB; -fx-border-radius: 15;" );

        ImageView logoView = new ImageView();
        try {
            File file = new File("uploads/" + sponsor.getLogo());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                logoView.setImage(image);
            } else {
                logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
            }
        } catch (Exception e) {
            try {
                logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
            } catch (Exception ex) {
                // Ignorer si l'image par défaut n'est pas disponible
            }
        }

        logoView.setFitHeight(24);
        logoView.setFitWidth(24);
        logoView.setPreserveRatio(true);

        Label nameLabel = new Label(sponsor.getNom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E86C1;" );

        Label montantLabel = new Label(String.format("%.2f €", sponsor.getMontant()));
        montantLabel.setStyle("-fx-text-fill: #27AE60;" );

        badge.getChildren().addAll(logoView, nameLabel, montantLabel);
        premiumSponsorsBox.getChildren().add(badge);
    }

    private void createSponsorListItem(Sponsor sponsor, boolean isPremium) {
        // Créer un item pour chaque sponsor
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setSpacing(15);
        item.setPadding(new Insets(15));

        // Style différent selon le type de sponsor
        if (isPremium) {
            item.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2); " +
                    "-fx-border-color: #3498DB; -fx-border-width: 0 0 0 5; -fx-border-radius: 10;" );
        } else {
            item.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);" );
        }

        // Logo du sponsor
        VBox logoContainer = new VBox();
        logoContainer.setAlignment(Pos.CENTER);
        logoContainer.setPrefWidth(80);

        ImageView logoView = new ImageView();
        try {
            File file = new File("uploads/" + sponsor.getLogo());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                logoView.setImage(image);
            } else {
                logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
            }
        } catch (Exception e) {
            try {
                logoView.setImage(new Image(getClass().getResourceAsStream("/icons/sponsor-icon.png" )));
            } catch (Exception ex) {
                // Ignorer si l'image par défaut n'est pas disponible
            }
        }

        logoView.setFitHeight(60);
        logoView.setFitWidth(60);
        logoView.setPreserveRatio(true);

        logoContainer.getChildren().add(logoView);

        // Informations du sponsor
        VBox infoContainer = new VBox();
        infoContainer.setSpacing(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.setPrefWidth(250);

        Label nameLabel = new Label(sponsor.getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;" );

        Label typeLabel = new Label(sponsor.getType());
        typeLabel.setStyle("-fx-text-fill: #7F8C8D;" );

        // Afficher le nombre d'événements
        Label eventsLabel = new Label(sponsor.getEventCount() + " événements" );
        eventsLabel.setStyle("-fx-text-fill: #E67E22;" );

        infoContainer.getChildren().addAll(nameLabel, typeLabel, eventsLabel);

        // Coordonnées du sponsor
        VBox contactContainer = new VBox();
        contactContainer.setSpacing(5);
        contactContainer.setAlignment(Pos.CENTER_LEFT);
        contactContainer.setPrefWidth(200);
        contactContainer.setMinWidth(200);

        Label emailLabel = new Label("✉️ " + sponsor.getEmail());
        emailLabel.setStyle("-fx-text-fill: #34495E;" );

        Label phoneLabel = new Label("📞 " + sponsor.getTelephone());
        phoneLabel.setStyle("-fx-text-fill: #34495E;" );

        Label webLabel = new Label("🌐 " + sponsor.getSiteWeb());
        webLabel.setStyle("-fx-text-fill: #34495E;" );

        contactContainer.getChildren().addAll(emailLabel, phoneLabel, webLabel);

        // Montant et actions
        VBox actionsContainer = new VBox();
        actionsContainer.setSpacing(10);
        actionsContainer.setAlignment(Pos.CENTER);
        actionsContainer.setPrefWidth(150);

        Label montantLabel = new Label(String.format("%.2f €", sponsor.getMontant()));
        montantLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (isPremium ? "#27AE60" : "#2C3E50" ) + ";" );

        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button modifierBtn = new Button("Modifier" );
        modifierBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white;" );
        modifierBtn.setOnAction(e -> handleModifier(sponsor));

        Button supprimerBtn = new Button("Supprimer" );
        supprimerBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white;" );
        supprimerBtn.setOnAction(e -> handleSupprimer(sponsor));

        buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
        actionsContainer.getChildren().addAll(montantLabel, buttonsBox);

        // Ajout de tous les éléments à l'item
        item.getChildren().addAll(logoContainer, infoContainer, contactContainer, actionsContainer);

        // Ajout à la VBox principale
        sponsorFlowPane.getChildren().add(item);

        // Ajouter un séparateur entre les items (sauf le dernier)
        sponsorFlowPane.getChildren().add(new Region());
    }

    private void handleModifier(Sponsor sponsor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierSponsor.fxml" ));
            Parent root = loader.load();
            ModifierSponsor controller = loader.getController();
            controller.setSponsor(sponsor);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Sponsor" );

            stage.setOnHiding(event -> refreshSponsors());
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur" );
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir la fenêtre de modification: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void handleSupprimer(Sponsor sponsor) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression" );
            alert.setHeaderText(null);
            alert.setContentText("Voulez-vous vraiment supprimer ce sponsor ?" );

            if (alert.showAndWait().get() == ButtonType.OK) {
                sponsorService.delete(sponsor);
                refreshSponsors();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur" );
            alert.setHeaderText(null);
            alert.setContentText("Impossible de supprimer le sponsor: " + ex.getMessage());
            alert.showAndWait();
        }
    }








}