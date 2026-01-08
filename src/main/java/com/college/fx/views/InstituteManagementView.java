package com.college.fx.views;

import com.college.dao.DepartmentDAO;
import com.college.dao.PermissionDAO;
import com.college.dao.RoleDAO;
import com.college.dao.UserDAO; // Added
import com.college.dao.AuditLogDAO;
import com.college.utils.DialogUtils;
import com.college.models.Department;
import com.college.models.Permission;
import com.college.models.Role;
import com.college.models.User; // Added
import com.college.models.AuditLog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JavaFX Institute Management View
 * Handles Departments, Roles, Permissions, and Special Users
 */
public class InstituteManagementView {

    private VBox root;
    private DepartmentDAO departmentDAO;
    private RoleDAO roleDAO;
    private PermissionDAO permissionDAO;
    private UserDAO userDAO; // Added

    // Departments
    private TableView<Department> deptTable;
    private ObservableList<Department> deptData;

    // Roles
    private TableView<Role> roleTable;
    private ObservableList<Role> roleData;
    private ListView<Permission> permissionList;
    private ObservableList<Permission> permissionData;
    private Role selectedRole;

    // Users (Special)
    private TableView<User> userTable;
    private ObservableList<User> userData;

    // Audit Logs
    private TableView<AuditLog> auditTable;
    private ObservableList<AuditLog> auditData;

    private String userRole;
    private int userId;

