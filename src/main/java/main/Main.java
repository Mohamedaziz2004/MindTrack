package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

import org.opencv.core.Core;
import utils.FaceRecognitionUtil;

public class    Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.setTitle("MindTrack");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);

        byte[] face = FaceRecognitionUtil.captureFace();
        if (face != null) {
            System.out.println("Face captured: " + face.length + " bytes");
        } else {
            System.out.println("No face captured.");
        }

    }
}
