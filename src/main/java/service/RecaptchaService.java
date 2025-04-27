package service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RecaptchaService {

    // Utilisez votre propre clé secrète reCAPTCHA
    private static final String RECAPTCHA_SECRET_KEY = "6Lc05x0rAAAAANq8iyMSwFWlm1gcwk0HnHgcYKLz";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public CompletableFuture<Boolean> verifyRecaptchaToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(VERIFY_URL);

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("secret", RECAPTCHA_SECRET_KEY));
                params.add(new BasicNameValuePair("response", token));

                post.setEntity(new UrlEncodedFormEntity(params));

                try (CloseableHttpResponse response = httpClient.execute(post)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // Vérifier à la fois le succès et le score (si vous utilisez v3)
                    return jsonResponse.getBoolean("success") &&
                            (!jsonResponse.has("score") || jsonResponse.getDouble("score") > 0.5);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}