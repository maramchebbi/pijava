package Utils;

import Models.textile;
import Services.TextileService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HtmlGenerator {

    private static final String HTML_OUTPUT_DIR = "textile_pages";

    public static void generateHtmlForAllTextiles() throws SQLException, IOException {
        TextileService textileService = new TextileService();
        List<textile> allTextiles = textileService.getAll();

        // Créer le répertoire de sortie s'il n'existe pas
        File outputDir = new File(HTML_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (textile t : allTextiles) {
            generateHtmlForTextile(t, outputDir);
        }

        System.out.println("Génération des pages HTML terminée. " + allTextiles.size() + " pages générées dans: " + outputDir.getAbsolutePath());
    }

    public static void generateHtmlForTextile(textile t, File outputDir) throws IOException {
        // Créer le contenu HTML pour ce textile - version améliorée avec un design responsive
        String htmlContent =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"fr\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>" + t.getNom() + " - Détails du Textile</title>\n" +
                        "    <style>\n" +
                        "        :root {\n" +
                        "            --primary: #603813;\n" +
                        "            --secondary: #b29f94;\n" +
                        "            --light: #f5f2f0;\n" +
                        "            --text: #333333;\n" +
                        "        }\n" +
                        "        body {\n" +
                        "            font-family: 'Segoe UI', Arial, sans-serif;\n" +
                        "            margin: 0;\n" +
                        "            padding: 0;\n" +
                        "            background-color: var(--light);\n" +
                        "            color: var(--text);\n" +
                        "        }\n" +
                        "        .container {\n" +
                        "            max-width: 800px;\n" +
                        "            margin: 0 auto;\n" +
                        "            padding: 20px;\n" +
                        "        }\n" +
                        "        header {\n" +
                        "            background-color: var(--primary);\n" +
                        "            color: white;\n" +
                        "            padding: 20px 0;\n" +
                        "            text-align: center;\n" +
                        "            box-shadow: 0 2px 5px rgba(0,0,0,0.2);\n" +
                        "        }\n" +
                        "        h1 {\n" +
                        "            margin: 0;\n" +
                        "            font-size: 28px;\n" +
                        "        }\n" +
                        "        .textile-card {\n" +
                        "            background: white;\n" +
                        "            border-radius: 10px;\n" +
                        "            box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
                        "            margin: 20px 0;\n" +
                        "            overflow: hidden;\n" +
                        "        }\n" +
                        "        .textile-header {\n" +
                        "            background-color: var(--secondary);\n" +
                        "            color: white;\n" +
                        "            padding: 15px 20px;\n" +
                        "        }\n" +
                        "        .textile-content {\n" +
                        "            padding: 20px;\n" +
                        "        }\n" +
                        "        .property {\n" +
                        "            margin-bottom: 15px;\n" +
                        "            border-bottom: 1px solid #eee;\n" +
                        "            padding-bottom: 10px;\n" +
                        "        }\n" +
                        "        .property:last-child {\n" +
                        "            border-bottom: none;\n" +
                        "        }\n" +
                        "        .label {\n" +
                        "            font-weight: bold;\n" +
                        "            color: var(--primary);\n" +
                        "            display: block;\n" +
                        "            margin-bottom: 5px;\n" +
                        "        }\n" +
                        "        .type-badge {\n" +
                        "            display: inline-block;\n" +
                        "            background-color: var(--primary);\n" +
                        "            color: white;\n" +
                        "            padding: 5px 10px;\n" +
                        "            border-radius: 15px;\n" +
                        "            font-size: 14px;\n" +
                        "            margin-top: 10px;\n" +
                        "        }\n" +
                        "        footer {\n" +
                        "            background-color: var(--primary);\n" +
                        "            color: white;\n" +
                        "            text-align: center;\n" +
                        "            padding: 15px 0;\n" +
                        "            font-size: 12px;\n" +
                        "            margin-top: 20px;\n" +
                        "        }\n" +
                        "        @media (max-width: 600px) {\n" +
                        "            .container {\n" +
                        "                padding: 10px;\n" +
                        "            }\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <header>\n" +
                        "        <h1>Détails du Textile</h1>\n" +
                        "    </header>\n" +
                        "    <div class=\"container\">\n" +
                        "        <div class=\"textile-card\">\n" +
                        "            <div class=\"textile-header\">\n" +
                        "                <h2>" + t.getNom() + "</h2>\n" +
                        "                <span class=\"type-badge\">" + t.getType() + "</span>\n" +
                        "            </div>\n" +
                        "            <div class=\"textile-content\">\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Description</span>\n" +
                        "                    <div>" + t.getDescription() + "</div>\n" +
                        "                </div>\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Matière</span>\n" +
                        "                    <div>" + t.getMatiere() + "</div>\n" +
                        "                </div>\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Dimension</span>\n" +
                        "                    <div>" + t.getDimension() + "</div>\n" +
                        "                </div>\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Couleur</span>\n" +
                        "                    <div>" + t.getCouleur() + "</div>\n" +
                        "                </div>\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Créateur</span>\n" +
                        "                    <div>" + t.getCreateur() + "</div>\n" +
                        "                </div>\n" +
                        "                <div class=\"property\">\n" +
                        "                    <span class=\"label\">Technique</span>\n" +
                        "                    <div>" + t.getTechnique() + "</div>\n" +
                        "                </div>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "    <footer>\n" +
                        "        &copy; " + java.time.Year.now().getValue() + " Système de Gestion Textile - Tous droits réservés\n" +
                        "    </footer>\n" +
                        "</body>\n" +
                        "</html>";

        // Écrire le fichier HTML
        File outputFile = new File(outputDir, t.getId() + ".html");
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(htmlContent);
        }
    }
}