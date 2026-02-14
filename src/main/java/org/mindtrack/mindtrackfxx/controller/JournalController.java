package org.mindtrack.mindtrackfxx.controller;

import entities.JournalEmotionnel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import servives.JournalService;

import java.time.LocalDate;

public class JournalController implements ServiceAware {
    @FXML private ListView<JournalEmotionnel> listView;
    @FXML private TextArea noteArea;
    @FXML private Button addBtn;

    private final JournalService service = new JournalService();

    @FXML
    public void initialize() {
        refresh();
    }

    @Override
    public void setService(org.mindtrack.mindtrackfxx.service.MindTrackService s) {
        // Not used here; using domain-specific service
    }

    @FXML
    public void onAdd() {
        String note = noteArea.getText();
        if (note != null && !note.isBlank()) {
            JournalEmotionnel j = new JournalEmotionnel(note, LocalDate.now(), 1);
            service.create(j);
            noteArea.clear();
            refresh();
        }
    }

    @FXML
    public void onDeleteSelected() {
        JournalEmotionnel selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.delete(selected.getIdJournal());
            refresh();
        }
    }

    private void refresh() {
        listView.setItems(FXCollections.observableArrayList(service.readAll()));
    }
}
