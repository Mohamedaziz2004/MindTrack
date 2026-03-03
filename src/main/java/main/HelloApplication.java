package main;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load the emotional journal view directly
        URL fxmlUrl = getClass().getResource("/FXML/emotional-journal-view.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find emotional-journal-view.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1100, 750);

        // Load stylesheet
        URL cssUrl = getClass().getResource("/styles/modern-style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Mind Track");
        stage.setScene(scene);
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove the default OS window bar

        // Add startup fade-in animation
        root.setOpacity(0);
        stage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
