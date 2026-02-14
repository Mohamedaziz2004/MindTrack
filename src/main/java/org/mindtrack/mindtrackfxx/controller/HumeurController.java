package org.mindtrack.mindtrackfxx.controller;

import entities.humeur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import servives.humeurService;

import java.time.LocalDate;

public class HumeurController implements ServiceAware {
    @FXML private ListView<humeur> listView;
    @FXML private ComboBox<String> moodBox;
    @FXML private Spinner<Integer> intensitySpinner;
    @FXML private Button addBtn;

    private final humeurService service = new humeurService();

    @FXML
    public void initialize() {
        moodBox.setItems(FXCollections.observableArrayList(humeur.MOOD_TYPES));
        moodBox.getSelectionModel().select("Neutral");
        intensitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 5));
        refresh();
    }

    @Override
    public void setService(org.mindtrack.mindtrackfxx.service.MindTrackService s) {
        // Not used here; using domain-specific service
    }

    @FXML
    public void onAdd() {
        String type = moodBox.getValue();
        int intensity = intensitySpinner.getValue();
        humeur h = new humeur(LocalDate.now(), type, intensity, 1);
        service.create(h);
        refresh();
    }

    @FXML
    public void onDeleteSelected() {
        humeur selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.delete(selected.getIdH());
            refresh();
        }
    }

    private void refresh() {
        listView.setItems(FXCollections.observableArrayList(service.readAll()));
    }
}
