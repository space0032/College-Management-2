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
    private ObservableList<ClubMembership> myApplicationsData;
    private TableView<Club> allClubsTable;
    private TableView<Club> myClubsTable;
    private TableView<ClubMembership> myApplicationsTable;
    private ComboBox<String> filterCombo;

    public ClubsView(int userId) {
        this.clubDAO = new ClubDAO();
        this.studentDAO = new StudentDAO();
        this.currentStudent = studentDAO.getStudentByUserId(userId);
        this.allClubsData = FXCollections.observableArrayList();
        this.myClubsData = FXCollections.observableArrayList();
        this.myApplicationsData = FXCollections.observableArrayList();

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

        Tab applicationsTab = new Tab("My Applications");
        applicationsTab.setContent(createApplicationsTab());

        tabPane.getTabs().addAll(browseTab, myClubsTab, applicationsTab);
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

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search clubs...");
        searchField.setPrefWidth(250);
        searchField.textProperty()
                .addListener((obs, oldVal, newVal) -> applySearchAndFilter(newVal, filterCombo.getValue()));

        Label filterLabel = new Label("Category:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Clubs", "TECHNICAL", "CULTURAL", "SPORTS", "SOCIAL", "ACADEMIC");
        filterCombo.setValue("All Clubs");
        filterCombo.setOnAction(e -> applySearchAndFilter(searchField.getText(), filterCombo.getValue()));

        filters.getChildren().addAll(searchField, filterLabel, filterCombo);

        // Table
        allClubsTable = createClubsTable(true);
        allClubsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(allClubsTable, Priority.ALWAYS);

        content.getChildren().addAll(filters, allClubsTable);
        return content;
    }

    private VBox createMyClubsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        myClubsTable = createClubsTable(false);
        myClubsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
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
        nameCol.setSortable(true);

        TableColumn<Club, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        categoryCol.setPrefWidth(120);
        categoryCol.setSortable(true);

        TableColumn<Club, String> presidentCol = new TableColumn<>("President");
        presidentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresidentName() != null ? data.getValue().getPresidentName() : "TBA"));
        presidentCol.setPrefWidth(150);
        presidentCol.setSortable(true);

        TableColumn<Club, String> coordinatorCol = new TableColumn<>("Faculty Coordinator");
        coordinatorCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCoordinatorName() != null ? data.getValue().getCoordinatorName() : "TBA"));
        coordinatorCol.setPrefWidth(180);
        coordinatorCol.setSortable(true);

        TableColumn<Club, String> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.valueOf(data.getValue().getMemberCount())));
        membersCol.setPrefWidth(80);
        membersCol.setSortable(true);
        // Sort numerically
        membersCol.setComparator((m1, m2) -> {
            try {
                return Integer.compare(Integer.parseInt(m1), Integer.parseInt(m2));
            } catch (Exception e) {
                return m1.compareTo(m2);
            }
        });

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

            TableColumn<Club, Void> announceCol = new TableColumn<>("Announcements");
            announceCol.setCellFactory(param -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                {
                    viewBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-padding: 5 15;");
                    viewBtn.setOnAction(e -> {
                        Club club = getTableView().getItems().get(getIndex());
                        showAnnouncementsDialog(club);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : viewBtn);
                }
            });
            announceCol.setPrefWidth(140);
            table.getColumns().add(announceCol);
        }

        return table;
    }

    private VBox createApplicationsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        myApplicationsTable = new TableView<>();
        myApplicationsTable.setItems(myApplicationsData);
        myApplicationsTable.setPlaceholder(new Label("No applications found."));
        VBox.setVgrow(myApplicationsTable, Priority.ALWAYS);

        TableColumn<ClubMembership, String> clubCol = new TableColumn<>("Club Name");
        clubCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getClubName()));
        clubCol.setPrefWidth(200);

        TableColumn<ClubMembership, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
        roleCol.setPrefWidth(150);

        TableColumn<ClubMembership, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<ClubMembership, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("APPROVED")) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if (item.equals("REJECTED")) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });
        statusCol.setPrefWidth(120);

        TableColumn<ClubMembership, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                new java.text.SimpleDateFormat("MMM dd, yyyy").format(data.getValue().getJoinedAt())));
        dateCol.setPrefWidth(150);

        myApplicationsTable.getColumns().addAll(clubCol, roleCol, statusCol, dateCol);
        content.getChildren().add(myApplicationsTable);
        return content;
    }

    private void loadData() {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                // Fetch data in background thread
                List<Club> allClubs = clubDAO.getAllClubs();

                List<Club> myClubs = java.util.Collections.emptyList();
                List<ClubMembership> myApps = java.util.Collections.emptyList();

                if (currentStudent != null) {
                    myClubs = clubDAO.getStudentClubs(currentStudent.getId());
                    myApps = clubDAO.getMyMemberships(currentStudent.getId());
                }

                final List<Club> finalAllClubs = allClubs;
                final List<Club> finalMyClubs = myClubs;
                final List<ClubMembership> finalApps = myApps;

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    allClubsData.setAll(finalAllClubs);
                    allClubsTable.setItems(allClubsData);

                    if (currentStudent != null) {
                        myClubsData.setAll(finalMyClubs);
                        myClubsTable.setItems(myClubsData);
                        myApplicationsData.setAll(finalApps);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null)
                ex.printStackTrace();
            javafx.application.Platform
                    .runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load club data."));
        });

        new Thread(task).start();
    }

    private void applySearchAndFilter(String searchText, String categoryFilter) {
        ObservableList<Club> filtered = allClubsData;

        // Apply category filter
        if (categoryFilter != null && !categoryFilter.equals("All Clubs")) {
            filtered = filtered.filtered(c -> c.getCategory().equals(categoryFilter));
        }

        // Apply search filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase();
            filtered = filtered.filtered(c -> c.getName().toLowerCase().contains(search) ||
                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(search)));
        }

        allClubsTable.setItems(filtered);
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

    private void showAnnouncementsDialog(Club club) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Club Announcements");
        dialog.setHeaderText("Announcements from " + club.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        content.setPrefHeight(400);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox listArea = new VBox(10);
        scroll.setContent(listArea);

        // Load announcements
        com.college.dao.ClubAnnouncementDAO dao = new com.college.dao.ClubAnnouncementDAO();
        List<com.college.models.ClubAnnouncement> list = dao.getAnnouncementsByClub(club.getId());

        if (list.isEmpty()) {
            Label placeholder = new Label("No active announcements.");
            placeholder.setTextFill(Color.GRAY);
            listArea.getChildren().add(placeholder);
        } else {
            for (com.college.models.ClubAnnouncement a : list) {
                VBox card = new VBox(5);
                card.setStyle(
                        "-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5; -fx-border-color: #cbd5e1;");

                Label title = new Label(a.getTitle());
                title.setFont(Font.font("System", FontWeight.BOLD, 14));

                Label meta = new Label("Posted by " + a.getPosterName() + " on " + a.getPostedAt());
                meta.setFont(Font.font("System", 10));
                meta.setTextFill(Color.GRAY);

                Label body = new Label(a.getContent());
                body.setWrapText(true);

                card.getChildren().addAll(title, meta, body);
                listArea.getChildren().add(card);
            }
        }

        content.getChildren().add(scroll);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
