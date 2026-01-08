package com.college.fx.views;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class EmployeeManagementView extends VBox {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final PayrollDAO payrollDAO = new PayrollDAO();
    private TableView<Employee> table;

    public EmployeeManagementView() {
        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("glass-pane");
        getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label header = new Label("Employee Management (HR)");
        header.getStyleClass().add("section-title");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        SessionManager session = SessionManager.getInstance();
        boolean canManage = session.hasPermission("MANAGE_EMPLOYEES");

        Button btnAdd = new Button("Add Employee");
        btnAdd.getStyleClass().add("accent");
        btnAdd.setOnAction(e -> showAddDialog());
        btnAdd.setDisable(!canManage);

        Button btnEdit = new Button("Edit Employee");
        btnEdit.setOnAction(e -> showEditDialog());
        btnEdit.setDisable(!canManage);

        Button btnPayroll = new Button("Generate Payroll (Check)");
        btnPayroll.setOnAction(e -> handleGeneratePayroll());
        btnPayroll.setDisable(!canManage);

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refreshTable());

        actions.getChildren().addAll(btnAdd, btnEdit, btnPayroll, btnRefresh);

        setupTable();
        refreshTable();

        getChildren().addAll(header, actions, table);
    }

    private void setupTable() {
        table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Employee, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmployeeId()));

        TableColumn<Employee, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<Employee, String> colDesignation = new TableColumn<>("Designation");
        colDesignation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDesignation()));

        TableColumn<Employee, String> colSalary = new TableColumn<>("Salary");
        colSalary.setCellValueFactory(data -> {
            BigDecimal salary = data.getValue().getSalary();
            return new SimpleStringProperty(salary != null ? "₹" + salary.toString() : "Not Set");
        });

        TableColumn<Employee, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        table.getColumns()
                .addAll(java.util.Arrays.asList(colId, colName, colEmail, colDesignation, colSalary, colStatus));
    }

    private void refreshTable() {
        table.getItems().setAll(employeeDAO.getAllEmployees());
    }

    private void showAddDialog() {
        Dialog<Employee> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Add New Employee");
        dialog.setHeaderText("Enter Employee Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfId = new TextField();
        TextField tfFirst = new TextField();
        TextField tfLast = new TextField();
        TextField tfEmail = new TextField();
        TextField tfPhone = new TextField();
        TextField tfDesignation = new TextField();
        TextField tfSalary = new TextField();
        tfSalary.setPromptText("e.g., 50000.00");
        DatePicker dpJoin = new DatePicker(LocalDate.now());

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(tfId, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(tfFirst, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(tfLast, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(tfEmail, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(tfPhone, 1, 4);
        grid.add(new Label("Designation:"), 0, 5);
        grid.add(tfDesignation, 1, 5);
        grid.add(new Label("Salary (₹):"), 0, 6);
        grid.add(tfSalary, 1, 6);
        grid.add(new Label("Join Date:"), 0, 7);
        grid.add(dpJoin, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Employee e = new Employee();
                e.setEmployeeId(tfId.getText());
                e.setFirstName(tfFirst.getText());
                e.setLastName(tfLast.getText());
                e.setEmail(tfEmail.getText());
                e.setPhone(tfPhone.getText());
                e.setDesignation(tfDesignation.getText());
                e.setJoinDate(dpJoin.getValue());
                e.setStatus(Employee.Status.ACTIVE);

                // Parse salary
                try {
                    String salaryText = tfSalary.getText().trim();
                    if (!salaryText.isEmpty()) {
                        e.setSalary(new BigDecimal(salaryText));
                    } else {
                        e.setSalary(BigDecimal.ZERO);
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Salary", "Please enter a valid salary amount.");
                    return null;
                }
                return e;
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            if (employeeDAO.addEmployee(employee)) {
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add employee.");
            }
        });
    }

    private void showEditDialog() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Employee", "Please select an employee to edit.");
            return;
        }

        Dialog<Employee> dialog = new Dialog<>();
        dialog.setTitle("Edit Employee");
        dialog.setHeaderText("Update Employee Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField tfId = new TextField(selected.getEmployeeId());
        tfId.setDisable(true); // Cannot change employee ID
        TextField tfFirst = new TextField(selected.getFirstName());
        TextField tfLast = new TextField(selected.getLastName());
        TextField tfEmail = new TextField(selected.getEmail());
        TextField tfPhone = new TextField(selected.getPhone() != null ? selected.getPhone() : "");
        TextField tfDesignation = new TextField(selected.getDesignation());
        TextField tfSalary = new TextField(selected.getSalary() != null ? selected.getSalary().toString() : "");
        tfSalary.setPromptText("e.g., 50000.00");
        DatePicker dpJoin = new DatePicker(selected.getJoinDate() != null ? selected.getJoinDate() : LocalDate.now());

        ComboBox<Employee.Status> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll(Employee.Status.values());
        cbStatus.setValue(selected.getStatus());

        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(tfId, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(tfFirst, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(tfLast, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(tfEmail, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(tfPhone, 1, 4);
        grid.add(new Label("Designation:"), 0, 5);
        grid.add(tfDesignation, 1, 5);
        grid.add(new Label("Salary (₹):"), 0, 6);
        grid.add(tfSalary, 1, 6);
        grid.add(new Label("Join Date:"), 0, 7);
        grid.add(dpJoin, 1, 7);
        grid.add(new Label("Status:"), 0, 8);
        grid.add(cbStatus, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                selected.setFirstName(tfFirst.getText());
                selected.setLastName(tfLast.getText());
                selected.setEmail(tfEmail.getText());
                selected.setPhone(tfPhone.getText());
                selected.setDesignation(tfDesignation.getText());
                selected.setJoinDate(dpJoin.getValue());
                selected.setStatus(cbStatus.getValue());

                // Parse salary
                try {
                    String salaryText = tfSalary.getText().trim();
                    if (!salaryText.isEmpty()) {
                        selected.setSalary(new BigDecimal(salaryText));
                    } else {
                        selected.setSalary(BigDecimal.ZERO);
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Salary", "Please enter a valid salary amount.");
                    return null;
                }
                return selected;
            }
            return null;
        });

        Optional<Employee> result = dialog.showAndWait();
        result.ifPresent(employee -> {
            if (employeeDAO.updateEmployee(employee)) {
                refreshTable();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update employee.");
            }
        });
    }

    private void handleGeneratePayroll() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Employee", "Please select an employee to generate payroll.");
            return;
        }

        // Check if salary is set
        if (selected.getId() == 0) {
            showAlert(Alert.AlertType.ERROR, "Profile Missing",
                    "This employee has no profile (salary/join date). Please 'Edit' to save profile first.");
            return;
        }
        if (selected.getSalary() == null || selected.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            showAlert(Alert.AlertType.ERROR, "Invalid Salary", "Employee has no salary set. Edit employee first.");
            return;
        }

        PayrollEntry entry = new PayrollEntry(
                selected.getId(),
                LocalDate.now().getMonthValue(),
                LocalDate.now().getYear(),
                selected.getSalary());

        if (payrollDAO.createPayrollEntry(entry)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Payroll generated for " + selected.getFirstName());
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate payroll. Check logs.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
