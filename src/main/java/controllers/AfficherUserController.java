package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.SessionManager;
import utils.SessionStorage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import utils.EmailSender;


public class AfficherUserController {



    @FXML
    private GridPane userGrid;

    @FXML
    private Button addButton; // Bouton d'ajout

    @FXML
    public void initialize() {
        UserService userService = new UserService();

        try {
            List<User> userList = userService.select(); // Récupère la liste des utilisateurs
            User currentUser = SessionManager.getCurrentUser(); // Récupère l'utilisateur connecté

            int column = 0;
            int row = 0;

            // Boucle pour afficher chaque utilisateur
            for (User user : userList) {
                VBox card = new VBox();
                card.setSpacing(10);
                card.getStyleClass().add("user-card");
                card.setPrefWidth(260);
                card.setPrefHeight(200);

                Text nomText = new Text("👤 Nom: " + user.getNom());
                Text prenomText = new Text("🧾 Prénom: " + user.getPrenom());
                Text emailText = new Text("📧 Email: " + user.getEmail());
                nomText.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50;");
                prenomText.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50;");
                emailText.setStyle("-fx-font-size: 14px; -fx-fill: #2c3e50;");

                Button detailButton = new Button("🔍 Détails");
                detailButton.setStyle("-fx-background-color: #5dade2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
                detailButton.setOnAction(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailUser.fxml"));
                        Parent root = loader.load();

                        // Passage des données de l'utilisateur sélectionné à la vue de détail
                        DetailUserController detailUserController = loader.getController();
                        detailUserController.setNom(user.getNom());
                        detailUserController.setPrenom(user.getPrenom());
                        detailUserController.setGenre(user.getGenre());
                        detailUserController.setEmail(user.getEmail());
                        detailUserController.setPassword(user.getPassword());
                        detailUserController.setRole(user.getRole());

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Détails de l'utilisateur");
                        stage.show();
                    } catch (IOException e) {
                        System.out.println("Erreur lors de la redirection vers Détails : " + e.getMessage());
                    }
                });

                // 🗑️ Bouton Supprimer
                Button deleteButton = new Button("\uD83D\uDDD1 Supprimer");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
                deleteButton.setOnAction(event -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation de suppression");
                    alert.setHeaderText(null);
                    alert.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            userService.delete(user.getId()); // Suppression de l'utilisateur
                            refreshUserGrid();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // ✏️ Bouton Modifier
                Button editButton = new Button("✏ Modifier");
                editButton.setStyle("-fx-background-color: #58d68d; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
                editButton.setOnAction(event -> {
                    User currentUserInSession = SessionManager.getCurrentUser();
                    if (currentUserInSession != null && !"admin".equalsIgnoreCase(currentUserInSession.getRole())) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Accès refusé");
                        alert.setContentText("Vous devez être administrateur pour modifier un utilisateur.");
                        alert.show();
                        return;
                    }

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierUser.fxml"));
                        Scene scene = new Scene(loader.load());

                        ModifierUserController controller = loader.getController();
                        controller.setUser(user);

                        Stage editStage = new Stage();
                        editStage.setTitle("Modifier utilisateur");
                        editStage.setScene(scene);
                        editStage.setOnHiding(e -> refreshUserGrid());
                        EmailSender.sendLoginNotification(user.getEmail(), user.getPrenom());
                        editStage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // 🧩 Ajout des composants dans le card
                card.getChildren().addAll(nomText, prenomText, emailText, detailButton);

                // Si l'utilisateur connecté est un administrateur, on ajoute les boutons Modifier et Supprimer
                if (currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole())) {
                    card.getChildren().addAll(editButton, deleteButton);
                }

                userGrid.add(card, column, row);
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            // Masquer le bouton "Ajouter un utilisateur" si l'utilisateur n'est pas admin
            if (addButton != null) {
                if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getRole())) {
                    addButton.setVisible(false);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Rafraîchissement de la grille des utilisateurs
    private void refreshUserGrid() {
        userGrid.getChildren().clear();
        initialize();
    }

    // Action pour le bouton "Ajouter un utilisateur"
    @FXML
    private void handleAddButtonAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Ajouter un utilisateur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Action pour ouvrir la page de modification du mot de passe
    @FXML
    private void ouvrirModifierMotDePasse() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMotDePasse.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Modifier mon mot de passe");
        stage.show();
    }

    // Déconnexion de l'utilisateur
    @FXML
    private void handleLogout(ActionEvent event) {
        SessionStorage.clearSession();
        SessionManager.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gestion des réclamations
    @FXML
    private void handleReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes réclamations");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter une réclamation");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAfficherReclamations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherReclamation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Réclamations");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Voir le profil de l'utilisateur connecté
    @FXML
    private void handleVoirMonProfil(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Parent root = loader.load();

            ProfileController profileController = loader.getController();
            User currentUser = SessionManager.getCurrentUser();

            if (currentUser != null) {
                profileController.setUser(currentUser);

                Stage stage = new Stage();
                stage.setTitle("Mon Profil");
                stage.setScene(new Scene(root));
                stage.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleOpenChat(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/chat.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Chat en ligne");
            stage.show();

            // Fermer la fenêtre actuelle si nécessaire
            // ((Node)(event.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir le chat: " + e.getMessage());
            alert.show();
        }
    }
}
