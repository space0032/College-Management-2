package com.college.fx.views;

import com.college.dao.EmployeeDAO;
import com.college.dao.PayrollDAO;
import com.college.models.Employee;
import com.college.models.PayrollEntry;
import com.college.utils.UIHelperExtensions;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.util.Optional;

public class EmployeeManagementView extends VBox {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final PayrollDAO payrollDAO = new PayrollDAO();
    private TableView<Employee> table;

    public EmployeeManagementView() {
        setSpacing(20);
        setPadding(new Insets(20));

        Label header = new Label("Employee Management (HR)");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        HBox actions = new HBox(10);
        Button btnAdd = new Button("Add Employee");
        btnAdd.getStyleClass().add("accent");
        btnAdd.setOnAction(e -> showAddDialog());

        Button btnPayroll = new Button("Generate Payroll (This Month)");
        btnPayroll.setOnAction(e -> handleGeneratePayroll());

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refreshTable());

        actions.getChildren().addAll(btnAdd, btnPayroll, btnRefresh);

        setupTable();
        refreshTable();

        getChildren().addAll(header, actions, table);
    }

    private void setupTable() {
        table = new TableView<>();
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

        TableColumn<Employee, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));

        table.getColumns().addAll(colId, colName, colEmail, colDesignation, colStatus);
    }

    private void refreshTable() {
        table.getItems().setAll(employeeDAO.getAllEmployees());
    }

    private void showAddDialog() {
        Dialog<Employee> dialog = new Dialog<>();
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
        grid.add(new Label("Join Date:"), 0, 6);
        grid.add(dpJoin, 1, 6);

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
                // Default low salary for manual set later
                e.setSalary(java.math.BigDecimal.ZERO);
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

    private void handleGeneratePayroll() {
        Employee selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select Employee", "Please select an employee to generate payroll.");
            return;
        }

        // Check if salary is set
        if (selected.getSalary() == null || selected.getSalary().compareTo(java.math.BigDecimal.ZERO) <= 0) {
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
