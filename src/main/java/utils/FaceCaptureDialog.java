package utils;

import controllers.FaceCaptureController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class FaceCaptureDialog {
    private FaceCaptureDialog() {
    }

    public static File captureFace(Window owner) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                FaceCaptureDialog.class.getResource("/fxml/face_capture.fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(FaceCaptureDialog.class.getResource("/css/style.css")).toExternalForm()
        );

        Stage stage = new Stage();
        stage.setTitle("Capture Face");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setScene(scene);

        FaceCaptureController controller = loader.getController();
        controller.setStage(stage);

        stage.showAndWait();
        return controller.getCapturedFile();
    }
}

