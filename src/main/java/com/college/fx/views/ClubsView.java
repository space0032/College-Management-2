package com.college.fx.views;

import com.college.dao.ClubDAO;
import com.college.dao.StudentDAO;
import com.college.models.Club;
import com.college.models.ClubMembership;
import com.college.models.Student;
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

import java.util.List;

/**
 * Student-facing view for browsing and joining clubs
 */
public class ClubsView {
    private VBox root;
    private ClubDAO clubDAO;
    private StudentDAO studentDAO;
    private Student currentStudent;

    private ObservableList<Club> allClubsData;
    private ObservableList<Club> myClubsData;
    private TableView<Club> allClubsTable;
    private TableView<Club> myClubsTable;
    private ComboBox<String> filterCombo;

    public ClubsView(int userId) {
        this.clubDAO = new ClubDAO();
        this.studentDAO = new StudentDAO();
        this.currentStudent = studentDAO.getStudentByUserId(userId);
        this.allClubsData = FXCollections.observableArrayList();
        this.myClubsData = FXCollections.observableArrayList();

        createView();
        loadData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        HBox header = createHeader();

        // Tab Pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab browseTab = new Tab("Browse Clubs");
        browseTab.setContent(createBrowseTab());

        Tab myClubsTab = new Tab("My Clubs");
        myClubsTab.setContent(createMyClubsTab());

        tabPane.getTabs().addAll(browseTab, myClubsTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("Student Clubs");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadData());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    private VBox createBrowseTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // Filters
        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter by Category:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Clubs", "TECHNICAL", "CULTURAL", "SPORTS", "SOCIAL", "ACADEMIC");
        filterCombo.setValue("All Clubs");
        filterCombo.setOnAction(e -> applyFilter());

        filters.getChildren().addAll(filterLabel, filterCombo);

        // Table
        allClubsTable = createClubsTable(true);
        // TODO: Fix CSS resource loading
        // allClubsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(allClubsTable, Priority.ALWAYS);

        content.getChildren().addAll(filters, allClubsTable);
        return content;
    }

    private VBox createMyClubsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        myClubsTable = createClubsTable(false);
        // TODO: Fix CSS resource loading
        // myClubsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(myClubsTable, Priority.ALWAYS);

