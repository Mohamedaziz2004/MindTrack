package controllers;

import entities.Exercice;
import entities.Session;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ExerciceService;
import services.MusiqueService;
import utils.AlertUtils;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DetailsExerciceController implements Initializable {

    @FXML
    private Label lblId;
    @FXML
    private Label lblNom;
    @FXML
    private Label lblType;
    @FXML
    private Label lblDuree;
    @FXML
    private Label lblDifficulte;
    @FXML
    private Label lblDateCreation;
    @FXML
    private Label lblDateModification;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Label lblNbSessions;
    @FXML
    private Label lblNbProgressions;
    @FXML
    private Label lblTauxCompletion;
    @FXML
    private VBox sessionsContainer;
    @FXML
    private Label lblNoSessions;
    @FXML
    private Button btnRafraichir;
    @FXML
    private TextArea txtDemarche;

    // Nouveaux composants pour la musique
    @FXML
    private VBox musiqueContainer;
    @FXML
    private Label lblMusiqueTitre;
    @FXML
    private Button btnPlayPause;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnNext;
    @FXML
    private Button btnPrev;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label lblTempsActuel;
    @FXML
    private Label lblTempsTotal;
    @FXML
    private ComboBox<String> playlistCombo;

    private ExerciceService exerciceService;
    private MusiqueService musiqueService;
    private Exercice exercice;
    private Stage dialogStage;

    private MediaPlayer mediaPlayer;
    private String currentMusicUrl;
    private List<String> playlistUrls;
    private boolean isPlaying = false;
    private PauseTransition progressUpdater;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exerciceService = new ExerciceService();
        musiqueService = new MusiqueService();

        // Initialiser les contrôles de musique
        initialiserMusique();

        // Configurer le slider de volume
        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100);
            }
        });

        // Initialiser le combo box des playlists
        playlistCombo.getItems().addAll("Relaxation", "Méditation", "Yoga", "Cardio", "Renforcement");
        playlistCombo.setValue("Relaxation");
        playlistCombo.setOnAction(e -> chargerPlaylist());
    }

    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        rafraichirDonnees();

        // Charger une playlist adaptée au type d'exercice
        if (exercice != null) {
            chargerPlaylistPourExercice();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void rafraichirDonnees() {
        try {
            if (exercice != null && exercice.getIdExercice() > 0) {
                exercice = exerciceService.getById(exercice.getIdExercice());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du rafraîchissement: " + e.getMessage());
        }
        afficherDetails();
    }

    private void afficherDetails() {
        if (exercice != null) {
            // Informations de base
            lblId.setText(String.valueOf(exercice.getIdExercice()));
            lblNom.setText(exercice.getNom());
            lblType.setText(exercice.getType());
            lblDuree.setText(exercice.getDuree() + " minutes");

            // Difficulté avec couleur
            lblDifficulte.setText(exercice.getDifficulte());
            String color;
            switch (exercice.getDifficulte()) {
                case "Débutant": color = "#27AE60"; break;
                case "Intermédiaire": color = "#F39C12"; break;
                case "Avancé": color = "#E74C3C"; break;
                case "Expert": color = "#8E44AD"; break;
                default: color = "black";
            }
            lblDifficulte.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

            // Dates
            if (exercice.getDateCreation() != null) {
                lblDateCreation.setText(exercice.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } else {
                lblDateCreation.setText("Non spécifiée");
            }

            if (exercice.getDateModification() != null) {
                lblDateModification.setText(exercice.getDateModification().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            } else {
                lblDateModification.setText("Jamais modifié");
            }

            // Description
            txtDescription.setText(exercice.getDescription() != null && !exercice.getDescription().isEmpty()
                    ? exercice.getDescription()
                    : "Aucune description");

            // Démarche
            txtDemarche.setText(exercice.getDemarche() != null && !exercice.getDemarche().isEmpty()
                    ? exercice.getDemarche()
                    : "Aucune démarche/instruction fournie pour cet exercice.");

            // Statistiques
            lblNbSessions.setText(String.valueOf(exercice.getSessions().size()));
            lblNbProgressions.setText(String.valueOf(exercice.getProgressions().size()));
            lblTauxCompletion.setText(String.format("%.1f%%", exercice.getTauxCompletion()));

            // Afficher les sessions récentes
            afficherSessionsRecentes();
        }
    }

    private void afficherSessionsRecentes() {
        sessionsContainer.getChildren().clear();

        List<Session> sessions = exercice.getSessions();

        if (sessions == null || sessions.isEmpty()) {
            lblNoSessions.setVisible(true);
            return;
        }

        lblNoSessions.setVisible(false);

        // Afficher les 5 sessions les plus récentes
        sessions.stream()
                .limit(5)
                .forEach(session -> {
                    HBox sessionItem = new HBox(10);
                    sessionItem.setPadding(new Insets(8, 12, 8, 12));
                    sessionItem.setStyle("-fx-background-color: #F8F9F9; -fx-background-radius: 8; -fx-border-color: #e9ecef; -fx-border-radius: 8;");

                    String date = session.getDateSession().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String statut = session.getStatut();
                    String duree = session.getDureeReelle() != null && session.getDureeReelle() > 0
                            ? (session.getDureeReelle() / 60) + "m " + (session.getDureeReelle() % 60) + "s"
                            : "-";

                    Label lblDate = new Label(date);
                    lblDate.setPrefWidth(100);
                    lblDate.setStyle("-fx-font-weight: bold;");

                    Label lblStatut = new Label(statut);
                    lblStatut.setPrefWidth(80);
                    String statutColor;
                    switch (statut) {
                        case "Terminée": statutColor = "#27AE60"; break;
                        case "En cours": statutColor = "#F39C12"; break;
                        default: statutColor = "#3498DB";
                    }
                    lblStatut.setStyle("-fx-text-fill: " + statutColor + "; -fx-font-weight: bold;");

                    Label lblDuree = new Label(duree);
                    lblDuree.setPrefWidth(80);

                    Label lblResultat = new Label(session.getResultat() != null ? session.getResultat() : "-");
                    lblResultat.setPrefWidth(200);

                    sessionItem.getChildren().addAll(lblDate, lblStatut, lblDuree, lblResultat);
                    sessionsContainer.getChildren().add(sessionItem);
                });
    }

    // ==================== MÉTHODES POUR LA MUSIQUE ====================

    private void initialiserMusique() {
        btnPlayPause.setOnAction(e -> playPause());
        btnStop.setOnAction(e -> stop());
        btnNext.setOnAction(e -> next());
        btnPrev.setOnAction(e -> prev());

        // Initialiser le mise à jour de la progression
        progressUpdater = new PauseTransition(Duration.seconds(0.5));
        progressUpdater.setOnFinished(e -> {
            if (mediaPlayer != null && isPlaying) {
                mettreAJourProgression();
                progressUpdater.playFromStart();
            }
        });
    }

    private void chargerPlaylistPourExercice() {
        if (exercice != null) {
            String type = exercice.getType();
            playlistUrls = musiqueService.getPlaylistParType(type, 10);

            if (!playlistUrls.isEmpty()) {
                currentMusicUrl = playlistUrls.get(0);
                lblMusiqueTitre.setText("Playlist: " + type);
            }
        }
    }

    private void chargerPlaylist() {
        String selected = playlistCombo.getValue();
        playlistUrls = musiqueService.getPlaylistParType(selected, 10);

        if (!playlistUrls.isEmpty()) {
            currentMusicUrl = playlistUrls.get(0);
            lblMusiqueTitre.setText("Playlist: " + selected);

            // Arrêter la musique en cours
            stop();
        }
    }

    private void playPause() {
        if (currentMusicUrl == null) {
            AlertUtils.showWarning("Information", "Aucune musique chargée");
            return;
        }

        try {
            if (mediaPlayer == null) {
                // Créer un nouveau MediaPlayer
                Media media = new Media(currentMusicUrl);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);

                // Configurer les listeners
                mediaPlayer.setOnReady(() -> {
                    Duration total = mediaPlayer.getTotalDuration();
                    lblTempsTotal.setText(formatDuration(total));
                });

                mediaPlayer.setOnEndOfMedia(() -> {
                    // Passer à la musique suivante automatiquement
                    next();
                });

                mediaPlayer.play();
                isPlaying = true;
                btnPlayPause.setText("⏸️");

                // Démarrer la mise à jour de la progression
                progressUpdater.playFromStart();

            } else {
                if (isPlaying) {
                    mediaPlayer.pause();
                    btnPlayPause.setText("▶️");
                } else {
                    mediaPlayer.play();
                    btnPlayPause.setText("⏸️");
                    progressUpdater.playFromStart();
                }
                isPlaying = !isPlaying;
            }

        } catch (Exception e) {
            AlertUtils.showError("Erreur", "Impossible de lire la musique: " + e.getMessage());
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
            isPlaying = false;
            btnPlayPause.setText("▶️");
            progressBar.setProgress(0);
            lblTempsActuel.setText("0:00");
            progressUpdater.stop();
        }
    }

    private void next() {
        if (playlistUrls != null && !playlistUrls.isEmpty()) {
            currentMusicUrl = musiqueService.getMusiqueSuivante();
            stop();
            playPause();
        }
    }

    private void prev() {
        if (playlistUrls != null && !playlistUrls.isEmpty()) {
            currentMusicUrl = musiqueService.getMusiquePrecedente();
            stop();
            playPause();
        }
    }

    private void mettreAJourProgression() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();

            if (total.greaterThan(Duration.ZERO)) {
                double progress = current.toMillis() / total.toMillis();
                progressBar.setProgress(progress);
                lblTempsActuel.setText(formatDuration(current));
            }
        }
    }

    private String formatDuration(Duration duration) {
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    @FXML
    private void handleFermer() {
        // Arrêter la musique avant de fermer
        stop();
        dialogStage.close();
    }

    @FXML
    private void handleRafraichir() {
        rafraichirDonnees();
    }
}