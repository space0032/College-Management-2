package com.college.fx.views;

import com.college.dao.StaffLeaveDAO;
import com.college.models.StaffLeave;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StaffLeaveView {

    private VBox root;
    private TableView<StaffLeave> leaveTable;
    private ObservableList<StaffLeave> leaveData;
    private StaffLeaveDAO leaveDAO;

    public StaffLeaveView() {
        this.leaveDAO = new StaffLeaveDAO();
        this.leaveData = FXCollections.observableArrayList();
        createView();
        refreshTable();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Staff Leave Application");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        root.getChildren().addAll(title, createForm(), createTable());
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("glass-card");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Casual Leave", "Sick Leave", "Earned Leave", "Duty Leave");
        typeCombo.setPromptText("Select Leave Type");

        DatePicker startDate = new DatePicker();
        DatePicker endDate = new DatePicker();
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Reason for leave...");
        reasonArea.setPrefRowCount(3);

        Button submitBtn = new Button("Submit Request");
        submitBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null || startDate.getValue() == null || endDate.getValue() == null
                    || reasonArea.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields.");
                return;
            }

            SessionManager session = SessionManager.getInstance();
            if (!session.isLoggedIn())
                return;

            StaffLeave leave = new StaffLeave(
                    session.getUserId(),
                    typeCombo.getValue(),
                    startDate.getValue(),
                    endDate.getValue(),
                    reasonArea.getText());

            if (leaveDAO.createLeaveRequest(leave)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully!");
                refreshTable();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit request.");
            }
        });

        grid.add(new Label("Leave Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDate, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDate, 1, 2);
        grid.add(new Label("Reason:"), 0, 3);
        grid.add(reasonArea, 1, 3);
        grid.add(submitBtn, 1, 4);

        return grid;
    }

    private VBox createTable() {
        leaveTable = new TableView<>();
        leaveTable.getStyleClass().add("glass-table");
        leaveTable.setItems(leaveData);

        TableColumn<StaffLeave, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaveType()));

        TableColumn<StaffLeave, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStartDate().toString()));

        TableColumn<StaffLeave, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEndDate().toString()));

        TableColumn<StaffLeave, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));

        TableColumn<StaffLeave, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equals(item))
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    else if ("REJECTED".equals(item))
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                }
            }
        });

        leaveTable.getColumns().addAll(typeCol, fromCol, toCol, reasonCol, statusCol);
        VBox.setVgrow(leaveTable, Priority.ALWAYS);

        VBox box = new VBox(10, new Label("My Leave History"), leaveTable);
        box.setPadding(new Insets(10, 0, 0, 0));
        return box;
    }

    private void refreshTable() {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn()) {
            leaveData.setAll(leaveDAO.getLeavesByUser(session.getUserId()));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    public VBox getView() {
        return root;
    }
}
