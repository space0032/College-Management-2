package com.college.fx.views;

import com.college.dao.CourseDAO;
import com.college.dao.SyllabusDAO;
import com.college.models.Course;
import com.college.models.Syllabus;
import com.college.services.FileUploadService;
import com.college.utils.SessionManager;

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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SyllabusManagementView {

    private static final String SVG_UPLOAD = "M9 16h6v-6h4l-7-7-7 7h4zm-4 2h14v2H5z";
    private static final String SVG_REFRESH = "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z";

    private final SyllabusDAO syllabusDAO;
    private final CourseDAO courseDAO;
    private final FileUploadService fileUploadService;

    private ComboBox<Course> courseComboBox;
    private TableView<Syllabus> syllabusTable;
    private BorderPane root;

    public SyllabusManagementView() {
        this.syllabusDAO = new SyllabusDAO();
        this.courseDAO = new CourseDAO();
        this.fileUploadService = new FileUploadService();
    }

    public BorderPane getView() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane"); // Glassmorphism background

        // Header
        Label headerLabel = new Label("Syllabus Management");
        headerLabel.getStyleClass().add("section-title"); // Use styled class

        // Controls
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(15));
        controls.getStyleClass().add("glass-card"); // Card style for controls

        courseComboBox = new ComboBox<>();
        courseComboBox.setPromptText("Select Course (Ctrl+F)");
        courseComboBox.setPrefWidth(250);
        loadCourses();
        courseComboBox.setOnAction(e -> loadSyllabi());

        Button uploadButton = new Button("Upload Syllabus");
        uploadButton.setGraphic(createIcon(SVG_UPLOAD));
        uploadButton.getStyleClass().addAll("accent-button", "icon-button");
        uploadButton.setOnAction(e -> showUploadDialog());
        uploadButton.disableProperty().bind(courseComboBox.valueProperty().isNull());

        Button refreshButton = new Button("Refresh");
        refreshButton.setGraphic(createIcon(SVG_REFRESH));
        refreshButton.getStyleClass().add("icon-button");
        refreshButton.setOnAction(e -> loadSyllabi());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(courseComboBox, spacer, refreshButton, uploadButton);

        // Table
        syllabusTable = new TableView<>();
        syllabusTable.getStyleClass().add("glass-table"); // Ensure glass styling
        VBox.setVgrow(syllabusTable, Priority.ALWAYS);
        setupTable();

        VBox centerContent = new VBox(20); // More spacing
        centerContent.getChildren().addAll(headerLabel, controls, syllabusTable);

        root.setCenter(centerContent);

        // Keyboard Shortcuts
        setupShortcuts(uploadButton);

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

    private void setupShortcuts(Button uploadButton) {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN).match(event)) {
                if (!uploadButton.isDisabled()) {
                    showUploadDialog();
                    event.consume();
                }
            } else if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                courseComboBox.requestFocus();
                courseComboBox.show();
                event.consume();
            } else if (event.getCode() == KeyCode.F5) {
                loadSyllabi();
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
        TableColumn<Syllabus, String> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        versionCol.setPrefWidth(80);

        TableColumn<Syllabus, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> {
            String path = cellData.getValue().getFilePath();
            String icon = "📄";
            if (path != null) {
                String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                icon = getFileIcon(ext);
            }
            return new javafx.beans.property.SimpleStringProperty(icon + " " + cellData.getValue().getTitle());
        });
        titleCol.setPrefWidth(230);

        TableColumn<Syllabus, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<Syllabus, String> dateCol = new TableColumn<>("Uploaded Date");
        dateCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        dateCol.setPrefWidth(150);

        TableColumn<Syllabus, String> uploaderCol = new TableColumn<>("Uploaded By");
        uploaderCol.setCellValueFactory(new PropertyValueFactory<>("uploaderName"));
        uploaderCol.setPrefWidth(120);

        TableColumn<Syllabus, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final Button downloadBtn = new Button("Download");

            {
                deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    Syllabus s = getTableView().getItems().get(getIndex());
                    deleteSyllabus(s);
                });

                downloadBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                downloadBtn.setOnAction(event -> {
                    Syllabus s = getTableView().getItems().get(getIndex());
                    String path = s.getFilePath();
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
                                alert.show();
                            } catch (Exception ex) {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Download failed: " + ex.getMessage());
                                alert.show();
                            }
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "File is stored locally at: " + path);
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

        syllabusTable.getColumns().addAll(versionCol, titleCol, descCol, dateCol, uploaderCol, actionCol);
    }

    private void loadSyllabi() {
        Course selected = courseComboBox.getValue();
        if (selected != null) {
            List<Syllabus> list = syllabusDAO.getSyllabiByCourse(selected.getId());
            syllabusTable.setItems(FXCollections.observableArrayList(list));
        } else {
            syllabusTable.getItems().clear();
        }
    }

    private void showUploadDialog() {
        Dialog<Syllabus> dialog = new Dialog<>();
        dialog.setTitle("Upload Syllabus");
        dialog.setHeaderText("Upload new version of syllabus for " + courseComboBox.getValue().getName());
        dialog.getDialogPane().setPrefWidth(500);

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
        titleField.setPromptText("Syllabus Title");

        TextField versionField = new TextField("1.0");

        TextArea descArea = new TextArea();
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        Label fileLabel = new Label("No file selected");
        Button selectFileBtn = new Button("Select File (PDF/Docs)");
        final File[] selectedFile = { null };

        // Inline Error Label
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        selectFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Syllabus File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.doc", "*.docx"));
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText(file.getName());
                errorLabel.setVisible(false);
            }
        });

        // Remove error when typing
        titleField.textProperty().addListener((obs, old, newVal) -> errorLabel.setVisible(false));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Version:"), 0, 1);
        grid.add(versionField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descArea, 1, 2);
        grid.add(new Label("File:"), 0, 3);
        grid.add(selectFileBtn, 1, 3);
        grid.add(fileLabel, 1, 4);
        grid.add(errorLabel, 1, 5); // Add error label at bottom

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node uploadButton = dialog.getDialogPane().lookupButton(uploadBtnType);

        // Custom validation handler
        uploadButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (titleField.getText().trim().isEmpty()) {
                errorLabel.setText("Title is required.");
                errorLabel.setVisible(true);
                event.consume(); // Prevent dialog close
                return;
            }
            if (selectedFile[0] == null) {
                errorLabel.setText("Please select a file.");
                errorLabel.setVisible(true);
                event.consume();
                return;
            }
            // If valid, let the event propagate to ResultConverter
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == uploadBtnType) {
                try {
                    String savedPath = fileUploadService.uploadSyllabus(
                            new FileInputStream(selectedFile[0]),
                            selectedFile[0].getName(),
                            selectedFile[0].length());

                    if (savedPath != null) {
                        Syllabus s = new Syllabus();
                        s.setCourseId(courseComboBox.getValue().getId());
                        s.setTitle(titleField.getText());
                        s.setVersion(versionField.getText());
                        s.setDescription(descArea.getText());
                        s.setFilePath(savedPath);
                        s.setUploadedBy(SessionManager.getInstance().getUserId());
                        return s;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // We can't easily show error in dialog here as it's closing,
                    // but we consumed event above for validation errors.
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(syllabus -> {
            if (syllabusDAO.addSyllabus(syllabus)) {
                loadSyllabi();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Syllabus uploaded successfully!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload syllabus to database.");
                alert.show();
            }
        });
    }

    private void deleteSyllabus(Syllabus syllabus) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete syllabus version " + syllabus.getVersion() + "?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            fileUploadService.deleteFile(syllabus.getFilePath());
            syllabusDAO.deleteSyllabus(syllabus.getId());
            loadSyllabi();
        }
    }

    private String getFileIcon(String type) {
        if (type == null)
            return "📄";
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
