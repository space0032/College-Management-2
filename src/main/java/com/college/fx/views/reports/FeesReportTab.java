package com.college.fx.views.reports;

import com.college.dao.EnhancedFeeDAO;
import com.college.models.StudentFee;
import com.college.utils.ReportGenerator;
import com.college.utils.DialogUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FeesReportTab {

    private VBox content;
    private EnhancedFeeDAO feeDAO;
    private PieChart statusChart;
    private TableView<StudentFee> tableView;
    private ObservableList<StudentFee> tableData;
    private Label totalCollectionLabel;
    private Label pendingAmountLabel;

    public FeesReportTab() {
        this.feeDAO = new EnhancedFeeDAO();
        createContent();
        loadData();
    }

    private void createContent() {
        content = new VBox(20);
        content.setPadding(new Insets(20));

        // Header Stats
        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        totalCollectionLabel = createStatLabel("Total Collected", 0);
        pendingAmountLabel = createStatLabel("Pending Amount", 0);

        Button exportBtn = new Button("Export CSV");
        exportBtn.setOnAction(e -> exportReport());
        exportBtn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statsBox.getChildren().addAll(totalCollectionLabel, pendingAmountLabel, spacer, exportBtn);

        // Split Pane for Chart and Table
        SplitPane splitPane = new SplitPane();
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Chart Section
        VBox chartBox = new VBox(10);
        chartBox.setAlignment(Pos.CENTER);
        statusChart = new PieChart();
        statusChart.setTitle("Fee Payment Status");
        statusChart.setLabelsVisible(true);
        chartBox.getChildren().add(statusChart);

        // Table Section
        VBox tableBox = new VBox(10);
        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableData = FXCollections.observableArrayList();
        tableView.setItems(tableData);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<StudentFee, String> nameCol = new TableColumn<>("Student");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));

        TableColumn<StudentFee, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));

        TableColumn<StudentFee, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());

        TableColumn<StudentFee, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPaidAmount()).asObject());

        TableColumn<StudentFee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<StudentFee, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PAID".equals(item))
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    else if ("PENDING".equals(item))
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                }
            }
        });

        tableView.getColumns().addAll(java.util.Arrays.asList(nameCol, categoryCol, totalCol, paidCol, statusCol));
        tableBox.getChildren().add(tableView);

        splitPane.getItems().addAll(chartBox, tableBox);
        splitPane.setDividerPositions(0.4);

        content.getChildren().addAll(statsBox, splitPane);
    }

    private Label createStatLabel(String title, double value) {
        Label label = new Label(String.format("%s: $%.2f", title, value));
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        return label;
    }

    private void updateStatLabel(Label label, String title, double value) {
        label.setText(String.format("%s: $%.2f", title, value));
    }

    private void loadData() {
        List<StudentFee> fees = feeDAO.getAllFees();
        tableData.setAll(fees);

        // Calculate Stats
        double totalCollected = 0;
        double totalPending = 0;
        int paidCount = 0;
        int partialCount = 0;
        int pendingCount = 0;

        for (StudentFee fee : fees) {
            totalCollected += fee.getPaidAmount();
            totalPending += (fee.getTotalAmount() - fee.getPaidAmount());

            switch (fee.getStatus()) {
                case "PAID":
                    paidCount++;
                    break;
                case "PARTIAL":
                    partialCount++;
                    break;
                case "PENDING":
                    pendingCount++;
                    break;
            }
        }

        updateStatLabel(totalCollectionLabel, "Total Collected", totalCollected);
        updateStatLabel(pendingAmountLabel, "Pending Amount", totalPending);

        // Update Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Paid (" + paidCount + ")", paidCount),
                new PieChart.Data("Partial (" + partialCount + ")", partialCount),
                new PieChart.Data("Pending (" + pendingCount + ")", pendingCount));
        statusChart.setData(pieData);

        // Apply Semantic Colors
        for (PieChart.Data data : pieData) {
            String name = data.getName();
            // Wait for node to be created
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    updateSliceColor(newNode, name);
                }
            });
            // If already created (e.g. data updated)
            if (data.getNode() != null) {
                updateSliceColor(data.getNode(), name);
            }
        }
    }

    private void updateSliceColor(javafx.scene.Node node, String name) {
        if (name.startsWith("Paid")) {
            node.setStyle("-fx-pie-color: #22c55e;"); // Green
        } else if (name.startsWith("Pending")) {
            node.setStyle("-fx-pie-color: #ef4444;"); // Red
        } else if (name.startsWith("Partial")) {
            node.setStyle("-fx-pie-color: #f59e0b;"); // Amber
        }
    }

    private void exportReport() {
        if (tableData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            DialogUtils.styleDialog(alert);
            alert.setTitle("No Data");
            alert.setContentText("No data to export.");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Fee Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("fee_report.csv");
        File file = fileChooser.showSaveDialog(content.getScene().getWindow());

        if (file != null) {
            List<Object[]> data = new ArrayList<>();
            for (StudentFee fee : tableData) {
                data.add(new Object[] {
                        fee.getStudentName(),
                        fee.getCategoryName(),
                        String.valueOf(fee.getTotalAmount()),
                        String.valueOf(fee.getPaidAmount()),
                        fee.getStatus()
                });
            }

            boolean success = ReportGenerator.generateCSV(file.getAbsolutePath(),
                    new String[] { "Student", "Category", "Total", "Paid", "Status" }, data);

            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                DialogUtils.styleDialog(alert);
                alert.setTitle("Success");
                alert.setContentText("Report exported successfully!");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                DialogUtils.styleDialog(alert);
                alert.setTitle("Error");
                alert.setContentText("Failed to export report.");
                alert.showAndWait();
            }
        }
    }

    public javafx.scene.Node getContent() {
        return content;
    }
}