    public InstituteManagementView(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.departmentDAO = new DepartmentDAO();
        this.roleDAO = new RoleDAO();
        this.permissionDAO = new PermissionDAO();
        this.userDAO = new UserDAO();
        this.deptData = FXCollections.observableArrayList();
        this.roleData = FXCollections.observableArrayList();
        this.permissionData = FXCollections.observableArrayList();
        this.userData = FXCollections.observableArrayList();
        this.auditData = FXCollections.observableArrayList();

        createView();
        loadDepartments();
        loadRoles();
        if ("ADMIN".equals(userRole)) {
            loadUsers();
        }
        loadAuditLogs();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Institute Management");
        title.getStyleClass().add("section-title");
        title.setPadding(new Insets(0, 0, 10, 10));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("pill-tab-pane");

        Tab deptTab = new Tab("Departments");
        deptTab.setContent(createDepartmentsSection());

        Tab roleTab = new Tab("Roles & Permissions");
        roleTab.setContent(createRolesSection());

        Tab auditTab = new Tab("Audit Logs");
        auditTab.setContent(createAuditSection());

        // Student & Faculty Tabs (Integrated)
        Tab studentTab = new Tab("Institute");
        StudentManagementView studentView = new StudentManagementView(userRole, userId);
        studentTab.setContent(studentView.getView());

        Tab facultyTab = new Tab("Faculty");
        FacultyManagementView facultyView = new FacultyManagementView(userRole, userId);
        facultyTab.setContent(facultyView.getView());

        tabPane.getTabs().addAll(studentTab, facultyTab, deptTab, roleTab);

        // Add Special Users tab only for Admins
        if ("ADMIN".equals(userRole)) {
            Tab usersTab = new Tab("Special Users");
            usersTab.setContent(createUsersTab());
            tabPane.getTabs().add(usersTab);
        }

        tabPane.getTabs().add(auditTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(title, tabPane);
    }

    // ==================== DEPARTMENTS TAB ====================

    private VBox createDepartmentsSection() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

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
        deptTable.getStyleClass().add("glass-table");
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
                editBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");

                editBtn.setOnAction(event -> showDepartmentDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> confirmDeleteDepartment(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        deptTable.getColumns().addAll(java.util.Arrays.asList(codeCol, nameCol, headCol, actionCol));
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
        DialogUtils.styleDialog(dialog);
        dialog.setTitle(dept == null ? "Add Department" : "Edit Department");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

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

        DialogUtils.addFormRow(grid, "Code:", codeField, 0);
        DialogUtils.addFormRow(grid, "Name:", nameField, 1);
        DialogUtils.addFormRow(grid, "Head:", headField, 2);
        DialogUtils.addFormRow(grid, "Description:", descArea, 3);

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
        DialogUtils.styleDialog(alert);
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

    private HBox createRolesSection() {
        HBox content = new HBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        // LEFT: Role List
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(400);

        HBox roleToolbar = new HBox(10);
        Button addRoleBtn = createButton("Add Role", "#22c55e");
        addRoleBtn.setOnAction(e -> showRoleDialog(null));

        Button editRoleBtn = createButton("Edit Role", "#3b82f6");
        editRoleBtn.setOnAction(e -> {
            Role selected = roleTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRoleDialog(selected);
            } else {
                showError("No Selection", "Please select a role to edit.");
            }
        });

        Button deleteRoleBtn = createButton("Delete Role", "#ef4444");
        deleteRoleBtn.setOnAction(e -> confirmDeleteRole());

        roleToolbar.getChildren().addAll(addRoleBtn, editRoleBtn, deleteRoleBtn);

        roleTable = new TableView<>();
        roleTable.getStyleClass().add("glass-table");
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

        roleTable.getColumns().addAll(java.util.Arrays.asList(rNameCol, rCodeCol, rSysCol));
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
        DialogUtils.styleDialog(dialog);
        dialog.setTitle(role == null ? "Add Role" : "Edit Role");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField codeField = new TextField();
        codeField.setPromptText("ROLE_CODE");
        TextField nameField = new TextField();
        nameField.setPromptText("Role Name");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");

        ComboBox<String> portalCombo = new ComboBox<>();
        portalCombo.getItems().addAll("ADMIN", "FACULTY", "STUDENT", "WARDEN", "FINANCE");
        portalCombo.setValue("STUDENT"); // Default
        portalCombo.setPromptText("Select Portal View");

        if (role != null) {
            codeField.setText(role.getCode());
            nameField.setText(role.getName());
            descArea.setText(role.getDescription());
            if (role.getPortalType() != null) {
                portalCombo.setValue(role.getPortalType());
            }
            if (role.isSystemRole()) {
                codeField.setDisable(true);
            }
        }

        DialogUtils.addFormRow(grid, "Code:", codeField, 0);
        DialogUtils.addFormRow(grid, "Name:", nameField, 1);
        DialogUtils.addFormRow(grid, "Description:", descArea, 2);
        DialogUtils.addFormRow(grid, "Portal Type:", portalCombo, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Role result = role != null ? role : new Role();
                result.setCode(codeField.getText().toUpperCase());
                result.setName(nameField.getText());
                result.setDescription(descArea.getText());
                result.setPortalType(portalCombo.getValue());
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
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Manage Permissions: " + selectedRole.getName());
        dialog.setHeaderText("Select permissions for this role");
        dialog.setResizable(true);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        List<Permission> allPerms = permissionDAO.getAllPermissions();

        // Root layout
        VBox rootLayout = new VBox(10);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setPrefSize(700, 600);

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search permissions...");
        rootLayout.getChildren().add(searchField);

        // Scroll Content
        VBox content = new VBox(15);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        rootLayout.getChildren().add(scroll);
        // Map to store category -> list of permission checkboxes
        Map<String, List<CheckBox>> categoryCheckboxes = new java.util.HashMap<>();
        // Map to store checkbox -> permission for filtering
        Map<CheckBox, String> checkboxSearchText = new java.util.HashMap<>();
        // Map to store category -> container VBox (for visibility control)
        Map<String, VBox> categoryContainers = new java.util.HashMap<>();
        // Map to store category -> GridPane (for re-layout during search)
        Map<String, GridPane> categoryGrids = new java.util.HashMap<>();

        // Group by category
        Map<String, List<Permission>> grouped = allPerms.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory() == null ? "General" : p.getCategory()));

        // Sort categories logic
        List<String> logicalOrder = java.util.Arrays.asList(
                "SYSTEM",
                "STUDENT",
                "ACADEMIC",
                "FACULTY",
                "HOSTEL",
                "HR",
                "FINANCE",
                "LIBRARY",
                "REPORT",
                "General");

        List<String> sortedCategories = new ArrayList<>(grouped.keySet());
        sortedCategories.sort((c1, c2) -> {
            int i1 = logicalOrder.indexOf(c1);
            int i2 = logicalOrder.indexOf(c2);

            if (i1 != -1 && i2 != -1)
                return Integer.compare(i1, i2);
            if (i1 != -1)
                return -1;
            if (i2 != -1)
                return 1;

            return c1.compareTo(c2);
        });

        for (String category : sortedCategories) {
            List<Permission> perms = grouped.get(category);

            // Category Container
            VBox catContainer = new VBox(5);
            catContainer.setStyle(
                    "-fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: rgba(255, 255, 255, 0.05);");

            // Store reference
            categoryContainers.put(category, catContainer);

            // Header: Select All + Label
            HBox header = new HBox(10);
            header.setAlignment(Pos.CENTER_LEFT);
            header.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-padding: 8; -fx-background-radius: 4;");

            CheckBox selectAllCat = new CheckBox();
            Label catLabel = new Label(category);
            catLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            header.getChildren().addAll(selectAllCat, catLabel);

            // Grid for Permissions
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(8);

            // Store grid reference
            categoryGrids.put(category, grid);

            List<CheckBox> boxes = new ArrayList<>();
            int col = 0;
            int row = 0;

            for (Permission p : perms) {
                CheckBox cb = new CheckBox(p.getName());
                // Use default tooltip if desc is null
                Tooltip tp = new Tooltip(p.getDescription() != null ? p.getDescription() : p.getName());
                cb.setTooltip(tp);
                cb.setUserData(p.getId());

                if (selectedRole.hasPermission(p.getCode())) {
                    cb.setSelected(true);
                }

                checkboxSearchText.put(cb, (p.getName() + " " + p.getCode() + " " + category).toLowerCase());
                boxes.add(cb);

                grid.add(cb, col, row);
                col++;
                if (col > 1) { // 2 Columns
                    col = 0;
                    row++;
                }
            }
            categoryCheckboxes.put(category, boxes);

            // Select All Logic
            selectAllCat.setOnAction(e -> {
                boolean selected = selectAllCat.isSelected();
                boxes.forEach(cb -> {
                    if (cb.isVisible())
                        cb.setSelected(selected);
                });
            });

            // Update "Select All" state if specific boxes clicked
            for (CheckBox cb : boxes) {
                cb.selectedProperty().addListener((obs, oldV, newV) -> {
                    boolean allSelected = boxes.stream().filter(javafx.scene.Node::isVisible)
                            .allMatch(CheckBox::isSelected);
                    boolean anySelected = boxes.stream().filter(javafx.scene.Node::isVisible)
                            .anyMatch(CheckBox::isSelected);
                    selectAllCat.setSelected(allSelected);
                    if (!allSelected && anySelected) {
                        selectAllCat.setIndeterminate(true);
                    } else {
                        selectAllCat.setIndeterminate(false);
                    }
                });
            }

            // Initial check for select all state
            boolean allInit = boxes.stream().allMatch(CheckBox::isSelected);
            if (allInit && !boxes.isEmpty())
                selectAllCat.setSelected(true);

            catContainer.getChildren().addAll(header, grid);
            content.getChildren().add(catContainer);
        }

        // Search Logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String term = newVal == null ? "" : newVal.toLowerCase();

            for (String category : sortedCategories) {
                List<CheckBox> boxes = categoryCheckboxes.get(category);
                GridPane grid = categoryGrids.get(category);
                VBox catContainer = categoryContainers.get(category);

                if (boxes == null || grid == null || catContainer == null) {
                    continue;
                }

                boolean anyVisible = false;
                grid.getChildren().clear(); // Clear grid to re-layout visible items

                int c = 0;
                int r = 0;

                for (CheckBox cb : boxes) {
                    String searchText = checkboxSearchText.get(cb);
                    if (term.isEmpty() || (searchText != null && searchText.contains(term))) {
                        cb.setVisible(true);
                        cb.setManaged(true);
                        anyVisible = true;

                        grid.add(cb, c, r);
                        c++;
                        if (c > 1) {
                            c = 0;
                            r++;
                        }
                    } else {
                        cb.setVisible(false);
                        cb.setManaged(false);
                    }
                }

                catContainer.setVisible(anyVisible);
                catContainer.setManaged(anyVisible);
            }
        });

