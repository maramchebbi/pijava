package Main;

import Models.Oeuvre;
import Models.CeramicCollection;
import Services.OeuvreService;

import java.sql.SQLException;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        OeuvreService oeuvreService = new OeuvreService();

        try {
            // Création d'une collection (juste pour associer via l'ID)
            CeramicCollection collection1 = new CeramicCollection();
            collection1.setId(1); // ID existant dans ta base

            // Ajouter une nouvelle oeuvre
            Oeuvre nouvelleOeuvre = new Oeuvre(
                    "Tableau Abstrait",
                    "Peinture",
                    "Une peinture abstraite moderne",
                    "Acrylique",
                    "Rouge",
                    "100x100",
                    "image.jpg",
                    "Art Moderne",
                    1, // user_id
                    collection1
            );
            oeuvreService.add(nouvelleOeuvre);

            // Supprimer une oeuvre (supposons ID = 4)
            CeramicCollection collection2 = new CeramicCollection();
            collection2.setId(2); // ID de la collection liée à l'œuvre supprimée

            Oeuvre oeuvreASupprimer = new Oeuvre(
                    4,
                    "Nom",
                    "Type",
                    "Description",
                    "Matiere",
                    "Couleur",
                    "Dimensions",
                    "Image",
                    "Categorie",
                    1
            );
            oeuvreASupprimer.setCollection(collection2);

            oeuvreService.delete(oeuvreASupprimer);

            // Récupérer toutes les œuvres et les afficher
            List<Oeuvre> oeuvres = oeuvreService.getAll();
            for (Oeuvre oeuvre : oeuvres) {
                System.out.println(oeuvre.toString());
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
