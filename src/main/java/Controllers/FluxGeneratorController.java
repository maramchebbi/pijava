package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class FluxGeneratorController {

    @FXML private TextField promptField;
    @FXML private Button generateButton;
    @FXML private ImageView imageView;
    @FXML private Label statusLabel;

    @FXML
    public void generateImage() {
        String prompt = promptField.getText();
        if (prompt.isEmpty()) {
            statusLabel.setText("Veuillez entrer un texte pour générer une image !");
            return;
        }

        try {
            statusLabel.setText("Génération en cours...");

            // === Simulation d'appel Python / Torch (à remplacer par un vrai appel) ===
            ProcessBuilder builder = new ProcessBuilder(
                    "python", "generateAI.py", prompt
            );
            builder.directory(new File("ImageAI/generateAI.py")); // ajuste ce chemin
            Process process = builder.start();
            process.waitFor();

            File file = new File("chemin/vers/ton/script/flux-schnell.png");
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                statusLabel.setText("Image générée avec succès !");
            } else {
                statusLabel.setText("Erreur : L'image n'a pas été trouvée.");
            }

        } catch (IOException | InterruptedException e) {
            statusLabel.setText("Erreur lors de la génération : " + e.getMessage());
        }
    }
}
