package com.college.fx.views;

import com.college.dao.StudentDAO;
import com.college.models.Student;
import com.college.services.TranscriptService;
import com.college.models.Grade;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * Enhanced Student Profile View
 */
public class StudentProfileView {

    private VBox root;
    private StudentDAO studentDAO;
    private Student student;
    private boolean isEditable;
    private Runnable onUpdateCallback;

    // UI Components
    private ImageView profileImageView;
    private Label nameLabel;
    private Label idLabel;

    public StudentProfileView(Student student, boolean isEditable, Runnable onUpdateCallback) {
        this.student = student;
        this.isEditable = isEditable;
        this.onUpdateCallback = onUpdateCallback;
        this.studentDAO = new StudentDAO();
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        // Header Section
        HBox header = createHeader();

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab personalTab = new Tab("Personal Details");
        personalTab.setContent(createPersonalTab());

        Tab familyTab = new Tab("Family & Background");
        familyTab.setContent(createFamilyTab());

        Tab academicTab = new Tab("Academic Record & Transcript");
        academicTab.setContent(createAcademicTab());

        Tab extracurricularTab = new Tab("Extracurriculars");
        extracurricularTab.setContent(createExtracurricularTab());

        tabPane.getTabs().addAll(personalTab, familyTab, academicTab, extracurricularTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        root.getChildren().addAll(header, tabPane);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10;");

        // Profile Image
        profileImageView = new ImageView();
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setPreserveRatio(true);
        updateProfileImageDisplay();

        VBox imageBox = new VBox(5, profileImageView);
        if (isEditable) {
            Button uploadBtn = new Button("Upload Photo");
            uploadBtn.setStyle("-fx-font-size: 10px;");
            uploadBtn.setOnAction(e -> handleImageUpload());
            imageBox.getChildren().add(uploadBtn);
        }

        // Basic Info
        VBox infoBox = new VBox(5);
        nameLabel = new Label(student.getName());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        idLabel = new Label("Student ID: " + student.getId() + " | " + student.getCourse() + " | Semester "
                + student.getSemester());
        idLabel.setTextFill(Color.GRAY);

        infoBox.getChildren().addAll(nameLabel, idLabel);

        header.getChildren().addAll(imageBox, infoBox);
        return header;
    }

    private VBox createPersonalTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // Fields
        TextField emailField = new TextField(student.getEmail());
        TextField phoneField = new TextField(student.getPhone());
        DatePicker dobPicker = new DatePicker();
        if (student.getDob() != null) {
            dobPicker.setValue(student.getDob().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Other");
        genderCombo.setValue(student.getGender());

        ComboBox<String> bloodGroupCombo = new ComboBox<>();
        bloodGroupCombo.getItems().addAll("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-");
        bloodGroupCombo.setValue(student.getBloodGroup());

        TextField addressField = new TextField(student.getAddress());
        TextField nationalityField = new TextField(student.getNationality());

        addFormRow(grid, "Email:", emailField, 0);
        addFormRow(grid, "Phone:", phoneField, 1);
        addFormRow(grid, "Date of Birth:", dobPicker, 2);
        addFormRow(grid, "Gender:", genderCombo, 3);
        addFormRow(grid, "Blood Group:", bloodGroupCombo, 4);
        addFormRow(grid, "Nationality:", nationalityField, 5);
        addFormRow(grid, "Address:", addressField, 6);

        if (isEditable) {
            Button saveBtn = new Button("Save Personal Details");
            saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
            saveBtn.setOnAction(e -> {
                student.setEmail(emailField.getText());
                student.setPhone(phoneField.getText());
                if (dobPicker.getValue() != null) {
                    student.setDob(Date.from(dobPicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                }
                student.setGender(genderCombo.getValue());
                student.setBloodGroup(bloodGroupCombo.getValue());
                student.setNationality(nationalityField.getText());
                student.setAddress(addressField.getText());

                updateStudent();
            });
            content.getChildren().addAll(grid, new Separator(), saveBtn);
        } else {
            disableInputs(emailField, phoneField, dobPicker, genderCombo, bloodGroupCombo, addressField,
                    nationalityField);
            content.getChildren().add(grid);
        }

        return content;
    }

    private VBox createFamilyTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        TextField fatherName = new TextField(student.getFatherName());
        TextField motherName = new TextField(student.getMotherName());
        TextField guardianContact = new TextField(student.getGuardianContact());
        TextField prevSchool = new TextField(student.getPreviousSchool());
        TextField tenthPerc = new TextField(String.valueOf(student.getTenthPercentage()));
        TextField twelfthPerc = new TextField(String.valueOf(student.getTwelfthPercentage()));

        addFormRow(grid, "Father's Name:", fatherName, 0);
        addFormRow(grid, "Mother's Name:", motherName, 1);
        addFormRow(grid, "Guardian Contact:", guardianContact, 2);
        addFormRow(grid, "Previous School:", prevSchool, 3);
        addFormRow(grid, "10th %:", tenthPerc, 4);
        addFormRow(grid, "12th %:", twelfthPerc, 5);

        if (isEditable) {
            Button saveBtn = new Button("Save Family Details");
            saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
            saveBtn.setOnAction(e -> {
                student.setFatherName(fatherName.getText());
                student.setMotherName(motherName.getText());
                student.setGuardianContact(guardianContact.getText());
                student.setPreviousSchool(prevSchool.getText());
                try {
                    student.setTenthPercentage(Double.parseDouble(tenthPerc.getText()));
                    student.setTwelfthPercentage(Double.parseDouble(twelfthPerc.getText()));
                } catch (NumberFormatException ex) {
                    // Ignore
                }
                updateStudent();
            });
            content.getChildren().addAll(grid, new Separator(), saveBtn);
        } else {
            disableInputs(fatherName, motherName, guardianContact, prevSchool, tenthPerc, twelfthPerc);
            content.getChildren().add(grid);
        }

        return content;
    }

    private VBox createExtracurricularTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label lbl = new Label("Activities & Achievements");
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        TextArea activitiesArea = new TextArea(student.getExtracurricularActivities());
        activitiesArea.setPromptText("List sports, clubs, awards, etc.");
        activitiesArea.setPrefHeight(200);

        if (isEditable) {
            Button saveBtn = new Button("Save Activities");
            saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white;");
            saveBtn.setOnAction(e -> {
                student.setExtracurricularActivities(activitiesArea.getText());
                updateStudent();
            });
            content.getChildren().addAll(lbl, activitiesArea, saveBtn);
        } else {
            activitiesArea.setEditable(false);
            content.getChildren().addAll(lbl, activitiesArea);
        }

        return content;
    }

    private VBox createAcademicTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Transcript Summary
        TranscriptService ts = new TranscriptService();
        TranscriptService.TranscriptSummary summary = ts.generateTranscript(student);

        HBox summaryBox = new HBox(30);
        summaryBox.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 15; -fx-background-radius: 8;");

        VBox cgpaBox = new VBox(5);
        Label cgpaLbl = new Label("CGPA");
        cgpaLbl.setTextFill(Color.GRAY);
        Label cgpaVal = new Label(String.format("%.2f", summary.getCgpa()));
        cgpaVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        cgpaVal.setTextFill(Color.web("#0284c7"));
        cgpaBox.getChildren().addAll(cgpaLbl, cgpaVal);
        cgpaBox.setAlignment(Pos.CENTER);

        summaryBox.getChildren().add(cgpaBox);

        // Add semester SGPAs
        for (Map.Entry<Integer, Double> entry : summary.getSemesterSgpa().entrySet()) {
            VBox sgpaBox = new VBox(5);
            Label sgpaLbl = new Label("Sem " + entry.getKey());
            sgpaLbl.setTextFill(Color.GRAY);
            Label sgpaVal = new Label(String.format("%.2f", entry.getValue()));
            sgpaVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            sgpaBox.getChildren().addAll(sgpaLbl, sgpaVal);
            sgpaBox.setAlignment(Pos.CENTER);
            summaryBox.getChildren().add(sgpaBox);
        }

        TableView<Grade> gradeTable = new TableView<>();
        // ... (standard table setup for grades, omitting for brevity in this snippet)

        Button printTranscriptBtn = new Button("Export Transcript (PDF)");
        printTranscriptBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "PDF Export feature coming soon!");
            alert.show();
        });

        content.getChildren().addAll(new Label("Academic Summary"), summaryBox, new Separator(), printTranscriptBtn);
        return content;
    }

    // Helper methods
    private void addFormRow(GridPane grid, String label, Control field, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(field, 1, row);
    }

    private void disableInputs(Control... controls) {
        for (Control c : controls) {
            if (c instanceof TextInputControl)
                ((TextInputControl) c).setEditable(false);
            else
                c.setDisable(true);
        }
    }

    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Ensure directory exists
                File destDir = new File("user_data/profiles");
                if (!destDir.exists())
                    destDir.mkdirs();

                File destFile = new File(destDir,
                        "student_" + student.getId() + "_" + System.currentTimeMillis() + ".jpg");
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                student.setProfilePhotoPath(destFile.getAbsolutePath());
                updateStudent();
                updateProfileImageDisplay();

            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to upload image.").show();
            }
        }
    }

    private void updateProfileImageDisplay() {
        if (student.getProfilePhotoPath() != null && !student.getProfilePhotoPath().isEmpty()) {
            File imgFile = new File(student.getProfilePhotoPath());
            if (imgFile.exists()) {
                profileImageView.setImage(new Image(imgFile.toURI().toString()));
            }
        } else {
            // Placeholder
            // Using a default colored rectangle or simple placeholder logic if no image
            // For now, leaving empty or simple icon logic can be added
        }
    }

    private void updateStudent() {
        if (studentDAO.updateStudent(student)) {
            new Alert(Alert.AlertType.INFORMATION, "Profile Updated Successfully!").show();
            if (onUpdateCallback != null)
                onUpdateCallback.run();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to update profile.").show();
        }
    }

    public VBox getView() {
        return root;
    }
}
