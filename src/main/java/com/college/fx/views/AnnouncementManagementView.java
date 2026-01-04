package com.college.fx.views;

import com.college.dao.AnnouncementDAO;
import com.college.models.Announcement;

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
 * JavaFX Announcement Management View
 */
public class AnnouncementManagementView {

    private VBox root;
    private TableView<Announcement> tableView;
    private ObservableList<Announcement> announcementData;
    private AnnouncementDAO announcementDAO;
    private int userId;

    public AnnouncementManagementView(String role, int userId) {
        this.userId = userId;
        this.announcementDAO = new AnnouncementDAO();
        this.announcementData = FXCollections.observableArrayList();
        createView();
        loadAnnouncements();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;");

        Label title = new Label("Announcement Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadAnnouncements());

        header.getChildren().addAll(title, spacer, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox();
        section.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;");
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(announcementData);

        TableColumn<Announcement, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriorityIcon()));
        priorityCol.setPrefWidth(80);

        TableColumn<Announcement, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(200);

        TableColumn<Announcement, String> contentCol = new TableColumn<>("Content");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getContent().length() > 50
                        ? data.getValue().getContent().substring(0, 50) + "..."
                        : data.getValue().getContent()));
        contentCol.setPrefWidth(300);

        TableColumn<Announcement, String> targetCol = new TableColumn<>("Target");
        targetCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTargetAudience()));
        targetCol.setPrefWidth(100);

        TableColumn<Announcement, String> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Yes" : "No"));
        activeCol.setPrefWidth(80);

        tableView.getColumns().addAll(priorityCol, titleCol, contentCol, targetCol, activeCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        Button addBtn = createButton("Add Announcement", "#22c55e");
        addBtn.setOnAction(e -> addAnnouncement());

        Button editBtn = createButton("Edit", "#3b82f6");
        editBtn.setOnAction(e -> editAnnouncement());

        Button deleteBtn = createButton("Delete", "#ef4444");
        deleteBtn.setOnAction(e -> deleteAnnouncement());

        section.getChildren().addAll(addBtn, editBtn, deleteBtn);
        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setPrefHeight(40);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        return btn;
    }

    private void loadAnnouncements() {
        announcementData.clear();
        List<Announcement> announcements = announcementDAO.getAllAnnouncements();
        announcementData.addAll(announcements);
    }

    private void addAnnouncement() {
        showAnnouncementDialog(null);
    }

    private void editAnnouncement() {
        Announcement selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an announcement to edit.");
            return;
        }
        showAnnouncementDialog(selected);
    }

    private void showAnnouncementDialog(Announcement announcement) {
        Dialog<Announcement> dialog = new Dialog<>();
        dialog.setTitle(announcement == null ? "Add Announcement" : "Edit Announcement");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        if (announcement != null)
            titleField.setText(announcement.getTitle());

        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Content");
        contentArea.setPrefRowCount(3);
        if (announcement != null)
            contentArea.setText(announcement.getContent());

        ComboBox<String> targetBox = new ComboBox<>();
        targetBox.getItems().addAll("ALL", "STUDENTS", "FACULTY", "STUDENTS_FACULTY");
        targetBox.setValue(announcement != null ? announcement.getTargetAudience() : "ALL");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("NORMAL", "HIGH", "URGENT", "LOW");
        priorityBox.setValue(announcement != null ? announcement.getPriority() : "NORMAL");

        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(announcement == null || announcement.isActive());

        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-text-fill: black;");
        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);

        Label contentLabel = new Label("Content:");
        contentLabel.setStyle("-fx-text-fill: black;");
        grid.add(contentLabel, 0, 1);
        grid.add(contentArea, 1, 1);

        Label targetLabel = new Label("Target:");
        targetLabel.setStyle("-fx-text-fill: black;");
        grid.add(targetLabel, 0, 2);
        grid.add(targetBox, 1, 2);

        Label priorityLabel = new Label("Priority:");
        priorityLabel.setStyle("-fx-text-fill: black;");
        grid.add(priorityLabel, 0, 3);
        grid.add(priorityBox, 1, 3);

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-text-fill: black;");
        grid.add(statusLabel, 0, 4);
        grid.add(activeCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Announcement result = announcement != null ? announcement : new Announcement();
                result.setTitle(titleField.getText());
                result.setContent(contentArea.getText());
                result.setTargetAudience(targetBox.getValue());
                result.setPriority(priorityBox.getValue());
                result.setActive(activeCheck.isSelected());
                result.setCreatedBy(userId); // Ensure userId is used
                return result;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (announcement == null) {
                if (announcementDAO.addAnnouncement(result) > 0) {
                    loadAnnouncements();
                    showAlert("Success", "Announcement added successfully!");
                } else {
                    showAlert("Error", "Failed to add announcement.");
                }
            } else {
                if (announcementDAO.updateAnnouncement(result)) {
                    loadAnnouncements();
                    showAlert("Success", "Announcement updated successfully!");
                } else {
                    showAlert("Error", "Failed to update announcement.");
                }
            }
        });
    }

    private void deleteAnnouncement() {
        Announcement selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an announcement to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Announcement");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete announcement: " + selected.getTitle() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (announcementDAO.deleteAnnouncement(selected.getId())) {
                    loadAnnouncements();
                    showAlert("Success", "Announcement deleted successfully!");
                } else {
                    showAlert("Error", "Failed to delete announcement.");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
