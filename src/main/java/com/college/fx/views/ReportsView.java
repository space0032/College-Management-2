package com.college.fx.views;

import com.college.fx.views.reports.AttendanceReportTab;
import com.college.fx.views.reports.FeesReportTab;
import com.college.fx.views.reports.GradesReportTab;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ReportsView {

    private BorderPane root;

    public ReportsView() {
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Reports & Analytics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        // title.setTextFill(Color.web("#0f172a"));

        header.getChildren().add(title);
        root.setTop(header);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("floating");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Attendance Tab
        Tab attendanceTab = new Tab("Attendance");
        attendanceTab.setClosable(false);
        AttendanceReportTab attendanceContent = new AttendanceReportTab();
        attendanceTab.setContent(attendanceContent.getContent());

        // Grades Tab
        Tab gradesTab = new Tab("Grades Report");
        GradesReportTab gradesContent = new GradesReportTab();
        gradesTab.setContent(gradesContent.getContent());
        gradesTab.setClosable(false);

        // Fees Tab
        Tab feesTab = new Tab("Fees Report");
        FeesReportTab feesContent = new FeesReportTab();
        feesTab.setContent(feesContent.getContent());
        feesTab.setClosable(false);

        // Role-based tab access
        // Permission-based tab access
        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("VIEW_ATTENDANCE_REPORT")) {
            tabPane.getTabs().add(attendanceTab);
        }

        if (session.hasPermission("VIEW_GRADES_REPORT")) {
            tabPane.getTabs().add(gradesTab);
        }

        if (session.hasPermission("VIEW_FEES_REPORT")) {
            tabPane.getTabs().add(feesTab);
        }
        root.setCenter(tabPane);
    }

    public BorderPane getView() {
        return root;
    }
}
