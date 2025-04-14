package Controllers;

import Models.Music;
import Models.Playlist;
import Services.MusicService;
import Services.PlaylistService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.geometry.Insets;

public class AfficherMusicController implements Initializable {

    @FXML
    private GridPane musicGrid;
    private Object song;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MusicService musicService = new MusicService();
        List<Music> musicList;

        try {
            musicList = musicService.getAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int column = 0;
        int row = 0;

        for (Music music : musicList) {
            VBox musicCard = createMusicCard(music);
            musicGrid.add(musicCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createMusicCard(Music music) {
        VBox card = new VBox(10); // spacing
        card.setStyle("""
        -fx-border-color: #ccddee;
        -fx-padding: 12;
        -fx-background-color: #f0f4f8;
        -fx-border-radius: 12;
        -fx-background-radius: 12;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 2, 2);
    """);
        card.setPrefWidth(250);

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);  // Wider image
        imageView.setFitHeight(150);
        try {
            Image image = new Image(new File(music.getPhoto()).toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Image not loaded: " + e.getMessage());
        }

        // Labels: Titre (bold) and Artist (same row)
        Label titreLabel = new Label("🎵 " + music.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a2a2a;");

        Label artistLabel = new Label("👤 " + music.getArtistName());
        artistLabel.setStyle("-fx-text-fill: #444444;");

        HBox titreArtistBox = new HBox(10, titreLabel, artistLabel);
        titreArtistBox.setAlignment(Pos.CENTER_LEFT);

        // Genre
        Label genreLabel = new Label("🎼 Genre: " + music.getGenre());
        genreLabel.setStyle("-fx-text-fill: #555555;");

        // ComboBox to select a playlist
        ComboBox<Playlist> playlistComboBox = new ComboBox<>();
        playlistComboBox.setPromptText("🎧 Choisir playlist");

        try {
            PlaylistService playlistService = new PlaylistService();
            List<Playlist> playlists = playlistService.getAll();
            playlistComboBox.getItems().addAll(playlists);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        playlistComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                setText(empty || playlist == null ? null : playlist.getTitre_p());
            }
        });
        playlistComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Playlist playlist, boolean empty) {
                super.updateItem(playlist, empty);
                setText(empty || playlist == null ? null : playlist.getTitre_p());
            }
        });

        // Playlist buttons
        Button addToPlaylistBtn = new Button("➕ Ajouter");
        Button removeFromPlaylistBtn = new Button("❌ Retirer");

        styleButton(addToPlaylistBtn);
        styleButton(removeFromPlaylistBtn);

