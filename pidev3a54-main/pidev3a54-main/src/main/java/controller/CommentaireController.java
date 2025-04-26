package controller;

import Models.Commentaire;
import Models.Oeuvre;
import Services.CommentaireService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.Region;

public class CommentaireController {

    private final CommentaireService commentaireService = new CommentaireService();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Méthode pour afficher un dialogue de commentaires pour une œuvre spécifique
    public void showCommentsDialog(Oeuvre oeuvre) {
        try {
            // Création du dialogue
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Commentaires - " + oeuvre.getNom());
            dialog.setHeaderText("Commentaires et discussions");

            // Création du conteneur principal
            VBox contentBox = new VBox(10);
            contentBox.setPadding(new Insets(20));
            contentBox.setPrefWidth(450);
            contentBox.setPrefHeight(500);

            // Zone de commentaires existants
            ScrollPane commentsScroll = new ScrollPane();
            commentsScroll.setFitToWidth(true);
            commentsScroll.setPrefHeight(350);

            VBox commentsContainer = new VBox(10);
            commentsContainer.setPadding(new Insets(10));
            commentsScroll.setContent(commentsContainer);

            // Chargement des commentaires existants
            loadComments(oeuvre, commentsContainer);

            // Zone d'ajout de commentaire
            TextField commentField = new TextField();
            commentField.setPromptText("Ajouter un commentaire...");
            commentField.setPrefHeight(40);

            Button addButton = new Button("Publier");
            addButton.setStyle("-fx-background-color: #e60023; -fx-text-fill: white;");

            HBox inputBox = new HBox(10);
            inputBox.setAlignment(Pos.CENTER_LEFT);
            inputBox.getChildren().addAll(commentField, addButton);
            HBox.setHgrow(commentField, javafx.scene.layout.Priority.ALWAYS);

            // Action d'ajout de commentaire
            addButton.setOnAction(e -> {
                String content = commentField.getText().trim();
                if (!content.isEmpty()) {
                    addComment(oeuvre, content, commentsContainer);
                    commentField.clear();
                }
            });

            // Assembler tous les éléments
            contentBox.getChildren().addAll(
                    createHeaderText("Commentaires"),
                    commentsScroll,
                    createHeaderText("Ajouter un commentaire"),
                    inputBox
            );

            // Configuration du dialog
            dialog.getDialogPane().setContent(contentBox);
            ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            // Afficher le dialogue
            dialog.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les commentaires: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour charger les commentaires existants
    // Méthode pour charger les commentaires existants
    private void loadComments(Oeuvre oeuvre, VBox container) {
        try {
            // Vider le conteneur
            container.getChildren().clear();

            // Récupérer les commentaires depuis la base de données
            List<Commentaire> commentaires = commentaireService.getByOeuvreId(oeuvre.getId());

            if (commentaires.isEmpty()) {
                Text noComments = new Text("Aucun commentaire pour le moment");
                noComments.setStyle("-fx-font-style: italic; -fx-fill: #888888;");
                container.getChildren().add(noComments);
                return;
            }

            // Ajouter chaque commentaire au conteneur
            for (Commentaire commentaire : commentaires) {
                try {
                    // Tentative de récupération du nom d'utilisateur
                    ResultSet rs = commentaireService.getUserNameById(commentaire.getUserId());
                    String userName = "Utilisateur #" + commentaire.getUserId();
                    if (rs != null && rs.next()) {
                        userName = rs.getString("nom");
                    }
                    container.getChildren().add(createCommentNode(commentaire, userName, container, oeuvre));
                } catch (Exception e) {
                    // En cas d'erreur, utiliser l'ID utilisateur
                    container.getChildren().add(createCommentNode(commentaire, "Utilisateur #" + commentaire.getUserId(), container, oeuvre));
                }
            }

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des commentaires: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Méthode pour ajouter un nouveau commentaire
    private void addComment(Oeuvre oeuvre, String content, VBox container) {
        try {
            // Créer un nouveau commentaire
            Commentaire commentaire = new Commentaire(content, 1, oeuvre); // User ID est fixé à 1 pour l'exemple

            // Vérifier si le commentaire existe déjà
            if (commentaireService.commentaireExists(content, commentaire.getUserId(), oeuvre.getId())) {
                showAlert("Attention", "Ce commentaire existe déjà");
                return;
            }

            // Ajouter à la base de données
            commentaireService.add(commentaire);

            // Recharger les commentaires pour actualiser l'affichage
            loadComments(oeuvre, container);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'ajout du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Créer un nœud d'affichage pour un commentaire
    // Créer un nœud d'affichage pour un commentaire
    private VBox createCommentNode(Commentaire commentaire, String userName, VBox container, Oeuvre oeuvre) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: #f8f8f8; -fx-background-radius: 8; -fx-padding: 12px;");

        // En-tête du commentaire (utilisateur + date + menu)
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Text username = new Text(userName);
        username.setStyle("-fx-font-weight: bold;");

        Text date = new Text(commentaire.getDate().format(formatter));
        date.setStyle("-fx-font-size: 11px; -fx-fill: #888888;");

        // Bouton avec trois points pour le menu
        Button menuButton = new Button("⋮");
        menuButton.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");
        menuButton.setAlignment(Pos.CENTER_RIGHT);

        // Créer le menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifier");
        MenuItem deleteItem = new MenuItem("Supprimer");

        // Ajouter les actions pour modifier et supprimer
        editItem.setOnAction(e -> showEditDialog(commentaire, container, oeuvre));
        deleteItem.setOnAction(e -> deleteComment(commentaire, container, oeuvre));

        contextMenu.getItems().addAll(editItem, deleteItem);

        // Associer le menu au bouton
        menuButton.setOnAction(e -> contextMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0));

        // Créer un spacer pour pousser le menu à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        headerBox.getChildren().addAll(username, date, spacer, menuButton);

        // Contenu du commentaire
        Text content = new Text(commentaire.getContenu());
        content.setWrappingWidth(380);

        // Assembler le tout
        commentBox.getChildren().addAll(headerBox, content);

        return commentBox;
    }
    // Méthode pour créer un texte d'en-tête
    private Text createHeaderText(String headerText) {
        Text header = new Text(headerText);
        header.setFont(Font.font("System", FontWeight.BOLD, 14));
        return header;
    }

    // Afficher une alerte
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Méthode pour afficher une boîte de dialogue d'édition de commentaire
    private void showEditDialog(Commentaire commentaire, VBox container, Oeuvre oeuvre) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modifier le commentaire");
        dialog.setHeaderText("Modifier votre commentaire");

        // Créer le champ de texte
        TextArea commentField = new TextArea(commentaire.getContenu());
        commentField.setWrapText(true);
        commentField.setPrefHeight(100);
        commentField.setPrefWidth(400);

        dialog.getDialogPane().setContent(commentField);

        // Ajouter les boutons
        ButtonType saveButton = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

        // Traiter le résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                return commentField.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty()) {
                try {
                    // CORRECTION: S'assurer que le commentaire a bien une référence à l'œuvre
                    if (commentaire.getOeuvre() == null) {
                        commentaire.setOeuvre(oeuvre);
                    }

                    // Mettre à jour le commentaire
                    commentaire.setContenu(newContent.trim());
                    commentaireService.update(commentaire);

                    // Recharger les commentaires
                    loadComments(oeuvre, container);

                    showAlert("Succès", "Commentaire modifié avec succès");
                } catch (SQLException ex) {
                    showAlert("Erreur", "Erreur lors de la modification du commentaire: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
    // Méthode pour supprimer un commentaire
    private void deleteComment(Commentaire commentaire, VBox container, Oeuvre oeuvre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce commentaire ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Supprimer le commentaire
                commentaireService.delete(commentaire);

                // Recharger les commentaires
                loadComments(oeuvre, container);

                showAlert("Succès", "Commentaire supprimé avec succès");
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur lors de la suppression du commentaire: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}