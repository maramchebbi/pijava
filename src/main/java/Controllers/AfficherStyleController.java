package Controllers;

import Models.Style;
import Services.StyleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;

public class AfficherStyleController {

    @FXML private GridPane affichageGrid;
    @FXML private Button ajouterStyleButton; // Bouton pour ajouter un style
    private StyleService styleService;
    private Style styleToEdit; // Déclarez une variable pour le style à éditer
    @FXML private TextField typeTextField;
    @FXML private TextField decriptionTextField;
    @FXML private TextField tableauexTextField;
    @FXML private ImageView imageView;



    public AfficherStyleController() {
        styleService = new StyleService();
    }

    public void setStyleToEdit(Style style) {
        this.styleToEdit = style;
        // Remplir les champs avec les données du style existant
        typeTextField.setText(style.getType());
        decriptionTextField.setText(style.getDescription());
        tableauexTextField.setText(style.getExtab());

        // Si l'image existe, l'afficher dans l'ImageView
        if (style.getExtab() != null && !style.getExtab().isEmpty()) {
            Image image = new Image("file:" + style.getExtab());
            imageView.setImage(image);
        }
    }


    @FXML
    public void initialize() {
        loadStyles(); // Charger les styles au démarrage
    }

    public void loadStyles() {
        try {
            List<Style> styles = styleService.getAll();  // Récupérer tous les styles de la base de données
            affichageGrid.getChildren().clear();          // Effacer les anciens éléments affichés
            int column = 0;
            int row = 0;

            for (Style style : styles) {
                VBox card = createStyleCard(style);       // Créer une carte pour chaque style
                affichageGrid.add(card, column, row);
                GridPane.setMargin(card, new Insets(10)); // Ajouter des marges autour des cartes

                column++;
                if (column == 3) {  // Passer à la ligne suivante après 3 colonnes
                    column = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createStyleCard(Style style) {
        // Créer une carte de style avec taille fixe
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 10;");
        card.setPrefWidth(280);  // Largeur fixe de la carte
        card.setPrefHeight(350); // Hauteur fixe de la carte

        // Titre du style
        Text titleText = new Text(style.getType());
        titleText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Description du style
        Text descriptionText = new Text(style.getDescription() != null ? style.getDescription() : "Aucune description disponible.");
        descriptionText.setStyle("-fx-font-size: 12px; -fx-fill: #7f8c8d;");
        descriptionText.setWrappingWidth(250);  // Définir la largeur pour le retour à la ligne
        descriptionText.setStyle("-fx-wrap-text: true;"); // Autoriser le retour à la ligne

        // Image du style
        ImageView imageView = new ImageView();
        if (style.getExtab() != null && !style.getExtab().isEmpty()) {
            Image image = new Image("file:" + style.getExtab());
            imageView.setImage(image);
            imageView.setFitWidth(250);  // Ajuster la taille de l'image
            imageView.setFitHeight(200); // Ajuster la hauteur de l'image
            imageView.setPreserveRatio(true);
        } else {
            // Si aucune image, afficher un espace réservé
            imageView.setImage(new Image("path/to/placeholder/image.png"));
            imageView.setFitWidth(250); // Ajuster la taille de l'image
            imageView.setFitHeight(200); // Ajuster la hauteur de l'image
            imageView.setPreserveRatio(true);
        }

        // Boutons pour modifier et supprimer un style
        Button deleteButton = new Button("Supprimer");
        Button updateButton = new Button("Modifier");

        // Style des boutons
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;");
        updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;");

        // Effet au survol des boutons
        deleteButton.setOnMouseEntered(event -> deleteButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;"));
        deleteButton.setOnMouseExited(event -> deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;"));

        updateButton.setOnMouseEntered(event -> updateButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;"));
        updateButton.setOnMouseExited(event -> updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10px;"));

        deleteButton.setOnAction(event -> {
            try {
                styleService.delete(style.getId());
                loadStyles(); // Recharger les styles après suppression
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        updateButton.setOnAction(event -> {
            try {
                // Ouvrir la fenêtre de modification du style
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterStyle.fxml"));
                Parent root = loader.load();
                AjouterStyleController controller = loader.getController();
                controller.setParentController(this);
                controller.setStyleToEdit(style); // Pré-remplir les champs avec le style sélectionné

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier Style");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(imageView, titleText, descriptionText, updateButton, deleteButton);
        return card;
    }



    @FXML
    private void handleAjouterStyleButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterStyle.fxml"));
            Parent ajouterRoot = loader.load();

            AjouterStyleController ajouterController = loader.getController();
            ajouterController.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un style");
            stage.setScene(new Scene(ajouterRoot));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
