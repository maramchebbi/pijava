package Controllers;

import com.github.sarxos.webcam.Webcam;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.stream.Collectors;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;

public class MenuController {

    @FXML
    private AnchorPane contentPane;

    public static AnchorPane staticContentPane;

    @FXML
    private void initialize() {
        // Initialisation de staticContentPane
        staticContentPane = contentPane;

        // Crée un objet Text
        Text welcomeText = new Text("Bienvenue dans l'interface Artisfera !");
        welcomeText.setFont(Font.font("Segoe UI", 30));
        welcomeText.setFill(Color.web("#3498db"));
        welcomeText.setStyle("-fx-font-weight: bold;");

        // Ajoute le texte au contentPane
        contentPane.getChildren().add(welcomeText);

        // Centre le texte sur le contentPane
        centerText(welcomeText);

        // Applique un effet de fade-in au texte
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), welcomeText);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);
        fadeIn.play();
    }

    private void centerText(Text text) {
        contentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double x = (newVal.doubleValue() - text.getBoundsInLocal().getWidth()) / 2;
            AnchorPane.setLeftAnchor(text, x);
        });
        contentPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double y = (newVal.doubleValue() - text.getBoundsInLocal().getHeight()) / 2;
            AnchorPane.setTopAnchor(text, y);
        });
    }

    public void goToAjouterPeinture() throws IOException {
        setContent("AjouterPeinture.fxml");
    }

    public void goToAfficherPeintures() throws IOException {
        setContent("AfficherPeinture.fxml");
    }

    public void goToAjouterStyle() throws IOException {
        setContent("AjouterStyle.fxml");
    }

    public void goToAfficherStyles() throws IOException {
        setContent("AfficherStyle.fxml");
    }

    @FXML
    private void goToChatbot() throws IOException {
        setContent("chatbot.fxml");
    }

    @FXML
    private void handleGhibliEffect(ActionEvent event) {
        try {
            // Charge le fichier FXML pour la nouvelle fenêtre
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WebcamView.fxml"));
            Parent root = loader.load();

            // Crée une nouvelle fenêtre (Stage)
            Stage stage = new Stage();
            stage.setTitle("Prendre une photo");

            // Définir la scène
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Montre la nouvelle fenêtre
            stage.show();

            // Si nécessaire, tu peux récupérer le contrôleur de la nouvelle fenêtre
            WebcamController webcamController = loader.getController();
            System.out.println("Contrôleur de la fenêtre WebcamView : " + webcamController);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de la fenêtre : " + e.getMessage());
        }
    }


    @FXML
    private void handleCaptureWebcam(ActionEvent event) {
        try {
            File capturedImage = capturePhotoFromWebcam(); // tu peux remplacer cette méthode par ton script
            displayImage(capturedImage, contentPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File capturePhotoFromWebcam() throws IOException, InterruptedException {
        Dimension[] resolutions = new Dimension[] {
                new Dimension(1280, 720),
                new Dimension(1920, 1080)
        };

        Webcam webcam = Webcam.getDefault();
        webcam.setCustomViewSizes(resolutions);
        webcam.setViewSize(new Dimension(1280, 720));
        webcam.open();

        Thread.sleep(500); // petite pause pour une image claire

        BufferedImage image = webcam.getImage();

        // Optionnel : amélioration de netteté
        float[] sharpenMatrix = {
                0.0f, -1.0f,  0.0f,
                -1.0f,  5.0f, -1.0f,
                0.0f, -1.0f,  0.0f
        };
        BufferedImageOp sharpen = new ConvolveOp(new Kernel(3, 3, sharpenMatrix));
        BufferedImage sharpenedImage = sharpen.filter(image, null);

        File file = new File("captured_resized.png");
        ImageIO.write(sharpenedImage, "PNG", file);
        webcam.close();
        return file;
    }


    public static File cartoonizeWithPython(File imageFile) throws IOException, InterruptedException {
        // Vérification du chemin du script Python
        String pythonScriptPath = new File("chatbot/cartoonize.py").getAbsolutePath();
        if (!new File(pythonScriptPath).exists()) {
            throw new FileNotFoundException("Le script Python 'cartoonize.py' est introuvable.");
        }

        // Execution du script Python
        ProcessBuilder pb = new ProcessBuilder("C:\\Users\\marwe\\IdeaProjects\\javap2\\chatbot\\venv\\Scripts\\python.exe",
                pythonScriptPath, imageFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder outputLog = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[PYTHON] " + line);
            outputLog.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Erreur script Python : \n" + outputLog);
        }

        File output = new File("result.png");
        if (!output.exists()) {
            throw new FileNotFoundException("L'image résultante 'result.png' est introuvable !");
        }

        return output;
    }

    public static void saveImageAsPDF(File imageFile) throws Exception {
        String outputPath = "image_artisfera.pdf";
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();
        com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageFile.getAbsolutePath());
        image.scaleToFit(document.getPageSize().getWidth() - 50, document.getPageSize().getHeight() - 50);
        image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER);
        document.add(image);
        document.close();
        Desktop.getDesktop().open(new File(outputPath));
    }

    // Méthode dans MenuController
    public static void displayImage(File imageFile, AnchorPane targetPane) {
        if (imageFile == null || !imageFile.exists()) {
            System.out.println("L'image n'existe pas !");
            return;
        }

        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(500);
        imageView.setPreserveRatio(true);

        Platform.runLater(() -> {
            targetPane.getChildren().clear();  // Efface les éléments précédents
            targetPane.getChildren().add(imageView);  // Ajoute la nouvelle image
        });
    }

    private void setContent(String fxml) throws IOException {
        Parent pane = FXMLLoader.load(getClass().getResource("/" + fxml));
        if (pane instanceof AnchorPane) {
            contentPane.getChildren().setAll(pane);
        } else if (pane instanceof BorderPane) {
            contentPane.getChildren().setAll(((BorderPane) pane).getCenter());
        } else {
            contentPane.getChildren().setAll(pane);
        }
    }

    public File openCameraAndCapture() throws IOException {
        // Lance la webcam native (Windows) – équivalent de "Caméra" app
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "start microsoft.windows.camera:");
        pb.start();

        // Affiche un popup pour demander à l'utilisateur de coller l'image manuellement après la capture
        System.out.println("Veuillez capturer une image avec l'application Caméra, puis l'enregistrer comme 'captured.png' dans le dossier du projet.");

        File captured = new File("captured.png");

        // Attendre que l'image apparaisse (ou boucle jusqu'à ce qu'elle existe)
        while (!captured.exists()) {
            System.out.println("En attente de la capture d'image...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return captured;
    }
}
