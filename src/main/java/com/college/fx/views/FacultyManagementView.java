package com.college.fx.views;

import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
import com.college.utils.SessionManager;
import com.college.utils.EnrollmentGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import com.college.dao.RoleDAO;
import com.college.models.Role;
import com.college.dao.UserDAO;
import com.college.dao.DepartmentDAO;
import com.college.models.Department;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;
import java.util.List;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * JavaFX Faculty Management View
 */
public class FacultyManagementView {

    private VBox root;
    private TableView<Faculty> tableView;
    private ObservableList<Faculty> facultyData;
    private FacultyDAO facultyDAO;
    private UserDAO userDAO;
    private RoleDAO roleDAO;
    private TextField searchField;

    public FacultyManagementView(String role, int userId) {
        this.facultyDAO = new FacultyDAO();
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
        this.facultyData = FXCollections.observableArrayList();
        createView();
        loadFaculty();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;");

        Label title = new Label("Faculty Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search faculty...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");

        Button searchBtn = createButton("Search", "#14b8a6");
        searchBtn.setOnAction(e -> searchFaculty());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadFaculty());

        header.getChildren().addAll(title, spacer, searchField, searchBtn, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox();
        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(facultyData);

        // Columns - Faculty ID should be first and prominent
        TableColumn<Faculty, String> facultyIdCol = new TableColumn<>("Faculty ID");
        facultyIdCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUsername() != null ? data.getValue().getUsername() : "N/A"));
        facultyIdCol.setPrefWidth(110);
        facultyIdCol.setStyle("-fx-font-weight: bold;");

        TableColumn<Faculty, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(180);

        TableColumn<Faculty, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(200);

        TableColumn<Faculty, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        phoneCol.setPrefWidth(120);

        TableColumn<Faculty, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        deptCol.setPrefWidth(120);

        TableColumn<Faculty, String> qualificationCol = new TableColumn<>("Qualification");
        qualificationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQualification()));
        qualificationCol.setPrefWidth(130);

        TableColumn<Faculty, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRoleName() != null ? data.getValue().getRoleName() : "N/A"));
        roleCol.setPrefWidth(100);

        tableView.getColumns().addAll(facultyIdCol, nameCol, emailCol, phoneCol, deptCol, qualificationCol, roleCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_FACULTY")) {
            Button addBtn = createButton("Add Faculty", "#22c55e");
            addBtn.setOnAction(e -> showAddFacultyDialog());

            Button editBtn = createButton("Edit Faculty", "#3b82f6");
            editBtn.setOnAction(e -> editFaculty());

            Button deleteBtn = createButton("Delete Faculty", "#ef4444");
            deleteBtn.setOnAction(e -> deleteFaculty());

            Button roleBtn = createButton("Assign Role", "#8b5cf6"); // Violet color
            roleBtn.setOnAction(e -> assignRole());

            section.getChildren().addAll(addBtn, editBtn, deleteBtn, roleBtn);
        }

        Button exportBtn = createButton("Export", "#64748b");
        exportBtn.setOnAction(e -> exportData());
        section.getChildren().add(exportBtn);

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
        btn.setPrefHeight(40);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        return btn;
    }

    private void loadFaculty() {
        facultyData.clear();
        List<Faculty> faculty = facultyDAO.getAllFaculty();
        facultyData.addAll(faculty);
    }

    private void searchFaculty() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadFaculty();
            return;
        }
        facultyData.clear();
        List<Faculty> faculty = facultyDAO.getAllFaculty();
        for (Faculty f : faculty) {
            if (f.getName().toLowerCase().contains(keyword) ||
                    (f.getEmail() != null && f.getEmail().toLowerCase().contains(keyword))) {
                facultyData.add(f);
            }
        }
    }

    private void editFaculty() {
        Faculty selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a faculty member to edit.");
            return;
        }

        Dialog<Faculty> dialog = new Dialog<>();
        dialog.setTitle("Edit Faculty");
        dialog.setHeaderText("Edit: " + selected.getName());
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());
        TextField deptField = new TextField(selected.getDepartment());
        TextField qualField = new TextField(selected.getQualification());

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Department:"), 0, 3);
        grid.add(deptField, 1, 3);
        grid.add(new Label("Qualification:"), 0, 4);
        grid.add(qualField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                selected.setPhone(phoneField.getText());
                selected.setDepartment(deptField.getText());
                selected.setQualification(qualField.getText());

                if (facultyDAO.updateFaculty(selected)) {
                    return selected;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            loadFaculty();
            showAlert("Success", "Faculty updated successfully!");
        });
    }

    private void assignRole() {
        Faculty selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a faculty member to assign a role.");
            return;
        }

        Dialog<Role> dialog = new Dialog<>();
        dialog.setTitle("Assign Role");
        dialog.setHeaderText("Assign Role to: " + selected.getName());
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<Role> roleCombo = new ComboBox<>();
        roleCombo.setPrefWidth(250);
        List<Role> roles = roleDAO.getAllRoles();
        roleCombo.getItems().addAll(roles);

        roleCombo.setConverter(new StringConverter<Role>() {
            @Override
            public String toString(Role r) {
                return r != null ? r.getName() : "";
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        // Try to select existing role if possible (requires fetching user's current
        // role via RoleDAO)
        // For simplicity, select first or nothing.
        if (!roles.isEmpty())
            roleCombo.getSelectionModel().selectFirst();

        content.getChildren().addAll(new Label("Select Role (RBAC):"), roleCombo);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return roleCombo.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(role -> {
            // Update RBAC role (role_id)
            boolean rbacSuccess = roleDAO.assignRoleToUser(selected.getUserId(), role.getId());
            // Update Legacy role (string) for fallback/display
            boolean legacySuccess = userDAO.updateUserRole(selected.getUserId(), role.getName());

            if (rbacSuccess || legacySuccess) { // At least one succeeded
                showAlert("Success", "Role assigned successfully (RBAC + Legacy)!");
                loadFaculty();
            } else {
                showAlert("Error", "Failed to update role.");
            }
        });
    }

    private void deleteFaculty() {
        Faculty selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a faculty member to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Faculty");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete faculty: " + selected.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (facultyDAO.deleteFaculty(selected.getId())) {
                    loadFaculty();
                    showAlert("Success", "Faculty deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete faculty.");
                }
            }
        });
    }

    private void showAddFacultyDialog() {
        Dialog<Faculty> dialog = new Dialog<>();
        dialog.setTitle("Add Faculty");
        dialog.setHeaderText("Create New Faculty Profile");

        ButtonType saveButtonType = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        ComboBox<String> deptCombo = new ComboBox<>();
        try {
            DepartmentDAO deptDAO = new DepartmentDAO();
            deptCombo.getItems()
                    .addAll(deptDAO.getAllDepartments().stream().map(Department::getName).collect(Collectors.toList()));
            if (!deptCombo.getItems().isEmpty())
                deptCombo.getSelectionModel().select(0);
        } catch (Exception e) {
            deptCombo.getItems().addAll("CS", "IT", "EC", "ME", "Civil", "Physics", "Chemistry", "Maths");
        }

        TextField qualField = new TextField();
        qualField.setPromptText("Qualification (e.g. PhD)");

        DatePicker joinDate = new DatePicker(LocalDate.now());

        // User Account
        Separator sep = new Separator();
        Label userLabel = new Label("User Account Credentials");
        userLabel.setStyle("-fx-font-weight: bold");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);

        grid.add(new Label("Department:"), 0, 3);
        grid.add(deptCombo, 1, 3);
        grid.add(new Label("Qualification:"), 0, 4);
        grid.add(qualField, 1, 4);
        grid.add(new Label("Join Date:"), 0, 5);
        grid.add(joinDate, 1, 5);

        grid.add(sep, 0, 6, 2, 1);
        grid.add(userLabel, 0, 7, 2, 1);

        Label autoGenLabel = new Label("Faculty ID will be auto-generated (FAC###) and used as username");
        autoGenLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-style: italic;");
        grid.add(autoGenLabel, 0, 8, 2, 1);

        grid.add(new Label("Password:"), 0, 9);
        grid.add(passwordField, 1, 9);

        Label passHint = new Label("(Leave empty for default: 123)");
        passHint.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        grid.add(passHint, 1, 10);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        nameField.textProperty().addListener((o, old, newValue) -> saveButton
                .setDisable(newValue.trim().isEmpty() || emailField.getText().trim().isEmpty()));
        emailField.textProperty().addListener((o, old, newValue) -> saveButton
                .setDisable(newValue.trim().isEmpty() || nameField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validate required fields
                    if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                        showAlert("Validation Error", "Name and Email are required");
                        return null;
                    }

                    // Validate email format
                    String email = emailField.getText().trim();
                    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                        showAlert("Validation Error", "Please enter a valid email address");
                        return null;
                    }

                    // Validate phone number if provided
                    String phone = phoneField.getText().trim();
                    if (!phone.isEmpty() && !phone.matches("^[0-9]{10}$")) {
                        showAlert("Validation Error", "Phone number must be exactly 10 digits");
                        return null;
                    }

                    // Auto-generate faculty ID
                    String facultyId = EnrollmentGenerator.generateFacultyId();
                    String password = passwordField.getText().trim().isEmpty() ? "123" : passwordField.getText();

                    // Create user account with faculty ID as username
                    UserDAO userDAO = new UserDAO();
                    int newUserId = userDAO.addUser(facultyId, password, "FACULTY");

                    if (newUserId != -1) {
                        Faculty f = new Faculty();
                        f.setName(nameField.getText());
                        f.setEmail(emailField.getText());
                        f.setPhone(phoneField.getText());
                        f.setDepartment(deptCombo.getValue());
                        f.setQualification(qualField.getText());
                        f.setJoinDate(Date.from(joinDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        f.setUserId(newUserId);

                        facultyDAO.addFaculty(f, newUserId);

                        // Show success with credentials
                        Platform.runLater(() -> {
                            showAlert("Success", "Faculty added successfully!\n\n" +
                                    "Faculty ID: " + facultyId + "\n" +
                                    "Username: " + facultyId + "\n" +
                                    "Password: " + password);
                        });

                        return f;
                    } else {
                        showAlert("Error", "Failed to create user account.");
                    }
                } catch (Exception e) {
                    // Handle duplicate constraints and other errors
                    if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                        if (e.getMessage().contains("email")) {
                            showAlert("Error", "This email address is already registered!");
                        } else if (e.getMessage().contains("phone")) {
                            showAlert("Error", "This phone number is already registered!");
                        } else {
                            showAlert("Error", "Duplicate entry detected.");
                        }
                    } else {
                        showAlert("Error", "Failed to add faculty: " + e.getMessage());
                    }
                }
            }
            return null;
        });

        Optional<Faculty> result = dialog.showAndWait();
        result.ifPresent(f -> {
            if (f != null) {
                loadFaculty();
                showAlert("Success", "Faculty added successfully!");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportData() {
        if (tableView.getItems().isEmpty()) {
            showAlert("Export", "No data to export.");
            return;
        }
        com.college.utils.FxTableExporter.exportWithDialog(tableView, root.getScene().getWindow());
    }

    public VBox getView() {
        return root;
    }
}
