package Controllers;

import Models.Playlist;
import Services.MusicService;
import Services.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherPlaylistController {

    @FXML
    private GridPane playlistGrid;

    private Runnable onAfficherTouteMusiqueListener;

    private MusiqueEtPlaylistsController mainController;

    public void setMainController(MusiqueEtPlaylistsController controller) {
        this.mainController = controller;
    }


    public void setOnAfficherTouteMusiqueListener(Runnable listener) {
        this.onAfficherTouteMusiqueListener = listener;
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (mainController != null) {
            mainController.reloadLeftPane();
        } else {
            System.out.println("MainController is null!");
        }
    }


    @FXML
    public void initialize() {
        // Add playlists below the button
        PlaylistService playlistService = new PlaylistService();
        List<Playlist> playlists;
        try {
            playlists = playlistService.getAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int row = 1;
        for (Playlist playlist : playlists) {
            VBox playlistCard = createPlaylistCard(playlist);
            playlistGrid.add(playlistCard, 0, row);
            GridPane.setFillWidth(playlistCard, true);
            row++;
        }
    }

    private VBox createPlaylistCard(Playlist playlist) {
        VBox card = new VBox(10);
        card.setStyle("""
        -fx-border-color: #d3c4a3; /* Same soft border color as the button */
    -fx-border-radius: 10; /* Rounded corners */
    -fx-background-color: #e8e1c6; /* Beige background to match button */
    -fx-background-radius: 10; /* Rounded corners */
    -fx-padding: 12; /* Padding inside the VBox */
    -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 2, 2); /* Subtle shadow for depth */
    """);
        card.setPrefWidth(250);

        Label titreLabel = new Label("🎵 Titre: " + playlist.getTitre_p());
        Label descLabel = new Label("📝 Description: " + playlist.getDescription());
//        Label dateLabel = new Label("📅 Date: " + playlist.getDate_creation());

        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a2a2a;");
        descLabel.setStyle("-fx-text-fill: #2a2a2a;");
//        dateLabel.setStyle("-fx-text-fill: #2a2a2a;");

        // 3-dot menu button
        Button moreButton = new Button("⋮");
        moreButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");

// Context menu for actions
        ContextMenu contextMenu = new ContextMenu();

        MenuItem updateItem = new MenuItem("✏ Modifier");
        MenuItem deleteItem = new MenuItem("🗑 Supprimer");
        MenuItem detailsItem = new MenuItem("👁 Détails");

// Playlist-specific actions
        updateItem.setOnAction(e -> openUpdateForm(playlist));
        deleteItem.setOnAction(e -> {
            PlaylistService playlistService = new PlaylistService();
            try {
                playlistService.delete(playlist);
                playlistGrid.getChildren().clear();
                initialize(); // Refresh
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        detailsItem.setOnAction(e -> showPlaylistDetails(playlist));

// Add items to the context menu
        contextMenu.getItems().addAll(updateItem, deleteItem, detailsItem);

// Show context menu on click
        moreButton.setOnMouseClicked(e -> {
            contextMenu.show(moreButton, e.getScreenX(), e.getScreenY());
        });
// Create a horizontal box for the description and 3-dot button
        HBox buttonBox = new HBox(10, descLabel, moreButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT); // Align contents within the HBox
        HBox.setHgrow(descLabel, Priority.ALWAYS); // Allow descLabel to grow and push moreButton to the right
        descLabel.setMaxWidth(Double.MAX_VALUE);   // Required to make the grow effective

// Add to the card layout
        card.getChildren().addAll(titreLabel, buttonBox);


        // 👉 Filtering music using the injected musicController
        card.setOnMouseClicked(event -> {
            try {
                mainController.loadMusicByPlaylistId(playlist.getId()); // Call the method you already wrote
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

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
    private void handleAjouterPlaylist(ActionEvent event) {
        try {
            // Load the "AjouterPlaylist.fxml" content
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterPlaylist.fxml"));
            AnchorPane addPlaylistPane = loader.load();

            // Find the left pane of your main layout (where you want to show the add playlist form)
            playlistGrid.getChildren().clear();  // Clear the existing content
            playlistGrid.getChildren().add(addPlaylistPane);  // Add the new content

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void openUpdateForm(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdatePlaylist.fxml"));
            AnchorPane updatePlaylistPane = loader.load();

            UpdatePlaylistController controller = loader.getController();
            controller.setPlaylist(playlist); // pass the selected playlist to update

            // Replace leftPane content (similar to music logic)
            playlistGrid.getChildren().clear();
            playlistGrid.getChildren().add(updatePlaylistPane);
            initialize();
            AnchorPane.setTopAnchor(updatePlaylistPane, 0.0);
            AnchorPane.setBottomAnchor(updatePlaylistPane, 0.0);
            AnchorPane.setLeftAnchor(updatePlaylistPane, 0.0);
            AnchorPane.setRightAnchor(updatePlaylistPane, 0.0);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    private void afficherPlaylists() {
        playlistGrid.getChildren().clear(); // Clear the grid first
        PlaylistService playlistService = new PlaylistService();

        try {
            List<Playlist> playlists = playlistService.getAll();

            int row = 0;

            for (Playlist p : playlists) {
                VBox card = createPlaylistCard(p); // create a VBox card for each playlist

                playlistGrid.add(card, 0, row); // always column 0
                GridPane.setFillWidth(card, true); // make card fill horizontally if needed

                row++; // move to the next row
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
