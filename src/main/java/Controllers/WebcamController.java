package Controllers;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WebcamController {

    @FXML
    private ImageView cameraView;

    private Webcam webcam;
    private boolean running = true;

    @FXML
    public void initialize() {
        new Thread(() -> {
            webcam = Webcam.getDefault();
            webcam.open();
            while (running) {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    Image fxImage = javafx.embed.swing.SwingFXUtils.toFXImage(frame, null);
                    Platform.runLater(() -> cameraView.setImage(fxImage));
                }
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        }).start();
    }

    @FXML
    private void capturePhoto() {
        try {
            BufferedImage frame = webcam.getImage();
            File file = new File("captured_resized.png");
            ImageIO.write(frame, "PNG", file);

            // Ferme la webcam
            running = false;
            webcam.close();

            // Ferme la fenêtre
            Stage stage = (Stage) cameraView.getScene().getWindow();
            stage.close();

            // Appel du traitement après fermeture
            File cartoonized = MenuController.cartoonizeWithPython(file);
            MenuController.displayImage(cartoonized, MenuController.staticContentPane);  // Assure-toi que staticContentPane existe
            MenuController.saveImageAsPDF(cartoonized);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
