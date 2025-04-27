package utils;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class CallbackServer {
    private HttpServer server;
    private CompletableFuture<String> authCodeFuture = new CompletableFuture<>();
    private int port;

    public CallbackServer() throws IOException {
        // Essayer plusieurs ports, en commençant par 8000
        IOException lastException = null;
        for (int tryPort = 8000; tryPort < 9000; tryPort++) {
            try {
                server = HttpServer.create(new InetSocketAddress(tryPort), 0);
                this.port = tryPort;
                // Si on arrive ici, le port a fonctionné
                break;
            } catch (IOException e) {
                lastException = e;
                // Continuer avec le port suivant
            }
        }

        // Si aucun port n'a fonctionné
        if (server == null) {
            throw new IOException("Impossible de trouver un port disponible", lastException);
        }

        // Mise à jour du REDIRECT_URI avec le port réel utilisé
        final String redirectBase = "http://localhost:" + this.port + "/callback";

        server.createContext("/callback", exchange -> {
            // Le reste du code reste identique...
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            if (query != null && query.contains("code=")) {
                code = query.substring(query.indexOf("code=") + 5);
                if (code.contains("&")) {
                    code = code.substring(0, code.indexOf("&"));
                }
            }

            String response = "<html><body style='font-family:Arial,sans-serif;'>" +
                    "<div style='text-align:center;margin-top:50px;'>" +
                    "<h2>Authentification réussie!</h2>" +
                    "<p>Vous pouvez maintenant fermer cette fenêtre.</p>" +
                    "</div></body></html>";

            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }

            if (code != null) {
                authCodeFuture.complete(code);
            } else {
                authCodeFuture.completeExceptionally(
                        new RuntimeException("Code d'authentification non trouvé"));
            }

            server.stop(1);
        });
    }

    public void start() {
        server.start();
    }

    public CompletableFuture<String> getAuthCodeFuture() {
        return authCodeFuture;
    }

    public int getPort() {
        return port;
    }
}