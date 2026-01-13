package com.college.fx.views;

import com.college.dao.VisitorDAO;
import com.college.models.Visitor;
import com.college.models.VisitorLog;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;

public class VisitorView {

    private VBox root;
    private VisitorDAO visitorDAO;
    private TabPane tabPane;
    private TableView<VisitorLog> activeTable;
    private TableView<VisitorLog> historyTable;

    // Form Fields
    private TextField phoneSearchField;
    private TextField nameField;
    private TextField emailField;
    private ComboBox<String> idProofTypeCombo;
    private TextField idProofNumField;
    private TextField purposeField;
    private TextField personToMeetField;
    private TextField gateField;

    private int currentVisitorId = -1; // -1 means new visitor

    public VisitorView() {
        visitorDAO = new VisitorDAO();
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: transparent;");

        // Header
        Label title = new Label("Visitor Management");
        title.getStyleClass().add("page-title");
        title.setStyle(
                "-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");

        tabPane = new TabPane();
        tabPane.getStyleClass().add("placement-tabs"); // Reuse existing clean tab styles if possible or standard

        Tab entryTab = new Tab("Visitor Entry", createEntryView());
        entryTab.setClosable(false);

        Tab activeTab = new Tab("Active Visitors", createActiveVisitorsView());
        activeTab.setClosable(false);
        activeTab.setOnSelectionChanged(e -> {
            if (activeTab.isSelected())
                refreshActiveTable();
        });

        tabPane.getTabs().addAll(entryTab, activeTab);

        // History Tab for Admins or detailed view
        if (SessionManager.getInstance().isAdmin() || SessionManager.getInstance().hasPermission("MANAGE_VISITORS")) {
            Tab historyTab = new Tab("Visitor History", createHistoryView());
            historyTab.setClosable(false);
            historyTab.setOnSelectionChanged(e -> {
                if (historyTab.isSelected())
                    refreshHistoryTable();
            });
            tabPane.getTabs().add(historyTab);
        }

        root.getChildren().addAll(title, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
    }

    public VBox getView() {
        return root;
    }

    private VBox createEntryView() {
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.5); -fx-background-radius: 10; -fx-padding: 30;");

        // Search Section
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        Label searchLbl = new Label("Search by Phone:");
        searchLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
        phoneSearchField = new TextField();
        phoneSearchField.setPromptText("Enter Phone Number");
        phoneSearchField.getStyleClass().add("search-field");
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("accent-button");
        searchBtn.setOnAction(e -> searchVisitor());
        searchBox.getChildren().addAll(searchLbl, phoneSearchField, searchBtn);

        // Visitor Details Form
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        nameField = new TextField();
        emailField = new TextField(); // Optional
        idProofTypeCombo = new ComboBox<>();
        idProofTypeCombo.getItems().addAll("Aadhaar", "PAN", "Driving License", "Voter ID", "Other");
        idProofNumField = new TextField();

        addFormRow(grid, "Name:", nameField, 0);
        addFormRow(grid, "Email (Opt):", emailField, 1);
        addFormRow(grid, "ID Proof Type:", idProofTypeCombo, 2);
        addFormRow(grid, "ID Proof No:", idProofNumField, 3);

        // Use TitledPane or Separator for logical grouping
        Label visitDetailsLbl = new Label("Visit Details");
        visitDetailsLbl
                .setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");

        purposeField = new TextField();
        personToMeetField = new TextField();
        gateField = new TextField("Main Gate"); // Default

        GridPane logGrid = new GridPane();
        logGrid.setHgap(20);
        logGrid.setVgap(15);

        addFormRow(logGrid, "Purpose:", purposeField, 0);
        addFormRow(logGrid, "Person to Meet:", personToMeetField, 1);
        addFormRow(logGrid, "Gate No:", gateField, 2);

        // Actions
        Button checkInBtn = new Button("Check In Visitor");
        checkInBtn.getStyleClass().add("accent-button");
        checkInBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 30;");
        checkInBtn.setOnAction(e -> handleCheckIn());

        Button clearBtn = new Button("Clear Form");
        clearBtn.getStyleClass().add("secondary-button");
        clearBtn.setOnAction(e -> clearForm());

        HBox actions = new HBox(15, checkInBtn, clearBtn);
        actions.setPadding(new Insets(20, 0, 0, 0));

        vbox.getChildren().addAll(searchBox, new Separator(), grid, new Separator(), visitDetailsLbl, logGrid, actions);
        return vbox;
    }

    private void addFormRow(GridPane grid, String labelText, Control field, int row) {
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");
        field.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 5;");
        field.setPrefWidth(250);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private VBox createActiveVisitorsView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        activeTable = new TableView<>();
        activeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN); // Or flex if available

        setupLogTable(activeTable, true);

