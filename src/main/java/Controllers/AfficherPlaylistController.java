package Controllers;

import Models.Playlist;
import Services.MusicService;
import Services.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherPlaylistController {

    @FXML
    private GridPane playlistGrid;

    @FXML
    public void initialize() {
        PlaylistService playlistService = new PlaylistService();
        List<Playlist> playlists = null;
        try {
            playlists = playlistService.getAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int column = 0;  // Column tracker
        int row = 0;     // Row tracker

        // Iterate through the playlists
        for (Playlist playlist : playlists) {
            VBox playlistCard = createPlaylistCard(playlist);  // Create card for the playlist

            // Add the card to the grid at the specified column and row
            playlistGrid.add(playlistCard, column, row);

            // Move to the next column
            column++;

            // If we've filled up 3 columns, move to the next row and reset column
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }



    private VBox createPlaylistCard(Playlist playlist) {
        VBox card = new VBox(10);
        card.setStyle("""
        -fx-border-color: #ccddee;
        -fx-padding: 12;
        -fx-background-color: #f0f4f8;
        -fx-border-radius: 12;
        -fx-background-radius: 12;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 2, 2);
    """);
        card.setPrefWidth(250);
        Label titreLabel = new Label("🎵 Titre: " + playlist.getTitre_p());
        Label descLabel = new Label("📝 Description: " + playlist.getDescription());
        Label dateLabel = new Label("📅 Date: " + playlist.getDate_creation());

        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a2a2a;");
        descLabel.setStyle("-fx-text-fill: #2a2a2a;");
        dateLabel.setStyle("-fx-text-fill: #2a2a2a;");


        Button btnDelete = new Button("🗑 Supprimer");
        Button btnUpdate = new Button("✏ Modifier");
        Button btnDetails = new Button("👁 Détails");

        // Apply soft blue style like music section
        styleButton(btnDelete);
        styleButton(btnUpdate);
        styleButton(btnDetails);

        btnDelete.setOnAction(e -> {
            PlaylistService playlistService = new PlaylistService();
            try {
                playlistService.delete(playlist);
                playlistGrid.getChildren().clear();
                initialize(); // Refresh
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnUpdate.setOnAction(e -> openUpdateForm(playlist));
        btnDetails.setOnAction(e -> showPlaylistDetails(playlist));

        HBox buttonBox = new HBox(10, btnDelete, btnUpdate, btnDetails);
        buttonBox.setStyle("-fx-alignment: center;");

        card.getChildren().addAll(titreLabel, descLabel, dateLabel, buttonBox);

        return card;
    }
    private void styleButton(Button button) {
        button.setStyle("""
        -fx-background-color: #dceeff;
        -fx-text-fill: #004080;
        -fx-background-radius: 6;
        -fx-padding: 5 10 5 10;
        -fx-font-size: 12;
    """);
    }


    @FXML
    private void handleRetourMusique(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherMusic.fxml"));
            Parent musicRoot = loader.load();

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(musicRoot);
            currentStage.setScene(scene);
            currentStage.setTitle("Liste des Musiques");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAjouterPlaylist(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPlaylist.fxml"));
        Scene scene;

        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du chargement de AjouterPlaylist.fxml", e);
        }

        Stage addStage = new Stage();
        addStage.setTitle("Ajouter une playlist");
        addStage.setScene(scene);

        // Rafraîchir la grille des playlists après la fermeture de la fenêtre d'ajout
        addStage.setOnHiding(e -> afficherPlaylists());  // Remplacez par votre méthode de rafraîchissement réelle

        addStage.show();
    }

    private void refreshPlaylistGrid() {
        // Clear the existing grid
        playlistGrid.getChildren().clear();

        // Reload the playlists from the database and update the grid
        PlaylistService playlistService = new PlaylistService();
        try {
            List<Playlist> playlists = playlistService.getAll();
            for (Playlist playlist : playlists) {
                VBox playlistCard = createPlaylistCard(playlist);
                playlistGrid.add(playlistCard, 0, playlistGrid.getRowCount());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openUpdateForm(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdatePlaylist.fxml"));
            Parent root = loader.load();

            UpdatePlaylistController controller = loader.getController();
            controller.setPlaylist(playlist);

            Stage stage = new Stage();
            stage.setTitle("Modifier Playlist");
            stage.setScene(new Scene(root));

            // 🔁 Refresh playlists after closing the update window
            stage.setOnHidden(event -> {
                playlistGrid.getChildren().clear(); // Clear existing cards (assuming your GridPane is named playlistGrid)
                try {
                    initialize();          // Reload playlists (assuming your controller has initialize method)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void afficherPlaylists() {
        playlistGrid.getChildren().clear(); // Clear the grid first
        PlaylistService playlistService = new PlaylistService();

        try {
            List<Playlist> playlists = playlistService.getAll();

            int column = 0;
            int row = 0;

            for (Playlist p : playlists) {
                VBox card = createPlaylistCard(p); // create a VBox card for each playlist

                playlistGrid.add(card, column, row);
                GridPane.setFillWidth(card, true);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void showPlaylistDetails(Playlist playlist) {
        try {
            VBox detailBox = new VBox(15);
            detailBox.setPadding(new Insets(20));
            detailBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10;");

            Label titre = new Label("🎵 Titre Playlist : " + playlist.getTitre_p());
            titre.setFont(Font.font("Arial", 16));
            titre.setTextFill(Color.DARKSLATEBLUE);

            Label description = new Label("📝 Description : " + playlist.getDescription());
            description.setWrapText(true);
            description.setFont(Font.font("Arial", 13));
            description.setTextFill(Color.GRAY);

            Label date = new Label("📅 Date de création : " + playlist.getDate_creation());
            date.setFont(Font.font("Arial", 13));
            date.setTextFill(Color.DIMGRAY);

            detailBox.getChildren().addAll(titre, description, date);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Playlist");
            stage.setScene(new Scene(detailBox, 400, 300));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
