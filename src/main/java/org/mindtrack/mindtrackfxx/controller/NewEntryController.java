package org.mindtrack.mindtrackfxx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.mindtrack.mindtrackfxx.model.MindTrackEntry;
import org.mindtrack.mindtrackfxx.service.MindTrackService;

public class NewEntryController implements ServiceAware {
    @FXML private TextField titleField;
    @FXML private TextArea noteArea;
    @FXML private ComboBox<MindTrackEntry.Mood> moodBox;
    @FXML private Button addBtn;

    private MindTrackService service;

    @FXML
    public void initialize() {
        moodBox.getItems().setAll(MindTrackEntry.Mood.values());
        moodBox.getSelectionModel().select(MindTrackEntry.Mood.NEUTRAL);
    }

    @Override
    public void setService(MindTrackService service) {
        this.service = service;
    }

    @FXML
    public void onAdd() {
        String title = titleField.getText();
        String note = noteArea.getText();
        MindTrackEntry.Mood mood = moodBox.getValue();
        if (title != null && !title.isBlank()) {
            service.addEntry(title, note, mood);
            titleField.clear();
            noteArea.clear();
            moodBox.getSelectionModel().select(MindTrackEntry.Mood.NEUTRAL);
        }
    }
}
