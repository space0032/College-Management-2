package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.LearningResourceDAO;
import com.college.models.Course;
import com.college.models.LearningResource;
import com.college.models.ResourceCategory;
import com.college.models.User;
import com.college.services.FileUploadService;
import com.college.utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ResourceManagementView {

    private final LearningResourceDAO resourceDAO;
    private final CourseDAO courseDAO;
    private final FileUploadService fileUploadService;

    private ComboBox<Course> courseComboBox;
    private ComboBox<ResourceCategory> categoryComboBox;
    private TableView<LearningResource> resourceTable;

    public ResourceManagementView() {
        this.resourceDAO = new LearningResourceDAO();
        this.courseDAO = new CourseDAO();
        this.fileUploadService = new FileUploadService();
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header
        Label headerLabel = new Label("Resource Management");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 10, 0));

        // Filter by Course
        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("All Courses");
        loadCourses();
        courseComboBox.setOnAction(e -> loadResources());

        Button clearCourseBtn = new Button("X");
        clearCourseBtn.setOnAction(e -> {
            courseComboBox.getSelectionModel().clearSelection();
            loadResources();
        });

        // Search Bar
        searchField = new TextField();
        searchField.setPromptText("Search resources...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadResources(newVal));

        Button uploadButton = new Button("Upload Resource");
        uploadButton.setStyle("-fx-background-color: #14b8a6; -fx-text-fill: white;");
        uploadButton.setOnAction(e -> showUploadDialog());

        controls.getChildren().addAll(
                new Label("Filter:"), courseComboBox, clearCourseBtn,
                searchField,
                new Region(), uploadButton);
        HBox.setHgrow(controls.getChildren().get(4), Priority.ALWAYS); // Spacer

        // Table
        resourceTable = new TableView<>();
        setupTable();

        loadResources(); // Initial load

        VBox centerContent = new VBox(15);
        centerContent.getChildren().addAll(headerLabel, controls, resourceTable);
        VBox.setVgrow(resourceTable, Priority.ALWAYS);

        root.setCenter(centerContent);
        return root;
    }

    private void loadCourses() {
        List<Course> courses;
        SessionManager session = SessionManager.getInstance();
        if (session.isAdmin()) {
            courses = courseDAO.getAllCourses();
        } else {
            courses = courseDAO.getCoursesByFaculty(session.getUserId());
        }
        courseComboBox.setItems(FXCollections.observableArrayList(courses));
    }

    private void setupTable() {
        TableColumn<LearningResource, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<LearningResource, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(120);

        TableColumn<LearningResource, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCol.setPrefWidth(150);

        TableColumn<LearningResource, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedSize()));
        sizeCol.setPrefWidth(80);

        TableColumn<LearningResource, Boolean> publicCol = new TableColumn<>("Public");
        publicCol.setCellValueFactory(new PropertyValueFactory<>("public"));
        publicCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Yes" : "No");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: grey;");
                }
            }
        });
        publicCol.setPrefWidth(60);

        TableColumn<LearningResource, String> uploaderCol = new TableColumn<>("Uploaded By");
        uploaderCol.setCellValueFactory(new PropertyValueFactory<>("uploaderName"));
        uploaderCol.setPrefWidth(120);

        TableColumn<LearningResource, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final Button downloadBtn = new Button("Download");

            {
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    LearningResource r = getTableView().getItems().get(getIndex());
                    deleteResource(r);
                });

                downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                downloadBtn.setOnAction(event -> {
                    LearningResource r = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "File is stored at: " + r.getFilePath());
                    alert.show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, downloadBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        resourceTable.getColumns().addAll(titleCol, categoryCol, courseCol, sizeCol, publicCol, uploaderCol, actionCol);
    }

    private void loadResources() {
        Course selected = courseComboBox.getValue();
        List<LearningResource> list;
        if (selected != null) {
            list = resourceDAO.getResourcesByCourse(selected.getId());
        } else {
            list = resourceDAO.getAllResources();
        }
        resourceTable.setItems(FXCollections.observableArrayList(list));
    }

    private void showUploadDialog() {
        Dialog<LearningResource> dialog = new Dialog<>();
        dialog.setTitle("Upload Resource");
        dialog.setHeaderText("Upload new learning resource");

        ButtonType uploadBtnType = new ButtonType("Upload", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(uploadBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Resource Title");

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);

        ComboBox<Course> dialogCourseBox = new ComboBox<>();
        dialogCourseBox.setItems(courseComboBox.getItems());
        if (courseComboBox.getValue() != null)
            dialogCourseBox.setValue(courseComboBox.getValue());

        ComboBox<ResourceCategory> dialogCategoryBox = new ComboBox<>();
        dialogCategoryBox.setItems(FXCollections.observableArrayList(resourceDAO.getAllCategories()));

        CheckBox publicCheck = new CheckBox("Public (Visible to all students?)");

        Label fileLabel = new Label("No file selected");
        Button selectFileBtn = new Button("Select File");
        final File[] selectedFile = { null };

        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Resource File");
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
            }
        });

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Course (Opt):"), 0, 1);
        grid.add(dialogCourseBox, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(dialogCategoryBox, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descArea, 1, 3);
        grid.add(new Label("File:"), 0, 4);
        grid.add(selectFileBtn, 1, 4);
        grid.add(fileLabel, 1, 5);
        grid.add(publicCheck, 1, 6);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node uploadButton = dialog.getDialogPane().lookupButton(uploadBtnType);
        uploadButton.setDisable(true);

        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
                uploadButton.setDisable(false);
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == uploadBtnType) {
                if (dialogCategoryBox.getValue() == null || titleField.getText().isEmpty()) {
                    return null; // Should validate better
                }
                try {
                    String savedPath = fileUploadService.uploadResource(
                            new FileInputStream(selectedFile[0]),
                            selectedFile[0].getName(),
                            selectedFile[0].length());

                    if (savedPath != null) {
                        LearningResource r = new LearningResource();
                        r.setTitle(titleField.getText());
                        r.setDescription(descArea.getText());
                        r.setCourseId(dialogCourseBox.getValue() != null ? dialogCourseBox.getValue().getId() : 0);
                        r.setCategoryId(dialogCategoryBox.getValue().getId());
                        r.setFilePath(savedPath);
                        r.setFileType(
                                selectedFile[0].getName().substring(selectedFile[0].getName().lastIndexOf('.') + 1));
                        r.setFileSize(selectedFile[0].length());
                        r.setUploadedBy(SessionManager.getInstance().getUserId());
                        r.setPublic(publicCheck.isSelected());
                        return r;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(resource -> {
            if (resourceDAO.addResource(resource)) {
                loadResources();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Resource uploaded successfully!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload resource.");
                alert.show();
            }
        });
    }

    private void deleteResource(LearningResource resource) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete resource '" + resource.getTitle() + "'?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            resourceDAO.deleteResource(resource.getId());
            loadResources();
        }
    }
}
