package com.college.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Utility class for exporting JavaFX TableView data to CSV
 */
public class FxTableExporter {

    public static <T> boolean exportToCSV(TableView<T> table, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write Headers
            String headers = table.getColumns().stream()
                    .map(TableColumn::getText)
                    .map(FxTableExporter::escapeCSV)
                    .collect(Collectors.joining(","));
            writer.write(headers + "\n");

            // Write Rows
            ObservableList<T> items = table.getItems();
            for (T item : items) {
                String row = table.getColumns().stream()
                        .map(col -> {
                            // This relies on the cell value factory to get the data
                            // Note: This might get complex if cell factories are complicated. 
                            // A simpler approach is strictly formatting the model data if known, but TableView generic export tries to use what's in columns.
                            // However, TableColumn.getCellData(item) is the standard way.
                            Object val = col.getCellData(item);
                            return escapeCSV(val != null ? val.toString() : "");
                        })
                        .collect(Collectors.joining(","));
                writer.write(row + "\n");
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> void exportWithDialog(TableView<T> table, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("export.csv");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            boolean success = exportToCSV(table, file);
            if (success) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Data exported to " + file.getName());
                alert.showAndWait();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Could not save file.");
                alert.showAndWait();
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}
