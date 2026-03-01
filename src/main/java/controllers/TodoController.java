package controllers;

import entities.Exercice;
import entities.Todo;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.BadgeService;
import services.ExerciceService;
import services.TodoService;
import utils.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TodoController implements Initializable {

    @FXML
    private VBox todoContainer;
    @FXML
    private VBox enCoursContainer;
    @FXML
    private VBox doneContainer;

    @FXML
    private Label todoCountLabel;
    @FXML
    private Label enCoursCountLabel;
    @FXML
    private Label doneCountLabel;
    @FXML
    private Label totalCountLabel;
    @FXML
    private Label enRetardCountLabel;

    @FXML
    private Button btnAjouterTodo;
    @FXML
    private Button btnRafraichir;

    @FXML
    private ComboBox<String> filterPrioriteCombo;
    @FXML
    private TextField searchField;
    @FXML
    private TabPane todoTabPane;

    private TodoService todoService;
    private ExerciceService exerciceService;
    private Stage dialogStage;
    private Stage primaryStage;

    private ObservableList<Todo> todoList;
    private Map<String, List<Todo>> todosOrganises;
    private String currentFilter = "TOUS";
    private String searchText = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        todoService = new TodoService();
        exerciceService = new ExerciceService();

        // Initialiser le filtre de priorité
        filterPrioriteCombo.getItems().addAll("TOUS", "BASSE", "MOYENNE", "HAUTE", "URGENTE");
        filterPrioriteCombo.setValue("TOUS");
        filterPrioriteCombo.setOnAction(e -> filtrerTodos());

        // Recherche en temps réel
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchText = newVal.toLowerCase();
            filtrerTodos();
        });

        // Charger les données
        chargerTodos();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void chargerTodos() {
        try {
            todosOrganises = todoService.getTodosOrganises();
            afficherTodos();
            mettreAJourStatistiques();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les todos: " + e.getMessage());
        }
    }

    private void afficherTodos() {
        // Vider les conteneurs
        todoContainer.getChildren().clear();
        enCoursContainer.getChildren().clear();
        doneContainer.getChildren().clear();

        // Ajouter les en-têtes
        ajouterEnTete(todoContainer, "📝 À FAIRE", todoCountLabel);
        ajouterEnTete(enCoursContainer, "🔄 EN COURS", enCoursCountLabel);
        ajouterEnTete(doneContainer, "✅ TERMINÉ", doneCountLabel);

        // Filtrer et afficher les todos
        List<Todo> todosTodo = filtrerListe(todosOrganises.getOrDefault("TODO", List.of()));
        List<Todo> todosEnCours = filtrerListe(todosOrganises.getOrDefault("EN_COURS", List.of()));
        List<Todo> todosDone = filtrerListe(todosOrganises.getOrDefault("DONE", List.of()));

        // Mettre à jour les compteurs
        todoCountLabel.setText(String.valueOf(todosTodo.size()));
        enCoursCountLabel.setText(String.valueOf(todosEnCours.size()));
        doneCountLabel.setText(String.valueOf(todosDone.size()));

        // Afficher les todos
        todosTodo.forEach(todo -> todoContainer.getChildren().add(creerTodoCard(todo)));
        todosEnCours.forEach(todo -> enCoursContainer.getChildren().add(creerTodoCard(todo)));
        todosDone.forEach(todo -> doneContainer.getChildren().add(creerTodoCard(todo)));

        // Ajouter des messages si vide
        if (todosTodo.isEmpty()) {
            todoContainer.getChildren().add(creerMessageVide("Aucune tâche à faire"));
        }
        if (todosEnCours.isEmpty()) {
            enCoursContainer.getChildren().add(creerMessageVide("Aucune tâche en cours"));
        }
        if (todosDone.isEmpty()) {
            doneContainer.getChildren().add(creerMessageVide("Aucune tâche terminée"));
        }
    }

    private void ajouterEnTete(VBox container, String titre, Label countLabel) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 10, 0));

        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        Label count = new Label();
        count.setStyle("-fx-font-size: 14px; -fx-background-color: #3498DB; -fx-text-fill: white; -fx-padding: 2 8; -fx-background-radius: 12;");
        count.textProperty().bind(countLabel.textProperty());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAjouter = new Button("+ Ajouter");
        btnAjouter.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
        btnAjouter.setOnAction(e -> ouvrirAjouterTodo());

        header.getChildren().addAll(titleLabel, count, spacer, btnAjouter);
        container.getChildren().add(header);
    }

    private Node creerTodoCard(Todo todo) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e9ecef; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        card.setMaxWidth(Double.MAX_VALUE);

        // Effet de survol
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: " + todo.getPrioriteColor() + "; -fx-border-radius: 12; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e9ecef; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);")
        );

        // Double-clic pour voir les détails
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ouvrirDetailsTodo(todo);
            }
        });

        // Ligne 1: Titre et priorité
        HBox line1 = new HBox(10);
        line1.setAlignment(Pos.CENTER_LEFT);

        Label iconeStatut = new Label(todo.getStatutIcone());
        iconeStatut.setStyle("-fx-font-size: 18px;");

        Label titreLabel = new Label(todo.getTitre());
        titreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        titreLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titreLabel, Priority.ALWAYS);

        Label prioriteBadge = new Label(todo.getPrioriteIcone() + " " + todo.getPriorite());
        prioriteBadge.setStyle("-fx-background-color: " + todo.getPrioriteColor() + "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        line1.getChildren().addAll(iconeStatut, titreLabel, prioriteBadge);

        // Ligne 2: Exercice associé (si existant)
        if (todo.getExercice() != null) {
            HBox line2 = new HBox(10);
            line2.setAlignment(Pos.CENTER_LEFT);

            Label exerciceIcone = new Label("🏋️");
            exerciceIcone.setStyle("-fx-font-size: 12px;");

            Label exerciceLabel = new Label(todo.getExercice().getNom());
            exerciceLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

            line2.getChildren().addAll(exerciceIcone, exerciceLabel);
            card.getChildren().add(line2);
        }

        // Ligne 3: Échéance et progression
        HBox line3 = new HBox(10);
        line3.setAlignment(Pos.CENTER_LEFT);

        if (todo.getDateEcheance() != null) {
            Label echeanceIcone = new Label("📅");
            echeanceIcone.setStyle("-fx-font-size: 12px;");

            Label echeanceLabel = new Label(todo.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            echeanceLabel.setStyle("-fx-text-fill: " + (todo.estEnRetard() ? "#E74C3C" : "#7F8C8D") + "; -fx-font-size: 12px; -fx-font-weight: " + (todo.estEnRetard() ? "bold" : "normal") + ";");

            line3.getChildren().addAll(echeanceIcone, echeanceLabel);
        }

        // Barre de progression
        if (!todo.isDone()) {
            ProgressBar progressBar = new ProgressBar(todo.getProgression() / 100.0);
            progressBar.setPrefWidth(150);
            progressBar.setStyle("-fx-accent: " + todo.getPrioriteColor() + ";");

            Label progressLabel = new Label(todo.getProgression() + "%");
            progressLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");

            line3.getChildren().addAll(new Region(), progressBar, progressLabel);
            HBox.setHgrow(line3.getChildren().get(0), Priority.ALWAYS);
        } else {
            Label dateCompletion = new Label("Terminé le " +
                    (todo.getDateCompletion() != null ?
                            todo.getDateCompletion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) :
                            "-"));
            dateCompletion.setStyle("-fx-text-fill: #27AE60; -fx-font-size: 11px; -fx-font-weight: bold;");
            line3.getChildren().add(new Region());
            HBox.setHgrow(line3.getChildren().get(0), Priority.ALWAYS);
        }

        card.getChildren().addAll(line1, line3);

        // Ligne 4: Boutons d'action
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        if (todo.isTodo()) {
            Button btnDemarrer = new Button("▶ Démarrer");
            btnDemarrer.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");
            btnDemarrer.setOnAction(e -> demarrerTodo(todo));

            Button btnModifier = new Button("✏");
            btnModifier.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 10;");
            btnModifier.setOnAction(e -> modifierTodo(todo));

            Button btnSupprimer = new Button("🗑");
            btnSupprimer.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 10;");
            btnSupprimer.setOnAction(e -> supprimerTodo(todo));

            actions.getChildren().addAll(btnDemarrer, btnModifier, btnSupprimer);

        } else if (todo.isEnCours()) {
            Button btnTerminer = new Button("✅ Terminer");
            btnTerminer.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");
            btnTerminer.setOnAction(e -> terminerTodo(todo));

            Button btnModifier = new Button("✏");
            btnModifier.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 10;");
            btnModifier.setOnAction(e -> modifierTodo(todo));

            Button btnSupprimer = new Button("🗑");
            btnSupprimer.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 10;");
            btnSupprimer.setOnAction(e -> supprimerTodo(todo));

            // Slider de progression
            Slider progressSlider = new Slider(0, 100, todo.getProgression());
            progressSlider.setPrefWidth(120);
            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    int nouvelleProgression = newVal.intValue();
                    todoService.mettreAJourProgression(todo.getIdTodo(), nouvelleProgression);
                    todo.setProgression(nouvelleProgression);

                    // Si on atteint 100%, déclencher le badge
                    if (nouvelleProgression == 100 && !todo.isDone()) {
                        BadgeService badgeService = BadgeService.getInstance();
                        BadgeService.Badge nouveauBadge = badgeService.incrementerExercice();

                        // Recharger après un court délai pour voir l'effet
                        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
                        pause.setOnFinished(e -> {
                            chargerTodos();
                            if (nouveauBadge != null) {
                                badgeService.showBadgeAlert(nouveauBadge);
                            }
                        });
                        pause.play();
                    } else {
                        chargerTodos();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            actions.getChildren().addAll(progressSlider, btnTerminer, btnModifier, btnSupprimer);

        } else {
            Button btnSupprimer = new Button("🗑 Supprimer");
            btnSupprimer.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 5 12;");
            btnSupprimer.setOnAction(e -> supprimerTodo(todo));

            actions.getChildren().add(btnSupprimer);
        }

        card.getChildren().add(actions);

        return card;
    }

    private Node creerMessageVide(String message) {
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: #BDC3C7; -fx-font-style: italic; -fx-padding: 20;");
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private List<Todo> filtrerListe(List<Todo> todos) {
        return todos.stream()
                .filter(todo -> {
                    if ("TOUS".equals(currentFilter)) return true;
                    return todo.getPriorite().equals(currentFilter);
                })
                .filter(todo -> {
                    if (searchText.isEmpty()) return true;
                    return todo.getTitre().toLowerCase().contains(searchText) ||
                            (todo.getExercice() != null && todo.getExercice().getNom().toLowerCase().contains(searchText));
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void filtrerTodos() {
        currentFilter = filterPrioriteCombo.getValue();
        afficherTodos();
    }

    private void mettreAJourStatistiques() {
        try {
            Map<String, Long> stats = todoService.getStatistiques();
            totalCountLabel.setText(String.valueOf(stats.get("total")));
            enRetardCountLabel.setText(String.valueOf(stats.get("enRetard")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ouvrirAjouterTodo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AjouterTodo.fxml"));
            Parent root = loader.load();

            AjouterTodoController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setTodoController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Ajouter une tâche");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }



    private void ouvrirDetailsTodo(Todo todo) {
        StringBuilder details = new StringBuilder();
        details.append("📋 ").append(todo.getTitre()).append("\n");
        details.append("══════════════════════════════\n");
        details.append("Statut: ").append(todo.getStatutIcone()).append(" ").append(todo.getStatut()).append("\n");
        details.append("Priorité: ").append(todo.getPrioriteIcone()).append(" ").append(todo.getPriorite()).append("\n");

        if (todo.getExercice() != null) {
            details.append("Exercice: 🏋️ ").append(todo.getExercice().getNom()).append("\n");
        }

        if (todo.getDescription() != null && !todo.getDescription().isEmpty()) {
            details.append("Description: ").append(todo.getDescription()).append("\n");
        }

        if (todo.getDateEcheance() != null) {
            details.append("Échéance: 📅 ").append(todo.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            if (todo.estEnRetard()) {
                details.append(" ⚠️ EN RETARD");
            }
            details.append("\n");
        }

        details.append("Progression: ").append(todo.getProgression()).append("%\n");

        if (todo.getDateCompletion() != null) {
            details.append("Terminé le: ✅ ").append(todo.getDateCompletion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }

        if (todo.getNotes() != null && !todo.getNotes().isEmpty()) {
            details.append("Notes: ").append(todo.getNotes()).append("\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la tâche");
        alert.setHeaderText("Tâche #" + todo.getIdTodo());

        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(400);
        textArea.setPrefHeight(300);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void demarrerTodo(Todo todo) {
        try {
            todoService.marquerEnCours(todo.getIdTodo());
            chargerTodos();
            AlertUtils.showInfo("Succès", "Tâche démarrée avec succès!");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de démarrer la tâche: " + e.getMessage());
        }
    }

    private void terminerTodo(Todo todo) {
        if (AlertUtils.showConfirmation("Confirmation", "Marquer cette tâche comme terminée ?")) {
            try {
                todoService.marquerTermine(todo.getIdTodo());

                // ✅ DÉCLENCHER LE BADGE ICI
                BadgeService badgeService = BadgeService.getInstance();
                BadgeService.Badge nouveauBadge = badgeService.incrementerExercice();

                // Recharger l'affichage
                chargerTodos();

                // Afficher l'alerte du badge si un nouveau badge a été obtenu
                if (nouveauBadge != null) {
                    badgeService.showBadgeAlert(nouveauBadge);
                } else {
                    AlertUtils.showInfo("Succès", "Tâche terminée avec succès !");
                }

            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de terminer la tâche: " + e.getMessage());
            }
        }
    }



    private void modifierTodo(Todo todo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ModifierTodo.fxml"));
            Parent root = loader.load();

            ModifierTodoController controller = loader.getController();
            controller.setTodo(todo);
            controller.setPrimaryStage(primaryStage);
            controller.setTodoController(this);

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Modifier la tâche");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    private void supprimerTodo(Todo todo) {
        if (AlertUtils.showConfirmation("Confirmation", "Voulez-vous vraiment supprimer cette tâche ?")) {
            try {
                todoService.supprimer(todo.getIdTodo());
                chargerTodos();
                AlertUtils.showInfo("Succès", "Tâche supprimée avec succès!");
            } catch (SQLException e) {
                AlertUtils.showError("Erreur", "Impossible de supprimer la tâche: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRafraichir() {
        chargerTodos();
    }
}