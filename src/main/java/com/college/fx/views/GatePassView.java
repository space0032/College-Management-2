package com.college.fx.views;

import com.college.dao.GatePassDAO;
import com.college.dao.StudentDAO;
import com.college.models.GatePass;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * JavaFX Gate Pass View
 */
public class GatePassView {

    private VBox root;
    private TableView<GatePass> tableView;
    private ObservableList<GatePass> gatePassData;
    @SuppressWarnings("unused")
    private GatePassDAO gatePassDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    public GatePassView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.gatePassDAO = new GatePassDAO();
        this.studentDAO = new StudentDAO();
        this.gatePassData = FXCollections.observableArrayList();
        createView();
        loadGatePasses();
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

        Label title = new Label(role.equals("STUDENT") ? "My Gate Passes" : "Gate Pass Management");
        title.getStyleClass().add("section-title");
        // title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadGatePasses());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createTableSection() {
        VBox section = new VBox();
        section.getStyleClass().add("glass-card");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.getStyleClass().add("glass-table");
        tableView.setItems(gatePassData);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        TableColumn<GatePass, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<GatePass, String> studentCol = new TableColumn<>("Student Name");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<GatePass, String> enrollCol = new TableColumn<>("Enrollment ID");
        enrollCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEnrollmentId()));
        enrollCol.setPrefWidth(120);

        TableColumn<GatePass, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));
        reasonCol.setPrefWidth(200);

        TableColumn<GatePass, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFromDate() != null ? data.getValue().getFromDate().format(formatter) : "-"));
        fromCol.setPrefWidth(140);

        TableColumn<GatePass, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getToDate() != null ? data.getValue().getToDate().format(formatter) : "-"));
        toCol.setPrefWidth(140);

        TableColumn<GatePass, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<GatePass, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "PENDING":
                            setTextFill(Color.web("#f59e0b"));
                            break;
                        case "APPROVED":
                            setTextFill(Color.web("#22c55e"));
                            break;
                        case "REJECTED":
                            setTextFill(Color.web("#ef4444"));
                            break;
                        default:
                            setTextFill(Color.web("#0f172a"));
                    }
                    setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                }
            }
        });

        tableView.getColumns()
                .addAll(java.util.Arrays.asList(idCol, studentCol, enrollCol, reasonCol, fromCol, toCol, statusCol));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (role.equals("STUDENT") || session.hasPermission("REQUEST_GATE_PASS")) {
            Button requestBtn = createButton("Request Gate Pass", "#22c55e");
            requestBtn.setOnAction(e -> requestGatePass());
            section.getChildren().add(requestBtn);
        }

        if (session.hasPermission("APPROVE_GATE_PASS")) {
            Button approveBtn = createButton("Approve", "#22c55e");
            approveBtn.setOnAction(e -> approveGatePass());

            Button rejectBtn = createButton("Reject", "#ef4444");
            rejectBtn.setOnAction(e -> rejectGatePass());

            section.getChildren().addAll(approveBtn, rejectBtn);
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

    private void loadGatePasses() {
        gatePassData.clear();
        List<GatePass> passes;

        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                passes = GatePassDAO.getStudentPasses(student.getId());
            } else {
                return;
            }
        } else {
            passes = GatePassDAO.getAllPasses();
        }

        gatePassData.addAll(passes);
    }

    private void requestGatePass() {
        // Create the custom dialog.
        Dialog<GatePass> dialog = new Dialog<>();
        dialog.setTitle("New Gate Pass Request");
        dialog.setHeaderText("Please fill in the details for your gate pass request.");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField reason = new TextField();
        reason.setPromptText("Reason");

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date");

        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date");

        TextField destination = new TextField();
        destination.setPromptText("Destination");

        TextField parentContact = new TextField();
        parentContact.setPromptText("Parent Contact");

        grid.add(new Label("Reason:"), 0, 0);
        grid.add(reason, 1, 0);
        grid.add(new Label("From Date:"), 0, 1);
        grid.add(fromDate, 1, 1);
        grid.add(new Label("To Date:"), 0, 2);
        grid.add(toDate, 1, 2);
        grid.add(new Label("Destination:"), 0, 3);
        grid.add(destination, 1, 3);
        grid.add(new Label("Parent Contact:"), 0, 4);
        grid.add(parentContact, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a GatePass object when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                // Basic Validation
                if (reason.getText().isEmpty() || fromDate.getValue() == null || toDate.getValue() == null ||
                        destination.getText().isEmpty() || parentContact.getText().isEmpty()) {
                    return null; // Handle invalid input better if possible, but this prevents empty objects
                }

                GatePass gp = new GatePass();
                gp.setReason(reason.getText());
                gp.setFromDate(fromDate.getValue());
                gp.setToDate(toDate.getValue());
                gp.setDestination(destination.getText());
                gp.setParentContact(parentContact.getText());

                return gp;
            }
            return null;
        });

        java.util.Optional<GatePass> result = dialog.showAndWait();

        result.ifPresent(gp -> {
            // Get Student ID for current user
            Student student = studentDAO.getStudentByUserId(userId);
            if (student == null) {
                showAlert("Error", "Student profile not found. Please contact admin.");
                return;
            }

            gp.setStudentId(student.getId());

            if (GatePassDAO.createRequest(gp)) {
                loadGatePasses();
                showAlert("Success", "Gate pass requested successfully!");
            } else {
                showAlert("Error", "Failed to create gate pass request.");
            }
        });
    }

    private void approveGatePass() {
        GatePass selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a gate pass to approve.");
            return;
        }
        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            showAlert("Error", "Can only approve pending gate passes.");
            return;
        }

        if (GatePassDAO.approveRequest(selected.getId(), userId, "Approved via JavaFX")) {
            loadGatePasses();
            showAlert("Success", "Gate pass approved!");
        } else {
            showAlert("Error", "Failed to approve gate pass.");
        }
    }

    private void rejectGatePass() {
        GatePass selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a gate pass to reject.");
            return;
        }
        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            showAlert("Error", "Can only reject pending gate passes.");
            return;
        }

        if (GatePassDAO.rejectRequest(selected.getId(), userId, "Rejected via JavaFX")) {
            loadGatePasses();
            showAlert("Success", "Gate pass rejected.");
        } else {
            showAlert("Error", "Failed to reject gate pass.");
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
