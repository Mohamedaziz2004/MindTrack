package controllers;

import entities.Exercice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ExerciceService;
import utils.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ListeExercicesController implements Initializable {

    @FXML
    private TableView<Exercice> tableView;

    // Note: colId is kept in controller for internal use but removed from FXML
    // The @FXML annotation will be ignored since it's not in the FXML anymore
    private TableColumn<Exercice, Integer> colId;

    @FXML
    private TableColumn<Exercice, String> colNom;
    @FXML
    private TableColumn<Exercice, String> colType;
    @FXML
    private TableColumn<Exercice, Integer> colDuree;
    @FXML
    private TableColumn<Exercice, String> colDifficulte;
    @FXML
    private TableColumn<Exercice, String> colDescription;
    @FXML
    private TextField searchField;
    @FXML
    private Label totalLabel;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnSupprimer;
    @FXML
    private Button btnDetails;
    @FXML
    private Button btnRetour;

    @FXML
    private TableColumn<Exercice, String> colDemarche;

    private ExerciceService exerciceService;
    private ObservableList<Exercice> exerciceList;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exerciceService = new ExerciceService();

        // Initialize table columns (colId is no longer set in FXML)
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colDifficulte.setCellValueFactory(new PropertyValueFactory<>("difficulte"));

        // If there's a description column in your FXML
        if (colDescription != null) {
            colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        }

        // Style for difficulty column
        colDifficulte.setCellFactory(column -> new TableCell<Exercice, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Débutant": setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); break;
                        case "Intermédiaire": setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;"); break;
                        case "Avancé": setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); break;
                        case "Expert": setStyle("-fx-text-fill: #8E44AD; -fx-font-weight: bold;"); break;
                    }
                }
            }
        });

        // Double-click to view details
        tableView.setRowFactory(tv -> {
            TableRow<Exercice> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleDetails(row.getItem());
                }
            });
            return row;
        });

        if (colDemarche != null) {
            colDemarche.setCellValueFactory(new PropertyValueFactory<>("demarche"));
            colDemarche.setCellFactory(column -> new TableCell<Exercice, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.isEmpty()) {
                        setText("-");
                    } else {
                        // Afficher seulement les premiers 50 caractères
                        String apercu = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                        setText(apercu);
                    }
                }
            });
        }

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerListe(newVal));

        // Load initial data
        chargerDonnees();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Charge les données depuis la base de données et met à jour la table
     */
    public void chargerDonnees() {
        try {
            List<Exercice> exercices = exerciceService.getAll();
            exerciceList = FXCollections.observableArrayList(exercices);
            tableView.setItems(exerciceList);
            totalLabel.setText("Total: " + exerciceList.size() + " exercice(s)");
            System.out.println("✅ Données rechargées: " + exerciceList.size() + " exercices");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur de chargement", "Impossible de charger la liste: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filtre la liste en fonction du texte de recherche
     */
    private void filtrerListe(String texte) {
        if (texte == null || texte.isEmpty()) {
            tableView.setItems(exerciceList);
            totalLabel.setText("Total: " + exerciceList.size() + " exercice(s)");
            return;
        }

        ObservableList<Exercice> filtree = FXCollections.observableArrayList();
        for (Exercice exo : exerciceList) {
            if (exo.getNom().toLowerCase().contains(texte.toLowerCase()) ||
                    exo.getType().toLowerCase().contains(texte.toLowerCase())) {
                filtree.add(exo);
            }
        }
        tableView.setItems(filtree);
        totalLabel.setText("Résultats: " + filtree.size() + " exercice(s)");
    }

    /**
     * Ouvre la fenêtre d'ajout d'exercice
     */
    @FXML
    private void handleAjouter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterExercice.fxml"));
            Parent root = loader.load();

            AjouterExerciceController controller = loader.getController();
            controller.setPrimaryStage(primaryStage); // Passer primaryStage

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Ajouter un exercice");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            // Store current search before opening modal
            String currentSearchText = searchField.getText();

            // Show modal and wait
            modalStage.showAndWait();

            // Note: Le rechargement des données se fait maintenant dans AjouterExerciceController
            // après un ajout réussi, avant de fermer la fenêtre modale

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    /**
     * Ouvre la fenêtre de modification d'exercice
     */
    @FXML
    private void handleModifier() {
        Exercice selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un exercice.");
            return;
        }

        // Store the ID of the selected item and current search
        int selectedId = selected.getIdExercice();
        String currentSearchText = searchField.getText();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierExercice.fxml"));
            Parent root = loader.load();

            ModifierExerciceController controller = loader.getController();
            controller.setExercice(selected);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Modifier l'exercice");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            // Show modal and wait
            modalStage.showAndWait();

            // Refresh data after modal closes
            chargerDonnees();

            // Restore search filter if needed
            if (currentSearchText != null && !currentSearchText.isEmpty()) {
                searchField.setText(currentSearchText);
                filtrerListe(currentSearchText);
            }

            // Try to reselect the modified item
            tableView.getSelectionModel().select(
                    exerciceList.stream()
                            .filter(e -> e.getIdExercice() == selectedId)
                            .findFirst()
                            .orElse(null)
            );

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    /**
     * Ouvre la fenêtre de confirmation de suppression
     */
    @FXML
    private void handleSupprimer() {
        Exercice selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un exercice.");
            return;
        }

        String currentSearchText = searchField.getText();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SupprimerExercice.fxml"));
            Parent root = loader.load();

            SupprimerExerciceController controller = loader.getController();
            controller.setExercice(selected);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Confirmation de suppression");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            modalStage.showAndWait();

            // Refresh data after modal closes
            chargerDonnees();

            // Restore search filter if needed
            if (currentSearchText != null && !currentSearchText.isEmpty()) {
                searchField.setText(currentSearchText);
                filtrerListe(currentSearchText);
            }

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre de suppression: " + e.getMessage());
        }
    }

    /**
     * Ouvre la fenêtre de détails d'exercice
     */
    @FXML
    private void handleDetails() {
        Exercice selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Aucune sélection", "Veuillez sélectionner un exercice.");
            return;
        }
        handleDetails(selected);
    }

    private void handleDetails(Exercice exercice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailsExercice.fxml"));
            Parent root = loader.load();

            DetailsExerciceController controller = loader.getController();
            controller.setExercice(exercice);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Détails de l'exercice");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);

            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
        }
    }



    /**
     * Retourne au menu principal
     */
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