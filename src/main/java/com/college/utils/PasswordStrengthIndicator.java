package com.college.utils;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class PasswordStrengthIndicator {

    public enum Strength {
        WEAK("Weak", Color.web("#ef4444")),
        MEDIUM("Medium", Color.web("#f59e0b")),
        STRONG("Strong", Color.web("#22c55e"));

        private final String label;
        private final Color color;

        Strength(String label, Color color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() {
            return label;
        }

        public Color getColor() {
            return color;
        }
    }

    public static Strength checkStrength(String password) {
        if (password == null || password.length() < 6) {
            return Strength.WEAK;
        }

        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");

        int score = 0;
        if (password.length() >= 8)
            score++;
        if (hasNumber)
            score++;
        if (hasSpecial)
            score++;
        if (hasUpper && hasLower)
            score++;

        if (score >= 3)
            return Strength.STRONG;
        if (score >= 2)
            return Strength.MEDIUM;
        return Strength.WEAK;
    }

    public static HBox createIndicator(PasswordField passwordField) {
        HBox indicator = new HBox(5);
        Label strengthLabel = new Label("Password: ");
        Label strengthValue = new Label("");

        indicator.getChildren().addAll(strengthLabel, strengthValue);
        indicator.setStyle("-fx-padding: 5 0 0 0;");

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                strengthValue.setText("");
                return;
            }

            Strength strength = checkStrength(newVal);
            strengthValue.setText(strength.getLabel());
            strengthValue.setTextFill(strength.getColor());
            strengthValue.setStyle("-fx-font-weight: bold;");
        });

        return indicator;
    }
}
