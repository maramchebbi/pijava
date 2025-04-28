package Controllers;

import Models.Music;
import Models.Playlist;
import Models.Recommendation;
import Services.LikeService;
import Services.MusicService;
import Services.PlaylistService;
import Utils.DeezerAPI;
import Utils.HostServicesUtil;
import javafx.application.HostServices;
import javafx.application.Platform;
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
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.FontWeight;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.util.Duration;


public class AfficherMusicController implements Initializable {

    @FXML
    private GridPane musicGrid;

    @FXML private TextField searchField;

    @FXML
    private ComboBox<String> genreComboBox;

    @FXML
    private void handleRefreshRightSide() {
        // Reload the music normally, depending on your setup
        afficherMusique();
    }

    @FXML
    private void rechercherMusique() {
        MusicService musicService = new MusicService();
        String keyword = searchField.getText().trim().toLowerCase();
        try {
            List<Music> allMusics = musicService.getAll(); // or whatever method gets all musics
            List<Music> filtered = allMusics.stream()
                    .filter(music -> music.getTitre().toLowerCase().contains(keyword) ||
                            music.getArtistName().toLowerCase().contains(keyword) ||
                            music.getGenre().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
            displayMusics(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void trierParGenre() {
        String selectedGenre = genreComboBox.getValue();

        try {
            List<Music> musics;
            MusicService musicService = new MusicService();

            if ("Tous".equals(selectedGenre)) {
                musics = musicService.getAll();
            } else {
                musics = musicService.getByGenre(selectedGenre);
            }

            displayMusics(musics);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void displayMusics(List<Music> musics) {
        musicGrid.getChildren().clear(); // Clear the grid first

        int column = 0;
        int row = 0;

        for (Music m : musics) {
            VBox card = createMusicCard(m); // create a VBox card for each music

            musicGrid.add(card, column, row);
            GridPane.setFillWidth(card, true);

            column++;
            if (column == 3) { // 3 cards per row
                column = 0;
                row++;
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MusicService musicService = new MusicService();
        List<Music> musicList;

        try {
            List<String> genres = musicService.getAllGenres();

            genreComboBox.getItems().add("Tous"); // add 'All' first
            genreComboBox.getItems().addAll(genres); // then genres from DB
            genreComboBox.setValue("Tous");

        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        // 💡 Force grid size reset to match parent
        GridPane.setHgrow(musicGrid, Priority.ALWAYS);
        AnchorPane.setTopAnchor(musicGrid, 0.0);
        AnchorPane.setBottomAnchor(musicGrid, 0.0);
        AnchorPane.setLeftAnchor(musicGrid, 0.0);
        AnchorPane.setRightAnchor(musicGrid, 0.0);
    }

    public void loadMusicByPlaylist(int playlistId) throws SQLException {
        MusicService musicService = new MusicService();
        List<Music> filteredMusicList = musicService.getMusicsByPlaylistId(playlistId);

        musicGrid.getChildren().clear();

        int column = 0;
        int row = 0;

        for (Music music : filteredMusicList) {
            VBox musicCard = createMusicCard(music);
            musicGrid.add(musicCard, column, row);

            column++;
            if (column == 3) {
                column = 0;
                row++;
            }
        }

        // 💡 Force grid size reset to match parent
        GridPane.setHgrow(musicGrid, Priority.ALWAYS);
        AnchorPane.setTopAnchor(musicGrid, 0.0);
        AnchorPane.setBottomAnchor(musicGrid, 0.0);
        AnchorPane.setLeftAnchor(musicGrid, 0.0);
        AnchorPane.setRightAnchor(musicGrid, 0.0);
    }



    private VBox createMusicCard(Music music) {
        VBox card = new VBox(10); // spacing between children
        card.setStyle("""
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-background-color: #e8e1c6;
        -fx-background-radius: 10;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 2, 2);
    """);

        card.setPrefWidth(250);
        card.setMinWidth(250);

        // --- ImageView setup ---
        ImageView imageView = new ImageView();
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        try {
            Image image = new Image(new File(music.getPhoto()).toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Image not loaded: " + e.getMessage());
        }

        // --- Wrap image in HBox centered ---
        HBox imageBox = new HBox(imageView);
        imageBox.setAlignment(Pos.CENTER);

        // Add the HBox to the card
        card.getChildren().add(imageBox);

        // --- Resize behavior: Bind image width to card width minus margin ---
        card.widthProperty().addListener((obs, oldVal, newVal) -> {
            double cardWidth = newVal.doubleValue();
            double imageTargetWidth = cardWidth -30;
            imageView.setFitWidth(imageTargetWidth);
        });

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

        // Create controls container
        VBox controlsContainer = createMusicControls(music);

        // Final layout
        card.getChildren().addAll(
                imageView,
                titreArtistBox,
                genreLabel,
                controlsContainer
        );

        card.setOnMouseClicked(event -> {
            displayMusicDetails(music);
        });

        return card;
    }

    private VBox createMusicControls(Music music) {
        VBox controlsContainer = new VBox(10);

        // Playlist ComboBox and buttons
        ComboBox<Playlist> playlistComboBox = createPlaylistComboBox(music);
        Button addToPlaylistBtn = createAddToPlaylistButton(music, playlistComboBox);
        Button removeFromPlaylistBtn = createRemoveFromPlaylistButton(music, playlistComboBox);

        HBox playlistBox = new HBox(10, playlistComboBox, addToPlaylistBtn, removeFromPlaylistBtn);
        playlistBox.setAlignment(Pos.CENTER_LEFT);

        // MediaPlayer controls
        HBox audioBox = createAudioControls(music);

        // Like and more buttons
        HBox buttonBox = createActionButtons(music);

        controlsContainer.getChildren().addAll(audioBox, playlistBox, buttonBox);
        return controlsContainer;
    }

    private ComboBox<Playlist> createPlaylistComboBox(Music music) {
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

        styleComboBox(playlistComboBox);
        return playlistComboBox;
    }

    private Button createAddToPlaylistButton(Music music, ComboBox<Playlist> playlistComboBox) {
        Button addToPlaylistBtn = new Button("➕ Ajouter");
        styleButton(addToPlaylistBtn);

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

        return addToPlaylistBtn;
    }

    private Button createRemoveFromPlaylistButton(Music music, ComboBox<Playlist> playlistComboBox) {
        Button removeFromPlaylistBtn = new Button("❌ Retirer");
        styleButton(removeFromPlaylistBtn);

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

        return removeFromPlaylistBtn;
    }

    private HBox createAudioControls(Music music) {
        Button toggleButton = new Button("▶");
        styleButton(toggleButton);

        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setPrefWidth(150);
        progressSlider.setDisable(true);

        Label timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        try {
            Media media = new Media(new File(music.getCheminFichier()).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            toggleButton.setDisable(false);
            progressSlider.setDisable(false);

            toggleButton.setOnAction(e -> {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    toggleButton.setText("▶");
                } else {
                    mediaPlayer.play();
                    toggleButton.setText("⏸");
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    double total = media.getDuration().toMillis();
                    double current = newTime.toMillis();
                    progressSlider.setValue((current / total) * 100);
                }

                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalTime = media.getDuration();
                String timeText = formatDuration(currentTime) + " / " + formatDuration(totalTime);
                timeLabel.setText(timeText);
            });

            progressSlider.setOnMousePressed(e -> mediaPlayer.pause());
            progressSlider.setOnMouseReleased(e -> {
                double percent = progressSlider.getValue() / 100;
                Duration seekTo = media.getDuration().multiply(percent);
                mediaPlayer.seek(seekTo);
                mediaPlayer.play();
                toggleButton.setText("⏸");
            });

        } catch (Exception e) {
            System.out.println("Audio not loaded: " + e.getMessage());
        }

        return new HBox(10, toggleButton, progressSlider, timeLabel);
    }

    private HBox createActionButtons(Music music) {
        int userId = 1; // Hardcoded for now
        LikeService likeService = new LikeService();

        Button likeButton = new Button();
        styleButton(likeButton);

        try {
            boolean liked = likeService.isLiked(music.getId(), userId);
            likeButton.setText(liked ? "❤" : "♡");
            likeButton.setStyle(liked ? likedStyle() : unlikedStyle());

            likeButton.setOnAction(e -> {
                try {
                    if (likeService.isLiked(music.getId(), userId)) {
                        likeService.dislikeMusic(music.getId(), userId);
                        likeButton.setText("♡");
                        likeButton.setStyle(unlikedStyle());
                    } else {
                        likeService.likeMusic(music.getId(), userId);
                        likeButton.setText("❤");
                        likeButton.setStyle(likedStyle());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            likeButton.setOnMouseEntered(e -> {
                likeButton.setStyle(getHoverStyle(likeButton.getText().equals("❤")));
            });

            likeButton.setOnMouseExited(e -> {
                try {
                    boolean isLiked = likeService.isLiked(music.getId(), userId);
                    likeButton.setStyle(isLiked ? likedStyle() : unlikedStyle());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3-dot menu button
        Button moreButton = new Button("⋮");
        moreButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");

        ContextMenu contextMenu = createContextMenu(music);
        moreButton.setOnMouseClicked(e -> contextMenu.show(moreButton, e.getScreenX(), e.getScreenY()));

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(likeButton, spacer, moreButton);

        return buttonBox;
    }

    private String likedStyle() {
        return """
        -fx-background-color: #f5f5dc;
        -fx-text-fill: red;
        -fx-font-size: 16px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
    """;
    }

    private String unlikedStyle() {
        return """
        -fx-background-color: #f5f5dc;
        -fx-text-fill: black;
        -fx-font-size: 16px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
    """;
    }

    private String getHoverStyle(boolean isLiked) {
        return isLiked ? """
        -fx-background-color: #e8e1c6;
        -fx-text-fill: red;
        -fx-font-size: 16px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
    """ : """
        -fx-background-color: #e8e1c6;
        -fx-text-fill: black;
        -fx-font-size: 16px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
    """;
    }

    private ContextMenu createContextMenu(Music music) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem updateItem = new MenuItem("✏ Modifier");
        MenuItem deleteItem = new MenuItem("🗑 Supprimer");
        //MenuItem detailsItem = new MenuItem("👁 Détails");
        MenuItem downloadItem = new MenuItem("⬇ Télécharger"); // NEW download option

        // Style for all items
//        String itemStyle = """
//        -fx-background-color: #f5f5dc;
//        -fx-text-fill: #a68c6d;
//        -fx-font-size: 14px;
//        -fx-background-radius: 10;
//        -fx-border-color: transparent;
//        -fx-border-width: 1;
//        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 2, 2);
//    """;

//        updateItem.setStyle(itemStyle);
//        deleteItem.setStyle(itemStyle);
//       // detailsItem.setStyle(itemStyle);
//        downloadItem.setStyle(itemStyle); // apply style to the download too

//        contextMenu.setStyle("""
//        -fx-background-color: #f5f5dc;
//        -fx-border-color: #d3c4a3;
//        -fx-border-width: 1;
//        -fx-background-radius: 10;
//        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 2, 2);
//    """);

        // Actions
        updateItem.setOnAction(e -> openUpdateForm(music));
        deleteItem.setOnAction(e -> {
            MusicService service = new MusicService();
            try {
                service.delete(music);
                musicGrid.getChildren().clear();
                initialize(null, null);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        //detailsItem.setOnAction(e -> showMusicDetails(music));

        // 🆕 Download Action
        downloadItem.setOnAction(e -> {
            try {
                File sourceFile = new File(music.getCheminFichier());
                if (sourceFile.exists()) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save Music File");
                    fileChooser.setInitialFileName(sourceFile.getName());
                    File targetFile = fileChooser.showSaveDialog(contextMenu.getOwnerWindow());

                    if (targetFile != null) {
                        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else {
                    System.out.println("Music file does not exist: " + music.getCheminFichier());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        // Add all items
        contextMenu.getItems().addAll(updateItem, deleteItem, downloadItem);
        return contextMenu;
    }



    private VBox createRecommendationsBox(Music music) {
        VBox recommendationsBox = new VBox(10);
        recommendationsBox.setStyle("""
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-background-color: #e8e1c6;
        -fx-background-radius: 10;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 2, 2);
    """);

        // Set resizable properties
        recommendationsBox.setMinWidth(380);
        recommendationsBox.setPrefWidth(450);
        recommendationsBox.setMaxWidth(Double.MAX_VALUE);

        // Title
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Text recommendationTitle = new Text("🎧 Recommendations");
        recommendationTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleBox.getChildren().add(recommendationTitle);

        // Scrollable content area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox recommendationsContent = new VBox(8);
        recommendationsContent.setPadding(new Insets(5));

        // Load recommendations with clickable links
        ArrayList<Recommendation> recommendations = DeezerAPI.getRecommendationsByGenre(music.getGenre());

        if (recommendations.isEmpty()) {
            Text noRecText = new Text("No recommendations found for genre: " + music.getGenre());
            noRecText.setStyle("-fx-font-style: italic;");
            recommendationsContent.getChildren().add(noRecText);
        } else {
            for (Recommendation rec : recommendations) {
                Hyperlink link = new Hyperlink("🎧 " + rec.getTitle() + " by " + rec.getArtist());
                String youtubeSearchUrl = "https://www.youtube.com/results?search_query=" +
                        URLEncoder.encode(rec.getTitle() + " " + rec.getArtist(), StandardCharsets.UTF_8);

                link.setOnAction(e -> {
                    HostServices hostServices = HostServicesUtil.getHostServices();
                    if (hostServices != null) {
                        hostServices.showDocument(youtubeSearchUrl);
                    }
                });

                link.setStyle("""
                -fx-text-fill: #2a5885;
                -fx-border-color: transparent;
                -fx-underline: false;
                -fx-font-size: 14px;
                -fx-padding: 8;
            """);

                link.setOnMouseEntered(e -> link.setStyle("-fx-text-fill: #3b7dd8;" +
                        "                -fx-border-color: transparent;\n" +
                        "                -fx-underline: false;\n" +
                        "                -fx-font-size: 14px;\n" +
                        "                -fx-padding: 8;" +
                        " -fx-underline: true;"));
                link.setOnMouseExited(e -> link.setStyle("-fx-text-fill: #2a5885;\n" +
                        "                -fx-border-color: transparent;\n" +
                        "                -fx-underline: false;\n" +
                        "                -fx-font-size: 14px;\n" +
                        "                -fx-padding: 8;" +
                        " -fx-underline: false;"));

                HBox linkContainer = new HBox(link);
                linkContainer.setStyle("""
                -fx-background-color: #f7f7f7;
                -fx-background-radius: 8;
                -fx-border-color: #d3c4a3;
                -fx-border-radius: 8;
                -fx-padding: 0 8 0 8;
            """);

                recommendationsContent.getChildren().add(linkContainer);
            }
        }

        scrollPane.setContent(recommendationsContent);
        recommendationsBox.getChildren().addAll(titleBox, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return recommendationsBox;
    }

    private void displayMusicDetails(Music music) {
        musicGrid.getChildren().clear(); // Clear old content

        // Create main container with left and right sections
        HBox mainContainer = new HBox(10); // Spacing between left and right
        mainContainer.setPadding(new Insets(15));

        // Left side - Recommendations
        VBox recommendationsBox = createRecommendationsBox(music);

        // RIGHT SIDE - Music Details Card
        VBox musicDetailsBox = new VBox(10);
        musicDetailsBox.setStyle("""
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-background-color: #e8e1c6;
        -fx-background-radius: 10;
        -fx-padding: 12;
        -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 2, 2);
    """);
        musicDetailsBox.setPrefWidth(350);
        HBox.setHgrow(musicDetailsBox, Priority.ALWAYS);    // Both panels grow equally
        // Image
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(new File(music.getPhoto()).toURI().toString());
            imageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Image not loaded: " + e.getMessage());
        }
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // Labels
        Label titreLabel = new Label("🎵 " + music.getTitre());
        titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #2a2a2a;");

        Label artistLabel = new Label("👤 " + music.getArtistName());
        artistLabel.setStyle("-fx-text-fill: #444444; -fx-font-size: 16px;");

        Label genreLabel = new Label("🎼 Genre: " + music.getGenre());
        genreLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px;");

        Label dateLabel = new Label("🎼 Sortie le: " + music.getDateSortie());
        genreLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px;");

        Label descriptionLabel = new Label("📝 " + music.getDescription());
        descriptionLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 16px;");
        descriptionLabel.setWrapText(true);

        // Controls (same as before)
        VBox controlsContainer = new VBox(10);

        // Audio controls
        HBox audioBox = new HBox(10);
        Button toggleButton = new Button("▶");
        styleButton(toggleButton);

        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setPrefWidth(200);

        Label timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 11px;");

        try {
            Media media = new Media(new File(music.getCheminFichier()).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            toggleButton.setOnAction(e -> {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    toggleButton.setText("▶");
                } else {
                    mediaPlayer.play();
                    toggleButton.setText("⏸");
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    double total = media.getDuration().toMillis();
                    double current = newTime.toMillis();
                    progressSlider.setValue((current / total) * 100);
                }
                Duration currentTime = mediaPlayer.getCurrentTime();
                Duration totalTime = media.getDuration();
                String timeText = formatDuration(currentTime) + " / " + formatDuration(totalTime);
                timeLabel.setText(timeText);
            });

            progressSlider.setOnMousePressed(e -> mediaPlayer.pause());
            progressSlider.setOnMouseReleased(e -> {
                double percent = progressSlider.getValue() / 100;
                Duration seekTo = media.getDuration().multiply(percent);
                mediaPlayer.seek(seekTo);
                mediaPlayer.play();
                toggleButton.setText("⏸");
            });
        } catch (Exception e) {
            System.out.println("Audio not loaded: " + e.getMessage());
        }
        audioBox.getChildren().addAll(toggleButton, progressSlider, timeLabel);

        // 2. Playlist controls (same as createMusicCard)
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
        styleComboBox(playlistComboBox);

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

        // 3. Action buttons (same as createMusicCard)
        int userId = 1;
        LikeService likeService = new LikeService();
        Button likeButton = new Button();
        styleButton(likeButton);
        try {
            boolean liked = likeService.isLiked(music.getId(), userId);
            likeButton.setText(liked ? "❤" : "♡");
            likeButton.setStyle(liked ? likedStyle() : unlikedStyle());

            likeButton.setOnAction(e -> {
                try {
                    if (likeService.isLiked(music.getId(), userId)) {
                        likeService.dislikeMusic(music.getId(), userId);
                        likeButton.setText("♡");
                        likeButton.setStyle(unlikedStyle());
                    } else {
                        likeService.likeMusic(music.getId(), userId);
                        likeButton.setText("❤");
                        likeButton.setStyle(likedStyle());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            likeButton.setOnMouseEntered(e -> {
                likeButton.setStyle(getHoverStyle(likeButton.getText().equals("❤")));
            });

            likeButton.setOnMouseExited(e -> {
                try {
                    boolean isLiked = likeService.isLiked(music.getId(), userId);
                    likeButton.setStyle(isLiked ? likedStyle() : unlikedStyle());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button moreButton = new Button("⋮");
        moreButton.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        ContextMenu contextMenu = new ContextMenu();
        MenuItem updateItem = new MenuItem("✏ Modifier");
        MenuItem deleteItem = new MenuItem("🗑 Supprimer");
        MenuItem detailsItem = new MenuItem("👁 Détails");
        contextMenu.getItems().addAll(updateItem, deleteItem, detailsItem);
        moreButton.setOnMouseClicked(e -> contextMenu.show(moreButton, e.getScreenX(), e.getScreenY()));

        HBox buttonBox = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonBox.getChildren().addAll(likeButton, spacer, moreButton);

        // Add all controls to container
        controlsContainer.getChildren().addAll(audioBox, playlistBox, buttonBox);

        // Final assembly
        musicDetailsBox.getChildren().addAll(
                imageView,
                titreLabel,
                artistLabel,
                genreLabel,
                descriptionLabel,
                dateLabel,
                controlsContainer
        );

        // Add both sections to main container
        mainContainer.getChildren().addAll(recommendationsBox, musicDetailsBox);

        // Add to grid with constraints
        musicGrid.getChildren().add(mainContainer);
        AnchorPane.setTopAnchor(mainContainer, 10.0);
        AnchorPane.setBottomAnchor(mainContainer, 10.0);
        AnchorPane.setLeftAnchor(mainContainer, 10.0);
        AnchorPane.setRightAnchor(mainContainer, 10.0);
    }

    // Helper method for styling general buttons
    private void styleButton(Button button) {
        button.setStyle("""
        -fx-background-color: #f5f5dc; /* Beige */
        -fx-text-fill: #4a4a4a;        /* Soft brown */
        -fx-font-size: 13px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-padding: 6 12;
    """);

        button.setOnMouseEntered(e -> button.setStyle("""
        -fx-background-color: #e8e1c6; /* Slightly darker beige */
        -fx-text-fill: #4a4a4a;
        -fx-font-size: 13px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-padding: 6 12;
    """));

        button.setOnMouseExited(e -> button.setStyle("""
        -fx-background-color: #f5f5dc;
        -fx-text-fill: #4a4a4a;
        -fx-font-size: 13px;
        -fx-cursor: hand;
        -fx-background-radius: 10;
        -fx-border-color: #d3c4a3;
        -fx-border-radius: 10;
        -fx-padding: 6 12;
    """));
    }


    private void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setStyle("""
        -fx-background-color: #f5f5dc;
        -fx-font-size: 10px;
        -fx-text-fill: #4a4a4a;
        -fx-padding: 6 12;
        -fx-background-radius: 10;
        -fx-border-color: #d3d3c3;
        -fx-border-radius: 10;
        -fx-cursor: hand;
    """);

        comboBox.setOnMouseEntered(e -> comboBox.setStyle("""
        -fx-background-color: #e8e1c6;
        -fx-font-size: 10px;
        -fx-text-fill: #4a4a4a;
        -fx-padding: 6 12;
        -fx-background-radius: 10;
        -fx-border-color: #d3d3c3;
        -fx-border-radius: 10;
        -fx-cursor: hand;
    """));

        comboBox.setOnMouseExited(e -> comboBox.setStyle("""
        -fx-background-color: #f5f5dc;
        -fx-font-size: 10px;
        -fx-text-fill: #4a4a4a;
        -fx-padding: 6 12;
        -fx-background-radius: 10;
        -fx-border-color: #d3d3c3;
        -fx-border-radius: 10;
        -fx-cursor: hand;
    """));
    }



    private String formatDuration(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    private void openUpdateForm(Music music) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UpdateMusic.fxml"));
            AnchorPane updateMusicPane = loader.load();

            UpdateMusicController controller = loader.getController();
            controller.setMusic(music); // pass the selected music to update

            // Replace rightPane content (same behavior as add)
            musicGrid.getChildren().clear();
            musicGrid.getChildren().add(updateMusicPane);

            AnchorPane.setTopAnchor(updateMusicPane, 0.0);
            AnchorPane.setBottomAnchor(updateMusicPane, 0.0);
            AnchorPane.setLeftAnchor(updateMusicPane, 0.0);
            AnchorPane.setRightAnchor(updateMusicPane, 0.0);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    @FXML
    private void handleAddMusicButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterMusic.fxml"));
            AnchorPane ajoutMusicPane = loader.load();

            // Replace the musicGrid (or rightPane) with the AjoutMusic pane
            musicGrid.getChildren().clear();
            musicGrid.getChildren().add(ajoutMusicPane);

            AnchorPane.setTopAnchor(ajoutMusicPane, 0.0);
            AnchorPane.setBottomAnchor(ajoutMusicPane, 0.0);
            AnchorPane.setLeftAnchor(ajoutMusicPane, 0.0);
            AnchorPane.setRightAnchor(ajoutMusicPane, 0.0);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void afficherMusique() {
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
        // 💡 Force grid size reset to match parent
        GridPane.setHgrow(musicGrid, Priority.ALWAYS);
        AnchorPane.setTopAnchor(musicGrid, 0.0);
        AnchorPane.setBottomAnchor(musicGrid, 0.0);
        AnchorPane.setLeftAnchor(musicGrid, 0.0);
        AnchorPane.setRightAnchor(musicGrid, 0.0);

    }

    @FXML
    private void handleGetLyrics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lyrics.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Lyrics Viewer");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMusicDetails(Music music) {
        try {
            // VBox container to hold the details
            VBox detailBox = new VBox(15);
            detailBox.setPadding(new Insets(20));
            detailBox.setStyle("-fx-background-color: #f5f5dc; " +   // beige background
                    "-fx-border-color: #d3c4a3; " +         // soft brown border
                    "-fx-border-width: 1; " +               // border width
                    "-fx-border-radius: 10; " +             // rounded corners
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 2, 2);");

            // Title label
            Label titre = new Label("🎵 Titre : " + music.getTitre());
            titre.setFont(Font.font("Arial", 16));
            titre.setTextFill(Color.DARKSLATEBLUE);
            titre.setStyle("-fx-font-weight: bold;");

            // Artist label
            Label artist = new Label("👤 Artiste : " + music.getArtistName());
            artist.setFont(Font.font("Arial", 14));
            artist.setTextFill(Color.DARKSLATEGRAY);

            // Genre label
            Label genre = new Label("🎼 Genre : " + music.getGenre());
            genre.setFont(Font.font("Arial", 14));
            genre.setTextFill(Color.DARKSLATEGRAY);

            // Description label
            Label description = new Label("📝 Description : " + music.getDescription());
            description.setWrapText(true);
            description.setFont(Font.font("Arial", 13));
            description.setTextFill(Color.GRAY);

            // Date label
            Label date = new Label("📅 Date de sortie : " + music.getDateSortie());
            date.setFont(Font.font("Arial", 13));
            date.setTextFill(Color.DIMGRAY);

//            // File path label
//            Label chemin = new Label("📁 Fichier : " + music.getCheminFichier());
//            chemin.setFont(Font.font("Arial", 12));
//            chemin.setTextFill(Color.GRAY);

            // Image display (optional, if photo exists)
            ImageView photo = new ImageView();
            photo.setFitWidth(150);
            photo.setFitHeight(150);
            try {
                // Assuming the image path is correct, you can use the file path from music
                Image img = new Image(new File(music.getPhoto()).toURI().toString());
                photo.setImage(img);
            } catch (Exception ex) {
                System.out.println("Image not found: " + ex.getMessage());
            }

            // Adding all the labels and the image to the VBox container
            detailBox.getChildren().addAll(titre, artist, genre, description, date, photo);

            // Create a new window (Stage) to display the music details
            Stage stage = new Stage();
            stage.setTitle("Détails de la Musique");
            stage.setScene(new Scene(detailBox, 400, 500));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
