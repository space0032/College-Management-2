package com.college.fx.views;

import com.college.fx.views.reports.AttendanceReportTab;
import com.college.fx.views.reports.FeesReportTab;
import com.college.fx.views.reports.GradesReportTab;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
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
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Reports & Analytics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        header.getChildren().add(title);
        root.setTop(header);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Attendance Tab
        Tab attendanceTab = new Tab("Attendance");
        attendanceTab.setClosable(false);
        AttendanceReportTab attendanceContent = new AttendanceReportTab();
        attendanceTab.setContent(attendanceContent.getContent());

        // Grades Tab
        Tab gradesTab = new Tab("Grades");
        gradesTab.setClosable(false);
        GradesReportTab gradesContent = new GradesReportTab();
        gradesTab.setContent(gradesContent.getContent());

        // Fees Tab
        Tab feesTab = new Tab("Fees");
        feesTab.setClosable(false);
        FeesReportTab feesContent = new FeesReportTab();
        feesTab.setContent(feesContent.getContent());

        tabPane.getTabs().addAll(attendanceTab, gradesTab, feesTab);
        root.setCenter(tabPane);
    }

    public BorderPane getView() {
        return root;
    }
}
