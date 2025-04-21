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
            loader = new FXMLLoader(getClass().getResource("/Afficheruser.fxml"));
        } else {
            // Aucun utilisateur connecté
            loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        }

        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.setTitle("Gestion Utilisateur");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
