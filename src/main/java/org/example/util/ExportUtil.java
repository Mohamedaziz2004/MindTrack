package org.example.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.model.Objectif;
import org.example.model.JalonProgression;
import org.example.model.PlanAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportUtil {

    private static final String APP_NAME = "MindTrack";
    private static final String SKY_BLUE = "#4A9EEB";

    /**
     * Exporte la liste des objectifs vers un fichier PDF stylisé.
     * 
     * @param goals Liste des objectifs.
     * @param stage Fenêtre parente pour le sélecteur de fichiers.
     */
    public static void exportGoalsToPDF(List<Objectif> goals, Stage stage) {
        File file = getSaveLocation("MindTrack_Goals.pdf", "PDF Files", "*.pdf", stage);
        if (file == null)
            return;

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Header
            addHeader(document, "Personal Goals Report");

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);

            addTableHeader(table, new String[] { "Title", "Status", "Due Date", "Progress" });

            for (Objectif goal : goals) {
                table.addCell(createStyledCell(goal.getTitre()));
                table.addCell(createStyledCell(goal.getStatut()));
                table.addCell(createStyledCell(goal.getDateFin().toString()));
                table.addCell(createStyledCell(String.format("%.1f%%", (double) goal.calculateProgression())));
            }

            document.add(table);
            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exporte les jalons de progression vers un fichier PDF.
     * 
     * @param milestones Liste des jalons.
     * @param stage      Fenêtre parente.
     */
    public static void exportMilestonesToPDF(List<JalonProgression> milestones, Stage stage) {
        File file = getSaveLocation("MindTrack_Milestones.pdf", "PDF Files", "*.pdf", stage);
        if (file == null)
            return;

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addHeader(document, "Milestones & Progress Report");

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);

            addTableHeader(table, new String[] { "Milestone", "Status", "Target Date" });

            for (JalonProgression milestone : milestones) {
                table.addCell(createStyledCell(milestone.getTitre()));
                table.addCell(createStyledCell(milestone.isAtteint() ? "✅ Reached" : "⏳ Pending"));
                table.addCell(createStyledCell(milestone.getDateCible().toString()));
            }

            document.add(table);
            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exporte le plan d'action vers un fichier PDF.
     * 
     * @param actions Liste des étapes d'action.
     * @param stage   Fenêtre parente.
     */
    public static void exportActionsToPDF(List<PlanAction> actions, Stage stage) {
        File file = getSaveLocation("MindTrack_Actions.pdf", "PDF Files", "*.pdf", stage);
        if (file == null)
            return;

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addHeader(document, "Action Plan Summary");

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);

            addTableHeader(table, new String[] { "Step Description", "Priority" });

            for (PlanAction action : actions) {
                table.addCell(createStyledCell(action.getEtape()));
                table.addCell(createStyledCell(action.getPrioriteLabel()));
            }

            document.add(table);
            addFooter(document);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère un fichier CSV contenant toutes les données des objectifs.
     * 
     * @param goals Liste des objectifs.
     * @param stage Fenêtre parente.
     */
    public static void exportGoalsToCSV(List<Objectif> goals, Stage stage) {
        File file = getSaveLocation("MindTrack_Goals.csv", "CSV Files", "*.csv", stage);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.append("ID,Title,Description,Status,Start Date,End Date,Progress\n");
            for (Objectif goal : goals) {
                writer.append(String.format("%d,\"%s\",\"%s\",\"%s\",%s,%s,%.1f%%\n",
                        goal.getIdObj(),
                        goal.getTitre().replace("\"", "\"\""),
                        goal.getDescription().replace("\"", "\"\""),
                        goal.getStatut(),
                        goal.getDateDebut(),
                        goal.getDateFin(),
                        goal.calculateProgression()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère un fichier CSV pour les jalons de progression.
     * 
     * @param milestones Liste des jalons.
     * @param stage      Fenêtre parente.
     */
    public static void exportMilestonesToCSV(List<JalonProgression> milestones, Stage stage) {
        File file = getSaveLocation("MindTrack_Milestones.csv", "CSV Files", "*.csv", stage);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.append("ID,Goal ID,Title,Target Date,Reached\n");
            for (JalonProgression milestone : milestones) {
                writer.append(String.format("%d,%d,\"%s\",%s,%s\n",
                        milestone.getIdJalon(),
                        milestone.getIdObj(),
                        milestone.getTitre().replace("\"", "\"\""),
                        milestone.getDateCible(),
                        milestone.isAtteint()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Génère un fichier CSV pour les étapes du plan d'action.
     * 
     * @param actions Liste des actions.
     * @param stage   Fenêtre parente.
     */
    public static void exportActionsToCSV(List<PlanAction> actions, Stage stage) {
        File file = getSaveLocation("MindTrack_Actions.csv", "CSV Files", "*.csv", stage);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.append("ID,Goal ID,Step,Priority\n");
            for (PlanAction action : actions) {
                writer.append(String.format("%d,%d,\"%s\",\"%s\"\n",
                        action.getIdPlan(),
                        action.getIdObj(),
                        action.getEtape().replace("\"", "\"\""),
                        action.getPrioriteLabel()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addHeader(Document document, String title) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, java.awt.Color.decode(SKY_BLUE));
        Paragraph header = new Paragraph(APP_NAME, titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, java.awt.Color.GRAY);
        Paragraph subHeader = new Paragraph(title, subTitleFont);
        subHeader.setAlignment(Element.ALIGN_CENTER);
        subHeader.setSpacingAfter(10);
        document.add(subHeader);

        document.add(new Paragraph("\n"));
    }

    private static void addTableHeader(PdfPTable table, String[] columnTitles) {
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.WHITE);
        for (String columnTitle : columnTitles) {
            PdfPCell header = new PdfPCell(new Phrase(columnTitle, headFont));
            header.setBackgroundColor(java.awt.Color.decode(SKY_BLUE));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            header.setBorderWidth(1);
            table.addCell(header);
        }
    }

    private static PdfPCell createStyledCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
        cell.setPadding(8);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private static void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, java.awt.Color.LIGHT_GRAY);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Paragraph footer = new Paragraph("\nGenerated by MindTrack on " + timestamp, footerFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);
    }

    private static File getSaveLocation(String initialName, String filterName, String filterExt, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Export");
        fileChooser.setInitialFileName(initialName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, filterExt));
        return fileChooser.showSaveDialog(stage);
    }
}
