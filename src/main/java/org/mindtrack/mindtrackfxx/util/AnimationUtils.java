package org.mindtrack.mindtrackfxx.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class for creating and applying animations to JavaFX nodes.
 * Provides reusable animation methods for consistent UI effects.
 */
public final class AnimationUtils {

    private AnimationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Fades in a node from invisible to visible.
     */
    public static void fadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Slides a node in from the specified X position with fade effect.
     */
    public static void slideInFromX(Node node, int durationMs, double fromX) {
        node.setTranslateX(fromX);
        node.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromX(fromX);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
    }

    /**
     * Slides a node up from the specified Y position with fade effect.
     */
    public static void slideUp(Node node, int durationMs, double fromY) {
        node.setTranslateY(fromY);
        node.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(fromY);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
    }

    /**
     * Scales a node in with a pop effect and fade.
     */
    public static void scaleIn(Node node, int durationMs) {
        node.setScaleX(0.8);
        node.setScaleY(0.8);
        node.setOpacity(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        new ParallelTransition(scale, fade).play();
    }

    /**
     * Creates a quick pulse effect on a node.
     */
    public static void pulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), node);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    /**
     * Animates a card entrance with staggered delay based on index.
     */
    public static void cardEntrance(Node card, int index) {
        card.setOpacity(0);
        card.setTranslateY(30);

        PauseTransition pause = new PauseTransition(Duration.millis(index * 80));
        pause.setOnFinished(e -> slideUp(card, 400, 30));
        pause.play();
    }

    /**
     * Adds hover scale animation to a node.
     */
    public static void addHoverEffect(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });
    }

    /**
     * Button click animation effect.
     */
    public static void buttonClick(Node button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1);
        scaleUp.setToY(1);

        new SequentialTransition(scaleDown, scaleUp).play();
    }
}
