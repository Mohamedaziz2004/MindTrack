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
    @FXML private CheckBox chkEtat;

    @FXML private TableView<SuiviHabitude> tvHistorique;
    @FXML private TableColumn<SuiviHabitude, String> colDate;
    @FXML private TableColumn<SuiviHabitude, String> colEtat;

    private final ObservableList<SuiviHabitude> hist = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpDate.setValue(LocalDate.now());

        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDate())));
        colEtat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isEtat() ? "Faite" : "Non faite"));
        tvHistorique.setItems(hist);

        loadHabitudes();
    }

    private void loadHabitudes() {
        try {
            cbHabitudes.setItems(FXCollections.observableArrayList(habService.afficherParUser(Session.getIdU())));
        } catch (SQLException e) {
            erreur("Habitudes", e.getMessage());
        }
    }

    @FXML
    public void marquer() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Suivi", "Choisis une habitude."); return; }

        LocalDate d = dpDate.getValue() == null ? LocalDate.now() : dpDate.getValue();
        boolean etat = chkEtat.isSelected();

        try {
            suiviService.marquerCommeFaite(h.getIdHabitude(), d, etat);
            info("Suivi", "Enregistré : " + (etat ? "Faite" : "Non faite"));
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
