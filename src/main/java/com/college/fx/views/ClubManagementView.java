package com.college.fx.views;

import com.college.dao.ClubDAO;
import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.models.Club;
import com.college.models.ClubMembership;
import com.college.models.Student;
import com.college.models.Faculty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * Admin/Faculty view for managing student clubs
 */
public class ClubManagementView {
    private VBox root;
    private ClubDAO clubDAO;
    private StudentDAO studentDAO;
    private FacultyDAO facultyDAO;

    private int currentUserId;
    private ObservableList<Club> clubsData;
    private TableView<Club> clubsTable;

    public ClubManagementView(int userId) {
        this.currentUserId = userId;
        this.clubDAO = new ClubDAO();
        this.studentDAO = new StudentDAO();
        this.facultyDAO = new FacultyDAO();
        this.clubsData = FXCollections.observableArrayList();

        createView();
        loadClubs();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();

        root.getChildren().addAll(header, tableSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");

        Label title = new Label("Club Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = createButton("Create Club", "#22c55e");
        createBtn.setOnAction(e -> showCreateClubDialog());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadClubs());

        header.getChildren().addAll(title, spacer, createBtn, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        VBox.setVgrow(section, Priority.ALWAYS);

        clubsTable = new TableView<>();
        clubsTable.setItems(clubsData);
        clubsTable.setPlaceholder(new Label("No clubs yet.\nClick 'Create Club' to get started."));
        // TODO: Fix CSS resource loading
        // clubsTable.getStylesheets().add(getClass().getResource("/styles/tables.css").toExternalForm());
        VBox.setVgrow(clubsTable, Priority.ALWAYS);

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

        TableColumn<Club, String> coordinatorCol = new TableColumn<>("Coordinator");
        coordinatorCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCoordinatorName() != null ? data.getValue().getCoordinatorName() : "TBA"));
        coordinatorCol.setPrefWidth(150);

        TableColumn<Club, String> membersCol = new TableColumn<>("Members");
        membersCol.setCellValueFactory(
                data -> new SimpleStringProperty(String.valueOf(data.getValue().getMemberCount())));
        membersCol.setPrefWidth(80);

        TableColumn<Club, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Club, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(440);

        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button membersBtn = new Button("Members");
            private final Button approveBtn = new Button("Approvals");
            private final Button announceBtn = new Button("Announce");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10;");
                membersBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-padding: 5 10;");
                approveBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-padding: 5 10;");
                announceBtn.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-padding: 5 10;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10;");

                editBtn.setOnAction(e -> {
                    Club club = getTableView().getItems().get(getIndex());
                    showEditClubDialog(club);
                });

                membersBtn.setOnAction(e -> {
                    Club club = getTableView().getItems().get(getIndex());
                    showMembersDialog(club);
                });

                approveBtn.setOnAction(e -> {
                    Club club = getTableView().getItems().get(getIndex());
                    showPendingApprovalsDialog(club);
                });

                announceBtn.setOnAction(e -> {
                    Club club = getTableView().getItems().get(getIndex());
                    showAnnouncementsDialog(club);
                });

                deleteBtn.setOnAction(e -> {
                    Club club = getTableView().getItems().get(getIndex());
                    deleteClub(club);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, membersBtn, approveBtn, announceBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });

        clubsTable.getColumns().addAll(nameCol, categoryCol, presidentCol, coordinatorCol, membersCol, statusCol,
                actionCol);
        section.getChildren().add(clubsTable);
        return section;
    }

    private void loadClubs() {
        List<Club> clubs = clubDAO.getAllClubs();
        clubsData.setAll(clubs);
    }

