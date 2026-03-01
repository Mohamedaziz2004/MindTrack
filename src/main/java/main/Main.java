package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.OpenCVLoader;
import utils.WindowBarHelper;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        OpenCVLoader.ensureLoaded();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );
        Parent root = loader.load();
        Parent wrapped = WindowBarHelper.wrap(root, stage, true, false);

        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().setAll(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")))
        );
        stage.setWidth(1120);
        stage.setHeight(700);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
