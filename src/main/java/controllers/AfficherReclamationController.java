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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Reclamation;
import models.User;
import service.ReclamationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherReclamationController {

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
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setSpacing(8);
                card.setPrefWidth(220);
                card.setStyle("""
                    -fx-background-color: white;
                    -fx-border-color: #dddddd;
                    -fx-border-radius: 12;
                    -fx-background-radius: 12;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
                """);

                Label userLabel = new Label("👤 Utilisateur : " + r.getUser().getNom() + " " + r.getUser().getPrenom());
                Label emailLabel = new Label("📧 Email : " + r.getUser().getEmail());
                Label optionLabel = new Label("📌 Option : " + r.getOption());
                Label descriptionLabel = new Label("📝 Description : " + r.getDescription());

                userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
                emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
                optionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
                descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                card.getChildren().addAll(userLabel, emailLabel, optionLabel, descriptionLabel);

                Button btnDetail = new Button("🔍  Détail");
                btnDetail.setStyle("""
                    -fx-background-color: #2196F3;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-padding: 6 12;
                    -fx-background-radius: 8;
                """);
                btnDetail.setOnAction(e -> openDetailForm(r));

                VBox buttonsBox = new VBox(6);
                buttonsBox.getChildren().add(btnDetail);

                boolean isAdmin = currentUser.getRole().equalsIgnoreCase("admin");
                boolean isOwner = r.getUser().getId() == currentUser.getId();

                // Permettre la modification pour admin ou créateur
                if (isAdmin || isOwner) {
                    Button btnModifier = new Button("✏  Modifier");
                    btnModifier.setStyle("""
                        -fx-background-color: #4CAF50;
                        -fx-text-fill: white;
                        -fx-font-size: 14px;
                        -fx-padding: 6 12;
                        -fx-background-radius: 8;
                    """);
                    btnModifier.setOnAction(e -> openModifierForm(r));
                    buttonsBox.getChildren().add(btnModifier);
                }

                // Supprimer si admin ou créateur
                if (isAdmin || isOwner) {
                    Button btnSupprimer = new Button("🗑 Supprimer");
                    btnSupprimer.setStyle("""
                        -fx-background-color: #e74c3c;
                        -fx-text-fill: white;
                        -fx-font-size: 14px;
                        -fx-padding: 6 12;
                        -fx-background-radius: 8;
                    """);
                    btnSupprimer.setOnAction(e -> {
                        try {
                            service.delete(r.getId());
                            loadReclamations(); // Refresh
                            showAlert("✅ Réclamation supprimée avec succès !");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            showAlert("❌ Erreur lors de la suppression !");
                        }
                    });
                    buttonsBox.getChildren().add(btnSupprimer);
                }

                card.getChildren().add(buttonsBox);

                reclamationContainer.add(card, column, row);
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

    public void handleAddReclamation(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReclamation.fxml"));
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
    void annulerAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("Erreur retour arrière : " + e.getMessage());
        }
    }
}
