package Services;

import Models.RecommendedEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service pour récupérer les recommandations depuis l'API Python
 */
public class RecommendationService {

    private static final Logger LOGGER = Logger.getLogger(RecommendationService.class.getName());
    private static final String API_URL = "http://127.0.0.1:5003/recommend";

    /**
     * Récupère les recommandations pour un utilisateur de manière asynchrone
     * @param userId ID de l'utilisateur
     * @return CompletableFuture contenant la liste des événements recommandés
     */
    public CompletableFuture<List<RecommendedEvent>> getRecommendationsAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getRecommendations(userId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des recommandations", e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Récupère les recommandations pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des événements recommandés
     * @throws Exception Si une erreur se produit lors de la communication avec l'API
     */
    public List<RecommendedEvent> getRecommendations(int userId) throws Exception {
        List<RecommendedEvent> recommendations = new ArrayList<>();

        URL url = new URL(API_URL + "?user_id=" + userId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("id");
                String titre = jsonObject.getString("titre");
                double similarityScore = jsonObject.getDouble("similarity_score");

                recommendations.add(new RecommendedEvent(id, titre, similarityScore));
            }
        } else {
            throw new Exception("Erreur API: " + responseCode);
        }

        return recommendations;
    }
}