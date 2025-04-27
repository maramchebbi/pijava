package controller;

import Models.textile;
import Models.Vote;
import Services.TextileService;
import Services.VoteService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class detail implements Initializable {

    @FXML private Label nameLabel;
    @FXML private ImageView imageView;
    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField matiereField;
    @FXML private TextField couleurField;
    @FXML private TextField dimensionField;
    @FXML private TextField createurField;
    @FXML private TextField techniqueField;
    @FXML private TextArea descriptionField;
    @FXML private HBox ratingContainer;

    private textile currentTextile;
    private TextileService textileService = new TextileService();
    private VoteService voteService = new VoteService();

    // Simuler l'ID de l'utilisateur actuel (à remplacer par votre système de connexion)
    private int currentUserId = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the rating service
        if (voteService == null) {
            voteService = new VoteService();
        }

        // Initialize any components here if needed
        if (ratingContainer == null) {
            // Create the rating container if it doesn't exist in FXML
            ratingContainer = new HBox(5);
            ratingContainer.setAlignment(Pos.CENTER);
        }
    }

    public void setTextileDetails(textile t) {
        this.currentTextile = t;

        if (t.getImage() != null && !t.getImage().isEmpty()) {
            try {
                imageView.setImage(new Image("file:" + t.getImage()));
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
                showAlert(AlertType.WARNING, "No Image", "Could not load image for this textile.");
            }
        } else {
            showAlert(AlertType.WARNING, "No Image", "No image associated with this textile.");
        }

        // Set the name in the larger title label
        if (nameLabel != null) {
            nameLabel.setText(t.getNom());
        }

        // Set information in the form fields
        if (nomField != null) nomField.setText(t.getNom());
        if (typeField != null) typeField.setText(t.getType());
        if (descriptionField != null) descriptionField.setText(t.getDescription());
        if (matiereField != null) matiereField.setText(t.getMatiere());
        if (couleurField != null) couleurField.setText(t.getCouleur());
        if (dimensionField != null) dimensionField.setText(t.getDimension());
        if (createurField != null) createurField.setText(t.getCreateur());
        if (techniqueField != null) techniqueField.setText(t.getTechnique());

        // Display the rating
        displayRating();
    }

    private void displayRating() {
        if (currentTextile != null && ratingContainer != null) {
            try {
                // Clear existing content
                ratingContainer.getChildren().clear();

                // Get average rating and vote count
                double avgRating = voteService.getAverageRating(currentTextile.getId());
                int voteCount = voteService.getVoteCount(currentTextile.getId());

                // Round average to nearest integer
                int roundedRating = (int) Math.round(avgRating);

                // Create star display
                for (int i = 1; i <= 5; i++) {
                    SVGPath starPath = new SVGPath();
                    starPath.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
                    starPath.setScaleX(1.5);
                    starPath.setScaleY(1.5);

                    if (i <= roundedRating) {
                        starPath.setFill(Color.web("#FFC107")); // Gold for filled stars
                    } else {
                        starPath.setFill(Color.web("#D3D3D3")); // Gray for empty stars
                    }

                    ratingContainer.getChildren().add(starPath);
                }

                // Add vote count
                Label voteCountLabel = new Label("  (" + voteCount + ")");
                voteCountLabel.setStyle("-fx-text-fill: #603813; -fx-font-size: 14px;");
                ratingContainer.getChildren().add(voteCountLabel);

            } catch (SQLException e) {
                System.err.println("Error loading ratings: " + e.getMessage());

                // Add placeholder if error occurs
                Label errorLabel = new Label("Évaluation non disponible");
                errorLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
                ratingContainer.getChildren().add(errorLabel);
            }
        }
    }

    @FXML
    public void modifiertextile(ActionEvent actionEvent) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "Invalid Textile", "No textile data to modify.");
            return;
        }

        try {
            // Try different resource loading approaches for edit.fxml
            URL resourceUrl = null;

            // Try options for finding the file
            resourceUrl = getClass().getResource("/edit.fxml");
            if (resourceUrl == null) resourceUrl = getClass().getResource("edit.fxml");
            if (resourceUrl == null) resourceUrl = getClass().getClassLoader().getResource("edit.fxml");

            if (resourceUrl == null) {
                showAlert(AlertType.ERROR, "Resource Not Found", "Could not find edit.fxml file");
                return;
            }
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            edit editController = loader.getController();
            editController.setTextileDetails(currentTextile);

            Scene editScene = new Scene(root,screenWidth,screenHeight);
            Stage currentStage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            currentStage.setScene(editScene);
            currentStage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Loading Error", "An error occurred while trying to load the edit screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimerTextile(ActionEvent actionEvent) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "No Textile Selected", "No textile selected for deletion.");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce textile ?");
        alert.setContentText("Cette action est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    textileService.delete(currentTextile);
                    showAlert(AlertType.INFORMATION, "Suppression réussie", "Le textile a été supprimé avec succès.");

                    // Return to the textiles list
                    navigateToTextilesList(actionEvent);
                } catch (SQLException e) {
                    showAlert(AlertType.ERROR, "Erreur de suppression", "Impossible de supprimer le textile: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateToTextilesList(event);
    }

    private void navigateToTextilesList(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();

            double screenWidth = screen.getVisualBounds().getWidth(); // Screen width
            double screenHeight = screen.getVisualBounds().getHeight();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root,screenWidth,screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRate(ActionEvent event) {
        if (currentTextile == null) {
            showAlert(AlertType.ERROR, "No Textile Selected", "No textile to rate.");
            return;
        }

        try {
            // Create a custom dialog
            Dialog<Integer> ratingDialog = new Dialog<>();
            ratingDialog.setTitle("Évaluer le textile");

            // Make the dialog more attractive
            DialogPane dialogPane = ratingDialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/css_files/style.css").toExternalForm());
            dialogPane.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-radius: 10; -fx-background-radius: 10;");

            // Create a better layout for the dialog content
            VBox mainContent = new VBox(20);
            mainContent.setAlignment(Pos.CENTER);
            mainContent.setStyle("-fx-padding: 10;");

            // Add textile image and name for context
            if (currentTextile.getImage() != null && !currentTextile.getImage().isEmpty()) {
                try {
                    ImageView dialogImageView = new ImageView(new Image("file:" + currentTextile.getImage()));
                    dialogImageView.setFitHeight(150);
                    dialogImageView.setFitWidth(150);
                    dialogImageView.setPreserveRatio(true);

                    StackPane imageContainer = new StackPane();
                    imageContainer.getChildren().add(dialogImageView);
                    imageContainer.setStyle("-fx-background-color: white; -fx-padding: 5; " +
                            "-fx-border-color: #e0e0e0; -fx-border-radius: 5; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                    mainContent.getChildren().add(imageContainer);
                } catch (Exception e) {
                    System.err.println("Error loading image in dialog: " + e.getMessage());
                }
            }

            // Add textile name
            Label nameLabel = new Label(currentTextile.getNom());
            nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #603813;");
            mainContent.getChildren().add(nameLabel);

            // Add rating prompt
            Label promptLabel = new Label("Comment évaluez-vous ce textile?");
            promptLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666666;");
            mainContent.getChildren().add(promptLabel);

            // Attempt to get the user's existing vote
            int currentRating = 0;
            try {
                Vote userVote = voteService.getUserVote(currentTextile.getId(), currentUserId);
                if (userVote != null) {
                    currentRating = userVote.getValue();
                }
            } catch (SQLException e) {
                System.err.println("Error getting user vote: " + e.getMessage());
            }

            // Create star rating buttons
            HBox starsContainer = new HBox(15);
            starsContainer.setAlignment(Pos.CENTER);
            starsContainer.setPadding(new Insets(10, 0, 10, 0));

            ToggleGroup ratingGroup = new ToggleGroup();

            for (int i = 1; i <= 5; i++) {
                final int rating = i;

                ToggleButton starButton = new ToggleButton();
                starButton.setToggleGroup(ratingGroup);
                starButton.setUserData(rating);
                starButton.setPrefSize(45, 45);
                starButton.setMinSize(45, 45);
                starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                        "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");

                // Create star icon
                SVGPath starPath = new SVGPath();
                starPath.setContent("M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z");
                starPath.setFill(Color.web("#E0E0E0")); // Default gray

                // Create label for star value
                Label valueLabel = new Label(String.valueOf(i));
                valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #666;");

                // Stack the star and value
                StackPane starStack = new StackPane();
                starStack.getChildren().addAll(starPath, valueLabel);

                starButton.setGraphic(starStack);

                // Add hover effect
                starButton.setOnMouseEntered(e -> {
                    if (!starButton.isSelected()) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFF8E1; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#FFECB3")); // Light gold on hover
                    }
                });

                starButton.setOnMouseExited(e -> {
                    if (!starButton.isSelected()) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#E0E0E0")); // Back to gray
                    }
                });

                // Set initial state based on current rating
                if (i <= currentRating) {
                    starButton.setSelected(true);
                    starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFC107; " +
                            "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                    starPath.setFill(Color.WHITE);
                    valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
                }

                // Add listener for state changes
                starButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: #FFC107; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.WHITE);
                        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: white;");
                    } else {
                        starButton.setStyle("-fx-background-radius: 30; -fx-background-color: white; " +
                                "-fx-border-color: #FFC107; -fx-border-radius: 30; -fx-border-width: 2;");
                        starPath.setFill(Color.web("#E0E0E0"));
                        valueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #666;");
                    }
                });

                starsContainer.getChildren().add(starButton);
            }

            // Add explanation labels for the rating scale
            HBox explanationBox = new HBox();
            explanationBox.setAlignment(Pos.CENTER);
            explanationBox.setSpacing(10);

            Label minLabel = new Label("Pas satisfait");
            minLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label maxLabel = new Label("Très satisfait");
            maxLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

            explanationBox.getChildren().addAll(minLabel, spacer, maxLabel);

            mainContent.getChildren().addAll(starsContainer, explanationBox);

            // Add custom buttons
            ButtonType submitButtonType = new ButtonType("Soumettre", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

            dialogPane.getButtonTypes().addAll(submitButtonType, cancelButtonType);

            // Style the buttons
            Button submitButton = (Button) dialogPane.lookupButton(submitButtonType);
            submitButton.setStyle("-fx-background-color: #603813; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 5;");

            Button cancelButton = (Button) dialogPane.lookupButton(cancelButtonType);
            cancelButton.setStyle("-fx-background-color: #F5F5F5; -fx-text-fill: #666; " +
                    "-fx-background-radius: 5;");

            dialogPane.setContent(mainContent);

            // Convert the result
            ratingDialog.setResultConverter(dialogButton -> {
                if (dialogButton == submitButtonType) {
                    ToggleButton selected = (ToggleButton) ratingGroup.getSelectedToggle();
                    if (selected != null) {
                        return (Integer) selected.getUserData();
                    }
                }
                return null;
            });

            // Show the dialog
            ratingDialog.showAndWait().ifPresent(rating -> {
                if (rating != null && rating > 0) {
                    try {
                        voteService.addOrUpdateVote(currentTextile.getId(), currentUserId, rating);
                        showAlert(AlertType.INFORMATION, "Évaluation enregistrée",
                                "Votre évaluation a été enregistrée avec succès.");
                        displayRating(); // Update the rating display
                    } catch (SQLException e) {
                        showAlert(AlertType.ERROR, "Erreur d'évaluation",
                                "Une erreur est survenue lors de l'enregistrement de votre évaluation: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}