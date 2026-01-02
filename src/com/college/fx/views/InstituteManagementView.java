package com.college.fx.views;

import com.college.dao.DepartmentDAO;
import com.college.dao.PermissionDAO;
import com.college.dao.RoleDAO;
import com.college.dao.AuditLogDAO;
import com.college.models.Department;
import com.college.models.Permission;
import com.college.models.Role;
import com.college.models.AuditLog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JavaFX Institute Management View
 * Handles Departments, Roles, and Permissions
 */
public class InstituteManagementView {

    private VBox root;
    private DepartmentDAO departmentDAO;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    
    // Departments
    private TableView<Department> deptTable;
    private ObservableList<Department> deptData;

    // Roles
    private TableView<Role> roleTable;
    private ObservableList<Role> roleData;
    private ListView<Permission> permissionList;
    private ObservableList<Permission> permissionData;
    private Role selectedRole;

    // Audit Logs
    private TableView<AuditLog> auditTable;
    private ObservableList<AuditLog> auditData;

    public InstituteManagementView(String role, int userId) {
        this.departmentDAO = new DepartmentDAO();
        this.roleDAO = new RoleDAO();
        this.permissionDAO = new PermissionDAO();
        this.deptData = FXCollections.observableArrayList();
        this.roleData = FXCollections.observableArrayList();
        this.permissionData = FXCollections.observableArrayList();
        this.auditData = FXCollections.observableArrayList();
        
        createView();
        loadDepartments();
        loadRoles();
        // Audit logs loaded when tab selected usually, but for now:
        loadAuditLogs();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Institute Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));
        title.setPadding(new Insets(0, 0, 10, 10));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab deptTab = new Tab("Departments");
        deptTab.setContent(createDepartmentTab());

        Tab roleTab = new Tab("Roles & Permissions");
        roleTab.setContent(createRoleTab());

        Tab auditTab = new Tab("Audit Logs");
        auditTab.setContent(createAuditTab());

        tabPane.getTabs().addAll(deptTab, roleTab, auditTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(title, tabPane);
    }

    // ==================== DEPARTMENTS TAB ====================

    private VBox createDepartmentTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12;");

        // Toolbar
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = createButton("Add Department", "#22c55e");
        addBtn.setOnAction(e -> showDepartmentDialog(null));

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadDepartments());

        toolbar.getChildren().addAll(addBtn, refreshBtn);

        // Table
        deptTable = new TableView<>();
        deptTable.setItems(deptData);

        TableColumn<Department, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        codeCol.setPrefWidth(100);

        TableColumn<Department, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Department, String> headCol = new TableColumn<>("Head of Dept");
        headCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHeadOfDepartment()));
        headCol.setPrefWidth(150);

        TableColumn<Department, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(180);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");

                editBtn.setOnAction(event -> showDepartmentDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> confirmDeleteDepartment(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        deptTable.getColumns().addAll(codeCol, nameCol, headCol, actionCol);
        VBox.setVgrow(deptTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, deptTable);
        return content;
    }

    private void loadDepartments() {
        deptData.clear();
        deptData.addAll(departmentDAO.getAllDepartments());
    }

    private void showDepartmentDialog(Department dept) {
        Dialog<Department> dialog = new Dialog<>();
        dialog.setTitle(dept == null ? "Add Department" : "Edit Department");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("DEPT_CODE");
        TextField nameField = new TextField();
        nameField.setPromptText("Department Name");
        TextField headField = new TextField();
        headField.setPromptText("Head of Department");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);

        if (dept != null) {
            codeField.setText(dept.getCode());
            nameField.setText(dept.getName());
            headField.setText(dept.getHeadOfDepartment());
            descArea.setText(dept.getDescription());
        }

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Head:"), 0, 2);
        grid.add(headField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Department result = dept != null ? dept : new Department();
                result.setCode(codeField.getText());
                result.setName(nameField.getText());
                result.setHeadOfDepartment(headField.getText());
                result.setDescription(descArea.getText());
                return result;
            }
            return null;
        });

        Optional<Department> result = dialog.showAndWait();
        result.ifPresent(newDept -> {
            boolean success;
            if (dept == null) {
                success = departmentDAO.addDepartment(newDept);
            } else {
                success = departmentDAO.updateDepartment(newDept);
            }

            if (success) {
                loadDepartments();
                showInfo("Success", "Department saved successfully.");
            } else {
                showError("Error", "Failed to save department.");
            }
        });
    }

    private void confirmDeleteDepartment(Department dept) {
        if (departmentDAO.hasCourses(dept.getId())) {
            showError("Cannot Delete", "This department has assigned courses. Remove associated courses first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Department");
        alert.setHeaderText("Delete " + dept.getName() + "?");
        alert.setContentText("Are you sure you want to delete this department? This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (departmentDAO.deleteDepartment(dept.getId())) {
                loadDepartments();
                showInfo("Deleted", "Department deleted successfully.");
            } else {
                showError("Error", "Failed to delete department.");
            }
        }
    }


    // ==================== ROLES TAB ====================

    private HBox createRoleTab() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12;");

        // LEFT: Role List
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(400);

        HBox roleToolbar = new HBox(10);
        Button addRoleBtn = createButton("Add Role", "#22c55e");
        addRoleBtn.setOnAction(e -> showRoleDialog(null));
        roleToolbar.getChildren().add(addRoleBtn);

        roleTable = new TableView<>();
        roleTable.setItems(roleData);

        TableColumn<Role, String> rNameCol = new TableColumn<>("Role Name");
        rNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        rNameCol.setPrefWidth(150);

        TableColumn<Role, String> rCodeCol = new TableColumn<>("Code");
        rCodeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCode()));
        rCodeCol.setPrefWidth(100);
        
        TableColumn<Role, String> rSysCol = new TableColumn<>("System");
        rSysCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isSystemRole() ? "Yes" : "No"));
        rSysCol.setPrefWidth(80);

        roleTable.getColumns().addAll(rNameCol, rCodeCol, rSysCol);
        VBox.setVgrow(roleTable, Priority.ALWAYS);

        roleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedRole = newVal;
            loadPermissionsForSelectedRole();
        });

        leftPane.getChildren().addAll(roleToolbar, roleTable);

        // RIGHT: Permissions
        VBox rightPane = new VBox(10);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        Label permTitle = new Label("Permissions for Selected Role");
        permTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        permissionList = new ListView<>();
        permissionList.setItems(permissionData);
        permissionList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Permission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " [" + item.getCategory() + "]");
                }
            }
        });
        VBox.setVgrow(permissionList, Priority.ALWAYS);

        Button managePermBtn = createButton("Manage Permissions", "#8b5cf6");
        managePermBtn.setOnAction(e -> showPermissionManager());

        rightPane.getChildren().addAll(permTitle, permissionList, managePermBtn);

        content.getChildren().addAll(leftPane, new Separator(javafx.geometry.Orientation.VERTICAL), rightPane);
        return content;
    }

    private void loadRoles() {
        roleData.clear();
        roleData.addAll(roleDAO.getAllRoles());
    }

    private void loadPermissionsForSelectedRole() {
        permissionData.clear();
        if (selectedRole != null) {
            // Need to fetch full role object with permissions if not already loaded
            // But assume role object from getAllRoles() might not have deep permissions
            // So let's re-fetch to be safe, or check mapping
            Role fullRole = roleDAO.getRoleById(selectedRole.getId());
            if (fullRole != null) {
                permissionData.addAll(fullRole.getPermissions());
            }
        }
    }

    private void showRoleDialog(Role role) {
        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle(role == null ? "Add Role" : "Edit Role");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField codeField = new TextField();
        codeField.setPromptText("ROLE_CODE");
        TextField nameField = new TextField();
        nameField.setPromptText("Role Name");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");

        if (role != null) {
            codeField.setText(role.getCode());
            nameField.setText(role.getName());
            descArea.setText(role.getDescription());
            if (role.isSystemRole()) {
                codeField.setDisable(true);
            }
        }

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Role result = role != null ? role : new Role();
                result.setCode(codeField.getText().toUpperCase());
                result.setName(nameField.getText());
                result.setDescription(descArea.getText());
                return result;
            }
            return null;
        });

        Optional<Role> result = dialog.showAndWait();
        result.ifPresent(newRole -> {
            if (role != null && role.isSystemRole()) {
                showError("Error", "Cannot edit system roles completely.");
                // Implementing basic edit support if needed or blocking it
                return; 
            }
            
            boolean success;
            if (role == null) {
                success = roleDAO.createRole(newRole);
            } else {
                success = roleDAO.updateRole(newRole);
            }

            if (success) {
                loadRoles();
                showInfo("Success", "Role saved successfully.");
            } else {
                showError("Error", "Failed to save role.");
            }
        });
    }

    private void showPermissionManager() {
        if (selectedRole == null) {
            showError("No Selection", "Please select a role first.");
            return;
        }

        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Manage Permissions: " + selectedRole.getName());
        dialog.setHeaderText("Select permissions for this role");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        List<Permission> allPerms = permissionDAO.getAllPermissions();
        VBox content = new VBox(10);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setPrefHeight(400);
        scroll.setFitToWidth(true);

        List<CheckBox> checkBoxes = new ArrayList<>();
        
        // Group by category
        Map<String, List<Permission>> grouped = allPerms.stream()
            .collect(Collectors.groupingBy(Permission::getCategory));

        grouped.forEach((category, perms) -> {
            Label catLabel = new Label(category);
            catLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            catLabel.setStyle("-fx-background-color: #e2e8f0; -fx-padding: 5;");
            catLabel.setMaxWidth(Double.MAX_VALUE);
            content.getChildren().add(catLabel);

            for (Permission p : perms) {
                CheckBox cb = new CheckBox(p.getName() + " (" + p.getCode() + ")");
                // Check if role has this permission
                if (selectedRole.hasPermission(p.getCode())) {
                    cb.setSelected(true);
                }
                cb.setUserData(p.getId());
                checkBoxes.add(cb);
                content.getChildren().add(cb);
            }
        });

        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(Button -> {
            if (Button == saveBtn) {
                return checkBoxes.stream()
                        .filter(CheckBox::isSelected)
                        .map(cb -> (Integer) cb.getUserData())
                        .collect(Collectors.toList());
            }
            return null;
        });

        Optional<List<Integer>> result = dialog.showAndWait();
        result.ifPresent(ids -> {
            if (roleDAO.setRolePermissions(selectedRole.getId(), ids)) {
                loadPermissionsForSelectedRole();
                showInfo("Success", "Permissions updated successfully.");
            } else {
                showError("Error", "Failed to update permissions.");
            }
        });
    }



    // ==================== AUDIT LOG TAB ====================

    private VBox createAuditTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12;");

        HBox toolbar = new HBox(15);
        Button refreshBtn = createButton("Refresh Logs", "#3b82f6");
        refreshBtn.setOnAction(e -> loadAuditLogs());
        toolbar.getChildren().add(refreshBtn);

        auditTable = new TableView<>();
        auditTable.setItems(auditData);

        TableColumn<AuditLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getTimestamp() != null ? data.getValue().getTimestamp().toString().replace("T", " ") : ""
        ));
        timeCol.setPrefWidth(160);

        TableColumn<AuditLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        userCol.setPrefWidth(120);

        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));
        actionCol.setPrefWidth(150);

        TableColumn<AuditLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDetails()));
        detailsCol.setPrefWidth(300);

        auditTable.getColumns().addAll(timeCol, userCol, actionCol, detailsCol);
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, auditTable);
        return content;
    }

    private void loadAuditLogs() {
        auditData.clear();
        auditData.addAll(AuditLogDAO.getAllLogs());
    }

    // ==================== HELPERS ====================

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
