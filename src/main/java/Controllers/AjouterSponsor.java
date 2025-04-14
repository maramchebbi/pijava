package Controllers;

import Models.Sponsor;
import Services.SponsorService;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class AjouterSponsor {

    @FXML
    private TextField SponsorNom;

    @FXML
    private TextField SponsorType;

    @FXML
    private TextField SponsorEmail;

    @FXML
    private TextField SponsorTelephone;

    @FXML
    private TextField SponsorSiteWeb;

    @FXML
    private TextField SponsorMontant;

    @FXML
    private TextField SponsorLogo;

    @FXML private Label errorNom;
    @FXML private Label errorType;
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorMontant;
    @FXML private Label errorSiteWeb;

    private File selectedFile;
    private final SponsorService sponsorService = new SponsorService();

    public AjouterSponsor() throws SQLException {
    }

    @FXML
    public void initialize() {
    }

    @FXML
    public void ajouterSponsor() {
        // Réinitialiser les messages d'erreur
        errorNom.setText("");
        errorType.setText("");
        errorEmail.setText("");
        errorTelephone.setText("");
        errorSiteWeb.setText("");
        errorMontant.setText("");

        try {
            String nom = SponsorNom.getText();
            String type = SponsorType.getText();
            String email = SponsorEmail.getText();
            String telephone = SponsorTelephone.getText();
            String siteWeb = SponsorSiteWeb.getText();
            String montantStr = SponsorMontant.getText();
            String logo = SponsorLogo.getText();

            boolean hasError = false;

            if (nom == null || nom.isBlank()) {
                errorNom.setText("Le nom est obligatoire !");
                hasError = true;
            }

            if (type == null || type.isBlank()) {
                errorType.setText("Le type est obligatoire !");
                hasError = true;
            }

            if (email == null || email.isBlank()) {
                errorEmail.setText("L'email est obligatoire !");
                hasError = true;
            } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                errorEmail.setText("Format d'email invalide (Contenir . et @) !");
                hasError = true;
            }

            if (siteWeb == null || siteWeb.isBlank()) {
                errorSiteWeb.setText("Le site web est obligatoire !");
                hasError = true;
            } else if (!siteWeb.matches("^(https?://)?(www\\.)?[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,}(/\\S*)?$")) {
                errorSiteWeb.setText("Format de site web invalide (https://) !");
                hasError = true;
            }

            if (telephone == null || telephone.isBlank()) {
                errorTelephone.setText("Le téléphone est obligatoire !");
                hasError = true;
            } else if (!telephone.matches("^(\\+?\\d{8,11})$")) {
                errorTelephone.setText("Numéro de téléphone invalide (entre 8 et 11 chiffres) !");
                hasError = true;
            }

            double montant = 0;
            try {
                montant = Double.parseDouble(montantStr);
                if (montant <= 0) {
                    errorMontant.setText("Le montant doit être supérieur à zéro !");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                errorMontant.setText("Le montant doit être un nombre valide !");
                hasError = true;
            }

            if (hasError) return;

            if (selectedFile != null && selectedFile.exists()) {
                try {
                    File destinationDir = new File("uploads");
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                    }

                    File destination = new File(destinationDir, selectedFile.getName());
                    Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logo = selectedFile.getName();

                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'upload du logo : " + e.getMessage());
                    return;
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Logo manquant", "Veuillez sélectionner un logo via le bouton 'Parcourir'.");
                return;
            }


            Sponsor sponsor = new Sponsor();
            sponsor.setNom(nom);
            sponsor.setType(type);
            sponsor.setEmail(email);
            sponsor.setTelephone(telephone);
            sponsor.setSiteWeb(siteWeb);
            sponsor.setMontant(montant);
            sponsor.setLogo(logo);

            sponsorService.add(sponsor);

            System.out.println("✅ Sponsor ajouté avec succès : " + nom);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Sponsor ajouté avec succès !");

            try {
                Parent detailsRoot = FXMLLoader.load(getClass().getResource("/ListeSponsor.fxml"));
                Stage stage = (Stage) SponsorNom.getScene().getWindow();
                Scene detailsScene = new Scene(detailsRoot);
                stage.setScene(detailsScene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page des sponsors : " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    void handleParcourir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            SponsorLogo.setText(selectedFile.getName());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
