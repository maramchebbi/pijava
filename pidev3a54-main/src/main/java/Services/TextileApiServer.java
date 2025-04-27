package Services;

import Models.textile;
import Services.TextileService;

import static spark.Spark.get;
import static spark.Spark.port;

public class TextileApiServer {
    private static TextileService textileService = new TextileService(); // Instance

    public static void start() {
        port(4567); // Port du serveur
        get("/textile/:id", (req, res) -> {
            String id = req.params(":id");
            textile t = textileService.getById(Integer.parseInt(id)); // Appel sur l'instance

            // Renvoyer une page HTML simple
            return "<h1>" + t.getNom() + "</h1>"
//                    + "<p>ID: " + t.getId() + "</p>"
                    + "<p>Type: " + t.getType() + "</p>";
        });
    }
}
