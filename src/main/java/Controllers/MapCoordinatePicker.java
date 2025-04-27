package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cette classe permet de sélectionner des coordonnées directement sur une carte OpenStreetMap
 * qui est affichée dans le navigateur par défaut de l'utilisateur.
 */
public class MapCoordinatePicker {

    private TextField latitudeField;
    private TextField longitudeField;
    private String location;
    private ServerSocket serverSocket;
    private boolean serverRunning = false;
    private int port = 8989; // Port par défaut, sera modifié si occupé

    /**
     * Ouvre une fenêtre avec OpenStreetMap pour sélectionner des coordonnées
     *
     * @param latitudeField Champ où sera stockée la latitude
     * @param longitudeField Champ où sera stockée la longitude
     * @param location Localisation initiale (adresse ou nom de lieu)
     */
    public void openMapPicker(TextField latitudeField, TextField longitudeField, String location) {
        if (latitudeField == null || longitudeField == null) {
            System.err.println("Erreur: Les champs de latitude et longitude ne peuvent pas être null");
            return;
        }

        this.latitudeField = latitudeField;
        this.longitudeField = longitudeField;
        this.location = (location != null) ? location : "Tunisie"; // Valeur par défaut si null

        // Démarrer un serveur pour recevoir les coordonnées
        startCoordinateServer();

        // Créer et ouvrir le fichier HTML dans le navigateur
        try {
            File htmlFile = createMapHtmlFile();
            Desktop.getDesktop().browse(htmlFile.toURI());

            // Afficher une alerte pour informer l'utilisateur
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Sélection de coordonnées");
                alert.setHeaderText("Carte ouverte dans votre navigateur");
                alert.setContentText("Une carte a été ouverte dans votre navigateur. Cliquez sur la carte pour sélectionner l'emplacement de l'événement, puis cliquez sur 'Confirmer la sélection'.");
                alert.show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Impossible d'ouvrir la carte");
                alert.setContentText("Erreur: " + e.getMessage() + "\nVérifiez votre connexion Internet.");
                alert.show();
            });
        }
    }

    private void startCoordinateServer() {
        // Arrêter le serveur s'il est déjà en cours d'exécution
        stopServer();

        // Trouver un port disponible
        try {
            serverSocket = new ServerSocket(0); // 0 pour trouver un port disponible
            port = serverSocket.getLocalPort();
            System.out.println("Serveur démarré sur le port " + port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        serverRunning = true;

        // Démarrer le serveur dans un thread séparé
        new Thread(() -> {
            try {
                while (serverRunning) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
                // Si le serveur a été arrêté délibérément, cette exception est normale
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream(), "UTF-8")) {
            String line = scanner.nextLine();
            System.out.println("Requête reçue: " + line);

            if (line.startsWith("GET /coords/")) {
                String coords = line.substring(line.indexOf("/coords/") + 8, line.lastIndexOf(" HTTP"));
                String[] parts = coords.split(",");
                if (parts.length == 2) {
                    final String lat = parts[0];
                    final String lng = parts[1];

                    System.out.println("Coordonnées extraites: " + lat + ", " + lng);

                    // Vérifier que les champs ne sont pas null avant de continuer
                    if (latitudeField == null || longitudeField == null) {
                        System.err.println("ERREUR CRITIQUE: Un ou plusieurs champs sont null!");
                        // Tentative de récupération - créer un fichier texte avec les coordonnées
                        try {
                            File coordsFile = new File("lastCoordinates.txt");
                            try (FileOutputStream out = new FileOutputStream(coordsFile)) {
                                String content = lat + "," + lng;
                                out.write(content.getBytes(StandardCharsets.UTF_8));
                            }
                            System.out.println("Coordonnées sauvegardées dans un fichier: " + coordsFile.getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    Platform.runLater(() -> {
                        try {
                            System.out.println("Mise à jour des champs...");
                            latitudeField.setText(lat);
                            longitudeField.setText(lng);

                            // Vérifier les valeurs après la mise à jour
                            System.out.println("Après mise à jour - latitude: " + latitudeField.getText());
                            System.out.println("Après mise à jour - longitude: " + longitudeField.getText());

                            // Informer l'utilisateur
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Coordonnées sélectionnées");
                            alert.setHeaderText("Coordonnées mises à jour");
                            alert.setContentText("Latitude: " + lat + "\nLongitude: " + lng);
                            alert.show();
                        } catch (Exception e) {
                            System.err.println("Exception lors de la mise à jour des champs: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }

                // Envoyer une réponse HTTP pour fermer proprement la connexion
                String response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n" +
                        "OK";
                clientSocket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void stopServer() {
        serverRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createMapHtmlFile() throws Exception {
        // Contenu HTML avec OpenStreetMap et Leaflet
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Sélecteur de coordonnées</title>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />\n" +
                "    <style>\n" +
                "        body, html { height: 100%; margin: 0; padding: 0; font-family: Arial, sans-serif; }\n" +
                "        #map { height: 80%; width: 100%; }\n" +
                "        #controls { padding: 10px; background-color: #f8f9fa; }\n" +
                "        #coordinates { font-weight: bold; margin-bottom: 10px; }\n" +
                "        button { padding: 8px 16px; background-color: #4CAF50; color: white; border: none; cursor: pointer; }\n" +
                "        button:hover { background-color: #45a049; }\n" +
                "        button:disabled { background-color: #cccccc; cursor: not-allowed; }\n" +
                "        #error { color: red; display: none; margin-top: 10px; }\n" +
                "        .search-container { margin-bottom: 10px; }\n" +
                "        #search-input { padding: 8px; width: 250px; }\n" +
                "        #search-button { padding: 8px 12px; margin-left: 5px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"controls\">\n" +
                "        <div class=\"search-container\">\n" +
                "            <input type=\"text\" id=\"search-input\" placeholder=\"Rechercher un lieu...\" value=\"" + location + "\">\n" +
                "            <button id=\"search-button\">Rechercher</button>\n" +
                "        </div>\n" +
                "        <div id=\"coordinates\">Cliquez sur la carte pour sélectionner un emplacement</div>\n" +
                "        <button id=\"confirmBtn\" disabled>Confirmer la sélection</button>\n" +
                "        <div id=\"error\"></div>\n" +
                "    </div>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n" +
                "    <script>\n" +
                "        let map;\n" +
                "        let marker;\n" +
                "        let selectedLat = null;\n" +
                "        let selectedLng = null;\n" +
                "        const coordsDisplay = document.getElementById('coordinates');\n" +
                "        const confirmBtn = document.getElementById('confirmBtn');\n" +
                "        const errorDiv = document.getElementById('error');\n" +
                "        const searchInput = document.getElementById('search-input');\n" +
                "        const searchButton = document.getElementById('search-button');\n" +
                "\n" +
                "        function showError(message) {\n" +
                "            console.error(message);\n" +
                "            errorDiv.textContent = message;\n" +
                "            errorDiv.style.display = 'block';\n" +
                "        }\n" +
                "\n" +
                "        // Initialiser la carte\n" +
                "        function initMap() {\n" +
                "            try {\n" +
                "                // Coordonnées par défaut (Tunisie)\n" +
                "                const defaultLat = 36.8065;\n" +
                "                const defaultLng = 10.1815;\n" +
                "\n" +
                "                // Créer la carte\n" +
                "                map = L.map('map').setView([defaultLat, defaultLng], 8);\n" +
                "\n" +
                "                // Ajouter la couche de tuiles OpenStreetMap\n" +
                "                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "                    attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n" +
                "                }).addTo(map);\n" +
                "\n" +
                "                // Créer un marqueur\n" +
                "                marker = L.marker([defaultLat, defaultLng], {\n" +
                "                    draggable: true\n" +
                "                }).addTo(map);\n" +
                "\n" +
                "                // Géocoder l'adresse initiale\n" +
                "                geocodeAddress('" + location + "');\n" +
                "\n" +
                "                // Mettre à jour les coordonnées quand le marqueur est déplacé\n" +
                "                marker.on('dragend', function() {\n" +
                "                    const position = marker.getLatLng();\n" +
                "                    updateCoordinates(position.lat, position.lng);\n" +
                "                });\n" +
                "\n" +
                "                // Permettre de cliquer sur la carte pour placer le marqueur\n" +
                "                map.on('click', function(e) {\n" +
                "                    marker.setLatLng(e.latlng);\n" +
                "                    updateCoordinates(e.latlng.lat, e.latlng.lng);\n" +
                "                });\n" +
                "\n" +
                "                // Configurer le bouton de recherche\n" +
                "                searchButton.addEventListener('click', function() {\n" +
                "                    geocodeAddress(searchInput.value);\n" +
                "                });\n" +
                "\n" +
                "                // Permettre d'appuyer sur Entrée dans le champ de recherche\n" +
                "                searchInput.addEventListener('keypress', function(e) {\n" +
                "                    if (e.key === 'Enter') {\n" +
                "                        geocodeAddress(searchInput.value);\n" +
                "                    }\n" +
                "                });\n" +
                "\n" +
                "                // Configurer le bouton de confirmation avec la nouvelle logique\n" +
                "                confirmBtn.addEventListener('click', function() {\n" +
                "                    if (selectedLat !== null && selectedLng !== null) {\n" +
                "                        // Désactiver le bouton pendant l'envoi\n" +
                "                        confirmBtn.disabled = true;\n" +
                "                        confirmBtn.textContent = 'Envoi en cours...';\n" +
                "                        \n" +
                "                        // Envoyer les coordonnées et attendre la confirmation avant de fermer\n" +
                "                        sendCoordinatesToJava(selectedLat, selectedLng)\n" +
                "                            .then(() => {\n" +
                "                                // Attendre 1 seconde pour s'assurer que le serveur a traité la requête\n" +
                "                                console.log('Attente de 1 seconde avant fermeture');\n" +
                "                                return new Promise(resolve => setTimeout(resolve, 1000));\n" +
                "                            })\n" +
                "                            .then(() => {\n" +
                "                                console.log('Fermeture de la fenêtre');\n" +
                "                                window.close();\n" +
                "                            })\n" +
                "                            .catch(() => {\n" +
                "                                // En cas d'erreur, réactiver le bouton\n" +
                "                                confirmBtn.disabled = false;\n" +
                "                                confirmBtn.textContent = 'Confirmer la sélection';\n" +
                "                            });\n" +
                "                    }\n" +
                "                });\n" +
                "            } catch (e) {\n" +
                "                showError('Erreur lors de l\\'initialisation de la carte: ' + e.message);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        function updateCoordinates(lat, lng) {\n" +
                "            selectedLat = lat;\n" +
                "            selectedLng = lng;\n" +
                "            coordsDisplay.textContent = 'Coordonnées: ' + lat.toFixed(6) + ', ' + lng.toFixed(6);\n" +
                "            confirmBtn.disabled = false;\n" +
                "        }\n" +
                "\n" +
                "        function geocodeAddress(address) {\n" +
                "            if (!address) return;\n" +
                "            \n" +
                "            try {\n" +
                "                // Utiliser Nominatim pour le géocodage (service OpenStreetMap)\n" +
                "                fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)\n" +
                "                    .then(response => response.json())\n" +
                "                    .then(data => {\n" +
                "                        if (data && data.length > 0) {\n" +
                "                            const result = data[0];\n" +
                "                            const lat = parseFloat(result.lat);\n" +
                "                            const lon = parseFloat(result.lon);\n" +
                "                            \n" +
                "                            map.setView([lat, lon], 12);\n" +
                "                            marker.setLatLng([lat, lon]);\n" +
                "                            updateCoordinates(lat, lon);\n" +
                "                        } else {\n" +
                "                            showError('Aucun résultat trouvé pour cette adresse.');\n" +
                "                        }\n" +
                "                    })\n" +
                "                    .catch(error => {\n" +
                "                        showError('Erreur lors de la recherche de l\\'adresse: ' + error.message);\n" +
                "                    });\n" +
                "            } catch (e) {\n" +
                "                showError('Erreur lors du géocodage: ' + e.message);\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        function sendCoordinatesToJava(lat, lng) {\n" +
                "            try {\n" +
                "                console.log('Envoi des coordonnées: ' + lat + ', ' + lng);\n" +
                "                // Envoyer les coordonnées au serveur local\n" +
                "                return fetch('http://localhost:" + port + "/coords/' + lat + ',' + lng)\n" +
                "                    .then(response => {\n" +
                "                        console.log('Réponse reçue du serveur');\n" +
                "                        return response;\n" +
                "                    })\n" +
                "                    .catch(error => {\n" +
                "                        console.warn('Erreur d\\'envoi: ' + error);\n" +
                "                        throw error;\n" +
                "                    });\n" +
                "            } catch (e) {\n" +
                "                showError('Erreur lors de l\\'envoi des coordonnées: ' + e.message);\n" +
                "                throw e;\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        // Initialiser la carte au chargement de la page\n" +
                "        window.onload = initMap;\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";

        // Créer un fichier temporaire
        File tempFile = File.createTempFile("openstreetmap_", ".html");
        tempFile.deleteOnExit();

        // Écrire le contenu HTML dans le fichier
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            out.write(htmlContent.getBytes(StandardCharsets.UTF_8));
        }

        return tempFile;
    }
}