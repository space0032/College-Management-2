package com.college.fx.views;

import com.college.dao.HostelDAO;
import com.college.dao.WardenDAO;
import com.college.dao.StudentDAO;
import com.college.models.Hostel;
import com.college.models.HostelAllocation;
import com.college.models.Room;
import com.college.models.Warden;
import com.college.models.Student;

import com.college.dao.HostelAttendanceDAO;
import com.college.models.HostelAttendance;
import com.college.utils.SearchableStudentComboBox;
import com.college.utils.SessionManager;

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
    private HostelAttendanceDAO attendanceDAO;

    // Components
    private TableView<HostelAllocation> allocationTable;
    private TableView<Hostel> hostelTable;
    private TableView<Room> roomTable;
    private TableView<Student> hostelAttendanceTable;

    public HostelManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.hostelDAO = new HostelDAO();
        this.wardenDAO = new WardenDAO();
        this.studentDAO = new StudentDAO();
        this.attendanceDAO = new HostelAttendanceDAO();
        this.allocationData = FXCollections.observableArrayList();
        this.hostelData = FXCollections.observableArrayList();
        this.roomData = FXCollections.observableArrayList();
        this.wardenData = FXCollections.observableArrayList();

        createView();
        loadData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label("Hostel Management");
        title.getStyleClass().add("section-title");
        title.setPadding(new Insets(0, 0, 10, 0));

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("floating");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (role.equals("STUDENT")) {
            // Student view - show only their allocation
            Tab myAllocationTab = new Tab("My Hostel");
            myAllocationTab.setContent(createStudentAllocationView());
            myAllocationTab.setClosable(false);

            Tab complaintsTab = new Tab("My Complaints");
            complaintsTab.setContent(createStudentComplaintsTab());
            complaintsTab.setClosable(false);

            tabPane.getTabs().addAll(myAllocationTab, complaintsTab);

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

            Tab complaintTab = new Tab("Complaints");
            complaintTab.setContent(createWardenComplaintsTab());
            complaintTab.setClosable(false);

            tabPane.getTabs().addAll(allocTab, roomTab, attendanceTab, complaintTab);
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

            // Check for Hostel Attendance Permission
            if (SessionManager.getInstance().hasPermission("VIEW_HOSTEL_ATTENDANCE")) {
                Tab attendanceTab = new Tab("Hostel Attendance");
                attendanceTab.setContent(createWardenAttendanceTab());
                attendanceTab.setClosable(false);
                tabPane.getTabs().add(attendanceTab);
            }

            // Admins can also see all complaints ideally, but for now sticking to
            // requirements
            // Adding a read-only or full complaints tab for Admin could be useful
            Tab complaintTab = new Tab("All Complaints");
            complaintTab.setContent(createWardenComplaintsTab()); // Reusing warden tab for admin
            complaintTab.setClosable(false);

            tabPane.getTabs().add(complaintTab);
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
                HostelAllocation activeAlloc = null;
                for (HostelAllocation alloc : allocs) {
                    if ("ACTIVE".equalsIgnoreCase(alloc.getStatus())) {
                        activeAlloc = alloc;
                        break;
                    }
                }

                if (activeAlloc != null) {
                    VBox card = new VBox(15);
                    card.setMaxWidth(500);
                    card.setPadding(new Insets(30));
                    card.getStyleClass().add("glass-card");

                    Label statusTitle = new Label("Current Accommodation");
                    statusTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                    statusTitle.setTextFill(Color.web("#14b8a6"));

                    GridPane grid = new GridPane();
                    grid.setHgap(15);
                    grid.setVgap(15);

                    addDetailRow(grid, "Hostel:", activeAlloc.getHostelName(), 0);
                    addDetailRow(grid, "Room No:", activeAlloc.getRoomNumber(), 1);
                    addDetailRow(grid, "Allocated On:", activeAlloc.getCheckInDate().toString(), 2);

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
        // val.setTextFill(Color.web("#0f172a"));

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // ==================== ALLOCATIONS TAB ====================

    private VBox createAllocationTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        HBox toolbar = new HBox(15);
        Button allocateBtn = createButton("New Allocation");
        allocateBtn.setOnAction(e -> showAllocationDialog());

        Button refreshBtn = createButton("Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(allocateBtn, refreshBtn);

        allocationTable = new TableView<>();
        allocationTable.getStyleClass().add("glass-table");
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
        content.getStyleClass().add("glass-card");

        HBox toolbar = new HBox(15);
        Button addHostelBtn = createButton("Add Hostel");
        addHostelBtn.setOnAction(e -> showHostelDialog(null));

        Button editHostelBtn = createButton("Edit Hostel");
        editHostelBtn.setOnAction(e -> editHostel());

        Button deleteHostelBtn = createButton("Delete Hostel");
        deleteHostelBtn.setOnAction(e -> deleteHostel());

        Button refreshBtn = createButton("Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addHostelBtn, editHostelBtn, deleteHostelBtn, refreshBtn);

        hostelTable = new TableView<>();
        hostelTable.getStyleClass().add("glass-table");
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
        content.getStyleClass().add("glass-card");

        HBox toolbar = new HBox(15);
        Button addRoomBtn = createButton("Add Room");
        addRoomBtn.setOnAction(e -> showAddRoomDialog());

        Button deleteRoomBtn = createButton("Delete Room");
        deleteRoomBtn.setOnAction(e -> deleteRoom());

        Button refreshBtn = createButton("Refresh");
        refreshBtn.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(addRoomBtn, deleteRoomBtn, refreshBtn);

        roomTable = new TableView<>();
        roomTable.getStyleClass().add("glass-table");
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

    private void editHostel() {
        Hostel selected = hostelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a hostel to edit.");
            return;
        }
        showHostelDialog(selected);
    }

    private void showHostelDialog(Hostel hostel) {
        Dialog<Hostel> dialog = new Dialog<>();
        dialog.setTitle(hostel == null ? "Add Hostel" : "Edit Hostel");
        dialog.setHeaderText(hostel == null ? "Create New Hostel" : "Edit Hostel Details");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(hostel != null ? hostel.getName() : "");
        nameField.setPromptText("Hostel Name");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("BOYS", "GIRLS");
        typeCombo.setValue(hostel != null ? hostel.getType() : "BOYS");

        ComboBox<Warden> wardenCombo = new ComboBox<>();
        List<Warden> allWardens = wardenDAO.getAllWardens();
        wardenCombo.getItems().addAll(allWardens);
        wardenCombo.setPromptText("Select Warden");

        // Define how to display Warden in ComboBox
        javafx.util.StringConverter<Warden> converter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(Warden w) {
                return w != null ? w.getName() : "";
            }

            @Override
            public Warden fromString(String string) {
                return null;
            }
        };
        wardenCombo.setConverter(converter);

        // Pre-select warden if editing
        if (hostel != null && hostel.getWardenName() != null) {
            for (Warden w : allWardens) {
                if (w.getName().equals(hostel.getWardenName())) {
                    wardenCombo.setValue(w);
                    break;
                }
            }
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Warden:"), 0, 2);
        grid.add(wardenCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn && !nameField.getText().trim().isEmpty()) {
                Hostel h = hostel != null ? hostel : new Hostel();
                h.setName(nameField.getText());
                h.setType(typeCombo.getValue());

                Warden selectedWarden = wardenCombo.getValue();
                if (selectedWarden != null) {
                    h.setWardenName(selectedWarden.getName());
                    h.setWardenContact(selectedWarden.getPhone());
                } else if (hostel == null) {
                    // If adding and no warden selected, clear fields
                    h.setWardenName("");
                    h.setWardenContact("");
                }

                if (hostel == null) {
                    if (hostelDAO.addHostel(h))
                        return h;
                } else {
                    if (hostelDAO.updateHostel(h))
                        return h;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(h -> {
            loadData();
            showAlert("Success", "Hostel " + (hostel == null ? "added" : "updated") + " successfully!");
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

    private Button createButton(String text) {
        Button btn = new Button(text);
        // Style classes are added by caller
        return btn;
    }

    private VBox createWardenTab() {
        VBox tab = new VBox(15);
        tab.setPadding(new Insets(15));

        // Warden table
        TableView<Warden> wardenTable = new TableView<>();
        wardenTable.getStyleClass().add("glass-table");
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

        TableColumn<Warden, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        userCol.setPrefWidth(120);

        wardenTable.getColumns().addAll(nameCol, userCol, emailCol, phoneCol, hostelCol);
        VBox.setVgrow(wardenTable, Priority.ALWAYS);

        // Control buttons
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        Button addWardenBtn = createButton("Add Warden");
        addWardenBtn.setOnAction(e -> showAddWardenDialog());

        Button editWardenBtn = createButton("Edit Warden");
        editWardenBtn.setOnAction(e -> showEditWardenDialog(wardenTable));

        Button deleteWardenBtn = createButton("Delete Warden");
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

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Hostel Student Attendance");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markBtn = createButton("Mark Attendance");
        markBtn.setOnAction(e -> showMarkHostelAttendanceDialog());

        Button bulkBtn = createButton("Bulk Attendance");
        bulkBtn.setOnAction(e -> showBulkHostelAttendanceDialog());

        header.getChildren().addAll(title, spacer, markBtn, bulkBtn);

        // Get warden's hostel
        Warden warden = wardenDAO.getWardenByUserId(userId);
        if (warden == null && !role.equals("ADMIN")) { // Admin check if managing all
            if (role.equals("ADMIN")) {
                // Admin logic if needed, simplify for now
            } else {
                Label noHostel = new Label("No hostel assigned to your account.");
                noHostel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
                tab.getChildren().addAll(title, noHostel);
                return tab;
            }
        }

        int hostelId = (warden != null) ? warden.getHostelId() : 0;

        // Show today's attendance summary or list
        // Simple table of students in this hostel

        // Use existing student fetch logic
        List<HostelAllocation> allAllocs = hostelDAO.getAllActiveAllocations();
        List<Room> allRooms = hostelDAO.getAllRooms();
        List<HostelAllocation> hostelAllocs = new ArrayList<>();

        for (HostelAllocation alloc : allAllocs) {
            for (Room r : allRooms) {
                // If ADMIN, maybe show all? stick to warden constraint for now or show all if
                // admin
                if (r.getId() == alloc.getRoomId()) {
                    if (hostelId == 0 || r.getHostelId() == hostelId) {
                        hostelAllocs.add(alloc);
                        break;
                    }
                }
            }
        }

        hostelAttendanceTable = new TableView<>();
        hostelAttendanceTable.getStyleClass().add("glass-table");
        ObservableList<Student> students = FXCollections.observableArrayList();

        for (HostelAllocation alloc : hostelAllocs) {
            Student s = studentDAO.getStudentById(alloc.getStudentId());
            if (s != null) {
                students.add(s);
            }
        }
        hostelAttendanceTable.setItems(students);

        TableColumn<Student, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Student, String> enrollCol = new TableColumn<>("Enrollment ID");
        // FIXED: Now uses username which is correctly populated by fixed DAO
        enrollCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        enrollCol.setPrefWidth(150);

        TableColumn<Student, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> {
            for (HostelAllocation a : hostelAllocs) {
                if (a.getStudentId() == data.getValue().getId()) {
                    for (Room r : allRooms) {
                        if (r.getId() == a.getRoomId())
                            return new SimpleStringProperty(r.getRoomNumber());
                    }
                }
            }
            return new SimpleStringProperty("-");
        });
        roomCol.setPrefWidth(100);

        TableColumn<Student, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            // Need efficient lookup.
            // Ideally we fetch a map once and use it.
            // But cell factory runs on update.
            // Let's rely on a property in the item or a map stored in the view.

            // Allow storing status map in table user data or similar hack, OR better:
            // Use a wrapper object in the table instead of raw Student.
            // Converting Table<Student> to Table<HostelAttendanceViewModel> is a larger
            // refactor.
            // Quick approach: Fetch single record (cached if possible) or just show "-"
            // until refresh.

            // To be efficient: changing the table logic to load a Map<Integer, String>
            // currentStatusMap
            // into the userData of the table or a local variable if we rebuild.

            @SuppressWarnings("unchecked")
            java.util.Map<Integer, String> statusMap = (java.util.Map<Integer, String>) hostelAttendanceTable
                    .getProperties()
                    .get("statusMap");
            if (statusMap != null) {
                String s = statusMap.getOrDefault(data.getValue().getId(), "NOT MARKED");
                return new SimpleStringProperty(s);
            }
            return new SimpleStringProperty("-");
        });
        statusCol.setCellFactory(column -> new TableCell<Student, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("PRESENT")) {
                        setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    } else if (item.equals("ABSENT")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item.equals("LEAVE")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else if (item.equals("LATE")) {
                        setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });
        statusCol.setPrefWidth(120);

        hostelAttendanceTable.getColumns().addAll(nameCol, enrollCol, roomCol, statusCol);
        VBox.setVgrow(hostelAttendanceTable, Priority.ALWAYS);

        // Initial Data Load with Status
        refreshHostelAttendanceTable();

        tab.getChildren().addAll(header, hostelAttendanceTable);
        return tab;
    }

    private void refreshHostelAttendanceTable() {
        if (hostelAttendanceTable == null)
            return;

        // This runs on FX thread
        // Fetch current attendance statuses
        List<HostelAttendance> todayAtt = attendanceDAO.getAttendanceByDate(new java.util.Date());
        java.util.Map<Integer, String> attMap = new java.util.HashMap<>();
        for (HostelAttendance ha : todayAtt) {
            attMap.put(ha.getStudentId(), ha.getStatus());
        }
        hostelAttendanceTable.getProperties().put("statusMap", attMap);
        hostelAttendanceTable.refresh();
    }

    private void showMarkHostelAttendanceDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mark Hostel Attendance");
        dialog.setHeaderText("Mark Individual Attendance");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(120);
        grid.getColumnConstraints().add(col1);

        // Hostel Students with Active Allocations only
        Warden warden = wardenDAO.getWardenByUserId(userId);
        List<Student> students = new ArrayList<>();
        int currentHostelId = 0;

        List<HostelAllocation> allActiveAllocations = hostelDAO.getAllActiveAllocations();

        if (warden != null && warden.getHostelId() > 0) {
            currentHostelId = warden.getHostelId();
            // Filter allocations for this warden's hostel
            for (HostelAllocation alloc : allActiveAllocations) {
                // Check if allocation belongs to warden's hostel
                // Since allocation object now contains hostelName but maybe not ID directly if
                // not joined efficiently
                // But wait, getAllActiveAllocations() joins rooms and hostels.
                // Let's check room -> hostel ID mapping.
                // Re-fetching rooms is safer or relies on allocation data if extended.
                // The current DAO method returns: ha.*, s.name, r.room_number, h.name
                // It does NOT return hostel_id explicit in the JOIN select unless ha.room_id ->
                // room -> hostel_id
                // But typically we can filter by room.

                // Efficient approach: fetch all rooms to map room_id -> hostel_id
                // Or relies on a helper.

                // Let's use the existing comprehensive list logic from previous method which
                // was correct
                // BUT simplify it by using the pre-fetched active allocations.

                // Optimization: Get room map
                List<Room> allRooms = hostelDAO.getAllRooms();
                for (Room r : allRooms) {
                    if (r.getId() == alloc.getRoomId() && r.getHostelId() == currentHostelId) {
                        Student s = studentDAO.getStudentById(alloc.getStudentId());
                        if (s != null)
                            students.add(s);
                        break;
                    }
                }
            }
        } else {
            // Admin / View All: Show ALL students with active allocations
            for (HostelAllocation alloc : allActiveAllocations) {
                Student s = studentDAO.getStudentById(alloc.getStudentId());
                if (s != null) {
                    // Check if already added to avoid duplicates if multiple allocations exist
                    // (shouldn't for active)
                    boolean exists = false;
                    for (Student existing : students) {
                        if (existing.getId() == s.getId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists)
                        students.add(s);
                }
            }
        }

        SearchableStudentComboBox studentSelector = new SearchableStudentComboBox(students);

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("PRESENT", "ABSENT", "LEAVE", "LATE");
        statusCombo.setValue("PRESENT");

        TextArea remarks = new TextArea();
        remarks.setPromptText("Remarks (optional)");
        remarks.setPrefRowCount(2);

        grid.add(new Label("Student:"), 0, 0);
        grid.add(studentSelector, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(statusCombo, 1, 2);
        grid.add(new Label("Remarks:"), 0, 3);
        grid.add(remarks, 1, 3);

        Button saveBtn = createButton("Mark"); // avoid clash with dialog button
        final int finalHostelId = currentHostelId;

        // Listener to check existing attendance
        Runnable checkExisting = () -> {
            Student sel = studentSelector.getSelectedStudent();
            java.time.LocalDate d = datePicker.getValue();
            if (sel != null && d != null) {
                HostelAttendance existing = attendanceDAO.getAttendanceByStudentAndDate(sel.getId(),
                        Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                if (existing != null) {
                    statusCombo.setValue(existing.getStatus());
                    remarks.setText(existing.getRemarks());
                    // Restrict modification if policy dictates "once per day"
                    // Assuming strict "once" means creation not allowed, or show warning.
                    // User said "only able to mark one student attendance once per day".
                    // This could mean "prevent duplicate inserts" or "prevent changing it".
                    // Let's use a warning label and maybe change button text to "Update" if we
                    // allow correction, or Disable.
                    // A strict interpretation "once" implies disabling.
                    saveBtn.setText("Update");
                    saveBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
                    if (!grid.getChildren().contains(saveBtn)) { // Ensure button is there
                        // It is there.
                    }
                    // Add warning
                    // We can't easily add a new row dynamically without managing indices, but we
                    // can change the button or tooltip.
                    saveBtn.setTooltip(new Tooltip("Attendance already marked. Click to update."));
                } else {
                    statusCombo.setValue("PRESENT");
                    remarks.clear();
                    saveBtn.setText("Mark");
                    saveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;");
                    saveBtn.setTooltip(null);
                }
            }
        };

        datePicker.valueProperty().addListener((obs, old, nv) -> checkExisting.run());
        // We need to hook into student selection. The SearchableStudentComboBox might
        // not expose a property easily if not designed well.
        // Assuming it has a selection model or we can add a listener to value property
        // if it's a ComboBox subclass.
        // If not, we might need to modify it or add a listener to its selection.
        // Let's look at SearchableStudentComboBox... it usually extends ComboBox or has
        // getSelectionModel().
        studentSelector.selectedItemProperty().addListener((obs, old, nv) -> checkExisting.run());

        saveBtn.setOnAction(e -> {
            Student s = studentSelector.getSelectedStudent();
            if (s == null || datePicker.getValue() == null) {
                showAlert("Error", "Select student and date");
                return;
            }

            // Need hostel ID
            int hId = finalHostelId;
            if (hId == 0) {
                // Fetch student's hostel allocation
                List<HostelAllocation> allocs = hostelDAO.getAllocationsByStudent(s.getId());
                for (HostelAllocation ha : allocs) {
                    if ("ACTIVE".equals(ha.getStatus())) {
                        // Find hostel ID from allocation/room...
                        List<Room> rooms = hostelDAO.getAllRooms();
                        for (Room r : rooms) {
                            if (r.getId() == ha.getRoomId()) {
                                hId = r.getHostelId();
                                break;
                            }
                        }
                        if (hId > 0)
                            break;
                    }
                }
                if (hId == 0) {
                    showAlert("Error", "Could not determine student's hostel. Warden permission required.");
                    return;
                }
            }

            // Re-check for existing to prevent "once per day" violation if we interpret it
            // as strict Create-Only
            // IF strict:
            // HostelAttendance existing = attendanceDAO.getAttendanceByStudentAndDate(...)
            // if(existing != null && !confirm("Overwrite?")) return;
            // The DAO does upsert (ON DUPLICATE KEY UPDATE).

            HostelAttendance ha = new HostelAttendance();
            ha.setStudentId(s.getId());
            ha.setHostelId(hId);
            ha.setDate(Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            ha.setStatus(statusCombo.getValue());
            ha.setRemarks(remarks.getText());
            ha.setMarkedBy(userId);

            if (attendanceDAO.markAttendance(ha)) {
                showAlert("Success", "Attendance Marked: " + ha.getStatus());
                // remarks.clear(); // Keep attributes visible as per "show marked attendance"
                // Refresh logic to show current state (which is what we just saved)
                checkExisting.run();
                // Refresh the main table to show updated status immediately
                refreshHostelAttendanceTable();
            } else {
                showAlert("Error", "Failed to mark.");
            }
        });

        grid.add(saveBtn, 1, 4);
        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private void showBulkHostelAttendanceDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Bulk Hostel Attendance");
        dialog.setHeaderText("Mark Attendance by Room");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(800);
        content.setPrefHeight(600);

        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        Button loadBtn = createButton("Load Students");

        HBox top = new HBox(10, new Label("Date:"), datePicker, loadBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        TableView<BulkHostelRecord> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        ObservableList<BulkHostelRecord> records = FXCollections.observableArrayList();
        table.setItems(records);
        table.setEditable(true);

        TableColumn<BulkHostelRecord, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().roomNumber));
        roomCol.setComparator((r1, r2) -> {
            // Try numeric sort
            try {
                return Integer.compare(Integer.parseInt(r1), Integer.parseInt(r2));
            } catch (Exception e) {
                return r1.compareTo(r2);
            }
        });

        TableColumn<BulkHostelRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().studentName));
        nameCol.setPrefWidth(200);

        TableColumn<BulkHostelRecord, String> enrollCol = new TableColumn<>("Enrollment");
        enrollCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().enrollmentId));

        TableColumn<BulkHostelRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> cb = new ComboBox<>();
            {
                cb.getItems().addAll("PRESENT", "ABSENT", "LEAVE", "LATE");
                cb.valueProperty().addListener((obs, old, nv) -> {
                    if (getTableRow().getItem() != null)
                        ((BulkHostelRecord) getTableRow().getItem()).status = nv;
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else {
                    cb.setValue(getTableView().getItems().get(getIndex()).status);
                    setGraphic(cb);
                }
            }
        });

        table.getColumns().addAll(roomCol, enrollCol, nameCol, statusCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        Warden warden = wardenDAO.getWardenByUserId(userId);

        loadBtn.setOnAction(e -> {
            if (warden == null || warden.getHostelId() <= 0) {
                showAlert("Error", "No hostel assigned.");
                return;
            }
            if (datePicker.getValue() == null) {
                showAlert("Error", "Select date first.");
                return;
            }

            records.clear();
            Date selectedDate = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Fetch existing attendance for this date
            List<HostelAttendance> existingList = attendanceDAO.getAttendanceByDateAndHostel(selectedDate,
                    warden.getHostelId());

            // Fetch all students in this hostel with room numbers
            List<HostelAllocation> allAllocs = hostelDAO.getAllActiveAllocations();
            List<Room> allRooms = hostelDAO.getAllRooms();

            for (HostelAllocation alloc : allAllocs) {
                // Find room
                Room r = allRooms.stream().filter(rm -> rm.getId() == alloc.getRoomId()).findFirst().orElse(null);
                if (r != null && r.getHostelId() == warden.getHostelId()) {
                    Student s = studentDAO.getStudentById(alloc.getStudentId());
                    if (s != null) {
                        BulkHostelRecord record = new BulkHostelRecord(s.getId(), s.getName(), s.getUsername(),
                                r.getRoomNumber());

                        // Check if already marked
                        HostelAttendance existing = existingList.stream()
                                .filter(ha -> ha.getStudentId() == s.getId())
                                .findFirst().orElse(null);

                        if (existing != null) {
                            record.status = existing.getStatus();
                            record.isExisting = true; // Flag for UI indication if needed
                        } else {
                            record.status = "PRESENT"; // Default
                            record.isExisting = false;
                        }

                        records.add(record);
                    }
                }
            }
            // Sort by room -> Numeric sort
            records.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(a.roomNumber), Integer.parseInt(b.roomNumber));
                } catch (Exception x) {
                    return a.roomNumber.compareTo(b.roomNumber);
                }
            });

            if (!records.isEmpty()) {
                long markedCount = records.stream().filter(r -> r.isExisting).count();
                if (markedCount > 0) {
                    showAlert("Info", "Loaded " + records.size() + " students. " + markedCount
                            + " already marked for this date.");
                }
            }
        });

        Button saveBtn = createButton("Save Attendance");
        saveBtn.setOnAction(e -> {
            if (records.isEmpty())
                return;
            if (datePicker.getValue() == null) {
                showAlert("Error", "Select date");
                return;
            }

            Date d = Date.from(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            int count = 0;
            for (BulkHostelRecord rec : records) {
                HostelAttendance ha = new HostelAttendance();
                ha.setStudentId(rec.studentId);
                ha.setHostelId(warden.getHostelId());
                ha.setDate(d);
                ha.setStatus(rec.status);
                ha.setMarkedBy(userId);
                if (attendanceDAO.markAttendance(ha))
                    count++;
            }
            showAlert("Success", "Saved " + count + " records.");
            dialog.close();
        });

        content.getChildren().addAll(top, table, saveBtn);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private static class BulkHostelRecord {
        int studentId;
        String studentName;
        String enrollmentId;
        String roomNumber;
        String status = "PRESENT";
        boolean isExisting = false;

        public BulkHostelRecord(int sid, String name, String eid, String rno) {
            this.studentId = sid;
            this.studentName = name;
            this.enrollmentId = eid;
            this.roomNumber = rno;
        }
    }

    // ==================== COMPLAINTS ====================

    private VBox createStudentComplaintsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Detect room info
        Student currentStudent = studentDAO.getStudentByUserId(userId);
        String detectedRoom = "Not Allocated";
        String roomInfoForDesc = "";

        if (currentStudent != null) {
            for (HostelAllocation ha : hostelDAO.getAllocationsByStudent(currentStudent.getId())) {
                if ("ACTIVE".equalsIgnoreCase(ha.getStatus())) {
                    detectedRoom = "Room " + ha.getRoomNumber() + ", " + ha.getHostelName();
                    roomInfoForDesc = "\n\n[Location: " + detectedRoom + "]";
                    break;
                }
            }
        }

        // Form to file new complaint
        VBox form = new VBox(10);
        form.getStyleClass().add("glass-card");
        form.setStyle("-fx-padding: 15;");
        Label formTitle = new Label("File a New Complaint");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label roomLabel = new Label("Your Location: " + detectedRoom);
        roomLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

        TextField titleField = new TextField();
        titleField.setPromptText("Complaint Title (e.g., Leaking Tap)");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Maintenance", "Food", "Cleanliness", "Discipline", "Other");
        typeCombo.setPromptText("Category");
        typeCombo.getSelectionModel().selectFirst();

        TextArea descArea = new TextArea();
        descArea.setPromptText("Describe the issue in detail...");
        descArea.setPrefHeight(80);

        Button fileBtn = createButton("Submit Complaint");
        final String finalRoomInfo = roomInfoForDesc;

        fileBtn.setOnAction(e -> {
            String t = titleField.getText().trim();
            String d = descArea.getText().trim();
            if (t.isEmpty() || d.isEmpty()) {
                showAlert("Error", "Please fill in title and description.");
                return;
            }
            Student s = studentDAO.getStudentByUserId(userId);
            if (s == null)
                return;

            // Append room info to description
            String fullDescription = d + finalRoomInfo;

            com.college.models.Complaint c = new com.college.models.Complaint(s.getId(), t, fullDescription,
                    typeCombo.getValue());
            com.college.dao.ComplaintDAO cDAO = new com.college.dao.ComplaintDAO();
            if (cDAO.createComplaint(c)) {
                showAlert("Success", "Complaint filed successfully.");
                titleField.clear();
                descArea.clear();
                // Refresh list if we had a dedicated list component here.
                // For now, simpler UI. To see history, user might need another view or just
                // refresh.
                // Or better, let's show history below.
            } else {
                showAlert("Error", "Failed to file complaint.");
            }
        });

        form.getChildren().addAll(formTitle, roomLabel, titleField, typeCombo, descArea, fileBtn);

        // List of my complaints
        TableView<com.college.models.Complaint> myComplaintsTable = new TableView<>();
        myComplaintsTable.getStyleClass().add("glass-table");

        TableColumn<com.college.models.Complaint, String> cTitle = new TableColumn<>("Title");
        cTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        cTitle.setPrefWidth(200);

        TableColumn<com.college.models.Complaint, String> cCat = new TableColumn<>("Category");
        cCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));

        TableColumn<com.college.models.Complaint, String> cStatus = new TableColumn<>("Status");
        cStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<com.college.models.Complaint, String> cDate = new TableColumn<>("Filed On");
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFiledDate().toString()));

        TableColumn<com.college.models.Complaint, String> cRemarks = new TableColumn<>("Warden Remarks");
        cRemarks.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRemarks()));
        cRemarks.setPrefWidth(200);

        myComplaintsTable.getColumns().addAll(cTitle, cCat, cStatus, cDate, cRemarks);
        VBox.setVgrow(myComplaintsTable, Priority.ALWAYS);

        // Load data button
        Button refreshBtn = createButton("Refresh History");
        refreshBtn.setOnAction(e -> {
            Student s = studentDAO.getStudentByUserId(userId);
            if (s != null) {
                com.college.dao.ComplaintDAO cDAO = new com.college.dao.ComplaintDAO();
                myComplaintsTable.getItems().setAll(cDAO.getComplaintsByStudent(s.getId()));
            }
        });

        // Initial load
        refreshBtn.fire();

        content.getChildren().addAll(form, new Separator(), new Label("My Complaint History"), myComplaintsTable,
                refreshBtn);
        return content;
    }

    private VBox createWardenComplaintsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label title = new Label("Manage Complaints");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        TableView<com.college.models.Complaint> table = new TableView<>();
        table.getStyleClass().add("glass-table");
        com.college.dao.ComplaintDAO cDAO = new com.college.dao.ComplaintDAO();

        // Cols
        TableColumn<com.college.models.Complaint, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));

        TableColumn<com.college.models.Complaint, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName()));

        TableColumn<com.college.models.Complaint, String> colHostel = new TableColumn<>("Title");
        colHostel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));

        TableColumn<com.college.models.Complaint, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));

        TableColumn<com.college.models.Complaint, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));

        TableColumn<com.college.models.Complaint, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFiledDate().toString()));

        table.getColumns().addAll(colId, colStudent, colHostel, colCat, colStatus, colDate);
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh");
        refreshBtn.setOnAction(e -> {
            // Should filter by warden's hostel realistically, but for now showing all or
            // all for assigned students
            table.getItems().setAll(cDAO.getAllComplaints());
        });

        Button viewBtn = createButton("View details");
        viewBtn.setOnAction(e -> {
            com.college.models.Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Error", "Select a complaint.");
                return;
            }

            Dialog<Void> d = new Dialog<>();
            d.setTitle("Complaint Details");
            d.setHeaderText(sel.getTitle());
            d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox v = new VBox(10);
            v.setPadding(new Insets(10));
            v.getChildren().addAll(
                    new Label("Student: " + sel.getStudentName()),
                    new Label("Category: " + sel.getCategory()),
                    new Label("Status: " + sel.getStatus()),
                    new Separator(),
                    new Label("Description:"),
                    new Label(sel.getDescription()),
                    new Separator(),
                    new Label("Resolution Remarks:"),
                    new Label(sel.getRemarks() == null ? "None" : sel.getRemarks()));
            d.getDialogPane().setContent(v);
            d.showAndWait();
        });

        Button resolveBtn = createButton("Resolve / Reject");
        resolveBtn.setOnAction(e -> {
            com.college.models.Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert("Error", "Select a complaint.");
                return;
            }
            if (!"OPEN".equals(sel.getStatus())) {
                showAlert("Info", "This complaint is already closed.");
                return;
            }

            Dialog<String> d = new Dialog<>();
            d.setTitle("Resolve Complaint");
            d.setHeaderText("Action on: " + sel.getTitle());
            ButtonType resolve = new ButtonType("Mark Resolved", ButtonData.OK_DONE);
            ButtonType reject = new ButtonType("Reject", ButtonData.OK_DONE);
            d.getDialogPane().getButtonTypes().addAll(resolve, reject, ButtonType.CANCEL);

            TextArea remarks = new TextArea();
            remarks.setPromptText("Remarks / Resolution Details");
            d.getDialogPane().setContent(remarks);

            d.setResultConverter(b -> {
                if (b == resolve)
                    return "RESOLVED";
                if (b == reject)
                    return "REJECTED";
                return null;
            });

            d.showAndWait().ifPresent(res -> {
                cDAO.updateStatus(sel.getId(), res, userId, remarks.getText());
                refreshBtn.fire();
            });
        });

        HBox actions = new HBox(10, refreshBtn, viewBtn, resolveBtn);
        content.getChildren().addAll(title, actions, table);

        refreshBtn.fire();
        return content;
    }

    public VBox getView() {
        return root;
    }
}
