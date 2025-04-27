package Services;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BadWordFilterService {
    private static final String PURGOMALUM_API_URL = "https://www.purgomalum.com/service/json?text=%s";

    public String filterBadWords(String text) {
        try {
            // Encoder le texte pour l'URL
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");
            String apiUrl = String.format(PURGOMALUM_API_URL, encodedText);

            // Faire la requête HTTP
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(apiUrl);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());

                // Parser la réponse JSON
                ObjectMapper mapper = new ObjectMapper();
                PurgoMalumResponse apiResponse = mapper.readValue(jsonResponse, PurgoMalumResponse.class);

                return apiResponse.getResult();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du filtrage des mots: " + e.getMessage());
            return text; // Retourne le texte original en cas d'erreur
        }
    }

    // Classe interne pour le parsing JSON
    private static class PurgoMalumResponse {
        private String result;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }
}