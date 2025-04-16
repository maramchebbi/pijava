package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class MenuController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    public void initialize() {
        contentPane.getStylesheets().add(getClass().getResource("/ss.css").toExternalForm());
    }

    public void goToAjouterPeinture() throws IOException {
        setContent("AjouterPeinture.fxml");
    }

    public void goToAfficherPeintures() throws IOException {
        setContent("AfficherPeinture.fxml");
    }

    public void goToAjouterStyle() throws IOException {
        setContent("AjouterStyle.fxml");
    }

    public void goToAfficherStyles() throws IOException {
        setContent("AfficherStyle.fxml");
    }

    private void setContent(String fxml) throws IOException {
        AnchorPane pane = FXMLLoader.load(getClass().getResource("/" + fxml));
        contentPane.getChildren().setAll(pane);
    }
}
