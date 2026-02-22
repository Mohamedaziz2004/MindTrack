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
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class HabitudeController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();

    // ===== TOP SORT =====
    @FXML private ComboBox<String> cbSort;

    // ===== FORM =====
    @FXML private TextField tfNom;
    @FXML private TextField tfFrequence;
    @FXML private TextArea tfObjectif;

    // ===== CARDS GRID =====
    @FXML private FlowPane cardsContainer;

    private final ObservableList<Habitude> habitudes = FXCollections.observableArrayList();
    private Habitude selected;

    @FXML
    public void initialize() {
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

    // ================= LOAD =================

    @FXML
    public void loadHabitudes() {
        try {
            habitudes.setAll(habService.afficherParUser(Session.getIdU()));
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

        if (nom.isEmpty() || freq.isEmpty()) {
            showInfo("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        try {
            habService.ajouter(new Habitude(nom, freq, obj, Session.getIdU()));
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

        if (nom.isEmpty() || freq.isEmpty()) {
            showInfo("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        selected.setNom(nom);
        selected.setFrequence(freq);
        selected.setObjectif(obj);
        selected.setIdU(Session.getIdU());

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

    // ================= UI: CARDS =================

    private void renderCards() {
        cardsContainer.getChildren().clear();

        if (habitudes.isEmpty()) {
            Label empty = new Label("No habits yet. Add your first habit above.");
            empty.getStyleClass().add("hint");
            cardsContainer.getChildren().add(empty);
            return;
        }

        // Copier liste pour trier
        List<Habitude> list = new ArrayList<>(habitudes);

        String sort = (cbSort == null || cbSort.getValue() == null) ? "Newest" : cbSort.getValue();

        switch (sort) {
            case "Streak (High → Low)" ->
                    list.sort((a, b) -> Integer.compare(getStreakSafe(b), getStreakSafe(a)));

            case "Streak (Low → High)" ->
                    list.sort(Comparator.comparingInt(this::getStreakSafe));

            case "Name" ->
                    list.sort(Comparator.comparing(h -> h.getNom().toLowerCase()));

            default -> {
                // "Newest" -> ton service retourne déjà DESC par idHabitude
            }
        }

        for (Habitude h : list) {
            cardsContainer.getChildren().add(buildHabitCard(h));
        }
    }

    private VBox buildHabitCard(Habitude h) {
        VBox card = new VBox(10);
        card.getStyleClass().add("habit-card");
        card.setPadding(new Insets(14));
        card.setPrefWidth(330);

        // Header row: title + icons
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

        // Description
        Label desc = new Label(safe(h.getObjectif()));
        desc.getStyleClass().add("habit-desc");
        desc.setWrapText(true);

        // Progress (days + dots)
        VBox progressBox = new VBox(6);

        final LocalDate today = LocalDate.now();
        final int n = 7;
        final LocalDate start = today.minusDays(n - 1);

        // Days letters
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

        // États (oldest -> today)
        final List<Boolean> last7;
        try {
            last7 = suiviService.getEtatDerniersJours(h.getIdHabitude(), n);
        } catch (SQLException ex) {
            showError("Suivi", ex.getMessage());
            return card;
        }

        // Dots cliquables
        HBox dots = new HBox(6);
        dots.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < n; i++) {
            final int idx = i;
            final LocalDate dayFinal = start.plusDays(i);

            boolean done = last7.get(idx);

            Button dot = new Button(done ? "✓" : "○");
            dot.getStyleClass().add(done ? "dot-done-btn" : "dot-miss-btn");
            dot.setFocusTraversable(false);
            Tooltip.install(dot, new Tooltip(dayFinal.toString()));

            dot.setOnAction(ev -> {
                try {
                    boolean newEtat = !last7.get(idx);
                    suiviService.marquerCommeFaite(h.getIdHabitude(), dayFinal, newEtat);
                    loadHabitudes(); // refresh complet (couleurs + streak)
                } catch (SQLException e) {
                    showError("Suivi", e.getMessage());
                }
            });

            // éviter que cliquer sur dot sélectionne la card
            dot.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

            dots.getChildren().add(dot);
        }

        progressBox.getChildren().addAll(daysRow, dots);

        // ✅ Streak correct: consécutif jusqu'à today
        int streak = getStreakSafe(h);
        Label streakLabel = new Label(streak + " day streak");
        streakLabel.getStyleClass().add("habit-streak");

        // Click card = select
        card.setOnMouseClicked(e -> selectHabit(h));

        if (selected != null && selected.getIdHabitude() == h.getIdHabitude()) {
            card.getStyleClass().add("habit-card-selected");
        }

        card.getChildren().addAll(header, desc, progressBox, streakLabel);
        return card;
    }

    // ✅ streak safe (sans crash)
    private int getStreakSafe(Habitude h) {
        try {
            return suiviService.getStreak7Jours(h.getIdHabitude());
        } catch (SQLException e) {
            return 0;
        }
    }

    private void selectHabit(Habitude h) {
        selected = h;
        tfNom.setText(h.getNom());
        tfFrequence.setText(h.getFrequence());
        tfObjectif.setText(h.getObjectif());
        renderCards();
    }

    private void clearForm() {
        tfNom.clear();
        tfFrequence.clear();
        tfObjectif.clear();
        selected = null;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "No description." : s;
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