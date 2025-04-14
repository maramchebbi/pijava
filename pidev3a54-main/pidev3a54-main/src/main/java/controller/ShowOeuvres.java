package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import Services.OeuvreService;
import Models.Oeuvre;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.List;

public class ShowOeuvres {

    @FXML
    private FlowPane imageContainer;

    private OeuvreService oeuvreService;

    public ShowOeuvres() {
        oeuvreService = new OeuvreService();
    }

    // Method to populate images in the FlowPane dynamically from the database
    public void populateImages() {
        try {

            List<Oeuvre> ouvres = oeuvreService.getAll();

            // Clear existing images in case of a refresh
            imageContainer.getChildren().clear();


            for (Oeuvre t : ouvres) {
                String imagePath = t.getImage(); // This should contain the image path or URL from the database
                if (imagePath != null && !imagePath.isEmpty()) {
                    ImageView imageView = new ImageView(new Image("file:" + imagePath)); // Assuming the images are local
                    imageView.setFitHeight(100);  // Set a fixed size for the image
                    imageView.setFitWidth(100);   // Set a fixed size for the image

                    // Add an event handler to open the details page when the image is clicked
                    imageView.setOnMouseClicked(event -> handleDetails(t));

                    imageContainer.getChildren().add(imageView);  // Add the ImageView to the FlowPane
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Initialize method to call populateImages when the view is loaded
    @FXML
    public void initialize() {
        populateImages();  // Call the method to populate the images when the controller is initialized
    }


    private void handleDetails(Oeuvre t) {
        try {
            // Load the Details view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();


            detail detailController = loader.getController();
            detailController.setOeuvreDetails(t);

            // Create a new stage for the details view
            Stage stage = new Stage();
            stage.setTitle("oeuvre Details");
            stage.setScene(new Scene(root));
            stage.show();

            // Add a listener to refresh the images when the detail window is closed
            stage.setOnCloseRequest(event -> populateImages()); // Refresh the images when the detail window is closed

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleAjouterOeuvre(ActionEvent event) {
        try {
            // Load the "ajouter.fxml" file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Add.fxml"));
            Parent root = loader.load();

            // Get the current scene and set the new scene with the form
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();  // Log the error or show an alert
        }
    }
    @FXML
    private void affichercollectionaction(ActionEvent event) {
        try {
            // Charger le fichier FXML pour l'interface d'ajout de collection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Collections.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle et définir la nouvelle scène avec le formulaire d'ajout
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();  // Loguer l'erreur ou afficher une alerte
        }
    }

}
