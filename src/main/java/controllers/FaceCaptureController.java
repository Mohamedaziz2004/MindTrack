package controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FaceCaptureController {

    @FXML
    private ImageView previewImage;

    @FXML
    private Label statusLabel;

    @FXML
    private Button captureButton;

    private Webcam webcam;
    private ScheduledExecutorService executor;
    private File capturedFile;
    private Stage stage;

    @FXML
    public void initialize() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            statusLabel.setText("No webcam detected.");
            captureButton.setDisable(true);
            return;
        }

        Dimension size = WebcamResolution.VGA.getSize();
        webcam.setViewSize(size);
        webcam.open();

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            if (webcam.isOpen()) {
                BufferedImage image = webcam.getImage();
                if (image != null) {
                    Image fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> previewImage.setImage(fxImage));
                }
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(event -> shutdown());
    }

    @FXML
    public void handleCapture() {
        if (webcam == null || !webcam.isOpen()) {
            statusLabel.setText("Webcam not available.");
            return;
        }
        try {
            BufferedImage image = webcam.getImage();
            if (image == null) {
                statusLabel.setText("No image captured.");
                return;
            }
            File tempFile = Files.createTempFile("mindtrack-face-", ".png").toFile();
            ImageIO.write(image, "png", tempFile);
            capturedFile = tempFile;
            statusLabel.setText("Face captured.");
            closeStage();
        } catch (IOException e) {
            statusLabel.setText("Capture failed.");
            e.printStackTrace();
        }
    }

    public File getCapturedFile() {
        return capturedFile;
    }

    private void closeStage() {
        if (stage != null) {
            stage.close();
        }
        shutdown();
    }

    private void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        if (webcam != null) {
            webcam.close();
            webcam = null;
        }
    }
}

