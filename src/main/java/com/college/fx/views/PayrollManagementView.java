package com.college.fx.views;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.college.utils.SessionManager;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

public class PayrollManagementView extends VBox {
    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private TableView<PayrollEntry> table;
    private ComboBox<String> monthCombo;
    private ComboBox<Integer> yearCombo;
    private Map<Integer, String> employeeNamesCache = new HashMap<>();

    public PayrollManagementView() {
        setSpacing(20);
        setPadding(new Insets(20));

        // Cache employee names for display
        loadEmployeeNames();

        Label header = new Label("Payroll Management");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Filters and actions
        HBox filtersAndActions = createFiltersAndActions();

        setupTable();
        refreshTable();

        getChildren().addAll(header, filtersAndActions, table);
    }

    private void loadEmployeeNames() {
        List<Employee> employees = employeeDAO.getAllEmployees();
        for (Employee emp : employees) {
            employeeNamesCache.put(emp.getId(), emp.getFirstName() + " " + emp.getLastName());
        }
    }

    private HBox createFiltersAndActions() {
        HBox container = new HBox(15);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Filter section
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold;");

        monthCombo = new ComboBox<>();
        monthCombo.getItems().add("All");
        for (Month month : Month.values()) {
            monthCombo.getItems().add(month.toString());
        }
        monthCombo.setValue("All");
        monthCombo.setPrefWidth(150);

        yearCombo = new ComboBox<>();
        yearCombo.getItems().add(0); // 0 represents "All"
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear; year >= currentYear - 5; year--) {
            yearCombo.getItems().add(year);
        }
        yearCombo.setValue(0);
        yearCombo.setPrefWidth(100);

