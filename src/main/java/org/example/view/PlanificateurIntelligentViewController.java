package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PlanificateurIntelligentViewController implements Initializable {

    @FXML private Button backBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (backBtn != null) {
            backBtn.setOnAction(e -> goBackToMenu());
        }
    }

    @FXML
    private void goToObjectif(MouseEvent event) {
        loadView("/objectif-view.fxml");
    }

    @FXML
    private void goToMilestones(MouseEvent event) {
        loadView("/jalon-progression-view.fxml");
    }

    @FXML
    private void goToActions(MouseEvent event) {
        loadView("/plan-action-view.fxml");
    }

    private void goBackToMenu() {
        loadView("/menu-view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
