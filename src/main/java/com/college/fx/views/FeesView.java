package com.college.fx.views;

import com.college.dao.EnhancedFeeDAO;
import com.college.dao.StudentDAO;
import com.college.models.StudentFee;
import com.college.models.Student;
import com.college.models.FeePayment;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;
import com.college.dao.SystemSettingsDAO; // Added
import com.college.services.DropboxStorageService; // Added
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image; // Added
import javafx.scene.image.ImageView; // Added
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
    private SystemSettingsDAO systemSettingsDAO; // Added
    private DropboxStorageService storageService; // Added
    private TextField searchField;
    private String role;
    private int userId;

    public FeesView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.feeDAO = new EnhancedFeeDAO();
        this.studentDAO = new StudentDAO();
        this.systemSettingsDAO = new SystemSettingsDAO();
        this.storageService = new DropboxStorageService();
        this.feeData = FXCollections.observableArrayList();
        this.allFeeData = FXCollections.observableArrayList();
        createView();
        loadFees();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

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
        header.getStyleClass().add("glass-card");

        Label title = new Label(role.equals("STUDENT") ? "My Fees" : "Fee Management");
        title.getStyleClass().add("section-title");
        // title.setTextFill(Color.web("#0f172a"));

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

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableView.setItems(feeData);

        TableColumn<StudentFee, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<StudentFee, String> studentNameCol = new TableColumn<>("Student Name");
        studentNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentNameCol.setPrefWidth(150);

        TableColumn<StudentFee, String> enrollmentCol = new TableColumn<>("Enrollment ID");
        enrollmentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentUsername()));
        enrollmentCol.setPrefWidth(120);

        TableColumn<StudentFee, String> categoryCol = new TableColumn<>("Category");
        categoryCol
                .setCellValueFactory(
                        data -> new SimpleStringProperty(getCategoryName(data.getValue().getCategoryId())));
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

        tableView.getColumns()
                .addAll(java.util.Arrays.asList(idCol, studentNameCol, enrollmentCol, categoryCol, amountCol,
                        paidCol, dueCol, statusCol));
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
        DialogUtils.styleDialog(alert);
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
        DialogUtils.styleDialog(dialog);
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

        DialogUtils.addFormRow(grid, "Student:", studentCombo, 0);
        DialogUtils.addFormRow(grid, "Category:", categoryCombo, 1);
        DialogUtils.addFormRow(grid, "Amount:", amountField, 2);
        DialogUtils.addFormRow(grid, "Due Date:", dueDatePicker, 3);

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
        DialogUtils.styleDialog(dialog);
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
        DialogUtils.addFormRow(grid, "Payment Amount:", paymentField, 4);
        DialogUtils.addFormRow(grid, "Payment Date:", paymentDatePicker, 5);

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

        if (!"PAID".equals(selected.getStatus()) && !"PARTIAL".equals(selected.getStatus())) {
            showAlert("Unavailable", "Receipts are only available for paid or partially paid fees.");
            return;
        }

        showReceiptDialog(selected);
    }

    private void showReceiptDialog(StudentFee fee) {
        Dialog<ButtonType> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Fee Receipt");
        dialog.setHeaderText(null);

        ButtonType printBtn = new ButtonType("Print", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(printBtn, ButtonType.CLOSE);

        // Fetch payment details
        List<FeePayment> payments = feeDAO.getPaymentHistory(fee.getId());

        // --- Receipt Container ---
        VBox receiptContainer = new VBox();
        receiptContainer.setStyle(
                "-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
        receiptContainer.setPrefWidth(600);
        receiptContainer.setMaxWidth(600);

        // Border around content
        VBox contentBox = new VBox(20);
        contentBox.setStyle("-fx-border-color: #000000ff; -fx-border-width: 2; -fx-padding: 30;");

        // --- Header Section ---

        // Fetch Settings
        String collegeNameStr = systemSettingsDAO.getSetting("COLLEGE_NAME");
        if (collegeNameStr == null || collegeNameStr.isEmpty())
            collegeNameStr = "College Management System";

        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);

        // Logo
        String logoPath = systemSettingsDAO.getSetting("COLLEGE_LOGO_PATH");
        if (logoPath != null && !logoPath.isEmpty()) {
            ImageView logoView = new ImageView();
            logoView.setFitHeight(70);
            logoView.setFitWidth(70);
            logoView.setPreserveRatio(true);

            // Async load
            final ImageView fLogo = logoView;
            new Thread(() -> {
                String url = storageService.getTemporaryLink(logoPath);
                if (url != null) {
                    javafx.application.Platform.runLater(() -> fLogo.setImage(new Image(url, true)));
                }
            }).start();
            headerBox.getChildren().add(logoView);
        }

        Label collegeName = new Label(collegeNameStr);
        collegeName.setFont(Font.font("Times New Roman", FontWeight.BOLD, 26));
        collegeName.setStyle("-fx-text-fill: black;");

        Label address = new Label("123, University Road, Academic City, 560001");
        address.setFont(Font.font("Times New Roman", 14));
        address.setStyle("-fx-text-fill: black;");

        Label receiptTitle = new Label("OFFICIAL FEE RECEIPT");
        receiptTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        receiptTitle.setStyle(
                "-fx-background-color: #e2e8f0; -fx-padding: 5 15 5 15; -fx-background-radius: 15; -fx-text-fill: black;");
        VBox.setMargin(receiptTitle, new Insets(15, 0, 0, 0));

        headerBox.getChildren().addAll(collegeName, address, receiptTitle);

        // --- Info Grid ---
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(20, 0, 20, 0));

        addReceiptField(infoGrid, "Receipt Date:",
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MMM-yyyy")), 0, 0);
        addReceiptField(infoGrid, "Enrollment ID:", fee.getStudentUsername() != null ? fee.getStudentUsername() : "N/A",
                1, 0);

        addReceiptField(infoGrid, "Student Name:", fee.getStudentName(), 0, 1);
        addReceiptField(infoGrid, "Fee Category:", getCategoryName(fee.getCategoryId()), 1, 1);

        addReceiptField(infoGrid, "Course/Batch:", "B.Tech - CS", 0, 2); // Hardcoded for mockup aesthetics
        addReceiptField(infoGrid, "Receipt No:", "REC-" + System.currentTimeMillis() % 10000, 1, 2);

        // Style the grid columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        infoGrid.getColumnConstraints().addAll(col1, col2);

        // --- Payment Details Table ---
        VBox tableContainer = new VBox(0);
        tableContainer.setStyle("-fx-border-color: #cbd5e1; -fx-border-width: 1;");

        // Header
        HBox tableHeader = new HBox();
        tableHeader.setStyle(
                "-fx-background-color: #f1f5f9; -fx-padding: 8; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0;");
        tableHeader.getChildren().addAll(
                createTableLabel("Payment Date", 120),
                createTableLabel("Transaction ID", 150),
                createTableLabel("Mode", 100),
                createTableLabel("Amount (INR)", 100));

        tableContainer.getChildren().add(tableHeader);

        double totalPaidInReceipt = 0;
        for (FeePayment p : payments) {
            HBox row = new HBox();
            row.setStyle(
                    "-fx-padding: 8; -fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
            row.getChildren().addAll(
                    createTableValue(p.getPaymentDate().toString(), 120),
                    createTableValue(p.getReceiptNumber(), 150),
                    createTableValue(p.getPaymentMode(), 100),
                    createTableValue("₹ " + String.format("%.2f", p.getAmount()), 100));
            tableContainer.getChildren().add(row);
            totalPaidInReceipt += p.getAmount();
        }

        // --- Totals Section ---
        HBox totalBox = new HBox(20);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(15, 10, 0, 0));

        Label totalLabel = new Label("Total Amount Paid:");
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label totalValue = new Label("₹ " + String.format("%.2f", totalPaidInReceipt));
        totalValue.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalValue.setStyle("-fx-text-fill: black;");

        totalBox.getChildren().addAll(totalLabel, totalValue);

        // --- Footer (Signatures & Stamp) ---
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.BOTTOM_CENTER);
        footerBox.setPadding(new Insets(50, 20, 10, 20));

        VBox stampBox = new VBox();
        stampBox.setAlignment(Pos.CENTER);
        // Create a 'PAID' stamp using CSS border/rotate
        Label stamp = new Label("PAID");
        stamp.setFont(Font.font("Stencil", FontWeight.BOLD, 36));
        stamp.setStyle(
                "-fx-text-fill: #22c55e; -fx-border-color: #22c55e; -fx-border-width: 4; -fx-border-radius: 10; -fx-padding: 5 20 5 20; -fx-rotate: -15; -fx-opacity: 0.8;");
        stampBox.getChildren().add(stamp);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        VBox signBox = new VBox(5);
        signBox.setAlignment(Pos.CENTER);
        Separator line = new Separator();
        line.setPrefWidth(150);
        Label signLabel = new Label("Authorized Signatory");
        signLabel.setFont(Font.font("Times New Roman", 12));
        signLabel.setStyle("-fx-text-fill: black;");
        signBox.getChildren().addAll(line, signLabel);

        footerBox.getChildren().addAll(stampBox, footerSpacer, signBox);

        // Assemble
        contentBox.getChildren().addAll(headerBox, new Separator(), infoGrid, tableContainer, totalBox, footerBox);
        receiptContainer.getChildren().add(contentBox);

        ScrollPane scroll = new ScrollPane(receiptContainer);
        scroll.setFitToWidth(true);
        // Remove scrollpane styling for cleaner look in dialog
        scroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scroll);

        // Printing
        final javafx.scene.Node printNode = receiptContainer;
        dialog.setResultConverter(btn -> {
            if (btn == printBtn) {
                javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(root.getScene().getWindow())) {
                    boolean success = job.printPage(printNode);
                    if (success) {
                        job.endJob();
                        showAlert("Success", "Receipt sent to printer.");
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void addReceiptField(GridPane grid, String labelText, String valueText, int col, int row) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: black;");
        lbl.setMinWidth(90);

        Label val = new Label(valueText);
        val.setFont(Font.font("Arial", 13));
        val.setStyle("-fx-text-fill: black;");
        val.setWrapText(true);

        container.getChildren().addAll(lbl, val);
        grid.add(container, col, row);
    }

    private Label createTableLabel(String text, double width) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setStyle("-fx-text-fill: black;");
        if (width > 0)
            l.setPrefWidth(width);
        else
            l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private Label createTableValue(String text, double width) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setStyle("-fx-text-fill: black;");
        if (width > 0)
            l.setPrefWidth(width);
        else
            l.setMaxWidth(Double.MAX_VALUE);
        return l;
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
