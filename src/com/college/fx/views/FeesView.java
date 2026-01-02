package com.college.fx.views;

import com.college.dao.EnhancedFeeDAO;
import com.college.dao.StudentDAO;
import com.college.models.StudentFee;
import com.college.models.Student;
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
 * JavaFX Fees Management View
 */
public class FeesView {

    private VBox root;
    private TableView<StudentFee> tableView;
    private ObservableList<StudentFee> feeData;
    private ObservableList<StudentFee> allFeeData;
    private EnhancedFeeDAO feeDAO;
    private StudentDAO studentDAO;
    private TextField searchField;
    private String role;
    private int userId;

    public FeesView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();
        this.studentDAO = new StudentDAO();
        this.feeData = FXCollections.observableArrayList();
        this.allFeeData = FXCollections.observableArrayList();
        createView();
        loadFees();
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

        Label title = new Label(role.equals("STUDENT") ? "My Fees" : "Fee Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Only show search for non-students (admin/faculty)
        if (!role.equals("STUDENT")) {
            searchField = new TextField();
            searchField.setPromptText("Search by Student ID...");
            searchField.setPrefWidth(200);
            searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");
            searchField.textProperty().addListener((obs, old, newVal) -> filterFees(newVal));
            header.getChildren().addAll(title, spacer, searchField);
        } else {
            header.getChildren().addAll(title, spacer);
        }

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadFees());
        header.getChildren().add(refreshBtn);
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
        tableView.setItems(feeData);

        TableColumn<StudentFee, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<StudentFee, String> categoryCol = new TableColumn<>("Category");
        // Assuming category name is available or we display category ID for now
        categoryCol
                .setCellValueFactory(data -> new SimpleStringProperty("Category " + data.getValue().getCategoryId()));
        categoryCol.setPrefWidth(120);

        TableColumn<StudentFee, String> amountCol = new TableColumn<>("Total Amount");
        amountCol.setCellValueFactory(data -> new SimpleStringProperty("₹" + data.getValue().getTotalAmount()));
        amountCol.setPrefWidth(120);

        TableColumn<StudentFee, String> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(data -> new SimpleStringProperty("₹" + data.getValue().getPaidAmount()));
        paidCol.setPrefWidth(120);

        TableColumn<StudentFee, String> dueCol = new TableColumn<>("Due");
        dueCol.setCellValueFactory(data -> new SimpleStringProperty(
                "₹" + (data.getValue().getTotalAmount() - data.getValue().getPaidAmount())));
        dueCol.setPrefWidth(120);

        TableColumn<StudentFee, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<StudentFee, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("PAID".equalsIgnoreCase(status)) {
                        setTextFill(Color.web("#22c55e"));
                    } else if ("PARTIAL".equalsIgnoreCase(status)) {
                        setTextFill(Color.web("#f59e0b"));
                    } else {
                        setTextFill(Color.web("#ef4444"));
                    }
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                }
            }
        });

        tableView.getColumns().addAll(idCol, categoryCol, amountCol, paidCol, dueCol, statusCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_FEES")) {
            Button addBtn = createButton("Add Fee Record", "#22c55e");
            addBtn.setOnAction(e -> showAlert("Add Fee Record", "Add fee dialog would open here."));

            Button payBtn = createButton("Record Payment", "#3b82f6");
            payBtn.setOnAction(e -> showAlert("Record Payment", "Payment recording dialog would open here."));

            section.getChildren().addAll(addBtn, payBtn);
        } else if (role.equals("STUDENT")) {
            Button payBtn = createButton("Pay Online", "#14b8a6");
            payBtn.setOnAction(e -> showAlert("Payment Gateway", "Online payment integration would open here."));
            section.getChildren().add(payBtn);
        }

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setPrefHeight(40);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        return btn;
    }

    private void loadFees() {
        feeData.clear();
        allFeeData.clear();

        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                List<StudentFee> fees = feeDAO.getStudentFees(student.getId());
                feeData.addAll(fees);
            }
        } else {
            // For admin/faculty
            List<StudentFee> fees = feeDAO.getAllFees();
            allFeeData.addAll(fees);
            feeData.addAll(fees);
        }
    }

    private void filterFees(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            feeData.setAll(allFeeData);
            return;
        }

        feeData.clear();
        String lowerKeyword = keyword.toLowerCase().trim();

        for (StudentFee fee : allFeeData) {
            if (String.valueOf(fee.getStudentId()).contains(lowerKeyword)) {
                feeData.add(fee);
            }
        }
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
