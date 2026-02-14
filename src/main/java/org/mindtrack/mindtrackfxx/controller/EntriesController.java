package org.mindtrack.mindtrackfxx.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.mindtrack.mindtrackfxx.model.MindTrackEntry;
import org.mindtrack.mindtrackfxx.service.MindTrackService;

public class EntriesController implements ServiceAware {
    @FXML private ListView<MindTrackEntry> entriesList;
    private MindTrackService service;

    @Override
    public void setService(MindTrackService service) {
        this.service = service;
        refresh();
    }

    private void refresh() {
        if (entriesList != null && service != null) {
            entriesList.setItems(FXCollections.observableArrayList(service.listEntries()));
        }
    }
}
