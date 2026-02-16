package controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import utils.Navigator;

public class MainController {
    @FXML private BorderPane root;

    @FXML
    public void initialize() {
        Navigator.setRoot(root);
        Navigator.loadCenter("/fxml/hab.fxml");
    }

    @FXML public void goHabitudes() { Navigator.loadCenter("/fxml/hab.fxml"); }
    @FXML public void goSuivi() { Navigator.loadCenter("/fxml/suivi.fxml"); }
    @FXML public void goStats() { Navigator.loadCenter("/fxml/stats.fxml"); }
    @FXML public void goRappels() { Navigator.loadCenter("/fxml/rappel.fxml"); }

    @FXML public void quitter() { System.exit(0); }
}
