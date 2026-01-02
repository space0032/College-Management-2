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
    @SuppressWarnings("unused")
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
        showAlert("Add Announcement", "Add announcement dialog would open here.");
    }

    private void editAnnouncement() {
        Announcement selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select an announcement to edit.");
            return;
        }
        showAlert("Edit Announcement", "Edit dialog for: " + selected.getTitle());
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
