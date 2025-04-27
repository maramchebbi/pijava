package controller;

import Services.TextileApiServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class home extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage)  {
        TextileApiServer.start(); // Démarrer le serveur API

        try {
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();
            Parent root = FXMLLoader.load(getClass().getResource("/show.fxml"));
            Scene scene = new Scene(root,screenWidth,screenHeight);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println(e.getMessage()
            );        }

    }
}