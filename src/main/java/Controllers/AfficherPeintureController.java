package Controllers;

import Models.Peinture;
import Services.PeintureService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AfficherPeintureController {

    @FXML
    private Button ajouterButton;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private GridPane affichageGrid;

    @FXML
    private TextField searchField;


    private PeintureService peintureService;

    @FXML
    public void initialize() {
        peintureService = new PeintureService();
        loadPeintures();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                filtrerPeintures(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void filtrerPeintures(String query) throws SQLException {
        List<Peinture> toutesPeintures = peintureService.getAll();
        List<Peinture> filtrees = toutesPeintures.stream()
                .filter(p -> p.getTitre().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        loadPeinture(filtrees); // Appelle la version avec paramètre
    }

    public void loadPeinture(List<Peinture> peintures) {
        affichageGrid.getChildren().clear();
        int column = 0;
        int row = 0;

        for (Peinture peinture : peintures) {
            VBox card = createPeintureCard(peinture);
            affichageGrid.add(card, column, row);
            GridPane.setMargin(card, new Insets(10));

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }



    public void loadPeintures() {
        try {
            List<Peinture> peintures = peintureService.getAll();
            affichageGrid.getChildren().clear();
            int column = 0;
            int row = 0;

            for (Peinture peinture : peintures) {
                VBox card = createPeintureCard(peinture);
                affichageGrid.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPeintureCard(Peinture peinture) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setMinWidth(220);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-padding: 12; -fx-border-color: #ddd; -fx-border-radius: 12px;");

        // Image
        ImageView imageView = new ImageView();
        if (peinture.getTableau() != null && !peinture.getTableau().isEmpty()) {
            Image image = new Image("file:" + peinture.getTableau());
            imageView.setImage(image);
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // Titre
        Label titleLabel = new Label(peinture.getTitre());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Date
        Label dateLabel = new Label("Créée le : " + (peinture.getDateCr() != null ? peinture.getDateCr().toString() : "N/A"));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Style
        Label styleLabel = new Label("Style : " + (peinture.getStyle() != null ? peinture.getStyle().getType() : "Inconnu"));
        styleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Rating stars
        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER);
        List<Label> starLabels = new ArrayList<>();
        int[] currentRating = {0}; // permet de modifier le rating dynamiquement

        for (int i = 1; i <= 5; i++) {
            Label star = new Label("☆");
            star.setStyle("-fx-font-size: 18px; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            final int ratingValue = i;
            star.setOnMouseClicked(e -> {
                currentRating[0] = ratingValue;
                for (int j = 0; j < 5; j++) {
                    starLabels.get(j).setText(j < ratingValue ? "★" : "☆");
                }

                // TODO: Sauvegarder la note dans la base de données si besoin
                System.out.println("Rated " + peinture.getTitre() + " : " + ratingValue + " étoiles");
            });
            starLabels.add(star);
            ratingBox.getChildren().add(star);
        }

        // Buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button updateButton = new Button("Modifier");
        updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
        updateButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPeinture.fxml"));
                Parent root = loader.load();
                AjouterPeintureController controller = loader.getController();
                controller.setParentController(this);
                controller.setPeintureToEdit(peinture);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Modifier Peinture");
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 8px; -fx-cursor: hand;");
        deleteButton.setOnAction(event -> {
            try {
                peintureService.delete(peinture.getId());
                loadPeintures();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(updateButton, deleteButton);

        // Assemble card
        card.getChildren().addAll(imageView, titleLabel, dateLabel, styleLabel, ratingBox, buttonsBox);
        return card;
    }


    @FXML
    private void handleAjouterButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPeinture.fxml"));
            Parent ajouterRoot = loader.load();

            AjouterPeintureController ajouterController = loader.getController();
            ajouterController.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une peinture");
            stage.setScene(new Scene(ajouterRoot));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
