package com.college.fx.views;

import com.college.dao.HostelDAO;
import com.college.dao.WardenDAO;
import com.college.dao.StudentDAO;
import com.college.models.Hostel;
import com.college.models.HostelAllocation;
import com.college.models.Room;
import com.college.models.Warden;
import com.college.models.Student;

import com.college.utils.SearchableStudentComboBox;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ButtonBar.ButtonData;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JavaFX Hostel Management View
 */
public class HostelManagementView {

    private VBox root;
    private HostelDAO hostelDAO;
    private WardenDAO wardenDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    // Data lists
    private ObservableList<HostelAllocation> allocationData;
    private ObservableList<Hostel> hostelData;
    private ObservableList<Room> roomData;
    private ObservableList<Warden> wardenData;

    // Components
    private TableView<HostelAllocation> allocationTable;
    private TableView<Hostel> hostelTable;
    private TableView<Room> roomTable;

    public HostelManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.hostelDAO = new HostelDAO();
        this.wardenDAO = new WardenDAO();
        this.studentDAO = new StudentDAO();
        this.allocationData = FXCollections.observableArrayList();
        this.hostelData = FXCollections.observableArrayList();
        this.roomData = FXCollections.observableArrayList();
        this.wardenData = FXCollections.observableArrayList();

        createView();
        loadData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label title = new Label("Hostel Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));
        title.setPadding(new Insets(0, 0, 10, 10));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        if (role.equals("STUDENT")) {
            // Student view - show only their allocation
            Tab myAllocationTab = new Tab("My Hostel");
            myAllocationTab.setContent(createStudentAllocationView());
            myAllocationTab.setClosable(false);
            tabPane.getTabs().add(myAllocationTab);
        } else if (role.equals("WARDEN")) {
            // Warden view - show their hostel allocations and rooms only
            Tab allocTab = new Tab("Hostel Allocations");
            allocTab.setContent(createAllocationTab());
            allocTab.setClosable(false);

            Tab roomTab = new Tab("Rooms");
            roomTab.setContent(createRoomTab());
            roomTab.setClosable(false);

            Tab attendanceTab = new Tab("Hostel Attendance");
            attendanceTab.setContent(createWardenAttendanceTab());
            attendanceTab.setClosable(false);

            tabPane.getTabs().addAll(allocTab, roomTab, attendanceTab);
        } else {
            // Admin/Faculty view - full access
            Tab allocTab = new Tab("Allocations");
            allocTab.setContent(createAllocationTab());
            allocTab.setClosable(false);

            Tab hostelTab = new Tab("Hostels");
            hostelTab.setContent(createHostelTab());
            hostelTab.setClosable(false);

            Tab roomTab = new Tab("Rooms");
            roomTab.setContent(createRoomTab());
            roomTab.setClosable(false);

            Tab wardenTab = new Tab("Wardens");
            wardenTab.setContent(createWardenTab());
            wardenTab.setClosable(false);

            tabPane.getTabs().addAll(allocTab, hostelTab, roomTab, wardenTab);
        }
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        root.getChildren().addAll(title, tabPane);
    }

    // ==================== STUDENT VIEW ====================

