package com.college.fx.views;

import com.college.dao.HostelDAO;
import com.college.dao.StudentDAO;
import com.college.models.Hostel;
import com.college.models.HostelAllocation;
import com.college.models.Room;
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
import javafx.stage.Modality;

import java.util.List;
import java.util.Optional;

/**
 * JavaFX Hostel Management View
 */
public class HostelManagementView {

    private VBox root;
    private HostelDAO hostelDAO;
    private StudentDAO studentDAO;
    private String role;
    private int userId;

    // Data lists
    private ObservableList<HostelAllocation> allocationData;
    private ObservableList<Hostel> hostelData;
    private ObservableList<Room> roomData;
    
    // Components
    private TableView<HostelAllocation> allocationTable;
    private TableView<Hostel> hostelTable;
    private TableView<Room> roomTable;

    public HostelManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.hostelDAO = new HostelDAO();
        this.studentDAO = new StudentDAO();
        this.allocationData = FXCollections.observableArrayList();
        this.hostelData = FXCollections.observableArrayList();
        this.roomData = FXCollections.observableArrayList();
        
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

        if (role.equals("STUDENT")) {
            root.getChildren().addAll(title, createStudentView());
        } else {
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setStyle("-fx-background-color: transparent;");

            Tab allocTab = new Tab("Allocations");
            allocTab.setContent(createAllocationTab());

            Tab hostelTab = new Tab("Hostels");
            hostelTab.setContent(createHostelTab());
            
            Tab roomTab = new Tab("Rooms");
            roomTab.setContent(createRoomTab());

            tabPane.getTabs().addAll(allocTab, hostelTab, roomTab);
            VBox.setVgrow(tabPane, Priority.ALWAYS);
            root.getChildren().addAll(title, tabPane);
        }
    }
    
    // ==================== STUDENT VIEW ====================
    
    private VBox createStudentView() {
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
                     card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
                     
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
        
        content.getChildren().add(hostelTable);
        return content;
    }
    
    private VBox createRoomTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        roomTable = new TableView<>();
        roomTable.setItems(roomData);
        
        TableColumn<Room, String> numCol = new TableColumn<>("Room No");
        numCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRoomNumber()));
        
        TableColumn<Room, String> hostelCol = new TableColumn<>("Hostel");
        hostelCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getHostelId())));
        
        TableColumn<Room, String> capCol = new TableColumn<>("Capacity");
        capCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOccupiedCount() + "/" + data.getValue().getCapacity()));
        
        roomTable.getColumns().addAll(numCol, hostelCol, capCol);
        VBox.setVgrow(roomTable, Priority.ALWAYS);
        
        content.getChildren().add(roomTable);
        return content;
    }

    // ==================== ACTIONS ====================

    private void loadData() {
        if (!role.equals("STUDENT")) {
            allocationData.clear();
            allocationData.addAll(hostelDAO.getAllActiveAllocations());
            
            hostelData.clear();
            hostelData.addAll(hostelDAO.getAllHostels());
            
            roomData.clear();
            roomData.addAll(hostelDAO.getAllRooms());
        }
    }

    private void showAllocationDialog() {
        // Simplified dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("New Allocation");
        alert.setHeaderText("Feature Placeholder");
        alert.setContentText("Full allocation dialog would go here (Select Student, Hostel, Room).");
        alert.showAndWait();
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

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    public VBox getView() {
        return root;
    }
}
