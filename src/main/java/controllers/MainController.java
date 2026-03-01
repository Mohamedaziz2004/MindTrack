package controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import utils.Navigator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane root;
    @FXML private ImageView logoImage;

    @FXML
    public void initialize() {

        // ✅ Navigator
        Navigator.setRoot(root);
        Navigator.loadCenter("/fxml/hab.fxml");

        // ✅ Debug: vérifier injection FXML
        System.out.println("logoImage injected? " + (logoImage != null));

        var url = getClass().getResource("/images/logo.png");
        System.out.println("logo url = " + url);

        if (logoImage != null && url != null) {
            logoImage.setImage(new Image(url.toExternalForm()));
            logoImage.setVisible(true);
            logoImage.setManaged(true);
        }
    }
    @FXML
    public void openChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
            Scene scene = new Scene(loader.load());

            Stage st = new Stage();
            st.setTitle("Chatbot");
            st.setScene(scene);
            st.setResizable(false);
            st.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML public void goHabitudes() { Navigator.loadCenter("/fxml/hab.fxml"); }
    @FXML public void goSuivi() { Navigator.loadCenter("/fxml/suivi.fxml"); }
    @FXML public void goStats() { Navigator.loadCenter("/fxml/stats.fxml"); }
    @FXML public void goRappels() { Navigator.loadCenter("/fxml/rappel.fxml"); }

    @FXML public void quitter() { System.exit(0); }
}