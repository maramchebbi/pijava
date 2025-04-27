package Controllers;

import Models.Peinture;
import Models.Rating;
import Models.Style;
import Services.PeintureService;
import Services.RatingService;
import Services.StyleService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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

    @FXML
    private ComboBox<String> styleComboBox;

    @FXML
    private ComboBox<String> starsComboBox;

    @FXML
    private ComboBox<String> triComboBox;

    private PeintureService peintureService;
    private StyleService styleService;
    private RatingService ratingService;
    private List<Peinture> allPeintures = new ArrayList<>();


    @FXML
    public void initialize() {
        peintureService = new PeintureService();
        styleService = new StyleService();
        ratingService = new RatingService();

        initStyleComboBox();
        // Pas besoin de remplir starsComboBox ici : il sera rempli directement depuis le FXML

        loadPeintures();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        styleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        starsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        triComboBox.setOnAction(event -> filterAndSortPeintures());

    }

    private void filterAndSortPeintures() {
        String selectedTri = triComboBox.getValue();

        List<Peinture> filteredList = new ArrayList<>(allPeintures);

        if (selectedTri != null) {
            if (selectedTri.contains("↑")) {
                filteredList.sort(Comparator.comparing(Peinture::getDateCr));
            } else if (selectedTri.contains("↓")) {
                filteredList.sort(Comparator.comparing(Peinture::getDateCr).reversed());
            } else if (selectedTri.contains("Aucun")) {
                // Pas de tri : garder la liste originale
            }
        }

        loadPeinture(filteredList);
    }




    private void initStyleComboBox() {
        styleComboBox.getItems().add("Tous les styles");
        try {
            for (Style style : styleService.getAll()) {
                styleComboBox.getItems().add(style.getType());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        styleComboBox.setValue("Tous les styles");
    }

    private void applyFilters() {
        try {
            String query = searchField.getText();
            String selectedStyle = styleComboBox.getValue();
            String selectedStars = starsComboBox.getValue();

            List<Peinture> peintures = peintureService.getAll();

            // Filtrage par recherche texte
            if (query != null && !query.isEmpty()) {
                peintures = peintures.stream()
                        .filter(p -> p.getTitre().toLowerCase().contains(query.toLowerCase()))
                        .collect(Collectors.toList());
            }

            // Filtrage par style
            if (selectedStyle != null && !"Tous les styles".equals(selectedStyle)) {
                peintures = peintures.stream()
                        .filter(p -> p.getStyle() != null && selectedStyle.equals(p.getStyle().getType()))
                        .collect(Collectors.toList());
            }

            // Filtrage par étoiles
            if (selectedStars != null && !selectedStars.isEmpty() && !selectedStars.equals("Toutes")) {
                int stars = selectedStars.length(); // Nombre d'étoiles = longueur de la chaîne
                int userId = 5; // ID statique

                List<Peinture> finalPeintures = new ArrayList<>();
                for (Peinture p : peintures) {
                    Integer note = ratingService.getRatingForPeintureAndUser(p.getId(), userId);
                    if (note != null && note == stars) {
                        finalPeintures.add(p);
                    }
                }
                peintures = finalPeintures;
            }

            loadPeinture(peintures);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            allPeintures = peintures;
            loadPeinture(peintures);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createPeintureCard(Peinture peinture) {
        VBox card = new VBox(12);
        card.setPrefWidth(240);
        card.setMaxWidth(240);
        card.setMinWidth(240);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-radius: 20px; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-width: 1px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0.2, 2, 2);");

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f9f9f9; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-radius: 20px; " +
                "-fx-border-color: #9575cd; " +
                "-fx-border-width: 1.5px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0.3, 2, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 20px; " +
                "-fx-border-radius: 20px; " +
                "-fx-border-color: #dddddd; " +
                "-fx-border-width: 1px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0.2, 2, 2);"));

        // Image
        ImageView imageView = new ImageView();
        if (peinture.getTableau() != null && !peinture.getTableau().isEmpty()) {
            Image image = new Image("file:" + peinture.getTableau(), false);
            imageView.setImage(image);
        }
        imageView.setFitWidth(180);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);

        // Clip ImageView
        Rectangle clip = new Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(imageView.fitWidthProperty());
        clip.heightProperty().bind(imageView.fitHeightProperty());
        imageView.setClip(clip);

        // Titre
        Label titleLabel = new Label(peinture.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Date
        Label dateLabel = new Label("Créée le : " + (peinture.getDateCr() != null ? peinture.getDateCr().toString() : "N/A"));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Style
        Label styleLabel = new Label("Style : " + (peinture.getStyle() != null ? peinture.getStyle().getType() : "Inconnu"));
        styleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Rating stars
        HBox ratingBox = createRatingBox(peinture);

        // Buttons
        HBox buttonsBox = createButtonsBox(peinture);

        card.getChildren().addAll(imageView, titleLabel, dateLabel, styleLabel, ratingBox, buttonsBox);
        return card;
    }

    private HBox createRatingBox(Peinture peinture) {
        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER);
        List<Label> starLabels = new ArrayList<>();
        int[] currentRating = {0};
        int userId = 5;

        try {
            Integer existingNote = ratingService.getRatingForPeintureAndUser(peinture.getId(), userId);
            if (existingNote != null) {
                currentRating[0] = existingNote;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 5; i++) {
            Label star = new Label(i <= currentRating[0] ? "★" : "☆");
            star.setStyle("-fx-font-size: 20px; -fx-text-fill: #f1c40f; -fx-cursor: hand;");
            final int ratingValue = i;
            star.setOnMouseClicked(e -> {
                currentRating[0] = ratingValue;
                for (int j = 0; j < 5; j++) {
                    starLabels.get(j).setText(j < ratingValue ? "★" : "☆");
                }
                try {
                    Rating newRating = new Rating(peinture.getId(), userId, ratingValue);
                    ratingService.saveOrUpdateRating(newRating);
                    System.out.println("Note enregistrée pour " + peinture.getTitre() + " : " + ratingValue + " étoiles");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            star.setOnMouseEntered(event -> { star.setScaleX(1.2); star.setScaleY(1.2); });
            star.setOnMouseExited(event -> { star.setScaleX(1.0); star.setScaleY(1.0); });
            starLabels.add(star);
            ratingBox.getChildren().add(star);
        }
        return ratingBox;
    }

    private HBox createButtonsBox(Peinture peinture) {
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);

        Button updateButton = new Button("Modifier");
        updateButton.setPrefWidth(90);
        updateButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8px;");
        updateButton.setOnAction(event -> handleModifierPeinture(peinture));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setPrefWidth(90);
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 8px;");
        deleteButton.setOnAction(event -> {
            try {
                peintureService.delete(peinture.getId());
                loadPeintures();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        buttonsBox.getChildren().addAll(updateButton, deleteButton);
        return buttonsBox;
    }

    private void handleModifierPeinture(Peinture peinture) {
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
