package Main;

import Models.textile;
import Models.collection_t; // Don't forget to import the model
import Services.TextileService;
import Services.CollectionTService;

import java.sql.SQLException;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        TextileService textileService = new TextileService();
        CollectionTService collectionTService = new CollectionTService();

        //for textile
        try {
            Integer collectionId = 2;
            String nom = "Textile Example";
            String type = "Fabric";
            String description = "A beautiful textile example.";
            String matiere = "Cotton";
            String couleur = "Red";
            String dimension = "30x30 cm";
            String createur = "Artist Name";
            String image = "image_path.jpg";
            String technique = "Weaving";
            int userId = 1;


            textile nouvelleTextile = new textile(collectionId, nom, type, description,
                    matiere, couleur, dimension, createur,
                    image, technique, userId);

            textileService.add(nouvelleTextile);

            textile textileASupprimer = new textile();
            textileASupprimer.setId(4);
            textileService.delete(textileASupprimer);

            List<textile> textiles = textileService.getAll();
            for (textile t : textiles) {
                System.out.println(t.toString());
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }

        //for collection textile
        try {
            String nom = "Collection Example";
            int userId = 1;
            String description = "A beautiful collection example.";

            collection_t nouvelleCollection = new collection_t(nom, userId, description);

            collectionTService.add(nouvelleCollection);

            collection_t collectionASupprimer = new collection_t();
            collectionASupprimer.setId(4);
            collectionTService.delete(collectionASupprimer);

            List<collection_t> collections = collectionTService.getAll();
            for (collection_t c : collections) {
                System.out.println(c.toString());
            }

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }





    }
}
