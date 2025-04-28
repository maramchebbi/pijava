package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.net.URL; // Importation ajoutée
import java.io.IOException;


public class home extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("Trying to load Add.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();
            Scene scene = new Scene(root,screenWidth,screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
