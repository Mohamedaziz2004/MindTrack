package controllers;

import entities.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.SessionService;
import utils.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ListeSessionsController implements Initializable {

    @FXML
    private TableView<Session> tableView;

    // Note: colId is removed from FXML, so we remove it from controller too
    @FXML
    private TableColumn<Session, String> colDate;
    @FXML
    private TableColumn<Session, String> colExercice;
    @FXML
    private TableColumn<Session, String> colStatut;
    @FXML
    private TableColumn<Session, String> colResultat;
    @FXML
    private TableColumn<Session, Integer> colDuree;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private Label totalLabel;
    @FXML
    private Label enCoursLabel;
    @FXML
    private Label termineesLabel;
    @FXML
    private Button btnNouvelle;
    @FXML
    private Button btnDetails;
    @FXML
    private Button btnTerminer;
    @FXML
    private Button btnRetour;
    @FXML
    private HBox statsBar;

    private SessionService sessionService;
    private ObservableList<Session> sessionList;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionService = new SessionService();

        // Initialize table columns (colId is removed)
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDateSession().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                )
        );
        colExercice.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getExercice() != null ?
                                cellData.getValue().getExercice().getNom() : "Inconnu"
                )
        );
        colStatut.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut())
        );
        colResultat.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeReelle"));

        // Style for status column
        colStatut.setCellFactory(column -> new TableCell<Session, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Terminée": setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); break;
                        case "En cours": setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;"); break;
                        case "Planifiée": setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;"); break;
                    }
                }
            }
        });

        // Format duration column to show minutes and seconds
        colDuree.setCellFactory(column -> new TableCell<Session, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0) {
                    setText("-");
                } else {
                    int minutes = item / 60;
                    int seconds = item % 60;
                    setText(minutes + "m " + seconds + "s");
                }
            }
        });

        filterCombo.getItems().addAll("Toutes", "En cours", "Terminées", "Planifiées");
        filterCombo.setValue("Toutes");
        filterCombo.setOnAction(e -> filtrerListe());

        tableView.setRowFactory(tv -> {
            TableRow<Session> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleDetails(row.getItem());
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
            List<Session> sessions = sessionService.getAll();
            sessionList = FXCollections.observableArrayList(sessions);
            tableView.setItems(sessionList);
            mettreAJourStats();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur de chargement", "Impossible de charger la liste: " + e.getMessage());
        }
    }

    private void mettreAJourStats() {
        if (sessionList == null || sessionList.isEmpty()) {
            totalLabel.setText("Total: 0");
            enCoursLabel.setText("En cours: 0");
            termineesLabel.setText("Terminées: 0");
            return;
        }

        long total = sessionList.size();
        long enCours = sessionList.stream().filter(s -> s.getStatut().equals("En cours")).count();
        long terminees = sessionList.stream().filter(s -> s.getStatut().equals("Terminée")).count();

        totalLabel.setText("Total: " + total);
        enCoursLabel.setText("En cours: " + enCours);
        termineesLabel.setText("Terminées: " + terminees);
    }

    private void filtrerListe() {
        String filtre = filterCombo.getValue();
        if (filtre == null || filtre.equals("Toutes") || sessionList == null) {
            tableView.setItems(sessionList);
            return;
        }

        ObservableList<Session> filtree = FXCollections.observableArrayList();
        for (Session session : sessionList) {
            if (session.getStatut().equals(filtre)) {
                filtree.add(session);
            }
        }
        tableView.setItems(filtree);
    }

    @FXML
    private void handleNouvelleSession() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DemarrerSession.fxml"));
            Parent root = loader.load();

            DemarrerSessionController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Démarrer une session");
            modalStage.setScene(new Scene(root));

            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            modalStage.showAndWait();
            chargerDonnees();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    @FXML
    private void handleDetails() {
        Session selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner une session.");
            return;
        }
        handleDetails(selected);
    }

    private void handleDetails(Session session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailsSession.fxml"));
            Parent root = loader.load();

            DetailsSessionController controller = loader.getController();
            controller.setSession(session);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Détails de la session");
            modalStage.setScene(new Scene(root));

            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
        }
    }

    @FXML
    private void handleTerminer() {
        Session selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner une session.");
            return;
        }

        if (selected.isTerminee()) {
            AlertUtils.showWarning("Session terminée", "Cette session est déjà terminée.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerminerSession.fxml"));
            Parent root = loader.load();

            TerminerSessionController controller = loader.getController();
            controller.setSession(selected);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Terminer la session");
            modalStage.setScene(new Scene(root));

            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            modalStage.showAndWait();
            chargerDonnees();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Parent root = loader.load();

            MenuPrincipalController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Menu Principal");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au menu: " + e.getMessage());
        }
    }
}