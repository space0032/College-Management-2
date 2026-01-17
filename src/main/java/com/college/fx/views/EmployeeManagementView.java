package com.college.fx.views;

import com.college.dao.EmployeeDAO;
import com.college.models.Employee;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeManagementView extends VBox {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private TableView<Employee> table;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ObservableList<Employee> allEmployees;
    private Label statsLabel;

    public EmployeeManagementView() {
        setSpacing(20);
        setPadding(new Insets(20));
        getStyleClass().add("glass-pane");
        getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header with stats
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label header = new Label("👥 Employee Management");
        header.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statsLabel = new Label();
        statsLabel.getStyleClass().addAll("text-white");
        statsLabel.setStyle("-fx-font-size: 14px;");
        
        headerBox.getChildren().addAll(header, spacer, statsLabel);

        // Search and Filter Bar
        HBox searchBar = createSearchBar();

        // Action Buttons
        HBox actions = createActionButtons();

        setupTable();
        refreshTable();

        getChildren().addAll(headerBox, searchBar, actions, table);
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10));
        searchBar.getStyleClass().add("glass-card");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 16px;");

        searchField = new TextField();
        searchField.setPromptText("Search by name, ID, email, or designation...");
        searchField.setPrefWidth(350);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, newVal) -> filterEmployees());

        Label filterLabel = new Label("Status:");
        filterLabel.getStyleClass().add("text-white");

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "ACTIVE", "INACTIVE", "ON_LEAVE");
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("combo-box");
        statusFilter.setOnAction(e -> filterEmployees());

        searchBar.getChildren().addAll(searchIcon, searchField, filterLabel, statusFilter);
        return searchBar;
    }

    private HBox createActionButtons() {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        SessionManager session = SessionManager.getInstance();
        boolean canManage = session.hasPermission("MANAGE_EMPLOYEES");

        Button btnAdd = new Button("➕ Add Employee");
        btnAdd.getStyleClass().add("accent-button");
        btnAdd.setOnAction(e -> showAddDialog());
        btnAdd.setDisable(!canManage);

        Button btnEdit = new Button("✏️ Edit");
        btnEdit.getStyleClass().add("icon-button");
        btnEdit.setOnAction(e -> showEditDialog());
        btnEdit.setDisable(!canManage);

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("icon-button");
        btnRefresh.setOnAction(e -> refreshTable());

        actions.getChildren().addAll(btnAdd, btnEdit, btnRefresh);
        return actions;
    }

    private void setupTable() {
        table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Employee, String> colId = new TableColumn<>("Employee ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmployeeId()));
        colId.setPrefWidth(120);

        TableColumn<Employee, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()));
        colName.setPrefWidth(180);

        TableColumn<Employee, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colEmail.setPrefWidth(200);

        TableColumn<Employee, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPhone() != null ? data.getValue().getPhone() : "N/A"));
        colPhone.setPrefWidth(120);

        TableColumn<Employee, String> colDesignation = new TableColumn<>("Designation");
        colDesignation.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDesignation()));
        colDesignation.setPrefWidth(150);

        TableColumn<Employee, String> colSalary = new TableColumn<>("Monthly Salary");
        colSalary.setCellValueFactory(data -> {
            BigDecimal salary = data.getValue().getSalary();
            return new SimpleStringProperty(salary != null ? "₹" + String.format("%,.2f", salary) : "Not Set");
        });
        colSalary.setPrefWidth(130);
        colSalary.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Employee, String> colJoinDate = new TableColumn<>("Join Date");
        colJoinDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getJoinDate() != null ? data.getValue().getJoinDate().toString() : "N/A"));
        colJoinDate.setPrefWidth(110);

        TableColumn<Employee, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(col -> new TableCell<Employee, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setPadding(new Insets(4, 12, 4, 12));
                    badge.setStyle("-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
                    
                    switch (status) {
                        case "ACTIVE":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(16, 185, 129, 0.2); -fx-text-fill: #10b981;");
                            break;
                        case "INACTIVE":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(239, 68, 68, 0.2); -fx-text-fill: #ef4444;");
                            break;
                        case "ON_LEAVE":
                            badge.setStyle(badge.getStyle() + "-fx-background-color: rgba(251, 191, 36, 0.2); -fx-text-fill: #fbbf24;");
                            break;
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        table.getColumns().addAll(colId, colName, colEmail, colPhone, colDesignation, colSalary, colJoinDate, colStatus);
    }

    private void refreshTable() {
        allEmployees = FXCollections.observableArrayList(employeeDAO.getAllEmployees());
        filterEmployees();
        updateStats();
    }

    private void filterEmployees() {
        if (allEmployees == null) return;

        String searchText = searchField.getText().toLowerCase().trim();
        String statusValue = statusFilter.getValue();

        List<Employee> filtered = allEmployees.stream()
                .filter(emp -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            emp.getEmployeeId().toLowerCase().contains(searchText) ||
                            (emp.getFirstName() + " " + emp.getLastName()).toLowerCase().contains(searchText) ||
                            emp.getEmail().toLowerCase().contains(searchText) ||
                            emp.getDesignation().toLowerCase().contains(searchText);

                    boolean matchesStatus = statusValue.equals("All") ||
                            emp.getStatus().name().equals(statusValue);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }

    private void updateStats() {
        if (allEmployees == null) return;
        
        long activeCount = allEmployees.stream().filter(e -> e.getStatus() == Employee.Status.ACTIVE).count();
        long totalCount = allEmployees.size();
        
        statsLabel.setText(String.format("Total: %d | Active: %d", totalCount, activeCount));
    }

    private void showAddDialog() {
        Dialog<Employee> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Add New Employee");
        dialog.setHeaderText("➕ Enter Employee Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setMaxWidth(500);

        TextField tfId = new TextField();
        tfId.setPromptText("e.g., EMP001");
        TextField tfFirst = new TextField();
        tfFirst.setPromptText("First name");
        TextField tfLast = new TextField();
        tfLast.setPromptText("Last name");
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("email@example.com");
        TextField tfPhone = new TextField();
        tfPhone.setPromptText("+91-XXXXXXXXXX");
        TextField tfDesignation = new TextField();
        tfDesignation.setPromptText("e.g., Professor, Assistant");
        TextField tfSalary = new TextField();
        tfSalary.setPromptText("50000.00");
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
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Edit Employee");
        dialog.setHeaderText("✏️ Update Employee Details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setMaxWidth(500);

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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
