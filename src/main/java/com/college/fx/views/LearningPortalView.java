package com.college.fx.views;

import com.college.dao.LearningResourceDAO;
import com.college.dao.StudentDAO;
import com.college.dao.SyllabusDAO;
import com.college.models.Course;
import com.college.models.LearningResource;
import com.college.models.Student;
import com.college.models.Syllabus;

import com.college.utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LearningPortalView {

    private final SyllabusDAO syllabusDAO;
    private final LearningResourceDAO resourceDAO;

    private final StudentDAO studentDAO;
    private Student currentStudent;

    private TabPane tabPane;

    public LearningPortalView() {
        this.syllabusDAO = new SyllabusDAO();
        this.resourceDAO = new LearningResourceDAO();

        this.studentDAO = new StudentDAO();

        SessionManager session = SessionManager.getInstance();
        if (session.isStudent()) {
            this.currentStudent = studentDAO.getStudentByUserId(session.getUserId());
        }
    }

    public BorderPane getView() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane"); // Dark background
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label header = new Label("Learning Portal");
        header.getStyleClass().add("section-title");
        root.setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0)); // Add spacing below header

        tabPane = new TabPane();
        tabPane.getStyleClass().add("floating"); // Floating tab style
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(createSyllabusTab());
        tabPane.getTabs().add(createResourcesTab());

        root.setCenter(tabPane);

        return root;
    }

    private Tab createSyllabusTab() {
        Tab tab = new Tab("Course Syllabi");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ComboBox<Course> courseFilter = new ComboBox<>();
        courseFilter.setPromptText("Select Course");

        // Load student courses
        List<Course> courses = new ArrayList<>();
        if (currentStudent != null) {
            courses = studentDAO.getRegisteredCourses(currentStudent.getId());
        }
        courseFilter.setItems(FXCollections.observableArrayList(courses));

        TableView<Syllabus> table = new TableView<>();

        TableColumn<Syllabus, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseId"));

        TableColumn<Syllabus, String> versionCol = new TableColumn<>("Version");
        versionCol.setCellValueFactory(new PropertyValueFactory<>("version"));

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
        titleCol.setPrefWidth(250);

        TableColumn<Syllabus, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getUploadedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        TableColumn<Syllabus, Void> downloadCol = new TableColumn<>("Download");
        downloadCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Download");
            {
                btn.setOnAction(e -> {
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
                                new com.college.services.FileUploadService().downloadFile(path, dest);
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
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(java.util.Arrays.asList(titleCol, versionCol, dateCol, downloadCol));

        courseFilter.setOnAction(e -> {
            Course c = courseFilter.getValue();
            if (c != null) {
                table.setItems(FXCollections.observableArrayList(syllabusDAO.getSyllabiByCourse(c.getId())));
            }
        });

        // Auto select first course
        if (!courses.isEmpty()) {
            courseFilter.getSelectionModel().selectFirst();
            courseFilter.fireEvent(new javafx.event.ActionEvent());
        }

        Label filterLabel = new Label("Filter by Course:");
        filterLabel.setStyle("-fx-text-fill: white;");
        content.getChildren().addAll(filterLabel, courseFilter, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        tab.setContent(content);
        return tab;
    }

    private Tab createResourcesTab() {
        Tab tab = new Tab("Learning Resources");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("glass-card");

        TextField searchField = new TextField();
        searchField.setPromptText("Search resources by title or course...");
        searchField.setMaxWidth(400);
        searchField.getStyleClass().add("search-field"); // Use styled search field

        TableView<LearningResource> table = new TableView<>();
        table.getStyleClass().add("glass-table");

        TableColumn<LearningResource, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> {
            String icon = getFileIcon(cellData.getValue().getFileType());
            return new javafx.beans.property.SimpleStringProperty(icon + " " + cellData.getValue().getTitle());
        });
        titleCol.setPrefWidth(250);

        TableColumn<LearningResource, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        catCol.setPrefWidth(120);

        TableColumn<LearningResource, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCol.setPrefWidth(150);

        TableColumn<LearningResource, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedSize()));
        sizeCol.setPrefWidth(80);

        TableColumn<LearningResource, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Download");
            {
                btn.getStyleClass().addAll("accent-button", "small-button");
                btn.setOnAction(e -> {
                    LearningResource r = getTableView().getItems().get(getIndex());
                    resourceDAO.incrementDownloadCount(r.getId());
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
                                new com.college.services.FileUploadService().downloadFile(path, dest);
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
                setGraphic(empty ? null : btn);
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        table.getColumns().addAll(java.util.Arrays.asList(titleCol, catCol, courseCol, sizeCol, actionCol));

        List<LearningResource> allResources = resourceDAO.getAllResources();
        List<LearningResource> filtered = new ArrayList<>();
        List<Integer> enrolledCourseIds = new ArrayList<>();
        if (currentStudent != null) {
            studentDAO.getRegisteredCourses(currentStudent.getId()).forEach(c -> enrolledCourseIds.add(c.getId()));
        }

        for (LearningResource r : allResources) {
            if (r.isPublic() || enrolledCourseIds.contains(r.getCourseId())) {
                filtered.add(r);
            }
        }

        table.setItems(FXCollections.observableArrayList(filtered));

        // Add listener for search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                table.setItems(FXCollections.observableArrayList(filtered));
            } else {
                String lower = newVal.toLowerCase();
                List<LearningResource> searchResults = new ArrayList<>();
                for (LearningResource r : filtered) {
                    if (r.getTitle().toLowerCase().contains(lower) ||
                            (r.getCourseName() != null && r.getCourseName().toLowerCase().contains(lower))) {
                        searchResults.add(r);
                    }
                }
                table.setItems(FXCollections.observableArrayList(searchResults));
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(searchField, table);
        tab.setContent(content);
        return tab;
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
