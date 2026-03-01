package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Overlays floating minimize / close buttons on the top-right of the window,
 * and an optional logo on the top-left.  The area between them is an invisible
 * drag region so the user can move the undecorated window.
 */
public final class WindowBarHelper {

    private static double xOffset = 0;
    private static double yOffset = 0;

    private WindowBarHelper() {}

    /**
     * @param content      the page root loaded from FXML
     * @param stage        the current UNDECORATED stage
     * @param darkButtons  true → light text on dark bg; false → dark text on light bg
     * @param showLogo     show logo + "MindTrack" on the top-left
     */
    public static StackPane wrap(Node content, Stage stage,
                                  boolean darkButtons, boolean showLogo) {

        String fg         = darkButtons ? "rgba(255,255,255,0.9)" : "rgba(0,0,0,0.75)";
        String btnBg      = darkButtons ? "rgba(0,0,0,0.35)"      : "rgba(0,0,0,0.08)";
        String btnHoverBg = darkButtons ? "rgba(0,0,0,0.55)"      : "rgba(0,0,0,0.18)";

        /* ---- buttons ---- */
        Button minimize = pillButton("—", btnBg, fg);
        minimize.setOnAction(e -> stage.setIconified(true));
        minimize.setCursor(Cursor.HAND);
        addHover(minimize, btnHoverBg, btnBg, fg);

        Button close = pillButton("✕", btnBg, fg);
        close.setOnAction(e -> { stage.close(); javafx.application.Platform.exit(); });
        close.setCursor(Cursor.HAND);
        close.setOnMouseEntered(e -> close.setStyle(pillStyle("rgba(220,50,50,0.85)", "white")));
        close.setOnMouseExited(e  -> close.setStyle(pillStyle(btnBg, fg)));

        HBox btnBox = new HBox(6, minimize, close);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(0));
        btnBox.setPickOnBounds(false);

        /* ---- optional logo ---- */
        HBox logoBox = new HBox(8);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPickOnBounds(false);
        if (showLogo) {
            ImageView logo = new ImageView(
                    new Image(WindowBarHelper.class.getResourceAsStream("/logo.png")));
            logo.setFitHeight(18);
            logo.setFitWidth(18);
            logo.setPreserveRatio(true);
            Label title = new Label("MindTrack");
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + fg + ";");
            logoBox.getChildren().addAll(logo, title);
        }

        /* ---- invisible drag region (fills the top strip) ---- */
        Region dragRegion = new Region();
        dragRegion.setPrefHeight(38);
        dragRegion.setMaxHeight(38);
        dragRegion.setStyle("-fx-background-color: transparent;");
        dragRegion.setCursor(Cursor.MOVE);
        dragRegion.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        dragRegion.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
        });

        /* ---- anchor pane to pin elements ---- */
        AnchorPane bar = new AnchorPane(dragRegion, logoBox, btnBox);

        // drag region fills the full width
        AnchorPane.setTopAnchor(dragRegion, 0.0);
        AnchorPane.setLeftAnchor(dragRegion, 0.0);
        AnchorPane.setRightAnchor(dragRegion, 0.0);

        // logo top-left
        AnchorPane.setTopAnchor(logoBox, 10.0);
        AnchorPane.setLeftAnchor(logoBox, 14.0);

        // buttons top-right
        AnchorPane.setTopAnchor(btnBox, 8.0);
        AnchorPane.setRightAnchor(btnBox, 12.0);

        bar.setPrefHeight(38);
        bar.setMaxHeight(38);
        bar.setPickOnBounds(false);           // let clicks pass through to content below
        bar.setMouseTransparent(false);

        /* ---- stack bar on top of content ---- */
        StackPane stack = new StackPane(content, bar);
        StackPane.setAlignment(bar, Pos.TOP_CENTER);

        return stack;
    }

    /* ---------- helpers ---------- */

    private static Button pillButton(String symbol, String bg, String fg) {
        Button btn = new Button(symbol);
        btn.setStyle(pillStyle(bg, fg));
        btn.setFocusTraversable(false);
        return btn;
    }

    private static void addHover(Button btn, String hoverBg, String normalBg, String fg) {
        btn.setOnMouseEntered(e -> btn.setStyle(pillStyle(hoverBg, fg)));
        btn.setOnMouseExited(e  -> btn.setStyle(pillStyle(normalBg, fg)));
    }

    private static String pillStyle(String bg, String fg) {
        return "-fx-background-color: " + bg + ";"
             + "-fx-text-fill: " + fg + ";"
             + "-fx-font-size: 13px;"
             + "-fx-font-weight: bold;"
             + "-fx-background-radius: 8;"
             + "-fx-padding: 4 12;"
             + "-fx-cursor: hand;"
             + "-fx-min-width: 32;"
             + "-fx-min-height: 26;";
    }
}

