package com.college.fx.views;

import com.college.dao.PlacementDAO;
import com.college.models.PlacementApplication;
import com.college.models.PlacementCompany;
import com.college.models.PlacementDrive;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;

public class PlacementView {

    private VBox root;
    private final String userRole;
    private final int userId;
    private final PlacementDAO placementDAO;
    private TextField searchField;
    private ComboBox<String> companyFilter;
    private Label statsLabel;

    public PlacementView(String role, int userId) {
        this.userRole = role;
        this.userId = userId;
        this.placementDAO = new PlacementDAO();
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");

        Label title = new Label("Placement Cell");
        title.getStyleClass().add("page-title");
        title.setStyle(
                "-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 2, 0, 0, 1);");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add("pill-tab-pane");
        VBox.setVgrow(tabPane, Priority.ALWAYS); // Ensure TabPane grows

        // Load CSS
        try {
            root.getStylesheets().add(getClass().getResource("/styles/placement.css").toExternalForm());
        } catch (Exception e) {
            // Ignore if missing
        }

        boolean hasTabs = false;

        // Tabs based on Role/Permission

        // Admin/Manager View
        if (SessionManager.getInstance().hasPermission("MANAGE_PLACEMENTS")
                || "System Administrator".equalsIgnoreCase(userRole) || "Admin".equalsIgnoreCase(userRole)) {
            tabPane.getTabs().addAll(
                    createTab("Companies", createCompaniesView()),
                    createTab("Drives", createDrivesManagementView()));
            hasTabs = true;
        }

        // Student/Viewer View
        if (SessionManager.getInstance().hasPermission("VIEW_PLACEMENTS") || "Student".equalsIgnoreCase(userRole)) {
            tabPane.getTabs().addAll(
                    createTab("Upcoming Drives", createStudentDrivesView()),
                    createTab("My Applications", createStudentApplicationsView()));
            hasTabs = true;
        }

        if (!hasTabs) {
            // Fallback for debugging or unhandled roles
            Label error = new Label("Access Restricted or Unknown Role: " + userRole);
            error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            root.getChildren().add(error);
        } else {
            root.getChildren().addAll(title, tabPane);
        }
    }

    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    // --- Admin Views ---

    private VBox createCompaniesView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.getStyleClass().add("glass-card");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search companies...");
        searchField.setPrefWidth(250);

        statsLabel = new Label("Total Companies: 0");
        statsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add Company");
        addBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        header.getChildren().addAll(searchField, spacer, statsLabel, addBtn, refreshBtn);

