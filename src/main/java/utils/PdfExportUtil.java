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
     * IMPORTANT: on évite les caractères Unicode (→ ✓ …) pour ne pas casser Helvetica.
     */
    public static void exportHabitsPdf(File file,
                                       List<HabitRow> rows,
                                       String search,
                                       String sort) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 48f;
            float y = page.getMediaBox().getHeight() - margin;

            float leading = 14f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

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
                cs.showText(crop(meta, 110));
                cs.endText();

                y -= 22;

                // Table header
                float[] colW = {170, 110, 90, 80, 60}; // total ~510
                String[] header = {"Name", "Frequency", "Target", "Progress", "Score"};

                drawRow(cs, margin, y, colW, header, true);
                y -= 18;

                // Rows
                if (rows != null) {
                    for (HabitRow r : rows) {
                        String[] cells = {
                                normalizePdfText(r.name),
                                normalizePdfText(r.frequency),
                                normalizePdfText(r.target),
                                normalizePdfText(r.progress),
                                normalizePdfText(r.score)
                        };

                        // si on dépasse la page -> nouvelle page
                        if (y < margin + 60) {
                            cs.close();

                            page = new PDPage(PDRectangle.A4);
                            doc.addPage(page);
                            y = page.getMediaBox().getHeight() - margin;

                            try (PDPageContentStream cs2 = new PDPageContentStream(doc, page)) {
                                // redessiner header sur la nouvelle page
                                drawRow(cs2, margin, y, colW, header, true);
                                y -= 18;

                                drawRow(cs2, margin, y, colW, cells, false);
                                y -= 16;

                                // on continue avec cs2 : astuce => on relance une nouvelle fonction
                                // mais plus simple: on sauvegarde maintenant et on retourne
                                // => donc pour multi-pages clean, on fait une version simple:
                                // ici on va juste continuer avec cs2 en écrivant le reste dans cette page.
                                // (On ne peut pas “revenir” à cs d’avant, mais c’est ok)
                                for (int i = rows.indexOf(r) + 1; i < rows.size(); i++) {
                                    HabitRow rr = rows.get(i);
                                    String[] cc = {
                                            normalizePdfText(rr.name),
                                            normalizePdfText(rr.frequency),
                                            normalizePdfText(rr.target),
                                            normalizePdfText(rr.progress),
                                            normalizePdfText(rr.score)
                                    };
                                    if (y < margin + 40) break; // simple (une page en plus max)
                                    drawRow(cs2, margin, y, colW, cc, false);
                                    y -= 16;
                                }
                            }

                            doc.save(file);
                            return;
                        }

                        drawRow(cs, margin, y, colW, cells, false);
                        y -= 16;
                    }
                }

            }

            doc.save(file);
        }
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