package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur principal du panneau d'administration
 */
public class AdminPanelController {

    @FXML
    private BorderPane adminRoot;

    @FXML
    private VBox adminSidebarVBox;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private AnchorPane adminContentPane;

    // Référence au contrôleur actif pour pouvoir appeler sa méthode refresh si nécessaire
    private Object currentController;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    private void initialize() {
        // Définir le nom de l'administrateur (à remplacer par l'utilisateur réel)
        adminNameLabel.setText("Admin");

        // Charger par défaut la vue de gestion des peintures
        goToPaintingsAdmin();
    }

    /**
     * Navigue vers le dashboard
     */
    @FXML
    private void goToDashboard() {
        // À implémenter: chargement du dashboard
        setActiveButton((Button) getButtonByText("📊 Dashboard"));
        pageTitleLabel.setText("Dashboard");

        // Pour le moment, juste un placeholder
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Admin/PlaceholderDashboard.fxml"));
            Parent view = loader.load();
            setContent(view, null);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorView("Impossible de charger le dashboard");
        }
    }

    /**
     * Navigue vers la gestion des peintures
     */
    @FXML
    private void goToPaintingsAdmin() {
        setActiveButton((Button) getButtonByText("🖼 Gérer peintures"));
        pageTitleLabel.setText("Gestion des peintures");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminPaintings.fxml"));
            Parent view = loader.load();
            setContent(view, loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorView("Impossible de charger la gestion des peintures");
        }
    }

    /**
     * Navigue vers la gestion des styles
     */
    @FXML
    private void goToStylesAdmin() {
        setActiveButton((Button) getButtonByText("🎨 Gérer styles"));
        pageTitleLabel.setText("Gestion des styles");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminStyles.fxml"));
            Parent view = loader.load();
            setContent(view, loader.getController());
        } catch (IOException e) {
            e.printStackTrace();
            showErrorView("Impossible de charger la gestion des styles");
        }
    }

    /**
     * Retourne à l'application principale
     */
    @FXML
    private void goToMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) adminRoot.getScene().getWindow();
            stage.setScene(new Scene(root, 1545, 840));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // En cas d'erreur critique, afficher un message et ne pas quitter l'admin
            showErrorView("Impossible de retourner à l'application principale");
        }
    }

    /**
     * Rafraîchit le contenu actuel
     */
    @FXML
    private void refreshContent() {
        if (currentController instanceof Refreshable) {
            ((Refreshable) currentController).refresh();
        }
    }

    /**
     * Définit le contenu principal du panneau d'administration
     */
    private void setContent(Parent view, Object controller) {
        adminContentPane.getChildren().clear();
        adminContentPane.getChildren().add(view);

        // Ajuster la taille pour remplir l'espace
        AnchorPane.setTopAnchor(view, 0.0);
        AnchorPane.setRightAnchor(view, 0.0);
        AnchorPane.setBottomAnchor(view, 0.0);
        AnchorPane.setLeftAnchor(view, 0.0);

        // Stocker le contrôleur actif
        currentController = controller;
    }

    /**
     * Affiche une vue d'erreur simple en cas de problème
     */
    private void showErrorView(String message) {
        VBox errorBox = new VBox();
        errorBox.setAlignment(javafx.geometry.Pos.CENTER);
        errorBox.setSpacing(20);
        errorBox.getStyleClass().add("admin-error-view");

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.getStyleClass().add("admin-error-message");

        Button retryButton = new Button("🔄 Réessayer");
        retryButton.getStyleClass().add("admin-retry-btn");
        retryButton.setOnAction(e -> refreshContent());

        errorBox.getChildren().addAll(errorLabel, retryButton);

        adminContentPane.getChildren().clear();
        adminContentPane.getChildren().add(errorBox);

        AnchorPane.setTopAnchor(errorBox, 0.0);
        AnchorPane.setRightAnchor(errorBox, 0.0);
        AnchorPane.setBottomAnchor(errorBox, 0.0);
        AnchorPane.setLeftAnchor(errorBox, 0.0);
    }

    /**
     * Récupère un bouton par son texte
     */
    private javafx.scene.Node getButtonByText(String text) {
        for (javafx.scene.Node node : adminSidebarVBox.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().equals(text)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Définit un bouton comme actif dans le menu
     */
    private void setActiveButton(Button button) {
        // Réinitialiser tous les boutons
        for (javafx.scene.Node node : adminSidebarVBox.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("admin-menu-btn-active");
            }
        }

        // Activer le bouton sélectionné
        if (button != null) {
            button.getStyleClass().add("admin-menu-btn-active");
        }
    }

    /**
     * Interface pour les contrôleurs qui supportent le rafraîchissement
     */
    public interface Refreshable {
        void refresh();
    }
}