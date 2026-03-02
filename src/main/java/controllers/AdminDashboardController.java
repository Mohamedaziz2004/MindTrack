package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import services.UtilisateurService;
import utils.UserSession;
import utils.WindowBarHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class AdminDashboardController {

    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> idColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> prenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, Integer> ageColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, Void> actionColumn;
    @FXML private Label messageLabel;
    @FXML private ImageView headerProfileImageView;
    @FXML private Label headerEmailLabel;

    private static final String DEFAULT_PROFILE_PICTURE = "/pfp_temp.png";

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    public void initialize() {
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("idU"));
        }
        if (nomColumn != null) {
            nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomU"));
        }
        if (prenomColumn != null) {
            prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenomU"));
        }
        if (emailColumn != null) {
            emailColumn.setCellValueFactory(new PropertyValueFactory<>("emailU"));
        }
        if (ageColumn != null) {
            ageColumn.setCellValueFactory(new PropertyValueFactory<>("ageU"));
        }
        if (roleColumn != null) {
            roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
            roleColumn.setCellFactory(ComboBoxTableCell.forTableColumn("USER", "ADMIN"));
            roleColumn.setOnEditCommit(event -> {
                Utilisateur user = event.getRowValue();
                String newRole = event.getNewValue();
                if (user == null || newRole == null || newRole.equalsIgnoreCase(user.getRole())) {
                    return;
                }
                try {
                    userService.updateRole(user.getIdU(), newRole);
                    user.setRole(newRole);
                } catch (SQLException e) {
                    if (messageLabel != null) {
                        messageLabel.setText("Unable to update role.");
                    }
                    e.printStackTrace();
                }
            });
        }

        if (usersTable != null) {
            usersTable.setEditable(true);
        }

        if (actionColumn != null) {
            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button openButton = new Button("Open Profile");

                {
                    openButton.getStyleClass().add("secondary-button");
                    openButton.setOnAction(e -> {
                        Utilisateur user = getTableView().getItems().get(getIndex());
                        if (user != null) {
                            openUserProfile(user);
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(openButton);
                    }
                }
            });
        }

        loadUsers();
        loadHeaderUser();
    }

    private void loadHeaderUser() {
        Utilisateur currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        if (headerEmailLabel != null) {
            headerEmailLabel.setText(currentUser.getEmailU());
        }
        loadHeaderProfilePicture(currentUser);
    }

    private void loadHeaderProfilePicture(Utilisateur currentUser) {
        if (currentUser == null || headerProfileImageView == null) {
            return;
        }
        Image image = null;
        String path = currentUser.getProfilePicturePath();
        if (path != null && !path.isBlank()) {
            Path filePath = Paths.get(path);
            if (Files.exists(filePath)) {
                image = new Image(filePath.toUri().toString(), true);
            }
        }
        if (image == null) {
            var resource = getClass().getResource(DEFAULT_PROFILE_PICTURE);
            if (resource != null) {
                image = new Image(resource.toExternalForm(), true);
            }
        }
        if (image != null) {
            headerProfileImageView.setImage(image);
            applyCircularClip(headerProfileImageView);
        }
    }

    private void applyCircularClip(ImageView target) {
        if (target == null) {
            return;
        }
        target.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double radius = Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0;
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                    newBounds.getMinX() + newBounds.getWidth() / 2.0,
                    newBounds.getMinY() + newBounds.getHeight() / 2.0,
                    radius
            );
            target.setClip(clip);
        });
    }

    private void loadUsers() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        try {
            List<Utilisateur> users = userService.read();
            if (usersTable != null) {
                usersTable.getItems().setAll(users);
            }
        } catch (SQLException e) {
            if (messageLabel != null) {
                messageLabel.setText("Unable to load users.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            UserSession.clear();

            Stage adminStage = (Stage) usersTable.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.getIcons().setAll(
                    new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")))
            );

            Parent wrapped = WindowBarHelper.wrap(root, loginStage, true, false);
            Scene scene = new Scene(wrapped);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

            loginStage.setScene(scene);
            WindowBarHelper.applyFixedLoginWindow(loginStage);
            loginStage.show();

            adminStage.close();
        } catch (IOException e) {
            if (messageLabel != null) {
                messageLabel.setText("Logout error.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHeaderProfileClick() {
        // Admin stays on dashboard; hook for future profile route.
    }

    private void openUserProfile(Utilisateur user) {
        try {
            UserSession.setViewedUser(user);

            Stage adminStage = (Stage) usersTable.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();

            Stage profileStage = new Stage();
            profileStage.initStyle(StageStyle.UNDECORATED);
            profileStage.getIcons().setAll(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")))
            );

            Parent wrapped = WindowBarHelper.wrap(root, profileStage, false, false);
            Scene scene = new Scene(wrapped);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
            profileStage.setScene(scene);
            WindowBarHelper.applyFullScreenWindow(profileStage);
            profileStage.show();

            adminStage.close();
        } catch (IOException e) {
            if (messageLabel != null) {
                messageLabel.setText("Unable to open profile.");
            }
            e.printStackTrace();
        }
    }
}
