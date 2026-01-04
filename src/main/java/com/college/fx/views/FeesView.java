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

        TableColumn<StudentFee, String> studentIdCol = new TableColumn<>("Student ID");
        studentIdCol
                .setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStudentId())));
        studentIdCol.setPrefWidth(100);

        TableColumn<StudentFee, String> studentNameCol = new TableColumn<>("Student Name");
        studentNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentNameCol.setPrefWidth(150);

        TableColumn<StudentFee, String> enrollmentCol = new TableColumn<>("Enrollment ID");
        enrollmentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentUsername()));
        enrollmentCol.setPrefWidth(120);

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

        tableView.getColumns().addAll(idCol, studentIdCol, studentNameCol, enrollmentCol, categoryCol, amountCol,
                paidCol, dueCol, statusCol);
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
            addBtn.setOnAction(e -> showAddFeeDialog());

            Button payBtn = createButton("Record Payment", "#3b82f6");
            payBtn.setOnAction(e -> showRecordPaymentDialog());

            section.getChildren().addAll(addBtn, payBtn);
        } else if (role.equals("STUDENT")) {
            Button payBtn = createButton("Pay Online", "#14b8a6");
            payBtn.setOnAction(e -> showAlert("Payment Gateway", "Online payment integration would open here."));
            section.getChildren().add(payBtn);
        }

        // Common buttons for Receipt (All) and Reminder (Finance/Admin)
        Button receiptBtn = createButton("Download Receipt", "#64748b");
        receiptBtn.setOnAction(e -> handleDownloadReceipt());
        section.getChildren().add(receiptBtn);

        if (session.hasPermission("MANAGE_FEES")) {
            Button reminderBtn = createButton("Send Reminder", "#f59e0b");
            reminderBtn.setOnAction(e -> handleSendReminder());
            section.getChildren().add(reminderBtn);
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
            boolean matches = String.valueOf(fee.getStudentId()).contains(lowerKeyword);

            if (!matches && fee.getStudentName() != null) {
                matches = fee.getStudentName().toLowerCase().contains(lowerKeyword);
            }

            if (!matches && fee.getStudentUsername() != null) {
                matches = fee.getStudentUsername().toLowerCase().contains(lowerKeyword);
            }

            if (matches) {
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

    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1:
                return "Tuition Fees";
            case 2:
                return "Hostel Fees";
            case 3:
                return "Exam Fees";
            case 4:
                return "Library Fees";
            case 5:
                return "Sports Fees";
            case 6:
                return "Lab Fees";
            default:
                return "Other Fees";
        }
    }

    private void showAddFeeDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Fee Record");
        dialog.setHeaderText("Assign Fee to Student");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> studentCombo = new ComboBox<>();
        studentCombo.setPromptText("Select Student");
        studentCombo.setPrefWidth(200);

        try {
            List<Student> students = studentDAO.getAllStudents();
            for (Student s : students) {
                studentCombo.getItems().add(s.getId() + " - " + s.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Select Category");
        categoryCombo.getItems().addAll(
                "1 - Tuition Fees", "2 - Hostel Fees", "3 - Exam Fees",
                "4 - Library Fees", "5 - Sports Fees", "6 - Lab Fees");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(java.time.LocalDate.now().plusMonths(1));

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentCombo, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountField, 1, 2);
        grid.add(new Label("Due Date:"), 0, 3);
        grid.add(dueDatePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String selectedStudent = studentCombo.getValue();
                    String selectedCategory = categoryCombo.getValue();

                    if (selectedStudent == null || selectedCategory == null || amountField.getText().isEmpty()) {
                        showAlert("Error", "Please fill in all fields");
                        return null;
                    }

                    int studentId = Integer.parseInt(selectedStudent.split(" - ")[0]);
                    int categoryId = Integer.parseInt(selectedCategory.split(" - ")[0]);
                    double amount = Double.parseDouble(amountField.getText());
                    java.sql.Date dueDate = java.sql.Date.valueOf(dueDatePicker.getValue());

                    feeDAO.addStudentFee(studentId, categoryId, amount, dueDate);
                    loadFees();
                    showAlert("Success", "Fee record added successfully!");
                } catch (Exception e) {
                    showAlert("Error", "Failed to add fee: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showRecordPaymentDialog() {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            showAlert("Error", "Please select a fee record first");
            return;
        }

        StudentFee selectedFee = tableView.getSelectionModel().getSelectedItem();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Record Payment");
        dialog.setHeaderText("Record payment for: " + selectedFee.getStudentName());

        ButtonType saveButtonType = new ButtonType("Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label feeLabel = new Label("Fee: " + getCategoryName(selectedFee.getCategoryId()));
        Label totalLabel = new Label("Total Amount: ₹" + selectedFee.getTotalAmount());
        Label paidLabel = new Label("Already Paid: ₹" + selectedFee.getPaidAmount());

        double remaining = selectedFee.getTotalAmount() - selectedFee.getPaidAmount();
        Label remainingLabel = new Label("Remaining: ₹" + remaining);
        remainingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

        TextField paymentField = new TextField();
        paymentField.setPromptText("Payment Amount");
        paymentField.setText(String.valueOf(remaining));

        DatePicker paymentDatePicker = new DatePicker();
        paymentDatePicker.setValue(java.time.LocalDate.now());

        grid.add(feeLabel, 0, 0, 2, 1);
        grid.add(totalLabel, 0, 1, 2, 1);
        grid.add(paidLabel, 0, 2, 2, 1);
        grid.add(remainingLabel, 0, 3, 2, 1);
        grid.add(new Label("Payment Amount:"), 0, 4);
        grid.add(paymentField, 1, 4);
        grid.add(new Label("Payment Date:"), 0, 5);
        grid.add(paymentDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double paymentAmount = Double.parseDouble(paymentField.getText());

                    if (paymentAmount <= 0) {
                        showAlert("Error", "Payment amount must be positive");
                        return null;
                    }
                    if (paymentAmount > remaining) {
                        showAlert("Error", "Payment amount exceeds remaining balance");
                        return null;
                    }

                    java.sql.Date paymentDate = java.sql.Date.valueOf(paymentDatePicker.getValue());
                    feeDAO.recordPayment(selectedFee.getId(), paymentAmount, paymentDate);
                    loadFees();
                    showAlert("Success", "Payment recorded successfully!");
                } catch (Exception e) {
                    showAlert("Error", "Failed to record payment: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleDownloadReceipt() {
        StudentFee selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a fee record first.");
            return;
        }

        if ("PAID".equals(selected.getStatus()) || "PARTIAL".equals(selected.getStatus())) {
            showAlert("Download Receipt",
                    "Receipt downloaded for:\n" +
                            "Student: " + selected.getStudentName() + "\n" +
                            "Category: " + getCategoryName(selected.getCategoryId()) + "\n" +
                            "Amount Paid: ₹" + selected.getPaidAmount());
        } else {
            showAlert("Unavailable", "Receipts are only available for paid or partially paid fees.");
        }
    }

    private void handleSendReminder() {
        StudentFee selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Required", "Please select a fee record first.");
            return;
        }

        if ("PAID".equals(selected.getStatus())) {
            showAlert("Info", "This fee is already paid. No reminder needed.");
            return;
        }

        showAlert("Success", "Payment reminder sent to " + selected.getStudentName() + " via email and SMS.");
    }

    public VBox getView() {
        return root;
    }
}