        vbox.getChildren().add(activeTable);
        VBox.setVgrow(activeTable, Priority.ALWAYS);
        return vbox;
    }

    private VBox createHistoryView() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15));

        historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        setupLogTable(historyTable, false);

        vbox.getChildren().add(historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        return vbox;
    }

    private void setupLogTable(TableView<VisitorLog> table, boolean addAction) {
        TableColumn<VisitorLog, String> nameCol = new TableColumn<>("Visitor Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVisitorName()));

        TableColumn<VisitorLog, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getVisitorPhone()));

        TableColumn<VisitorLog, String> inTimeCol = new TableColumn<>("Entry Time");
        inTimeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEntryTime().format(DateTimeFormatter.ofPattern("dd-MM HH:mm"))));

        TableColumn<VisitorLog, String> purposeCol = new TableColumn<>("Purpose");
        purposeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPurpose()));

        TableColumn<VisitorLog, String> personCol = new TableColumn<>("Visiting");
        personCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersonToMeet()));

        table.getColumns().add(nameCol);
        table.getColumns().add(phoneCol);
        table.getColumns().add(inTimeCol);
        table.getColumns().add(purposeCol);
        table.getColumns().add(personCol);

        if (!addAction) {
            TableColumn<VisitorLog, String> outTimeCol = new TableColumn<>("Exit Time");
            outTimeCol.setCellValueFactory(data -> {
                if (data.getValue().getExitTime() != null) {
                    return new SimpleStringProperty(
                            data.getValue().getExitTime().format(DateTimeFormatter.ofPattern("dd-MM HH:mm")));
                }
                return new SimpleStringProperty("-");
            });
            table.getColumns().add(outTimeCol);
        }

        if (addAction) {
            TableColumn<VisitorLog, Void> actionCol = new TableColumn<>("Action");
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Check Out");
                {
                    btn.getStyleClass().add("small-button");
                    btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;");
                    btn.setOnAction(event -> {
                        VisitorLog log = getTableView().getItems().get(getIndex());
                        visitorDAO.logExit(log.getId());
                        getTableView().getItems().remove(log);
                        DialogUtils.showSuccess("Success", "Visitor checked out.");
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
            table.getColumns().add(actionCol);
        }
    }

    // --- Logic ---

    private void searchVisitor() {
        String phone = phoneSearchField.getText().trim();
        if (phone.isEmpty()) {
            DialogUtils.showError("Error", "Please enter a phone number.");
            return;
        }

        Visitor v = visitorDAO.getVisitorByPhone(phone);
        if (v != null) {
            currentVisitorId = v.getId();
            nameField.setText(v.getName());
            emailField.setText(v.getEmail());
            idProofTypeCombo.setValue(v.getIdProofType());
            idProofNumField.setText(v.getIdProofNumber());
            // Disable editing fields that shouldn't change easily or keep them editable
            // but for now let's assume they can edit if details changed
            DialogUtils.showInfo("Found", "Visitor found. ID details auto-filled.");
        } else {
            currentVisitorId = -1;
            DialogUtils.showInfo("Not Found", "New Visitor. Please enter details.");
            clearVisitorFields();
        }
    }

    private void handleCheckIn() {
        String name = nameField.getText();
        String phone = phoneSearchField.getText(); // Use the search field as the primary phone
        String purpose = purposeField.getText();

        if (name.isEmpty() || phone.isEmpty() || purpose.isEmpty()) {
            DialogUtils.showError("Required", "Name, Phone and Purpose are required.");
            return;
        }

        if (currentVisitorId == -1) {
            // New Visitor - Create first
            Visitor v = new Visitor(0, name, phone, emailField.getText(),
                    idProofTypeCombo.getValue(), idProofNumField.getText(), null);
            currentVisitorId = visitorDAO.addVisitor(v);
            if (currentVisitorId == -1) {
                DialogUtils.showError("Error", "Failed to register visitor.");
                return;
            }
        }

        visitorDAO.logEntry(currentVisitorId, purpose, personToMeetField.getText(), gateField.getText());
        DialogUtils.showSuccess("Checked In", "Visitor checked in successfully.");
        clearForm();
    }

    private void clearVisitorFields() {
        nameField.clear();
        emailField.clear();
        idProofTypeCombo.setValue(null);
        idProofNumField.clear();
    }

    private void clearForm() {
        phoneSearchField.clear();
        clearVisitorFields();
        purposeField.clear();
        personToMeetField.clear();
        gateField.setText("Main Gate");
        currentVisitorId = -1;
    }

    private void refreshActiveTable() {
        activeTable.getItems().setAll(visitorDAO.getActiveVisitors());
    }

    private void refreshHistoryTable() {
        historyTable.getItems().setAll(visitorDAO.getAllVisitorLogs());
    }
}
