package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import services.ChatbotService;

public class ChatController {

    @FXML private VBox messagesBox;
    @FXML private TextField tfMessage;
    @FXML private Label lbStatus;

    private final ChatbotService bot = new ChatbotService();
    private volatile boolean busy = false;

    @FXML
    public void initialize() {
        addBot("Salut 👋 Je suis ton assistant MindTrack. Demande-moi des conseils d’habitudes, motivation, planning…");
        tfMessage.setOnAction(e -> send());
        lbStatus.setText("");
    }

    @FXML
    public void send() {
        if (busy) return;

        String msg = (tfMessage.getText() == null) ? "" : tfMessage.getText().trim();
        if (msg.isEmpty()) return;

        busy = true;
        tfMessage.clear();
        tfMessage.setDisable(true);

        addUser(msg);
        lbStatus.setText("Typing...");

        new Thread(() -> {
            try {
                String reply = bot.ask(msg);

                Platform.runLater(() -> addBot(reply));

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    addBot("❌ Erreur: " + ex.getMessage());
                });

            } finally {
                Platform.runLater(() -> {
                    lbStatus.setText("");
                    tfMessage.setDisable(false);
                    tfMessage.requestFocus();
                });
                busy = false;
            }
        }, "Chatbot-Thread").start();
    }

    private void addUser(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.getStyleClass().add("msg-user");

        HBox row = new HBox(l);
        row.setFillHeight(true);
        row.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(l, Priority.NEVER);
        row.setStyle("-fx-alignment: CENTER_RIGHT;");
        messagesBox.getChildren().add(row);
    }

    private void addBot(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.getStyleClass().add("msg-bot");

        HBox row = new HBox(l);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle("-fx-alignment: CENTER_LEFT;");
        messagesBox.getChildren().add(row);
    }
}