        dialog.getDialogPane().setContent(rootLayout);

        dialog.setResultConverter(btnType -> {
            if (btnType == saveBtn) {
                List<Integer> ids = new ArrayList<>();
                categoryCheckboxes.values().forEach(list -> list.stream().filter(CheckBox::isSelected)
                        .forEach(cb -> ids.add((Integer) cb.getUserData())));
                return ids;
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

    private void confirmDeleteRole() {
        Role selected = roleTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("No Selection", "Please select a role to delete.");
            return;
        }

        if (selected.isSystemRole()) {
            showError("Cannot Delete", "System roles cannot be deleted.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        DialogUtils.styleDialog(confirm);
        confirm.setTitle("Delete Role");
        confirm.setHeaderText("Delete role: " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone. Users with this role may lose access.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (roleDAO.deleteRole(selected.getId())) {
                loadRoles();
                showInfo("Success", "Role deleted successfully.");
            } else {
                showError("Error", "Failed to delete role. It may be assigned to users.");
            }
        }
    }

    // ==================== AUDIT LOG TAB ====================

    private VBox createAuditSection() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        HBox toolbar = new HBox(15);
        Button refreshBtn = createButton("Refresh Logs", "#3b82f6");
        refreshBtn.setOnAction(e -> loadAuditLogs());
        toolbar.getChildren().add(refreshBtn);

        auditTable = new TableView<>();
        auditTable.getStyleClass().add("glass-table");
        auditTable.setItems(auditData);

        TableColumn<AuditLog, String> timeCol = new TableColumn<>("Timestamp");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTimestamp() != null ? data.getValue().getTimestamp().toString().replace("T", " ")
                        : ""));
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

        auditTable.getColumns().addAll(java.util.Arrays.asList(timeCol, userCol, actionCol, detailsCol));
        VBox.setVgrow(auditTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, auditTable);
        return content;
    }

    private void loadAuditLogs() {
        auditData.clear();
        auditData.addAll(AuditLogDAO.getAllLogs());
    }

    // ==================== SPECIAL USERS TAB ====================

    private VBox createUsersTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        HBox toolbar = new HBox(15);

        Button addUserBtn = createButton("Create Special User", "#8b5cf6");
        addUserBtn.setOnAction(e -> showAddUserDialog());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadUsers());

