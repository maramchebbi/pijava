package controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User;
import service.UserService;
import utils.ValidationUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ModifierUserController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private CheckBox verifiedCheckBox;
    @FXML private Label messageLabel;
    @FXML private VBox messageBox;
    @FXML private Button saveButton;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private StackPane statusIndicator;
    @FXML private Button changeAvatarBtn;
    @FXML private Accordion userInfoAccordion;

    private User user;
    private Runnable onUserUpdated;
    private File selectedAvatarFile;
    private boolean fieldsModified = false;

    /**
     * Initialise le contrôleur après le chargement du FXML
     */
    @FXML
    public void initialize() {
        // Initialiser la liste des rôles
        List<String> roles = Arrays.asList("admin", "membre");
        roleComboBox.setItems(FXCollections.observableArrayList(roles));

        // Initialiser le statut du message d'erreur
        messageBox.setVisible(false);

        // Ajouter des écouteurs pour détecter les changements dans les champs
        setupFieldListeners();

        // Centrer la fenêtre sur l'écran
        Platform.runLater(this::centerWindowOnScreen);
    }

    /**
     * Centre la fenêtre sur l'écran
     */
    private void centerWindowOnScreen() {
        try {
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Impossible de centrer la fenêtre: " + e.getMessage());
        }
    }

    /**
     * Configure les écouteurs pour détecter les modifications des champs
     */
    private void setupFieldListeners() {
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            fieldsModified = true;
            validateField(nomField, !newValue.trim().isEmpty(), "Le nom est requis");
        });

        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            fieldsModified = true;
            validateField(prenomField, !newValue.trim().isEmpty(), "Le prénom est requis");
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            fieldsModified = true;
            validateField(emailField, ValidationUtils.isValidEmail(newValue.trim()), "L'email n'est pas valide");
        });

        roleComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            fieldsModified = true;
        });

        verifiedCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            fieldsModified = true;
            updateStatusIndicator(newValue);
        });
    }

    /**
     * Met à jour l'indicateur de statut en fonction de l'état de vérification
     * @param isVerified Si le compte est vérifié
     */
    private void updateStatusIndicator(boolean isVerified) {
        if (isVerified) {
            statusIndicator.setStyle("-fx-background-color: #10b981; -fx-background-radius: 30;");
            if (!statusIndicator.getChildren().isEmpty() && statusIndicator.getChildren().get(0) instanceof Label) {
                Label statusLabel = (Label) statusIndicator.getChildren().get(0);
                statusLabel.setText("Actif");
            }
        } else {
            statusIndicator.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 30;");
            if (!statusIndicator.getChildren().isEmpty() && statusIndicator.getChildren().get(0) instanceof Label) {
                Label statusLabel = (Label) statusIndicator.getChildren().get(0);
                statusLabel.setText("En attente");
            }
        }
    }

    /**
     * Valide un champ de texte et met à jour son style en conséquence
     * @param field Le champ à valider
     * @param isValid Si le champ est valide
     * @param errorMessage Le message d'erreur à afficher si non valide
     */
    private void validateField(TextField field, boolean isValid, String errorMessage) {
        if (isValid) {
            field.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 12; -fx-font-size: 14; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1.5;");
        } else {
            field.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 10; -fx-padding: 12; -fx-font-size: 14; -fx-border-color: #ef4444; -fx-border-radius: 10; -fx-border-width: 1.5;");
            showError(errorMessage);
        }
    }

    /**
     * Définit l'utilisateur à modifier et remplit les champs du formulaire
     * @param user L'utilisateur à modifier
     */
    public void setUser(User user) {
        if (user == null) {
            showError("Utilisateur non trouvé.");
            return;
        }
        this.user = user;

        // Remplir les champs avec les données de l'utilisateur
        nomField.setText(user.getNom() != null ? user.getNom() : "");
        prenomField.setText(user.getPrenom() != null ? user.getPrenom() : "");
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        verifiedCheckBox.setSelected(user.isVerified());

        // Mise à jour du statut
        updateStatusIndicator(user.isVerified());

        // Définition du rôle
        String userRole = user.getRole();
        if (userRole == null || userRole.isEmpty() ||
                (!userRole.equals("admin") && !userRole.equals("membre"))) {
            roleComboBox.setValue("membre");
        } else {
            roleComboBox.setValue(userRole);
        }

        // Mise à jour des informations d'historique si disponibles
        updateUserHistoryInfo();

        // Réinitialisation de l'état de modification
        fieldsModified = false;
    }

    /**
     * Met à jour les informations d'historique de l'utilisateur
     */
    private void updateUserHistoryInfo() {
        // Cette méthode pourrait être utilisée pour charger l'historique réel depuis la base de données
    }

    /**
     * Définit le callback à appeler lorsque l'utilisateur est mis à jour
     * @param callback Le callback à appeler
     */
    public void setOnUserUpdated(Runnable callback) {
        this.onUserUpdated = callback;
    }

    /**
     * Gère l'événement de sauvegarde des modifications
     */
    @FXML
    private void handleSave() {
        try {
            // Masquer le message d'erreur précédent
            messageBox.setVisible(false);

            // Vérifier que l'utilisateur est bien défini
            if (user == null) {
                showError("Erreur: Aucun utilisateur à modifier.");
                return;
            }

            // Récupérer les valeurs des champs
            String nom = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email = emailField.getText().trim();
            boolean isVerified = verifiedCheckBox.isSelected();
            String role = roleComboBox.getValue();

            // Validation des champs
            if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || role == null) {
                showError("Veuillez remplir tous les champs obligatoires.");
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                showError("L'adresse email n'est pas valide.");
                return;
            }

            // Vérifier que le rôle est valide
            if (!role.equals("admin") && !role.equals("membre")) {
                roleComboBox.setValue("membre");
                role = "membre";
            }

            // Mise à jour de l'utilisateur
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setVerified(isVerified);
            user.setRole(role);

            // Mettre à jour la date de dernière modification si la méthode existe
            try {
                Method lastUpdateMethod = user.getClass().getMethod("setLastUpdate", LocalDateTime.class);
                if (lastUpdateMethod != null) {
                    lastUpdateMethod.invoke(user, LocalDateTime.now());
                }            } catch (Exception e) {
                System.out.println("La méthode setLastUpdate n'existe pas ou a échoué: " + e.getMessage());
                // Ne pas bloquer la mise à jour pour cette raison
            }

            // Gérer l'avatar si une nouvelle image a été sélectionnée
            if (selectedAvatarFile != null) {
                // Dans une implémentation réelle, vous stockeriez l'image
                // et mettriez à jour le chemin dans l'objet utilisateur
                System.out.println("Nouvel avatar sélectionné: " + selectedAvatarFile.getAbsolutePath());
            }

            // Sauvegarder les modifications
            UserService userService = new UserService();
            userService.update(user);

            // Appeler le callback si défini
            if (onUserUpdated != null) {
                onUserUpdated.run();
            }

            // Afficher une notification de succès avant de fermer
            showSuccess("Utilisateur mis à jour avec succès");

            // Fermer la fenêtre après un court délai
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) nomField.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showError("Erreur lors de la mise à jour: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère l'événement de changement d'avatar
     */
    @FXML
    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        try {
            // Ouvrir la boîte de dialogue de sélection de fichier
            Stage stage = (Stage) changeAvatarBtn.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                // Stocker le fichier sélectionné pour traitement ultérieur lors de la sauvegarde
                selectedAvatarFile = selectedFile;

                // Afficher l'aperçu de l'image
                Image image = new Image(selectedFile.toURI().toString());

                // Trouver l'ImageView dans la vue et mettre à jour l'image
                // Vérifier d'abord que le parent est bien un StackPane
                if (changeAvatarBtn.getParent() instanceof StackPane) {
                    StackPane avatarContainer = (StackPane) changeAvatarBtn.getParent();
                    // Vérifier que l'index 2 est valide et contient une ImageView
                    if (avatarContainer.getChildren().size() > 2 &&
                            avatarContainer.getChildren().get(2) instanceof ImageView) {
                        ImageView avatarImageView = (ImageView) avatarContainer.getChildren().get(2);
                        avatarImageView.setImage(image);
                    }
                }

                // Marquer comme modifié
                fieldsModified = true;
            }
        } catch (Exception e) {
            showError("Impossible de charger l'image: " + e.getMessage());
        }
    }

    /**
     * Gère l'événement d'annulation
     * @param event L'événement d'action
     */
    @FXML
    private void annulerAction(ActionEvent event) {
        // Vérifier s'il y a des modifications non sauvegardées
        if (fieldsModified) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Modifications non sauvegardées");
            alert.setHeaderText("Vous avez des modifications non sauvegardées");
            alert.setContentText("Voulez-vous quitter sans sauvegarder?");

            ButtonType buttonTypeYes = new ButtonType("Oui");
            ButtonType buttonTypeNo = new ButtonType("Non");

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            // Centrer la boîte de dialogue
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.setOnShown(e -> Platform.runLater(dialogStage::centerOnScreen));

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeYes) {
                    closeWindow();
                }
            });
        } else {
            closeWindow();
        }
    }

    /**
     * Ferme la fenêtre actuelle
     */
    private void closeWindow() {
        try {
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture de la fenêtre: " + e.getMessage());
        }
    }

    /**
     * Affiche un message d'erreur
     * @param message Le message d'erreur à afficher
     */
    private void showError(String message) {
        if (messageLabel == null || messageBox == null) return;

        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #b91c1c;");
        messageBox.setStyle("-fx-background-color: #fee2e2; -fx-background-radius: 12; -fx-padding: 12 15; -fx-border-color: #fecaca; -fx-border-radius: 12; -fx-border-width: 1;");
        messageBox.setVisible(true);
    }

    /**
     * Affiche un message de succès
     * @param message Le message de succès à afficher
     */
    private void showSuccess(String message) {
        if (messageLabel == null || messageBox == null) return;

        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: #047857;");
        messageBox.setStyle("-fx-background-color: #dcfce7; -fx-background-radius: 12; -fx-padding: 12 15; -fx-border-color: #bbf7d0; -fx-border-radius: 12; -fx-border-width: 1;");
        messageBox.setVisible(true);
    }
}