package com.college.fx.views;

import com.college.dao.StudentLeaveDAO;
import com.college.models.StudentLeave;
import com.college.models.User;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LeaveApprovalView {

    private final StudentLeaveDAO leaveDAO = new StudentLeaveDAO();
    private TableView<StudentLeave> table;

    public VBox getView() {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label title = new Label("Leave Approvals");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #1e293b;");

        table = new TableView<>();

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

                approveBtn.setOnAction(e -> handleAction("APPROVED"));
                rejectBtn.setOnAction(e -> handleAction("REJECTED"));
            }

            private void handleAction(String status) {
                StudentLeave leave = getTableView().getItems().get(getIndex());
                SessionManager session = SessionManager.getInstance();
                if (session.isLoggedIn() && leaveDAO.updateLeaveStatus(leave.getId(), status, session.getUserId())) {
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

        table.getColumns().addAll(studentCol, typeCol, datesCol, reasonCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        refreshTable();

        mainLayout.getChildren().addAll(title, table);
        return mainLayout;
    }

    private void refreshTable() {
        table.getItems().setAll(leaveDAO.getPendingLeaves());
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
