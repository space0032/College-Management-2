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

        Label title = new Label(role.equals("STUDENT") ? "My Gate Passes" : "Gate Pass Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadGatePasses());

        header.getChildren().addAll(title, spacer, refreshBtn);
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
        tableView.setItems(gatePassData);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        TableColumn<GatePass, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<GatePass, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));
        reasonCol.setPrefWidth(200);

        TableColumn<GatePass, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getFromDate() != null ? data.getValue().getFromDate().format(formatter) : "-"
        ));
        fromCol.setPrefWidth(140);

        TableColumn<GatePass, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getToDate() != null ? data.getValue().getToDate().format(formatter) : "-"
        ));
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

        tableView.getColumns().addAll(idCol, reasonCol, fromCol, toCol, statusCol);
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
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private void loadGatePasses() {
        gatePassData.clear();
        List<GatePass> passes;
        
        if (role.equals("STUDENT")) {
            Student student = studentDAO.getStudentByUserId(userId);
            if (student != null) {
                passes = gatePassDAO.getStudentPasses(student.getId());
            } else {
                return;
            }
        } else {
            passes = gatePassDAO.getAllPasses();
        }
        
        gatePassData.addAll(passes);
    }

    private void requestGatePass() {
        showAlert("Request Gate Pass", "Request gate pass dialog would open here.");
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
        
        if (gatePassDAO.approveRequest(selected.getId(), userId, "Approved via JavaFX")) {
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
        
        if (gatePassDAO.rejectRequest(selected.getId(), userId, "Rejected via JavaFX")) {
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
