package controllers;

import entities.Habitude;
import entities.SuiviHabitude;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.HabitudeService;
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;

public class SuiviController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();

    @FXML private ComboBox<Habitude> cbHabitudes;
    @FXML private DatePicker dpDate;

    // UI boolean
    @FXML private CheckBox chkEtat;

    // UI quantitatif
    @FXML private Spinner<Integer> spValeur;

    @FXML private Label lbHint;

    @FXML private TableView<SuiviHabitude> tvHistorique;
    @FXML private TableColumn<SuiviHabitude, String> colDate;
    @FXML private TableColumn<SuiviHabitude, String> colValeur;
    @FXML private TableColumn<SuiviHabitude, String> colEtat;

    private final ObservableList<SuiviHabitude> hist = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpDate.setValue(LocalDate.now());

        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDate())));
        colValeur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getValeur())));
        colEtat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isEtat() ? "Done ✅" : "Miss"));

        tvHistorique.setItems(hist);

        // spinner default
        if (spValeur != null) {
            spValeur.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, 0));
        }

        loadHabitudes();

        cbHabitudes.setOnAction(e -> updateUiForType());
        updateUiForType();
    }

    private void loadHabitudes() {
        try {
            cbHabitudes.setItems(FXCollections.observableArrayList(habService.afficherParUser(Session.getIdU())));
            if (!cbHabitudes.getItems().isEmpty()) cbHabitudes.getSelectionModel().select(0);
            updateUiForType();
            chargerHistorique();
        } catch (SQLException e) {
            erreur("Habitudes", e.getMessage());
        }
    }

    private void updateUiForType() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) return;

        boolean isBool = h.isBooleanType();
        int target = Math.max(1, h.getTargetValue());
        String unit = (h.getUnit() == null) ? "" : h.getUnit().trim();

        if (chkEtat != null) chkEtat.setDisable(!isBool);
        if (spValeur != null) spValeur.setDisable(isBool);

        if (isBool) {
            lbHint.setText("Type BOOLEAN: coche Done/Not done.");
            if (spValeur != null && spValeur.getValueFactory() != null) {
                spValeur.getValueFactory().setValue(0);
            }
        } else {
            String u = unit.isBlank() ? ("COUNT".equalsIgnoreCase(h.getHabitType()) ? "x" : "min") : unit;
            lbHint.setText("Type " + h.getHabitType() + ": entre une valeur (objectif = " + target + " " + u + ").");
            if (spValeur != null) {
                spValeur.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 9999, target));
            }
        }
    }

    @FXML
    public void marquer() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Suivi", "Choisis une habitude."); return; }

        LocalDate d = (dpDate.getValue() == null) ? LocalDate.now() : dpDate.getValue();
        int target = Math.max(1, h.getTargetValue());

        try {
            if (h.isBooleanType()) {
                boolean done = chkEtat != null && chkEtat.isSelected();
                int valeur = done ? 1 : 0;
                suiviService.marquerValeur(h.getIdHabitude(), d, valeur, 1);
            } else {
                int valeur = (spValeur == null || spValeur.getValue() == null) ? 0 : spValeur.getValue();
                if (valeur < 0) valeur = 0;
                suiviService.marquerValeur(h.getIdHabitude(), d, valeur, target);
            }

            info("Suivi", "Enregistré ✅");
            chargerHistorique();
        } catch (SQLException e) {
            erreur("Suivi", e.getMessage());
        }
    }

    @FXML
    public void chargerHistorique() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Historique", "Choisis une habitude."); return; }

        try {
            hist.setAll(suiviService.historiqueParHabitude(h.getIdHabitude()));
        } catch (SQLException e) {
            erreur("Historique", e.getMessage());
        }
    }

    private void info(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        a.showAndWait();
    }
    private void erreur(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(t); a.setHeaderText("Erreur"); a.setContentText(m);
        a.showAndWait();
    }
}