package controllers;

import entities.Habitude;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import services.HabitudeService;
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class StatsController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();

    @FXML private ComboBox<Habitude> cbHabitudes;
    @FXML private DatePicker dpMonth;

    @FXML private Label lbStreak, lbSemaine, lbMois, lbBestDay;
    @FXML private Label lbMonthSummary, lbConsistency;

    @FXML private ProgressBar pbMonth;
    @FXML private GridPane gpHeatmap;

    @FXML
    public void initialize() {
        dpMonth.setValue(LocalDate.now());
        reloadHabitudes();
    }

    private void reloadHabitudes() {
        try {
            cbHabitudes.setItems(FXCollections.observableArrayList(habService.afficherParUser(Session.getIdU())));
            if (!cbHabitudes.getItems().isEmpty()) cbHabitudes.getSelectionModel().select(0);
        } catch (SQLException e) {
            erreur("Habitudes", e.getMessage());
        }
    }

    @FXML
    public void thisMonth() {
        dpMonth.setValue(LocalDate.now());
        refresh();
    }

    @FXML
    public void refresh() {
        Habitude h = cbHabitudes.getValue();
        if (h == null) { info("Stats", "Choisis une habitude."); return; }

        try {
            // KPIs (utilise ton service)
            int streak = suiviService.getStreakActuel(h.getIdHabitude());
            double week = suiviService.getTauxReussiteSemaine(h.getIdHabitude());
            double month30 = suiviService.getTauxReussiteMois(h.getIdHabitude());

            lbStreak.setText(String.valueOf(streak));
            lbSemaine.setText(String.format("%.1f %%", week));
            lbMois.setText(String.format("%.1f %%", month30));

            // Heatmap month range
            LocalDate any = (dpMonth.getValue() == null) ? LocalDate.now() : dpMonth.getValue();
            LocalDate start = any.withDayOfMonth(1);
            LocalDate end = any.withDayOfMonth(any.lengthOfMonth());

            Map<LocalDate, Integer> valMap = suiviService.getValeurMapBetween(h.getIdHabitude(), start, end);
            Map<LocalDate, Boolean> doneMap = suiviService.getEtatMapBetween(h.getIdHabitude(), start, end);

            buildHeatmap(h, start, end, valMap, doneMap);

            // month summary + consistency
            int totalDays = end.getDayOfMonth();
            int doneDays = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (doneMap.getOrDefault(d, false)) doneDays++;
            }

            lbMonthSummary.setText(doneDays + " / " + totalDays + " days done");
            double consistency = (totalDays == 0) ? 0.0 : (doneDays * 1.0 / totalDays);
            pbMonth.setProgress(consistency);
            lbConsistency.setText(String.format("%.0f %%", consistency * 100));

            // Best day of week
            lbBestDay.setText(bestDayOfWeek(doneMap, start, end));

        } catch (SQLException e) {
            erreur("Stats", e.getMessage());
        }
    }

    private void buildHeatmap(Habitude h,
                              LocalDate start, LocalDate end,
                              Map<LocalDate, Integer> valMap,
                              Map<LocalDate, Boolean> doneMap) {

        gpHeatmap.getChildren().clear();

        // headers (Mon..Sun)
        String[] headers = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (int c = 0; c < 7; c++) {
            Label lab = new Label(headers[c]);
            lab.getStyleClass().add("heat-header");
            gpHeatmap.add(lab, c, 0);
        }

        int target = Math.max(1, h.getTargetValue());
        boolean isBool = h.isBooleanType();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int col = d.getDayOfWeek().getValue() - 1; // Mon=0..Sun=6
            int row = 1 + (int)((start.getDayOfWeek().getValue() - 1 + (d.getDayOfMonth() - 1)) / 7);

            int val = valMap.getOrDefault(d, 0);

            double pct;
            if (isBool) pct = doneMap.getOrDefault(d, false) ? 1.0 : 0.0;
            else pct = Math.min(1.0, val / (double) target);

            Label cell = new Label("");
            cell.getStyleClass().add("heat-cell");
            cell.getStyleClass().add(heatClass(pct));

            String tip;
            if (isBool) {
                tip = d + " : " + (doneMap.getOrDefault(d, false) ? "Done ✅" : "Miss");
            } else {
                String u = (h.getUnit() == null || h.getUnit().isBlank()) ? "" : (" " + h.getUnit());
                tip = d + " : " + val + "/" + target + u;
            }
            Tooltip.install(cell, new Tooltip(tip));

            gpHeatmap.add(cell, col, row);
        }
    }

    private String heatClass(double pct) {
        if (pct >= 1.0) return "heat-3";
        if (pct >= 0.5) return "heat-2";
        if (pct > 0.0) return "heat-1";
        return "heat-0";
    }

    private String bestDayOfWeek(Map<LocalDate, Boolean> doneMap, LocalDate start, LocalDate end) {
        Map<DayOfWeek, Integer> counts = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek d : DayOfWeek.values()) counts.put(d, 0);

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (doneMap.getOrDefault(d, false)) {
                counts.put(d.getDayOfWeek(), counts.get(d.getDayOfWeek()) + 1);
            }
        }

        DayOfWeek best = DayOfWeek.MONDAY;
        int bestVal = -1;
        for (var e : counts.entrySet()) {
            if (e.getValue() > bestVal) {
                bestVal = e.getValue();
                best = e.getKey();
            }
        }

        String name = best.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return name + " (" + bestVal + ")";
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