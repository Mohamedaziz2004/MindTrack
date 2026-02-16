package controllers;

import entities.Habitude;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.HabitudeService;
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;

public class StatsController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();

    @FXML private ComboBox<Habitude> cbHabitudes;
    @FXML private Label lbStreak, lbSemaine, lbMois;

    @FXML
    public void initialize() {
        reloadHabitudes();
    }

    private void reloadHabitudes() {
        try {
            cbHabitudes.setItems(FXCollections.observableArrayList(habService.afficherParUser(Session.getIdU())));
        } catch (SQLException e) {
            erreur("Habitudes", e.getMessage());
        }
    }

    @FXML
    public void calculer() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Stats", "Choisis une habitude."); return; }

        try {
            int streak = suiviService.getStreakActuel(h.getIdHabitude());
            double s = suiviService.getTauxReussiteSemaine(h.getIdHabitude());
            double m = suiviService.getTauxReussiteMois(h.getIdHabitude());

            lbStreak.setText(String.valueOf(streak));
            lbSemaine.setText(String.format("%.1f %%", s));
            lbMois.setText(String.format("%.1f %%", m));
        } catch (SQLException e) {
            erreur("Stats", e.getMessage());
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
