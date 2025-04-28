package controller;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ImageProcessingService {

    static {
        // Chargement de la bibliothèque native OpenCV
        nu.pattern.OpenCV.loadLocally();
        // Ou pour JavaCV: System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * Ajuste la luminosité et le contraste d'une image
     * @param imagePath Chemin de l'image
     * @param brightness Valeur de luminosité (-100 à 100)
     * @param contrast Valeur de contraste (-100 à 100)
     * @return Image JavaFX modifiée
     */
    public Image adjustBrightnessContrast(String imagePath, double brightness, double contrast) {
        // Charger l'image avec OpenCV
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        // Normaliser les valeurs de contraste et luminosité
        brightness = brightness * 2.55; // Convertir de -100,100 à -255,255
        contrast = (100 + contrast) / 100.0; // Convertir en facteur (0,2)

        // Créer une nouvelle matrice pour le résultat
        Mat destination = new Mat(source.size(), source.type());

        // Appliquer la transformation: nouveauPixel = contraste * pixel + luminosité
        source.convertTo(destination, -1, contrast, brightness);

        // Convertir le résultat en Image JavaFX
        return matToJavaFXImage(destination);
    }

    /**
     * Améliore automatiquement une image en ajustant les niveaux et le contraste
     * @param imagePath Chemin de l'image
     * @return Image JavaFX améliorée
     */
    public Image autoEnhance(String imagePath) {
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        // Convertir en LAB pour séparer la luminance des couleurs
        Mat labImage = new Mat();
        Imgproc.cvtColor(source, labImage, Imgproc.COLOR_BGR2Lab);

        // Séparer les canaux
        List<Mat> labChannels = new ArrayList<>(3);
        Core.split(labImage, labChannels);

        // Appliquer CLAHE (Contrast Limited Adaptive Histogram Equalization) au canal L
        Mat enhancedL = new Mat();
        Imgproc.equalizeHist(labChannels.get(0), enhancedL);

        // Remplacer le canal L
        labChannels.set(0, enhancedL);

        // Fusionner les canaux
        Mat enhancedLab = new Mat();
        Core.merge(labChannels, enhancedLab);

        // Convertir en BGR puis RGB
        Mat result = new Mat();
        Imgproc.cvtColor(enhancedLab, result, Imgproc.COLOR_Lab2BGR);
        Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGB);

        return matToJavaFXImage(result);
    }

    /**
     * Ajuste la balance des couleurs d'une image
     * @param imagePath Chemin de l'image
     * @param redOffset Ajustement pour le rouge (-100 à 100)
     * @param greenOffset Ajustement pour le vert (-100 à 100)
     * @param blueOffset Ajustement pour le bleu (-100 à 100)
     * @return Image JavaFX modifiée
     */
    public Image adjustColorBalance(String imagePath, double redOffset, double greenOffset, double blueOffset) {
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        // Normaliser les offsets
        redOffset = redOffset * 2.55;
        greenOffset = greenOffset * 2.55;
        blueOffset = blueOffset * 2.55;

        // Séparer les canaux
        List<Mat> bgrChannels = new ArrayList<>(3);
        Core.split(source, bgrChannels);

        // Ajuster chaque canal
        Mat blueChannel = bgrChannels.get(0);
        Mat greenChannel = bgrChannels.get(1);
        Mat redChannel = bgrChannels.get(2);

        Core.add(blueChannel, new Scalar(blueOffset), blueChannel);
        Core.add(greenChannel, new Scalar(greenOffset), greenChannel);
        Core.add(redChannel, new Scalar(redOffset), redChannel);

        // Fusionner les canaux
        Mat result = new Mat();
        Core.merge(bgrChannels, result);

        // Convertir en RGB pour JavaFX
        Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGB);

        return matToJavaFXImage(result);
    }

    /**
     * Applique un filtre de netteté à l'image
     * @param imagePath Chemin de l'image
     * @param amount Intensité du filtre (0-5)
     * @return Image JavaFX avec netteté améliorée
     */
    public Image sharpenImage(String imagePath, double amount) {
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        // Créer un noyau de netteté
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0, 0, -amount, 0, -amount, 1 + 4 * amount, -amount, 0, -amount, 0);

        // Appliquer le filtre
        Mat destination = new Mat();
        Imgproc.filter2D(source, destination, -1, kernel);

        // Convertir en RGB pour JavaFX
        Imgproc.cvtColor(destination, destination, Imgproc.COLOR_BGR2RGB);

        return matToJavaFXImage(destination);
    }

    /**
     * Sauvegarde une image modifiée
     * @param image Image JavaFX à sauvegarder
     * @param outputPath Chemin de sauvegarde
     */
    public void saveImage(Mat image, String outputPath) {
        Imgcodecs.imwrite(outputPath, image);
    }

    /**
     * Redimensionne une image
     * @param imagePath Chemin de l'image
     * @param width Nouvelle largeur
     * @param height Nouvelle hauteur
     * @return Image JavaFX redimensionnée
     */
    public Image resizeImage(String imagePath, int width, int height) {
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        Mat resized = new Mat();
        Size size = new Size(width, height);
        Imgproc.resize(source, resized, size, 0, 0, Imgproc.INTER_AREA);

        // Convertir en RGB pour JavaFX
        Imgproc.cvtColor(resized, resized, Imgproc.COLOR_BGR2RGB);

        return matToJavaFXImage(resized);
    }

    /**
     * Détecte les bords dans une image
     * @param imagePath Chemin de l'image
     * @return Image JavaFX avec détection de bords
     */
    public Image detectEdges(String imagePath) {
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new IllegalArgumentException("Impossible de charger l'image: " + imagePath);
        }

        // Convertir en niveaux de gris
        Mat grayImage = new Mat();
        Imgproc.cvtColor(source, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Appliquer le détecteur de bords Canny
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges, 50, 150);

        // Convertir en image à 3 canaux pour JavaFX
        Mat edgesColored = new Mat();
        Imgproc.cvtColor(edges, edgesColored, Imgproc.COLOR_GRAY2RGB);

        return matToJavaFXImage(edgesColored);
    }

    /**
     * Convertit une matrice OpenCV en image JavaFX
     * @param mat Matrice OpenCV (RGB)
     * @return Image JavaFX
     */
    private Image matToJavaFXImage(Mat mat) {
        // Créer un tampon pour l'image
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);

        // Convertir en InputStream
        byte[] byteArray = buffer.toArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);

        // Créer l'image JavaFX
        return new Image(inputStream);
    }
}