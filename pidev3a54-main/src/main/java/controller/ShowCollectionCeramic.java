package controller;

import Models.collection_ceramic;
import Services.CollectionCService;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ShowCollectionCeramic {
    private CollectionCService collectionCService;

    @FXML
    private TableColumn<collection_ceramic, String> nomColumn;

    @FXML
    private TableColumn<collection_ceramic, String> descriptionColumn;

    @FXML
    private TableColumn<collection_ceramic, String> actionColumn;

    @FXML
    private TableView<collection_ceramic> tableView;


    @FXML
    void handleAjouterCollectionC(ActionEvent event) {

            try {
                // Load the FXML file for adding a ceramic collection
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCollectionCeramic.fxml")); // ✅ Make sure the FXML path is correct
                Parent root = loader.load();

                // Set the new scene
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();  // You can replace this with an alert if needed
            }
        }

    @FXML
    public void initialize() {
        // Initialize the CollectionCService to interact with the database
        collectionCService = new CollectionCService();

        // Set up the columns of the TableView using PropertyValueFactory
        nomColumn.setCellValueFactory(new PropertyValueFactory<collection_ceramic, String>("nom_c"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<collection_ceramic, String>("description_c"));

        // Populate the table with collections from the database
        populateCollections();
    }

    private void populateCollections() {
        try {
            ObservableList<collection_ceramic> collections = FXCollections.observableArrayList(collectionCService.getAll());

            // Set up the action column to add buttons
            actionColumn.setCellFactory(column -> {
                return new TableCell<collection_ceramic, String>() {
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

                            // Adding both buttons to the cell
                            HBox buttonsBox = new HBox(10, editButton, deleteButton);
                            setGraphic(buttonsBox);
                        }
                    }
                };
            });

            // Set the items in the TableView
            tableView.setItems(collections);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(collection_ceramic item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer cette collection ?");
        alert.setContentText("Cela supprimera aussi toutes les œuvres associées à cette collection.");

        // Show the alert and wait for a response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                collectionCService.delete(item);  // Call the delete method in the ceramic service
                populateCollections();  // Refresh the table after deletion
            } catch (SQLException e) {
                e.printStackTrace();  // Log or show an alert in case of an error

                // Show error alert to the user
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de suppression");
                errorAlert.setHeaderText("Une erreur est survenue lors de la suppression.");
                errorAlert.setContentText("Veuillez réessayer plus tard.");
                errorAlert.showAndWait();
            }
        }
    }

    private void handleEdit(collection_ceramic item) {
        try {
            // Load the FXML file for the ceramic edit screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editCeramic.fxml"));
            Parent root = loader.load();

            // Get the controller for the ceramic edit view
            EditCollectionCeramic editController = loader.getController();

            // Set the collection details in the ceramic edit controller
            editController.setCollectionDetails(item);

            // Create a new scene for the edit window
            Scene editScene = new Scene(root);

            // Create a new stage (new window) for the ceramic edit view
            Stage editStage = new Stage();
            editStage.setTitle("Modifier Collection Céramique");
            editStage.setScene(editScene);

            // Show the new window
            editStage.show();

            // Refresh the table when the edit window is closed
            editStage.setOnCloseRequest(event -> populateCollections());

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Une erreur est survenue lors du chargement de l'interface de modification.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String erreurDeChargement, String s) {
    }

    @FXML
    void Oeuvres(ActionEvent event) {

    }


    }


