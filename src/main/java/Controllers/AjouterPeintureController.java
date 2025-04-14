package Controllers;

import Models.Peinture;
import Models.Style;
import Services.PeintureService;
import Services.StyleService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AjouterPeintureController {

    @FXML
    private TextField titreTextField;

    @FXML
    private DatePicker dateDeCreationTextField;

    @FXML
    private TextField tableauTextField;

    @FXML
    private ComboBox<String> styleComboBox;

    @FXML
    private Button parcourirBtn;
    @FXML
    private Label lbDcr;

    @FXML
    private Label lbStyle;

    @FXML
    private Label lbTab;

    @FXML
    private Label lbTitre;


    @FXML
    private Button validerButton;

    @FXML
    private ImageView imageView;

    private StyleService styleService;
    private PeintureService peintureService;

    private Peinture peintureToEdit;
    private boolean isEditMode = false;
    private String selectedImagePath = "";

    // Référence au contrôleur parent
    private AfficherPeintureController parentController;

    public void setParentController(AfficherPeintureController parentController) {
        this.parentController = parentController;
    }

    // Appelée par le contrôleur parent si modification
    public void setPeintureToEdit(Peinture peinture) {
        this.peintureToEdit = peinture;
        this.isEditMode = true;

        // Pré-remplir les champs
        titreTextField.setText(peinture.getTitre());
        dateDeCreationTextField.setValue(peinture.getDateCr());
        tableauTextField.setText(peinture.getTableau());
        selectedImagePath = peinture.getTableau();

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            Image image = new Image("file:" + selectedImagePath);
            imageView.setImage(image);
        }

        if (peinture.getStyle() != null) {
            styleComboBox.setValue(peinture.getStyle().getType());
        }

        validerButton.setText("Modifier");
    }

    @FXML
    private void initialize() {
        styleService = new StyleService();
        peintureService = new PeintureService();

        try {
            List<Style> styles = styleService.getAll();
            for (Style style : styles) {
                styleComboBox.getItems().add(style.getType());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleParcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) parcourirBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            tableauTextField.setText(selectedImagePath);
            imageView.setImage(new Image("file:" + selectedImagePath));
        }
    }

    @FXML
    private void handleValider() {
        String titre = titreTextField.getText();
        String tableau = tableauTextField.getText();
        String styleStr = styleComboBox.getValue();
        LocalDate dateDeCreation = dateDeCreationTextField.getValue();

        boolean valid = true;
        lbTitre.setText("");
        lbDcr.setText("");
        lbTab.setText("");
        lbStyle.setText("");

        // Validation du titre
        if (titre.isEmpty()) {
            lbTitre.setText("Titre requis.");
            valid = false;
        } else if (titre.trim().length() < 3) {
            lbTitre.setText("Le titre doit avoir au moins 3 caractères.");
            valid = false;
        } else if (!titre.matches("[a-zA-Z]+")) { // Vérification que le titre contient seulement des lettres
            lbTitre.setText("Le titre doit contenir uniquement des lettres.");
            valid = false;
        }

        // Validation de la date
        if (dateDeCreation == null) {
            lbDcr.setText("Date requise.");
            valid = false;
        } else if (dateDeCreation.isAfter(LocalDate.now())) {
            lbDcr.setText("La date ne peut pas être dans le futur.");
            valid = false;
        }

        // Validation de l'image
        if (tableau.isEmpty()) {
            lbTab.setText("Image requise.");
            valid = false;
        }

        // Validation du style
        if (styleStr == null) {
            lbStyle.setText("Style requis.");
            valid = false;
        }

        if (!valid) return; // Bloquer si une erreur est présente

        try {
            // Vérification du style sélectionné
            Style selectedStyle = styleService.getByType(styleStr);
            if (selectedStyle == null) {
                lbStyle.setText("Style non trouvé.");
                return;
            }

            // Création de la peinture
            Peinture peinture = new Peinture(titre, dateDeCreation, tableau, selectedStyle, 5);

            // Si c'est un mode édition, mettre à jour la peinture existante
            if (isEditMode && peintureToEdit != null) {
                peinture.setId(peintureToEdit.getId());
                peintureService.update(peinture);
            } else {
                // Sinon, ajouter une nouvelle peinture
                peintureService.add(peinture);
            }

            // Fermer la fenêtre
            Stage stage = (Stage) validerButton.getScene().getWindow();
            stage.close();

            // Rafraîchir la liste dans le contrôleur parent
            if (parentController != null) {
                parentController.loadPeintures();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer l'erreur SQL si nécessaire
        }
    }

}
