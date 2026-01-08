package com.college.fx.views;

import com.college.dao.StudentLeaveDAO;
import com.college.dao.StaffLeaveDAO;
import com.college.models.StudentLeave;
import com.college.models.StaffLeave;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LeaveApprovalView {

    private final StudentLeaveDAO studentLeaveDAO = new StudentLeaveDAO();
    private final StaffLeaveDAO staffLeaveDAO = new StaffLeaveDAO();
    private TableView<StudentLeave> studentTable;
    private TableView<StaffLeave> staffTable;

    public VBox getView() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        Label title = new Label("Leave Approvals");
        title.getStyleClass().add("section-title");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab studentTab = new Tab("Student Leaves");
        studentTab.setContent(createStudentTable());

        Tab staffTab = new Tab("Staff Leaves");
        staffTab.setContent(createStaffTable());

        tabPane.getTabs().add(studentTab);

        // Only Admin can see Staff Leaves (or maybe HOD later)
        if (SessionManager.getInstance().isAdmin()) {
            tabPane.getTabs().add(staffTab);
        }

        mainLayout.getChildren().addAll(title, tabPane);
        return mainLayout;
    }

    private VBox createStudentTable() {
        studentTable = new TableView<>();
        studentTable.getStyleClass().add("glass-table");

        TableColumn<StudentLeave, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));

        TableColumn<StudentLeave, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaveType()));

        TableColumn<StudentLeave, String> datesCol = new TableColumn<>("Dates");
        datesCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDate().toString() + " to " + data.getValue().getEndDate().toString()));

        TableColumn<StudentLeave, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));

        TableColumn<StudentLeave, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox pane = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> handleStudentAction("APPROVED"));
                rejectBtn.setOnAction(e -> handleStudentAction("REJECTED"));
            }

            private void handleStudentAction(String status) {
                StudentLeave leave = getTableView().getItems().get(getIndex());
                SessionManager session = SessionManager.getInstance();
                if (session.isLoggedIn()
                        && studentLeaveDAO.updateLeaveStatus(leave.getId(), status, session.getUserId())) {
                    getTableView().getItems().remove(leave);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Request " + status);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        studentTable.getColumns().addAll(java.util.Arrays.asList(studentCol, typeCol, datesCol, reasonCol, actionCol));
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        refreshStudentTable();
        return new VBox(studentTable);
    }

    private VBox createStaffTable() {
        staffTable = new TableView<>();
        staffTable.getStyleClass().add("glass-table");

        TableColumn<StaffLeave, String> staffCol = new TableColumn<>("Staff Name");
        staffCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStaffName()));

        TableColumn<StaffLeave, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaveType()));

        TableColumn<StaffLeave, String> datesCol = new TableColumn<>("Dates");
        datesCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStartDate().toString() + " to " + data.getValue().getEndDate().toString()));

        TableColumn<StaffLeave, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));

        TableColumn<StaffLeave, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox pane = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> handleStaffAction("APPROVED"));
                rejectBtn.setOnAction(e -> handleStaffAction("REJECTED"));
            }

            private void handleStaffAction(String status) {
                StaffLeave leave = getTableView().getItems().get(getIndex());
                SessionManager session = SessionManager.getInstance();
                // Simple comment dialog for rejection if needed, but keeping it simple for now
                if (session.isLoggedIn()
                        && staffLeaveDAO.updateLeaveStatus(leave.getId(), status, session.getUserId(), "")) {
                    getTableView().getItems().remove(leave);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Request " + status);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        staffTable.getColumns().addAll(java.util.Arrays.asList(staffCol, typeCol, datesCol, reasonCol, actionCol));
        staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(staffTable, Priority.ALWAYS);

        refreshStaffTable();
        return new VBox(staffTable);
    }

    private void refreshStudentTable() {
        studentTable.getItems().setAll(studentLeaveDAO.getPendingLeaves());
    }

    private void refreshStaffTable() {
        staffTable.getItems().setAll(staffLeaveDAO.getAllPendingLeaves());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