        TableView<PlacementCompany> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            String searchText = newVal.toLowerCase();
            List<PlacementCompany> all = placementDAO.getAllCompanies();
            table.getItems().setAll(all.stream()
                .filter(c -> searchText.isEmpty() ||
                    c.getName().toLowerCase().contains(searchText) ||
                    c.getIndustry().toLowerCase().contains(searchText))
                .collect(java.util.stream.Collectors.toList()));
            statsLabel.setText(String.format("Total Companies: %d", table.getItems().size()));
        });

        addBtn.setOnAction(e -> {
            showAddCompanyDialog();
            table.getItems().setAll(placementDAO.getAllCompanies());
            searchField.clear();
        });

        refreshBtn.setOnAction(e -> {
            table.getItems().setAll(placementDAO.getAllCompanies());
            searchField.clear();
        });

        TableColumn<PlacementCompany, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<PlacementCompany, String> indCol = new TableColumn<>("Industry");
        indCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIndustry()));

        TableColumn<PlacementCompany, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContactPerson()));

        TableColumn<PlacementCompany, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<PlacementCompany, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));

        // Delete Action
        TableColumn<PlacementCompany, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.getStyleClass().add("small-button");
                btn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                btn.setOnAction(event -> {
                    PlacementCompany comp = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    DialogUtils.styleDialog(alert);
                    alert.setTitle("Confirm Delete");
                    alert.setHeaderText("Delete Company?");
                    alert.setContentText("Are you sure you want to delete " + comp.getName() + "?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            placementDAO.deleteCompany(comp.getId());
                            getTableView().getItems().remove(comp);
                            DialogUtils.showSuccess("Deleted", "Company deleted.");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btn);
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(indCol);
        table.getColumns().add(contactCol);
        table.getColumns().add(emailCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(actionCol);
        table.getItems().addAll(placementDAO.getAllCompanies());
        statsLabel.setText(String.format("Total Companies: %d", table.getItems().size()));

        vbox.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return vbox;
    }

    private void showAddCompanyDialog() {
        Dialog<PlacementCompany> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Add Company");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField indField = new TextField();
        TextField contactField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        TextField webField = new TextField();

        DialogUtils.addFormRow(grid, "Name:", nameField, 0);
        DialogUtils.addFormRow(grid, "Industry:", indField, 1);
        DialogUtils.addFormRow(grid, "Contact Person:", contactField, 2);
        DialogUtils.addFormRow(grid, "Email:", emailField, 3);
        DialogUtils.addFormRow(grid, "Phone:", phoneField, 4);
        DialogUtils.addFormRow(grid, "Website:", webField, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return new PlacementCompany(0, nameField.getText(), indField.getText(), contactField.getText(),
                        emailField.getText(), phoneField.getText(), webField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            placementDAO.addCompany(c);
            DialogUtils.showSuccess("Success", "Company registered successfully.");
            // Ideally trigger refresh here
        });
    }

    private VBox createDrivesManagementView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));
        vbox.getStyleClass().add("glass-card");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField driveSearch = new TextField();
        driveSearch.setPromptText("Search drives...");
        driveSearch.setPrefWidth(250);

        companyFilter = new ComboBox<>();
        companyFilter.getItems().add("All Companies");
        companyFilter.getItems().addAll(placementDAO.getAllCompanies().stream()
            .map(PlacementCompany::getName)
            .collect(java.util.stream.Collectors.toList()));
        companyFilter.setValue("All Companies");

        Label driveStats = new Label("Total Drives: 0");
        driveStats.setStyle("-fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Schedule Drive");
        addBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        header.getChildren().addAll(driveSearch, companyFilter, spacer, driveStats, addBtn, refreshBtn);

        TableView<PlacementDrive> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        Runnable filterDrives = () -> {
            String searchText = driveSearch.getText().toLowerCase();
            String company = companyFilter.getValue();
            List<PlacementDrive> all = placementDAO.getAllDrives();
            table.getItems().setAll(all.stream()
                .filter(d -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                        d.getCompanyName().toLowerCase().contains(searchText) ||
                        d.getJobRole().toLowerCase().contains(searchText);
                    boolean matchesCompany = company.equals("All Companies") || d.getCompanyName().equals(company);
                    return matchesSearch && matchesCompany;
                })
                .collect(java.util.stream.Collectors.toList()));
            driveStats.setText(String.format("Total Drives: %d", table.getItems().size()));
        };

        driveSearch.textProperty().addListener((obs, old, newVal) -> filterDrives.run());
        companyFilter.setOnAction(e -> filterDrives.run());

        addBtn.setOnAction(e -> {
            showAddDriveDialog();
            filterDrives.run();
        });

        refreshBtn.setOnAction(e -> filterDrives.run());

        TableColumn<PlacementDrive, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompanyName()));

        TableColumn<PlacementDrive, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobRole()));

        TableColumn<PlacementDrive, Number> pkgCol = new TableColumn<>("Package (LPA)");
        pkgCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPackageLpa()));

        TableColumn<PlacementDrive, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDriveDate().toString()));

        TableColumn<PlacementDrive, String> deadCol = new TableColumn<>("Deadline");
        deadCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDeadline().toString()));

        // Action Column: View Applicants & Delete
        TableColumn<PlacementDrive, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button appBtn = new Button("Applicants");
            private final Button delBtn = new Button("Delete");
            private final HBox pane = new HBox(5, appBtn, delBtn);

            {
                appBtn.getStyleClass().add("small-button");
                appBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                appBtn.setOnAction(event -> {
                    PlacementDrive drive = getTableView().getItems().get(getIndex());
                    showApplicantsDialog(drive);
                });

                delBtn.getStyleClass().add("small-button");
                delBtn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                delBtn.setOnAction(event -> {
                    PlacementDrive drive = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    DialogUtils.styleDialog(alert);
                    alert.setTitle("Confirm Delete");
                    alert.setHeaderText("Delete Drive?");
                    alert.setContentText("Are you sure you want to delete drive for " + drive.getJobRole() + "?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            placementDAO.deleteDrive(drive.getId());
                            getTableView().getItems().remove(drive);
                            DialogUtils.showSuccess("Deleted", "Drive deleted.");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });

        table.getColumns().add(compCol);
        table.getColumns().add(roleCol);
        table.getColumns().add(pkgCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(deadCol);
        table.getColumns().add(actionCol);
        table.getItems().addAll(placementDAO.getAllDrives());
        driveStats.setText(String.format("Total Drives: %d", table.getItems().size()));

        vbox.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return vbox;
    }

    private void showApplicantsDialog(PlacementDrive drive) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Applicants for " + drive.getCompanyName());
        dialog.setHeaderText("Candidates for " + drive.getJobRole());

        // Apply Dark Theme
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
        dialogPane.getStylesheets().add(getClass().getResource("/styles/placement.css").toExternalForm()); // Add Table
                                                                                                           // Styles
        dialogPane.getStyleClass().add("my-dialog");
        dialogPane.setStyle("-fx-background-color: #1e293b; -fx-text-fill: white;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0f172a; -fx-text-fill: white;");
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        TableView<PlacementApplication> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<PlacementApplication, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));

        TableColumn<PlacementApplication, String> dateCol = new TableColumn<>("Applied On");
        dateCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getAppliedAt().toLocalDate().toString()));

        TableColumn<PlacementApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    switch (item) {
                        case "SELECTED":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "SHORTLISTED":
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        // Update Status Action
        TableColumn<PlacementApplication, Void> updateCol = new TableColumn<>("Update Status");
        updateCol.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            {
                statusCombo.getItems().addAll("APPLIED", "SHORTLISTED", "SELECTED", "REJECTED");
                statusCombo.setStyle("-fx-font-size: 11px; -fx-pref-width: 110px;");
                statusCombo.setOnAction(e -> {
                    PlacementApplication app = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (newStatus != null && !newStatus.equals(app.getStatus())) {
                        placementDAO.updateApplicationStatus(app.getId(), newStatus);
                        app.setStatus(newStatus);
                        getTableView().refresh(); // Refresh to update colors
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PlacementApplication app = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(app.getStatus());
                    setGraphic(statusCombo);
                }
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(updateCol);
        table.getItems().addAll(placementDAO.getApplicationsForDrive(drive.getId()));

        VBox content = new VBox(10, table);
        content.setPadding(new Insets(10));
        content.setPrefSize(600, 400);

        dialogPane.setContent(content);
        dialog.showAndWait();
    }

    private void showAddDriveDialog() {
        Dialog<PlacementDrive> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Schedule Drive");

        ButtonType saveBtn = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        ComboBox<String> compNameCombo = new ComboBox<>();
        List<PlacementCompany> companies = placementDAO.getAllCompanies();
        for (PlacementCompany c : companies)
            compNameCombo.getItems().add(c.getName() + " [" + c.getId() + "]");
        compNameCombo.setMaxWidth(Double.MAX_VALUE);

        TextField roleField = new TextField();
        TextField pkgField = new TextField();
        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        DatePicker deadPicker = new DatePicker();
        deadPicker.setMaxWidth(Double.MAX_VALUE);

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);
        descArea.setStyle(
                "-fx-control-inner-background: #1e293b; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");

        DialogUtils.addFormRow(grid, "Company:", compNameCombo, 0);
        DialogUtils.addFormRow(grid, "Job Role:", roleField, 1);
        DialogUtils.addFormRow(grid, "Package (LPA):", pkgField, 2);
        DialogUtils.addFormRow(grid, "Drive Date:", datePicker, 3);
        DialogUtils.addFormRow(grid, "Deadline:", deadPicker, 4);

        Label descLbl = new Label("Description:");
        descLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        grid.add(descLbl, 0, 5);
        grid.add(descArea, 1, 5);
        GridPane.setValignment(descLbl, javafx.geometry.VPos.TOP);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String selComp = compNameCombo.getValue();
                if (selComp == null)
                    return null;
                int compId = Integer
                        .parseInt(selComp.substring(selComp.lastIndexOf('[') + 1, selComp.lastIndexOf(']')));
                try {
                    return new PlacementDrive(0, compId, roleField.getText(), Double.parseDouble(pkgField.getText()),
                            descArea.getText(), datePicker.getValue(), deadPicker.getValue(), "");
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(d -> {
            placementDAO.addDrive(d);
            DialogUtils.showSuccess("Success", "Drive scheduled successfully.");
            // Ideally trigger refresh
        });
    }

    // --- Student Views ---

    private VBox createStudentDrivesView() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));

        Label info = new Label("Upcoming Placement Drives");
        info.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Use FlowPane for responsive layout
        FlowPane flow = new FlowPane();
        flow.setHgap(20);
        flow.setVgap(20);
        flow.setPadding(new Insets(10, 0, 0, 0));

        List<PlacementDrive> drives = placementDAO.getUpcomingDrives();
        for (PlacementDrive d : drives) {
            flow.getChildren().add(createDriveCard(d));
        }

        if (drives.isEmpty()) {
            Label empty = new Label("No upcoming drives at the moment.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            flow.getChildren().add(empty);
        }

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPannable(true);
        VBox.setVgrow(scroll, Priority.ALWAYS); // Critical for layout to expand

        vbox.getChildren().addAll(info, scroll);
        return vbox;
    }

    private VBox createDriveCard(PlacementDrive d) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.8); -fx-background-radius: 12; -fx-padding: 20; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        card.setPrefWidth(280);
        card.setMinWidth(280);

        Label comp = new Label(d.getCompanyName());
        comp.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;"); // Blue title

        Label role = new Label(d.getJobRole());
        role.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: 600;");

        Label pkg = new Label("₹ " + d.getPackageLpa() + " LPA");
        pkg.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #22c55e;"); // Green package

        Label date = new Label("Date: " + d.getDriveDate());
        date.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button applyBtn = new Button("Apply Now");
        applyBtn.getStyleClass().add("accent-button");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        if (placementDAO.hasApplied(d.getId(), userId)) {
            applyBtn.setText("Applied");
            applyBtn.setDisable(true);
            applyBtn.setStyle("-fx-background-color: #475569; -fx-opacity: 0.7;");
        } else {
            applyBtn.setOnAction(e -> {
                placementDAO.applyForDrive(d.getId(), userId);
                applyBtn.setText("Applied");
                applyBtn.setDisable(true);
                applyBtn.setStyle("-fx-background-color: #475569; -fx-opacity: 0.7;");
                DialogUtils.showSuccess("Success", "Successfully applied for " + d.getCompanyName());
                
                // Refresh applications table if it exists
                TableView<PlacementApplication> appTable = (TableView<PlacementApplication>) root.lookupAll(".table-view").stream()
                    .filter(node -> node.getParent() != null && node.getParent().getParent() instanceof VBox)
                    .findFirst().orElse(null);
                if (appTable != null) {
                    appTable.getItems().setAll(placementDAO.getApplicationsForStudent(userId));
                }
            });
        }

        applyBtn.setOnMouseEntered(e -> {
            if (!applyBtn.isDisabled())
                applyBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        });
        applyBtn.setOnMouseExited(e -> {
            if (!applyBtn.isDisabled())
                applyBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        });

        card.getChildren().addAll(comp, role, pkg, date, spacer, applyBtn);
        return card;
    }

    private VBox createStudentApplicationsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        Label title = new Label("My Applications");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20;");

        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, refreshBtn);

        TableView<PlacementApplication> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        refreshBtn.setOnAction(e -> {
            table.getItems().setAll(placementDAO.getApplicationsForStudent(userId));
        });

        TableColumn<PlacementApplication, String> compCol = new TableColumn<>("Company");
        compCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCompanyName()));

        TableColumn<PlacementApplication, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDriveTitle()));

        TableColumn<PlacementApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold;");
                    switch (item) {
                        case "SELECTED":
                            setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                            break;
                        case "SHORTLISTED":
                            setStyle("-fx-text-fill: #eab308; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
                            break; // APPLIED
                    }
                }
            }
        });

        TableColumn<PlacementApplication, String> dateCol = new TableColumn<>("Applied On");
        dateCol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getAppliedAt().toLocalDate().toString()));

        // Action Column for Withdraw
        TableColumn<PlacementApplication, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Withdraw");

            {
                btn.getStyleClass().add("small-button"); // Assuming this style exists or just style inline
                btn.setStyle(
                        "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5 10;");
                btn.setOnAction(event -> {
                    PlacementApplication app = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    DialogUtils.styleDialog(alert);
                    alert.setTitle("Confirm Withdraw");
                    alert.setHeaderText("Withdraw Application?");
                    alert.setContentText(
                            "Are you sure you want to withdraw your application for " + app.getCompanyName() + "?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            placementDAO.withdrawApplication(app.getId());
                            getTableView().getItems().remove(app);
                            DialogUtils.showSuccess("Withdrawn", "Application withdrawn successfully.");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PlacementApplication app = getTableView().getItems().get(getIndex());
                    if ("APPLIED".equals(app.getStatus())) {
                        setGraphic(btn);
                    } else {
                        setGraphic(null); // Can't withdraw if processed
                    }
                }
            }
        });

        table.getColumns().add(compCol);
        table.getColumns().add(roleCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(actionCol);
        table.getItems().addAll(placementDAO.getApplicationsForStudent(userId));

        vbox.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return vbox;
    }

    public VBox getView() {
        return root;
    }
}
