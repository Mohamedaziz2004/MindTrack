package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.example.model.Objectif;

import java.util.function.Consumer;

public class GoalCardController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label statusBadge;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label dateLabel;
    @FXML
    private Button detailsBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button deleteBtn;

    /**
     * Remplit la carte d'objectif avec les données et définit les actions des
     * boutons.
     * 
     * @param objectif  L'objectif à afficher.
     * @param onDetails Action lors du clic sur détails.
     * @param onEdit    Action lors du clic sur modifier.
     * @param onDelete  Action lors du clic sur supprimer.
     */
    public void setData(Objectif objectif, Consumer<Objectif> onDetails, Consumer<Objectif> onEdit,
            Consumer<Objectif> onDelete) {
        titleLabel.setText(objectif.getTitre());
        descriptionLabel.setText(objectif.getDescription());

        int progression = objectif.calculateProgression();
        progressPercentLabel.setText(progression + "%");
        progressBar.setProgress(progression / 100.0);

        dateLabel.setText(objectif.getDateDebut() + " - " + objectif.getDateFin());

        // Status styling
        statusBadge.setText(objectif.getStatut().toUpperCase());
        String badgeStyle = "-fx-padding: 2 6; -fx-background-radius: 8; -fx-font-size: 9px; -fx-font-weight: 900; -fx-text-fill: white;";
        String bgColor = "#718096"; // Default

        if (objectif.getStatut().equalsIgnoreCase("Complétée")) {
            bgColor = "#48bb78";
        } else if (objectif.getStatut().equalsIgnoreCase("En cours")) {
            bgColor = "#ed8936";
        }

        statusBadge.setStyle(badgeStyle + " -fx-background-color: " + bgColor + ";");

        // Action handlers
        detailsBtn.setOnAction(e -> onDetails.accept(objectif));
        editBtn.setOnAction(e -> onEdit.accept(objectif));
        deleteBtn.setOnAction(e -> onDelete.accept(objectif));
    }
}
