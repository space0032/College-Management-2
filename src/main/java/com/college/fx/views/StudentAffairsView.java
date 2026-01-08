package com.college.fx.views;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.college.fx.views.CourseManagementView;
import com.college.fx.views.AttendanceView;
import com.college.fx.views.GradesView;
import com.college.fx.views.TimetableView;
import com.college.fx.views.LibraryManagementView;
import com.college.fx.views.HostelManagementView;
import com.college.fx.views.GatePassView;
import com.college.fx.views.FeesView;
import com.college.fx.views.AssignmentsView;

/**
 * Student Affairs View (Consolidated "Student Management")
 * Contains: Courses, Attendance, Grades, Library, Timetable, Gate Pass, Hostel,
 * Assignments
 */
public class StudentAffairsView {

    private VBox root;
    private String userRole;
    private int userId;

    public StudentAffairsView(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Student Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));
        title.setPadding(new Insets(0, 0, 10, 10));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("floating");

        // 1. Courses
        Tab courseTab = new Tab("Courses");
        CourseManagementView courseView = new CourseManagementView(userRole, userId);
        courseTab.setContent(courseView.getView());

        // 2. Attendance
        Tab attTab = new Tab("Attendance");
        AttendanceView attView = new AttendanceView(userRole, userId);
        attTab.setContent(attView.getView());

        // 3. Grades
        Tab gradeTab = new Tab("Grades");
        GradesView gradeView = new GradesView(userRole, userId);
        gradeTab.setContent(gradeView.getView());

        // 4. Assignments
        Tab assignTab = new Tab("Assignments");
        AssignmentsView assignView = new AssignmentsView(userRole, userId);
        assignTab.setContent(assignView.getView());

        // 5. Timetable
        Tab timeTab = new Tab("Timetable");
        TimetableView timeView = new TimetableView(userRole, userId);
        timeTab.setContent(timeView.getView());

        // 6. Library
        Tab libTab = new Tab("Library");
        LibraryManagementView libView = new LibraryManagementView(userRole, userId);
        libTab.setContent(libView.getView());

        // 7. Hostel
        Tab hostelTab = new Tab("Hostel");
        HostelManagementView hostelView = new HostelManagementView(userRole, userId);
        hostelTab.setContent(hostelView.getView());

        // 8. Gate Pass
        Tab gateTab = new Tab("Gate Pass");
        GatePassView gateView = new GatePassView(userRole, userId);
        gateTab.setContent(gateView.getView());

        // 9. Fees
        Tab feesTab = new Tab("Fees");
        FeesView feesView = new FeesView(userRole, userId);
        feesTab.setContent(feesView.getView());

        tabPane.getTabs().addAll(
                courseTab, attTab, gradeTab, assignTab,
                timeTab, libTab, hostelTab, gateTab, feesTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(title, tabPane);
    }

    public VBox getView() {
        return root;
    }
}
