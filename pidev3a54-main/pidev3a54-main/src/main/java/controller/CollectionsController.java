package controller;

import Models.CeramicCollection;
import Services.CollectionCeramiqueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import Models.Oeuvre;
import Services.OeuvreService;
import controller.Detailco;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CollectionsController {

    @FXML
    private TableColumn<CeramicCollection, String> actionColumn;


    @FXML
    private TableColumn<CeramicCollection, String> descriptionColumn;

    @FXML
    private TableColumn<CeramicCollection, String> nomColumn;

    @FXML
    private TableView<CeramicCollection> tableView;

    private ObservableList<CeramicCollection> collectionsList = FXCollections.observableArrayList();
    private CollectionCeramiqueService collectionService;

    public CollectionsController() {
        this.collectionService = new CollectionCeramiqueService();
    }

    @FXML
    public void initialize() {
        // Configuration des colonnes comme vous aviez déjà
        nomColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom_c()));
        descriptionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription_c()));

        // Style pour le TableView et la sélection de ligne
       // tableView.getStylesheets().add(getClass().getResource("/css_files/collections.css").toExternalResource());

        // Configuration de la colonne d'actions
        actionColumn.setCellFactory(param -> new javafx.scene.control.TableCell<CeramicCollection, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button modifyButton = new Button("Modifier");
                    modifyButton.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: #f5f5dc; " +
                            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10;");
                    modifyButton.setOnAction(event -> modifyCollection(getTableView().getItems().get(getIndex())));

                    Button deleteButton = new Button("Supprimer");
                    deleteButton.setStyle("-fx-background-color: #b22222; -fx-text-fill: #f5f5dc; " +
                            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10;");
                    deleteButton.setOnAction(event -> delete(getTableView().getItems().get(getIndex())));

                    Button detailButton = new Button("Détails");
                    detailButton.setStyle("-fx-background-color: #6b4226; -fx-text-fill: #f5f5dc; " +
                            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10;");
                    detailButton.setOnAction(event -> showDetails(getTableView().getItems().get(getIndex())));

                    Button showButton = new Button("Voir");
                    showButton.setStyle("-fx-background-color: #a0522d; -fx-text-fill: #f5f5dc; " +
                            "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10;");
                    showButton.setOnAction(event -> voir(getTableView().getItems().get(getIndex())));

                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(8);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER);
                    hbox.getChildren().addAll(modifyButton, deleteButton, detailButton, showButton);
                    setGraphic(hbox);
                }
            }
        });

        // Définir la scène en plein écran à l'initialisation
        tableView.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Stage stage = (Stage) tableView.getScene().getWindow();
                stage.setMaximized(true);
            }
        });

        loadCollectionsData();
    }
    private void voir(CeramicCollection collection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailco.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur lié au fichier FXML
            Detailco controller = loader.getController();

            // Charger les images de la collection sélectionnée
            controller.populateImagesByCollection(collection.getId());

            // Afficher la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage = new Stage(); // nouvelle fenêtre
            stage.setScene(scene);
            stage.setTitle("Détails de la Collection: " + collection.getNom_c());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Charger les collections depuis la base de données
    private void loadCollectionsData() {
        try {
            List<CeramicCollection> collections = collectionService.getAll();
            collectionsList.setAll(collections); // Remplacer toutes les collections dans la liste observable
            tableView.setItems(collectionsList);  // Afficher les collections dans la TableView
        } catch (SQLException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la récupération des collections");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Modifier une collection
    private void modifyCollection(CeramicCollection collection) {
        // Créer un dialog personnalisé
        Dialog<CeramicCollection> dialog = new Dialog<>();
        dialog.setTitle("Modifier la Collection");
        dialog.setHeaderText("Modifier : " + collection.getNom_c());

        ButtonType updateButtonType = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        // Création des champs
        TextField nomField = new TextField(collection.getNom_c());
        TextField descriptionField = new TextField(collection.getDescription_c());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convertir le résultat en objet CeramicCollection quand on clique sur "Mettre à jour"
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                collection.setNom_c(nomField.getText());
                collection.setDescription_c(descriptionField.getText());
                return collection;
            }
            return null;
        });

        Optional<CeramicCollection> result = dialog.showAndWait();

        result.ifPresent(updatedCollection -> {
            try {
                collectionService.update(updatedCollection);
                tableView.refresh(); // Actualiser la table
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Collection modifiée !");
                alert.setContentText("Les informations ont été mises à jour.");
                alert.showAndWait();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de la mise à jour");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }


    private void delete(CeramicCollection collection) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Voulez-vous vraiment supprimer cette collection ?");
        alert.setContentText("Cela supprimera aussi toutes les œuvres associées à cette collection.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                collectionService.delete(collection);  // Supprimer via le service (œuvres + collection)
                collectionsList.remove(collection);    // Supprimer de la liste observable
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                        "La collection \"" + collection.getNom_c() + "\" et ses œuvres associées ont été supprimées.");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                        "Une erreur est survenue lors de la suppression. Veuillez réessayer plus tard.");
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


    // Afficher les détails d'une collection
    private void showDetails(CeramicCollection collection) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Détails de la collection");
        alert.setHeaderText("Détails de : " + collection.getNom_c());
        alert.setContentText("Nom : " + collection.getNom_c() + "\nDescription : " + collection.getDescription_c());
        alert.showAndWait();
    }

    @FXML
    private void ajoutercoaction(ActionEvent event) {
        try {
            // Charger le fichier FXML pour l'interface d'ajout de collection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterCollection.fxml"));
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
//new method
@FXML
private void oeuvreaction(ActionEvent event) {
    try {
        // Charger le fichier FXML pour l'interface d'ajout de collection
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
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


