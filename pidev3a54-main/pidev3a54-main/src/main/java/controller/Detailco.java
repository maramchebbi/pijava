package controller;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import Services.OeuvreService;
import Models.Oeuvre;
import java.io.File;
import java.util.List;

public class Detailco {

    @FXML
    private FlowPane imageContainer;

    private final OeuvreService oeuvreService = new OeuvreService();

    public void populateImagesByCollection(int collectionId) {
        try {
            List<Oeuvre> ouvres = oeuvreService.getByCollectionId(collectionId);
            imageContainer.getChildren().clear();

            // Configuration du FlowPane pour un affichage organisé
            imageContainer.setAlignment(Pos.CENTER);
            imageContainer.setHgap(20); // Espacement horizontal entre les éléments
            imageContainer.setVgap(20); // Espacement vertical entre les éléments
            imageContainer.setPadding(new Insets(15)); // Marge intérieure
            imageContainer.setPrefWrapLength(700); // Largeur avant retour à la ligne

            if (ouvres.isEmpty()) {
                Text noItemsText = new Text("Aucune œuvre trouvée pour cette collection.");
                noItemsText.setStyle("-fx-font-size: 16px; -fx-fill: #666;");
                imageContainer.getChildren().add(noItemsText);
                return;
            }

            for (Oeuvre oeuvre : ouvres) {
                // Créer un conteneur pour chaque œuvre
                VBox oeuvreContainer = new VBox(8); // Espacement de 8 pixels entre les éléments
                oeuvreContainer.setAlignment(Pos.CENTER);
                oeuvreContainer.setPadding(new Insets(10));
                oeuvreContainer.setStyle("-fx-background-color: #f8f8f8; -fx-border-radius: 5; -fx-background-radius: 5;");
                oeuvreContainer.setMaxWidth(200); // Largeur fixe pour chaque conteneur

                // Afficher le nom de l'œuvre
                String oeuvreName = oeuvre.getNom();
                if (oeuvreName != null && !oeuvreName.isEmpty()) {
                    Text text = new Text(oeuvreName);
                    text.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #333;");
                    text.setWrappingWidth(180); // Largeur de texte adaptée
                    oeuvreContainer.getChildren().add(text);
                }

                // Ajouter l'image
                String imageUrl = oeuvre.getImage();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    try {
                        // Convertir le chemin relatif en URL valide
                        if (!imageUrl.startsWith("http") && !imageUrl.startsWith("file:")) {
                            imageUrl = new File(imageUrl).toURI().toString();
                        }

                        Image image = new Image(imageUrl, 150, 150, true, true, true);
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(150);
                        imageView.setFitHeight(150);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        imageView.setCache(true);
                        imageView.setOnMouseClicked(event -> handleDetails(oeuvre));

                        oeuvreContainer.getChildren().add(imageView);
                    } catch (Exception e) {
                        System.err.println("Erreur de chargement de l'image: " + imageUrl);
                        Text errorText = new Text("Image non disponible");
                        errorText.setStyle("-fx-font-size: 12px; -fx-fill: #999;");
                        oeuvreContainer.getChildren().add(errorText);
                    }
                }

                imageContainer.getChildren().add(oeuvreContainer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Text errorText = new Text("Erreur de chargement des œuvres");
            errorText.setStyle("-fx-font-size: 16px; -fx-fill: #cc0000;");
            imageContainer.getChildren().add(errorText);
        }
    }

    private void handleDetails(Oeuvre oeuvre) {
        System.out.println("Détails de l'œuvre: " + oeuvre);
        // Ici vous pouvez implémenter l'ouverture d'une nouvelle vue détaillée
    }
}