package org.mindtrack.mindtrackfxx.util;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;

/**
 * IconFactory provides Lucide-style SVGPath icons for the UI.
 * All icons are scaled to fit a 24x24 coordinate system, with thin white strokes and rounded line caps.
 */
public class IconFactory {

    public static SVGPath journalIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M4 19.5A2.5 2.5 0 0 1 6.5 17H20 M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z M16 4v10");
        style(path);
        return path;
    }

    public static SVGPath profileIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2 M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z");
        style(path);
        return path;
    }

    public static SVGPath adminIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z M12 8v4 M12 16h.01");
        style(path);
        return path;
    }

    public static SVGPath analyseIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M21 21l-4.35-4.35M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z M11 8v6 M8 11h6");
        style(path);
        return path;
    }

    public static SVGPath deleteIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2");
        style(path);
        return path;
    }

    public static SVGPath editIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z");
        style(path);
        return path;
    }

    public static SVGPath exerciseIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M18 8h3a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1h-3M6 8H3a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3M6 5v14M18 5v14M6 12h12");
        style(path);
        return path;
    }

    public static SVGPath goalIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10zm0-10l4 4m-4-4v-4m0 4h-4");
        style(path);
        return path;
    }

    public static SVGPath habitsIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M17 21l-5-4-5 4V5a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v16z");
        style(path);
        return path;
    }

    public static SVGPath logoutIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9");
        style(path);
        return path;
    }

    public static SVGPath readMoreIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M1 12h22M17 5l7 7-7 7");
        style(path);
        return path;
    }

    public static SVGPath saveIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2zM17 21v-8H7v8M7 3v5h8");
        style(path);
        return path;
    }

    public static SVGPath settingsIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M12.22 2h-0.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-0.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.1a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h0.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l0.22-.39a2 2 0 0 0-.73-2.73l-.15-.1a2 2 0 0 1-1-1.72v-.51a2 2 0 0 1 1-1.74l.15-.1a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2zM12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z");
        style(path);
        return path;
    }

    public static SVGPath statisticsIcon() {
        SVGPath path = new SVGPath();
        path.setContent("M18 20V10M12 20V4M6 20v-6");
        style(path);
        return path;
    }

    public static SVGPath statsIcon() {
        // Bar chart icon for statistics
        SVGPath path = new SVGPath();
        path.setContent("M18 20V10M12 20V4M6 20v-6"); // same as statisticsIcon for consistency
        style(path);
        return path;
    }

    private static void style(SVGPath path) {
        path.setStroke(Color.WHITE);
        path.setFill(Color.TRANSPARENT);
        path.setStrokeWidth(1.5);
        path.setStrokeLineCap(StrokeLineCap.ROUND);
        path.setScaleX(24.0 / 24.0);
        path.setScaleY(24.0 / 24.0);
    }
}
