package Controllers;

import Models.Sponsor;
import Services.SponsorService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;

public class ModifierSponsor {

    @FXML private TextField nomField;
    @FXML private TextField typeField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField siteWebField;
    @FXML private TextField montantField;
    @FXML private ImageView logoImageView;
    @FXML private Label errorNom;
    @FXML private Label errorType;
    @FXML private Label errorEmail;
    @FXML private Label errorTelephone;
    @FXML private Label errorSiteWeb;
    @FXML private Label errorLogo;
    @FXML private Label errorMontant;

    private Sponsor sponsor;
    private File selectedFile;
    private final SponsorService sponsorService = new SponsorService();

    public ModifierSponsor() throws SQLException {
    }

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
        nomField.setText(sponsor.getNom());
        typeField.setText(sponsor.getType());
        emailField.setText(sponsor.getEmail());
        telephoneField.setText(sponsor.getTelephone());
        siteWebField.setText(sponsor.getSiteWeb());
        montantField.setText(String.valueOf(sponsor.getMontant()));

        File file = new File("uploads/" + sponsor.getLogo());
        if(file.exists()) {
            logoImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void choisirLogo() {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            logoImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }



    @FXML
    private void modifierSponsor() {
        boolean valid = true;

        // Réinitialiser les erreurs
        errorNom.setText("");
        errorType.setText("");
        errorEmail.setText("");
        errorTelephone.setText("");
        errorSiteWeb.setText("");
        errorLogo.setText("");
        errorMontant.setText("");

        if (nomField.getText().trim().isEmpty()) {
            errorNom.setText("Nom requis !");
            valid = false;
        }
        if (typeField.getText().trim().isEmpty()) {
            errorType.setText("Type requis !");
            valid = false;
        }
        if (!emailField.getText().matches("^(.+)@(.+)$")) {
            errorEmail.setText("Email invalide !");
            valid = false;
        }
        if (!telephoneField.getText().matches("\\d{8,15}")) {
            errorTelephone.setText("Téléphone invalide !");
            valid = false;
        }
        if (siteWebField.getText().trim().isEmpty()) {
            errorSiteWeb.setText("Site Web requis !");
            valid = false;
        }
        try {
            double montant = Double.parseDouble(montantField.getText());
            if (montant <= 0) {
                errorMontant.setText("Montant > 0 !");
                valid = false;
            }
        } catch (NumberFormatException e) {
            errorMontant.setText("Montant invalide !");
            valid = false;
        }

        if (!valid) return;

        sponsor.setNom(nomField.getText());
        sponsor.setType(typeField.getText());
        sponsor.setEmail(emailField.getText());
        sponsor.setTelephone(telephoneField.getText());
        sponsor.setSiteWeb(siteWebField.getText());
        sponsor.setMontant(Double.parseDouble(montantField.getText()));

        if (selectedFile != null) {
            sponsor.setLogo(selectedFile.getName());
        }

        try {
            sponsorService.update(sponsor);
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
