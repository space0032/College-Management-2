package com.college.fx.views;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PayrollManagementView {

    private VBox root;
    private TableView<PayrollEntry> payrollTable;
    private ObservableList<PayrollEntry> payrollData;
    private PayrollDAO payrollDAO;
    private EmployeeDAO employeeDAO;

    private ComboBox<Month> monthCombo;
    private Spinner<Integer> yearSpinner;
    private Map<Integer, Employee> employeeMap;

    public PayrollManagementView() {
        this.payrollDAO = new PayrollDAO();
        this.employeeDAO = new EmployeeDAO();
        this.payrollData = FXCollections.observableArrayList();
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Payroll Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        HBox controls = createControls();
        payrollTable = createTable();

        root.getChildren().addAll(title, controls, payrollTable);

        // Initial Load
        loadEmployees();
        refreshData();
    }

    private void loadEmployees() {
        List<Employee> employees = employeeDAO.getAllEmployees(); // Ensure this method exists
        employeeMap = employees.stream().collect(Collectors.toMap(Employee::getId, e -> e));
    }

    private HBox createControls() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);

        monthCombo = new ComboBox<>();
        monthCombo.getItems().addAll(Month.values());
        monthCombo.setValue(LocalDate.now().getMonth());
        monthCombo.setOnAction(e -> refreshData());

        yearSpinner = new Spinner<>(2020, 2030, LocalDate.now().getYear());
        yearSpinner.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());

        Button generateBtn = new Button("Generate Payroll");
        generateBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        generateBtn.setOnAction(e -> generatePayroll());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refreshData());

        Button bulkPayBtn = new Button("Mark All Paid");
        bulkPayBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold;");
        bulkPayBtn.setOnAction(e -> markAllAsPaid());

        box.getChildren().addAll(new Label("Month:"), monthCombo, new Label("Year:"), yearSpinner, generateBtn,
                bulkPayBtn, refreshBtn);
        return box;
    }

    private TableView<PayrollEntry> createTable() {
        TableView<PayrollEntry> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setItems(payrollData);

        TableColumn<PayrollEntry, String> empCol = new TableColumn<>("Employee");
        empCol.setCellValueFactory(data -> {
            Employee e = employeeMap.get(data.getValue().getEmployeeId());
            return new SimpleStringProperty(e != null ? e.getFirstName() + " " + e.getLastName() : "Unknown");
        });

        TableColumn<PayrollEntry, String> basicCol = new TableColumn<>("Basic Salary");
        basicCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBasicSalary().toString()));

        TableColumn<PayrollEntry, String> bonusCol = new TableColumn<>("Bonus");
        bonusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBonuses().toString()));

        TableColumn<PayrollEntry, String> dedCol = new TableColumn<>("Deductions");
        dedCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeductions().toString()));

        TableColumn<PayrollEntry, String> netCol = new TableColumn<>("Net Salary");
        netCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNetSalary().toString()));

        TableColumn<PayrollEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        TableColumn<PayrollEntry, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button payBtn = new Button("Mark Paid");
            private final HBox pane = new HBox(5, editBtn, payBtn);

            {
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                payBtn.setOnAction(e -> markAsPaid(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PayrollEntry entry = getTableView().getItems().get(getIndex());
                    if (entry.getStatus() == PayrollEntry.Status.PAID) {
                        payBtn.setDisable(true);
                        editBtn.setDisable(true);
                    } else {
                        payBtn.setDisable(false);
                        editBtn.setDisable(false);
                    }
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().addAll(empCol, basicCol, bonusCol, dedCol, netCol, statusCol, actionCol);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    private void refreshData() {
        int month = monthCombo.getValue().getValue();
        int year = yearSpinner.getValue();
        payrollData.setAll(payrollDAO.getPayrollEntriesByMonthYear(month, year));
    }

    private void generatePayroll() {
        int month = monthCombo.getValue().getValue();
        int year = yearSpinner.getValue();

        List<Employee> activeEmployees = employeeDAO.getAllEmployees().stream()
                .filter(e -> e.getStatus() == Employee.Status.ACTIVE)
                .collect(Collectors.toList());

        int count = 0;
        for (Employee e : activeEmployees) {
            // Check if exists
            boolean exists = payrollData.stream()
                    .anyMatch(p -> p.getEmployeeId() == e.getId());

            if (!exists) {
                PayrollEntry entry = new PayrollEntry(e.getId(), month, year, e.getSalary());
                if (payrollDAO.createPayrollEntry(entry)) {
                    count++;
                }
            }
        }

        if (count > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Generated " + count + " payroll entries.");
            refreshData();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Info", "No new payroll entries generated.");
        }
    }

    private void showEditDialog(PayrollEntry entry) {
        Dialog<PayrollEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Payroll");
        dialog.setHeaderText("Edit Deductions/Bonuses");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField bonusField = new TextField(entry.getBonuses().toString());
        TextField dedField = new TextField(entry.getDeductions().toString());

        grid.add(new Label("Bonuses:"), 0, 0);
        grid.add(bonusField, 1, 0);
        grid.add(new Label("Deductions:"), 0, 1);
        grid.add(dedField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    entry.setBonuses(new BigDecimal(bonusField.getText()));
                    entry.setDeductions(new BigDecimal(dedField.getText()));
                    return entry;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid number format");
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(e -> {
            payrollDAO.updatePayrollEntry(e);
            refreshData();
        });
    }

    private void markAsPaid(PayrollEntry entry) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Mark this entry as PAID?", ButtonType.YES,
                ButtonType.NO);
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                if (payrollDAO.markAsPaid(entry.getId())) {
                    refreshData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                }
            }
        });
    }

    private void markAllAsPaid() {
        int month = monthCombo.getValue().getValue();
        int year = yearSpinner.getValue();

        long pendingCount = payrollData.stream()
                .filter(p -> p.getStatus() != PayrollEntry.Status.PAID)
                .count();

        if (pendingCount == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Info", "No pending entries to pay.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Mark all " + pendingCount + " pending entries for " + monthCombo.getValue() + " " + year + " as PAID?",
                ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                if (payrollDAO.markMonthAsPaid(month, year)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "All entries marked as PAID.");
                    refreshData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update statuses.");
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    public VBox getView() {
        return root;
    }
}
