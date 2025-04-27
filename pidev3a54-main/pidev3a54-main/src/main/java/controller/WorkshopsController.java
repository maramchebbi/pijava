package controller;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import Models.Workshops;
import Services.WorkshopService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.scene.control.DialogPane;

public class WorkshopsController {


    @FXML
    private VBox workshopsContainer;
    private final WorkshopService workshopService = new WorkshopService();
    private List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private MediaPlayer currentlyPlaying = null;

    // Couleurs principales
    private final String primaryColor = "#8A715E"; // Brown
    private final String secondaryColor = "#D6C6B1"; // Light beige
    private final String accentColor = "#5D4C3C"; // Dark brown
    private final String textColor = "#3A3A3A"; // Dark gray
    private final String backgroundColor = "#FFFFFF"; // White

    // Images pour les icônes (à adapter selon vos ressources)
    private static final String PLAY_ICON_PATH = "/path/to/play_icon.png";
    private static final String DOWNLOAD_ICON_PATH = "/path/to/download_icon.png";
    private static final String POTTERY_ICON_PATH = "/images/icon.png";

    @FXML
    public void initialize() {
        try {
            // Configuration des boutons de navigation


            // Code existant pour charger les ateliers
            loadWorkshops();
        } catch (SQLException e) {
            showError("Erreur de base de données : " + e.getMessage());
        }

    }


    private void loadWorkshops() throws SQLException {
        List<Workshops> workshops = workshopService.getAll();
        workshopsContainer.getChildren().clear();
        mediaPlayers.clear();

        // Style de base pour le conteneur principal
        workshopsContainer.setStyle("-fx-background-color: " + backgroundColor + ";");
        workshopsContainer.setSpacing(50); // Espacement plus important entre les éléments

        // Ajout de l'en-tête
        addHeaderSection();

        // Ajouter un séparateur élégant
        addSeparator();

        // Afficher tous les ateliers
        for (int i = 0; i < workshops.size(); i++) {
            Node workshopCard = createWorkshopCard(workshops.get(i), i);

            // Animation d'apparition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), workshopCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            workshopsContainer.getChildren().add(workshopCard);

            // Ajouter un séparateur entre les cartes (sauf pour la dernière)
            if (i < workshops.size() - 1) {
                addSeparator();
            }
        }
    }

    private void addSeparator() {
        Line separator = new Line();
        separator.setStartX(0);
        separator.setEndX(800);
        separator.setStroke(Color.web(secondaryColor));
        separator.setStrokeWidth(1);
        separator.setOpacity(0.8);

        HBox separatorBox = new HBox(separator);
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setPadding(new Insets(20, 0, 20, 0));

        workshopsContainer.getChildren().add(separatorBox);
    }