        addToPlaylistBtn.setOnAction(e -> {
            Playlist selectedPlaylist = playlistComboBox.getValue();
            if (selectedPlaylist != null) {
                try {
                    PlaylistService playlistService = new PlaylistService();
                    playlistService.addMusicToPlaylist(music, selectedPlaylist);
                    System.out.println("✅ Ajoutee à " + selectedPlaylist.getTitre_p());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        removeFromPlaylistBtn.setOnAction(e -> {
            Playlist selectedPlaylist = playlistComboBox.getValue();
            if (selectedPlaylist != null) {
                try {
                    PlaylistService playlistService = new PlaylistService();
                    playlistService.removeMusicFromPlaylist(music, selectedPlaylist);
                    System.out.println("🗑 Supprimee de " + selectedPlaylist.getTitre_p());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox playlistBox = new HBox(10, playlistComboBox, addToPlaylistBtn, removeFromPlaylistBtn);
        playlistBox.setAlignment(Pos.CENTER_LEFT);

        // MediaPlayer controls
        Button playButton = new Button("▶ Play");
        Button pauseButton = new Button("⏸ Pause");
        styleButton(playButton);
        styleButton(pauseButton);

        playButton.setDisable(true);
        pauseButton.setDisable(true);

        try {
            Media media = new Media(new File(music.getCheminFichier()).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            playButton.setDisable(false);
            pauseButton.setDisable(false);

            playButton.setOnAction(e -> mediaPlayer.play());
            pauseButton.setOnAction(e -> mediaPlayer.pause());

        } catch (Exception e) {
            System.out.println("Audio not loaded: " + e.getMessage());
        }

        HBox audioBox = new HBox(10, playButton, pauseButton);
        audioBox.setAlignment(Pos.CENTER_LEFT);

        // Action buttons
        Button btnDelete = new Button("🗑 Supprimer");
        Button btnUpdate = new Button("✏ Modifier");
        Button btnDetails = new Button("👁 Détails");

        styleButton(btnDelete);
        styleButton(btnUpdate);
        styleButton(btnDetails);
        styleComboBox(playlistComboBox);

        btnDelete.setOnAction(e -> {
            MusicService service = new MusicService();
            try {
                service.delete(music);
                musicGrid.getChildren().clear();
                initialize(null, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnUpdate.setOnAction(e -> openUpdateForm(music));
        btnDetails.setOnAction(e -> showMusicDetails(music));

        HBox buttonBox = new HBox(10, btnDelete, btnUpdate, btnDetails);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Final layout
        card.getChildren().addAll(
                imageView,
                titreArtistBox,
                genreLabel,
                audioBox,
                playlistBox,
                buttonBox
        );

        return card;
    }

    // Helper method for styling buttons
    private void styleButton(Button button) {
        button.setStyle("""
        -fx-background-color: #dceeff;
        -fx-text-fill: #004080;
        -fx-background-radius: 6;
        -fx-padding: 5 10 5 10;
        -fx-font-size: 12;
    """);
    }
    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("""
        -fx-background-color: #dceeff;
        -fx-text-fill: #004080;
        -fx-background-radius: 6;
        -fx-padding: 2 8 2 8;  /* smaller vertical padding */
        -fx-font-size: 11;
        -fx-pref-height: 28;   /* control overall height */
    """);
    }

    @FXML
    private void handleAfficherPlaylists(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherPlaylist.fxml")); // Make sure this path is correct
            Parent playlistView = loader.load();

            // Get the current stage from the event
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Set the new scene to the current stage
            Scene newScene = new Scene(playlistView);
            currentStage.setScene(newScene);
            currentStage.setTitle("Liste des Playlists");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openUpdateForm(Music music) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateMusic.fxml"));
            Parent root = loader.load();

            UpdateMusicController controller = loader.getController();
            controller.setMusic(music);

            Stage stage = new Stage();
            stage.setTitle("Modifier Musique");
            stage.setScene(new Scene(root));

            // Refresh the music grid after the update window is closed
            stage.setOnHidden(event -> {
                musicGrid.getChildren().clear();   // Clear old cards
                try {
                    initialize(null, null);        // Reload updated music list
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @FXML
    private void handleAddMusicButton(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMusic.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Stage addStage = new Stage();
        addStage.setTitle("Ajouter une musique");
        addStage.setScene(scene);

        // Rafraîchir la grille ou liste après fermeture
        addStage.setOnHiding(e -> afficherMusique());  // Replace with your actual refresh method

        addStage.show();

    }

    private void afficherMusique() {
        musicGrid.getChildren().clear(); // Clear the grid first
        MusicService musicService = new MusicService();

        try {
            List<Music> musics = musicService.getAll();

            int column = 0;
            int row = 0;

            for (Music m : musics) {
                VBox card = createMusicCard(m); // create a VBox card for each music

                musicGrid.add(card, column, row);
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    private void showMusicDetails(Music music) {
        try {
            VBox detailBox = new VBox(15);
            detailBox.setPadding(new Insets(20));
            detailBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 10;");

            Label titre = new Label("🎵 Titre : " + music.getTitre());
            titre.setFont(Font.font("Arial", 16));
            titre.setTextFill(Color.DARKSLATEBLUE);

            Label artist = new Label("👤 Artiste : " + music.getArtistName());
            artist.setFont(Font.font("Arial", 14));
            artist.setTextFill(Color.DARKSLATEGRAY);

            Label genre = new Label("🎼 Genre : " + music.getGenre());
            genre.setFont(Font.font("Arial", 14));
            genre.setTextFill(Color.DARKSLATEGRAY);

            Label description = new Label("📝 Description : " + music.getDescription());
            description.setWrapText(true);
            description.setFont(Font.font("Arial", 13));
            description.setTextFill(Color.GRAY);

            Label date = new Label("📅 Date de sortie : " + music.getDateSortie());
            date.setFont(Font.font("Arial", 13));
            date.setTextFill(Color.DIMGRAY);

            Label chemin = new Label("📁 Fichier : " + music.getCheminFichier());
            chemin.setFont(Font.font("Arial", 12));
            chemin.setTextFill(Color.GRAY);

            // Optional: show image if exists
            ImageView photo = new ImageView();
            photo.setFitWidth(150);
            photo.setFitHeight(150);
            try {
                //     Image img = new Image(getClass().getResource("/images/" + music.getPhoto()).toExternalForm());
                Image img = new Image(new File(music.getPhoto()).toURI().toString());
                photo.setImage(img);
            } catch (Exception ex) {
                System.out.println("Image not found: " + ex.getMessage());
            }

            detailBox.getChildren().addAll(titre, artist, genre, description, date, chemin, photo);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Musique");
            stage.setScene(new Scene(detailBox, 400, 500));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
