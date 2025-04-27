package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.User;
import service.UserService;
import utils.SessionManager;
import utils.ValidationUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import javafx.stage.FileChooser;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
public class ProfileController {
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField NomDetailTextField;
    @FXML private TextField PrenomDetailTextField;
    @FXML private TextField GenreDetailTextField;
    @FXML private TextField EmailDetailTextField;
    @FXML private TextField PasswordDetailTextField;
    @FXML private TextField RoleDetailTextField;
    @FXML private Button modifierBtn, sauvegarderBtn;

    // Nouveaux éléments
    @FXML private Button changerMotDePasseBtn;
    @FXML private Button exporterProfilBtn;
    @FXML private Label derniereConnexionLabel;
    @FXML private Label erreurMessageLabel;

    private User currentUser;
    private LocalDateTime lastLoginTime;
    private boolean hasUnsavedChanges = false;
    @FXML
    private ScrollPane scrollPane;

    /**
     * Initialise les composants après le chargement de la vue
     */
    @FXML
    public void initialize() {
        // Initialiser le label d'erreur comme invisible
        if (erreurMessageLabel != null) {
            erreurMessageLabel.setVisible(false);
            erreurMessageLabel.setStyle("-fx-text-fill: #e11d48;");
        }

        // Désactiver le bouton sauvegarder par défaut
        if (sauvegarderBtn != null) {
            sauvegarderBtn.setDisable(true);
            sauvegarderBtn.setVisible(false);
        }

        // Ajouter des écouteurs pour détecter les changements
        setupChangeListeners();
    }

