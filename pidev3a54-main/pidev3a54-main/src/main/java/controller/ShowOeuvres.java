package controller;

import Models.Oeuvre;
import Services.OeuvreService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ShowOeuvres {

    @FXML
    private FlowPane imageContainer;

    private final OeuvreService oeuvreService = new OeuvreService();

    @FXML
    public void initialize() {
        populateImages();
    }

    public void populateImages() {
        try {
            List<Oeuvre> ouvres = oeuvreService.getAll();
            imageContainer.getChildren().clear();

            for (Oeuvre oeuvre : ouvres) {
                // Création du conteneur pour chaque œuvre
                VBox oeuvreBox = new VBox(10);
                oeuvreBox.setAlignment(Pos.TOP_CENTER);
                oeuvreBox.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");
                oeuvreBox.setPadding(new Insets(15));
                oeuvreBox.setPrefWidth(200);

                // Nom de l'œuvre
                Text nameText = new Text(oeuvre.getNom());
                nameText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                nameText.setWrappingWidth(180);

                // Image de l'œuvre
                ImageView imageView = new ImageView();
                String imagePath = oeuvre.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        Image image = new Image("file:" + imagePath);
                        imageView.setImage(image);
                        imageView.setFitWidth(180);
                        imageView.setFitHeight(180);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                    } catch (Exception e) {
                        System.err.println("Erreur de chargement de l'image: " + imagePath);
                        Text errorText = new Text("Image non disponible");
                        errorText.setStyle("-fx-fill: #999999;");
                        oeuvreBox.getChildren().add(errorText);
                    }
                }

                // Boutons d'action
                HBox buttonBox = new HBox(10);
                buttonBox.setAlignment(Pos.CENTER);

                Button detailsBtn = new Button("Détails");
                detailsBtn.setStyle("-fx-background-color: #8a6d5b; -fx-text-fill: white;");
                detailsBtn.setOnAction(e -> handleDetails(oeuvre));

                Button editBtn = new Button("Modifier");
                editBtn.setStyle("-fx-background-color: #5e4b3c; -fx-text-fill: white;");
                editBtn.setOnAction(e -> handleEdit(oeuvre));

                Button deleteBtn = new Button("Supprimer");
                deleteBtn.setStyle("-fx-background-color: #a52a2a; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> handleDelete(oeuvre));

                buttonBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);
                oeuvreBox.getChildren().addAll(nameText, imageView, buttonBox);
                imageContainer.getChildren().add(oeuvreBox);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des œuvres: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDetails(Oeuvre oeuvre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();

            detail detailController = loader.getController();
            detailController.setOeuvreDetails(oeuvre);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'œuvre");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails");
            e.printStackTrace();
        }
    }

    private void handleEdit(Oeuvre oeuvre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit.fxml"));
            Parent root = loader.load();

            edit editController = loader.getController();
            editController.setOeuvreDetails(oeuvre);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'œuvre");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur");
            e.printStackTrace();
        }
    }

    private void handleDelete(Oeuvre oeuvre) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'œuvre '" + oeuvre.getNom() + "' ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Modification ici pour utiliser la méthode qui accepte un Oeuvre
                oeuvreService.delete(oeuvre); // Passer l'objet Oeuvre complet
                showAlert("Succès", "Œuvre supprimée avec succès");
                populateImages(); // Rafraîchir l'affichage
            } catch (SQLException e) {
                showAlert("Erreur", "Échec de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleAjouterOeuvre(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Add.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'interface d'ajout");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleWorkshopRedirect(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddWorkshop.fxml")); // adapte le chemin
            Parent workshopRoot = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter Workshop");
            stage.setScene(new Scene(workshopRoot));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void affichercollectionaction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Collections.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les collections");
            e.printStackTrace();
        }
    }
}