    private void showCreateClubDialog() {
        Dialog<Club> dialog = new Dialog<>();
        dialog.setTitle("Create Club");
        dialog.setHeaderText("Create New Club");

        ButtonType createBtn = new ButtonType("Create", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = createClubForm(null);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btnType -> {
            if (btnType == createBtn) {
                return extractClubFromForm(grid, null);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(club -> {
            if (clubDAO.createClub(club)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Club created successfully!");
                loadClubs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create club. Name may already exist.");
            }
        });
    }

    private void showEditClubDialog(Club club) {
        Dialog<Club> dialog = new Dialog<>();
        dialog.setTitle("Edit Club");
        dialog.setHeaderText("Edit Club: " + club.getName());

        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = createClubForm(club);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btnType -> {
            if (btnType == saveBtn) {
                return extractClubFromForm(grid, club);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedClub -> {
            if (clubDAO.updateClub(updatedClub)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Club updated successfully!");
                loadClubs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update club.");
            }
        });
    }

    private GridPane createClubForm(Club club) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setPrefWidth(500);

        // Set column constraints to prevent label truncation
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(180);
        col1.setPrefWidth(180);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField nameField = new TextField(club != null ? club.getName() : "");
        nameField.setPromptText("Club Name");
        nameField.setUserData("name");

        TextArea descArea = new TextArea(club != null ? club.getDescription() : "");
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);
        descArea.setUserData("description");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("TECHNICAL", "CULTURAL", "SPORTS", "SOCIAL", "ACADEMIC");
        categoryCombo.setValue(club != null ? club.getCategory() : "TECHNICAL");
        categoryCombo.setUserData("category");

        // President ComboBox
        ComboBox<Student> presidentCombo = new ComboBox<>();
        presidentCombo.setPromptText("Select President (optional)");
        List<Student> students = studentDAO.getAllStudents();
        presidentCombo.setItems(FXCollections.observableArrayList(students));
        if (club != null && club.getPresidentStudentId() != null) {
            students.stream()
                    .filter(s -> s.getId() == club.getPresidentStudentId())
                    .findFirst()
                    .ifPresent(presidentCombo::setValue);
        }
        presidentCombo.setUserData("president");

        // Faculty Coordinator ComboBox
        ComboBox<Faculty> coordinatorCombo = new ComboBox<>();
        coordinatorCombo.setPromptText("Select Faculty Coordinator (optional)");
        List<Faculty> faculty = facultyDAO.getAllFaculty();
        coordinatorCombo.setItems(FXCollections.observableArrayList(faculty));
        if (club != null && club.getFacultyCoordinatorId() != null) {
            faculty.stream()
                    .filter(f -> f.getId() == club.getFacultyCoordinatorId())
                    .findFirst()
                    .ifPresent(coordinatorCombo::setValue);
        }
        coordinatorCombo.setUserData("coordinator");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("ACTIVE", "INACTIVE");
        statusCombo.setValue(club != null ? club.getStatus() : "ACTIVE");
        statusCombo.setUserData("status");

        int row = 0;
        grid.add(new Label("Club Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descArea, 1, row++);

        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row++);

        grid.add(new Label("President:"), 0, row);
        grid.add(presidentCombo, 1, row++);

        grid.add(new Label("Coordinator:"), 0, row);
        grid.add(coordinatorCombo, 1, row++);

        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);

        return grid;
    }

    @SuppressWarnings("unchecked")
    private Club extractClubFromForm(GridPane grid, Club existingClub) {
        Club club = existingClub != null ? existingClub : new Club();

        for (javafx.scene.Node node : grid.getChildren()) {
            if (node.getUserData() == null)
                continue;

            String field = (String) node.getUserData();
            switch (field) {
                case "name":
                    club.setName(((TextField) node).getText());
                    break;
                case "description":
                    club.setDescription(((TextArea) node).getText());
                    break;
                case "category":
                    club.setCategory(((ComboBox<String>) node).getValue());
                    break;
                case "status":
                    club.setStatus(((ComboBox<String>) node).getValue());
                    break;
                case "president":
                    Student president = ((ComboBox<Student>) node).getValue();
                    club.setPresidentStudentId(president != null ? president.getId() : null);
                    break;
                case "coordinator":
                    Faculty coordinator = ((ComboBox<Faculty>) node).getValue();
                    club.setFacultyCoordinatorId(coordinator != null ? coordinator.getId() : null);
                    break;
            }
        }

        return club;
    }

    @SuppressWarnings("unchecked")
    private void showMembersDialog(Club club) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Club Members");
        dialog.setHeaderText("Members of " + club.getName());
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

        TableColumn<ClubMembership, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10;");
                removeBtn.setOnAction(e -> {
                    ClubMembership membership = getTableView().getItems().get(getIndex());
                    removeMember(club, membership);
                    // Refresh table
                    getTableView().getItems().remove(membership);
                    loadClubs();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        actionCol.setPrefWidth(100);

        memberTable.getColumns().addAll(nameCol, roleCol, actionCol);

        List<ClubMembership> members = clubDAO.getClubMembers(club.getId());
        memberTable.setItems(FXCollections.observableArrayList(members));
        VBox.setVgrow(memberTable, Priority.ALWAYS);

        content.getChildren().add(memberTable);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private void showPendingApprovalsDialog(Club club) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Pending Join Requests");
        dialog.setHeaderText("Approve or Reject Join Requests for " + club.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setPrefHeight(400);

        TableView<ClubMembership> pendingTable = new TableView<>();
        TableColumn<ClubMembership, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        nameCol.setPrefWidth(200);

        TableColumn<ClubMembership, String> dateCol = new TableColumn<>("Requested On");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                new java.text.SimpleDateFormat("MMM dd, yyyy").format(data.getValue().getJoinedAt())));
        dateCol.setPrefWidth(150);

        TableColumn<ClubMembership, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            {
                approveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-padding: 5 15;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 15;");

                approveBtn.setOnAction(e -> {
                    ClubMembership membership = getTableView().getItems().get(getIndex());
                    if (clubDAO.approveMembership(membership.getId())) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Membership approved!");
                        getTableView().getItems().remove(membership);
                        loadClubs();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve membership.");
                    }
                });

                rejectBtn.setOnAction(e -> {
                    ClubMembership membership = getTableView().getItems().get(getIndex());
                    if (clubDAO.rejectMembership(membership.getId())) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Membership rejected.");
                        getTableView().getItems().remove(membership);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject membership.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, approveBtn, rejectBtn);
                    setGraphic(buttons);
                }
            }
        });
        actionCol.setPrefWidth(200);

