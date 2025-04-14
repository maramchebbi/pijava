package controller;

import Services.CollectionCeramiqueService;
import Models.CeramicCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class AjouterCollectionController {

    @FXML
    private Button ajouterButton;

    @FXML
    private Button annulerButton;


    @FXML
    private TextArea descriptionCollectionField;

    @FXML
    private TextField nomCollectionField;

    @FXML
    private TextField useridfield;

    private CollectionCeramiqueService collectionCeramiqueService;


    // Constructeur pour initialiser le service
    public AjouterCollectionController() {
        collectionCeramiqueService = new CollectionCeramiqueService();
    }

//    @FXML
//    void ajoutercollectionaction(ActionEvent event) {
//        // Récupérer les valeurs des champs FXML
//        String nom_c = nomCollectionField.getText();
//        String description_c = descriptionCollectionField.getText();
//
//        int userId;
//
//
//        // Vérification et conversion de l'ID utilisateur
//        try {
//            userId = Integer.parseInt(useridfield.getText());
//        } catch (NumberFormatException e) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur");
//            alert.setHeaderText("Format incorrect !");
//            alert.setContentText("Le champ ID utilisateur doit être numérique.");
//            alert.show();
//            return;
//        }
//
//        // Vérification si les champs sont vides
//        if (nom_c.isEmpty() || description_c.isEmpty()) {
//            Alert alert = new Alert(Alert.AlertType.ERROR);
//            alert.setTitle("Erreur");
//            alert.setHeaderText(null);
//            alert.setContentText("Veuillez remplir tous les champs !");
//            alert.showAndWait();
//        } else {
//            // Créer une nouvelle collection avec les données saisies
//            CeramicCollection newCollection = new CeramicCollection();
//            newCollection.setNom_c(nom_c);
//            newCollection.setDescription_c(description_c);
//            newCollection.setUser_id(userId);
//
//
//
//
//            // Tentative d'ajout avec gestion des exceptions
//            try {
//                collectionCeramiqueService.add(newCollection);
//
//                // Afficher un message de succès
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Succès");
//                alert.setHeaderText("Collection ajoutée avec succès !");
//                alert.show();
//
//                // Réinitialiser les champs après l'ajout
//                nomCollectionField.clear();
//                descriptionCollectionField.clear();
//                useridfield.clear();
//
//
//            } catch (SQLException e) {
//                // Afficher un message d'erreur en cas d'exception
//                Alert alert = new Alert(Alert.AlertType.ERROR);
//                alert.setTitle("Erreur lors de l'ajout");
//                alert.setHeaderText("Impossible d'ajouter la collection");
//                alert.setContentText(e.getMessage());
//                alert.show();
//            }
//        }
//    }


    @FXML
    void ajoutercollectionaction(ActionEvent event) {
        String nom_c = nomCollectionField.getText();
        String description_c = descriptionCollectionField.getText();

        int userId;

        try {
            userId = Integer.parseInt(useridfield.getText());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Le champ ID utilisateur doit être numérique.").show();
            return;
        }

        if (nom_c.isEmpty() || description_c.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs !").showAndWait();
        } else {
            try {
                // Vérifier si le nom existe déjà
                if (collectionCeramiqueService.isNomCollectionExists(nom_c)) {
                    new Alert(Alert.AlertType.WARNING, "Ce nom de collection existe déjà ! Veuillez en choisir un autre.").show();
                    return;
                }

                // Créer la collection
                CeramicCollection newCollection = new CeramicCollection();
                newCollection.setNom_c(nom_c);
                newCollection.setDescription_c(description_c);
                newCollection.setUser_id(userId);

                collectionCeramiqueService.add(newCollection);

                new Alert(Alert.AlertType.INFORMATION, "Collection ajoutée avec succès !").show();

                nomCollectionField.clear();
                descriptionCollectionField.clear();
                useridfield.clear();

            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout : " + e.getMessage()).show();
            }
        }
    }


    @FXML
    void annuleraction(ActionEvent event) {
        // Réinitialiser les champs si l'utilisateur annule
        nomCollectionField.clear();
        descriptionCollectionField.clear();

    }


    @FXML
    private void retouraction(ActionEvent event) {
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
