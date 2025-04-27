package Controllers;

import Models.Event;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapViewer {
    private final Event event;

    public MapViewer(Event event) {
        this.event = event;
    }

    public void showMap() {
        try {
            // Vérifier si les coordonnées sont valides
            if (event.getLatitude() == null || event.getLongitude() == null
                    || event.getLatitude().compareTo(BigDecimal.ZERO) == 0
                    || event.getLongitude().compareTo(BigDecimal.ZERO) == 0) {
                System.err.println("Aucune coordonnée disponible pour cet événement");
                return;
            }

            // Convertir les coordonnées BigDecimal en double
            double lat = event.getLatitude().doubleValue();
            double lng = event.getLongitude().doubleValue();

            // Option 1: Ouvrir directement Google Maps dans le navigateur
            // Desktop.getDesktop().browse(new URI("https://www.google.com/maps?q=" + lat + "," + lng));

            // Option 2: Créer un fichier HTML temporaire et l'ouvrir dans le navigateur
            String htmlContent = createMapHtml(lat, lng);
            File tempFile = File.createTempFile("map_event_" + event.getId() + "_", ".html");
            tempFile.deleteOnExit(); // Le fichier sera supprimé à la fermeture de l'application

            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(htmlContent);
            }

            // Ouvrir le fichier dans le navigateur par défaut
            Desktop.getDesktop().browse(tempFile.toURI());

            System.out.println("Carte ouverte dans le navigateur: " + tempFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    private String createMapHtml(double lat, double lng) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>Localisation de l'événement: " + event.getTitre() + "</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <style>\n" +
                "        body, html {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            height: 100%;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        #header {\n" +
                "            padding: 15px;\n" +
                "            background-color: #f5f5f5;\n" +
                "        }\n" +
                "        #map {\n" +
                "            height: calc(100% - 100px);\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"header\">\n" +
                "        <h2>" + escapeHtml(event.getTitre()) + "</h2>\n" +
                "        <p>📍 " + escapeHtml(event.getLocalisation()) + "</p>\n" +
                "        <p>Coordonnées: " + lat + ", " + lng + "</p>\n" +
                "    </div>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([" + lat + ", " + lng + "], 15);\n" +
                "        \n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "            attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n" +
                "        }).addTo(map);\n" +
                "        \n" +
                "        var marker = L.marker([" + lat + ", " + lng + "]).addTo(map);\n" +
                "        marker.bindPopup(\"<b>" + escapeJs(event.getTitre()) + "</b><br>" + escapeJs(event.getLocalisation()) + "\").openPopup();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
    }

    // Méthode pour échapper les caractères spéciaux dans les chaînes JavaScript
    private String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // Méthode pour échapper les caractères spéciaux en HTML
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}