package Services;

import Models.textile;
import Utils.HtmlGenerator;
import Utils.QRCodeGenerator;
import com.google.zxing.WriterException;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class TextileQRService {
    private static final String QR_CODE_DIRECTORY = "qrcodes";
    private static final int QR_CODE_SIZE = 300;
    private static final String HTML_OUTPUT_DIR = "textile_pages";

    // URL de base pour GitHub Pages - IMPORTANT: il doit se terminer par un slash (/)
    private static final String BASE_URL = "https://ghalya3001.github.io/textile-details/";

    private final TextileService textileService;

    public TextileQRService() {
        textileService = new TextileService();
        try {
            // Créer les répertoires nécessaires
            Files.createDirectories(Paths.get(QR_CODE_DIRECTORY));
            Files.createDirectories(Paths.get(HTML_OUTPUT_DIR));

            // Générer les pages HTML pour tous les textiles
            HtmlGenerator.generateHtmlForAllTextiles();
            System.out.println("Pages HTML générées avec succès dans: " + new File(HTML_OUTPUT_DIR).getAbsolutePath());
        } catch (IOException | SQLException e) {
            System.err.println("Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère un QR code pour un textile spécifique
     * @param textileId L'ID du textile
     * @return L'image du QR code
     */
    public Image generateQRCodeForTextile(int textileId) throws SQLException, WriterException {
        // Récupérer les informations du textile
        textile t = textileService.getById(textileId);
        if (t == null) {
            throw new SQLException("Textile non trouvé avec l'ID: " + textileId);
        }

        // Construire l'URL vers la page HTML du textile
        String qrCodeUrl = BASE_URL + t.getId() + ".html";
        System.out.println("Générant QR Code avec URL: " + qrCodeUrl);

        // Générer un QR code contenant l'URL plutôt que les informations directes
        return QRCodeGenerator.generateQRCode(qrCodeUrl, QR_CODE_SIZE, QR_CODE_SIZE);
    }




}