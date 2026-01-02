package com.college.fx.views;

import com.college.dao.FacultyDAO;
import com.college.models.Faculty;
import com.college.utils.SessionManager;
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

import java.util.List;

/**
 * JavaFX Faculty Management View
 */
public class FacultyManagementView {

    private VBox root;
    private TableView<Faculty> tableView;
    private ObservableList<Faculty> facultyData;
    private FacultyDAO facultyDAO;
    private TextField searchField;

    public FacultyManagementView(String role, int userId) {
        this.facultyDAO = new FacultyDAO();
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
            "-fx-border-radius: 12;"
        );

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
            "-fx-border-radius: 12;"
        );
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(facultyData);

        TableColumn<Faculty, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

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
        deptCol.setPrefWidth(150);

        TableColumn<Faculty, String> qualificationCol = new TableColumn<>("Qualification");
        qualificationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQualification()));
        qualificationCol.setPrefWidth(150);

        tableView.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, deptCol, qualificationCol);
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
            addBtn.setOnAction(e -> showAlert("Add Faculty", "Add faculty dialog would open here."));

            Button editBtn = createButton("Edit Faculty", "#3b82f6");
            editBtn.setOnAction(e -> editFaculty());

            Button deleteBtn = createButton("Delete Faculty", "#ef4444");
            deleteBtn.setOnAction(e -> deleteFaculty());

            section.getChildren().addAll(addBtn, editBtn, deleteBtn);
        }

        Button exportBtn = createButton("Export", "#64748b");
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
            "-fx-cursor: hand;"
        );
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
        showAlert("Edit Faculty", "Edit dialog for: " + selected.getName());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
