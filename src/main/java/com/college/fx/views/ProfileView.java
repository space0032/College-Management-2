package com.college.fx.views;

import com.college.dao.StudentDAO;
import com.college.dao.FacultyDAO;
import com.college.models.Student;
import com.college.models.Faculty;

import com.college.utils.UserDisplayNameUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * JavaFX Profile View
 */
public class ProfileView {

    private VBox root;
    private String role;
    private int userId;
    private String username;

    public ProfileView(String role, int userId, String username) {
        this.role = role;
        this.userId = userId;
        this.username = username;
        createView();
    }

    private void createView() {
        root = new VBox(25);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f8fafc;");

        // Profile card
        VBox profileCard = new VBox(20);
        profileCard.setPadding(new Insets(40));
        profileCard.setMaxWidth(600);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;");

        // Avatar placeholder
        Label avatar = new Label(getInitials());
        avatar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        avatar.setTextFill(Color.WHITE);
        avatar.setAlignment(Pos.CENTER);
        avatar.setPrefSize(100, 100);
        avatar.setStyle(
                "-fx-background-color: #14b8a6;" +
                        "-fx-background-radius: 50;");

        // Name
        String displayName = UserDisplayNameUtil.getDisplayName(userId, role, username);
        Label nameLabel = new Label(displayName);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#0f172a"));

        // Role badge
        Label roleBadge = new Label(role);
        roleBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        roleBadge.setTextFill(Color.WHITE);
        roleBadge.setPadding(new Insets(5, 15, 5, 15));
        roleBadge.setStyle(
                "-fx-background-color: #14b8a6;" +
                        "-fx-background-radius: 20;");

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Profile details
        GridPane details = new GridPane();
        details.setHgap(30);
        details.setVgap(15);
        details.setAlignment(Pos.CENTER);

        loadProfileDetails(details);

        profileCard.getChildren().addAll(avatar, nameLabel, roleBadge, separator, details);
        root.getChildren().add(profileCard);
    }

    private String getInitials() {
        String displayName = UserDisplayNameUtil.getDisplayName(userId, role, username);
        String[] parts = displayName.split(" ");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
    }

    private void loadProfileDetails(GridPane details) {
        int row = 0;

        if (role.equals("STUDENT")) {
            Student student = new StudentDAO().getStudentByUserId(userId);
            if (student != null) {
                addDetailRow(details, "Email", student.getEmail(), row++);
                addDetailRow(details, "Phone", student.getPhone(), row++);
                addDetailRow(details, "Department", student.getDepartment(), row++);
                addDetailRow(details, "Semester", String.valueOf(student.getSemester()), row++);
                addDetailRow(details, "Batch", student.getBatch(), row++);
                addDetailRow(details, "Hostelite", student.isHostelite() ? "Yes" : "No", row++);
            }
        } else if (role.equals("FACULTY")) {
            Faculty faculty = new FacultyDAO().getFacultyByUserId(userId);
            if (faculty != null) {
                addDetailRow(details, "Email", faculty.getEmail(), row++);
                addDetailRow(details, "Phone", faculty.getPhone(), row++);
                addDetailRow(details, "Department", faculty.getDepartment(), row++);
                addDetailRow(details, "Qualification", faculty.getQualification(), row++);
            }
        } else {
            addDetailRow(details, "Username", username, row++);
            addDetailRow(details, "Role", role, row++);
        }
    }

    private void addDetailRow(GridPane grid, String label, String value, int row) {
        Label labelNode = new Label(label + ":");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        labelNode.setTextFill(Color.web("#64748b"));

        Label valueNode = new Label(value != null ? value : "-");
        valueNode.setFont(Font.font("Segoe UI", 14));
        valueNode.setTextFill(Color.web("#0f172a"));

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    public VBox getView() {
        return root;
    }
}
