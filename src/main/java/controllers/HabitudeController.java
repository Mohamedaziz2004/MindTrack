package controllers;

import entities.Habitude;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import services.HabitudeService;
import services.SmartReminderService;
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class HabitudeController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();
    private final SmartReminderService smartReminderService = new SmartReminderService();

    // ===== SORT =====
    @FXML private ComboBox<String> cbSort;

    // ===== FORM =====
    @FXML private TextField tfNom;
    @FXML private TextField tfFrequence;
    @FXML private TextArea tfObjectif;

    // ✅ NEW form (métier B)
    @FXML private ComboBox<String> cbType;
    @FXML private Spinner<Integer> spTarget;
    @FXML private TextField tfUnit;

    // ===== CARDS GRID =====
    @FXML private FlowPane cardsContainer;

    private final ObservableList<Habitude> habitudes = FXCollections.observableArrayList();
    private Habitude selected;

    @FXML
    public void initialize() {

        // type choices
        if (cbType != null) {
            cbType.setItems(FXCollections.observableArrayList("BOOLEAN", "COUNT", "TIME"));
            cbType.getSelectionModel().select("BOOLEAN");
            cbType.setOnAction(e -> updateTargetUi());
        }

        // target spinner
        if (spTarget != null) {
            spTarget.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));
        }

        updateTargetUi();

        // sort choices
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

        loadHabitudes();
    }

    private void updateTargetUi() {
        if (cbType == null || spTarget == null || tfUnit == null) return;
        String t = cbType.getValue();
        boolean isBool = "BOOLEAN".equalsIgnoreCase(t);

        spTarget.setDisable(isBool);
        tfUnit.setDisable(isBool);

        if (isBool) {
            spTarget.getValueFactory().setValue(1);
            tfUnit.setText("");
        } else {
            if (tfUnit.getText().isBlank()) {
                tfUnit.setText("COUNT".equalsIgnoreCase(t) ? "x" : "min");
            }
        }
    }

    // ================= LOAD =================

    @FXML
    public void loadHabitudes() {
        Integer keepSelected = (selected == null) ? null : selected.getIdHabitude();

        try {
            habitudes.setAll(habService.afficherParUser(Session.getIdU()));

            // ✅ smart reminders auto check
            smartReminderService.evaluateAll(habitudes);

            // restore selection
            if (keepSelected != null) {
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

    // ================= CRUD =================

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

    // ================= UI: CARDS + TRI =================

    private void renderCards() {
        cardsContainer.getChildren().clear();

        if (habitudes.isEmpty()) {
            Label empty = new Label("No habits yet. Add your first habit above.");
            empty.getStyleClass().add("hint");
            cardsContainer.getChildren().add(empty);
            return;
        }

        List<Habitude> list = new ArrayList<>(habitudes);
        String sort = (cbSort == null || cbSort.getValue() == null) ? "Newest" : cbSort.getValue();

        switch (sort) {
            case "Streak (High → Low)" ->
                    list.sort((a, b) -> Integer.compare(getStreak7Safe(b), getStreak7Safe(a)));
            case "Streak (Low → High)" ->
                    list.sort(Comparator.comparingInt(this::getStreak7Safe));
            case "Name (A → Z)" ->
                    list.sort(Comparator.comparing(h -> h.getNom().toLowerCase()));
            default -> { /* Newest déjà desc */ }
        }

        for (Habitude h : list) {
            cardsContainer.getChildren().add(buildHabitCard(h));
        }
    }

    private int getStreak7Safe(Habitude h) {
        try {
            return suiviService.countDoneDerniersJours(h.getIdHabitude(), 7);
        } catch (SQLException e) {
            return 0;
        }
    }

    private VBox buildHabitCard(Habitude h) {
        VBox card = new VBox(10);
        card.getStyleClass().add("habit-card");
        card.setPadding(new Insets(14));
        card.setPrefWidth(330);

        // Header
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

        // Meta chips
        Label chipFreq = new Label(safeChip(h.getFrequence()));
        chipFreq.getStyleClass().addAll("chip", "chip-soft");

        Label chipTarget = new Label(h.targetLabel());
        chipTarget.getStyleClass().addAll("chip", "chip-outline");

        HBox meta = new HBox(8, chipFreq, chipTarget);
        meta.setAlignment(Pos.CENTER_LEFT);

        // Description
        Label desc = new Label(safe(h.getObjectif()));
        desc.getStyleClass().add("habit-desc");
        desc.setWrapText(true);

        // 7 jours
        VBox progressBox = new VBox(6);

        final LocalDate today = LocalDate.now();
        final int n = 7;
        final LocalDate start = today.minusDays(n - 1);

        // day letters
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

        // valeurs 7 jours
        final List<Integer> values;
        try {
            values = suiviService.getValeursDerniersJours(h.getIdHabitude(), n);
        } catch (SQLException e) {
            showError("Suivi", e.getMessage());
            return card;
        }

        final int target = Math.max(1, h.getTargetValue());

        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < n; i++) {
            final int idx = i;
            final LocalDate dayFinal = start.plusDays(i);

            int val = values.get(idx);

            boolean done = h.isBooleanType() ? (val >= 1) : (val >= target);
            boolean partial = !h.isBooleanType() && val > 0 && val < target;

            String text = done ? "✓" : (partial ? (val <= 9 ? String.valueOf(val) : "•") : "○");

            Button dot = new Button(text);
            dot.setFocusTraversable(false);

            if (done) dot.getStyleClass().add("dot-done-btn");
            else if (partial) dot.getStyleClass().add("dot-partial-btn");
            else dot.getStyleClass().add("dot-miss-btn");

            String tip;
            if (h.isBooleanType()) {
                tip = dayFinal + " : " + (done ? "Done" : "Not done");
            } else {
                String u = (h.getUnit() == null || h.getUnit().isBlank()) ? "" : (" " + h.getUnit());
                tip = dayFinal + " : " + val + "/" + target + u + (done ? " ✅" : "");
            }
            Tooltip.install(dot, new Tooltip(tip));

            dot.setOnAction(ev -> {
                try {
                    if (h.isBooleanType()) {
                        int newVal = (values.get(idx) >= 1) ? 0 : 1;
                        suiviService.marquerValeur(h.getIdHabitude(), dayFinal, newVal, 1);
                    } else {
                        Integer userVal = askValue(h, dayFinal, values.get(idx), target);
                        if (userVal == null) return;
                        suiviService.marquerValeur(h.getIdHabitude(), dayFinal, userVal, target);
                    }

                    // smart reminder auto update
                    smartReminderService.evaluateHabit(h);

                    loadHabitudes();
                } catch (SQLException e) {
                    showError("Suivi", e.getMessage());
                }
            });

            // éviter sélection card via dot
            dot.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            dots.getChildren().add(dot);
        }

        progressBox.getChildren().addAll(daysRow, dots);

        // "streak coché" = nb fait / 7
        int streak7 = getStreak7Safe(h);
        Label streakLabel = new Label(streak7 + " / 7 days");
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

        if (cbType != null) cbType.getSelectionModel().select(h.getHabitType());
        if (spTarget != null && spTarget.getValueFactory() != null) spTarget.getValueFactory().setValue(Math.max(1, h.getTargetValue()));
        if (tfUnit != null) tfUnit.setText(h.getUnit());

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

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "No description." : s;
    }

    private String safeChip(String s) {
        return (s == null || s.isBlank()) ? "No frequency" : s.trim();
    }

    // ================= Alerts =================

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
}