    private VBox createStudentAllocationView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);

        Student student = studentDAO.getStudentByUserId(userId);
        if (student != null) {
            List<HostelAllocation> allocs = hostelDAO.getAllocationsByStudent(student.getId());
            if (!allocs.isEmpty()) {
                HostelAllocation current = allocs.get(0); // Assuming active allocation is first or only
                if ("ACTIVE".equalsIgnoreCase(current.getStatus())) {
                    VBox card = new VBox(15);
                    card.setMaxWidth(500);
                    card.setPadding(new Insets(30));
                    card.setStyle(
                            "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");

                    Label statusTitle = new Label("Current Accommodation");
                    statusTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    statusTitle.setTextFill(Color.web("#14b8a6"));

                    GridPane grid = new GridPane();
                    grid.setHgap(15);
                    grid.setVgap(15);

                    addDetailRow(grid, "Hostel:", current.getHostelName(), 0);
                    addDetailRow(grid, "Room No:", current.getRoomNumber(), 1);
                    addDetailRow(grid, "Allocated On:", current.getCheckInDate().toString(), 2);

                    card.getChildren().addAll(statusTitle, new Separator(), grid);
                    content.getChildren().add(card);
                    return content;
                }
            }
        }

        Label noAllocLabel = new Label("No hostel room allocated assigned.");
        noAllocLabel.setFont(Font.font("Segoe UI", 16));
        noAllocLabel.setTextFill(Color.web("#64748b"));
        content.getChildren().add(noAllocLabel);

        return content;
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web("#64748b"));

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", 14));
        val.setTextFill(Color.web("#0f172a"));

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // ==================== ALLOCATIONS TAB ====================

    private VBox createAllocationTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12 12;");

        HBox toolbar = new HBox(15);
        Button allocateBtn = createButton("New Allocation", "#22c55e");
        allocateBtn.setOnAction(e -> showAllocationDialog());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(allocateBtn, refreshBtn);

        allocationTable = new TableView<>();
        allocationTable.setItems(allocationData);

        TableColumn<HostelAllocation, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(200);

        TableColumn<HostelAllocation, String> hostelCol = new TableColumn<>("Hostel");
        hostelCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHostelName()));
        hostelCol.setPrefWidth(150);

        TableColumn<HostelAllocation, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        roomCol.setPrefWidth(100);

        TableColumn<HostelAllocation, String> dateCol = new TableColumn<>("Allocated Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCheckInDate().toString()));
        dateCol.setPrefWidth(120);

        TableColumn<HostelAllocation, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button vacateBtn = new Button("Vacate");

            {
                vacateBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10;");
                vacateBtn.setOnAction(event -> vacateRoom(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : vacateBtn);
            }
        });

        allocationTable.getColumns().addAll(studentCol, hostelCol, roomCol, dateCol, actionCol);
        VBox.setVgrow(allocationTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, allocationTable);
        return content;
    }

    // ==================== HOSTEL/ROOM TABS (Simplified) ====================

    private VBox createHostelTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        HBox toolbar = new HBox(15);
        Button addHostelBtn = createButton("Add Hostel", "#22c55e");
        addHostelBtn.setOnAction(e -> showAddHostelDialog());

        Button deleteHostelBtn = createButton("Delete Hostel", "#ef4444");
        deleteHostelBtn.setOnAction(e -> deleteHostel());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addHostelBtn, deleteHostelBtn, refreshBtn);

        hostelTable = new TableView<>();
        hostelTable.setItems(hostelData);

        TableColumn<Hostel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

        TableColumn<Hostel, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));

        TableColumn<Hostel, String> wardenCol = new TableColumn<>("Warden");
        wardenCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWardenName()));

        hostelTable.getColumns().addAll(nameCol, typeCol, wardenCol);
        VBox.setVgrow(hostelTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, hostelTable);
        return content;
    }

    private VBox createRoomTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        HBox toolbar = new HBox(15);
        Button addRoomBtn = createButton("Add Room", "#22c55e");
        addRoomBtn.setOnAction(e -> showAddRoomDialog());

        Button deleteRoomBtn = createButton("Delete Room", "#ef4444");
        deleteRoomBtn.setOnAction(e -> deleteRoom());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addRoomBtn, deleteRoomBtn, refreshBtn);

        roomTable = new TableView<>();
        roomTable.setItems(roomData);

        TableColumn<Room, String> numCol = new TableColumn<>("Room No");
        numCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));

        TableColumn<Room, String> hostelCol = new TableColumn<>("Hostel");
        hostelCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getHostelId())));

        TableColumn<Room, String> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOccupiedCount() + "/" + data.getValue().getCapacity()));

        roomTable.getColumns().addAll(numCol, hostelCol, capCol);
        VBox.setVgrow(roomTable, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, roomTable);
        return content;
    }

    // ==================== ACTIONS ====================

    private void loadData() {
        if (role.equals("STUDENT")) {
            // Students don't need to load list data
            return;
        } else if (role.equals("WARDEN")) {
            // Wardens see only their assigned hostel
            Warden warden = wardenDAO.getWardenByUserId(userId);
            if (warden != null && warden.getHostelId() > 0) {
                // Filter allocations by hostel
                List<HostelAllocation> allAllocs = hostelDAO.getAllActiveAllocations();
                List<Room> allRooms = hostelDAO.getAllRooms();
                allocationData.clear();
                for (HostelAllocation alloc : allAllocs) {
                    for (Room r : allRooms) {
                        if (r.getId() == alloc.getRoomId() && r.getHostelId() == warden.getHostelId()) {
                            allocationData.add(alloc);
                            break;
                        }
                    }
                }

                hostelData.clear();
                // Load only their hostel
                List<Hostel> allHostels = hostelDAO.getAllHostels();
                for (Hostel h : allHostels) {
                    if (h.getId() == warden.getHostelId()) {
                        hostelData.add(h);
                        break;
                    }
                }

                // Filter rooms by hostel
                roomData.clear();
                for (Room r : allRooms) {
                    if (r.getHostelId() == warden.getHostelId()) {
                        roomData.add(r);
                    }
                }
            }
        } else {
            // Admin/Faculty see all data
            allocationData.clear();
            allocationData.addAll(hostelDAO.getAllActiveAllocations());

            hostelData.clear();
            hostelData.addAll(hostelDAO.getAllHostels());

            roomData.clear();
            roomData.addAll(hostelDAO.getAllRooms());

            wardenData.clear();
            wardenData.addAll(wardenDAO.getAllWardens());
        }
    }

    private void showAllocationDialog() {
        Dialog<HostelAllocation> dialog = new Dialog<>();
        dialog.setTitle("New Allocation");
        dialog.setHeaderText("Allocate Room to Student");
        ButtonType allocBtn = new ButtonType("Allocate", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(allocBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        SearchableStudentComboBox studentSelector = new SearchableStudentComboBox(studentDAO.getAllStudents());

        ComboBox<Hostel> hostelCombo = new ComboBox<>();
        hostelCombo.setPrefWidth(250);
        hostelCombo.getItems().addAll(hostelDAO.getAllHostels());

        ComboBox<Room> roomCombo = new ComboBox<>();
        roomCombo.setPrefWidth(250);

        hostelCombo.setOnAction(e -> {
            Hostel selectedHostel = hostelCombo.getValue();
            if (selectedHostel != null) {
                roomCombo.getItems().setAll(hostelDAO.getAvailableRooms(selectedHostel.getId()));
            }
        });

        DatePicker checkInDate = new DatePicker(java.time.LocalDate.now());

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentSelector, 1, 0);
        grid.add(new Label("Hostel:"), 0, 1);
        grid.add(hostelCombo, 1, 1);
        grid.add(new Label("Room:"), 0, 2);
        grid.add(roomCombo, 1, 2);
        grid.add(new Label("Check-In:"), 0, 3);
        grid.add(checkInDate, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == allocBtn && roomCombo.getValue() != null && studentSelector.getSelectedStudent() != null) {
                HostelAllocation alloc = new HostelAllocation();
                alloc.setStudentId(studentSelector.getSelectedStudent().getId());
                alloc.setRoomId(roomCombo.getValue().getId());
                if (checkInDate.getValue() != null) {
                    alloc.setCheckInDate(
                            Date.from(checkInDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                alloc.setAllocatedBy(userId);

                if (hostelDAO.allocateRoom(alloc)) {
                    return alloc;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            loadData();
            // Show alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Room allocated successfully!");
            alert.showAndWait();
        });
    }

    private void vacateRoom(HostelAllocation allocation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Vacate Room");
        alert.setHeaderText("Vacate " + allocation.getStudentName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (hostelDAO.vacateRoom(allocation.getId())) {
                loadData();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void deleteHostel() {
        Hostel selected = hostelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a hostel to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Hostel");
        confirm.setHeaderText("Delete hostel: " + selected.getName() + "?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Call DAO delete method when implemented
            showAlert("Info", "Delete hostel functionality needs DAO implementation.");
            // hostelDAO.deleteHostel(selected.getId());
            // loadData();
        }
    }

    private void deleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a room to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Room");
        confirm.setHeaderText("Delete room: " + selected.getRoomNumber() + "?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Call DAO delete method when implemented
            showAlert("Info", "Delete room functionality needs DAO implementation.");
            // hostelDAO.deleteRoom(selected.getId());
            // loadData();
        }
    }

    private void showAddHostelDialog() {
        Dialog<Hostel> dialog = new Dialog<>();
        dialog.setTitle("Add Hostel");
        dialog.setHeaderText("Create New Hostel");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Hostel Name");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("BOYS", "GIRLS");
        typeCombo.setValue("BOYS");
        TextField wardenField = new TextField();
        wardenField.setPromptText("Warden Name");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Warden:"), 0, 2);
        grid.add(wardenField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && !nameField.getText().trim().isEmpty()) {
                Hostel h = new Hostel();
                h.setName(nameField.getText());
                h.setType(typeCombo.getValue());
                h.setWardenName(wardenField.getText());
                if (hostelDAO.addHostel(h)) {
                    return h;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(h -> {
            loadData();
            showAlert("Success", "Hostel added successfully!");
        });
    }

    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add Room");
        dialog.setHeaderText("Create New Room");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField roomNumField = new TextField();
        roomNumField.setPromptText("Room Number");
        ComboBox<Hostel> hostelCombo = new ComboBox<>();
        hostelCombo.getItems().addAll(hostelDAO.getAllHostels());
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumField, 1, 0);
        grid.add(new Label("Hostel:"), 0, 1);
        grid.add(hostelCombo, 1, 1);
        grid.add(new Label("Capacity:"), 0, 2);
        grid.add(capacityField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && hostelCombo.getValue() != null) {
                try {
                    Room r = new Room();
                    r.setRoomNumber(roomNumField.getText());
                    r.setHostelId(hostelCombo.getValue().getId());
                    r.setCapacity(Integer.parseInt(capacityField.getText()));
                    r.setOccupiedCount(0);
                    if (hostelDAO.addRoom(r)) {
                        return r;
                    }
                } catch (NumberFormatException e) {
                    // Invalid number
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(r -> {
            loadData();
            showAlert("Success", "Room added successfully!");
        });
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");
        return btn;
    }

    private VBox createWardenTab() {
        VBox tab = new VBox(15);
        tab.setPadding(new Insets(15));

        // Warden table
        TableView<Warden> wardenTable = new TableView<>();
        wardenTable.setItems(wardenData);

        TableColumn<Warden, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(150);

        TableColumn<Warden, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        emailCol.setPrefWidth(180);

        TableColumn<Warden, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        phoneCol.setPrefWidth(120);

        TableColumn<Warden, String> hostelCol = new TableColumn<>("Assigned Hostel");
        hostelCol.setCellValueFactory(data -> {
            int hid = data.getValue().getHostelId();
            return new SimpleStringProperty(hid > 0 ? "Hostel ID: " + hid : "-");
        });
        hostelCol.setPrefWidth(150);

        wardenTable.getColumns().addAll(nameCol, emailCol, phoneCol, hostelCol);
        VBox.setVgrow(wardenTable, Priority.ALWAYS);

        // Control buttons
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        Button addWardenBtn = createButton("Add Warden", "#22c55e");
        addWardenBtn.setOnAction(e -> showAddWardenDialog());

        Button editWardenBtn = createButton("Edit Warden", "#3b82f6");
        editWardenBtn.setOnAction(e -> showEditWardenDialog(wardenTable));

        Button deleteWardenBtn = createButton("Delete Warden", "#ef4444");
        deleteWardenBtn.setOnAction(e -> deleteWarden(wardenTable));

        controls.getChildren().addAll(addWardenBtn, editWardenBtn, deleteWardenBtn);

        tab.getChildren().addAll(wardenTable, controls);
        return tab;
    }

    private void showAddWardenDialog() {
        Dialog<Warden> dialog = new Dialog<>();
        dialog.setTitle("Add Warden");
        dialog.setHeaderText("Create New Warden");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Warden Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        ComboBox<Hostel> hostelCombo = new ComboBox<>();
        hostelCombo.getItems().addAll(hostelDAO.getAllHostels());
        hostelCombo.setPromptText("Select Hostel");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Hostel:"), 0, 3);
        grid.add(hostelCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Warden w = new Warden();
                w.setName(nameField.getText());
                w.setEmail(emailField.getText());
                w.setPhone(phoneField.getText());
                if (hostelCombo.getValue() != null) {
                    w.setHostelId(hostelCombo.getValue().getId());
                }

                if (wardenDAO.addWarden(w) > 0) {
                    return w;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(w -> {
            loadWardens(); // Refresh the list
            if (w.getUsername() != null && !w.getUsername().isEmpty()) {
                showAlert("Warden Created",
                        "Warden added successfully!\n\n" +
                                "Login Credentials:\n" +
                                "Username: " + w.getUsername() + "\n" +
                                "Password: password123\n\n" +
                                "Please share these credentials with the warden.");
            } else {
                showAlert("Success", "Warden added successfully!");
            }
        });
    }

    private void showEditWardenDialog(TableView<Warden> table) {
        Warden selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a warden to edit.");
            return;
        }

        Dialog<Warden> dialog = new Dialog<>();
        dialog.setTitle("Edit Warden");
        dialog.setHeaderText("Edit: " + selected.getName());
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField emailField = new TextField(selected.getEmail());
        TextField phoneField = new TextField(selected.getPhone());

        ComboBox<Hostel> hostelCombo = new ComboBox<>();
        hostelCombo.getItems().addAll(hostelDAO.getAllHostels());
        if (selected.getHostelId() > 0) {
            // Find hostel in combo by ID
            for (Hostel h : hostelCombo.getItems()) {
                if (h.getId() == selected.getHostelId()) {
                    hostelCombo.setValue(h);
                    break;
                }
            }
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Hostel:"), 0, 3);
        grid.add(hostelCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                selected.setName(nameField.getText());
                selected.setEmail(emailField.getText());
                selected.setPhone(phoneField.getText());
                if (hostelCombo.getValue() != null) {
                    selected.setHostelId(hostelCombo.getValue().getId());
                }

                if (wardenDAO.updateWarden(selected)) {
                    return selected;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(w -> {
            loadWardens();
            showAlert("Success", "Warden updated successfully!");
        });
    }

    private void deleteWarden(TableView<Warden> table) {
        Warden selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a warden to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Warden");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete warden: " + selected.getName() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (wardenDAO.deleteWarden(selected.getId())) {
                    loadWardens();
                    showAlert("Success", "Warden deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete warden.");
                }
            }
        });
    }

    private void loadWardens() {
        wardenData.clear();
        wardenData.addAll(wardenDAO.getAllWardens());
    }

    private VBox createWardenAttendanceTab() {
        VBox tab = new VBox(15);
        tab.setPadding(new Insets(15));

        Label title = new Label("Hostel Student Attendance");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Get warden's hostel
        Warden warden = wardenDAO.getWardenByUserId(userId);
        if (warden == null || warden.getHostelId() <= 0) {
            Label noHostel = new Label("No hostel assigned to your account.");
            noHostel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
            tab.getChildren().addAll(title, noHostel);
            return tab;
        }

        // Get students in this hostel
        List<HostelAllocation> allAllocs = hostelDAO.getAllActiveAllocations();
        List<Room> allRooms = hostelDAO.getAllRooms();
        List<HostelAllocation> hostelAllocs = new ArrayList<>();
        for (HostelAllocation alloc : allAllocs) {
            for (Room r : allRooms) {
                if (r.getId() == alloc.getRoomId() && r.getHostelId() == warden.getHostelId()) {
                    hostelAllocs.add(alloc);
                    break;
                }
            }
        }

        TableView<Student> studentTable = new TableView<>();
        ObservableList<Student> students = FXCollections.observableArrayList();

        for (HostelAllocation alloc : hostelAllocs) {
            Student s = studentDAO.getStudentById(alloc.getStudentId());
            if (s != null) {
                students.add(s);
            }
        }
        studentTable.setItems(students);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Student, String> enrollCol = new TableColumn<>("Enrollment ID");
        enrollCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        enrollCol.setPrefWidth(150);

        TableColumn<Student, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartment()));
        deptCol.setPrefWidth(150);

        TableColumn<Student, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> {
            for (HostelAllocation a : hostelAllocs) {
                if (a.getStudentId() == data.getValue().getId()) {
                    for (Room r : allRooms) {
                        if (r.getId() == a.getRoomId()) {
                            return new SimpleStringProperty(r.getRoomNumber());
                        }
                    }
                }
            }
            return new SimpleStringProperty("-");
        });
        roomCol.setPrefWidth(100);

        studentTable.getColumns().addAll(nameCol, enrollCol, deptCol, roomCol);
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        Label info = new Label(
                "Students residing in your hostel. For detailed attendance reports, use the Reports module.");
        info.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-padding: 10 0 0 0;");

        tab.getChildren().addAll(title, studentTable, info);
        return tab;
    }

    public VBox getView() {
        return root;
    }
}
