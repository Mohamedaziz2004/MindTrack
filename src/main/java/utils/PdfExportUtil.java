package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExportUtil {

    // Ligne “déjà prête” depuis le controller
    public static class HabitRow {
        public final String name;
        public final String frequency;
        public final String target;
        public final String progress;
        public final String score;

        public HabitRow(String name, String frequency, String target, String progress, String score) {
            this.name = safe(name);
            this.frequency = safe(frequency);
            this.target = safe(target);
            this.progress = safe(progress);
            this.score = safe(score);
        }
    }

    /**
     * Export PDF des habitudes (avec info de tri/recherche en haut).
     * IMPORTANT: on évite les caractères Unicode pour ne pas casser Helvetica.
     */
    public static void exportHabitsPdf(File file,
                                       List<HabitRow> rows,
                                       String search,
                                       String sort) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            float margin = 48f;
            float[] colW = {170, 110, 90, 80, 60}; // total ~510
            String[] header = {"Name", "Frequency", "Target", "Progress", "Score"};

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float pageH = page.getMediaBox().getHeight();
            float y = pageH - margin;

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            // ----- Title
            y = drawTitleAndMeta(cs, margin, y, search, sort);

            // ----- Table header
            drawRow(cs, margin, y, colW, header, true);
            y -= 18;

            // ----- Rows
            if (rows != null) {
                for (HabitRow r : rows) {

                    String[] cells = {
                            normalizePdfText(r.name),
                            normalizePdfText(r.frequency),
                            normalizePdfText(r.target),
                            normalizePdfText(r.progress),
                            normalizePdfText(r.score)
                    };

                    // besoin nouvelle page ?
                    if (y < margin + 40) {
                        cs.close();

                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);

                        pageH = page.getMediaBox().getHeight();
                        y = pageH - margin;

                        cs = new PDPageContentStream(doc, page);

                        // répéter title + header sur chaque page (optionnel mais pro)
                        y = drawTitleAndMeta(cs, margin, y, search, sort);
                        drawRow(cs, margin, y, colW, header, true);
                        y -= 18;
                    }

                    drawRow(cs, margin, y, colW, cells, false);
                    y -= 16;
                }
            }

            cs.close();
            doc.save(file);
        }
    }

    private static float drawTitleAndMeta(PDPageContentStream cs,
                                          float margin,
                                          float y,
                                          String search,
                                          String sort) throws Exception {

        // Title
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
        cs.newLineAtOffset(margin, y);
        cs.showText("MindTrack - Habits Export");
        cs.endText();

        y -= 24;

        // Meta line
        String meta = "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        meta += " | Search: " + safe(search);
        meta += " | Sort: " + safe(sort);

        meta = normalizePdfText(meta);

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(margin, y);
        cs.showText(crop(meta, 120));
        cs.endText();

        y -= 22;
        return y;
    }

    private static void drawRow(PDPageContentStream cs,
                                float x,
                                float y,
                                float[] colW,
                                String[] cells,
                                boolean header) throws Exception {

        cs.beginText();
        cs.setFont(header ? PDType1Font.HELVETICA_BOLD : PDType1Font.HELVETICA, header ? 11 : 10);
        cs.newLineAtOffset(x, y);

        float curX = 0;
        for (int i = 0; i < cells.length; i++) {
            String t = crop(safe(cells[i]), header ? 22 : 26);
            cs.newLineAtOffset(curX, 0);
            cs.showText(t);
            curX = colW[i];
        }

        cs.endText();
    }

    // Remplace caractères non supportés par Helvetica
    private static String normalizePdfText(String s) {
        if (s == null) return "";
        return s
                .replace("→", "->")
                .replace("✓", "OK")
                .replace("✅", "OK")
                .replace("✎", "edit")
                .replace("🗑", "del")
                .replace("＋", "+")
                .replace("⏰", "reminder")
                .replace("\u2192", "->"); // U+2192
    }

    private static String crop(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}