        toolbar.getChildren().addAll(addUserBtn, refreshBtn);

        userTable = new TableView<>();
        userTable.getStyleClass().add("glass-table");
        userTable.setItems(userData);

        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoleName()));
        roleCol.setPrefWidth(150);

        TableColumn<User, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-font-size: 11px;");
                deleteBtn.setOnAction(event -> deleteUser(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    // Prevent deleting self or certain critical users if needed
                    if (user.getId() == userId) {
                        setGraphic(null);
                    } else {
                        setGraphic(deleteBtn);
                    }
                }
            }
        });

        userTable.getColumns().addAll(java.util.Arrays.asList(idCol, nameCol, roleCol, actionCol));
        VBox.setVgrow(userTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, userTable);
        return content;
    }

    private void loadUsers() {
        userData.clear();
        userData.addAll(userDAO.getSpecialUsers());
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create Special User");
        dialog.setHeaderText("Create a new system user");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setPromptText("Select Role");

        // Filter roles: Show only ADMIN, FINANCE, EXAM_COORD
        List<Role> allRoles = roleDAO.getAllRoles();
        List<String> allowedRoles = List.of("ADMIN", "FINANCE", "EXAM_COORD");
        List<Role> specialRoles = allRoles.stream()
                .filter(r -> allowedRoles.contains(r.getCode().toUpperCase()))
                .collect(Collectors.toList());
        roleCombo.getItems().addAll(specialRoles);

        // Custom cell factory to show role name
        roleCombo.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        roleCombo.setButtonCell(new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        DialogUtils.addFormRow(grid, "Username:", usernameField, 0);
        DialogUtils.addFormRow(grid, "Password:", passwordField, 1);
        DialogUtils.addFormRow(grid, "Role:", roleCombo, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createBtn) {
                if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()
                        || roleCombo.getValue() == null) {
                    return null; // Validate
                }

                String username = usernameField.getText();
                if (userDAO.isUsernameTaken(username)) {
                    showError("Error", "Username already taken");
                    return null;
                }

                String password = passwordField.getText();
                Role role = roleCombo.getValue();

                // Create user
                // Note: addUser takes string role (legacy). We pass the role name for now,
                // but we also need to set the role_id for RBAC.
                // Since UserDAO.addUser doesn't take roleId in the method I saw,
                // I might need to update UserDAO or do a two-step process: Create User ->
                // Assign Role ID.
                // Let's check UserDAO again. It has assignRoleToUser(int userId, int roleId) in
                // RoleDAO.

                int newUserId = userDAO.addUser(username, password, role.getCode());
                if (newUserId != -1) {
                    roleDAO.assignRoleToUser(newUserId, role.getId());
                    return new User(newUserId, username, role.getName());
                } else {
                    showError("Error", "Failed to create user");
                }
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();
        result.ifPresent(u -> {
            loadUsers();
            showInfo("Success", "User created successfully.");
        });
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete " + user.getUsername() + "?");
        alert.setContentText("Are you sure? This cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (userDAO.deleteUser(user.getId())) {
                loadUsers();
            } else {
                showError("Error", "Failed to delete user.");
            }
        }
    }

    // ==================== HELPERS ====================

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");
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
