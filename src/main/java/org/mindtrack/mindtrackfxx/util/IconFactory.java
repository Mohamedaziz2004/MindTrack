package org.mindtrack.mindtrackfxx.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * IconFactory — Feather-style outline icons.
 * All paths use a 24×24 coordinate system, 2px stroke, no fill,
 * round caps and round joins for the clean pencil-outline look.
 */
public final class IconFactory {

    private IconFactory() {}

    // ──────────────────────────────────────────────
    //  SIDEBAR ICONS
    // ──────────────────────────────────────────────

    /** Book / Journal — open book with spine and pages */
    public static SVGPath journalIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z "
          + "M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"
        );
        style(p);
        return p;
    }

    /** Target / Goals — concentric circles with centre dot */
    public static SVGPath goalIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z "
          + "M12 18a6 6 0 1 0 0-12 6 6 0 0 0 0 12z "
          + "M12 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"
        );
        style(p);
        return p;
    }

    /** Dumbbell / Exercises — barbell with end-caps */
    public static SVGPath exerciseIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M6.5 6.5h-3a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h3 "
          + "M17.5 6.5h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1h-3 "
          + "M6.5 4v16 M17.5 4v16 "
          + "M6.5 12h11"
        );
        style(p);
        return p;
    }

    /** Check-circle / Habits — circle with interior check-mark */
    public static SVGPath habitsIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M22 11.08V12a10 10 0 1 1-5.93-9.14 "
          + "M22 4L12 14.01l-3-3"
        );
        style(p);
        return p;
    }

    /** User / Profile — head circle + shoulder arc */
    public static SVGPath profileIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 "
          + "M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z"
        );
        style(p);
        return p;
    }

    /** Bar-chart / Statistics — three vertical bars */
    public static SVGPath statisticsIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 20V10 M18 20V4 M6 20v-4"
        );
        style(p);
        return p;
    }

    /** Shield / Admin — shield outline */
    public static SVGPath adminIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"
        );
        style(p);
        return p;
    }

    /** Settings / Gear — Feather settings cog */
    public static SVGPath settingsIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z "
          + "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 "
          + "1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15 "
          + "1.65 1.65 0 0 0 3.13 14H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68 "
          + "1.65 1.65 0 0 0 10 3.13V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9 "
          + "1.65 1.65 0 0 0 20.87 10H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"
        );
        style(p);
        return p;
    }

    /** Log-out — door with arrow pointing right */
    public static SVGPath logoutIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 "
          + "M16 17l5-5-5-5 "
          + "M21 12H9"
        );
        style(p);
        return p;
    }

    // ──────────────────────────────────────────────
    //  CARD / ACTION ICONS
    // ──────────────────────────────────────────────

    /** Edit — pencil */
    public static SVGPath editIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"
        );
        style(p);
        return p;
    }

    /** Trash / Delete */
    public static SVGPath deleteIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M3 6h18 "
          + "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6 "
          + "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2 "
          + "M10 11v6 M14 11v6"
        );
        style(p);
        return p;
    }

    /** Arrow-right / Read More */
    public static SVGPath readMoreIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M5 12h14 M12 5l7 7-7 7"
        );
        style(p);
        return p;
    }

    /** Save / Floppy */
    public static SVGPath saveIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z "
          + "M17 21v-8H7v8 "
          + "M7 3v5h8"
        );
        style(p);
        return p;
    }

    /** Search / Analyse */
    public static SVGPath analyseIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z "
          + "M21 21l-4.35-4.35 "
          + "M11 8v6 M8 11h6"
        );
        style(p);
        return p;
    }

    /** Alias for statisticsIcon */
    public static SVGPath statsIcon() {
        return statisticsIcon();
    }

    /** Sort / Arrow-up-down — sort by date toggle icon */
    public static SVGPath sortIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M3 6h18 "
          + "M3 12h12 "
          + "M3 18h6 "
          + "M19 15l-3 3-3-3 "
          + "M16 18V9"
        );
        style(p);
        return p;
    }

    /** Search — clean magnifying glass */
    public static SVGPath searchIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z "
          + "M21 21l-4.35-4.35"
        );
        style(p);
        return p;
    }

    // ──────────────────────────────────────────────
    //  DISPATCHER
    // ──────────────────────────────────────────────

    /** Globe / Languages — globe with meridians for translation */
    public static SVGPath translateIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z "
          + "M2 12h20 "
          + "M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"
        );
        style(p);
        return p;
    }

    /** Microphone — classic mic icon for audio/transcription */
    public static SVGPath microphoneIcon() {
        SVGPath p = new SVGPath();
        p.setContent(
            "M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z "
          + "M19 10v2a7 7 0 0 1-14 0v-2 "
          + "M12 19v4 "
          + "M8 23h8"
        );
        style(p);
        return p;
    }

    public static SVGPath getIcon(String name) {
        switch (name.toLowerCase()) {
            case "journal":      return journalIcon();
            case "goals":        return goalIcon();
            case "exercise":     return exerciseIcon();
            case "habits":       return habitsIcon();
            case "profile":      return profileIcon();
            case "statistics":   return statisticsIcon();
            case "admin":        return adminIcon();
            case "settings":     return settingsIcon();
            case "logout":       return logoutIcon();
            case "edit":         return editIcon();
            case "delete":       return deleteIcon();
            case "read more":    return readMoreIcon();
            case "save":         return saveIcon();
            case "analyse":      return analyseIcon();
            case "translate":    return translateIcon();
            case "sort":         return sortIcon();
            case "search":       return searchIcon();
            case "microphone":   return microphoneIcon();
            case "audio":        return microphoneIcon();
            case "transcription": return microphoneIcon();
            default:             return journalIcon();
        }
    }

    // ──────────────────────────────────────────────
    //  SHARED STYLE  (Feather: no fill, 2px stroke, round everything)
    // ──────────────────────────────────────────────

    private static void style(SVGPath p) {
        p.getStyleClass().add("svg-icon");
        p.setFill(Color.TRANSPARENT);
        p.setStroke(Color.web("#9ca3af"));
        p.setStrokeWidth(1.8);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        p.setScaleX(0.75);
        p.setScaleY(0.75);
    }
}
