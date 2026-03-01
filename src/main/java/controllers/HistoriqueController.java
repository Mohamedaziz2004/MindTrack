package controllers;

import entities.Progression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import services.ProgressionService;
import utils.AlertUtils;
import utils.SessionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistoriqueController implements Initializable {

    @FXML
    private TableView<Progression> tableView;

    // Note: colId is removed from FXML, so we remove it from controller too
    @FXML
    private TableColumn<Progression, String> colDate;
    @FXML
    private TableColumn<Progression, String> colExercice;
    @FXML
    private TableColumn<Progression, Integer> colScore;
    @FXML
    private TableColumn<Progression, Integer> colRessenti;
    @FXML
    private TableColumn<Progression, Integer> colTemps;
    @FXML
    private TableColumn<Progression, String> colNotes;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private Label totalLabel;
    @FXML
    private Label scoreMoyenLabel;
    @FXML
    private Label ressentiMoyenLabel;
    @FXML
    private Label reussisLabel;
    @FXML
    private Button btnActualiser;
    @FXML
    private Button btnExporter;
    @FXML
    private Button btnDetails;
    @FXML
    private Button btnRetour;
    @FXML
    private Button btnAppliquerFiltre;
    @FXML
    private Button btnResetFiltres;

    private ProgressionService progressionService;
    private SessionManager sessionManager;
    private ObservableList<Progression> progressionList;
    private int userId;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressionService = new ProgressionService();
        sessionManager = SessionManager.getInstance();
        userId = sessionManager.getCurrentUserId();

        // Initialize table columns (colId is removed)
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                )
        );
        colExercice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getExercice() != null ?
                                cellData.getValue().getExercice().getNom() : "Inconnu"
                )
        );
        colScore.setCellValueFactory(new PropertyValueFactory<>("scoreObtenu"));
        colRessenti.setCellValueFactory(new PropertyValueFactory<>("ressentiUtilisateur"));
        colTemps.setCellValueFactory(new PropertyValueFactory<>("tempsPasse"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notesPersonnelles"));

        // Style for score column
        colScore.setCellFactory(column -> new TableCell<Progression, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + "%");
                    setStyle("-fx-text-fill: " + (item >= 70 ? "#27AE60" : "#F39C12") + "; -fx-font-weight: bold;");
                }
            }
        });

        // Style for ressenti column with emojis
        colRessenti.setCellFactory(column -> new TableCell<Progression, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String emoji = item >= 8 ? "😊" : item >= 5 ? "😐" : "😔";
                    setText(emoji + " " + item + "/10");
                }
            }
        });

        // Format duration column
        colTemps.setCellFactory(column -> new TableCell<Progression, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("-");
                } else {
                    int minutes = item / 60;
                    int secondes = item % 60;
                    setText(minutes + "m " + secondes + "s");
                }
            }
        });

        filterCombo.getItems().addAll("Tous", "Réussis (≥70%)", "À améliorer (<70%)", "Avec ressenti");
        filterCombo.setValue("Tous");
        filterCombo.setOnAction(e -> filtrerListe());

        tableView.setRowFactory(tv -> {
            TableRow<Progression> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    afficherDetails(row.getItem());
                }
            });
            return row;
        });

        chargerDonnees();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void chargerDonnees() {
        try {
            List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);
            progressionList = FXCollections.observableArrayList(progressions);
            tableView.setItems(progressionList);
            mettreAJourStatistiques();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur de chargement", "Impossible de charger l'historique: " + e.getMessage());
        }
    }

    private void mettreAJourStatistiques() throws SQLException {
        double[] stats = progressionService.getStatistiquesGlobales(userId);

        totalLabel.setText(String.valueOf((int) stats[0]));
        scoreMoyenLabel.setText(String.format("%.1f%%", stats[1]));
        ressentiMoyenLabel.setText(String.format("%.1f/10", stats[2]));
        reussisLabel.setText(String.valueOf((int) stats[3]));

        appliquerCouleurStat(scoreMoyenLabel, stats[1], 70, 50);
        appliquerCouleurStat(ressentiMoyenLabel, stats[2], 7.0, 5.0);
    }

    private void appliquerCouleurStat(Label label, double valeur, double seuilBon, double seuilMoyen) {
        if (valeur >= seuilBon) {
            label.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        } else if (valeur >= seuilMoyen) {
            label.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
        } else {
            label.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        }
    }

    private void filtrerListe() {
        String filtre = filterCombo.getValue();
        if (filtre == null || filtre.equals("Tous")) {
            tableView.setItems(progressionList);
            return;
        }

        ObservableList<Progression> filtree = FXCollections.observableArrayList();
        for (Progression p : progressionList) {
            switch (filtre) {
                case "Réussis (≥70%)":
                    if (p.getScoreObtenu() != null && p.getScoreObtenu() >= 70) filtree.add(p);
                    break;
                case "À améliorer (<70%)":
                    if (p.getScoreObtenu() != null && p.getScoreObtenu() < 70) filtree.add(p);
                    break;
                case "Avec ressenti":
                    if (p.getRessentiUtilisateur() != null) filtree.add(p);
                    break;
            }
        }
        tableView.setItems(filtree);
    }

    @FXML
    private void handleActualiser() {
        chargerDonnees();
        AlertUtils.showInfo("Actualisation", "L'historique a été mis à jour.");
    }

    @FXML
    private void handleExporter() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter l'historique");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            exporterCSV(file);
        }
    }

    private void exporterCSV(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Date;Exercice;Score;Ressenti;Temps(secondes);Notes");

            for (Progression p : progressionList) {
                writer.print(p.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + ";");
                writer.print((p.getExercice() != null ? p.getExercice().getNom() : "Inconnu") + ";");
                writer.print((p.getScoreObtenu() != null ? p.getScoreObtenu() : "") + ";");
                writer.print((p.getRessentiUtilisateur() != null ? p.getRessentiUtilisateur() : "") + ";");
                writer.print(p.getTempsPasse() + ";");
                writer.println(p.getNotesPersonnelles() != null ? p.getNotesPersonnelles() : "");
            }

            AlertUtils.showSuccess("Export réussi", "Les données ont été exportées vers :\n" + file.getAbsolutePath());

        } catch (Exception e) {
            AlertUtils.showError("Erreur d'export", "Impossible d'exporter les données: " + e.getMessage());
        }
    }

    @FXML
    private void handleDetails() {
        Progression selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner une progression.");
            return;
        }
        afficherDetails(selected);
    }

    private void afficherDetails(Progression progression) {
        StringBuilder details = new StringBuilder();
        details.append("📊 DÉTAILS DE LA PROGRESSION\n");
        details.append("══════════════════════════════\n");
        details.append("📅 Date: ").append(progression.getDateRealisation()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        details.append("🏋️ Exercice: ").append(progression.getExercice() != null ?
                progression.getExercice().getNom() : "Inconnu").append("\n");

        if (progression.getScoreObtenu() != null) {
            details.append("📈 Score: ").append(progression.getScoreObtenu()).append("%\n");
        }

        if (progression.getRessentiUtilisateur() != null) {
            details.append("😊 Ressenti: ").append(progression.getRessentiUtilisateur())
                    .append("/10 - ").append(progression.evaluerBienEtre()).append("\n");
        }

        if (progression.getNotesPersonnelles() != null && !progression.getNotesPersonnelles().isEmpty()) {
            details.append("📝 Notes: ").append(progression.getNotesPersonnelles()).append("\n");
        }

        if (progression.getTempsPasse() > 0) {
            int minutes = progression.getTempsPasse() / 60;
            int secondes = progression.getTempsPasse() % 60;
            details.append("⏱️ Temps: ").append(minutes).append("m ").append(secondes).append("s\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la progression");
        alert.setHeaderText("Progression");
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    @FXML
    private void handleAppliquerFiltreDate() {
        if (dateDebut.getValue() == null || dateFin.getValue() == null) {
            AlertUtils.showWarning("Filtre incomplet", "Veuillez sélectionner une date de début et de fin.");
            return;
        }

        ObservableList<Progression> filtree = FXCollections.observableArrayList();
        for (Progression p : progressionList) {
            if (!p.getDateRealisation().toLocalDate().isBefore(dateDebut.getValue()) &&
                    !p.getDateRealisation().toLocalDate().isAfter(dateFin.getValue())) {
                filtree.add(p);
            }
        }
        tableView.setItems(filtree);
    }

    @FXML
    private void handleResetFiltres() {
        filterCombo.setValue("Tous");
        dateDebut.setValue(null);
        dateFin.setValue(null);
        tableView.setItems(progressionList);
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TableauBord.fxml"));
            Parent root = loader.load();

            TableauBordController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Tableau de Bord");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au tableau de bord: " + e.getMessage());
        }
    }
}