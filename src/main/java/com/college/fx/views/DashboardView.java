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
        // Load CSS
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Remove old inline style
        // root.setStyle("-fx-background-color: #f8fafc;");

        // Create top bar (Simplified for new design, or integrated?)
        // The design implies a clean top, let's keep the existing topbar but maybe
        // style it darker if needed.
        // For now, keep existing logic but update styling.
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f1f5f9;"); // Light grey bg for content
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
        topBar.setStyle("-fx-background-color: transparent;"); // Transparent to blend with gradient or specific color?
        // Actually, let's stick to the previous style or a dark theme one.
        // If the sidebar is dark (#0f172a), the top bar could match or be transparent
        // if root has background.
        // Let's use a solid dark color to match or contrast slightly.
        topBar.setStyle("-fx-background-color: #0f172a;");
        topBar.setSpacing(20);

        // Title - Now in sidebar, so maybe removal from TopBar?
        // The mock shows "College Manager" in the sidebar. The TopBar might be for
        // global actions or breadcrumbs.
        // For now, let's keep it simple: just the User Info and Logout on the right, as
        // Title is in sidebar.

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
        roleLabel.setTextFill(Color.web("#94a3b8"));

        userInfo.getChildren().addAll(welcomeLabel, roleLabel);

        // Notifications Button
        Button notifBtn = new Button("Notifications");
        notifBtn.setStyle(
                "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        notifBtn.setOnAction(e -> NotificationView.showDialog());

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

        topBar.getChildren().addAll(spacer, notifBtn, userInfo, logoutBtn);
        return topBar;
    }

    private Button homeButton;

    // --- SVG Paths ---
    private static final String SVG_HOME = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";
    private static final String SVG_INSTITUTE = "M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm14-12v7h3v-7h-3zm-4.5-9L2 6v2h19V6l-9.5-5z";
    private static final String SVG_EMPLOYEES = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
    private static final String SVG_SYLLABUS = "M18 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 4h5v8l-2.5-1.5L6 12V4z";
    private static final String SVG_RESOURCE = "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z";
    private static final String SVG_STUDENT = "M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3 1 9l11 6 9-4.91V17h2V9L12 3z";
    private static final String SVG_CALENDAR = "M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z";
    private static final String SVG_ACTIVITIES = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";
    private static final String SVG_LEAVE = "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2.53 11.12l-1.41 1.41L12 12.41l-3.06 3.12-1.41-1.41L10.59 11 7.53 7.88l1.41-1.41L12 9.59l3.06-3.12 1.41 1.41L13.41 11l3.06 3.12z";
    private static final String SVG_TIMETABLE = "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z";
    private static final String SVG_PAYROLL = "M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z";
    private static final String SVG_ANNOUNCEMENTS = "M18 11v2h4v-2h-4zm-2 6.61c.96.71 2.21 1.65 3.2 2.39.4-.53.8-1.07 1.2-1.6-.99-.74-2.24-1.68-3.2-2.4-.4.54-.8 1.08-1.2 1.61zM20.4 5.6c-.4-.53-.8-1.06-1.2-1.59-.99.74-2.24 1.68-3.2 2.4.4.53.8 1.07 1.2 1.6.96-.72 2.21-1.65 3.2-2.41zM4 9c-1.1 0-2 .9-2 2v2c0 1.1.9 2 2 2h1v5h2v-5h2l3-3V9H4zm9 0h2v5h-2z";
    private static final String SVG_FEES = "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z";
    private static final String SVG_REPORTS = "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z";
    private static final String SVG_PROFILE = "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z";
    private static final String SVG_SEARCH = "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";

    private javafx.scene.shape.SVGPath createIcon(String pathContent) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(pathContent);
        icon.setFill(Color.web("#cbd5e1")); // Default inactive color
        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(260); // Slightly wider
        sidebar.getStyleClass().add("sidebar");

        SessionManager session = SessionManager.getInstance();

        // 1. App Title
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("College Manager");
        title.getStyleClass().add("sidebar-title");

        // Search Icon
        javafx.scene.shape.SVGPath searchIcon = createIcon(SVG_SEARCH);
        searchIcon.setFill(Color.WHITE);
        searchIcon.setScaleX(0.9);
        searchIcon.setScaleY(0.9);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBox.getChildren().addAll(title, spacer, searchIcon);

        // 2. Home Button (Prominent)
        homeButton = new Button("Home");
        homeButton.setGraphic(createIcon(SVG_HOME));
        homeButton.getStyleClass().add("home-button");
        homeButton.setMaxWidth(Double.MAX_VALUE);
        homeButton.setOnAction(e -> navigateTo("home"));
        menuButtons.put("home", homeButton);

        // 3. Sections
        VBox sectionsContainer = new VBox(10);

        // --- Management Section ---
        VBox managementContent = new VBox(5);
        boolean hasManagement = false;

        if (session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(managementContent, "Institute", "institute", SVG_INSTITUTE);
            hasManagement = true;
        }
        if (session.hasPermission("VIEW_EMPLOYEES")) {
            addMenuItem(managementContent, "Employees", "employees", SVG_EMPLOYEES);
            hasManagement = true;
        }
        if (session.hasPermission("UPLOAD_SYLLABUS")) {
            addMenuItem(managementContent, "Syllabus", "syllabus_management", SVG_SYLLABUS);
            hasManagement = true;
        }
        if (session.hasPermission("UPLOAD_RESOURCES")) {
            addMenuItem(managementContent, "Resource", "resource_management", SVG_RESOURCE);
            hasManagement = true;
        }
        if ((session.hasPermission("VIEW_STUDENTS") || session.hasPermission("MANAGE_STUDENTS"))
                && !session.isStudent()) {
            addMenuItem(managementContent, "Student", "students", SVG_STUDENT);
            hasManagement = true;
        }

        if (hasManagement) {
            sectionsContainer.getChildren().add(createSection("Management", managementContent));
        }

        // --- Academic Section ---
        VBox academicContent = new VBox(5);
        boolean hasAcademic = false;

        addMenuItem(academicContent, "Calendar", "calendar", SVG_CALENDAR);
        hasAcademic = true;

        if (session.isStudent() || session.hasPermission("VIEW_EVENTS")) {
            addMenuItem(academicContent, "Activities", "student_activities", SVG_ACTIVITIES);
            hasAcademic = true;
        }

        // Leave
        if (session.isStudent()) {
            addMenuItem(academicContent, "Leave", "student_leave", SVG_LEAVE);
        } else {
            addMenuItem(academicContent, "Leave", "staff_leave", SVG_LEAVE);
        }
        // Leave Approvals
        if (session.hasPermission("APPROVE_LEAVE")) {
            addMenuItem(academicContent, "Leave Approvals", "leave_approval", SVG_LEAVE);
        }
        // Timetable
        if (session.hasPermission("VIEW_TIMETABLE")) {
            addMenuItem(academicContent, "Timetable", "timetable", SVG_TIMETABLE);
        }

        sectionsContainer.getChildren().add(createSection("Academic", academicContent));

        // --- Finance & News ---
        VBox financeContent = new VBox(5);
        boolean hasFinance = false;

        if (session.hasPermission("MANAGE_PAYROLL") || session.isAdmin()) {
            addMenuItem(financeContent, "Payroll", "payroll", SVG_PAYROLL);
            hasFinance = true;
        }
        if (!role.equals("STUDENT")) {
            addMenuItem(financeContent, "Announcements", "announcements", SVG_ANNOUNCEMENTS);
            hasFinance = true;
        }
        // Fees
        if (session.hasPermission("VIEW_OWN_FEES") || session.hasPermission("MANAGE_FEES")) {
            addMenuItem(financeContent, "Fees", "fees", SVG_FEES);
            hasFinance = true;
        }

        if (hasFinance) {
            sectionsContainer.getChildren().add(createSection("Finance & News", financeContent));
        }

        // --- Reports ---
        if (session.hasAnyPermission("VIEW_FEES_REPORT", "VIEW_ATTENDANCE_REPORT", "VIEW_GRADES_REPORT")) {
            VBox reportsContent = new VBox(5);
            addMenuItem(reportsContent, "Reports", "reports", SVG_REPORTS);
            sectionsContainer.getChildren().add(createSection("Reports", reportsContent));
        }

        // ScrollPane for menu items if they get too long
        ScrollPane scroll = new ScrollPane(sectionsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // --- User Account Section (Bottom) ---
        VBox userSection = new VBox(5);
        userSection.getStyleClass().add("user-account-section");

        HBox userHeader = new HBox(10);
        userHeader.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.shape.SVGPath userIcon = createIcon(SVG_PROFILE);
        userIcon.setScaleX(1.2);
        userIcon.setScaleY(1.2);
        // Container for icon to add padding/bg
        StackPane iconPane = new StackPane(userIcon);
        iconPane.setStyle("-fx-background-color: #334155; -fx-padding: 8; -fx-background-radius: 20;");
        iconPane.setMinSize(36, 36);

        VBox userText = new VBox(0);
        Label userNameLbl = new Label("User Account");
        userNameLbl.getStyleClass().add("user-name");
        Label userRoleLbl = new Label(displayName);
        userRoleLbl.getStyleClass().add("user-role");
        userText.getChildren().addAll(userNameLbl, userRoleLbl);

        userHeader.getChildren().addAll(iconPane, userText);

        // Profile Links
        Button profileBtn = new Button("My Profile");
        profileBtn.getStyleClass().add("menu-item");
        profileBtn.setMaxWidth(Double.MAX_VALUE);
        profileBtn.setOnAction(e -> navigateTo("profile"));

        Button pwdBtn = new Button("Change Password");
        pwdBtn.getStyleClass().add("menu-item");
        pwdBtn.setMaxWidth(Double.MAX_VALUE);
        pwdBtn.setOnAction(e -> navigateTo("password"));

        // Collapsible User Menu
        TitledPane userPane = new TitledPane();
        userPane.setGraphic(userHeader);
        userPane.setContent(new VBox(5, profileBtn, pwdBtn));
        userPane.getStyleClass().add("sidebar-section");
        userPane.setExpanded(false);

        sidebar.getChildren().addAll(titleBox, homeButton, scroll, userPane);

        return sidebar;
    }

    private TitledPane createSection(String title, VBox content) {
        TitledPane pane = new TitledPane(title, content);
        pane.getStyleClass().add("sidebar-section");
        pane.setExpanded(true); // Default open?
        pane.setAnimated(true);
        return pane;
    }

    private void addMenuItem(VBox container, String text, String viewName, String iconPath) {
        Button btn = new Button(text);
        if (iconPath != null) {
            javafx.scene.shape.SVGPath icon = createIcon(iconPath);
            icon.getStyleClass().add("menu-icon");
            btn.setGraphic(icon);
        }

        btn.getStyleClass().add("menu-item");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> navigateTo(viewName));

        container.getChildren().add(btn);
        menuButtons.put(viewName, btn);
    }

    private String currentView = "home"; // Default

    private void updateActiveState(String activeView) {
        this.currentView = activeView;

        // Reset all mapped buttons
        menuButtons.forEach((view, btn) -> {
            btn.getStyleClass().removeAll("menu-item-active");
        });

        // Set active
        if (menuButtons.containsKey(activeView)) {
            menuButtons.get(activeView).getStyleClass().add("menu-item-active");
        }

        // Handle standalone Home button special case?
        // Actually, I can map "home" to `homeButton` if I put it in the map.
        // Let's ensure "home" is in menuButtons.
    }

    private void navigateTo(String viewName) {
        updateActiveState(viewName);
        contentArea.getChildren().clear();

        try {
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
                case "calendar":
                    showCalendar();
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
                    loadViewReflectively("com.college.fx.views.AnnouncementManagementView", role, userId);
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
                case "faculty_workload":
                    showFacultyWorkload();
                    break;
                case "employees":
                    showEmployees();
                    break;
                case "student_leave":
                    loadViewReflectively("com.college.fx.views.StudentLeaveView");
                    break;
                case "leave_approval":
                    loadViewReflectively("com.college.fx.views.LeaveApprovalView");
                    break;
                case "volunteer_tasks":
                    loadViewReflectively("com.college.fx.views.VolunteerTasksView");
                    break;
                case "staff_leave":
                    loadViewReflectively("com.college.fx.views.StaffLeaveView");
                    break;
                case "payroll":
                    loadViewReflectively("com.college.fx.views.PayrollManagementView");
                    break;
                case "student_affairs":
                    loadViewReflectively("com.college.fx.views.StudentAffairsView", role, userId);
                    break;
                case "reports":
                    loadViewReflectively("com.college.fx.views.ReportsView");
                    break;
                case "events":
                    showEvents();
                    break;
                case "event_management":
                    showEventManagement();
                    break;
                case "clubs":
                    showClubs();
                    break;
                case "club_management":
                    showClubManagement();
                    break;
                case "student_activities":
                    showStudentActivities();
                    break;
                case "syllabus_management":
                    loadViewReflectively("com.college.fx.views.SyllabusManagementView");
                    break;
                case "resource_management":
                    loadViewReflectively("com.college.fx.views.ResourceManagementView");
                    break;
                case "learning_portal":
                    loadViewReflectively("com.college.fx.views.LearningPortalView");
                    break;
                default:
                    showHome();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to load view: " + viewName);
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    private void loadViewReflectively(String className, Object... args) {
        try {
            Class<?> clazz = Class.forName(className);
            Object viewInstance;

            if (args.length == 0) {
                viewInstance = clazz.getDeclaredConstructor().newInstance();
            } else {
                Class<?>[] paramTypes = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Integer)
                        paramTypes[i] = int.class;
                    else if (args[i] instanceof String)
                        paramTypes[i] = String.class;
                    else
                        paramTypes[i] = args[i].getClass();
                }
                // Simple matching, might be fragile for interfaces, but okay for int/String
                viewInstance = clazz.getDeclaredConstructor(paramTypes).newInstance(args);
            }

            currentController = viewInstance;
            java.lang.reflect.Method getViewMethod = clazz.getMethod("getView");
            javafx.scene.Node node = (javafx.scene.Node) getViewMethod.invoke(viewInstance);
            contentArea.getChildren().add(node);
        } catch (Exception e) {
            e.printStackTrace(); // Print full trace
            throw new RuntimeException("Reflective load failed for " + className + ": " + e.getMessage(), e);
        }
    }

    private Object currentController;

    private void showHome() {
        HomeView homeView = new HomeView(displayName, role, userId);
        currentController = homeView;
        contentArea.getChildren().add(homeView.getView());
    }

    private void showInstitute() {
        InstituteManagementView view = new InstituteManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showStudents() {
        StudentManagementView view = new StudentManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showFaculty() {
        FacultyManagementView view = new FacultyManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showFacultyWorkload() {
        FacultyWorkloadView view = new FacultyWorkloadView();
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showEmployees() {
        EmployeeManagementView view = new EmployeeManagementView();
        // EmployeeView is a VBox itself in this case? No, it has view logic.
        // Checking previous code: view is EmployeeManagementView which IS a VBox/Pane
        // or has getView?
        // Let's assume standard pattern. Previous code:
        // contentArea.getChildren().add(view);
        // meaning view IS the Node. So it is the controller/view hybrid.
        currentController = view;
        contentArea.getChildren().add(view);
    }

    private void showCourses() {
        CourseManagementView view = new CourseManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showAttendance() {
        AttendanceView view = new AttendanceView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showLibrary() {
        LibraryManagementView view = new LibraryManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showFees() {
        FeesView view = new FeesView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showTimetable() {
        TimetableView view = new TimetableView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showCalendar() {
        AcademicCalendarView view = new AcademicCalendarView();
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showGrades() {
        GradesView view = new GradesView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showGatePass() {
        GatePassView view = new GatePassView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showHostel() {
        HostelManagementView view = new HostelManagementView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showAssignments() {
        AssignmentsView view = new AssignmentsView(role, userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showProfile() {
        if ("STUDENT".equals(role)) {
            com.college.dao.StudentDAO sDAO = new com.college.dao.StudentDAO();
            com.college.models.Student student = sDAO.getStudentByUserId(userId);
            if (student != null) {
                StudentProfileView view = new StudentProfileView(student, true, null);
                currentController = view;
                contentArea.getChildren().add(view.getView());
                return;
            }
        }
        ProfileView view = new ProfileView(role, userId, username);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showChangePassword() {
        ChangePasswordView view = new ChangePasswordView(userId);
        currentController = view;
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

    private void showEvents() {
        EventsView view = new EventsView(userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showEventManagement() {
        EventManagementView view = new EventManagementView(userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showClubs() {
        ClubsView view = new ClubsView(userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showClubManagement() {
        ClubManagementView view = new ClubManagementView(userId);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private void showStudentActivities() {
        StudentActivitiesView view = new StudentActivitiesView(
                this::showEvents,
                this::showClubs,
                this::showEventManagement,
                this::showClubManagement);
        currentController = view;
        contentArea.getChildren().add(view.getView());
    }

    private StackPane mainContainer;

    public javafx.scene.Parent getView() {
        if (mainContainer == null) {
            mainContainer = new StackPane(root);

            // Add ChatBot Overlay
            com.college.fx.components.ChatBotOverlay chatBot = new com.college.fx.components.ChatBotOverlay();

            // Inject Context Provider
            chatBot.setContextProvider(() -> {
                if (currentController instanceof com.college.fx.interfaces.ContextAware) {
                    return ((com.college.fx.interfaces.ContextAware) currentController).getContextData();
                }
                return "User is on the " + currentView + " page.";
            });

            mainContainer.getChildren().add(chatBot);

            // Ensure chat bot doesn't block interaction with underlying dashboard when
            // collapsed
            chatBot.setPickOnBounds(false);
        }
        return mainContainer;
    }
}