    private Node createWorkshopCard(Workshops workshop, int index) {
        HBox card = new HBox(50); // Augmenter l'espacement entre les éléments
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));

        // Contenu vidéo
        Node videoContent = createVideoContent(workshop);

        // Contenu de description
        Node descriptionContent = createDescriptionContent(workshop);

        // Alterner l'ordre en fonction de l'index
        if (index % 2 == 0) {
            // Pair: vidéo à gauche, texte à droite
            card.getChildren().addAll(videoContent, descriptionContent);
        } else {
            // Impair: texte à gauche, vidéo à droite
            card.getChildren().addAll(descriptionContent, videoContent);
        }

        // Appliquer un effet shadow subtil à la carte
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));

        VBox container = new VBox(card);
        container.setEffect(shadow);
        container.setPadding(new Insets(0));
        container.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-background-radius: 10;");

        return container;
    }

    private Node createVideoContent(Workshops workshop) {
        VBox videoBox = new VBox(15);
        videoBox.setAlignment(Pos.CENTER);
        videoBox.setPrefWidth(450); // Légèrement plus large
        videoBox.setMinWidth(450);
        videoBox.setMaxWidth(450);

        // Créer le conteneur pour la vidéo avec un style amélioré
        StackPane videoContainer = new StackPane();
        videoContainer.setStyle("-fx-background-color: " + secondaryColor + "22; " + // 22 pour l'opacité
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + secondaryColor + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12;");

        if (workshop.getVideo() != null && !workshop.getVideo().isEmpty()) {
            Node videoNode = createVideoNode(workshop.getVideo());
            if (videoNode != null) {
                videoContainer.getChildren().add(videoNode);
                videoBox.getChildren().add(videoContainer);

                // Bouton de téléchargement sous la vidéo
                Button downloadBtn = createStylizedButton("Télécharger la vidéo", DOWNLOAD_ICON_PATH);
                downloadBtn.setOnAction(e -> downloadVideo(workshop.getVideo()));

                HBox buttonContainer = new HBox(downloadBtn);
                buttonContainer.setAlignment(Pos.CENTER);
                buttonContainer.setPadding(new Insets(10, 0, 0, 0));

                videoBox.getChildren().add(buttonContainer);
            }
        } else {
            // Afficher une image placeholder si pas de vidéo
            Label noVideoLabel = new Label("Vidéo non disponible");
            noVideoLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-style: italic;");
            videoContainer.getChildren().add(noVideoLabel);
            videoBox.getChildren().add(videoContainer);
        }

        return videoBox;
    }

    private Button createStylizedButton(String text, String iconPath) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + primaryColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5; " +
                "-fx-padding: 8 15 8 15;");

        // Ajouter une icône si le chemin est valide
        try {
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                ImageView icon = new ImageView(new Image(new FileInputStream(iconFile)));
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                button.setGraphic(icon);
                button.setGraphicTextGap(8);
            }
        } catch (Exception e) {
            // Ignorer si l'icône n'est pas disponible
            System.out.println("Icône non disponible: " + iconPath);
        }

        // Effets au survol
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: " + accentColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8 15 8 15;"));

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: " + primaryColor + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 8 15 8 15;"));

        return button;
    }

    private Node createDescriptionContent(Workshops workshop) {
        VBox descBox = new VBox(15);
        descBox.setAlignment(Pos.TOP_LEFT);
        descBox.setPrefWidth(450);
        descBox.setPadding(new Insets(25));
        descBox.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-border-color: " + secondaryColor + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;");

        // Titre de l'atelier avec style amélioré
        Label titleLabel = new Label(workshop.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        titleLabel.setStyle("-fx-text-fill: " + accentColor + ";");

        // Ligne de séparation stylisée sous le titre
        Line titleSeparator = new Line(0, 0, 100, 0);
        titleSeparator.setStroke(Color.web(primaryColor));
        titleSeparator.setStrokeWidth(2);
        titleSeparator.setOpacity(0.7);

        // Description avec style amélioré
        TextFlow descriptionFlow = new TextFlow();
        Text descriptionText = new Text(workshop.getDescription());
        descriptionText.setFont(Font.font("System", FontWeight.NORMAL, 15));
        descriptionText.setStyle("-fx-fill: " + textColor + ";");
        descriptionFlow.getChildren().add(descriptionText);
        descriptionFlow.setMaxWidth(400);
        descriptionFlow.setLineSpacing(5); // Améliorer l'espacement des lignes

        // Ajouter tous les éléments à la boîte de description
        descBox.getChildren().addAll(titleLabel, titleSeparator, descriptionFlow);

        return descBox;
    }

    private Node createVideoNode(String videoPath) {
        try {
            MediaView videoView = tryCreateVideoView(videoPath);
            if (videoView != null) {
                // Créer un conteneur pour la vidéo avec le bouton play
                StackPane videoPlayerContainer = new StackPane();
                videoPlayerContainer.setMinHeight(250);
                videoPlayerContainer.setMaxHeight(250);
                videoPlayerContainer.setMinWidth(450);
                videoPlayerContainer.setMaxWidth(450);
                videoPlayerContainer.setStyle("-fx-background-color: #000000;");
                videoPlayerContainer.getChildren().add(videoView);

                // Créer un bouton play élégant
                Circle playButtonBg = new Circle(30);
                playButtonBg.setFill(Color.web(primaryColor + "99")); // Avec transparence

                // Triangle de lecture
                Node playButton = createPlayButton();

                StackPane playButtonContainer = new StackPane(playButtonBg, playButton);
                videoPlayerContainer.getChildren().add(playButtonContainer);

                // Gérer le clic sur la vidéo
                videoPlayerContainer.setOnMouseClicked(event -> {
                    MediaPlayer player = videoView.getMediaPlayer();
                    handleVideoClick(player, playButtonContainer);
                });

                return videoPlayerContainer;
            }
        } catch (Exception e) {
            System.err.println("Error creating video view: " + e.getMessage());
        }
        return createVideoLinkWithDebug(videoPath);
    }

    private Node createPlayButton() {
        // Créer un triangle pour le bouton play
        javafx.scene.shape.Polygon triangle = new javafx.scene.shape.Polygon(
                -10.0, -15.0,
                20.0, 0.0,
                -10.0, 15.0
        );
        triangle.setFill(Color.WHITE);

        return triangle;
    }

    private void handleVideoClick(MediaPlayer player, Node playButton) {
        if (currentlyPlaying != null && currentlyPlaying != player) {
            currentlyPlaying.pause();
            playButton.setVisible(true);
        }

        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            currentlyPlaying = null;
            playButton.setVisible(true);
        } else {
            player.play();
            currentlyPlaying = player;
            playButton.setVisible(false); // Cacher le bouton pendant la lecture
        }
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

                // Afficher un message de confirmation stylisé
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Téléchargement réussi");
                alert.setHeaderText(null);
                alert.setContentText("La vidéo a été enregistrée avec succès dans :\n" +
                        destinationFile.getAbsolutePath());

                // Styliser l'alerte si possible
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: " + backgroundColor + ";" +
                        "-fx-border-color: " + secondaryColor + ";" +
                        "-fx-border-width: 2px;");

                alert.showAndWait();
            }
        } catch (Exception e) {
            showError("Erreur lors du téléchargement de la vidéo : " + e.getMessage());
            e.printStackTrace();
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
            mediaView.setFitWidth(450);
            mediaView.setFitHeight(250);
            player.setAutoPlay(false);

            // Effet de bordure arrondie pour la vidéo
            // Note: Ceci peut être limité par JavaFX, peut nécessiter une approche différente
            mediaView.setStyle("-fx-background-radius: 12;");

            return mediaView;
        } catch (Exception e) {
            System.err.println("Error loading video: " + e.getMessage());
            return null;
        }
    }

    private Hyperlink createVideoLinkWithDebug(String videoPath) {
        Hyperlink link = new Hyperlink("Voir les détails de la vidéo");
        link.setStyle("-fx-text-fill: " + primaryColor + "; -fx-font-weight: bold;");
        link.setOnAction(e -> showVideoDebugInfo(videoPath));
        return link;
    }

    private void showVideoDebugInfo(String videoPath) {
        File file = new File(videoPath);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la vidéo");
        alert.setHeaderText(null);
        alert.setContentText("Chemin: " + videoPath + "\nExiste: " + file.exists());

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + backgroundColor + ";" +
                "-fx-border-color: " + secondaryColor + ";" +
                "-fx-border-width: 2px;");

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
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styliser l'alerte d'erreur
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + backgroundColor + ";" +
                "-fx-border-color: #dd3333;" +
                "-fx-border-width: 2px;");

        alert.showAndWait();
    }

    private void addHeaderSection() {
        VBox headerBox = new VBox(25);
        headerBox.setAlignment(Pos.TOP_CENTER);
        headerBox.setPadding(new Insets(20, 0, 40, 0));
        headerBox.setMaxWidth(900);

        // Ajouter une image d'arrière-plan ou un élément décoratif
        try {
            ImageView logo = new ImageView(new Image(new FileInputStream(POTTERY_ICON_PATH)));
            logo.setFitHeight(60);
            logo.setFitWidth(60);
            logo.setOpacity(0.9);

            // Titre principal
            Label mainTitle = new Label("COURS DE POTERIE EN LIGNE");
            mainTitle.setFont(Font.font("System", FontWeight.BOLD, 32));
            mainTitle.setStyle("-fx-text-fill: " + accentColor + ";");

            HBox titleContainer = new HBox(20, logo, mainTitle);
            titleContainer.setAlignment(Pos.CENTER);

            // Sous-titre
            Label subtitle = new Label("AVEC L'ÉQUIPE ARTISFERA");
            subtitle.setFont(Font.font("System", FontWeight.NORMAL, 18));
            subtitle.setStyle("-fx-text-fill: " + primaryColor + ";");

            // Ligne décorative
            Line decorativeLine = new Line(0, 0, 400, 0);
            decorativeLine.setStroke(Color.web(secondaryColor));
            decorativeLine.setStrokeWidth(2);

            // Description avec style amélioré
            TextFlow descriptionFlow = new TextFlow();
            Text descriptionText = new Text(
                    "Parce que j'aime partager ma passion de la céramique, mais que je sais aussi qu'il est si agréable d'être chez soi, " +
                            "je vous propose des cours de poterie « nomade », en faisant venir l'atelier jusqu'à vous !\n\n" +
                            "Que ce soit seul(e) ou en petit groupe, je donne des cours particuliers de tournage à domicile grâce à un tour de potier transportable."
            );
            descriptionText.setFont(Font.font("System", FontWeight.NORMAL, 16));
            descriptionText.setStyle("-fx-fill: " + textColor + ";");
            descriptionFlow.getChildren().add(descriptionText);
            descriptionFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            descriptionFlow.setLineSpacing(8);
            descriptionFlow.setMaxWidth(800);

            // Ajout d'une bordure décorative autour de la description
            VBox descriptionContainer = new VBox(descriptionFlow);
            descriptionContainer.setPadding(new Insets(25));
            descriptionContainer.setStyle("-fx-background-color: " + secondaryColor + "22; " + // Légère opacité
                    "-fx-border-color: " + secondaryColor + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;");

            headerBox.getChildren().addAll(titleContainer, subtitle, decorativeLine, descriptionContainer);

        } catch (Exception e) {
            // Si l'image n'est pas disponible, utiliser le titre simple
            System.err.println("Cannot load logo: " + e.getMessage());

            // Titre principal alternatif sans logo
            Label mainTitle = new Label("COURS DE POTERIE EN LIGNE");
            mainTitle.setFont(Font.font("System", FontWeight.BOLD, 32));
            mainTitle.setStyle("-fx-text-fill: " + accentColor + ";");

            // Sous-titre
            Label subtitle = new Label("AVEC L'ÉQUIPE ARTISFERA");
            subtitle.setFont(Font.font("System", FontWeight.NORMAL, 18));
            subtitle.setStyle("-fx-text-fill: " + primaryColor + ";");

            // Description
            TextFlow descriptionFlow = new TextFlow();
            Text descriptionText = new Text(
                    "Parce que j'aime partager ma passion de la céramique, mais que je sais aussi qu'il est si agréable d'être chez soi, " +
                            "je vous propose des cours de poterie « nomade », en faisant venir l'atelier jusqu'à vous !\n\n" +
                            "Que ce soit seul(e) ou en petit groupe, je donne des cours particuliers de tournage à domicile grâce à un tour de potier transportable."
            );
            descriptionText.setFont(Font.font("System", FontWeight.NORMAL, 16));
            descriptionText.setStyle("-fx-fill: " + textColor + ";");
            descriptionFlow.getChildren().add(descriptionText);
            descriptionFlow.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            descriptionFlow.setLineSpacing(8);
            descriptionFlow.setMaxWidth(800);

            headerBox.getChildren().addAll(mainTitle, subtitle, descriptionFlow);
        }

        workshopsContainer.getChildren().add(headerBox);
    }
    @FXML
    private void goToHome(ActionEvent event) {
        try {
            // Arrêter tous les lecteurs media en cours si nécessaire
            stopAllMediaPlayers();

            // Charger le fichier FXML pour la page d'accueil
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/show.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle et définir la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCollections(ActionEvent event) {
        try {
            // Arrêter tous les lecteurs media en cours si nécessaire
            stopAllMediaPlayers();

            // Charger le fichier FXML pour la page des collections
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Collections.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle et définir la nouvelle scène
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // Méthode d'aide pour arrêter tous les lecteurs média
    private void stopAllMediaPlayers() {
        for (MediaPlayer player : mediaPlayers) {
            if (player != null) {
                player.stop();
            }
        }
        currentlyPlaying = null;
    }
}