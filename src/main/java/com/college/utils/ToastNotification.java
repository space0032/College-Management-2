package com.college.utils;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Toast Notification Utility
 * Displays temporary notification messages with auto-dismiss
 */
public class ToastNotification {

    public enum Type {
        SUCCESS("#22c55e", "✓", 3000), // Green, 3 seconds
        ERROR("#ef4444", "✗", 0), // Red, manual dismiss
        INFO("#3b82f6", "ℹ", 5000), // Blue, 5 seconds
        WARNING("#f59e0b", "⚠", 0); // Orange, manual dismiss

        private final String color;
        private final String icon;
        private final long duration; // 0 = manual dismiss

        Type(String color, String icon, long duration) {
            this.color = color;
            this.icon = icon;
            this.duration = duration;
        }

        public String getColor() {
            return color;
        }

        public String getIcon() {
            return icon;
        }

        public long getDuration() {
            return duration;
        }
    }

    public static void show(StackPane container, String message, Type type) {
        // Create toast
        VBox toast = new VBox(5);
        toast.setAlignment(Pos.CENTER);
        toast.setStyle(
                "-fx-background-color: " + type.getColor() + ";" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 20 12 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");

        // Icon + Message label
        Label label = new Label(type.getIcon() + "  " + message);
        label.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;");
        toast.getChildren().add(label);

        // Position in top-right
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);
        StackPane.setMargin(toast, new javafx.geometry.Insets(20, 20, 0, 0));

        // Add to container
        container.getChildren().add(toast);

        // Slide in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toast);
        slideIn.setFromX(400);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition showTransition = new ParallelTransition(slideIn, fadeIn);
        showTransition.play();

        // Auto-dismiss or manual close button
        if (type.getDuration() > 0) {
            // Auto-dismiss after duration
            PauseTransition pause = new PauseTransition(Duration.millis(type.getDuration()));
            pause.setOnFinished(e -> dismissToast(container, toast));
            pause.play();
        } else {
            // Add close button for manual dismiss
            Label closeBtn = new Label("✕");
            closeBtn.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0 0 0 10;");
            closeBtn.setOnMouseClicked(e -> dismissToast(container, toast));

            // Change layout to HBox for close button
            javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(10);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(label, closeBtn);
            toast.getChildren().clear();
            toast.getChildren().add(content);
        }
    }

    private static void dismissToast(StackPane container, VBox toast) {
        // Slide out animation
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toast);
        slideOut.setToX(400);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        // Fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
        fadeOut.setToValue(0);

        ParallelTransition hideTransition = new ParallelTransition(slideOut, fadeOut);
        hideTransition.setOnFinished(e -> container.getChildren().remove(toast));
        hideTransition.play();
    }

    // Convenience methods
    public static void success(StackPane container, String message) {
        show(container, message, Type.SUCCESS);
    }

    public static void error(StackPane container, String message) {
        show(container, message, Type.ERROR);
    }

    public static void info(StackPane container, String message) {
        show(container, message, Type.INFO);
    }

    public static void warning(StackPane container, String message) {
        show(container, message, Type.WARNING);
    }
}
