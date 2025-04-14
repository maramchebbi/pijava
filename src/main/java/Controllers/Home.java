package Controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeatilsEvent.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            stage.setTitle("Liste des Événements");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.out.println("❌ Erreur lors du chargement de l'interface : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
