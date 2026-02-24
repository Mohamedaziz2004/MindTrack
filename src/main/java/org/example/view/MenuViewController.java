package org.example.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.example.controller.UtilisateurController;
import org.example.util.AppState;

import java.io.IOException;

public class MenuViewController {

    private static MenuViewController instance;

    public static MenuViewController getInstance() {
        return instance;
    }

    @FXML
    private StackPane contentArea;

    @FXML
    private HBox journalNavItem;
    @FXML
    private HBox goalsNavItem;
    @FXML
    private HBox exercisesNavItem;
    @FXML
    private HBox habitsNavItem;
    @FXML
    private HBox profileNavItem;
    @FXML
    private HBox statsNavItem;
    @FXML
    private HBox adminNavItem;
    @FXML
    private HBox plannerNavItem;

    private UtilisateurController utilisateurController = new UtilisateurController();

    /**
     * Initialise le menu latéral et définit l'ID utilisateur global.
     */
    @FXML
    public void initialize() {
        instance = this;
        // Ensure default user exists and set current user ID
        int userId = utilisateurController.ensureDefaultUserExists();
        AppState.setCurrentUserId(userId);

        // Load goals dashboard (Choose Your Focus) by default
        openObjectifView(null);
    }

    /**
     * Charge la vue du tableau de bord dans la zone de contenu.
     */
    @FXML
    public void openDashboardView(MouseEvent event) {
        updateActiveNavItem(journalNavItem);
        loadContent("/dashboard-view.fxml");
    }

    /**
     * Ouvre le menu de sélection des catégories d'objectifs.
     */
    @FXML
    public void openObjectifView(MouseEvent event) {
        updateActiveNavItem(goalsNavItem);
        loadContent("/selection-menu-view.fxml");
    }

    /**
     * Charge directement la vue détaillée des objectifs.
     */
    public void loadActualObjectifView() {
        loadContent("/objectif-view.fxml");
    }

    /**
     * Ouvre la vue de gestion des jalons (exercices).
     */
    @FXML
    public void openJalonView(MouseEvent event) {
        updateActiveNavItem(goalsNavItem);
        loadContent("/jalon-progression-view.fxml");
    }

    /**
     * Ouvre la vue du plan d'action (habitudes).
     */
    @FXML
    public void openPlanActionView(MouseEvent event) {
        updateActiveNavItem(goalsNavItem);
        loadContent("/plan-action-view.fxml");
    }

    /**
     * Ouvre l'outil de planification intelligente.
     */
    @FXML
    public void openPlanificateurView(MouseEvent event) {
        updateActiveNavItem(plannerNavItem);
        loadContent("/planificateur-view.fxml");
    }

    /**
     * Ouvre la vue des statistiques et analyses IA.
     */
    @FXML
    public void openInsightsView(MouseEvent event) {
        updateActiveNavItem(statsNavItem);
        loadContent("/insights-view.fxml");
    }

    /**
     * Quitte proprement l'application.
     */
    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    /**
     * Méthode utilitaire pour changer le contenu affiché dynamiquement.
     * 
     * @param fxmlPath Chemin vers le fichier FXML à charger.
     */
    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Met à jour l'état visuel du menu pour refléter la sélection actuelle.
     * 
     * @param activeItem L'élément de menu à mettre en évidence.
     */
    private void updateActiveNavItem(HBox activeItem) {
        HBox[] navItems = { journalNavItem, goalsNavItem, exercisesNavItem, habitsNavItem,
                profileNavItem, statsNavItem, adminNavItem, plannerNavItem };

        for (HBox item : navItems) {
            if (item != null) {
                item.getStyleClass().remove("nav-item-active");
            }
        }

        if (activeItem != null) {
            activeItem.getStyleClass().add("nav-item-active");
        }
    }
}
