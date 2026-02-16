package controllers;

import entities.Habitude;
import entities.RappelHabitude;
import entities.SuiviHabitude;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.HabitudeService;
import services.RappelHabitudeService;
import services.SuiviHabitudeService;
import utils.Session;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HabitudeController {

    private final HabitudeService habService = new HabitudeService();
    private final SuiviHabitudeService suiviService = new SuiviHabitudeService();
    private final RappelHabitudeService rappelService = new RappelHabitudeService();

    // ===== Habitudes CRUD
    @FXML private TableView<Habitude> tvHabitudes;
    @FXML private TableColumn<Habitude, String> colNom;
    @FXML private TableColumn<Habitude, String> colFrequence;
    @FXML private TableColumn<Habitude, String> colObjectif;
    @FXML private TextField tfNom, tfFrequence, tfObjectif;

    private final ObservableList<Habitude> habitudes = FXCollections.observableArrayList();

    // ===== Suivi / Historique
    @FXML private DatePicker dpDate;
    @FXML private CheckBox chkEtat;
    @FXML private TableView<SuiviHabitude> tvHistorique;
    @FXML private TableColumn<SuiviHabitude, String> colDate;
    @FXML private TableColumn<SuiviHabitude, String> colEtat;

    private final ObservableList<SuiviHabitude> historique = FXCollections.observableArrayList();

    // ===== Stats
    @FXML private Label lbStreak, lbSemaine, lbMois;

    // ===== Rappels
    @FXML private Spinner<Integer> spH, spM;
    @FXML private CheckBox chkActifRappel;
    @FXML private TextField tfMsg;
    @FXML private CheckBox jLun, jMar, jMer, jJeu, jVen, jSam, jDim;

    @FXML private TableView<RappelHabitude> tvRappels;
    @FXML private TableColumn<RappelHabitude, String> colHeureR;
    @FXML private TableColumn<RappelHabitude, String> colJoursR;
    @FXML private TableColumn<RappelHabitude, String> colMsgR;
    @FXML private TableColumn<RappelHabitude, Boolean> colActifR;

    private final ObservableList<RappelHabitude> rappels = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // Habitudes table
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colFrequence.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFrequence()));
        colObjectif.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getObjectif()));
        tvHabitudes.setItems(habitudes);

        tvHabitudes.getSelectionModel().selectedItemProperty().addListener((obs, old, h) -> {
            if (h != null) {
                tfNom.setText(h.getNom());
                tfFrequence.setText(h.getFrequence());
                tfObjectif.setText(h.getObjectif());
                dpDate.setValue(LocalDate.now());
            }
        });

        // Historique table
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getDate())));
        colEtat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isEtat() ? "Faite" : "Non faite"));
        tvHistorique.setItems(historique);

        dpDate.setValue(LocalDate.now());

        // Rappels table + spinners
        spH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, LocalTime.now().getHour()));
        spM.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, LocalTime.now().getMinute()));

        colHeureR.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getHeureRappel())));
        colJoursR.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getJours()));
        colMsgR.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessage()));
        colActifR.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActif()).asObject());
        tvRappels.setItems(rappels);

        loadHabitudes();
        chargerRappelsActifs();
    }

    // ========= 1) CRUD Habitudes =========

    @FXML
    public void loadHabitudes() {
        try {
            habitudes.setAll(habService.afficherParUser(Session.getIdU()));
        } catch (SQLException e) {
            erreur("Chargement habitudes", e.getMessage());
        }
    }

    @FXML
    public void ajouterHabitude() {
        String nom = tfNom.getText().trim();
        String freq = tfFrequence.getText().trim();
        String obj = tfObjectif.getText().trim();

        if (nom.isEmpty() || freq.isEmpty()) {
            info("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        try {
            habService.ajouter(new Habitude(nom, freq, obj, Session.getIdU()));
            clearHabFields();
            loadHabitudes();
        } catch (SQLException e) {
            erreur("Ajout habitude", e.getMessage());
        }
    }

    @FXML
    public void modifierHabitude() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        String nom = tfNom.getText().trim();
        String freq = tfFrequence.getText().trim();
        String obj = tfObjectif.getText().trim();

        if (nom.isEmpty() || freq.isEmpty()) {
            info("Validation", "Nom et fréquence obligatoires.");
            return;
        }

        h.setNom(nom);
        h.setFrequence(freq);
        h.setObjectif(obj);
        h.setIdU(Session.getIdU());

        try {
            habService.modifier(h);
            clearHabFields();
            loadHabitudes();
        } catch (SQLException e) {
            erreur("Modifier habitude", e.getMessage());
        }
    }

    @FXML
    public void supprimerHabitude() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        if (!confirm("Suppression", "Supprimer: " + h.getNom() + " ?")) return;

        try {
            habService.supprimer(h.getIdHabitude());
            clearHabFields();
            loadHabitudes();
            historique.clear();
            lbStreak.setText("-");
            lbSemaine.setText("-");
            lbMois.setText("-");
            chargerRappelsActifs();
        } catch (SQLException e) {
            erreur("Supprimer habitude", e.getMessage());
        }
    }

    private void clearHabFields() {
        tfNom.clear();
        tfFrequence.clear();
        tfObjectif.clear();
        tvHabitudes.getSelectionModel().clearSelection();
    }

    private Habitude getHabitudeSelectionnee() {
        Habitude h = tvHabitudes.getSelectionModel().getSelectedItem();
        if (h == null) info("Sélection", "Sélectionne une habitude dans le tableau.");
        return h;
    }

    // ========= 2) Suivi =========

    @FXML
    public void marquerAujourdHui() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        LocalDate d = (dpDate.getValue() == null) ? LocalDate.now() : dpDate.getValue();
        boolean etat = chkEtat.isSelected();

        try {
            suiviService.marquerCommeFaite(h.getIdHabitude(), d, etat);
            info("Suivi", "Enregistré : " + (etat ? "Faite" : "Non faite") + " (" + d + ")");
            chargerHistorique();
        } catch (SQLException e) {
            erreur("Suivi", e.getMessage());
        }
    }

    @FXML
    public void chargerHistorique() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        try {
            historique.setAll(suiviService.historiqueParHabitude(h.getIdHabitude()));
        } catch (SQLException e) {
            erreur("Historique", e.getMessage());
        }
    }

    // ========= 3) Stats =========

    @FXML
    public void calculerStats() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        try {
            int streak = suiviService.getStreakActuel(h.getIdHabitude());
            double semaine = suiviService.getTauxReussiteSemaine(h.getIdHabitude());
            double mois = suiviService.getTauxReussiteMois(h.getIdHabitude());

            lbStreak.setText(String.valueOf(streak));
            lbSemaine.setText(String.format("%.1f %%", semaine));
            lbMois.setText(String.format("%.1f %%", mois));
        } catch (SQLException e) {
            erreur("Stats", e.getMessage());
        }
    }

    // ========= 4) Rappels =========

    @FXML
    public void ajouterRappel() {
        Habitude h = getHabitudeSelectionnee();
        if (h == null) return;

        String jours = buildJours();
        if (jours.isEmpty()) { info("Rappel", "Choisis au moins 1 jour."); return; }

        LocalTime heure = LocalTime.of(spH.getValue(), spM.getValue());
        boolean actif = chkActifRappel.isSelected();

        String msg = tfMsg.getText().trim();
        if (msg.isEmpty()) msg = "Rappel : " + h.getNom();

        try {
            rappelService.ajouter(new RappelHabitude(h.getIdHabitude(), heure, jours, actif, msg));
            tfMsg.clear();
            chargerRappelsActifs();
        } catch (SQLException e) {
            erreur("Ajouter rappel", e.getMessage());
        }
    }

    @FXML
    public void chargerRappelsActifs() {
        try {
            rappels.setAll(rappelService.rappelsActifsDuUser(Session.getIdU()));
        } catch (SQLException e) {
            erreur("Rappels actifs", e.getMessage());
        }
    }

    @FXML
    public void toggleRappelActif() {
        RappelHabitude r = tvRappels.getSelectionModel().getSelectedItem();
        if (r == null) { info("Toggle", "Sélectionne un rappel."); return; }

        try {
            boolean nouveau = !r.isActif();
            rappelService.setActif(r.getIdRappel(), nouveau);
            chargerRappelsActifs();
        } catch (SQLException e) {
            erreur("Toggle actif", e.getMessage());
        }
    }

    @FXML
    public void supprimerRappel() {
        RappelHabitude r = tvRappels.getSelectionModel().getSelectedItem();
        if (r == null) { info("Suppression", "Sélectionne un rappel."); return; }

        if (!confirm("Suppression", "Supprimer ce rappel ?")) return;

        try {
            rappelService.supprimer(r.getIdRappel());
            chargerRappelsActifs();
        } catch (SQLException e) {
            erreur("Suppression rappel", e.getMessage());
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

    // ========= helpers =========

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
