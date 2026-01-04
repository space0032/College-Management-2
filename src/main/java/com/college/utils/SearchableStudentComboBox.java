package com.college.utils;

import com.college.models.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Searchable Student ComboBox
 * Allows filtering students by name or enrollment ID
 */
public class SearchableStudentComboBox extends VBox {

    private final TextField searchField;
    private final ComboBox<Student> comboBox;
    private final ObservableList<Student> allStudents;
    private final ObservableList<Student> filteredStudents;

    public SearchableStudentComboBox(List<Student> students) {
        this.allStudents = FXCollections.observableArrayList(students);
        this.filteredStudents = FXCollections.observableArrayList(students);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search by name or enrollment ID...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-font-size: 12px;");

        // ComboBox
        comboBox = new ComboBox<>(filteredStudents);
        comboBox.setPrefWidth(280);
        comboBox.setTooltip(new Tooltip("Select a student"));

        // Custom display format: "Name (EnrollmentID)"
        comboBox.setButtonCell(new javafx.scene.control.ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getName() + " (" + student.getUsername() + ")");
                }
            }
        });

        comboBox.setCellFactory(param -> new javafx.scene.control.ListCell<Student>() {
            @Override
            protected void updateItem(Student student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    setText(student.getName() + " (" + student.getUsername() + ")");
                }
            }
        });

        // Add search filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStudents(newVal));

        this.setSpacing(5);
        this.getChildren().addAll(searchField, comboBox);
    }

    private void filterStudents(String searchText) {
        filteredStudents.clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredStudents.addAll(allStudents);
            return;
        }

        String lowerSearch = searchText.toLowerCase().trim();
        for (Student student : allStudents) {
            boolean matches = false;

            // Search by name
            if (student.getName() != null && student.getName().toLowerCase().contains(lowerSearch)) {
                matches = true;
            }

            // Search by enrollment ID (username)
            if (!matches && student.getUsername() != null &&
                    student.getUsername().toLowerCase().contains(lowerSearch)) {
                matches = true;
            }

            if (matches) {
                filteredStudents.add(student);
            }
        }
    }

    public Student getSelectedStudent() {
        return comboBox.getValue();
    }

    public void setSelectedStudent(Student student) {
        comboBox.setValue(student);
    }

    public ComboBox<Student> getComboBox() {
        return comboBox;
    }

    public void clear() {
        searchField.clear();
        comboBox.setValue(null);
    }

    public void setStudents(List<Student> students) {
        this.allStudents.setAll(students);
        this.filteredStudents.setAll(students);
        this.searchField.clear();
        this.comboBox.setValue(null);
    }
}
