package service;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;
import utils.EmailValidator;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Service d'authentification Facebook simple
 * Cette implémentation reproduit l'apparence de l'interface de connexion Facebook
 * avec uniquement email et mot de passe
 */
public class FacebookSimpleAuthService {

    private final UserService userService;

    public FacebookSimpleAuthService() {
        this.userService = new UserService();
    }

    /**
     * Démarre le processus d'authentification Facebook simulée
     * @return CompletableFuture<User> qui sera complété avec l'utilisateur authentifié
     */
    public CompletableFuture<User> startFacebookAuth() {
        CompletableFuture<User> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            try {
                // Créer une nouvelle fenêtre modale
                Stage loginStage = new Stage();
                loginStage.initModality(Modality.APPLICATION_MODAL);
                loginStage.setTitle("Connexion avec Facebook");
                loginStage.setResizable(false);

                // Créer le conteneur principal
                BorderPane root = new BorderPane();

                // Créer la barre de navigation supérieure (header)
                HBox header = createFacebookHeader();
                root.setTop(header);

                // Créer le contenu principal
                VBox mainContent = new VBox(20);
                mainContent.setPadding(new Insets(20, 30, 20, 30));
                mainContent.setAlignment(Pos.CENTER);
                mainContent.setStyle("-fx-background-color: #f0f2f5;");

                // Section de connexion
                VBox loginSection = new VBox(15);
                loginSection.setAlignment(Pos.CENTER);
                loginSection.setMaxWidth(400);

                // Logo Facebook bleu
                Text facebookText = new Text("facebook");
                facebookText.setFont(Font.font("Arial", FontWeight.BOLD, 40));
                facebookText.setFill(Color.web("#1877f2"));

                // Phrase d'accroche
                Text taglineText = new Text("Facebook vous aide à vous connecter et à partager avec les personnes qui font partie de votre vie.");
                taglineText.setWrappingWidth(400);
                taglineText.setFont(Font.font("Arial", 16));
                taglineText.setFill(Color.web("#1c1e21"));

                loginSection.getChildren().addAll(facebookText, taglineText);

                // Formulaire de connexion (carte blanche)
                VBox formCard = new VBox(15);
                formCard.setAlignment(Pos.CENTER);
                formCard.setPadding(new Insets(15));
                formCard.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 1);");
                formCard.setMaxWidth(400);

                // Champs de formulaire
                TextField emailField = new TextField();
                emailField.setPromptText("Adresse e-mail ou numéro de téléphone");
                emailField.setPrefHeight(50);
                emailField.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #dddfe2; -fx-border-width: 1px;");

                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Mot de passe");
                passwordField.setPrefHeight(50);
                passwordField.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #dddfe2; -fx-border-width: 1px;");

                // Label d'erreur
                Label errorLabel = new Label();
                errorLabel.setTextFill(Color.RED);
                errorLabel.setWrapText(true);
                errorLabel.setMaxWidth(380);

                // Bouton de connexion
                Button loginButton = new Button("Se connecter");
                loginButton.setPrefWidth(400);
                loginButton.setPrefHeight(50);
                loginButton.setStyle("-fx-background-color: #1877f2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 6px;");

                // Lien "Mot de passe oublié"
                Hyperlink forgotPasswordLink = new Hyperlink("Mot de passe oublié ?");
                forgotPasswordLink.setStyle("-fx-text-fill: #1877f2; -fx-underline: false;");
                forgotPasswordLink.setAlignment(Pos.CENTER);

                // Séparateur
                Separator separator = new Separator();
                separator.setPrefWidth(350);
                separator.setPadding(new Insets(10, 0, 10, 0));

                // Bouton de création de compte
                Button createAccountButton = new Button("Créer un compte");
                createAccountButton.setPrefHeight(50);
                createAccountButton.setStyle("-fx-background-color: #42b72a; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 6px;");

                // Message du bas
                HBox bottomMessage = new HBox();
                bottomMessage.setAlignment(Pos.CENTER);
                Text celebrityText = new Text("Une célébrité, une marque ou une entreprise ? ");
                Hyperlink createPageLink = new Hyperlink("Créer une Page");
                createPageLink.setStyle("-fx-text-fill: #1c1e21; -fx-underline: false; -fx-font-weight: bold;");
                bottomMessage.getChildren().addAll(celebrityText, createPageLink);

                // Ajouter tous les éléments au formulaire
                formCard.getChildren().addAll(emailField, passwordField, errorLabel, loginButton, forgotPasswordLink, separator, createAccountButton, bottomMessage);

                // Ajouter les sections à la mise en page principale
                mainContent.getChildren().addAll(loginSection, formCard);
                root.setCenter(mainContent);

                // Créer le footer
                VBox footer = createFacebookFooter();
                root.setBottom(footer);

                // Actions des boutons
                loginButton.setOnAction(e -> {
                    String email = emailField.getText().trim();
                    String password = passwordField.getText().trim();

                    // Validation des champs
                    if (email.isEmpty() || password.isEmpty()) {
                        errorLabel.setText("Veuillez remplir tous les champs");
                        return;
                    }

                    if (!EmailValidator.isValid(email)) {
                        errorLabel.setText("Veuillez entrer une adresse email valide");
                        return;
                    }

                    // Traitement de la connexion
                    try {
                        // Vérifier si l'utilisateur existe déjà
                        User existingUser = null;
                        try {
                            existingUser = userService.findByEmail(email);
                        } catch (SQLException ex) {
                            // Erreur silencieuse, on considère que l'utilisateur n'existe pas
                        }

                        if (existingUser != null) {
                            // Utilisateur existant - on met simplement à jour le flag Facebook
                            existingUser.setFacebookAccount(true);
                            loginStage.close();
                            future.complete(existingUser);
                        } else {
                            // Si l'utilisateur n'existe pas encore, on en crée un nouveau de base
                            // Le nom et prénom seront par défaut basés sur l'email
                            String userName = email.split("@")[0];

                            User newUser = new User();
                            newUser.setEmail(email);
                            // On extrait un nom/prénom de l'email
                            if (userName.contains(".")) {
                                String[] parts = userName.split("\\.");
                                newUser.setPrenom(capitalizeFirstLetter(parts[0]));
                                newUser.setNom(parts.length > 1 ? capitalizeFirstLetter(parts[1]) : "");
                            } else {
                                newUser.setPrenom(capitalizeFirstLetter(userName));
                                newUser.setNom("");
                            }

                            newUser.setFacebookAccount(true);
                            newUser.setVerified(true);
                            newUser.setRole("membre");
                            newUser.setPassword("");
                            newUser.setGenre("Non spécifié");

                            try {
                                userService.add(newUser);
                                loginStage.close();
                                future.complete(newUser);
                            } catch (SQLException ex) {
                                errorLabel.setText("Erreur lors de la création du compte: " + ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        errorLabel.setText("Erreur d'authentification: " + ex.getMessage());
                    }
                });

                // Créer la scène
                Scene scene = new Scene(root, 800, 730);
                loginStage.setScene(scene);
                loginStage.showAndWait();

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Met en majuscule la première lettre d'une chaîne
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Crée la barre de navigation supérieure de Facebook
     */
    private HBox createFacebookHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);
        header.setStyle("-fx-background-color: white; -fx-border-color: #dddfe2; -fx-border-width: 0 0 1 0;");

        // Logo Facebook
        Text facebookText = new Text("facebook");
        facebookText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        facebookText.setFill(Color.web("#1877f2"));

        header.getChildren().add(facebookText);

        return header;
    }

    /**
     * Crée le pied de page de Facebook
     */
    private VBox createFacebookFooter() {
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-background-color: white;");

        // Langues
        HBox languages = new HBox(10);
        languages.setAlignment(Pos.CENTER);

        String[] langs = {"Français (France)", "English (US)", "العربية", "Español", "Português (Brasil)", "Italiano", "Deutsch"};

        for (String lang : langs) {
            Hyperlink langLink = new Hyperlink(lang);
            langLink.setStyle("-fx-text-fill: #8a8d91; -fx-underline: false;");
            languages.getChildren().add(langLink);
        }

        // Séparateur
        Separator separator = new Separator();

        // Liens de service (version courte)
        HBox services = new HBox(15);
        services.setAlignment(Pos.CENTER);

        String[] serviceLinks = {"Inscription", "Connexion", "Messenger", "Facebook Lite", "Watch", "Lieux", "Jeux"};

        for (String service : serviceLinks) {
            Hyperlink serviceLink = new Hyperlink(service);
            serviceLink.setStyle("-fx-text-fill: #8a8d91; -fx-underline: false;");
            services.getChildren().add(serviceLink);
        }

        // Copyright
        Text copyright = new Text("Meta © 2025");
        copyright.setFill(Color.web("#8a8d91"));

        footer.getChildren().addAll(languages, separator, services, copyright);

        return footer;
    }
}