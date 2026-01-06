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
            if (session.hasPermission("MANAGE_SYSTEM")) {
                addMenuItem(sidebar, "Faculty Workload", "faculty_workload", false);
            }
        }

        // Employee (HR) Management
        if (session.hasPermission("MANAGE_EMPLOYEES") || session.hasPermission("VIEW_EMPLOYEES")) {
            addMenuItem(sidebar, "Employees (HR)", "employees", false);
        }

        // Payroll Management
        if (session.hasPermission("MANAGE_PAYROLL") || session.hasPermission("VIEW_PAYROLL")) {
            addMenuItem(sidebar, "Payroll", "payroll", false);
        }

        // Course Management - Students should always see their courses
        if (session.isStudent()) {
            addMenuItem(sidebar, "My Courses", "courses", false);
        }

        // Attendance - Students always see, Faculty can see, Admin uses Institute
        // Management
        if (session.isStudent()) {
            addMenuItem(sidebar, "My Attendance", "attendance", false);
        } else if ((session.hasPermission("VIEW_ATTENDANCE") || session.hasPermission("MANAGE_ATTENDANCE"))
                && !session.isAdmin()) {
            addMenuItem(sidebar, "Attendance", "attendance", false);
        }

        // Grades - Students always see, Faculty can see, Admin uses Institute
        // Management
        if (session.isStudent()) {
            addMenuItem(sidebar, "My Grades", "grades", false);
        } else if ((session.hasPermission("VIEW_GRADES") || session.hasPermission("MANAGE_GRADES"))
                && !session.isAdmin()) {
            addMenuItem(sidebar, "Grades", "grades", false);
        }

        // Library
        if (session.hasPermission("VIEW_LIBRARY") && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Library", "library", false);
        }

        // Fees
        // Students see own fees.
        // Admin, Finance see all fees (Student Fees)
        if (session.hasPermission("VIEW_OWN_FEES") && session.isStudent()) {
            addMenuItem(sidebar, "My Fees", "fees", false);
        } else if ((session.hasPermission("VIEW_ALL_FEES") || session.hasPermission("MANAGE_FEES"))
                && !session.isAdmin()) {
            addMenuItem(sidebar, "Student Fees", "fees", false);
        }

        // Timetable
        if (session.hasPermission("VIEW_TIMETABLE") && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Timetable", "timetable", false);
        }

        // Learning Management (Faculty/Admin)
        if (session.hasPermission("UPLOAD_SYLLABUS")) {
            addMenuItem(sidebar, "Syllabus Management", "syllabus_management", false);
        }
        if (session.hasPermission("UPLOAD_RESOURCES")) {
            addMenuItem(sidebar, "Resource Management", "resource_management", false);
        }

        // Learning Portal (Student)
        if (session.hasPermission("VIEW_RESOURCES") && session.isStudent()) {
            addMenuItem(sidebar, "Learning Portal", "learning_portal", false);
        }

        // Academic Calendar (Visible to all)
        addMenuItem(sidebar, "Academic Calendar", "calendar", false);

        // Student Activities (Events & Clubs Dashboard)
        if (session.hasPermission("VIEW_EVENTS") || session.hasPermission("MANAGE_EVENTS") ||
                session.hasPermission("JOIN_CLUBS") || session.hasPermission("MANAGE_CLUBS")) {
            addMenuItem(sidebar, "Student Activities", "student_activities", false);
        }

        // Gate Pass (Wardens have their own section)
        if ((session.hasPermission("REQUEST_GATE_PASS") || session.hasPermission("APPROVE_GATE_PASS"))
                && !session.hasPermission("MANAGE_SYSTEM") && !role.equals("WARDEN")) {
            addMenuItem(sidebar, "Gate Pass", "gatepass", false);
        }

        // Hostel (Wardens have their own section)
        if ((session.hasPermission("MANAGE_HOSTEL") || session.isStudent())
                && !session.hasPermission("MANAGE_SYSTEM") && !role.equals("WARDEN")) {
            addMenuItem(sidebar, "Hostel", "hostel", false);
        }

        // Announcements (Everyone except students)
        if (!role.equals("STUDENT")) {
            addMenuItem(sidebar, "Announcements", "announcements", false);
        }

        // Assignments
        if ((session.hasPermission("VIEW_ASSIGNMENTS") || session.hasPermission("SUBMIT_ASSIGNMENTS"))
                && !session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Assignments", "assignments", false);
        }

        // Warden-specific menu items
        if (role.equals("WARDEN")) {
            addMenuItem(sidebar, "Hostel Management", "hostel", false);
            addMenuItem(sidebar, "Gate Pass Approvals", "gatepass", false);
        }

        // Admin Consolidated "Student Management"
        if (session.hasPermission("MANAGE_SYSTEM")) {
            addMenuItem(sidebar, "Student Management", "student_affairs", false);
        }

        // Reports
        // Show if user has ANY report viewing permission
        if (session.hasAnyPermission("VIEW_FEES_REPORT", "VIEW_ATTENDANCE_REPORT", "VIEW_GRADES_REPORT")) {
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

    private void addCategoryHeader(VBox sidebar, String headerText) {
        Label categoryLabel = new Label(headerText);
        categoryLabel.setStyle("-fx-text-fill: #64748b; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 15 15 5 15;");
        sidebar.getChildren().add(categoryLabel);
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
            case "faculty_workload":
                showFacultyWorkload();
                break;
            case "employees":
                showEmployees();
                break;
            case "payroll":
                showPayroll();
                break;
            case "student_affairs":
                showStudentAffairs();
                break;
            case "reports":
                showReports();
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
                showSyllabusManagement();
                break;
            case "resource_management":
                showResourceManagement();
                break;
            case "learning_portal":
                showLearningPortal();
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

    private void showFacultyWorkload() {
        FacultyWorkloadView view = new FacultyWorkloadView();
        contentArea.getChildren().add(view.getView());
    }

    private void showEmployees() {
        EmployeeManagementView view = new EmployeeManagementView();
        contentArea.getChildren().add(view);
    }

    private void showPayroll() {
        PayrollManagementView view = new PayrollManagementView();
        contentArea.getChildren().add(view);
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

    private void showCalendar() {
        AcademicCalendarView view = new AcademicCalendarView();
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
        if ("STUDENT".equals(role)) {
            com.college.dao.StudentDAO sDAO = new com.college.dao.StudentDAO();
            com.college.models.Student student = sDAO.getStudentByUserId(userId);
            if (student != null) {
                StudentProfileView view = new StudentProfileView(student, false, null); // Students can't edit basic
                                                                                        // info freely, maybe some?
                // Actually StudentProfileView handles editability. Let's allowing editing
                // contact info?
                // For now, let's keep editable=true for students on their own profile for
                // Personal/Family tabs
                // But Academic is read-only. The view handles this.
                // Re-instantiating with editable=true so they can update phone/email/activities
                view = new StudentProfileView(student, true, null);
                contentArea.getChildren().add(view.getView());
                return;
            }
        }
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

    private void showEvents() {
        EventsView view = new EventsView(userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showEventManagement() {
        EventManagementView view = new EventManagementView(userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showClubs() {
        ClubsView view = new ClubsView(userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showClubManagement() {
        ClubManagementView view = new ClubManagementView(userId);
        contentArea.getChildren().add(view.getView());
    }

    private void showStudentActivities() {
        StudentActivitiesView view = new StudentActivitiesView(
                this::showEvents,
                this::showClubs,
                this::showEventManagement,
                this::showClubManagement);
        contentArea.getChildren().add(view.getView());
    }

    private void showSyllabusManagement() {
        SyllabusManagementView view = new SyllabusManagementView();
        contentArea.getChildren().add(view.getView());
    }

    private void showResourceManagement() {
        ResourceManagementView view = new ResourceManagementView();
        contentArea.getChildren().add(view.getView());
    }

    private void showLearningPortal() {
        LearningPortalView view = new LearningPortalView();
        contentArea.getChildren().add(view.getView());
    }

    private StackPane mainContainer;

    public javafx.scene.Parent getView() {
        if (mainContainer == null) {
            mainContainer = new StackPane(root);

            // Add ChatBot Overlay
            com.college.fx.components.ChatBotOverlay chatBot = new com.college.fx.components.ChatBotOverlay();
            mainContainer.getChildren().add(chatBot);

            // Ensure chat bot doesn't block interaction with underlying dashboard when
            // collapsed
            chatBot.setPickOnBounds(false);
        }
        return mainContainer;
    }
}
