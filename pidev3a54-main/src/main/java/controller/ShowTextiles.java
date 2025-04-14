package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import Services.TextileService;
import Models.textile;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.List;

public class ShowTextiles {

    @FXML
    private FlowPane imageContainer;


    private TextileService textileService;

    public ShowTextiles() {
        textileService = new TextileService();
    }

    public void populateImages() {
        try {
            List<textile> textiles = textileService.getAll();

            imageContainer.getChildren().clear();

            for (textile t : textiles) {
                String imagePath = t.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    ImageView imageView = new ImageView(new Image("file:" + imagePath));
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);

                    imageView.setOnMouseClicked(event -> handleDetails(t));

                    imageContainer.getChildren().add(imageView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        populateImages();
    }

    private void handleDetails(textile t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details.fxml"));
            Parent root = loader.load();

            detail detailController = loader.getController();
            detailController.setTextileDetails(t);

            Stage stage = new Stage();
            stage.setTitle("Textile Details");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnCloseRequest(event -> populateImages());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        populateImages();
    }

    @FXML
    private void handleAjouterTextile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ajouter.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Collection(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleFilter(ActionEvent actionEvent) {
    }
}