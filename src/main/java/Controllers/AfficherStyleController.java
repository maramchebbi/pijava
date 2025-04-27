package Controllers;

import Models.Style;
import Services.StyleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.TextField;

import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AfficherStyleController {

    @FXML private Button ajouterStyleButton;
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane affichageGrid;

    private StyleService styleService;

    @FXML
    private TextField searchField;


    @FXML
    public void initialize() {
        styleService = new StyleService();
        loadStyles();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStyles(newValue);
        });
    }

    private void filterStyles(String query) {
        try {
            List<Style> styles = styleService.getAll(); // récupère tous les styles
            affichageGrid.getChildren().clear();
            int column = 0;
            int row = 0;

            for (Style style : styles) {
                if (query == null || query.isEmpty() ||
                        style.getType().toLowerCase().contains(query.toLowerCase())) {

                    VBox card = createStyleCard(style);
                    affichageGrid.add(card, column, row);
                    GridPane.setMargin(card, new Insets(10));

                    column++;
                    if (column == 3) {
                        column = 0;
                        row++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadStyles() {
        try {
            List<Style> styles = styleService.getAll();
            affichageGrid.getChildren().clear();
            int column = 0;
            int row = 0;

            for (Style style : styles) {
                VBox card = createStyleCard(style);
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

    private VBox createStyleCard(Style style) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setMinWidth(220);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 12px; " +
                        "-fx-border-color: #ddd; " +
                        "-fx-border-radius: 12px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0.2, 2, 2);"
        );

        // Image
        ImageView imageView = new ImageView();
        if (style.getExtab() != null && !style.getExtab().isEmpty()) {
            Image image = new Image("file:" + style.getExtab(), false);
            imageView.setImage(image);
        } else {
            imageView.setImage(new Image("path/to/placeholder/image.png"));
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // Clip arrondi
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        imageView.setClip(clip);

        // Animation hover sur image
        imageView.setOnMouseEntered(e -> {
            imageView.setScaleX(1.05);
            imageView.setScaleY(1.05);
        });
        imageView.setOnMouseExited(e -> {
            imageView.setScaleX(1.0);
            imageView.setScaleY(1.0);
        });

        // Type
        Label typeLabel = new Label(style.getType());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Description
        Label descriptionLabel = new Label(style.getDescription() != null ? style.getDescription() : "Aucune description");
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        descriptionLabel.setWrapText(true);

        // Buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button updateButton = new Button("Modifier");
        updateButton.setPrefWidth(80);
        updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 8px;");
        updateButton.setOnAction(event -> openEditWindow(style));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setPrefWidth(80);
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; -fx-background-radius: 8px;");
        deleteButton.setOnAction(event -> {
            try {
                styleService.delete(style.getId());
                loadStyles();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(updateButton, deleteButton);

        card.getChildren().addAll(imageView, typeLabel, descriptionLabel, buttonsBox);
        return card;
    }


    private void openEditWindow(Style style) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterStyle.fxml"));
            Parent root = loader.load();
            AjouterStyleController controller = loader.getController();
            controller.setParentController(this);
            controller.setStyleToEdit(style);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Style");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
