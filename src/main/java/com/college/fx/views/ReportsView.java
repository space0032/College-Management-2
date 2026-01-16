package com.college.fx.views;

import com.college.fx.views.reports.AttendanceReportTab;
import com.college.fx.views.reports.FeesReportTab;
import com.college.fx.views.reports.GradesReportTab;
import com.college.services.ExcelReportGenerator;
import com.college.services.PdfReportGenerator;
import com.college.services.ReportDataService;
import com.college.utils.DialogUtils;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class ReportsView {

    private final ReportDataService reportService;
    private final PdfReportGenerator pdfGenerator;
    private final ExcelReportGenerator excelGenerator;

    public ReportsView() {
        this.reportService = new ReportDataService();
        this.pdfGenerator = new PdfReportGenerator();
        this.excelGenerator = new ExcelReportGenerator();
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent;");

        Label header = new Label("Reports & Analytics");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Tab Pane Configuration
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        // 1. Visitor Log Tab
        Tab visitorTab = new Tab("Visitor Logs");
        visitorTab.setContent(createVisitorSection());

        // 2. Attendance Tab
        Tab attendanceTab = new Tab("Attendance");
        AttendanceReportTab attendanceView = new AttendanceReportTab();
        attendanceTab.setContent(attendanceView.getContent());

        // 3. Fees Tab
        Tab feesTab = new Tab("Fees");
        FeesReportTab feesView = new FeesReportTab();
        feesTab.setContent(feesView.getContent());

        // 4. Grades Tab
        Tab gradesTab = new Tab("Grades");
        GradesReportTab gradesView = new GradesReportTab();
        gradesTab.setContent(gradesView.getContent());

        // 5. Placement Tab
        Tab placementTab = new Tab("Placements");
        placementTab.setContent(createPlacementSection());

        tabPane.getTabs().addAll(visitorTab, attendanceTab, feesTab, gradesTab, placementTab);

        root.getChildren().addAll(header, tabPane);
        return root;
    }

    private VBox createVisitorSection() {
        VBox section = new VBox(15);
        section.setStyle("-fx-padding: 20;");

        GridPane visitorGrid = new GridPane();
        visitorGrid.setHgap(15);
        visitorGrid.setVgap(15);
        visitorGrid.setPadding(new Insets(10));

        DatePicker startDate = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker endDate = new DatePicker(LocalDate.now());

        Button pdfBtn = new Button("Export PDF");
        styleButton(pdfBtn);
        pdfBtn.setOnAction(e -> generateVisitorReport(startDate.getValue(), endDate.getValue(), "pdf"));

        Button excelBtn = new Button("Export Excel");
        styleButton(excelBtn);
        excelBtn.setOnAction(e -> generateVisitorReport(startDate.getValue(), endDate.getValue(), "excel"));

        visitorGrid.add(new Label("Start Date:"), 0, 0);
        visitorGrid.add(startDate, 1, 0);
        visitorGrid.add(new Label("End Date:"), 2, 0);
        visitorGrid.add(endDate, 3, 0);
        visitorGrid.add(pdfBtn, 0, 1);
        visitorGrid.add(excelBtn, 1, 1);

        // Style labels in grid
        visitorGrid.getChildren().stream()
                .filter(n -> n instanceof Label)
                .forEach(n -> n.setStyle("-fx-text-fill: white;"));

        section.getChildren().add(visitorGrid);
        return section;
    }

    private VBox createPlacementSection() {
        VBox placementSection = new VBox(15);
        placementSection.setStyle("-fx-padding: 20;");

        Button placementPdfBtn = new Button("Export Placement Summary (PDF)");
        styleButton(placementPdfBtn);
        placementPdfBtn.setDisable(true); // Placeholder for now or simple impl
        // placementPdfBtn.setOnAction(e -> generatePlacementReport());

        Label comingSoon = new Label("Detailed placement reports coming soon.");
        comingSoon.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");

        placementSection.getChildren().addAll(placementPdfBtn, comingSoon);
        return placementSection;
    }

    private void styleButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand;"));
    }

    private void generateVisitorReport(LocalDate start, LocalDate end, String type) {
        if (start == null || end == null || start.isAfter(end)) {
            DialogUtils.showError("Invalid Date Range", "Please select a valid start and end date.");
            return;
        }

        var logs = reportService.getVisitorLogs(start, end);
        if (logs.isEmpty()) {
            DialogUtils.showInfo("No Data", "No visitor logs found for the selected range.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(
                "visitor_report_" + start + "_to_" + end + "." + (type.equals("excel") ? "xlsx" : "pdf"));

        if (type.equals("pdf")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        }

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                if (type.equals("pdf")) {
                    pdfGenerator.generateVisitorLogPdf(logs, file);
                } else {
                    excelGenerator.generateVisitorLogExcel(logs, file);
                }
                DialogUtils.showInfo("Success", "Report generated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtils.showError("Error", "Failed to generate report: " + e.getMessage());
            }
        }
    }
}
