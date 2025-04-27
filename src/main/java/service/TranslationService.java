package service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TranslationService {

    // API clé pour LibreTranslate (gratuit, open-source)
    private static final String API_URL = "https://translate.googleapis.com/translate_a/single";
    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * Traduit un texte d'une langue à une autre en utilisant l'API de traduction.
     *
     * @param text Texte à traduire
     * @param sourceLang Code de la langue source
     * @param targetLang Code de la langue cible
     * @return Texte traduit
     * @throws Exception Si une erreur se produit lors de la traduction
     */
    public String translate(String text, String sourceLang, String targetLang) throws Exception {
        // Si les langues sont identiques, retourner le texte original
        if (sourceLang.equals(targetLang)) {
            return text;
        }

        // Version utilisant l'API Google Translate sans clé API (méthode non officielle)
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlStr = API_URL + "?client=gtx&sl=" + sourceLang + "&tl=" + targetLang +
                "&dt=t&q=" + encodedText;

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configuration de la requête HTTP
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        // Lire la réponse
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Erreur API: Code " + responseCode);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            // Extraire le texte traduit de la réponse
            // Le format est complexe: [[["translated text","original text",null,null,1]],null,"en"]
            // Nous prenons juste le premier élément qui contient la traduction
            String responseStr = response.toString();

            // Méthode simple d'extraction (pourrait être améliorée avec une bibliothèque JSON complète)
            int startIdx = responseStr.indexOf("\"") + 1;
            int endIdx = responseStr.indexOf("\"", startIdx);

            if (startIdx > 0 && endIdx > startIdx) {
                return responseStr.substring(startIdx, endIdx);
            } else {
                // Alternative: utiliser une bibliothèque JSON comme org.json si disponible
                try {
                    org.json.JSONArray jsonArray = new org.json.JSONArray(responseStr);
                    org.json.JSONArray translationsArray = jsonArray.getJSONArray(0);
                    StringBuilder translatedText = new StringBuilder();

                    for (int i = 0; i < translationsArray.length(); i++) {
                        JSONArray translationInfo = translationsArray.getJSONArray(i);
                        translatedText.append(translationInfo.getString(0));
                    }

                    return translatedText.toString();
                } catch (Exception e) {
                    throw new Exception("Erreur lors du parsing de la réponse: " + e.getMessage());
                }
            }
        }
    }

    // Alternative: si l'API Google ne fonctionne pas, on peut utiliser d'autres API gratuites comme LibreTranslate
    public String translateWithLibreTranslate(String text, String sourceLang, String targetLang) throws Exception {
        // URL d'une instance LibreTranslate publique (à remplacer par votre propre instance si nécessaire)
        String apiUrl = "https://libretranslate.de/translate";
        URL url = new URL(apiUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Préparer les données pour la requête
        String jsonInputString = String.format(
                "{\"q\": \"%s\", \"source\": \"%s\", \"target\": \"%s\", \"format\": \"text\"}",
                text.replace("\"", "\\\""), sourceLang, targetLang
        );

        // Envoyer la requête
        try (java.io.OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Lire la réponse
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            // Extraire la traduction du JSON
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getString("translatedText");
        }
    }
    private final Map<String, CachedTranslation> translationCache = new HashMap<>();

    // Durée de validité du cache en minutes
    private static final long CACHE_VALIDITY_MINUTES = 60;

    // Classe pour stocker une traduction mise en cache avec un timestamp
    private static class CachedTranslation {
        private final String translatedText;
        private final long timestamp;

        public CachedTranslation(String translatedText) {
            this.translatedText = translatedText;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTranslatedText() {
            return translatedText;
        }

        public boolean isValid() {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - timestamp;
            return TimeUnit.MILLISECONDS.toMinutes(elapsedTime) < CACHE_VALIDITY_MINUTES;
        }
    }


}