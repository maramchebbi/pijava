package Controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

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
            Parent root = FXMLLoader.load(getClass().getResource("/menu.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/ss.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}