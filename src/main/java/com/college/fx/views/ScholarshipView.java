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
import javafx.scene.shape.SVGPath;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ScholarshipView {

    private final CommunityDAO communityDAO = new CommunityDAO();
    private BorderPane mainLayout;
    private StackPane centerStack;
    private int userId;
    private String userRole;
    private List<Scholarship> scholarships;

    // Combined or simple big icon for empty state
    private static final String EMPTY_STATE_ICON = "M12 2L1 7l11 5 9-4.09V17h2V7L12 2zm0 8.18l-8 3.64L12 17.5l8-3.64-8-3.68z M12 22l-8-3.64v-6.36l8 3.64 8-3.64v6.36L12 22z";
    // Academic Cap

    public ScholarshipView() {
        this.userId = SessionManager.getInstance().getUserId();
        com.college.models.Role r = SessionManager.getInstance().getUserRole();
        this.userRole = r != null ? r.toString() : "";
    }

    public BorderPane getView() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Scholarship Portal");
        title.getStyleClass().add("scholarship-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Post Scholarship");
        createBtn.getStyleClass().add("scholarship-post-btn");
        createBtn.setOnAction(e -> showCreateScholarshipDialog());

        // RESTRICTION: Hide button if user is STUDENT
        if ("STUDENT".equalsIgnoreCase(userRole)) {
            createBtn.setVisible(false);
            createBtn.setManaged(false); // Remove from layout calculation
        }

        header.getChildren().addAll(title, spacer, createBtn);
        mainLayout.setTop(header);

        // Center Stack (Empty State vs List)
        centerStack = new StackPane();
        centerStack.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerStack, Priority.ALWAYS);

        refreshContent(); // Load content

        return mainLayout;
    }

    private void refreshContent() {
        scholarships = communityDAO.getAllScholarships();

        centerStack.getChildren().clear();
        mainLayout.setCenter(centerStack);
        mainLayout.setBottom(null);

        // 1. Empty State Layer
        VBox emptyState = createEmptyState();

        // 2. Main Content Layer (Grid)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        VBox gridContainer = new VBox(15);
        gridContainer.setPadding(new Insets(10));

        for (Scholarship s : scholarships) {
            gridContainer.getChildren().add(createScholarshipCard(s));
        }
        scrollPane.setContent(gridContainer);

        // Logic: Show empty state if no scholarships, otherwise show list
        if (scholarships.isEmpty()) {
            centerStack.getChildren().add(emptyState);
        } else {
            // If we have items, we might want to show them in Center or Bottom
            // Per design: "No scholarships available" is the main view when empty.
            // "Recent Scholarships" is at the bottom.
            // Let's ALWAYS show "Recent Scholarships" at the bottom if there are any.

            // If there are NO scholarships, center has Empty State.
            // If there ARE scholarships, center could be "Featured" or just empty space?
            // But usually a portal lists them.

            // Let's follow the image structure implying:
            // Center = "Featured" or "Message".
            // Bottom = "Recent".

            // If list is not empty, I'll put the full list in the Center for now,
            // AND a carousel at the bottom for "Recent" (same items for demo).

            centerStack.getChildren().add(scrollPane);
        }

        // Bottom Carousel (Always show if there are items, or maybe even if empty
        // placeholders?)
        // The image shows "Recent Scholarships" with placeholders even when empty?
        // "No scholarships available... Check back soon!" is visible.
        // AND "Recent Scholarships" is below it with cards.

        // Wait, the image shows "No scholarships available" AND "Recent Scholarships"
        // below it.
        // This implies the center message is just a status, and the bottom is the list.
        // OR the "Recent" list is empty/placeholders?

        // Let's implement BOTH.
        // Center: Empty State Message (always visible if truly empty, or hidden if we
        // have logic)
        // But the image explicitly confirms: "No scholarships available" IS visible.
        // And "Recent Scholarships" has cards (maybe disabled or past ones?).

        // For this functional app:
        // If empty -> Show Center Message. Show Bottom with maybe placeholder or
        // hidden.
        // If not empty -> Show List in Center? Or Show Message "Available!"?

        // I will stick to:
        // If empty -> Center = Illustrated Message.
        // If not empty -> Center = List.

        // ADDITIONALLY: I will add the "Recent Scholarships" bar at the bottom
        // with the actual list (mini cards) regardless of Center content (if we have
        // data).

        if (!scholarships.isEmpty()) {
            VBox bottomSection = createRecentSection(scholarships);
            mainLayout.setBottom(bottomSection);
        } else {
            // Even if empty, maybe show the header "Recent Scholarships" with empty grid?
            // For now, only show if we have data to mimic a functional app.
        }
    }

    private VBox createEmptyState() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("scholarship-empty-container");

        SVGPath icon = new SVGPath();
        icon.setContent(EMPTY_STATE_ICON); // Graduation Cap
        icon.setScaleX(5);
        icon.setScaleY(5);
        icon.getStyleClass().add("scholarship-empty-icon");

        // Add a stack for the glow effect or just use CSS drop shadow
        StackPane iconPane = new StackPane(icon);
        iconPane.setPadding(new Insets(40)); // Space for scale

        Label mainText = new Label("No scholarships available at the moment.");
        mainText.getStyleClass().add("scholarship-empty-text");

        Label subText = new Label("Check back soon!");
        subText.getStyleClass().add("scholarship-empty-subtext");

        box.getChildren().addAll(iconPane, mainText, subText);
        return box;
    }

    private VBox createRecentSection(List<Scholarship> list) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(20, 0, 0, 0));

        Label title = new Label("Recent Scholarships");
        title.getStyleClass().add("recent-headers");

        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox cardBox = new HBox(15);
        cardBox.setPadding(new Insets(10));
        cardBox.getStyleClass().add("recent-scholarships-bar");

        for (Scholarship s : list) {
            cardBox.getChildren().add(createMiniCard(s));
        }

        scroll.setContent(cardBox);
        section.getChildren().addAll(title, scroll);
        return section;
    }

    private Button createMiniCard(Scholarship s) {
        // Using Button for clickable mini card
        Button card = new Button();
        card.getStyleClass().add("scholarship-card-mini");

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        icon.setContent("M12 2l9 4-9 4-9-4 9-4zm0 9l-9-4v6h2v-5l7 3 7-3v5h2v-6l-9 4z"); // Cap small
        icon.getStyleClass().add("mini-card-icon");

        Label title = new Label(s.getTitle());
        title.getStyleClass().add("mini-card-title");
        title.setWrapText(true);
        title.setMaxWidth(180);

        Label amount = new Label(String.format("$%.0f", s.getAmount()));
        amount.setStyle("-fx-text-fill: #14b8a6; -fx-font-weight: bold;");

        content.getChildren().addAll(icon, title, amount);
        card.setGraphic(content);

        card.setOnAction(e -> showApplyDialog(s));

        return card;
    }

    private VBox createScholarshipCard(Scholarship s) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.6); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10;");

        HBox top = new HBox(10);
        Label name = new Label(s.getTitle());
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        name.setStyle("-fx-text-fill: white;");

        Label amount = new Label(String.format("$%.2f", s.getAmount()));
        amount.setStyle("-fx-text-fill: #2dd4bf; -fx-font-weight: bold; -fx-font-size: 16px;");

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
        applyBtn.getStyleClass().add("accent-button");
        applyBtn.setOnAction(e -> showApplyDialog(s));

        Button viewAppsBtn = new Button("View Applications");
        viewAppsBtn.getStyleClass().add("icon-button");
        viewAppsBtn.setOnAction(e -> showApplicationsDialog(s));

        actions.getChildren().add(applyBtn);
        if (s.getCreatedBy() == userId || "ADMIN".equals(userRole) || "FACULTY".equals(userRole)) {
            actions.getChildren().add(viewAppsBtn);
        }

        card.getChildren().addAll(top, donor, desc, actions);
        return card;
    }

    private void showCreateScholarshipDialog() {
        Dialog<Scholarship> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Post Scholarship");
        dialog.setHeaderText("Create new scholarship opportunity");

        ButtonType postBtn = new ButtonType("Post", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(postBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleF = new TextField();
        titleF.setPromptText("Title");
        TextField donorF = new TextField();
        donorF.setPromptText("Donor/Org Name");
        TextField amountF = new TextField();
        amountF.setPromptText("Amount");
        TextArea descF = new TextArea();
        descF.setPromptText("Description & Criteria");
        descF.setStyle("-fx-text-fill: white; -fx-control-inner-background: #1e293b;");

        DialogUtils.addFormRow(grid, "Title:", titleF, 0);
        DialogUtils.addFormRow(grid, "Donor:", donorF, 1);
        DialogUtils.addFormRow(grid, "Amount:", amountF, 2);
        DialogUtils.addFormRow(grid, "Details:", descF, 3);

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
                // Trigger Announcement
                com.college.models.Announcement ann = new com.college.models.Announcement();
                ann.setTitle("New Scholarship: " + s.getTitle());
                ann.setContent("A new scholarship opportunity is available. Amount: $" + s.getAmount()
                        + ". Check the Scholarship Portal.");
                ann.setTargetAudience("ALL");
                ann.setPriority("HIGH");
                ann.setCreatedBy(userId);
                ann.setActive(true);
                new com.college.dao.AnnouncementDAO().addAnnouncement(ann);

                refreshContent();
                showAlert("Success", "Scholarship Posted!");
            }
        });
    }

    private void showApplyDialog(Scholarship s) {
        Dialog<String> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Apply for Scholarship");
        dialog.setHeaderText("Apply for: " + s.getTitle());
        ButtonType applyBtn = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyBtn, ButtonType.CANCEL);

        TextArea stmtArea = new TextArea();
        stmtArea.setPromptText("Personal Statement...");
        stmtArea.setStyle("-fx-text-fill: white; -fx-control-inner-background: #e3e5e7ff;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        DialogUtils.addFormRow(grid, "Statement:", stmtArea, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> btn == applyBtn ? stmtArea.getText() : null);

        dialog.showAndWait().ifPresent(stmt -> {
            com.college.models.Student student = new StudentDAO().getStudentByUserId(userId);
            if (student == null) {
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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ScholarshipApplication, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStudentName()));

        TableColumn<ScholarshipApplication, String> stmtCol = new TableColumn<>("Statement");
        stmtCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatement()));

        TableColumn<ScholarshipApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<ScholarshipApplication, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(p -> new TableCell<>() {
            Button approve = new Button("✔");
            Button reject = new Button("✘");
            {
                approve.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                reject.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
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
                else {
                    HBox box = new HBox(5, approve, reject);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
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

    private void handleUpdate(ScholarshipApplication app, String status, TableView<ScholarshipApplication> table) {
        if (communityDAO.updateApplicationStatus(app.getId(), status)) {
            app.setStatus(status);
            table.refresh();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
