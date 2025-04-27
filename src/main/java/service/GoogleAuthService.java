package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import models.User;
import utils.CallbackServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class GoogleAuthService {

    // Remplacez ces valeurs par celles obtenues de Google Cloud Console
    private static final String CLIENT_ID = "659352778662-p3i674ucgu25tv0ab0v2l1ovqt0jrtt5.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8000/callback";

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserService userService;

    public GoogleAuthService() {
        this.userService = new UserService();
    }

    public CompletableFuture<User> startGoogleAuth() {
        CompletableFuture<User> future = new CompletableFuture<>();

        try {
            // Démarrer le serveur de callback
            CallbackServer callbackServer = new CallbackServer();
            callbackServer.start();

            // Créer la WebView pour afficher la page de connexion Google
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();

            // Créer une fenêtre pour la WebView
            Stage authStage = new Stage();
            authStage.setTitle("Connexion avec Google");
            authStage.setScene(new Scene(webView, 650, 700));

            // Construire l'URL d'authentification
            String authUrl = buildAuthURL();

            // Charger la page d'authentification
            webEngine.load(authUrl);
            authStage.show();

            // Attendre le code d'autorisation du serveur de callback
            callbackServer.getAuthCodeFuture()
                    .thenCompose(authCode -> {
                        // Fermer la fenêtre d'authentification
                        Platform.runLater(() -> authStage.close());

                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                // Échanger le code contre un token d'accès
                                String tokenJson = exchangeCodeForToken(authCode);
                                JsonNode tokenData = mapper.readTree(tokenJson);
                                String accessToken = tokenData.get("access_token").asText();

                                // Récupérer les informations de l'utilisateur
                                String userInfoJson = getUserInfo(accessToken);
                                User googleUser = parseUserInfo(userInfoJson);

                                // Marquer l'utilisateur comme venant de Google
                                googleUser.setGoogleAccount(true);

                                // Vérifier si l'utilisateur existe déjà
                                User existingUser = null;
                                try {
                                    existingUser = userService.findByEmail(googleUser.getEmail());
                                } catch (SQLException e) {
                                    throw new RuntimeException("Erreur de base de données: " + e.getMessage());
                                }

                                final User finalUser;
                                if (existingUser != null) {
                                    // Utilisateur existant
                                    finalUser = existingUser;
                                } else {
                                    // Nouvel utilisateur
                                    googleUser.setVerified(true); // Déjà vérifié par Google
                                    googleUser.setRole("membre"); // Rôle par défaut
                                    googleUser.setPassword(""); // Pas de mot de passe local

                                    try {
                                        userService.add(googleUser);
                                        finalUser = googleUser;
                                    } catch (SQLException e) {
                                        throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
                                    }
                                }

                                return finalUser;
                            } catch (Exception e) {
                                throw new RuntimeException("Erreur pendant l'authentification: " + e.getMessage(), e);
                            }
                        });
                    })
                    .thenAccept(user -> Platform.runLater(() -> future.complete(user)))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> future.completeExceptionally(ex));
                        return null;
                    });

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }
    private String buildAuthURL() {
        try {
            return AUTH_URL + "?" +
                    "client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") + "&" +
                    "redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") + "&" +
                    "response_type=code&" +
                    "scope=" + URLEncoder.encode("email profile", "UTF-8") + "&" +
                    "access_type=offline";
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la construction de l'URL d'authentification", e);
        }
    }

    private String extractAuthCode(String url) {
        int start = url.indexOf("code=");
        if (start == -1) return null;

        start += 5; // Longueur de "code="
        int end = url.indexOf('&', start);
        if (end == -1) end = url.length();

        return url.substring(start, end);
    }

    private String exchangeCodeForToken(String code) throws IOException {
        URL url = new URL(TOKEN_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);

        String postData = "code=" + URLEncoder.encode(code, "UTF-8") +
                "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8") +
                "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, "UTF-8") +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                "&grant_type=authorization_code";

        // Impression pour débogage
        System.out.println("Requête de token: " + postData);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        String responseMessage = conn.getResponseMessage();

        // Impression pour débogage
        System.out.println("Code de réponse: " + responseCode + " - " + responseMessage);

        if (responseCode != 200) {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
            }
            System.out.println("Erreur complète: " + errorResponse);
            throw new IOException("Erreur HTTP: " + responseCode + " - " + errorResponse);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
    private String getUserInfo(String accessToken) throws IOException {
        URL url = new URL(USERINFO_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Erreur HTTP lors de la récupération des infos utilisateur: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private User parseUserInfo(String json) throws IOException {
        JsonNode userInfo = mapper.readTree(json);

        User user = new User();
        user.setEmail(userInfo.has("email") ? userInfo.get("email").asText() : "");
        user.setNom(userInfo.has("family_name") ? userInfo.get("family_name").asText() : "");
        user.setPrenom(userInfo.has("given_name") ? userInfo.get("given_name").asText() : "");
        user.setGenre("Non spécifié"); // Par défaut car Google ne fournit pas le genre

        return user;
    }
}