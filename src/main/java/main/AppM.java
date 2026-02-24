package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppM extends Application {

    private SmartReminderRunner smartRunner;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load());

        scene.getStylesheets().add(getClass().getResource("/css/hab.css").toExternalForm());

        stage.setTitle("MindTrack");
        stage.setScene(scene);
        stage.show();

        // ✅ démarre smart reminders en background
        smartRunner = new SmartReminderRunner();
        smartRunner.start();
    }

    @Override
    public void stop() {
        if (smartRunner != null) smartRunner.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}