        pendingTable.getColumns().addAll(nameCol, dateCol, actionCol);

        List<ClubMembership> pending = clubDAO.getPendingMemberships(club.getId());
        if (pending.isEmpty()) {
            Label noRequestsLabel = new Label("No pending join requests.");
            noRequestsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            content.getChildren().add(noRequestsLabel);
        } else {
            pendingTable.setItems(FXCollections.observableArrayList(pending));
            VBox.setVgrow(pendingTable, Priority.ALWAYS);
            content.getChildren().add(pendingTable);
        }

        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void showAnnouncementsDialog(Club club) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Club Announcements");
        dialog.setHeaderText("Announcements by " + club.getName());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setPrefHeight(500);

        // Post new announcement area
        VBox newPostBox = new VBox(10);
        newPostBox.setStyle(
                "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");

        Label postLabel = new Label("Post New Announcement");
        postLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Write your announcement here...");
        contentArea.setPrefRowCount(3);

        Button postBtn = createButton("Post", "#22c55e");
        postBtn.setOnAction(e -> {
            String title = titleField.getText();
            String body = contentArea.getText();
            if (title.isEmpty() || body.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Title and content are required.");
                return;
            }

            com.college.models.ClubAnnouncement ann = new com.college.models.ClubAnnouncement();
            ann.setClubId(club.getId());
            ann.setTitle(title);
            ann.setContent(body);
            // Assuming current user is posting (need to pass current userId to View or
            // fetch from session)
            // For now, let's use the ID passed to this View's constructor, presuming it's
            // the logged-in user.
            // Oh wait, `userId` is not stored as a field in this class except implicitly
            // via constructor arg.
            // I need to add `private int currentUserId;` to the class. Let's fix that.
            // Actually, I can use SessionManager.getInstance().getCurrentUser().getId() if
            // available,
            // or I should prompt the user content fix to store it.
            // Looking at `ClubManagementView` constructor: `public ClubManagementView(int
            // userId)`
            // I should store this userId.
            ann.setPostedBy(currentUserId);

            com.college.dao.ClubAnnouncementDAO annDAO = new com.college.dao.ClubAnnouncementDAO();
            if (annDAO.createAnnouncement(ann)) {
                titleField.clear();
                contentArea.clear();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Announcement posted!");
                // Refresh list
                loadAnnouncements(club, (VBox) content.getChildren().get(1)); // Hacky, better separate method
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to post announcement.");
            }
        });

        newPostBox.getChildren().addAll(postLabel, titleField, contentArea, postBtn);

        // List area
        VBox listArea = new VBox(10);
        ScrollPane scroll = new ScrollPane(listArea);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Initial load
        loadAnnouncements(club, listArea);

        content.getChildren().addAll(newPostBox, new Separator(), scroll);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void loadAnnouncements(Club club, VBox container) {
        container.getChildren().clear();
        com.college.dao.ClubAnnouncementDAO dao = new com.college.dao.ClubAnnouncementDAO();
        List<com.college.models.ClubAnnouncement> list = dao.getAnnouncementsByClub(club.getId());

        if (list.isEmpty()) {
            Label placeholder = new Label("No announcements yet.");
            placeholder.setTextFill(Color.GRAY);
            container.getChildren().add(placeholder);
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
                container.getChildren().add(card);
            }
        }
    }

    private void removeMember(Club club, ClubMembership membership) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Member");
        confirm.setHeaderText("Remove " + membership.getStudentName() + "?");
        confirm.setContentText("Remove this student from the club?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (clubDAO.leaveClub(club.getId(), membership.getStudentId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Member removed successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove member.");
            }
        }
    }

    private void deleteClub(Club club) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Club");
        confirm.setHeaderText("Delete " + club.getName() + "?");
        confirm.setContentText("This will remove all memberships. This action cannot be undone.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (clubDAO.deleteClub(club.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Club deleted successfully!");
                loadClubs();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete club.");
            }
        }
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
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
