package controller;

import Models.collection_t;
import Services.CollectionTService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ShowCollectionT {

    private CollectionTService collectionTService;

    @FXML
    private TableColumn<collection_t, String> actionColumn;

    @FXML
    private TableColumn<collection_t, String> descriptionColumn;

    @FXML
    private TableColumn<collection_t, String> nomColumn;

    @FXML
    private TableView<collection_t> tableView;



    @FXML
    public void handleAjouterCollection(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ajouter1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        collectionTService = new CollectionTService();

        nomColumn.setCellValueFactory(new PropertyValueFactory<collection_t, String>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<collection_t, String>("description"));

        populateCollections();
    }

    private void populateCollections() {
        try {
            ObservableList<collection_t> collections = FXCollections.observableArrayList(collectionTService.getAll());

            actionColumn.setCellFactory(column -> {
                return new TableCell<collection_t, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Button editButton = new Button("Modifier");
                            editButton.setOnAction(event -> handleEdit(getTableRow().getItem())); // Get the collection and pass to the edit handler

                            Button deleteButton = new Button("Supprimer");
                            deleteButton.setOnAction(event -> handleDelete(getTableRow().getItem()));

                            HBox buttonsBox = new HBox(10, editButton, deleteButton);
                            setGraphic(buttonsBox);
                        }
                    }
                };
            });

            tableView.setItems(collections);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleEdit(collection_t collection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit1.fxml"));
            Parent root = loader.load();

            edit1 editController = loader.getController();

            editController.setCollectionDetails(collection);

            Scene editScene = new Scene(root);

            Stage editStage = new Stage();
            editStage.setTitle("Edit Collection");
            editStage.setScene(editScene);

            editStage.show();

            editStage.setOnCloseRequest(event -> populateCollections());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loading Error", "An error occurred while trying to load the edit screen.");
            e.printStackTrace();
        }
    }



    public void handleDelete(collection_t collection) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer cette collection ?");
        alert.setContentText("Cela supprimera aussi tous les textiles associés à cette collection.");


        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                collectionTService.delete(collection);
                populateCollections();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de suppression");
                errorAlert.setHeaderText("Une erreur est survenue lors de la suppression.");
                errorAlert.setContentText("Veuillez réessayer plus tard.");
                errorAlert.showAndWait();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

    public void Textiles(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}