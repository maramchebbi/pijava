package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class HomeController {

    @FXML
    private AnchorPane contentArea; // ID utilisé dans Home.fxml

    private Object currentUser; // Utilisez le type approprié à votre modèle d'utilisateur

    public void initData(Object user) {
        this.currentUser = user;
        // Initialiser l'interface en fonction de l'utilisateur si nécessaire
    }

    @FXML
    private void handleAfficherEvent() {
       loadUI("/DeatilsEvent.fxml"); // Corrigé de "Deatils" à "Details"
    }

    @FXML
    private void handleAfficherSponsor() {
        loadUI("/DetailsSponsor.fxml");
    }

    @FXML
    private void handleAjouterSponsor() {
        loadUI("/AjouterSponsor.fxml");
    }

    @FXML
    private void handleAjouterEvent() {
       loadUI("/AjouterEvent.fxml");
    }

    @FXML
    private void handleModifierEvent() {
       loadUI("/ModifierEvent.fxml");
    }

    @FXML
    private void handleModifierSponsor() {
        loadUI("/ModifierSponsor.fxml");
    }

    @FXML
    private void handleParticipations() {
       loadUI("/ParticipationDetails.fxml");
    }

    @FXML
    private void handleAfficherRecommendations() {
       try {
            System.out.println("Tentative de chargement de Recommendations.fxml");
            URL recommendationsUrl = getClass().getResource("/Recommendations.fxml");
            if (recommendationsUrl == null) {
                throw new IOException("Impossible de trouver le fichier Recommendations.fxml dans les ressources");
            }

            // Charger la vue des recommandations
            FXMLLoader loader = new FXMLLoader(recommendationsUrl);
            Parent recommendationsView = loader.load();

            // Vider le contenu actuel
            contentArea.getChildren().clear();

            // Adapter la vue à la taille du contentArea
            AnchorPane.setTopAnchor(recommendationsView, 0.0);
            AnchorPane.setRightAnchor(recommendationsView, 0.0);
            AnchorPane.setBottomAnchor(recommendationsView, 0.0);
            AnchorPane.setLeftAnchor(recommendationsView, 0.0);

            // Ajouter la vue des recommandations dans le contentArea
            contentArea.getChildren().add(recommendationsView);
            System.out.println("Recommendations.fxml chargé avec succès");
        } catch (IOException e) {
            System.out.println("❌ Erreur lors du chargement des recommandations : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUI(String fxmlPath) {
        try {
            System.out.println("Tentative de chargement de " + fxmlPath);
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                System.out.println("URL null pour " + fxmlPath);
                // Essayez différentes variations du chemin
                System.out.println("Tentative avec chemin alternatif...");
                resourceUrl = getClass().getClassLoader().getResource(fxmlPath.substring(1)); // enlever le slash initial
                if (resourceUrl == null) {
                    throw new IOException("Impossible de trouver le fichier " + fxmlPath + " dans les ressources");
                }
            }
            System.out.println("URL de la ressource : " + resourceUrl);

            // Utilisez un FXMLLoader pour plus de contrôle
            FXMLLoader loader = new FXMLLoader(resourceUrl);
            try {
                Parent root = loader.load();
                contentArea.getChildren().setAll(root);
                System.out.println(fxmlPath + " chargé avec succès");
            } catch (Exception e) {
                System.out.println("Erreur spécifique lors du chargement : " + e.getMessage());
                if (loader.getController() != null) {
                    System.out.println("Contrôleur créé : " + loader.getController().getClass().getName());
                } else {
                    System.out.println("Aucun contrôleur n'a été créé");
                }
                throw e;
            }
        } catch (IOException e) {
            System.out.println("❌ Erreur complète lors du chargement de " + fxmlPath + " : " + e);
            e.printStackTrace();
        }
    }
}