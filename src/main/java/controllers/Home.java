package controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import models.User;
import utils.SessionManager;
import utils.SessionPersistence;

public class Home extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        User user = SessionPersistence.loadSession();

        FXMLLoader loader;

        if (user != null) {
            // L'utilisateur est déjà connecté
            SessionManager.setCurrentUser(user);

            // Vérifier le rôle de l'utilisateur
            if ("admin".equalsIgnoreCase(user.getRole())) {
                // Si c'est un administrateur, rediriger vers AfficherUser.fxml
                loader = new FXMLLoader(getClass().getResource("/AfficherUser.fxml"));
            } else {
                // Si c'est un membre normal, rediriger vers Home.fxml
                loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            }
        } else {
            // Aucun utilisateur connecté, rediriger vers la page de connexion
            loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        }

        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Définir le titre en fonction de la page chargée
        if (user != null) {
            if ("admin".equalsIgnoreCase(user.getRole())) {
                primaryStage.setTitle("Panneau d'administration");
            } else {
                primaryStage.setTitle("Espace membre");
            }
        } else {
            primaryStage.setTitle("Connexion");
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}