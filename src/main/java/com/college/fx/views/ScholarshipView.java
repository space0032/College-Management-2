package com.college.fx.views;

import com.college.dao.CommunityDAO;
import com.college.dao.StudentDAO;
import com.college.models.Scholarship;
import com.college.models.ScholarshipApplication;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ScholarshipView {

    private final CommunityDAO communityDAO = new CommunityDAO();
    private VBox mainLayout;
    private int userId;
    private String userRole;

    public ScholarshipView() {
        this.userId = SessionManager.getInstance().getUserId();
        com.college.models.Role r = SessionManager.getInstance().getUserRole();
        this.userRole = r != null ? r.toString() : "";
    }

    public VBox getView() {
        mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Scholarship Portal");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Only Admins/Staff usually create scholarships
        Button createBtn = new Button("Post Scholarship");
        createBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white;");
        createBtn.setOnAction(e -> showCreateScholarshipDialog());

        // Check perms if needed, for now let anyone "Post" (simulating Donors) or
        // restricted
        // Assuming implementation allows it for demo
        header.getChildren().addAll(title, spacer, createBtn);

        ScrollPane scroll = new ScrollPane(createContent());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        mainLayout.getChildren().addAll(header, scroll);
        return mainLayout;
    }

    private VBox createContent() {
        VBox container = new VBox(15);
        List<Scholarship> scholarships = communityDAO.getAllScholarships();

        if (scholarships.isEmpty()) {
            container.getChildren().add(new Label("No scholarships available at the moment."));
        } else {
            for (Scholarship s : scholarships) {
                container.getChildren().add(createScholarshipCard(s));
            }
        }
        return container;
    }

    private VBox createScholarshipCard(Scholarship s) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10;");

        HBox top = new HBox(10);
        Label name = new Label(s.getTitle());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        name.setStyle("-fx-text-fill: white;");

        Label amount = new Label(String.format("$%.2f", s.getAmount()));
        amount.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-font-size: 16px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        top.getChildren().addAll(name, sp, amount);

        Label donor = new Label("Donor: " + s.getDonorName());
        donor.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");

        Label desc = new Label(s.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #cbd5e1;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button applyBtn = new Button("Apply");
        applyBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
        applyBtn.setOnAction(e -> showApplyDialog(s));

        Button viewAppsBtn = new Button("View Applications");
        viewAppsBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white;");
        viewAppsBtn.setOnAction(e -> showApplicationsDialog(s));

        // Show View Apps if creator or admin
        // Show Apply if Student

        actions.getChildren().add(applyBtn); // Default show apply
        // If user is creator, show View Apps
        if (s.getCreatedBy() == userId || "ADMIN".equals(userRole)) {
            actions.getChildren().add(viewAppsBtn);
        }

        card.getChildren().addAll(top, donor, desc, actions);
        return card;
    }

    private void showCreateScholarshipDialog() {
        Dialog<Scholarship> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Post Scholarship");
        dialog.getDialogPane().setMinWidth(500); // Fix truncation
        dialog.setHeaderText("Create new scholarship opportunity");

        ButtonType postBtn = new ButtonType("Post", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(postBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField titleF = new TextField();
        titleF.setPromptText("Title");
        TextField donorF = new TextField();
        donorF.setPromptText("Donor/Org Name");
        TextField amountF = new TextField();
        amountF.setPromptText("Amount");
        TextArea descF = new TextArea();
        descF.setPromptText("Description & Criteria");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleF, 1, 0);
        grid.add(new Label("Donor:"), 0, 1);
        grid.add(donorF, 1, 1);
        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountF, 1, 2);
        grid.add(new Label("Details:"), 0, 3);
        grid.add(descF, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == postBtn) {
                try {
                    Scholarship s = new Scholarship();
                    s.setTitle(titleF.getText());
                    s.setDonorName(donorF.getText());
                    s.setAmount(Double.parseDouble(amountF.getText()));
                    s.setDescription(descF.getText());
                    s.setCreatedBy(userId);
                    s.setStatus("OPEN");
                    return s;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            if (communityDAO.createScholarship(s)) {
                refresh();
            }
        });
    }

    private void showApplyDialog(Scholarship s) {
        Dialog<String> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Apply for Scholarship");
        dialog.setHeaderText("Apply for: " + s.getTitle());

        ButtonType applyBtn = new ButtonType("Submit Application", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyBtn, ButtonType.CANCEL);

        TextArea stmtArea = new TextArea();
        stmtArea.setPromptText("Personal Statement / Why do you need this scholarship?");

        VBox content = new VBox(10, new Label("Statement:"), stmtArea);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == applyBtn)
                return stmtArea.getText();
            return null;
        });

        dialog.showAndWait().ifPresent(stmt -> {
            // Need Student ID
            com.college.models.Student student = new StudentDAO().getStudentByUserId(userId);
            if (student == null) {
                // Not a student
                showAlert("Error", "Only students can apply.");
                return;
            }
            ScholarshipApplication app = new ScholarshipApplication();
            app.setScholarshipId(s.getId());
            app.setStudentId(student.getId());
            app.setStatement(stmt);
            app.setStatus("APPLIED");

            if (communityDAO.applyForScholarship(app)) {
                showAlert("Success", "Application Submitted!");
            }
        });
    }

    private void showApplicationsDialog(Scholarship s) {
        Dialog<Void> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Applications");
        dialog.setHeaderText("Applications for " + s.getTitle());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<ScholarshipApplication> table = new TableView<>();
        table.getStyleClass().add("glass-table");

        TableColumn<ScholarshipApplication, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStudentName()));

        TableColumn<ScholarshipApplication, String> stmtCol = new TableColumn<>("Statement");
        stmtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatement()));

        TableColumn<ScholarshipApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<ScholarshipApplication, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(p -> new TableCell<>() {
            Button approve = new Button("Approve");
            Button reject = new Button("Reject");
            {
                approve.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 10px;");
                reject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10px;");
                approve.setOnAction(
                        e -> handleUpdate(getTableView().getItems().get(getIndex()), "APPROVED", getTableView()));
                reject.setOnAction(
                        e -> handleUpdate(getTableView().getItems().get(getIndex()), "REJECTED", getTableView()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(new HBox(5, approve, reject));
            }
        });

        table.getColumns().addAll(java.util.Arrays.asList(studentCol, stmtCol, statusCol, actionCol));

        List<ScholarshipApplication> apps = communityDAO.getApplications(s.getId());
        table.getItems().setAll(apps);

        VBox box = new VBox(table);
        box.setPadding(new Insets(10));
        box.setPrefSize(600, 400);

        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void refresh() {
        ScrollPane scroll = (ScrollPane) mainLayout.getChildren().get(1);
        scroll.setContent(createContent());
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleUpdate(ScholarshipApplication app, String status, TableView<ScholarshipApplication> table) {
        if (communityDAO.updateApplicationStatus(app.getId(), status)) {
            app.setStatus(status);
            table.refresh();
        }
    }
}
