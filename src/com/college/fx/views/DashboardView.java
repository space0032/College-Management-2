package com.college.fx.views;

import com.college.utils.SessionManager;
import com.college.utils.UserDisplayNameUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX Dashboard View
 * Main dashboard with sidebar navigation and content area
 */
public class DashboardView {

    private BorderPane root;
    private StackPane contentArea;
    private String username;
    private String role;
    private int userId;
    private String displayName;
    private VBox sidebar;
    private final java.util.Map<String, Button> menuButtons = new java.util.HashMap<>();

    public DashboardView(String username, String role, int userId) {
        this.username = username;
        this.role = role;
        this.userId = userId;
        this.displayName = UserDisplayNameUtil.getDisplayName(userId, role, username);
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8fafc;");

        // Create top bar
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8fafc;");
        contentArea.setPadding(new Insets(20));

        // Show home by default
        showHome();

        root.setCenter(contentArea);
    }

    private HBox createTopBar() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 25, 0, 25));
        topBar.setPrefHeight(70);
        topBar.setStyle("-fx-background-color: #14b8a6;");
        topBar.setSpacing(20);

        // Title
        Label titleLabel = new Label("College Management System");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User info
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label welcomeLabel = new Label("Welcome, " + displayName);
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        welcomeLabel.setTextFill(Color.WHITE);

        Label roleLabel = new Label("[" + role + "]");
        roleLabel.setFont(Font.font("Segoe UI", 12));
        roleLabel.setTextFill(Color.web("#99f6e4"));

        userInfo.getChildren().addAll(welcomeLabel, roleLabel);

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 8 20;" +
                        "-fx-cursor: hand;");
        logoutBtn.setOnAction(e -> handleLogout());

        topBar.getChildren().addAll(titleLabel, spacer, userInfo, logoutBtn);
        return topBar;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setStyle("-fx-background-color: #0f172a;");

        SessionManager session = SessionManager.getInstance();

        // Menu items
        addMenuItem(sidebar, "Home", "home", true);

        // Admin specific
        if (session.hasPermission("MANAGE_SYSTEM") || session.hasPermission("VIEW_AUDIT_LOGS")) {
            addMenuItem(sidebar, "Institute Management", "institute", false);
        }

        // Student Management
        if ((session.hasPermission("VIEW_STUDENTS") || session.hasPermission("MANAGE_STUDENTS"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Students", "students", false);
        }

        // Faculty Management
        if ((session.hasPermission("MANAGE_FACULTY") || session.hasPermission("VIEW_FACULTY"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Faculty", "faculty", false);
        }

        // Course Management - Students only (Admin/Faculty use Institute Management)
        if (session.hasPermission("VIEW_COURSES") && session.isStudent()) {
            addMenuItem(sidebar, "My Courses", "courses", false);
        }

        // Attendance
        if ((session.hasPermission("VIEW_ATTENDANCE") || session.hasPermission("VIEW_OWN_ATTENDANCE"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            String label = session.isStudent() ? "My Attendance" : "Attendance";
            addMenuItem(sidebar, label, "attendance", false);
        }

        // Grades
        if ((session.hasPermission("VIEW_GRADES") || session.hasPermission("VIEW_OWN_GRADES"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            String label = session.isStudent() ? "My Grades" : "Grades";
            addMenuItem(sidebar, label, "grades", false);
        }

        // Library
        if (session.hasPermission("VIEW_LIBRARY") && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Library", "library", false);
        }

        // Fees
        if (session.hasPermission("VIEW_OWN_FEES") && session.isStudent()) {
            addMenuItem(sidebar, "My Fees", "fees", false);
        } else if (session.hasPermission("VIEW_ALL_FEES") && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Student Fees", "fees", false);
        }

        // Timetable
        if (session.hasPermission("VIEW_TIMETABLE") && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Timetable", "timetable", false);
        }

        // Gate Pass
        if ((session.hasPermission("REQUEST_GATE_PASS") || session.hasPermission("APPROVE_GATE_PASS"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Gate Pass", "gatepass", false);
        }

        // Hostel
        if ((session.hasPermission("MANAGE_HOSTEL") || session.isStudent())
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Hostel", "hostel", false);
        }

        // Announcements (faculty)
        if (role.equals("FACULTY")) {
            addMenuItem(sidebar, "Announcements", "announcements", false);
        }

        // Assignments
        if ((session.hasPermission("VIEW_ASSIGNMENTS") || session.hasPermission("SUBMIT_ASSIGNMENTS"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Assignments", "assignments", false);
        }

        // Admin Consolidated \"Student Management\"
        if (session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Student Management", "student_affairs", false);
        }

        // Reports
        if (!session.isStudent()) {
            addMenuItem(sidebar, "Reports", "reports", false);
        }

        // Settings section
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #1e293b;");
        sidebar.getChildren().add(separator);

        addMenuItem(sidebar, "My Profile", "profile", false);
        addMenuItem(sidebar, "Change Password", "password", false);

        return sidebar;
    }

    private void addMenuItem(VBox sidebar, String text, String viewName, boolean isActive) {
        Button menuBtn = new Button(text);
        menuBtn.setMaxWidth(Double.MAX_VALUE);
        menuBtn.setPrefHeight(42);
        menuBtn.setAlignment(Pos.CENTER_LEFT);
        menuBtn.setPadding(new Insets(0, 15, 0, 15));

        String baseStyle = "-fx-background-color: transparent;" +
                "-fx-text-fill: #cbd5e1;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        menuButtons.put(viewName, menuBtn); // Track button

        if (isActive) {
            updateActiveState(viewName);
        }

        // Hover effects are now handled by updateActiveState logic implicitly or needs
        // style persistence
        // We set initial style
        if (!isActive) {
            menuBtn.setStyle(baseStyle);
            menuBtn.setOnMouseEntered(e -> {
                if (!viewName.equals(currentView))
                    menuBtn.setStyle(hoverStyle);
            });
            menuBtn.setOnMouseExited(e -> {
                if (!viewName.equals(currentView))
                    menuBtn.setStyle(baseStyle);
            });
        }

        menuBtn.setOnAction(e -> navigateTo(viewName));
        sidebar.getChildren().add(menuBtn);
    }

    private String currentView = "home"; // Default

    private void updateActiveState(String activeView) {
        this.currentView = activeView;

        String baseStyle = "-fx-background-color: transparent;" +
                "-fx-text-fill: #cbd5e1;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        String activeStyle = "-fx-background-color: #14b8a6;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: #1e293b;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";

        menuButtons.forEach((view, btn) -> {
            if (view.equals(activeView)) {
                btn.setStyle(activeStyle);
                btn.setOnMouseEntered(null);
                btn.setOnMouseExited(null);
            } else {
                btn.setStyle(baseStyle);
                btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
                btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
            }
        });
    }

    private void navigateTo(String viewName) {
        updateActiveState(viewName);
        contentArea.getChildren().clear();

        switch (viewName) {
            case "home":
                showHome();
                break;
            case "institute":
                showInstitute();
                break;
            case "students":
                showStudents();
                break;
            case "courses":
                showCourses();
                break;
            case "attendance":
                showAttendance();
                break;
            case "library":
                showLibrary();
                break;
            case "fees":
                showFees();
                break;
            case "timetable":
                showTimetable();
                break;
            case "grades":
                showGrades();
                break;
            case "gatepass":
                showGatePass();
                break;
            case "hostel":
                showHostel();
                break;
            case "announcements":
                showAnnouncements();
                break;
            case "assignments":
                showAssignments();
                break;
            case "profile":
                showProfile();
                break;
            case "password":
                showChangePassword();
                break;
            case "faculty":
                showFaculty();
                break;
            case "student_affairs":
                showStudentAffairs();
                break;
            case "reports":
                showReports();
                break;
            default:
                showHome();
        }
    }

    private void showReports() {
        ReportsView view = new ReportsView();
        contentArea.getChildren().add(view.getView());
    }

    private void showHome() {
        HomeView homeView = new HomeView(displayName, role, userId);
        contentArea.getChildren().add(homeView.getView());
    }

    private void showInstitute() {
        InstituteManagementView view = new InstituteManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showStudents() {
        StudentManagementView view = new StudentManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showFaculty() {
        FacultyManagementView view = new FacultyManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showStudentAffairs() {
        StudentAffairsView view = new StudentAffairsView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showCourses() {
        CourseManagementView view = new CourseManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showAttendance() {
        AttendanceView view = new AttendanceView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showLibrary() {
        LibraryManagementView view = new LibraryManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showFees() {
        FeesView view = new FeesView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showTimetable() {
        TimetableView view = new TimetableView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showGrades() {
        GradesView view = new GradesView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showGatePass() {
        GatePassView view = new GatePassView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showHostel() {
        HostelManagementView view = new HostelManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showAnnouncements() {
        AnnouncementManagementView view = new AnnouncementManagementView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showAssignments() {
        AssignmentsView view = new AssignmentsView(role, userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showProfile() {
        ProfileView view = new ProfileView(role, userId, username);
        contentArea.getChildren().add(view.getView());
    }

    private void showChangePassword() {
        ChangePasswordView view = new ChangePasswordView(userId);
        contentArea.getChildren().add(view.getView());
    }

    private void handleLogout() {
        com.college.dao.AuditLogDAO.logAction(userId, username, "LOGOUT", "USER", userId, "User logged out");
        SessionManager.getInstance().clearSession();

        LoginView loginView = new LoginView();
        com.college.MainFX.getPrimaryStage().getScene().setRoot(loginView.getView());
        com.college.MainFX.getPrimaryStage().setMaximized(false);
        com.college.MainFX.getPrimaryStage().setWidth(1000);
        com.college.MainFX.getPrimaryStage().setHeight(650);
        com.college.MainFX.getPrimaryStage().centerOnScreen();
    }

    public BorderPane getView() {
        return root;
    }
}
