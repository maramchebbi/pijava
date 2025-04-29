package Controllers;

import Services.ReminderService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class Home extends Application {

    @FXML
    private AnchorPane contentArea; // ID utilisé dans Home.fxml

    private Stage mainStage;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        try {
            System.out.println("Tentative de chargement de Login.fxml");
            ReminderService.startReminderService();

            URL loginUrl = getClass().getResource("/Login.fxml");
            if (loginUrl == null) {
                throw new IOException("Impossible de trouver le fichier Login.fxml dans les ressources");
            }

            FXMLLoader loginLoader = new FXMLLoader(loginUrl);
            Parent loginRoot = loginLoader.load();
            Scene loginScene = new Scene(loginRoot);

            stage.setTitle("Connexion");
            stage.setScene(loginScene);
            stage.show();
            System.out.println("Login.fxml chargé avec succès");

            LoginController loginController = loginLoader.getController();

            loginController.setLoginSuccessHandler(user -> {
                try {
                    System.out.println("Tentative de chargement de Home.fxml");
                    URL homeUrl = getClass().getResource("/Home.fxml");
                    if (homeUrl == null) {
                        throw new IOException("Impossible de trouver le fichier Home.fxml dans les ressources");
                    }

                    FXMLLoader homeLoader = new FXMLLoader(homeUrl);
                    // Ne pas définir manuellement le contrôleur
                    // Laissez le FXMLLoader instancier le contrôleur défini dans le FXML

                    Parent homeRoot = homeLoader.load();
                    Scene homeScene = new Scene(homeRoot);

                    // Récupérer le contrôleur créé par FXMLLoader
                    HomeController homeController = homeLoader.getController();
                    // Passer toute information nécessaire au contrôleur
                    homeController.initData(user);

                    stage.setTitle("Accueil");
                    stage.setScene(homeScene);
                    System.out.println("Home.fxml chargé avec succès");
                } catch (Exception e) {
                    System.out.println("❌ Erreur lors du chargement de la page d'accueil : " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement de la page de connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cette méthode n'est plus nécessaire car Home ne sera plus utilisé comme contrôleur FXML
    // Les méthodes de gestion doivent être déplacées vers HomeController

    public static void main(String[] args) {
        launch(args);
    }

    @FXML
    private void openAdminDashboard() {
        try {
            System.out.println("Chargement de AdminEvents.fxml...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminEvents.fxml"));
            AnchorPane dashboardView = loader.load();

            // Remplacer le contenu du contentArea par la vue du dashboard
            contentArea.getChildren().setAll(dashboardView);

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }



}