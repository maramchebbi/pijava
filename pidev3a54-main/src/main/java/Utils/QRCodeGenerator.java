package Utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QRCodeGenerator {


    public static void generateQRCodeFile(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        // Créer le QR code
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

        // Créer une image BufferedImage
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Remplir l'image avec les données du QR code
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = matrix.get(x, y) ? 0x000000 : 0xFFFFFF; // Noir ou blanc
                bufferedImage.setRGB(x, y, rgb);
            }
        }

        // Sauvegarder l'image
        File qrCodeFile = new File(filePath);
        javax.imageio.ImageIO.write(bufferedImage, "png", qrCodeFile);
    }

    public static Image generateQRCode(String text, int width, int height) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return image;
    }
}