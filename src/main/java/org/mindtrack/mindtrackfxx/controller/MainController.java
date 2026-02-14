package org.mindtrack.mindtrackfxx.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.mindtrack.mindtrackfxx.service.MindTrackService;

import java.io.IOException;

public class MainController {
    @FXML private BorderPane root;

    private final MindTrackService service = new MindTrackService();

    @FXML
    public void initialize() {
        // Default view
        showEntries();
    }

    @FXML
    public void showEntries() {
        loadCenter("entries-view.fxml");
    }

    @FXML
    public void showNewEntry() {
        loadCenter("new-entry-view.fxml");
    }

    @FXML
    public void showHumeurs() {
        loadCenter("humeur-view.fxml");
    }

    @FXML
    public void showJournal() {
        loadCenter("journal-view.fxml");
    }

    @FXML
    public void onExit() {
        Platform.exit();
    }

    private void loadCenter(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource("/org/mindtrack/mindtrackfxx/view/" + fxml));
            // Inject service if controller supports it
            Node content = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ServiceAware) {
                ((ServiceAware) controller).setService(service);
            }
            root.setCenter(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
