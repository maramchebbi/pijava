package Controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Home extends Application {

    static {
        System.load(new File("chatbot/opencv_java4110.dll").getAbsolutePath());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminPanel.fxml")); // ou /fxml/AdminPanel.fxml si déplacé
            Scene scene = new Scene(root, 1545, 840);
            scene.getStylesheets().add(getClass().getResource("/ss.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // mieux pour voir l'erreur complète
        }
    }
}
