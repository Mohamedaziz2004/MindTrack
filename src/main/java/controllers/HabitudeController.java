package controllers;

import entities.Habitude;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import services.HabitudeService;
import services.MotivationService;
import services.SmartReminderService;
import services.SuiviHabitudeService;
import services.WeatherService;
import utils.PdfExportUtil;
import utils.Session;
import utils.Toast;
import utils.AlertUtil;
import utils.NotificationUtil;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class HabitudeController {

    // ===================== SERVICES =====================
    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();
    private final SmartReminderService smartReminderService = new SmartReminderService();
    private final MotivationService motivationService = new MotivationService();
    private final WeatherService weatherService = new WeatherService();

    // ===================== SORT + SEARCH =====================
    @FXML private ComboBox<String> cbSort;
    @FXML private TextField tfSearch;

    // ===================== QUOTE + WEATHER =====================
    @FXML private Label lbQuote;
    @FXML private Label lbQuoteAuthor;
    @FXML private Label lbWeather;
    @FXML private Label lbWeatherCoach;

    // ===================== FORM =====================
    @FXML private TextField tfNom;
    @FXML private TextField tfFrequence;
    @FXML private TextArea tfObjectif;

    // ===================== METIER B (quantitatif) =====================
    @FXML private ComboBox<String> cbType;
    @FXML private Spinner<Integer> spTarget;
    @FXML private TextField tfUnit;

    // ===================== CARDS GRID =====================
    @FXML private FlowPane cardsContainer;

    private final ObservableList<Habitude> habitudes = FXCollections.observableArrayList();
    private Habitude selected;

    // ===================== INIT =====================
    @FXML
    public void initialize() {

        if (cbType != null) {
            cbType.setItems(FXCollections.observableArrayList("BOOLEAN", "COUNT", "TIME"));
            cbType.getSelectionModel().select("BOOLEAN");
            cbType.setOnAction(e -> updateTargetUi());
        }

        if (spTarget != null) {
            spTarget.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        }

        updateTargetUi();

        if (cbSort != null) {
            cbSort.setItems(FXCollections.observableArrayList(
                    "Newest",
                    "Streak (High → Low)",
                    "Streak (Low → High)",
                    "Name (A → Z)"
            ));
            cbSort.getSelectionModel().select("Streak (High → Low)");
            cbSort.setOnAction(e -> renderCards());
        }

        if (tfSearch != null) {
            tfSearch.textProperty().addListener((obs, o, n) -> renderCards());
        }

        loadHabitudes();
        Toast.show(cardsContainer, "✅ Habitudes chargées !");
        Toast.show(cardsContainer, "✅ Enregistré !");
        AlertUtil.info("MindTrack", "Habitudes chargées : " + habitudes.size());
        smartReminderService.evaluateAll(habitudes);
        NotificationUtil.showToast(
                NotificationUtil.stageFromAnyNode(cardsContainer),
                "✅ Habitudes chargées: " + habitudes.size()
        );
        loadQuoteAndWeather();
        Toast.show(cardsContainer, "Bienvenue sur MindTrack 👋");
    }

    private void updateTargetUi() {
        if (cbType == null || spTarget == null || tfUnit == null) return;

        String t = cbType.getValue();
        boolean isBool = (t == null) || "BOOLEAN".equalsIgnoreCase(t);

        spTarget.setDisable(isBool);
        tfUnit.setDisable(isBool);

        if (isBool) {
            if (spTarget.getValueFactory() != null) spTarget.getValueFactory().setValue(1);
            tfUnit.setText("");
        } else {
            if (tfUnit.getText().isBlank()) {
                tfUnit.setText("COUNT".equalsIgnoreCase(t) ? "x" : "min");
            }
        }
    }

    // ===================== LOAD =====================
    @FXML
    public void loadHabitudes() {
        Integer keepSelected = (selected == null) ? null : selected.getIdHabitude();

        try {
            habitudes.setAll(habService.afficherParUser(Session.getIdU()));

            // auto smart evaluation
            smartReminderService.evaluateAll(habitudes);

            if (keepSelected != null) {
                selected = null;
                for (Habitude h : habitudes) {
                    if (h.getIdHabitude() == keepSelected) {
                        selected = h;
                        break;
                    }
                }
            }

            renderCards();
        } catch (SQLException e) {
            showError("Chargement", e.getMessage());
        }
    }

    // ===================== CRUD =====================
    @FXML
    public void ajouterHabitude() {
        String nom = tfNom.getText().trim();
        String freq = tfFrequence.getText().trim();
        String obj = tfObjectif.getText().trim();

        String type = (cbType == null || cbType.getValue() == null) ? "BOOLEAN" : cbType.getValue();
        int target = (spTarget == null || spTarget.getValue() == null) ? 1 : spTarget.getValue();
        String unit = (tfUnit == null) ? "" : tfUnit.getText().trim();

        if (nom.isEmpty() || freq.isEmpty()) {
            showInfo("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        if (!"BOOLEAN".equalsIgnoreCase(type) && target <= 0) {
            showInfo("Validation", "Target doit être > 0.");
            return;
        }

        if ("BOOLEAN".equalsIgnoreCase(type)) {
            target = 1;
            unit = "";
        }

        try {
            habService.ajouter(new Habitude(nom, freq, obj, Session.getIdU(), type, target, unit));
            clearForm();
            loadHabitudes();
        } catch (SQLException e) {
            showError("Ajout", e.getMessage());
        }
    }

    @FXML
    public void modifierHabitude() {
        if (selected == null) {
            showInfo("Sélection", "Clique sur une card d’habitude d’abord.");
            return;
        }

        String nom = tfNom.getText().trim();
        String freq = tfFrequence.getText().trim();
        String obj = tfObjectif.getText().trim();

        String type = (cbType == null || cbType.getValue() == null) ? "BOOLEAN" : cbType.getValue();
        int target = (spTarget == null || spTarget.getValue() == null) ? 1 : spTarget.getValue();
        String unit = (tfUnit == null) ? "" : tfUnit.getText().trim();

        if (nom.isEmpty() || freq.isEmpty()) {
            showInfo("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        if (!"BOOLEAN".equalsIgnoreCase(type) && target <= 0) {
            showInfo("Validation", "Target doit être > 0.");
            return;
        }

        if ("BOOLEAN".equalsIgnoreCase(type)) {
            target = 1;
            unit = "";
        }

        selected.setNom(nom);
        selected.setFrequence(freq);
        selected.setObjectif(obj);
        selected.setIdU(Session.getIdU());
        selected.setHabitType(type);
        selected.setTargetValue(target);
        selected.setUnit(unit);

        try {
            habService.modifier(selected);
            clearForm();
            loadHabitudes();
        } catch (SQLException e) {
            showError("Modification", e.getMessage());
        }
    }

    @FXML
    public void supprimerHabitude() {
        if (selected == null) {
            showInfo("Sélection", "Clique sur une card d’habitude d’abord.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer l’habitude : " + selected.getNom() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            habService.supprimer(selected.getIdHabitude());
            clearForm();
            loadHabitudes();
        } catch (SQLException e) {
            showError("Suppression", e.getMessage());
        }
    }

    // ===================== PDF EXPORT =====================
    @FXML
    public void exportPdf() {
        try {
            String search = (tfSearch == null) ? "" : tfSearch.getText().trim();
            String sort = (cbSort == null || cbSort.getValue() == null) ? "Newest" : cbSort.getValue();

            // 1) base list
            List<Habitude> list = new ArrayList<>(habitudes);

            // 2) filter search
            if (!search.isBlank()) {
                String q = search.toLowerCase();
                list.removeIf(h ->
                        (h.getNom() == null || !h.getNom().toLowerCase().contains(q)) &&
                                (h.getFrequence() == null || !h.getFrequence().toLowerCase().contains(q)) &&
                                (h.getObjectif() == null || !h.getObjectif().toLowerCase().contains(q))
                );
            }

            // 3) sort (same as UI)
            switch (sort) {
                case "Streak (High -> Low)", "Streak (High → Low)" ->
                        list.sort((a, b) -> Integer.compare(calcDoneDays7(b), calcDoneDays7(a)));
                case "Streak (Low -> High)", "Streak (Low → High)" ->
                        list.sort(Comparator.comparingInt(this::calcDoneDays7));
                case "Name (A -> Z)", "Name (A → Z)" ->
                        list.sort(Comparator.comparing(h -> (h.getNom() == null ? "" : h.getNom().toLowerCase())));
                default -> { /* Newest */ }
            }

            // 4) build rows with REAL progress + score
            List<PdfExportUtil.HabitRow> rows = new ArrayList<>();
            for (Habitude h : list) {

                List<Integer> values;
                try {
                    values = suiviService.getValeursDerniersJours(h.getIdHabitude(), 7);
                } catch (SQLException e) {
                    values = Collections.nCopies(7, 0);
                }

                int target = Math.max(1, h.getTargetValue());
                int doneDays = 0;
                int cappedSum = 0;

                for (int v : values) {
                    if (h.isBooleanType()) {
                        if (v >= 1) doneDays++;
                        cappedSum += (v >= 1 ? 1 : 0);
                    } else {
                        if (v >= target) doneDays++;
                        cappedSum += Math.min(Math.max(v, 0), target);
                    }
                }

                String progress = doneDays + " / 7 days";
                int denom = h.isBooleanType() ? 7 : (target * 7);
                int scorePct = denom == 0 ? 0 : (int) Math.round((cappedSum * 100.0) / denom);
                String score = scorePct + "%";

                rows.add(new PdfExportUtil.HabitRow(
                        h.getNom(),
                        h.getFrequence(),
                        (h.targetLabel() == null ? "" : h.targetLabel()),
                        progress,
                        score
                ));
            }

            // 5) choose file
            FileChooser fc = new FileChooser();
            fc.setTitle("Export Habits PDF");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            var file = fc.showSaveDialog(cardsContainer.getScene().getWindow());
            if (file == null) return;

            // 6) export
            PdfExportUtil.exportHabitsPdf(file, rows, search, sort);
            showInfo("PDF", "Exported successfully:\n" + file.getAbsolutePath());

        } catch (Exception e) {
            showError("PDF", e.getMessage());
        }
    }

    private int calcDoneDays7(Habitude h) {
        try {
            List<Integer> values = suiviService.getValeursDerniersJours(h.getIdHabitude(), 7);
            int target = Math.max(1, h.getTargetValue());
            int done = 0;

            for (int v : values) {
                if (h.isBooleanType()) {
                    if (v >= 1) done++;
                } else {
                    if (v >= target) done++;
                }
            }
            return done;
        } catch (Exception e) {
            return 0;
        }
    }

    // ===================== UI RENDER =====================
    private void renderCards() {
        cardsContainer.getChildren().clear();

        List<HabitVM> vms = buildDisplayedVms();

        if (vms.isEmpty()) {
            Label empty = new Label((tfSearch != null && !tfSearch.getText().isBlank())
                    ? "No results. Try another search."
                    : "No habits yet. Add your first habit above.");
            empty.getStyleClass().add("hint");
            cardsContainer.getChildren().add(empty);
            return;
        }

        for (HabitVM vm : vms) {
            cardsContainer.getChildren().add(buildHabitCard(vm));
        }
    }

    private List<HabitVM> buildDisplayedVms() {
        List<Habitude> list = new ArrayList<>(habitudes);

        String q = (tfSearch == null || tfSearch.getText() == null) ? "" : tfSearch.getText().trim().toLowerCase();
        if (!q.isEmpty()) {
            list.removeIf(h -> {
                String nom = safeText(h.getNom()).toLowerCase();
                String freq = safeText(h.getFrequence()).toLowerCase();
                String obj = safeText(h.getObjectif()).toLowerCase();
                return !(nom.contains(q) || freq.contains(q) || obj.contains(q));
            });
        }

        List<HabitVM> vms = new ArrayList<>();
        for (Habitude h : list) {
            List<Integer> values7 = getWeekValuesSafe(h); // ✅ semaine courante
            int doneCount7 = countDoneFromValues(h, values7);
            vms.add(new HabitVM(h, values7, doneCount7));
        }

        String sort = (cbSort == null || cbSort.getValue() == null) ? "Newest" : cbSort.getValue();
        switch (sort) {
            case "Streak (High → Low)" -> vms.sort((a, b) -> Integer.compare(b.doneCount7, a.doneCount7));
            case "Streak (Low → High)" -> vms.sort(Comparator.comparingInt(a -> a.doneCount7));
            case "Name (A → Z)" -> vms.sort(Comparator.comparing(a -> safeText(a.h.getNom()).toLowerCase()));
            default -> { /* Newest */ }
        }

        return vms;
    }

    private VBox buildHabitCard(HabitVM vm) {
        Habitude h = vm.h;

        VBox card = new VBox(10);
        card.getStyleClass().add("habit-card");
        card.setPadding(new Insets(14));
        card.setPrefWidth(330);

        Label title = new Label(h.getNom());
        title.getStyleClass().add("habit-title");

        Button btnEdit = new Button("✎");
        btnEdit.getStyleClass().addAll("icon-btn-sm", "icon-edit");

        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().addAll("icon-btn-sm", "icon-delete");

        btnEdit.setOnAction(e -> {
            selectHabit(h);
            tfNom.requestFocus();
        });

        btnDelete.setOnAction(e -> {
            selectHabit(h);
            supprimerHabitude();
        });

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(8, title, spacer, btnEdit, btnDelete);
        header.setAlignment(Pos.CENTER_LEFT);

        Label chipFreq = new Label(safeChip(h.getFrequence()));
        chipFreq.getStyleClass().addAll("chip", "chip-soft");

        Label chipTarget = new Label(buildTargetLabel(h));
        chipTarget.getStyleClass().addAll("chip", "chip-outline");

        HBox meta = new HBox(8, chipFreq, chipTarget);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label desc = new Label(safe(h.getObjectif()));
        desc.getStyleClass().add("habit-desc");
        desc.setWrapText(true);

        VBox progressBox = new VBox(6);

        final int n = 7;
        final LocalDate start = LocalDate.now().with(java.time.DayOfWeek.MONDAY); // ✅ semaine courante
        final int target = Math.max(1, h.getTargetValue());

        HBox daysRow = new HBox(12);
        daysRow.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < n; i++) {
            LocalDate day = start.plusDays(i);
            String letter = day.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.FRENCH)
                    .substring(0, 1)
                    .toUpperCase();

            Label d = new Label(letter);
            d.getStyleClass().add("day-letter");
            daysRow.getChildren().add(d);
        }

        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < n; i++) {
            final int idx = i;
            final LocalDate dayFinal = start.plusDays(i);

            int val = vm.values7.get(idx);

            boolean done = isDoneValue(h, val, target);
            boolean partial = !isBooleanType(h) && val > 0 && val < target;

            String text = done ? "✓" : (partial ? (val <= 9 ? String.valueOf(val) : "•") : "○");

            Button dot = new Button(text);
            dot.setFocusTraversable(false);

            if (done) dot.getStyleClass().add("dot-done-btn");
            else if (partial) dot.getStyleClass().add("dot-partial-btn");
            else dot.getStyleClass().add("dot-miss-btn");

            dot.setOnAction(ev -> {
                try {
                    if (isBooleanType(h)) {
                        int newVal = (vm.values7.get(idx) >= 1) ? 0 : 1;
                        suiviService.marquerValeur(h.getIdHabitude(), dayFinal, newVal, 1);
                    } else {
                        Integer userVal = askValue(h, dayFinal, vm.values7.get(idx), target);
                        if (userVal == null) return;
                        suiviService.marquerValeur(h.getIdHabitude(), dayFinal, userVal, target);
                    }

                    smartReminderService.evaluateHabit(h);
                    try {
                        int misses = suiviService.getConsecutiveMisses(h.getIdHabitude(), 7);
                        if (misses >= 3) {
                            NotificationUtil.showToast(
                                    NotificationUtil.stageFromAnyNode(cardsContainer),
                                    "⚠️ " + h.getNom() + " : " + misses + " jours sans faire. Petit rappel 💪"
                            );
                        }
                    } catch (Exception ignored) {}
                    loadHabitudes();
                } catch (SQLException e) {
                    showError("Suivi", e.getMessage());
                }
            });

            dot.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
            dots.getChildren().add(dot);
        }

        progressBox.getChildren().addAll(daysRow, dots);

        // ✅ label demandé
        Label streakLabel = new Label(vm.doneCount7 + " / 7 days (this week)");
        streakLabel.getStyleClass().add("habit-streak");

        card.setOnMouseClicked(e -> selectHabit(h));
        if (selected != null && selected.getIdHabitude() == h.getIdHabitude()) {
            card.getStyleClass().add("habit-card-selected");
        }

        card.getChildren().addAll(header, meta, desc, progressBox, streakLabel);
        return card;
    }

    private Integer askValue(Habitude h, LocalDate date, int current, int target) {
        TextInputDialog d = new TextInputDialog(String.valueOf(current));
        d.setTitle("Daily value");
        d.setHeaderText(h.getNom() + " — " + date);
        String u = (h.getUnit() == null || h.getUnit().isBlank()) ? "" : (" " + h.getUnit());
        d.setContentText("Enter value (0.." + target + u + "):");

        Optional<String> r = d.showAndWait();
        if (r.isEmpty()) return null;

        try {
            int v = Integer.parseInt(r.get().trim());
            if (v < 0) v = 0;
            return v;
        } catch (Exception ex) {
            showInfo("Value", "Please enter a valid integer.");
            return null;
        }
    }

    private void selectHabit(Habitude h) {
        selected = h;

        tfNom.setText(h.getNom());
        tfFrequence.setText(h.getFrequence());
        tfObjectif.setText(h.getObjectif());

        if (cbType != null) cbType.getSelectionModel().select(safeText(h.getHabitType()));

        if (spTarget != null && spTarget.getValueFactory() != null)
            spTarget.getValueFactory().setValue(Math.max(1, h.getTargetValue()));

        if (tfUnit != null) tfUnit.setText(safeText(h.getUnit()));

        updateTargetUi();
        renderCards();
    }

    private void clearForm() {
        tfNom.clear();
        tfFrequence.clear();
        tfObjectif.clear();

        if (cbType != null) cbType.getSelectionModel().select("BOOLEAN");
        if (spTarget != null && spTarget.getValueFactory() != null) spTarget.getValueFactory().setValue(1);
        if (tfUnit != null) tfUnit.clear();

        selected = null;
        updateTargetUi();
    }

    // ===================== DONE LOGIC =====================
    private boolean isBooleanType(Habitude h) {
        String t = h.getHabitType();
        return (t == null || t.isBlank() || "BOOLEAN".equalsIgnoreCase(t));
    }

    private boolean isDoneValue(Habitude h, int val, int target) {
        if (isBooleanType(h)) return val >= 1;
        return val >= target;
    }

    private int countDoneFromValues(Habitude h, List<Integer> values) {
        int target = Math.max(1, h.getTargetValue());
        int c = 0;
        for (int v : values) {
            if (isDoneValue(h, v, target)) c++;
        }
        return c;
    }

    // ✅ semaine courante (reset automatique chaque lundi)
    private List<Integer> getWeekValuesSafe(Habitude h) {
        try {
            List<Integer> v = suiviService.getValeursSemaineCourante(h.getIdHabitude());
            if (v == null || v.size() != 7) return Collections.nCopies(7, 0);
            return v;
        } catch (Exception e) {
            return Collections.nCopies(7, 0);
        }
    }

    // ===================== PDF HELPERS =====================
    private String buildTargetLabel(Habitude h) {
        String type = safeText(h.getHabitType());
        if (type.isBlank() || "BOOLEAN".equalsIgnoreCase(type)) return "BOOLEAN";

        int target = Math.max(1, h.getTargetValue());
        String unit = safeText(h.getUnit()).trim();
        if (unit.isBlank()) unit = "COUNT".equalsIgnoreCase(type) ? "x" : "min";

        return type.toUpperCase() + " • " + target + " " + unit;
    }

    // ===================== SAFE TEXT =====================
    private String safe(String s) {
        return (s == null || s.isBlank()) ? "No description." : s;
    }

    private String safeChip(String s) {
        return (s == null || s.isBlank()) ? "No frequency" : s.trim();
    }

    private String safeText(String s) {
        return (s == null) ? "" : s;
    }

    // ===================== ALERTS =====================
    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText("Erreur");
        a.setContentText(msg);
        a.showAndWait();
    }

    // ===================== VIEW MODEL =====================
    private static class HabitVM {
        final Habitude h;
        final List<Integer> values7;
        final int doneCount7;

        HabitVM(Habitude h, List<Integer> values7, int doneCount7) {
            this.h = h;
            this.values7 = values7;
            this.doneCount7 = doneCount7;
        }
    }

    // ===================== QUOTE + WEATHER =====================
    private void loadQuoteAndWeather() {

        if (lbQuote != null) lbQuote.setText("Loading quote...");
        if (lbQuoteAuthor != null) lbQuoteAuthor.setText("");

        new Thread(() -> {
            try {
                var q = motivationService.fetchRandomQuote();
                Platform.runLater(() -> {
                    if (lbQuote != null) lbQuote.setText("Citation du jour : “" + q.text + "”");
                    if (lbQuoteAuthor != null) lbQuoteAuthor.setText("— " + q.author);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lbQuote != null) lbQuote.setText("“Keep going. Small steps matter.”");
                    if (lbQuoteAuthor != null) lbQuoteAuthor.setText("— MindTrack");
                });
            }
        }, "Quote-Thread").start();

        if (lbWeather != null) lbWeather.setText("Loading weather...");
        if (lbWeatherCoach != null) lbWeatherCoach.setText("");

        new Thread(() -> {
            try {
                var w = weatherService.fetchCurrentWeather();
                String line = String.format("%.0f°C • %s • Wind %.0f km/h", w.temperature, w.label, w.windSpeed);
                String coach = weatherService.coachSuggestion(w);

                Platform.runLater(() -> {
                    if (lbWeather != null) lbWeather.setText(line);
                    if (lbWeatherCoach != null) lbWeatherCoach.setText(coach);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (lbWeather != null) lbWeather.setText("Weather unavailable");
                    if (lbWeatherCoach != null) lbWeatherCoach.setText("");
                });
            }
        }, "Weather-Thread").start();
    }
}