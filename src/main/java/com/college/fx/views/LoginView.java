package com.college.fx.views;

import com.college.utils.DatabaseConnection;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * JavaFX Login View
 * Modern login screen with AtlantaFX styling
 */
public class LoginView {

    private BorderPane root;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private Label messageLabel;

    public LoginView() {
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        // Center content
        VBox centerBox = new VBox(25);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(40));
        centerBox.setMaxWidth(420);
        centerBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8);");

        // Logo/Title section
        VBox titleBox = new VBox(8);
        titleBox.setAlignment(Pos.CENTER);

        Text titleText = new Text("College Management");
        titleText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleText.setFill(Color.web("#0f172a"));

        Text subtitleText = new Text("System");
        subtitleText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 20));
        subtitleText.setFill(Color.web("#64748b"));

        Text welcomeText = new Text("Welcome back! Please sign in.");
        welcomeText.setFont(Font.font("Segoe UI", 14));
        welcomeText.setFill(Color.web("#94a3b8"));

        titleBox.getChildren().addAll(titleText, subtitleText, welcomeText);

        // Form fields
        VBox formBox = new VBox(18);
        formBox.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameBox = new VBox(6);
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(45);
        usernameField.setStyle(
                "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 14px;");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(6);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
                "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;" +
                        "-fx-font-size: 14px;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Role dropdown
        VBox roleBox = new VBox(6);
        Label roleLabel = new Label("Login As");
        roleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-font-weight: bold;");
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("ADMIN", "FACULTY", "STUDENT", "WARDEN", "FINANCE");
        roleComboBox.setValue("ADMIN");
        roleComboBox.setPrefHeight(45);
        roleComboBox.setMaxWidth(Double.MAX_VALUE);
        roleComboBox.setStyle(
                "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-font-size: 14px;");
        roleBox.getChildren().addAll(roleLabel, roleComboBox);

        // Login button
        Button loginButton = new Button("Sign In");
        loginButton.setPrefHeight(48);
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.setStyle(
                "-fx-background-color: #14b8a6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(loginButton.getStyle().replace("#14b8a6", "#0d9488")));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(loginButton.getStyle().replace("#0d9488", "#14b8a6")));
        loginButton.setOnAction(e -> handleLogin());

        // Message label for errors
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
        messageLabel.setWrapText(true);

        formBox.getChildren().addAll(usernameBox, passwordBox, roleBox, loginButton, messageLabel);

        // Add all to center box
        centerBox.getChildren().addAll(titleBox, formBox);

        // Wrap in a StackPane for centering
        StackPane centerWrapper = new StackPane(centerBox);
        centerWrapper.setAlignment(Pos.CENTER);

        root.setCenter(centerWrapper);

        // Enter key handler
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        int userId = authenticateUser(username, password, role);

        if (userId > 0) {
            // Initialize session
            SessionManager.getInstance().initSession(userId, username, role);

            // Log login
            com.college.dao.AuditLogDAO.logAction(userId, username, "LOGIN", "USER", userId,
                    "User logged in as " + role);

            // Switch to dashboard
            DashboardView dashboardView = new DashboardView(username, role, userId);
            com.college.MainFX.getPrimaryStage().getScene().setRoot(dashboardView.getView());
            com.college.MainFX.getPrimaryStage().setMaximized(true);
        } else {
            messageLabel.setText("Invalid username, password, or role. Please try again.");
            passwordField.clear();
        }
    }

    private int authenticateUser(String username, String password, String role) {
        String sql = "SELECT id FROM users WHERE username=? AND password=? AND role=?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not establish database connection.");
                return 0;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, com.college.utils.PasswordUtils.hashPasswordLegacy(password));
                pstmt.setString(3, role);

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
            return 0;
        } catch (Exception e) {
            com.college.utils.Logger.error("Authentication failed", e);
            return 0;
        }
    }

    public BorderPane getView() {
        return root;
    }
}
