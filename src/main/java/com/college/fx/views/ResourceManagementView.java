package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.LearningResourceDAO;
import com.college.models.Course;
import com.college.models.LearningResource;
import com.college.models.ResourceCategory;
import com.college.services.FileUploadService;
import com.college.utils.SessionManager;
import com.college.utils.DialogUtils;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;

public class ResourceManagementView {

    private static final String SVG_UPLOAD = "M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z";
    private static final String SVG_REFRESH = "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z";

    private final LearningResourceDAO resourceDAO;
    private final CourseDAO courseDAO;
    private final FileUploadService fileUploadService;

    private ComboBox<Course> courseComboBox;
    private TableView<LearningResource> resourceTable;
    private TextField searchField;
    private BorderPane root;

    public ResourceManagementView() {
        this.resourceDAO = new LearningResourceDAO();
        this.courseDAO = new CourseDAO();
        this.fileUploadService = new FileUploadService();
    }

    public BorderPane getView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        Label headerLabel = new Label("Resource Management");
        headerLabel.getStyleClass().add("section-title");

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(15));
        controls.getStyleClass().add("glass-card");

        // Filter by Course
        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("All Courses");
        courseComboBox.setPrefWidth(200);
        loadCourses();
        courseComboBox.setOnAction(e -> loadResources());

        Button clearCourseBtn = new Button("X");
        clearCourseBtn.getStyleClass().add("small-button");
        clearCourseBtn.setTooltip(new Tooltip("Clear Course Filter"));
        clearCourseBtn.setOnAction(e -> {
            courseComboBox.getSelectionModel().clearSelection();
            loadResources();
        });

        // Search Bar
        searchField = new TextField();
        searchField.setPromptText("Search resources... (Ctrl+F)");
        searchField.setPrefWidth(250);
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadResources(newVal));

        Button uploadButton = new Button("Upload Resource");
        uploadButton.setGraphic(createIcon(SVG_UPLOAD));
        uploadButton.getStyleClass().addAll("accent-button", "icon-button");
        uploadButton.setOnAction(e -> showUploadDialog());

        Button refreshButton = new Button("Refresh");
        refreshButton.setGraphic(createIcon(SVG_REFRESH));
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setOnAction(e -> loadResources());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(
                courseComboBox, clearCourseBtn,
                searchField,
                spacer, refreshButton, uploadButton);

        // Table
        resourceTable = new TableView<>();
        resourceTable.getStyleClass().add("glass-table");
        setupTable();

        loadResources(); // Initial load

        VBox centerContent = new VBox(20);
        centerContent.getChildren().addAll(headerLabel, controls, resourceTable);
        VBox.setVgrow(resourceTable, Priority.ALWAYS);

        root.setCenter(centerContent);

        // Keyboard Shortcuts
        setupShortcuts();

        return root;
    }

    private javafx.scene.shape.SVGPath createIcon(String pathContent) {
        javafx.scene.shape.SVGPath icon = new javafx.scene.shape.SVGPath();
        icon.setContent(pathContent);
        icon.setFill(Color.WHITE);
        icon.setScaleX(0.9);
        icon.setScaleY(0.9);
        return icon;
    }

    private void setupShortcuts() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                showUploadDialog();
                event.consume();
            } else if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                searchField.requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.F5) {
                loadResources();
                event.consume();
            }
        });
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
        titleCol.setCellValueFactory(cellData -> {
            String icon = getFileIcon(cellData.getValue().getFileType());
            return new javafx.beans.property.SimpleStringProperty(icon + " " + cellData.getValue().getTitle());
        });
        titleCol.setPrefWidth(230);

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
                    String path = r.getFilePath();
                    if (path != null && (path.startsWith("http") || path.startsWith("www"))) {
                        com.college.MainFX.getHostServicesInstance().showDocument(path);
                    } else if (path != null && path.startsWith("/")) {
                        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
                        fileChooser.setTitle("Save File");
                        fileChooser.setInitialFileName(path.substring(path.lastIndexOf("/") + 1));
                        java.io.File dest = fileChooser.showSaveDialog(getTableView().getScene().getWindow());
                        if (dest != null) {
                            try {
                                fileUploadService.downloadFile(path, dest);
                                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                        "File downloaded to: " + dest.getAbsolutePath());
                                DialogUtils.styleDialog(alert);
                                alert.show();
                            } catch (Exception ex) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Download failed: " + ex.getMessage());
                                DialogUtils.styleDialog(alert);
                                alert.show();
                            }
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "File is stored locally at: " + path);
                        DialogUtils.styleDialog(alert);
                        alert.show();
                    }
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

        resourceTable.getColumns().addAll(
                java.util.Arrays.asList(titleCol, categoryCol, courseCol, sizeCol, publicCol, uploaderCol, actionCol));
    }

    private void loadResources() {
        loadResources(searchField.getText());
    }

    private void loadResources(String searchQuery) {
        Course selected = courseComboBox.getValue();
        List<LearningResource> list;
        if (selected != null) {
            list = resourceDAO.getResourcesByCourse(selected.getId());
        } else {
            list = resourceDAO.getAllResources();
        }

        // Filter by search query
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            final String query = searchQuery.toLowerCase();
            list.removeIf(r -> !r.getTitle().toLowerCase().contains(query) &&
                    !(r.getDescription() != null && r.getDescription().toLowerCase().contains(query)));
        }

        resourceTable.setItems(FXCollections.observableArrayList(list));
    }

    private void showUploadDialog() {
        Dialog<LearningResource> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Upload Resource");
        dialog.setHeaderText("Upload new learning resource");
        dialog.getDialogPane().setPrefWidth(550);

        ButtonType uploadBtnType = new ButtonType("Upload", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(uploadBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        col1.setPrefWidth(100);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

        TextField titleField = new TextField();
        titleField.setPromptText("Resource Title");

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        ComboBox<Course> dialogCourseBox = new ComboBox<>();
        dialogCourseBox.setItems(courseComboBox.getItems());
        if (courseComboBox.getValue() != null)
            dialogCourseBox.setValue(courseComboBox.getValue());

        ComboBox<ResourceCategory> dialogCategoryBox = new ComboBox<>();
        dialogCategoryBox.setItems(FXCollections.observableArrayList(resourceDAO.getAllCategories()));
        dialogCategoryBox.setPromptText("Select Category");

        CheckBox publicCheck = new CheckBox("Public (Visible to all students?)");

        Label fileLabel = new Label("No file selected");
        Button selectFileBtn = new Button("Select File");
        final File[] selectedFile = { null };

        // Inline Error Label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Resource File");
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
                errorLabel.setVisible(false);
            }
        });

        // Validation listeners
        titleField.textProperty().addListener((obs, old, newVal) -> errorLabel.setVisible(false));
        dialogCategoryBox.valueProperty().addListener((obs, old, newVal) -> errorLabel.setVisible(false));

        DialogUtils.addFormRow(grid, "Title:", titleField, 0);
        DialogUtils.addFormRow(grid, "Course (Opt):", dialogCourseBox, 1);
        DialogUtils.addFormRow(grid, "Category:", dialogCategoryBox, 2);
        DialogUtils.addFormRow(grid, "Description:", descArea, 3);
        DialogUtils.addFormRow(grid, "File:", selectFileBtn, 4);
        grid.add(fileLabel, 1, 5);
        grid.add(publicCheck, 1, 6);
        grid.add(errorLabel, 1, 7);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node uploadButton = dialog.getDialogPane().lookupButton(uploadBtnType);

        // Custom validation handler
        uploadButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (titleField.getText().trim().isEmpty()) {
                errorLabel.setText("Title is required.");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            if (dialogCategoryBox.getValue() == null) {
                errorLabel.setText("Please select a category.");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            if (selectedFile[0] == null) {
                errorLabel.setText("Please select a file.");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == uploadBtnType) {
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
                DialogUtils.styleDialog(alert);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload resource to database.");
                DialogUtils.styleDialog(alert);
                alert.show();
            }
        });
    }

    private void deleteResource(LearningResource resource) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete resource '" + resource.getTitle() + "'?");
        DialogUtils.styleDialog(alert);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            fileUploadService.deleteFile(resource.getFilePath());
            resourceDAO.deleteResource(resource.getId());
            loadResources();
        }
    }

    private String getFileIcon(String type) {
        if (type == null)
            return "📄";
        type = type.toLowerCase();
        if (type.equals("pdf"))
            return "📕";
        if (type.contains("doc"))
            return "📘";
        if (type.contains("xls"))
            return "📊";
        if (type.contains("ppt"))
            return "📽️";
        if (type.contains("jpg") || type.contains("png"))
            return "🖼️";
        if (type.contains("zip") || type.contains("rar"))
            return "📦";
        return "📄";
    }
}
