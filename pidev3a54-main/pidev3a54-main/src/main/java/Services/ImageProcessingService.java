package controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
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

    // Cache pour stocker l'image originale chargée pour optimiser les performances
    private Mat cachedSourceImage;
    private String cachedImagePath;

    static {
        // Chargement de la bibliothèque native OpenCV
        try {
            nu.pattern.OpenCV.loadLocally();
            System.out.println("OpenCV chargé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement d'OpenCV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charge une image à partir du chemin spécifié
     * @param imagePath Chemin de l'image
     * @return Mat OpenCV contenant l'image
     */
    private Mat loadImage(String imagePath) {
        // Utiliser l'image en cache si le chemin correspond
        if (cachedImagePath != null && cachedImagePath.equals(imagePath) && cachedSourceImage != null) {
            return cachedSourceImage.clone(); // Clone pour éviter de modifier l'original
        }

        // Charger une nouvelle image
        Mat source = Imgcodecs.imread(imagePath);
        if (source.empty()) {
            throw new RuntimeException("Impossible de charger l'image: " + imagePath);
        }

        // Mettre à jour le cache
        cachedSourceImage = source.clone();
        cachedImagePath = imagePath;

        return source;
    }

    /**
     * Convertit un Mat OpenCV en Image JavaFX
     * @param mat Mat OpenCV
     * @return Image JavaFX
     */
    private Image matToJavaFXImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    /**
     * Applique tous les ajustements à l'image en une seule opération
     * @param imagePath Chemin de l'image
     * @param brightness Valeur de luminosité (-100 à 100)
     * @param contrast Valeur de contraste (-100 à 100)
     * @param sharpness Valeur de netteté (0 à 5)
     * @return Image JavaFX ajustée
     */
    public Image applyAdjustments(String imagePath, double brightness, double contrast, double sharpness) {
        Mat source = loadImage(imagePath);

        // Appliquer luminosité et contraste
        Mat adjusted = new Mat();
        brightness = brightness * 2.55; // Convertir de -100,100 à -255,255
        contrast = (100 + contrast) / 100.0; // Convertir en facteur (0,2)
        source.convertTo(adjusted, -1, contrast, brightness);

        // Appliquer la netteté si nécessaire
        if (sharpness > 0) {
            Mat blurred = new Mat();
            Mat sharpened = new Mat();

            // Créer un effet de netteté en soustrayant une version floue de l'image
            Imgproc.GaussianBlur(adjusted, blurred, new Size(0, 0), 3);
            Core.addWeighted(adjusted, 1 + sharpness, blurred, -sharpness, 0, sharpened);

            adjusted = sharpened;
        }

        return matToJavaFXImage(adjusted);
    }

    /**
     * Version légère pour la prévisualisation en temps réel
     */
    public Image previewAdjustments(String imagePath, double brightness, double contrast, double sharpness) {
        return applyAdjustments(imagePath, brightness, contrast, sharpness);
    }

    /**
     * Amélioration automatique de l'image (égalisation d'histogramme)
     * @param imagePath Chemin de l'image
     * @return Image JavaFX améliorée
     */
    public Image autoEnhance(String imagePath) {
        Mat source = loadImage(imagePath);
        Mat enhanced = new Mat();

        // Convertir en YCrCb pour séparer la luminance de la chrominance
        Imgproc.cvtColor(source, enhanced, Imgproc.COLOR_BGR2YCrCb);

        // Séparer les canaux
        List<Mat> channels = new ArrayList<>();
        Core.split(enhanced, channels);

        // Appliquer l'égalisation d'histogramme uniquement au canal Y (luminance)
        Imgproc.equalizeHist(channels.get(0), channels.get(0));

        // Fusionner les canaux
        Core.merge(channels, enhanced);

        // Reconvertir en BGR
        Imgproc.cvtColor(enhanced, enhanced, Imgproc.COLOR_YCrCb2BGR);

        // Appliquer un léger rehaussement des détails
        Mat sharpened = new Mat();
        Imgproc.GaussianBlur(enhanced, sharpened, new Size(0, 0), 3);
        Core.addWeighted(enhanced, 1.2, sharpened, -0.2, 0, enhanced);

        return matToJavaFXImage(enhanced);
    }

    /**
     * Détection des bords dans l'image
     * @param imagePath Chemin de l'image
     * @return Image JavaFX avec les bords détectés
     */
    public Image detectEdges(String imagePath) {
        Mat source = loadImage(imagePath);
        Mat edges = new Mat();

        // Convertir en niveaux de gris
        Imgproc.cvtColor(source, edges, Imgproc.COLOR_BGR2GRAY);

        // Réduire le bruit avec un flou gaussien
        Imgproc.GaussianBlur(edges, edges, new Size(5, 5), 1.4);

        // Détecter les bords avec Canny
        Imgproc.Canny(edges, edges, 50, 150);

        // Convertir les bords en image en noir et blanc
        Imgproc.cvtColor(edges, edges, Imgproc.COLOR_GRAY2BGR);

        return matToJavaFXImage(edges);
    }

    /**
     * Convertit l'image en noir et blanc
     * @param imagePath Chemin de l'image
     * @return Image JavaFX en noir et blanc
     */
    public Image convertToBlackAndWhite(String imagePath) {
        Mat source = loadImage(imagePath);
        Mat bwImage = new Mat();

        // Convertir en niveaux de gris
        Imgproc.cvtColor(source, bwImage, Imgproc.COLOR_BGR2GRAY);

        // Reconvertir en BGR pour l'affichage
        Imgproc.cvtColor(bwImage, bwImage, Imgproc.COLOR_GRAY2BGR);

        return matToJavaFXImage(bwImage);
    }

    /**
     * Applique un filtre sépia à l'image
     * @param imagePath Chemin de l'image
     * @return Image JavaFX avec effet sépia
     */
    public Image applySepiaFilter(String imagePath) {
        Mat source = loadImage(imagePath);
        Mat sepia = new Mat();

        // Matrice de transformation pour l'effet sépia
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        kernel.put(0, 0,
                0.272, 0.534, 0.131,
                0.349, 0.686, 0.168,
                0.393, 0.769, 0.189
        );

        // Appliquer la transformation
        Core.transform(source, sepia, kernel);

        return matToJavaFXImage(sepia);
    }

    /**
     * Applique un filtre de flou à l'image
     * @param imagePath Chemin de l'image
     * @return Image JavaFX floutée
     */
    public Image applyBlurFilter(String imagePath) {
        Mat source = loadImage(imagePath);
        Mat blurred = new Mat();

        // Appliquer un flou gaussien
        Imgproc.GaussianBlur(source, blurred, new Size(15, 15), 0);

        return matToJavaFXImage(blurred);
    }

    /**
     * Redimensionne l'image aux dimensions spécifiées
     * @param imagePath Chemin de l'image
     * @param width Nouvelle largeur
     * @param height Nouvelle hauteur
     * @return Image JavaFX redimensionnée
     */
    public Image resizeImage(String imagePath, int width, int height) {
        Mat source = loadImage(imagePath);
        Mat resized = new Mat();

        // Redimensionner l'image avec interpolation
        Imgproc.resize(source, resized, new Size(width, height), 0, 0, Imgproc.INTER_AREA);

        return matToJavaFXImage(resized);
    }
}