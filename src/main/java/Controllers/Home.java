package Controllers;

import Utils.HostServicesUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

import javafx.application.HostServices;

public class Home extends Application {

    public static HostServices hostServices;

    public static void main(String[] args) {
        launch(args);
    }

        @Override
    public void start(Stage stage) {
            hostServices = getHostServices();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherMusiqueEtPlaylists.fxml"));

            HostServicesUtil.setHostServices(getHostServices());

            Scene scene = new Scene(root); // TableBackMusic TableBackPlaylist  AfficherMusiqueEtPlaylists
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}