package com.college.fx.views;

import com.college.utils.DatabaseConnection;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * JavaFX Change Password View
 */
public class ChangePasswordView {

    private VBox root;
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Label messageLabel;
    private int userId;

    public ChangePasswordView(int userId) {
        this.userId = userId;
        createView();
    }

    private void createView() {
        root = new VBox(25);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Form card
        VBox formCard = new VBox(25);
        formCard.setPadding(new Insets(40));
        formCard.setMaxWidth(450);
        formCard.setAlignment(Pos.CENTER);
        formCard.getStyleClass().add("glass-card");

        // Title
        Label title = new Label("Change Password");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        // title.setTextFill(Color.web("#0f172a"));

        Label subtitle = new Label("Enter your current password and choose a new one");
        subtitle.setFont(Font.font("Segoe UI", 14));
        // subtitle.setTextFill(Color.web("#64748b"));

        // Form fields
        VBox fieldsBox = new VBox(18);
        fieldsBox.setMaxWidth(350);

        currentPasswordField = createPasswordField("Current Password");
        newPasswordField = createPasswordField("New Password");
        confirmPasswordField = createPasswordField("Confirm New Password");

        fieldsBox.getChildren().addAll(
                createFieldBox("Current Password", currentPasswordField),
                createFieldBox("New Password", newPasswordField),
                createFieldBox("Confirm New Password", confirmPasswordField));

        // Submit button
        Button submitBtn = new Button("Change Password");
        submitBtn.setPrefWidth(200);
        submitBtn.setPrefHeight(45);
        submitBtn.setStyle(
                "-fx-background-color: #14b8a6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        submitBtn.setOnAction(e -> changePassword());

        // Message label
        messageLabel = new Label();
        messageLabel.setFont(Font.font("Segoe UI", 13));
        messageLabel.setWrapText(true);

        formCard.getChildren().addAll(title, subtitle, fieldsBox, submitBtn, messageLabel);
        root.getChildren().add(formCard);
    }

    private VBox createFieldBox(String labelText, PasswordField field) {
        VBox box = new VBox(6);
        Label label = new Label(labelText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        // label.setTextFill(Color.web("#475569"));
        box.getChildren().addAll(label, field);
        return box;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(45);
        field.setStyle(
                "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 14px;");
        return field;
    }

    private void changePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields.", true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("New passwords do not match.", true);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("New password must be at least 6 characters.", true);
            return;
        }

        // Verify current password and update
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verify current password
            String storedHash = null;
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE id = ?")) {
                pstmt.setInt(1, userId);
                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        storedHash = rs.getString("password");
                    }
                }
            }

            if (storedHash == null || !com.college.utils.PasswordUtils.verifyPassword(currentPassword, storedHash)) {
                showMessage("Current password is incorrect.", true);
                return;
            }

            // Update password
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setString(1, com.college.utils.PasswordUtils.hashPassword(newPassword));
                pstmt.setInt(2, userId);
                if (pstmt.executeUpdate() > 0) {
                    showMessage("Password changed successfully!", false);
                    clearFields();
                } else {
                    showMessage("Failed to change password.", true);
                }
            }
        } catch (Exception e) {
            com.college.utils.Logger.error("Error changing password", e);
            showMessage("Error: " + e.getMessage(), true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setTextFill(isError ? Color.web("#ef4444") : Color.web("#22c55e"));
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    public VBox getView() {
        return root;
    }
}
