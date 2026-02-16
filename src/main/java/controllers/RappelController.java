package controllers;

import entities.Habitude;
import entities.RappelHabitude;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.HabitudeService;
import services.RappelHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RappelController {

    private final HabitudeService habService = new HabitudeService();
    private final RappelHabitudeService rappelService = new RappelHabitudeService();

    @FXML private ComboBox<Habitude> cbHabitudes;
    @FXML private Spinner<Integer> spH, spM;
    @FXML private CheckBox chkActif;

    @FXML private CheckBox jLun, jMar, jMer, jJeu, jVen, jSam, jDim;
    @FXML private TextField tfMsg;

    @FXML private TableView<RappelHabitude> tvRappels;
    @FXML private TableColumn<RappelHabitude, String> colHeure, colJours, colMsg;
    @FXML private TableColumn<RappelHabitude, Boolean> colActif;

    private final ObservableList<RappelHabitude> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        spH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalTime.now().getHour()));
        spM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalTime.now().getMinute()));

        colHeure.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getHeureRappel())));
        colJours.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getJours()));
        colMsg.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessage()));
        colActif.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActif()).asObject());
        tvRappels.setItems(data);

        loadHabitudes();
        actifs();
    }

    private void loadHabitudes() {
        try {
            cbHabitudes.setItems(FXCollections.observableArrayList(habService.afficherParUser(Session.getIdU())));
        } catch (SQLException e) {
            erreur("Habitudes", e.getMessage());
        }
    }

    @FXML
    public void ajouter() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Rappel", "Choisis une habitude."); return; }

        String jours = buildJours();
        if (jours.isEmpty()) { info("Rappel", "Choisis au moins 1 jour."); return; }

        LocalTime heure = LocalTime.of(spH.getValue(), spM.getValue());
        String msg = tfMsg.getText().trim();
        if (msg.isEmpty()) msg = "Rappel : " + h.getNom();

        try {
            rappelService.ajouter(new RappelHabitude(h.getIdHabitude(), heure, jours, chkActif.isSelected(), msg));
            tfMsg.clear();
            actifs();
        } catch (SQLException e) {
            erreur("Ajout rappel", e.getMessage());
        }
    }

    @FXML
    public void actifs() {
        try {
            data.setAll(rappelService.rappelsActifsDuUser(Session.getIdU()));
        } catch (SQLException e) {
            erreur("Rappels actifs", e.getMessage());
        }
    }

    @FXML
    public void toggleActif() {
        RappelHabitude r = tvRappels.getSelectionModel().getSelectedItem();
        if (r == null) { info("Toggle", "Sélectionne un rappel."); return; }

        try {
            boolean nouveau = !r.isActif();
            rappelService.setActif(r.getIdRappel(), nouveau);
            actifs();
        } catch (SQLException e) {
            erreur("Toggle actif", e.getMessage());
        }
    }

    @FXML
    public void supprimer() {
        RappelHabitude r = tvRappels.getSelectionModel().getSelectedItem();
        if (r == null) { info("Suppression", "Sélectionne un rappel."); return; }

        if (!confirm("Suppression", "Supprimer ce rappel ?")) return;

        try {
            rappelService.supprimer(r.getIdRappel());
            actifs();
        } catch (SQLException e) {
            erreur("Suppression", e.getMessage());
        }
    }

    private String buildJours() {
        List<String> j = new ArrayList<>();
        if (jLun.isSelected()) j.add("Lun");
        if (jMar.isSelected()) j.add("Mar");
        if (jMer.isSelected()) j.add("Mer");
        if (jJeu.isSelected()) j.add("Jeu");
        if (jVen.isSelected()) j.add("Ven");
        if (jSam.isSelected()) j.add("Sam");
        if (jDim.isSelected()) j.add("Dim");
        return String.join(",", j);
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
    private boolean confirm(String t, String m) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
