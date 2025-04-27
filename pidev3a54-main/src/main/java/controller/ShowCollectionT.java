package controller;

import Models.collection_t;
import Models.textile;
import Services.CollectionTService;
import Services.TextileService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ShowCollectionT {

    private CollectionTService collectionTService;
    private TextileService textileService;

    @FXML
    private TableColumn<collection_t, Void> actionColumn;

    @FXML
    private TextField searchField;

    @FXML
    private TableColumn<collection_t, String> descriptionColumn;

    @FXML
    private TableColumn<collection_t, String> nomColumn;

    @FXML
    private TableView<collection_t> tableView;

    @FXML
    public void handleAjouterCollection(ActionEvent actionEvent) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Ajouter1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);
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

        // Configure table columns
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Set up search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchCollections(newValue);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur de recherche", "Une erreur est survenue lors de la recherche", Alert.AlertType.ERROR);
            }
        });

        // Load initial data
        populateCollections();
    }

    private void populateCollections() {
        try {
            ObservableList<collection_t> collections = FXCollections.observableArrayList(collectionTService.getAll());

            // Configuration de la colonne Description
            descriptionColumn.setCellFactory(column -> new TableCell<collection_t, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            });

            // Configuration de la colonne Actions
            actionColumn.setCellFactory(column -> new TableCell<collection_t, Void>() {
                private final Button editButton = new Button("Modifier");
                private final Button deleteButton = new Button("Supprimer");
                private final Button detailsButton = new Button("Détails");
                private final HBox buttonsBox = new HBox(10, editButton, deleteButton, detailsButton);

                {
                    editButton.setStyle("-fx-background-color: #d7c3b6; -fx-text-fill: #603813;");
                    deleteButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
                    detailsButton.setStyle("-fx-background-color: #603813; -fx-text-fill: white;");
                    buttonsBox.setAlignment(Pos.CENTER);

                    editButton.setOnAction(event -> {
                        collection_t collection = getTableView().getItems().get(getIndex());
                        handleEdit(collection, event);
                    });

                    deleteButton.setOnAction(event -> {
                        collection_t collection = getTableView().getItems().get(getIndex());
                        handleDelete(collection);
                    });

                    detailsButton.setOnAction(event -> {
                        collection_t collection = getTableView().getItems().get(getIndex());
                        handleDetailsCollection(collection);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            });

            tableView.setItems(collections);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les collections: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEdit(collection_t collection, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit1.fxml"));
            Parent root = loader.load();

            edit1 editController = loader.getController();
            editController.setCollectionDetails(collection);

            Scene currentScene = ((Node) event.getSource()).getScene();
            currentScene.setRoot(root);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", "Une erreur est survenue lors du chargement de l'interface d'édition.");
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

    @FXML
    private void handleDetailsCollection(collection_t collection) {
        try {

            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/details1.fxml"));
            Parent root = loader.load();


            detail1 controller = loader.getController();
            controller.setCollection(collection);

            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) tableView.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'afficher les détails de la collection: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void Textiles(ActionEvent event) {
        try {
            Screen screen = Screen.getPrimary();
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, screenWidth, screenHeight);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchCollections(String query) throws SQLException {
        List<collection_t> collections = collectionTService.searchCollections(query);
        tableView.getItems().clear();

        for (collection_t c : collections) {
            tableView.getItems().add(c);
        }

        if (query.isEmpty()) {
            populateCollections();
        }
    }
}
