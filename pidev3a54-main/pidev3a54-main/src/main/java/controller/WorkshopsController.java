package controller;
import javafx.scene.control.Button;
import Models.Workshops;
import Services.WorkshopService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Node;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;




public class WorkshopsController {

    @FXML
    private VBox workshopsContainer;
    private final WorkshopService workshopService = new WorkshopService();
    private List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private MediaPlayer currentlyPlaying = null;

    @FXML
    public void initialize() {
        try {
            loadWorkshops();
        } catch (SQLException e) {
            showError("Erreur de base de données : " + e.getMessage());
        }
    }

    private void loadWorkshops() throws SQLException {
        List<Workshops> workshops = workshopService.getAll();
        workshopsContainer.getChildren().clear();
        mediaPlayers.clear();

        workshopsContainer.setStyle("-fx-background-color: white; -fx-padding: 20;");

        // Ajout de l'en-tête
        addHeaderSection();

        for (int i = 0; i < workshops.size(); i++) {
            Node workshopCard = createWorkshopCard(workshops.get(i), i);
            workshopsContainer.getChildren().add(workshopCard);
        }
    }
    private Node createWorkshopCard(Workshops workshop, int index) {
        HBox card = new HBox(40);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));

        // Video content
        Node videoContent = createVideoContent(workshop);
        // Description content
        Node descriptionContent = createDescriptionContent(workshop);

        // Alterner l'ordre en fonction de l'index
        if (index % 2 == 0) {
            // Pair: image à gauche, texte à droite
            card.getChildren().addAll(videoContent, descriptionContent);
        } else {
            // Impair: texte à gauche, image à droite
            card.getChildren().addAll(descriptionContent, videoContent);
        }

        VBox container = new VBox(card);
        container.setPadding(new Insets(0, 0, 40, 0));
        container.setStyle("-fx-background-color: white;");
        return container;
    }
    private Node createVideoContent(Workshops workshop) {
        VBox videoBox = new VBox();
        videoBox.setAlignment(Pos.CENTER);
        videoBox.setPrefWidth(400); // Largeur fixe pour l'alignement
        videoBox.setMinWidth(400);  // Empêche le rétrécissement
        videoBox.setMaxWidth(400);  // Empêche l'agrandissement

        if (workshop.getVideo() != null && !workshop.getVideo().isEmpty()) {
            Node videoNode = createVideoNode(workshop.getVideo());
            videoBox.getChildren().add(videoNode);
        }

        return videoBox;
    }

    private Node createDescriptionContent(Workshops workshop) {
        VBox descBox = new VBox(15);
        descBox.setAlignment(Pos.TOP_LEFT);
        descBox.setPrefWidth(400);
        descBox.setPadding(new Insets(20));
        descBox.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 3);");

        Label titleLabel = new Label(workshop.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #333;");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        TextFlow descriptionFlow = new TextFlow();
        Text descriptionText = new Text(workshop.getDescription());
        descriptionText.setStyle("-fx-font-size: 14; -fx-fill: #555;");
        descriptionFlow.getChildren().add(descriptionText);
        descriptionFlow.setMaxWidth(350);

        VBox detailsBox = new VBox(5);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));

        // Ajouter le bouton de téléchargement si une vidéo existe
        if (workshop.getVideo() != null && !workshop.getVideo().isEmpty()) {
            Button downloadBtn = new Button("Télécharger la vidéo");
            downloadBtn.setStyle("-fx-background-color: #5a4a3a; -fx-text-fill: white;");
            downloadBtn.setOnAction(e -> downloadVideo(workshop.getVideo()));
            detailsBox.getChildren().add(downloadBtn);
        }

        descBox.getChildren().addAll(titleLabel, descriptionFlow, detailsBox);
        return descBox;
    }
    private Node createVideoNode(String videoPath) {
        try {
            MediaView videoView = tryCreateVideoView(videoPath);
            if (videoView != null) {
                VBox videoContainer = new VBox(5); // Utiliser VBox au lieu de StackPane
                videoContainer.setAlignment(Pos.CENTER);

                // Conteneur pour la vidéo avec le bouton play
                StackPane videoPlayerContainer = new StackPane();
                videoPlayerContainer.setStyle("-fx-background-color: black;");
                videoPlayerContainer.getChildren().add(videoView);

                // Play button indicator
                Polygon playIndicator = createPlayIndicator();
                StackPane.setAlignment(playIndicator, Pos.CENTER);
                videoPlayerContainer.getChildren().add(playIndicator);

                // Bouton de téléchargement
//                Button downloadBtn = new Button("Télécharger la vidéo");
//                downloadBtn.setStyle("-fx-background-color: #5a4a3a; -fx-text-fill: white;");
//                downloadBtn.setOnAction(e -> downloadVideo(videoPath));

                // Ajouter les éléments au VBox
                videoContainer.getChildren().addAll(videoPlayerContainer);

                videoPlayerContainer.setOnMouseClicked(event -> {
                    MediaPlayer player = videoView.getMediaPlayer();
                    handleVideoClick(player, playIndicator);
                });

                return videoContainer;
            }
        } catch (Exception e) {
            System.err.println("Error creating video view: " + e.getMessage());
        }
        return createVideoLinkWithDebug(videoPath);
    }
    private void downloadVideo(String videoPath) {
        try {
            File sourceFile = new File(videoPath);
            if (!sourceFile.exists()) {
                showError("Le fichier vidéo n'existe pas : " + videoPath);
                return;
            }

            // Créer un FileChooser pour sélectionner l'emplacement de sauvegarde
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la vidéo");
            fileChooser.setInitialFileName(sourceFile.getName());

            // Filtrer pour n'afficher que les extensions vidéo
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                    "Fichiers MP4 (*.mp4)", "*.mp4");
            fileChooser.getExtensionFilters().add(extFilter);

            // Afficher la boîte de dialogue d'enregistrement
            Stage stage = (Stage) workshopsContainer.getScene().getWindow();
            File destinationFile = fileChooser.showSaveDialog(stage);

            if (destinationFile != null) {
                // Copier le fichier vers l'emplacement sélectionné
                Files.copy(
                        sourceFile.toPath(),
                        destinationFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Afficher un message de confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Téléchargement réussi");
                alert.setHeaderText(null);
                alert.setContentText("La vidéo a été enregistrée avec succès dans :\n" +
                        destinationFile.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            showError("Erreur lors du téléchargement de la vidéo : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Polygon createPlayIndicator() {
        Polygon triangle = new Polygon(
                0.0, 0.0,
                20.0, 10.0,
                0.0, 20.0
        );
        triangle.setFill(Color.WHITE);
        triangle.setOpacity(0.8);
        triangle.setStroke(Color.WHITE);
        return triangle;
    }

    private void handleVideoClick(MediaPlayer player, Polygon indicator) {
        if (currentlyPlaying != null && currentlyPlaying != player) {
            currentlyPlaying.pause();
            indicator.getPoints().setAll(0.0, 0.0, 20.0, 10.0, 0.0, 20.0);
        }

        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            currentlyPlaying = null;
            indicator.getPoints().setAll(0.0, 0.0, 20.0, 10.0, 0.0, 20.0);
        } else {
            player.play();
            currentlyPlaying = player;
            indicator.getPoints().clear(); // Remove play button when playing
        }
    }

    private MediaView tryCreateVideoView(String videoPath) {
        try {
            File file = new File(videoPath);
            if (!file.exists() || file.length() == 0 || !isSupportedVideoFormat(videoPath)) {
                return null;
            }

            Media media = new Media(file.toURI().toString());
            MediaPlayer player = new MediaPlayer(media);
            mediaPlayers.add(player);

            MediaView mediaView = new MediaView(player);
            mediaView.setFitWidth(400);
            mediaView.setFitHeight(225);
            player.setAutoPlay(false);

            return mediaView;
        } catch (Exception e) {
            System.err.println("Error loading video: " + e.getMessage());
            return null;
        }
    }

    private Hyperlink createVideoLinkWithDebug(String videoPath) {
        Hyperlink link = new Hyperlink("View Video (Details)");
        link.setOnAction(e -> showVideoDebugInfo(videoPath));
        return link;
    }

    private void showVideoDebugInfo(String videoPath) {
        File file = new File(videoPath);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Video Details");
        alert.setHeaderText(null);
        alert.setContentText("Path: " + videoPath + "\nExists: " + file.exists());
        alert.showAndWait();
    }

    private boolean isSupportedVideoFormat(String path) {
        String[] formats = {".mp4", ".m4v", ".flv", ".f4v"};
        String lowerPath = path.toLowerCase();
        for (String format : formats) {
            if (lowerPath.endsWith(format)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void addHeaderSection() {
        VBox headerBox = new VBox(20);
        headerBox.setAlignment(Pos.TOP_CENTER);
        headerBox.setPadding(new Insets(0, 0, 40, 0));

        // Titre principal avec icône (vous pouvez remplacer par une vraie icône)
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER);

        // Icône (remplacer par une ImageView si vous avez une image)
        Polygon icon = new Polygon(
                0.0, 10.0,
                10.0, 0.0,
                20.0, 10.0,
                10.0, 20.0
        );
        icon.setFill(Color.web("#5a4a3a"));

        Label mainTitle = new Label("COURS DE POTERIE EN LIGNE");
        mainTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #5a4a3a;");

        titleBox.getChildren().addAll(icon, mainTitle);

        // Sous-titre
        Label subtitle = new Label("AVEC L'EQUIPE ARTISFERA");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b5b4b;");

        // Description
        Text description = new Text(
                "Parce que j'aime partager ma passion de la céramique, mais que je sais aussi qu'il est si agréable d'être chez soi, " +
                        "je vous propose des cours de poterie « nomade », en faisant venir l'atelier jusqu'à vous !\n\n" +
                        "Que ce soit seul(e) ou en petit groupe, je donne des cours particuliers de tournage à domicile grâce à un tour de potier transportable."
        );
        description.setStyle("-fx-font-size: 14px; -fx-fill: #5a4a3a;");
        description.setWrappingWidth(800);



        headerBox.getChildren().addAll(titleBox, subtitle, description);
        workshopsContainer.getChildren().add(headerBox);
    }
}