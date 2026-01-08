package com.college.fx.views;

import com.college.utils.DatabaseConnection;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
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

    // SVG Paths
    private static final String USER_ICON = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String LOCK_ICON = "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z";
    private static final String LOGO_ICON = "M12 3L1 9l11 6 9-4.91V17h2V9L12 3zM5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82z";

    public LoginView() {
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.getStyleClass().add("login-root");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Glass Card Container
        VBox card = new VBox(25);
        card.getStyleClass().add("login-glass-card");
        card.setMaxWidth(400); // Standard width for login card
        card.setAlignment(Pos.CENTER);

        // Logo Section
        SVGPath logoIcon = new SVGPath();
        logoIcon.setContent(LOGO_ICON);
        logoIcon.setFill(Color.web("#2dd4bf")); // Teal/Cyan
        logoIcon.setScaleX(2.5);
        logoIcon.setScaleY(2.5);

        StackPane logoContainer = new StackPane(logoIcon);
        logoContainer.setMinSize(60, 60);
        logoContainer.setPrefSize(60, 60);
        logoContainer.setMaxSize(60, 60);
        logoContainer.setStyle("-fx-background-color: rgba(45, 212, 191, 0.1); -fx-background-radius: 50%;");

        Label title = new Label("College Management");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("System");
        subtitle.getStyleClass().add("login-subtitle");

        VBox header = new VBox(10, logoContainer, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Form Section
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER_LEFT);

        // Username Field
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("login-label");

        HBox userInputGroup = new HBox(10);
        userInputGroup.getStyleClass().add("login-input-container");
        userInputGroup.setAlignment(Pos.CENTER_LEFT);

        SVGPath userIcon = new SVGPath();
        userIcon.setContent(USER_ICON);
        userIcon.getStyleClass().add("login-field-icon");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("login-input-field");
        HBox.setHgrow(usernameField, Priority.ALWAYS);

        userInputGroup.getChildren().addAll(userIcon, usernameField);
        VBox userBox = new VBox(8, userLabel, userInputGroup);

        // Password Field
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("login-label");

        HBox passInputGroup = new HBox(10);
        passInputGroup.getStyleClass().add("login-input-container");
        passInputGroup.setAlignment(Pos.CENTER_LEFT);

        SVGPath lockIcon = new SVGPath();
        lockIcon.setContent(LOCK_ICON);
        lockIcon.getStyleClass().add("login-field-icon");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-input-field");
        HBox.setHgrow(passwordField, Priority.ALWAYS);

        passInputGroup.getChildren().addAll(lockIcon, passwordField);
        VBox passBox = new VBox(8, passLabel, passInputGroup);

        // Role Selector
        Label roleLabel = new Label("Login As");
        roleLabel.getStyleClass().add("login-label");

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Select", "ADMIN", "FACULTY", "STUDENT", "WARDEN", "FINANCE");
        roleComboBox.setValue("Select");
        roleComboBox.setMaxWidth(Double.MAX_VALUE);
        roleComboBox.getStyleClass().add("login-combo-box");

        VBox roleBox = new VBox(8, roleLabel, roleComboBox);

        // Sign In Button
        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("login-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> handleLogin());

        // Error Message
        messageLabel = new Label();
        messageLabel.setTextFill(Color.web("#ef4444"));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        form.getChildren().addAll(userBox, passBox, roleBox, loginBtn, messageLabel);

        card.getChildren().addAll(header, form);

        // Center Wrapper
        StackPane centerWrapper = new StackPane(card);
        centerWrapper.setAlignment(Pos.CENTER);

        root.setCenter(centerWrapper);

        // Decoration (Optional - subtle corner glow can be added via CSS on root)

        // Key Handlers
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String roleSelection = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        // Add role check if "Select" is chosen? Or just ignore for now as requested
        // design has "Select"
        // Ideally we should enforce selection if it's not "Select".
        // Current logic doesn't strictly depend on UI role selection for auth (it gets
        // role from DB),
        // but UI shows it. Let's assume user might not select it if they are just
        // logging in.
        // However, standard flow implies selecting it.
        // PROCEED with standard auth logic.

        int userId = authenticateUser(username, password);

        if (userId > 0) {
            // Retrieve full user details including legacy role
            com.college.dao.UserDAO userDAO = new com.college.dao.UserDAO();
            com.college.models.User user = userDAO.getUserById(userId);

            // ... (Rest of login logic remains same) ...

            // Initialize session with legacy role string
            SessionManager.getInstance().initSession(userId, username,
                    (user != null && user.getRole() != null) ? user.getRole() : "");

            // Get the actual Role object from session to determine Portal
            com.college.models.Role userRole = SessionManager.getInstance().getUserRole();

            if (userRole == null) {
                com.college.utils.Logger.error("Login successful but no role assigned for user: " + username);
                messageLabel.setText("Login failed: Account configuration error.");
                return;
            }

            String portalType = userRole.getPortalType();
            if (portalType == null || portalType.isEmpty()) {
                portalType = "STUDENT"; // Default fallback
            }

            // Re-initialize session with the resolved Portal Type
            SessionManager.getInstance().initSession(userId, username, portalType);

            // Log login
            com.college.dao.AuditLogDAO.logAction(userId, username, "LOGIN", "USER", userId,
                    "User logged in. Portal: " + portalType);

            // Switch to dashboard using Portal Type
            DashboardView dashboardView = new DashboardView(username, portalType, userId);
            com.college.MainFX.getPrimaryStage().getScene().setRoot(dashboardView.getView());
            com.college.MainFX.getPrimaryStage().setMaximized(true);
        } else {
            messageLabel.setText("Invalid credentials.");
            passwordField.clear();
        }
    }

    private int authenticateUser(String username, String password) {
        String sql = "SELECT id, password FROM users WHERE username=?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not establish database connection.");
                return 0;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        if (com.college.utils.PasswordUtils.verifyPassword(password, storedHash)) {
                            return rs.getInt("id");
                        }
                    }
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
