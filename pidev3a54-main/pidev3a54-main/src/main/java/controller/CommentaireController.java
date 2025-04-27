package controller;

import Models.Commentaire;
import Models.Oeuvre;
import Services.CommentaireService;
import Services.BadWordFilterService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CommentaireController {

    private final CommentaireService commentaireService = new CommentaireService();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String PRIMARY_COLOR = "#222222";
    private static final String ACCENT_COLOR = "#e60023";
    private static final String BACKGROUND_COLOR = "#fcfcfc";
    private static final String COMMENT_BG_COLOR = "#f5f5f5";

    // Méthode principale pour afficher la fenêtre des commentaires
    public void showCommentsDialog(Oeuvre oeuvre) {
        try {
            // Création d'une nouvelle fenêtre au lieu d'un Dialog
            Stage commentStage = new Stage();
            commentStage.initModality(Modality.APPLICATION_MODAL);
            commentStage.setTitle("Commentaires");
            commentStage.initStyle(StageStyle.UNDECORATED);

            // Configuration du conteneur principal
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + "; -fx-background-radius: 10;");

            // Ajout d'un effet d'ombre
            root.setEffect(new DropShadow(10, Color.web("#00000055")));

            // En-tête personnalisé
            HBox header = createCustomHeader(oeuvre.getNom(), commentStage);
            root.setTop(header);

            // Conteneur principal pour les commentaires
            VBox mainContent = new VBox(20);
            mainContent.setPadding(new Insets(20));

            // Zone de commentaires existants avec ScrollPane
            VBox commentsContainer = new VBox(15);
            commentsContainer.setPadding(new Insets(5));

            ScrollPane commentsScroll = new ScrollPane(commentsContainer);
            commentsScroll.setFitToWidth(true);
            commentsScroll.setPrefHeight(400);
            commentsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            // Chargement des commentaires
            loadComments(oeuvre, commentsContainer);

            // Section pour ajouter un commentaire
            VBox addCommentSection = createAddCommentSection(oeuvre, commentsContainer);

            // Assemblage de la zone principale
            mainContent.getChildren().addAll(
                    createSectionLabel("Discussions"),
                    commentsScroll,
                    createSectionDivider(),
                    createSectionLabel("Nouveau commentaire"),
                    addCommentSection
            );

            root.setCenter(mainContent);

            // Configuration de la scène et affichage
            Scene scene = new Scene(root, 500, 600);
            commentStage.setScene(scene);
            commentStage.setResizable(false);
            commentStage.showAndWait();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les commentaires: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Création d'un en-tête personnalisé pour la fenêtre
    private HBox createCustomHeader(String oeuvreName, Stage stage) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-background-radius: 10 10 0 0;");

        Label titleLabel = new Label("Commentaires - " + oeuvreName);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
        closeButton.setOnAction(e -> stage.close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);
        return header;
    }

    // Création d'une section pour ajouter des commentaires
    private VBox createAddCommentSection(Oeuvre oeuvre, VBox commentsContainer) {
        VBox addCommentBox = new VBox(10);

        TextArea commentField = new TextArea();
        commentField.setPromptText("Partagez votre avis...");
        commentField.setPrefRowCount(3);
        commentField.setWrapText(true);
        commentField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dddddd;");

        Button submitButton = new Button("Publier");
        submitButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-font-weight: bold; -fx-padding: 10 20;");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(submitButton);

        submitButton.setOnAction(e -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                addComment(oeuvre, content, commentsContainer);
                commentField.clear();
            } else {
                showAlert("Attention", "Le commentaire ne peut pas être vide");
            }
        });

        addCommentBox.getChildren().addAll(commentField, buttonBox);
        return addCommentBox;
    }

    // Méthode pour créer un label de section
    private HBox createSectionLabel(String text) {
        HBox box = new HBox();

        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setTextFill(Color.web(PRIMARY_COLOR));

        box.getChildren().add(label);
        return box;
    }

    // Méthode pour créer un séparateur de section
    private HBox createSectionDivider() {
        HBox box = new HBox();
        box.setPadding(new Insets(5, 0, 5, 0));

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #dddddd;");
        HBox.setHgrow(separator, Priority.ALWAYS);

        box.getChildren().add(separator);
        return box;
    }

    // Méthode pour charger les commentaires existants
    private void loadComments(Oeuvre oeuvre, VBox container) {
        try {
            // Vider le conteneur
            container.getChildren().clear();

            // Récupérer les commentaires depuis la base de données
            List<Commentaire> commentaires = commentaireService.getByOeuvreId(oeuvre.getId());

            if (commentaires.isEmpty()) {
                VBox emptyBox = new VBox();
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setPadding(new Insets(30));

                Label emptyLabel = new Label("Aucun commentaire pour le moment");
                emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #888888;");

                emptyBox.getChildren().add(emptyLabel);
                container.getChildren().add(emptyBox);
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
            // Filtrer les mots inappropriés
            BadWordFilterService filterService = new BadWordFilterService();
            String filteredContent = filterService.filterBadWords(content);

            // Créer un nouveau commentaire avec le contenu filtré
            Commentaire commentaire = new Commentaire(filteredContent, 1, oeuvre);

            // Vérifier si le commentaire existe déjà
            if (commentaireService.commentaireExists(filteredContent, commentaire.getUserId(), oeuvre.getId())) {
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
    private VBox createCommentNode(Commentaire commentaire, String userName, VBox container, Oeuvre oeuvre) {
        VBox commentBox = new VBox(8);
        commentBox.setPadding(new Insets(15));
        commentBox.setStyle("-fx-background-color: " + COMMENT_BG_COLOR + "; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, #00000022, 4, 0, 0, 1);");

        // En-tête du commentaire (utilisateur + date + menu)
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Avatar (cercle avec première lettre)
        StackPane avatar = createUserAvatar(userName);

        VBox userInfo = new VBox(2);

        Label username = new Label(userName);
        username.setFont(Font.font("System", FontWeight.BOLD, 13));

        Label date = new Label(commentaire.getDate().format(formatter));
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");

        userInfo.getChildren().addAll(username, date);

        // Bouton avec trois points pour le menu
        Button menuButton = new Button("⋮");
        menuButton.setStyle("-fx-background-color: transparent; -fx-font-weight: bold;");

        // Créer le menu contextuel
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifier");
        MenuItem deleteItem = new MenuItem("Supprimer");

        editItem.setOnAction(e -> showEditDialog(commentaire, container, oeuvre));
        deleteItem.setOnAction(e -> deleteComment(commentaire, container, oeuvre));

        contextMenu.getItems().addAll(editItem, deleteItem);
        menuButton.setOnAction(e -> contextMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 0));

        // Créer un spacer pour pousser le menu à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(avatar, userInfo, spacer, menuButton);

        // Contenu du commentaire
        Text content = new Text(commentaire.getContenu());
        content.setWrappingWidth(400);

        // Assembler le tout
        commentBox.getChildren().addAll(headerBox, content);

        return commentBox;
    }

    // Créer un avatar pour l'utilisateur
    private StackPane createUserAvatar(String userName) {
        StackPane avatar = new StackPane();
        avatar.setMinSize(36, 36);
        avatar.setMaxSize(36, 36);

        // Cercle de fond
        Region circle = new Region();
        circle.setMinSize(36, 36);
        circle.setMaxSize(36, 36);

        // Déterminer une couleur basée sur le nom d'utilisateur
        String colorHex = generateColorFromName(userName);
        circle.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 50%;");

        // Première lettre du nom d'utilisateur
        String initial = userName.substring(0, 1).toUpperCase();
        Label initialLabel = new Label(initial);
        initialLabel.setTextFill(Color.WHITE);
        initialLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        avatar.getChildren().addAll(circle, initialLabel);
        return avatar;
    }

    // Générer une couleur basée sur le nom d'utilisateur
    private String generateColorFromName(String name) {
        // Couleurs prédéfinies agréables
        String[] colors = {
                "#1abc9c", "#2ecc71", "#3498db", "#9b59b6", "#f1c40f",
                "#e67e22", "#e74c3c", "#34495e", "#16a085", "#27ae60",
                "#2980b9", "#8e44ad", "#f39c12", "#d35400", "#c0392b"
        };

        // Utiliser la somme des caractères pour choisir une couleur
        int hash = 0;
        for (char c : name.toCharArray()) {
            hash += c;
        }

        return colors[Math.abs(hash) % colors.length];
    }

    // Méthode pour afficher une boîte de dialogue d'édition de commentaire
    private void showEditDialog(Commentaire commentaire, VBox container, Oeuvre oeuvre) {
        // Création d'une nouvelle fenêtre personnalisée
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, #00000066, 10, 0, 0, 0);");

        Label titleLabel = new Label("Modifier votre commentaire");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        TextArea commentField = new TextArea(commentaire.getContenu());
        commentField.setWrapText(true);
        commentField.setPrefHeight(120);
        commentField.setPrefWidth(400);
        commentField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #dddddd;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; " +
                "-fx-background-radius: 5; -fx-padding: 8 15;");

        Button saveButton = new Button("Enregistrer");
        saveButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 8 15; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(cancelButton, saveButton);

        root.getChildren().addAll(titleLabel, commentField, buttonBox);

        // Configuration des actions
        cancelButton.setOnAction(e -> editStage.close());

        saveButton.setOnAction(e -> {
            String newContent = commentField.getText().trim();
            if (!newContent.isEmpty()) {
                try {
                    // S'assurer que le commentaire a bien une référence à l'œuvre
                    if (commentaire.getOeuvre() == null) {
                        commentaire.setOeuvre(oeuvre);
                    }

                    // Mettre à jour le commentaire
                    commentaire.setContenu(newContent);
                    commentaireService.update(commentaire);

                    // Recharger les commentaires
                    loadComments(oeuvre, container);

                    editStage.close();
                } catch (SQLException ex) {
                    showAlert("Erreur", "Erreur lors de la modification du commentaire: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                showAlert("Erreur", "Le commentaire ne peut pas être vide");
            }
        });

        Scene scene = new Scene(root);
        editStage.setScene(scene);
        editStage.showAndWait();
    }

    // Méthode pour supprimer un commentaire avec confirmation
    private void deleteComment(Commentaire commentaire, VBox container, Oeuvre oeuvre) {
        // Création d'une boîte de dialogue de confirmation personnalisée
        Stage confirmStage = new Stage();
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, #00000066, 10, 0, 0, 0);");

        Label warningIcon = new Label("⚠️");
        warningIcon.setFont(Font.font("System", 24));
        warningIcon.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Confirmation de suppression");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setAlignment(Pos.CENTER);

        Label messageLabel = new Label("Êtes-vous sûr de vouloir supprimer ce commentaire ? Cette action est irréversible.");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelButton = new Button("Annuler");
        cancelButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333333; " +
                "-fx-background-radius: 5; -fx-padding: 8 20;");

        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-background-radius: 5; -fx-padding: 8 20; -fx-font-weight: bold;");

        buttonBox.getChildren().addAll(cancelButton, deleteButton);

        VBox iconBox = new VBox(warningIcon);
        iconBox.setAlignment(Pos.CENTER);

        VBox textBox = new VBox(5, titleLabel, messageLabel);
        textBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(iconBox, textBox, buttonBox);

        // Configuration des actions
        cancelButton.setOnAction(e -> confirmStage.close());

        deleteButton.setOnAction(e -> {
            try {
                // Supprimer le commentaire
                commentaireService.delete(commentaire);

                // Recharger les commentaires
                loadComments(oeuvre, container);

                confirmStage.close();
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur lors de la suppression du commentaire: " + ex.getMessage());
                ex.printStackTrace();
                confirmStage.close();
            }
        });

        Scene scene = new Scene(root, 400, 200);
        confirmStage.setScene(scene);
        confirmStage.showAndWait();
    }

    // Afficher une alerte personnalisée
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Stage alertStage = new Stage();
            alertStage.initModality(Modality.APPLICATION_MODAL);
            alertStage.initStyle(StageStyle.UNDECORATED);

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, #00000066, 10, 0, 0, 0);");

            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            messageLabel.setAlignment(Pos.CENTER);

            Button okButton = new Button("OK");
            okButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 5; -fx-padding: 8 20; -fx-font-weight: bold;");
            okButton.setOnAction(e -> alertStage.close());

            root.getChildren().addAll(titleLabel, messageLabel, okButton);

            Scene scene = new Scene(root, 350, 150);
            alertStage.setScene(scene);
            alertStage.showAndWait();
        });
    }
}