        content.getChildren().add(myClubsTable);
        return content;
    }

    @SuppressWarnings("unchecked")
    private TableView<Club> createClubsTable(boolean includeActions) {
        TableView<Club> table = new TableView<>();

        TableColumn<Club, String> nameCol = new TableColumn<>("Club Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Club, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryCol.setPrefWidth(120);

        TableColumn<Club, String> presidentCol = new TableColumn<>("President");
        presidentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresidentName() != null ? data.getValue().getPresidentName() : "TBA"));
        presidentCol.setPrefWidth(150);

        TableColumn<Club, String> coordinatorCol = new TableColumn<>("Faculty Coordinator");
        coordinatorCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCoordinatorName() != null ? data.getValue().getCoordinatorName() : "TBA"));
        coordinatorCol.setPrefWidth(180);

        TableColumn<Club, String> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.valueOf(data.getValue().getMemberCount())));
        membersCol.setPrefWidth(80);

        table.getColumns().addAll(nameCol, categoryCol, presidentCol, coordinatorCol, membersCol);

        if (includeActions) {
            TableColumn<Club, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button joinBtn = new Button("Join");
                private final Button viewBtn = new Button("View");

                {
                    joinBtn.setStyle(
                            "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                    viewBtn.setStyle(
                            "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");

                    joinBtn.setOnAction(e -> {
                        Club club = getTableView().getItems().get(getIndex());
                        joinClub(club);
                    });

                    viewBtn.setOnAction(e -> {
                        Club club = getTableView().getItems().get(getIndex());
                        showClubDetails(club);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Club club = getTableView().getItems().get(getIndex());
                        boolean isMember = false;
                        boolean isPending = false;

                        if (currentStudent != null) {
                            isMember = clubDAO.isStudentMember(club.getId(), currentStudent.getId());
                            // Check if status is pending by querying pending memberships
                            List<ClubMembership> pending = clubDAO.getPendingMemberships(club.getId());
                            int finalStudentId = currentStudent.getId();
                            isPending = pending.stream().anyMatch(m -> m.getStudentId() == finalStudentId);
                        }

                        if (isPending) {
                            joinBtn.setText("Pending");
                            joinBtn.setDisable(true);
                            joinBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
                        } else if (isMember) {
                            joinBtn.setText("Member");
                            joinBtn.setDisable(true);
                            joinBtn.setStyle("-fx-background-color: #94a3b8; -fx-text-fill: white;");
                        } else {
                            joinBtn.setText("Join");
                            joinBtn.setDisable(false);
                            joinBtn.setStyle(
                                    "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
                        }

                        HBox buttons = new HBox(5, viewBtn, joinBtn);
                        setGraphic(buttons);
                    }
                }
            });
            actionCol.setPrefWidth(160);
            table.getColumns().add(actionCol);
        } else {
            // Add "Leave" button for my clubs
            TableColumn<Club, Void> leaveCol = new TableColumn<>("Actions");
            leaveCol.setCellFactory(param -> new TableCell<>() {
                private final Button leaveBtn = new Button("Leave");

                {
                    leaveBtn.setStyle(
                            "-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15;");
                    leaveBtn.setOnAction(e -> {
                        Club club = getTableView().getItems().get(getIndex());
                        leaveClub(club);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : leaveBtn);
                }
            });
            leaveCol.setPrefWidth(100);
            table.getColumns().add(leaveCol);
        }

        return table;
    }

    private void loadData() {
        List<Club> allClubs = clubDAO.getAllClubs();
        allClubsData.setAll(allClubs);
        allClubsTable.setItems(allClubsData);

        if (currentStudent != null) {
            List<Club> myClubs = clubDAO.getStudentClubs(currentStudent.getId());
            myClubsData.setAll(myClubs);
            myClubsTable.setItems(myClubsData);
        }
    }

    private void applyFilter() {
        String filter = filterCombo.getValue();
        if (filter.equals("All Clubs")) {
            allClubsTable.setItems(allClubsData);
        } else {
            ObservableList<Club> filtered = allClubsData.filtered(c -> c.getCategory().equals(filter));
            allClubsTable.setItems(filtered);
        }
    }

    private void joinClub(Club club) {
        if (currentStudent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student profile not found.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Join Club");
        confirm.setHeaderText("Join " + club.getName() + "?");
        confirm.setContentText("Your join request will be sent to the club president/coordinator for approval.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (clubDAO.joinClub(club.getId(), currentStudent.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Join request submitted! Please wait for approval from the club president or faculty coordinator.");
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to submit join request. You may have already requested to join.");
            }
        }
    }

    private void leaveClub(Club club) {
        if (currentStudent == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student profile not found.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Leave Club");
        confirm.setHeaderText("Leave " + club.getName() + "?");
        confirm.setContentText("Are you sure you want to leave this club?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (clubDAO.leaveClub(club.getId(), currentStudent.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "You have left " + club.getName());
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to leave club.");
            }
        }
    }

    private void showClubDetails(Club club) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(club.getName());
        dialog.setHeaderText(club.getCategory() + " Club");

        ButtonType viewMembersBtn = new ButtonType("View Members", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(viewMembersBtn, ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        content.getChildren().addAll(
                new Label("Description:"),
                new Label(club.getDescription() != null ? club.getDescription() : "No description available."),
                new Separator(),
                new Label("President: " + (club.getPresidentName() != null ? club.getPresidentName() : "TBA")),
                new Label("Faculty Coordinator: "
                        + (club.getCoordinatorName() != null ? club.getCoordinatorName() : "TBA")),
                new Label("Total Members: " + club.getMemberCount()));

        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == viewMembersBtn) {
                showClubMembers(club);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void showClubMembers(Club club) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Members of " + club.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setPrefHeight(400);

        TableView<ClubMembership> memberTable = new TableView<>();
        TableColumn<ClubMembership, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        nameCol.setPrefWidth(200);

        TableColumn<ClubMembership, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        roleCol.setPrefWidth(150);

        memberTable.getColumns().addAll(nameCol, roleCol);

        List<ClubMembership> members = clubDAO.getClubMembers(club.getId());
        memberTable.setItems(FXCollections.observableArrayList(members));
        VBox.setVgrow(memberTable, Priority.ALWAYS);

        content.getChildren().add(memberTable);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        return btn;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
