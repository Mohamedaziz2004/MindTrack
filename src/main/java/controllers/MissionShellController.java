package controllers;

import application.MainApp;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MissionShellController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private VBox sidebar;
    @FXML private Button btnMenuPrincipal;
    @FXML private Button btnExercices;
    @FXML private Button btnSessions;
    @FXML private Button btnTableauBord;
    @FXML private Button btnStatistiques;
    @FXML private Button btnHistorique;
    @FXML private Button btnTheme;
    @FXML private Label userName;
    @FXML private Label userRole;
    @FXML private Circle userAvatar;
    @FXML private ImageView brandLogo;
    @FXML private Button btnTodo;

    @FXML
    private Button btnBadges;

    private Button activeButton = null;
    private static MissionShellController instance;
    private Timeline expandAnimation;
    private Timeline collapseAnimation;

    public MissionShellController() {
        instance = this;
    }

    public static MissionShellController getInstance() {
        return instance;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userName.setText("Admin User");
        userRole.setText("Administrateur");

        // Load the logo from resources/images/logo.png
        loadLogo();

        // Debug CSS loading
        debugCSS();

        // Tooltip pour l'avatar
        Tooltip tooltip = new Tooltip("Profil utilisateur");
        Tooltip.install(userAvatar, tooltip);

        // Initialiser le bouton de thème
        setupThemeButton();

        // Configurer les animations
        setupAnimations();

        // Afficher le menu principal par défaut
        Platform.runLater(() -> {
            showMenuPrincipal();
        });

        System.out.println("✅ MissionShellController initialisé avec succès");
    }

    /**
     * Load the logo from resources/images/logo.png
     */
    private void loadLogo() {
        try {
            // Check if brandLogo is properly injected
            if (brandLogo == null) {
                System.err.println("❌ brandLogo is null - check FXML fx:id");
                return;
            }

            // Try multiple possible paths for the logo
            InputStream inputStream = null;
            String[] possiblePaths = {
                    "/images/logo.jpg",           // Your logo path
                    "/fxml/images/logo.jpg",       // Alternative path
                    "/logo.jpg",                    // Root path
                    "/images/mindtrack-logo.jpg",   // Alternative name
                    "/mindtrack-logo.jpg"           // Root with alternative name
            };

            for (String path : possiblePaths) {
                inputStream = getClass().getResourceAsStream(path);
                if (inputStream != null) {
                    System.out.println("✅ Logo found at: " + path);
                    Image image = new Image(inputStream);
                    brandLogo.setImage(image);
                    brandLogo.setFitWidth(36);
                    brandLogo.setFitHeight(36);
                    brandLogo.setPreserveRatio(true);

                    // Add a glowing effect to the logo
                    brandLogo.setStyle("-fx-effect: dropshadow(gaussian, rgba(6,182,212,0.5), 10, 0, 0, 0);");
                    return;
                }
            }

            // If no logo found, use a text emoji as fallback
            System.err.println("❌ Logo not found in any of the attempted paths");
            useFallbackLogo();

        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            useFallbackLogo();
        }
    }

    /**
     * Fallback method when logo is not found
     */
    private void useFallbackLogo() {
        // Since we can't easily replace ImageView with a Label in FXML,
        // we'll hide the ImageView and show a text label in its parent
        if (brandLogo != null) {
            brandLogo.setVisible(false);
            brandLogo.setManaged(false);

            // Get the parent StackPane
            StackPane parent = (StackPane) brandLogo.getParent();

            // Create a text label as fallback
            Label fallbackLabel = new Label("🧠");
            fallbackLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
            StackPane.setAlignment(fallbackLabel, Pos.CENTER);

            // Add it to the parent
            parent.getChildren().add(fallbackLabel);

            System.out.println("✅ Using fallback emoji logo");
        }
    }

    /**
     * Debug CSS loading
     */
    private void debugCSS() {
        System.out.println("=== CSS DEBUG INFO ===");
        System.out.println("Current FXML URL: " + getClass().getResource("/fxml/MissionShell.fxml"));
        System.out.println("CSS URL: " + getClass().getResource("/fxml/mission-shell.css"));
        System.out.println("ClassLoader CSS URL: " + getClass().getClassLoader().getResource("fxml/mission-shell.css"));
        System.out.println("=====================");
    }

    // ============================================
    // GESTION DU THÈME
    // ============================================

    private void setupThemeButton() {
        updateThemeIcon();
        btnTheme.setOnAction(e -> toggleTheme());
    }

    private void toggleTheme() {
        boolean isDarkMode = btnTheme.getText().equals("🌙");
        if (isDarkMode) {
            btnTheme.setText("☀");
            if (sidebar.getScene() != null && sidebar.getScene().getRoot() != null) {
                sidebar.getScene().getRoot().getStyleClass().add("dark-theme");
            }
        } else {
            btnTheme.setText("🌙");
            if (sidebar.getScene() != null && sidebar.getScene().getRoot() != null) {
                sidebar.getScene().getRoot().getStyleClass().remove("dark-theme");
            }
        }
    }

    private void updateThemeIcon() {
        btnTheme.setText("🌙");
    }

    // ============================================
    // ANIMATIONS SIDEBAR
    // ============================================

    private void setupAnimations() {
        expandAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 250, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.minWidthProperty(), 250, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.maxWidthProperty(), 250, Interpolator.EASE_BOTH)
                )
        );

        collapseAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), 70, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.minWidthProperty(), 70, Interpolator.EASE_BOTH),
                        new KeyValue(sidebar.maxWidthProperty(), 70, Interpolator.EASE_BOTH)
                )
        );
    }

    @FXML
    public void expandSidebar() {
        expandAnimation.play();
    }


    @FXML
    public void collapseSidebar() {
        collapseAnimation.play();
    }

    // ============================================
    // MÉTHODES DE NAVIGATION PRINCIPALES
    // ============================================

    @FXML
    public void showMenuPrincipal() {
        loadView("/fxml/MenuPrincipal.fxml");
        setActiveButton(btnMenuPrincipal);
    }

    @FXML
    public void showExercices() {
        loadView("/fxml/ListeExercices.fxml");
        setActiveButton(btnExercices);
    }

    @FXML
    public void showTodo() {
        loadView("/fxml/Todo.fxml");
        setActiveButton(btnTodo);
    }

    @FXML
    public void showSessions() {
        loadView("/fxml/ListeSessions.fxml");
        setActiveButton(btnSessions);
    }

    @FXML
    public void showTableauBord() {
        loadView("/fxml/TableauBord.fxml");
        setActiveButton(btnTableauBord);
    }

    @FXML
    public void showStatistiques() {
        loadView("/fxml/Statistiques.fxml");
        setActiveButton(btnStatistiques);
    }

    @FXML
    public void showHistorique() {
        loadView("/fxml/Historique.fxml");
        setActiveButton(btnHistorique);
    }

    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================

    private void loadView(String fxmlFile) {
        try {
            System.out.println("📂 Chargement: " + fxmlFile);
            URL resourceUrl = getClass().getResource(fxmlFile);
            if (resourceUrl == null) {
                System.err.println("❌ Fichier non trouvé: " + fxmlFile);
                showErrorPlaceholder("Fichier non trouvé: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node view = loader.load();

            // Pass the primary stage to controllers that need it
            Object controller = loader.getController();
            if (controller instanceof MenuPrincipalController) {
                ((MenuPrincipalController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            } else if (controller instanceof ListeExercicesController) {
                ((ListeExercicesController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            } else if (controller instanceof ListeSessionsController) {
                ((ListeSessionsController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            } else if (controller instanceof TableauBordController) {
                ((TableauBordController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            } else if (controller instanceof StatistiquesController) {
                ((StatistiquesController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            } else if (controller instanceof HistoriqueController) {
                ((HistoriqueController) controller).setPrimaryStage(MainApp.getPrimaryStage());
            }

            animateContentChange(view);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorPlaceholder("Erreur: " + fxmlFile);
        }
    }

    private void animateContentChange(Node newView) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            contentPane.getChildren().setAll(newView);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void showErrorPlaceholder(String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: bold;");
        StackPane.setAlignment(errorLabel, Pos.CENTER);
        contentPane.getChildren().setAll(errorLabel);
    }

    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        activeButton = button;
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML
    public void logout() {
        if (showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), sidebar.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                Platform.exit();
                System.exit(0);
            });
            fadeOut.play();
        }
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}