        Button btnFilter = new Button("Apply Filter");
        btnFilter.setOnAction(e -> applyFilter());

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refreshTable());

        // Check permissions for action buttons
        SessionManager session = SessionManager.getInstance();
        boolean canManage = session.hasPermission("MANAGE_PAYROLL");
        boolean canApprove = session.hasPermission("APPROVE_PAYROLL") || canManage;

        // Action buttons
        Button btnEdit = new Button("Edit Entry");
        btnEdit.setOnAction(e -> showEditDialog());
        btnEdit.setDisable(!canManage);

        Button btnMarkPaid = new Button("Mark as Paid");
        btnMarkPaid.getStyleClass().add("accent");
        btnMarkPaid.setOnAction(e -> markAsPaid());
        btnMarkPaid.setDisable(!canApprove);

        Button btnBulkMarkPaid = new Button("Bulk Mark as Paid");
        btnBulkMarkPaid.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white;");
        btnBulkMarkPaid.setOnAction(e -> handleBulkMarkAsPaid());
        btnBulkMarkPaid.setDisable(!canApprove);

        Button btnExport = new Button("Export CSV");
        btnExport.setOnAction(e -> exportToCSV());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        container.getChildren().addAll(
                filterLabel, monthCombo, yearCombo, btnFilter, btnRefresh,
                spacer, btnEdit, btnMarkPaid, btnBulkMarkPaid, btnExport);

        return container;
    }

    private void setupTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PayrollEntry, String> colEmpId = new TableColumn<>("Employee ID");
        colEmpId.setCellValueFactory(data -> {
            int empId = data.getValue().getEmployeeId();
            return new SimpleStringProperty("EMP-" + empId);
        });
        colEmpId.setPrefWidth(100);

        TableColumn<PayrollEntry, String> colEmpName = new TableColumn<>("Employee Name");
        colEmpName.setCellValueFactory(data -> {
            String name = employeeNamesCache.getOrDefault(data.getValue().getEmployeeId(), "Unknown");
            return new SimpleStringProperty(name);
        });
        colEmpName.setPrefWidth(150);

        TableColumn<PayrollEntry, String> colPeriod = new TableColumn<>("Period");
        colPeriod.setCellValueFactory(data -> {
            PayrollEntry entry = data.getValue();
            String monthName = Month.of(entry.getMonth()).toString();
            return new SimpleStringProperty(monthName + " " + entry.getYear());
        });
        colPeriod.setPrefWidth(120);

        TableColumn<PayrollEntry, String> colBasic = new TableColumn<>("Basic Salary");
        colBasic.setCellValueFactory(
                data -> new SimpleStringProperty("₹" + formatCurrency(data.getValue().getBasicSalary())));
        colBasic.setPrefWidth(100);

        TableColumn<PayrollEntry, String> colBonuses = new TableColumn<>("Bonuses");
        colBonuses.setCellValueFactory(
                data -> new SimpleStringProperty("₹" + formatCurrency(data.getValue().getBonuses())));
        colBonuses.setPrefWidth(90);

        TableColumn<PayrollEntry, String> colDeductions = new TableColumn<>("Deductions");
        colDeductions.setCellValueFactory(
                data -> new SimpleStringProperty("₹" + formatCurrency(data.getValue().getDeductions())));
        colDeductions.setPrefWidth(90);

        TableColumn<PayrollEntry, String> colNet = new TableColumn<>("Net Salary");
        colNet.setCellValueFactory(
                data -> new SimpleStringProperty("₹" + formatCurrency(data.getValue().getNetSalary())));
        colNet.setPrefWidth(100);
        colNet.setStyle("-fx-font-weight: bold;");

        TableColumn<PayrollEntry, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> {
            PayrollEntry.Status status = data.getValue().getStatus();
            return new SimpleStringProperty(status.name());
        });
        colStatus.setCellFactory(col -> new TableCell<PayrollEntry, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PAID":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "PENDING":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "CANCELLED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        colStatus.setPrefWidth(80);

        TableColumn<PayrollEntry, String> colPaymentDate = new TableColumn<>("Payment Date");
        colPaymentDate.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getPaymentDate();
            return new SimpleStringProperty(date != null ? date.toString() : "-");
        });
        colPaymentDate.setPrefWidth(100);

        table.getColumns().addAll(colEmpId, colEmpName, colPeriod, colBasic,
                colBonuses, colDeductions, colNet, colStatus, colPaymentDate);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0.00";
        return String.format("%,.2f", amount);
    }

    private void refreshTable() {
        monthCombo.setValue("All");
        yearCombo.setValue(0);
        table.getItems().setAll(payrollDAO.getAllPayrollEntries());
    }

    private void applyFilter() {
        String selectedMonth = monthCombo.getValue();
        Integer selectedYear = yearCombo.getValue();

        if (selectedMonth.equals("All") || selectedYear == 0) {
            // Show all entries
            table.getItems().setAll(payrollDAO.getAllPayrollEntries());
        } else {
            // Get month number
            int monthNum = Month.valueOf(selectedMonth).getValue();
            table.getItems().setAll(payrollDAO.getPayrollEntriesByMonthYear(monthNum, selectedYear));
        }
    }

    private void showEditDialog() {
        PayrollEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Entry", "Please select a payroll entry to edit.");
            return;
        }

        // Prevent editing if status is PAID
        if (selected.getStatus() == PayrollEntry.Status.PAID) {
            showAlert(Alert.AlertType.ERROR, "Cannot Edit", "Payroll entries with PAID status cannot be edited.");
            return;
        }

        Dialog<PayrollEntry> dialog = new Dialog<>();
        dialog.setTitle("Edit Payroll Entry");
        dialog.setHeaderText("Update Bonuses and Deductions");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        String empName = employeeNamesCache.getOrDefault(selected.getEmployeeId(), "Unknown");
        String period = Month.of(selected.getMonth()).toString() + " " + selected.getYear();

        Label lblEmployee = new Label(empName);
        Label lblPeriod = new Label(period);
        Label lblBasic = new Label("₹" + formatCurrency(selected.getBasicSalary()));

        TextField tfBonuses = new TextField(selected.getBonuses().toString());
        tfBonuses.setPromptText("e.g., 5000.00");
        TextField tfDeductions = new TextField(selected.getDeductions().toString());
        tfDeductions.setPromptText("e.g., 1000.00");

        Label lblNetPreview = new Label();
        lblNetPreview.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Update net salary preview when values change
        Runnable updateNet = () -> {
            try {
                BigDecimal bonuses = new BigDecimal(tfBonuses.getText().isEmpty() ? "0" : tfBonuses.getText());
                BigDecimal deductions = new BigDecimal(tfDeductions.getText().isEmpty() ? "0" : tfDeductions.getText());
                BigDecimal net = selected.getBasicSalary().add(bonuses).subtract(deductions);
                lblNetPreview.setText("₹" + formatCurrency(net));
            } catch (NumberFormatException e) {
                lblNetPreview.setText("Invalid");
            }
        };

        tfBonuses.textProperty().addListener((obs, old, newVal) -> updateNet.run());
        tfDeductions.textProperty().addListener((obs, old, newVal) -> updateNet.run());
        updateNet.run(); // Initial calculation

        grid.add(new Label("Employee:"), 0, 0);
        grid.add(lblEmployee, 1, 0);
        grid.add(new Label("Period:"), 0, 1);
        grid.add(lblPeriod, 1, 1);
        grid.add(new Label("Basic Salary:"), 0, 2);
        grid.add(lblBasic, 1, 2);
        grid.add(new Label("Bonuses (₹):"), 0, 3);
        grid.add(tfBonuses, 1, 3);
        grid.add(new Label("Deductions (₹):"), 0, 4);
        grid.add(tfDeductions, 1, 4);
        grid.add(new Label("Net Salary:"), 0, 5);
        grid.add(lblNetPreview, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    selected.setBonuses(new BigDecimal(tfBonuses.getText().isEmpty() ? "0" : tfBonuses.getText()));
                    selected.setDeductions(
                            new BigDecimal(tfDeductions.getText().isEmpty() ? "0" : tfDeductions.getText()));
                    return selected;
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid amounts.");
                    return null;
                }
            }
            return null;
        });

        Optional<PayrollEntry> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            if (payrollDAO.updatePayrollEntry(entry)) {
                applyFilter(); // Refresh with current filter
                showAlert(Alert.AlertType.INFORMATION, "Success", "Payroll entry updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update payroll entry.");
            }
        });
    }

    private void markAsPaid() {
        PayrollEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Entry", "Please select a payroll entry to mark as paid.");
            return;
        }

        if (selected.getStatus() == PayrollEntry.Status.PAID) {
            showAlert(Alert.AlertType.INFORMATION, "Already Paid", "This entry is already marked as paid.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Payment");
        confirm.setHeaderText("Mark as Paid");
        String empName = employeeNamesCache.getOrDefault(selected.getEmployeeId(), "Unknown");
        confirm.setContentText("Mark payroll for " + empName + " as PAID?");

        Optional<ButtonType> response = confirm.showAndWait();
        if (response.isPresent() && response.get() == ButtonType.OK) {
            if (payrollDAO.markAsPaid(selected.getId())) {
                applyFilter(); // Refresh with current filter
                showAlert(Alert.AlertType.INFORMATION, "Success", "Payroll entry marked as paid!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark payroll entry as paid.");
            }
        }
    }

    private void exportToCSV() {
        List<PayrollEntry> entries = table.getItems();
        if (entries.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No payroll entries to export.");
            return;
        }

        String filename = "payroll_export_" + LocalDate.now() + ".csv";
        try (FileWriter writer = new FileWriter(filename)) {
            // Write header
            writer.append(
                    "Employee ID,Employee Name,Month,Year,Basic Salary,Bonuses,Deductions,Net Salary,Status,Payment Date\n");

            // Write data
            for (PayrollEntry entry : entries) {
                String empName = employeeNamesCache.getOrDefault(entry.getEmployeeId(), "Unknown");
                writer.append(String.valueOf(entry.getEmployeeId())).append(",");
                writer.append(empName).append(",");
                writer.append(String.valueOf(entry.getMonth())).append(",");
                writer.append(String.valueOf(entry.getYear())).append(",");
                writer.append(entry.getBasicSalary().toString()).append(",");
                writer.append(entry.getBonuses().toString()).append(",");
                writer.append(entry.getDeductions().toString()).append(",");
                writer.append(entry.getNetSalary().toString()).append(",");
                writer.append(entry.getStatus().name()).append(",");
                writer.append(entry.getPaymentDate() != null ? entry.getPaymentDate().toString() : "").append("\n");
            }

            showAlert(Alert.AlertType.INFORMATION, "Export Success",
                    "Payroll data exported to " + filename);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed",
                    "Failed to export payroll data: " + e.getMessage());
        }
    }

    private void handleBulkMarkAsPaid() {
        // Get all PENDING payroll entries from current view
        List<PayrollEntry> pendingEntries = table.getItems().stream()
                .filter(entry -> entry.getStatus() == PayrollEntry.Status.PENDING)
                .collect(java.util.stream.Collectors.toList());

        if (pendingEntries.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Pending Entries",
                    "There are no pending payroll entries to process.");
            return;
        }

        // Dialog for bonus/deduction input
        Dialog<BigDecimal[]> dialog = new Dialog<>();
        dialog.setTitle("Bulk Mark as Paid");
        dialog.setHeaderText("Process " + pendingEntries.size() + " pending payroll entries");

        ButtonType confirmButtonType = new ButtonType("Confirm & Mark Paid", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label infoLabel = new Label("Apply same bonus/deduction to all pending entries:");
        infoLabel.setStyle("-fx-font-weight: bold;");

        TextField tfBonus = new TextField("0");
        tfBonus.setPromptText("e.g., 5000.00");

        TextField tfDeduction = new TextField("0");
        tfDeduction.setPromptText("e.g., 1000.00");

        grid.add(infoLabel, 0, 0, 2, 1);
        grid.add(new Label("Bonus (₹):"), 0, 1);
        grid.add(tfBonus, 1, 1);
        grid.add(new Label("Deduction (₹):"), 0, 2);
        grid.add(tfDeduction, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                try {
                    BigDecimal bonus = new BigDecimal(tfBonus.getText().isEmpty() ? "0" : tfBonus.getText());
                    BigDecimal deduction = new BigDecimal(
                            tfDeduction.getText().isEmpty() ? "0" : tfDeduction.getText());
                    return new BigDecimal[] { bonus, deduction };
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter valid amounts.");
                    return null;
                }
            }
            return null;
        });

        Optional<BigDecimal[]> result = dialog.showAndWait();
        result.ifPresent(amounts -> {
            BigDecimal bonus = amounts[0];
            BigDecimal deduction = amounts[1];

            int successCount = 0;
            int failCount = 0;

            for (PayrollEntry entry : pendingEntries) {
                // Apply bonus/deduction
                entry.setBonuses(entry.getBonuses().add(bonus));
                entry.setDeductions(entry.getDeductions().add(deduction));
                entry.calculateNet(); // Recalculate net salary

                // Update entry first
                if (payrollDAO.updatePayrollEntry(entry)) {
                    // Then mark as paid
                    if (payrollDAO.markAsPaid(entry.getId())) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } else {
                    failCount++;
                }
            }

            // Refresh table
            applyFilter();

            // Completion dialog
            Alert completion = new Alert(Alert.AlertType.INFORMATION);
            completion.setTitle("Bulk Payment Complete");
            completion.setHeaderText("Payroll Processing Complete");
            completion.setContentText(
                    "✓ Successfully paid: " + successCount + " entries\n" +
                            "✗ Failed: " + failCount + "\n\n" +
                            "Bonus applied: ₹" + formatCurrency(bonus) + "\n" +
                            "Deduction applied: ₹" + formatCurrency(deduction));
            completion.showAndWait();
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
