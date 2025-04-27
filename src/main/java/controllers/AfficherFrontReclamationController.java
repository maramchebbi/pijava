package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Reclamation;
import models.User;
import service.ReclamationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherFrontReclamationController {

    @FXML
    private GridPane reclamationContainer;

    @FXML
    private Button addReclamationButton;

    @FXML
    public void initialize() {
        loadReclamations();
    }

    private void loadReclamations() {
        ReclamationService service = new ReclamationService();
        User currentUser = SessionManager.getCurrentUser();

        reclamationContainer.getChildren().clear();

        try {
            List<Reclamation> reclamations = service.select();
            int column = 0;
            int row = 0;

            for (Reclamation r : reclamations) {
                // Création du conteneur principal de la carte
                VBox card = new VBox(10);
                card.setPadding(new Insets(0));
                card.setPrefWidth(300);
                card.setMinHeight(220);
                card.setMaxHeight(300);
                card.setStyle("""
            -fx-background-color: #F5F5DC;
            -fx-border-radius: 8px;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);
        """);

                // Entête de la carte avec statut et date
                HBox header = new HBox();
                header.setPadding(new Insets(12, 15, 12, 15));
                header.setSpacing(10);
                header.setStyle("""
            -fx-background-color: #EEE8AA;
            -fx-background-radius: 8px 8px 0 0;
            -fx-border-color: #D2B48C;
            -fx-border-width: 0 0 1 0;
        """);

                // Indicateur de statut
                Label statusLabel = new Label("⬤");

                // Titre de la réclamation (option)
                Label titleLabel = new Label(r.getOption());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #654321; -fx-font-size: 14px;");
                HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

                header.getChildren().addAll(statusLabel, titleLabel);

                // Contenu principal
                VBox content = new VBox(8);
                content.setPadding(new Insets(15));

                // Information utilisateur avec style moderne
                HBox userInfo = new HBox(10);
                userInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                // Cercle avec initiales (simulé avec un label)
                Label initialsLabel = new Label(getInitials(r.getUser().getPrenom(), r.getUser().getNom()));
                initialsLabel.setPrefSize(36, 36);
                initialsLabel.setAlignment(javafx.geometry.Pos.CENTER);
                initialsLabel.setStyle("""
            -fx-background-color: #DEB887;
            -fx-text-fill: #8B4513;
            -fx-font-weight: bold;
            -fx-background-radius: 50%;
        """);

                VBox userDetails = new VBox(2);
                Label userName = new Label(r.getUser().getPrenom() + " " + r.getUser().getNom());
                userName.setStyle("-fx-font-weight: bold; -fx-text-fill: #654321; -fx-font-size: 13px;");

                Label userEmail = new Label(r.getUser().getEmail());
                userEmail.setStyle("-fx-text-fill: #8B7355; -fx-font-size: 12px;");

                userDetails.getChildren().addAll(userName, userEmail);
                userInfo.getChildren().addAll(initialsLabel, userDetails);

                // Description de la réclamation
                Label descriptionLabel = new Label(truncateText(r.getDescription(), 120));
                descriptionLabel.setWrapText(true);
                descriptionLabel.setStyle("-fx-text-fill: #5D4037; -fx-font-size: 13px;");

                content.getChildren().addAll(userInfo, new Separator(), descriptionLabel);

                // Barre d'actions pour les boutons
                HBox actions = new HBox();
                actions.setPadding(new Insets(10, 15, 15, 15));
                actions.setSpacing(8);
                actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                // Bouton détail avec style beige
                Button btnDetail = new Button("Détails");
                styleButton(btnDetail, "detail", false);
                btnDetail.setOnAction(e -> openDetailForm(r));

                actions.getChildren().add(btnDetail);

                boolean isAdmin = currentUser.getRole().equalsIgnoreCase("admin");
                boolean isOwner = r.getUser().getId() == currentUser.getId();

                // Actions conditionnelles selon les droits
                if (isAdmin || isOwner) {
                    Button btnModifier = new Button("Modifier");
                    styleButton(btnModifier, "modifier", false);
                    btnModifier.setOnAction(e -> openModifierForm(r));

                    Button btnSupprimer = new Button("Supprimer");
                    styleButton(btnSupprimer, "supprimer", true);
                    btnSupprimer.setOnAction(e -> {
                        try {
                            service.delete(r.getId());
                            loadReclamations();
                            showAlert("Réclamation supprimée avec succès !");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            showAlert("Erreur lors de la suppression !");
                        }
                    });

                    actions.getChildren().addAll(btnModifier, btnSupprimer);
                }

                // Assemblage des éléments
                card.getChildren().addAll(header, content, actions);
                reclamationContainer.add(card, column, row);

                // Navigation dans la grille
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur lors du chargement des réclamations");
        }
    }
    // Méthode utilitaire pour obtenir les initiales
    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(firstName.charAt(0));
        }
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(lastName.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    // Méthode pour tronquer le texte
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    // Style uniforme pour les boutons avec couleurs beige
    private void styleButton(Button button, String buttonType, boolean outline) {
        String baseColor;

        // Définir les couleurs beige en fonction du type de bouton
        switch (buttonType) {
            case "detail":
                baseColor = "#A0522D"; // Marron-sienna pour détails
                break;
            case "modifier":
                baseColor = "#8B4513"; // Marron pour modifier
                break;
            case "supprimer":
                baseColor = "#B22222"; // Rouge brique pour supprimer
                break;
            default:
                baseColor = "#8B4513"; // Marron par défaut
        }

        if (outline) {
            button.setStyle(String.format("""
        -fx-background-color: transparent;
        -fx-border-color: %s;
        -fx-text-fill: %s;
        -fx-border-radius: 4px;
        -fx-background-radius: 4px;
        -fx-font-size: 12px;
        -fx-cursor: hand;
        -fx-padding: 5px 10px;
    """, baseColor, baseColor));
        } else {
            button.setStyle(String.format("""
        -fx-background-color: %s;
        -fx-text-fill: #F5F5DC;
        -fx-background-radius: 4px;
        -fx-font-size: 12px;
        -fx-cursor: hand;
        -fx-padding: 5px 10px;
    """, baseColor));
        }
    }
    private void openModifierForm(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_reclamation.fxml"));
            Parent root = loader.load();

            ModifierReclamationController controller = loader.getController();
            controller.setReclamation(reclamation);

            Stage stage = new Stage();
            stage.setTitle("Modifier Réclamation");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadReclamations();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddReclamation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterFrontReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une réclamation");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDetailForm(Reclamation reclamation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detail_reclamation.fxml"));
            Parent root = loader.load();

            DetailReclamationController controller = loader.getController();
            controller.setReclamation(reclamation);

            Stage stage = new Stage();
            stage.setTitle("Détail Réclamation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void retourAccueil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AccueilFront.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur retour arrière : " + e.getMessage());
        }
    }
}