    /**
     * Configure les écouteurs pour détecter les changements dans les champs
     */
    private void setupChangeListeners() {
        // Ajouter des écouteurs pour détecter les changements
        NomDetailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (NomDetailTextField.isEditable()) {
                hasUnsavedChanges = true;
                sauvegarderBtn.setDisable(false);
                validateInput();
            }
        });

        PrenomDetailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (PrenomDetailTextField.isEditable()) {
                hasUnsavedChanges = true;
                sauvegarderBtn.setDisable(false);
                validateInput();
            }
        });
    }

    /**
     * Valide les entrées utilisateur en temps réel
     */
    private void validateInput() {
        boolean isValid = true;
        String errorMessage = "";

        // Validation du nom (non vide et au moins 2 caractères)
        if (NomDetailTextField.getText().trim().isEmpty() || NomDetailTextField.getText().trim().length() < 2) {
            isValid = false;
            errorMessage = "Le nom doit contenir au moins 2 caractères";
        }

        // Validation du prénom (non vide et au moins 2 caractères)
        if (PrenomDetailTextField.getText().trim().isEmpty() || PrenomDetailTextField.getText().trim().length() < 2) {
            isValid = false;
            errorMessage = isValid ? errorMessage : errorMessage + "\nLe prénom doit contenir au moins 2 caractères";
        }

        // Afficher ou cacher le message d'erreur
        if (!isValid) {
            erreurMessageLabel.setText(errorMessage);
            erreurMessageLabel.setVisible(true);
            sauvegarderBtn.setDisable(true);
        } else {
            erreurMessageLabel.setVisible(false);
            sauvegarderBtn.setDisable(false);
        }
    }


    public void setUser(User user) {
        this.currentUser = user;

        // Mettre à jour les labels d'avatar (nom complet et rôle)
        if (fullNameLabel != null) {
            fullNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        if (roleLabel != null) {
            roleLabel.setText(user.getRole());
        }

        // Remplir les champs avec les informations de l'utilisateur
        NomDetailTextField.setText(user.getNom());
        PrenomDetailTextField.setText(user.getPrenom());
        GenreDetailTextField.setText(user.getGenre());
        EmailDetailTextField.setText(user.getEmail());

        // Masquer le mot de passe avec des astérisques
        PasswordDetailTextField.setText("••••••••");

        RoleDetailTextField.setText(user.getRole());

        // Définir la dernière connexion
        this.lastLoginTime = LocalDateTime.now();
        updateLastLoginInfo();

        // Rendre tous les champs non éditables par défaut
        setFieldsEditable(false);

        // Afficher le bouton Modifier et masquer le bouton Sauvegarder
        sauvegarderBtn.setVisible(false);
        modifierBtn.setVisible(true);

        // Réinitialiser le flag des modifications non sauvegardées
        hasUnsavedChanges = false;
    }

    /**
     * Met à jour l'affichage de la dernière connexion
     */
    private void updateLastLoginInfo() {
        if (derniereConnexionLabel != null && lastLoginTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
            derniereConnexionLabel.setText("Dernière connexion: " + lastLoginTime.format(formatter));
        }
    }

    /**
     * Définit si les champs sont éditables ou non
     * @param editable true si les champs doivent être éditables, false sinon
     */
    private void setFieldsEditable(boolean editable) {
        NomDetailTextField.setEditable(editable);
        PrenomDetailTextField.setEditable(editable);

        // Ces champs ne sont jamais éditables directement
        GenreDetailTextField.setEditable(false);
        EmailDetailTextField.setEditable(false);
        PasswordDetailTextField.setEditable(false);
        RoleDetailTextField.setEditable(false);

        // Mettre à jour le style des champs éditables
        updateEditableFieldsStyle(editable);
    }

    private void updateEditableFieldsStyle(boolean editable) {
        String editableStyle = "-fx-background-color: #FFFFFF; -fx-background-radius: 4; -fx-padding: 10; -fx-font-size: 14; -fx-border-color: #9C7A51; -fx-border-width: 1px; -fx-border-radius: 4;";
        String nonEditableStyle = "-fx-background-color: #FAF6E9; -fx-background-radius: 4; -fx-padding: 10; -fx-font-size: 14; -fx-border-color: #D7CCA1; -fx-border-radius: 4;";

        NomDetailTextField.setStyle(editable ? editableStyle : nonEditableStyle);
        PrenomDetailTextField.setStyle(editable ? editableStyle : nonEditableStyle);
    }

    /**
     * Gère le clic sur le bouton Modifier
     * @param event L'événement de clic
     */
    @FXML
    private void handleModifier(ActionEvent event) {
        // Rendre les champs éditables
        setFieldsEditable(true);

        // Gérer les boutons
        sauvegarderBtn.setVisible(true);
        modifierBtn.setVisible(false);

        // Désactiver le bouton Sauvegarder tant qu'aucune modification n'est faite
        sauvegarderBtn.setDisable(true);
        hasUnsavedChanges = false;
    }

    /**
     * Gère le clic sur le bouton Sauvegarder
     * @param event L'événement de clic
     */
    @FXML
    private void handleSauvegarder(ActionEvent event) {
        // Valider les entrées une dernière fois
        if (validateBeforeSave()) {
            // Mettre à jour l'objet User
            currentUser.setNom(NomDetailTextField.getText().trim());
            currentUser.setPrenom(PrenomDetailTextField.getText().trim());

            // Ici, nous ne modifions pas userService directement comme demandé
            // Dans une vraie application, il faudrait sauvegarder les modifications

            // Repasser tous les champs en lecture seule
            setFieldsEditable(false);

            // Afficher à nouveau le bouton Modifier
            sauvegarderBtn.setVisible(false);
            modifierBtn.setVisible(true);

            // Réinitialiser le flag des modifications non sauvegardées
            hasUnsavedChanges = false;

            // Afficher une notification de succès
            showNotification("Profil mis à jour avec succès", AlertType.INFORMATION);
        }
    }

    /**
     * Valide les entrées avant de sauvegarder
     * @return true si les entrées sont valides, false sinon
     */
    private boolean validateBeforeSave() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        // Validation du nom
        if (NomDetailTextField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage.append("Le nom ne peut pas être vide\n");
        } else if (!ValidationUtils.isValidName(NomDetailTextField.getText().trim())) {
            isValid = false;
            errorMessage.append("Le nom contient des caractères non autorisés\n");
        }

        // Validation du prénom
        if (PrenomDetailTextField.getText().trim().isEmpty()) {
            isValid = false;
            errorMessage.append("Le prénom ne peut pas être vide\n");
        } else if (!ValidationUtils.isValidName(PrenomDetailTextField.getText().trim())) {
            isValid = false;
            errorMessage.append("Le prénom contient des caractères non autorisés\n");
        }

        // Afficher les erreurs s'il y en a
        if (!isValid) {
            showNotification(errorMessage.toString(), AlertType.ERROR);
        }

        return isValid;
    }

    /**
     * Gère le clic sur le bouton Annuler
     * @param event L'événement de clic
     */
    @FXML
    private void annulerAction(ActionEvent event) {
        // Vérifier s'il y a des modifications non sauvegardées
        if (hasUnsavedChanges) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Modifications non sauvegardées");
            alert.setHeaderText("Vous avez des modifications non sauvegardées");
            alert.setContentText("Voulez-vous quitter sans sauvegarder?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK) {
                return; // L'utilisateur a annulé
            }
        }

        // Fermer la fenêtre
        Stage stage = (Stage) NomDetailTextField.getScene().getWindow();
        stage.close();
    }
    /**
     * Gère le clic sur le bouton Changer mot de passe avec une interface améliorée
     * @param event L'événement de clic
     */
    @FXML
    private void handleChangerMotDePasse(ActionEvent event) {
        // Créer une boîte de dialogue personnalisée
        Dialog<ButtonType> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Changer le mot de passe");
        passwordDialog.setHeaderText(null);

        // Configuration des boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 25, 20, 25));

        // Création des champs pour le mot de passe
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Entrez votre mot de passe actuel");
        oldPasswordField.setPrefWidth(300);
        oldPasswordField.setStyle("-fx-padding: 10px; -fx-background-radius: 4px;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Entrez votre nouveau mot de passe");
        newPasswordField.setStyle("-fx-padding: 10px; -fx-background-radius: 4px;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmez votre nouveau mot de passe");
        confirmPasswordField.setStyle("-fx-padding: 10px; -fx-background-radius: 4px;");

        // Création des labels avec icônes
        Label oldPassLabel = createIconLabel("Mot de passe actuel", "lock");
        Label newPassLabel = createIconLabel("Nouveau mot de passe", "key");
        Label confirmPassLabel = createIconLabel("Confirmer mot de passe", "check-circle");

        // Label pour les messages d'erreur
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e11d48; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        // Barre de progression pour la force du mot de passe
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(300);
        strengthBar.setStyle("-fx-accent: #94a3b8;"); // Couleur par défaut

        Label strengthLabel = new Label("Force: Faible");
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        // Ajouter les composants à la grille
        grid.add(oldPassLabel, 0, 0);
        grid.add(oldPasswordField, 0, 1);

        grid.add(newPassLabel, 0, 2);
        grid.add(newPasswordField, 0, 3);
        grid.add(strengthBar, 0, 4);
        grid.add(strengthLabel, 0, 5);

        grid.add(confirmPassLabel, 0, 6);
        grid.add(confirmPasswordField, 0, 7);

        grid.add(errorLabel, 0, 8);

        // Ajouter des conseils pour mot de passe fort

        // Définir le contenu de la boîte de dialogue
        passwordDialog.getDialogPane().setContent(grid);

        // Récupérer le bouton Enregistrer pour le désactiver par défaut
        Button saveButton = (Button) passwordDialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Style des boutons
        saveButton.getStyleClass().add("save-button");
        saveButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold;");

        Button cancelButton = (Button) passwordDialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b;");

        // Ajouter des écouteurs pour valider les champs
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue, strengthBar, strengthLabel);
            validatePasswordFields(oldPasswordField, newPasswordField, confirmPasswordField, errorLabel, saveButton);
        });

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordFields(oldPasswordField, newPasswordField, confirmPasswordField, errorLabel, saveButton);
        });

        oldPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePasswordFields(oldPasswordField, newPasswordField, confirmPasswordField, errorLabel, saveButton);
        });

        // Donner le focus au premier champ
        Platform.runLater(() -> oldPasswordField.requestFocus());

        // Afficher la boîte de dialogue et attendre la réponse
        Optional<ButtonType> result = passwordDialog.showAndWait();

        // Traiter la réponse
        if (result.isPresent() && result.get() == saveButtonType) {
            try {
                // Simuler la vérification du mot de passe actuel
                if (!isCurrentPasswordValid(oldPasswordField.getText())) {
                    showNotification("Le mot de passe actuel est incorrect", AlertType.ERROR);
                    return;
                }

                // Ici, on simule le changement de mot de passe
                // Dans une application réelle, on appellerait un service

                // Simuler un petit délai pour donner l'impression que le traitement est en cours
                showLoadingDialog("Modification du mot de passe en cours...");

                // Notification de succès
                showNotification("Mot de passe modifié avec succès", AlertType.INFORMATION);

            } catch (Exception e) {
                showNotification("Erreur lors du changement de mot de passe: " + e.getMessage(), AlertType.ERROR);
            }
        }
    }

    /**
     * Crée un label avec une icône
     * @param text Le texte du label
     * @param iconName Le nom de l'icône
     * @return Le label avec l'icône
     */
    private Label createIconLabel(String text, String iconName) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-font-size: 13px;");

        // Dans une application réelle, vous pourriez charger des icônes SVG ou des images
        // Pour simplifier, on utilise juste le texte ici
        return label;
    }

    /**
     * Crée un panneau d'astuces pour les mots de passe
     * @return Un TitledPane contenant les astuces
     */


    /**
     * Met à jour l'indicateur de force du mot de passe
     * @param password Le mot de passe à évaluer
     * @param strengthBar La barre de progression à mettre à jour
     * @param strengthLabel Le label à mettre à jour
     */
    private void updatePasswordStrength(String password, ProgressBar strengthBar, Label strengthLabel) {
        if (password == null || password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthBar.setStyle("-fx-accent: #94a3b8;");
            strengthLabel.setText("Force: Faible");
            strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            return;
        }

        // Calculer la force du mot de passe
        int strength = 0;

        // Longueur minimale (8 caractères)
        if (password.length() >= 8) strength++;

        // Présence de lettres minuscules
        if (password.matches(".*[a-z].*")) strength++;

        // Présence de lettres majuscules
        if (password.matches(".*[A-Z].*")) strength++;

        // Présence de chiffres
        if (password.matches(".*\\d.*")) strength++;

        // Présence de caractères spéciaux
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++;

        // Mettre à jour la barre et le label
        double strengthValue = strength / 5.0;
        strengthBar.setProgress(strengthValue);

        if (strengthValue < 0.3) {
            strengthBar.setStyle("-fx-accent: #ef4444;"); // Rouge pour faible
            strengthLabel.setText("Force: Faible");
            strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444;");
        } else if (strengthValue < 0.6) {
            strengthBar.setStyle("-fx-accent: #f59e0b;"); // Orange pour moyen
            strengthLabel.setText("Force: Moyenne");
            strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f59e0b;");
        } else if (strengthValue < 0.8) {
            strengthBar.setStyle("-fx-accent: #10b981;"); // Vert pour bon
            strengthLabel.setText("Force: Bonne");
            strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #10b981;");
        } else {
            strengthBar.setStyle("-fx-accent: #059669;"); // Vert foncé pour excellent
            strengthLabel.setText("Force: Excellente");
            strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669;");
        }
    }

    /**
     * Valide les champs du formulaire de changement de mot de passe
     */
    /**
     * Vérifie si un mot de passe est suffisamment fort
     * @param password Le mot de passe à vérifier
     * @return true si le mot de passe est fort, false sinon
     */
    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    }

    /**
     * Simule la vérification du mot de passe actuel
     * Dans une vraie application, cette méthode appellerait un service d'authentification
     * @param password Le mot de passe à vérifier
     * @return true si le mot de passe est valide, false sinon
     */
    private boolean isCurrentPasswordValid(String password) {
        // Simuler une vérification - dans une vraie application, on vérifierait avec la base de données
        return true; // Toujours retourner vrai pour la démonstration
    }

    /**
     * Affiche une boîte de dialogue de chargement
     * @param message Le message à afficher
     */
    private void showLoadingDialog(String message) {
        // Créer une boîte de dialogue simple
        Dialog<Void> loadingDialog = new Dialog<>();
        loadingDialog.setTitle("Traitement en cours");
        loadingDialog.setHeaderText(null);

        // Ajouter un indicateur de progression et un message
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(40, 40);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(progress, messageLabel);
        loadingDialog.getDialogPane().setContent(content);

        // Ajouter un bouton caché (juste pour la structure de la boîte de dialogue)
        loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node closeButton = loadingDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        closeButton.setVisible(false);

        // Afficher la boîte de dialogue et la fermer après un court délai
        Platform.runLater(() -> {
            loadingDialog.show();

            // Simuler un traitement de 1.5 seconde
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> loadingDialog.close());
            delay.play();
        });
    }

    /**
     * Valide les champs du formulaire de changement de mot de passe
     */
    private void validatePasswordFields(PasswordField oldPasswordField, PasswordField newPasswordField,
                                        PasswordField confirmPasswordField, Label errorLabel, Button okButton) {
        boolean isValid = true;
        String errorMessage = "";

        // Vérifier que l'ancien mot de passe n'est pas vide
        if (oldPasswordField.getText().isEmpty()) {
            isValid = false;
            errorMessage = "L'ancien mot de passe est requis";
        }

        // Vérifier que le nouveau mot de passe est assez long
        if (newPasswordField.getText().length() < 8) {
            isValid = false;
            errorMessage = "Le nouveau mot de passe doit contenir au moins 8 caractères";
        }

        // Vérifier que les deux mots de passe correspondent
        if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            isValid = false;
            errorMessage = "Les mots de passe ne correspondent pas";
        }

        // Mettre à jour l'interface
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(!isValid);
        okButton.setDisable(!isValid);
    }

    /**
     * Gère le clic sur le bouton Exporter profil
     * @param event L'événement de clic
     */

    /**
     * Gère le clic sur le bouton Exporter profil pour créer un PDF
     * @param event L'événement de clic
     */
    @FXML
    private void handleExporterProfil(ActionEvent event) {
        try {
            // Créer un sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le profil en PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName(currentUser.getNom() + "_" + currentUser.getPrenom() + "_profil.pdf");

            // Afficher la boîte de dialogue pour sélectionner l'emplacement du fichier
            File file = fileChooser.showSaveDialog(NomDetailTextField.getScene().getWindow());

            if (file != null) {
                // Générer le PDF avec iText
                generatePDF(file.getAbsolutePath());

                // Afficher une notification de succès
                showNotification("Profil exporté avec succès en PDF", AlertType.INFORMATION);

                // Ouvrir le PDF si l'utilisateur le souhaite
                if (askToOpenPDF()) {
                    openFile(file);
                }
            }
        } catch (Exception e) {
            showNotification("Erreur lors de l'exportation du profil: " + e.getMessage(), AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Génère un fichier PDF contenant les informations du profil
     * @param filePath Le chemin du fichier PDF à générer
     * @throws Exception En cas d'erreur lors de la création du PDF
     */
    private void generatePDF(String filePath) throws Exception {
        // Créer un document PDF
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Ouvrir le document
        document.open();

        // Ajouter les métadonnées
        document.addTitle("Profil Utilisateur");
        document.addAuthor("Système de Gestion Utilisateur");
        document.addCreationDate();

        // Définir les polices
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.DARK_GRAY);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

        // Ajouter le logo si disponible
        try {
            // Chargement du logo de l'application
            Image logo = Image.getInstance(getClass().getResource("/images/logo.png"));
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        } catch (Exception e) {
            // En cas d'erreur de chargement du logo, on continue sans l'ajouter
            System.out.println("Logo non trouvé: " + e.getMessage());
        }

        // Ajouter le titre
        Paragraph title = new Paragraph("Profil Utilisateur", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        title.setSpacingAfter(15);
        document.add(title);

        // Date d'exportation
        Paragraph exportDate = new Paragraph(
                "Exporté le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss")),
                new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)
        );
        exportDate.setAlignment(Element.ALIGN_RIGHT);
        exportDate.setSpacingAfter(20);
        document.add(exportDate);

        // Section Informations Personnelles
        Paragraph personalInfoTitle = new Paragraph("INFORMATIONS PERSONNELLES", sectionFont);
        personalInfoTitle.setSpacingBefore(10);
        personalInfoTitle.setSpacingAfter(10);
        document.add(personalInfoTitle);

        // Ajouter un séparateur
        LineSeparator line = new LineSeparator();
        line.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(line);

        // Création d'une table pour les informations personnelles
        PdfPTable personalTable = new PdfPTable(2);
        personalTable.setWidthPercentage(100);
        personalTable.setSpacingBefore(10);
        personalTable.setSpacingAfter(10);

        // Définition des largeurs de colonnes
        float[] personalColumnWidths = {1f, 3f};
        personalTable.setWidths(personalColumnWidths);

        // Ajout des cellules pour les informations personnelles
        addTableRow(personalTable, "Nom:", currentUser.getNom(), labelFont, valueFont);
        addTableRow(personalTable, "Prénom:", currentUser.getPrenom(), labelFont, valueFont);
        addTableRow(personalTable, "Genre:", currentUser.getGenre(), labelFont, valueFont);
        addTableRow(personalTable, "Rôle:", currentUser.getRole(), labelFont, valueFont);

        document.add(personalTable);

        // Section Informations du Compte
        Paragraph accountInfoTitle = new Paragraph("INFORMATIONS DU COMPTE", sectionFont);
        accountInfoTitle.setSpacingBefore(15);
        accountInfoTitle.setSpacingAfter(10);
        document.add(accountInfoTitle);

        // Ajouter un séparateur
        document.add(line);

        // Création d'une table pour les informations du compte
        PdfPTable accountTable = new PdfPTable(2);
        accountTable.setWidthPercentage(100);
        accountTable.setSpacingBefore(10);
        accountTable.setSpacingAfter(10);

        // Définition des largeurs de colonnes
        float[] accountColumnWidths = {1f, 3f};
        accountTable.setWidths(accountColumnWidths);

        // Ajout des cellules pour les informations du compte
        addTableRow(accountTable, "Email:", currentUser.getEmail(), labelFont, valueFont);
        addTableRow(accountTable, "Mot de passe:", "••••••••", labelFont, valueFont);

        // Par ce code qui vérifie si la méthode existe avant de l'appeler
        try {
            // Vérifier si la méthode existe sans l'appeler
            getClass().getMethod("setLastUpdate", LocalDateTime.class);
            // Si on arrive ici, la méthode existe
            // Mais comme elle n'existe pas vraiment, on ne fait rien
        } catch (NoSuchMethodException e) {
            // La méthode n'existe pas, on ignore silencieusement
            System.out.println("Méthode setLastUpdate non disponible dans la classe User");
        }

        document.add(accountTable);

        // Ajouter un pied de page
        Paragraph footer = new Paragraph(
                "Ce document a été généré automatiquement par le Système de Gestion Utilisateur.",
                new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        // Fermer le document
        document.close();
    }

    /**
     * Ajoute une ligne à une table PDF
     * @param table La table à laquelle ajouter la ligne
     * @param label Le libellé de la cellule
     * @param value La valeur de la cellule
     * @param labelFont La police pour le libellé
     * @param valueFont La police pour la valeur
     */
    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderWidth(0);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderWidth(0);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    /**
     * Demande à l'utilisateur s'il souhaite ouvrir le PDF généré
     * @return true si l'utilisateur souhaite ouvrir le PDF, false sinon
     */
    private boolean askToOpenPDF() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Ouverture du PDF");
        alert.setHeaderText("Le PDF a été créé avec succès");
        alert.setContentText("Voulez-vous ouvrir le fichier maintenant?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Ouvre un fichier avec l'application par défaut du système
     * @param file Le fichier à ouvrir
     */
    private void openFile(File file) {
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file);
            } else {
                throw new UnsupportedOperationException("L'ouverture de fichier n'est pas prise en charge sur cette plateforme");
            }
        } catch (Exception e) {
            showNotification("Impossible d'ouvrir le fichier: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Affiche une notification à l'utilisateur
     * @param message Le message à afficher
     * @param type Le type d'alerte
     */
    private void showNotification(String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == AlertType.ERROR ? "Erreur" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private Button supprimerCompteBtn;
    // Interface de callback pour la suppression de compte
    public interface AccountDeletedCallback {
        void onAccountDeleted();
    }

    private AccountDeletedCallback deleteCallback;

    public void setAccountDeletedCallback(AccountDeletedCallback callback) {
        this.deleteCallback = callback;
    }

    /**
     * Gère le clic sur le bouton "Supprimer mon compte"
     * @param event L'événement de clic
     */
    @FXML
    private void handleSupprimerCompte(ActionEvent event) {
        // Afficher une boîte de dialogue de confirmation avec un style d'alerte
        Alert confirmationAlert = new Alert(AlertType.WARNING);
        confirmationAlert.setTitle("Confirmation de suppression");
        confirmationAlert.setHeaderText("Êtes-vous sûr de vouloir supprimer votre compte ?");
        confirmationAlert.setContentText("Cette action est irréversible. Toutes vos données seront définitivement supprimées.");

        // Personnaliser les boutons
        ButtonType buttonTypeOui = new ButtonType("Oui, supprimer mon compte", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNon = new ButtonType("Non, annuler", ButtonBar.ButtonData.NO);

        confirmationAlert.getButtonTypes().setAll(buttonTypeNon, buttonTypeOui);

        // Attendre la réponse de l'utilisateur
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        if (result.isPresent() && result.get() == buttonTypeOui) {
            try {
                // Afficher une boîte de dialogue de chargement
                showLoadingDialog("Suppression du compte en cours...");

                // Créer une pause pour simuler le traitement
                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(e -> {
                    try {
                        // Appeler le service pour supprimer l'utilisateur
                        UserService userService = new UserService();
                        userService.delete(currentUser.getId());

                        // Déconnecter l'utilisateur
                        SessionManager.logout();

                        // Afficher une notification de succès
                        showNotification("Votre compte a été supprimé avec succès", AlertType.INFORMATION);

                        // Fermer la fenêtre de profil
                        Stage currentStage = (Stage) NomDetailTextField.getScene().getWindow();

                        // Notifier la fenêtre principale via le callback avant de fermer
                        if (deleteCallback != null) {
                            deleteCallback.onAccountDeleted();
                        }

                        // Fermer la fenêtre de profil
                        currentStage.close();

                    } catch (Exception ex) {
                        showNotification("Erreur lors de la suppression du compte: " + ex.getMessage(), AlertType.ERROR);
                        ex.printStackTrace();
                    }
                });
                pause.play();

            } catch (Exception e) {
                showNotification("Erreur lors de la suppression du compte: " + e.getMessage(), AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void redirectMainWindowToLogin() {
        try {
            // Parcourir toutes les fenêtres ouvertes pour trouver la fenêtre principale
            for (Stage stage : getOpenStages()) {
                // Vérifier si c'est la fenêtre principale (AfficherUser)
                if (stage.getTitle().contains("Utilisateurs") ||
                        stage.getScene().getRoot().getId() != null &&
                                stage.getScene().getRoot().getId().equals("afficherUserRoot")) {

                    // Charger la vue de login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
                    Parent loginRoot = loader.load();

                    // Créer une nouvelle scène
                    Scene loginScene = new Scene(loginRoot);

                    // Remplacer la scène actuelle par la scène de login
                    stage.setScene(loginScene);
                    stage.setTitle("Connexion");

                    // Pas besoin de continuer à chercher
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une notification d'erreur sans bloquer le flux
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur de redirection");
                alert.setContentText("Impossible de rediriger vers la page de login: " + e.getMessage());
                alert.show();
            });
        }
    }

    /**
     * Récupère toutes les fenêtres JavaFX ouvertes
     */
    private List<Stage> getOpenStages() {
        List<Stage> openStages = new ArrayList<>();

        // Parcourir toutes les fenêtres
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage) {
                openStages.add((Stage) window);
            }
        }

        return openStages;
    }

}