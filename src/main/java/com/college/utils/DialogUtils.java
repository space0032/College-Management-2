package com.college.utils;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import java.net.URL;

public class DialogUtils {

    public static void styleDialog(Dialog<?> dialog) {
        try {
            DialogPane dialogPane = dialog.getDialogPane();
            URL cssResource = DialogUtils.class.getResource("/styles/dashboard.css");
            if (cssResource != null) {
                dialogPane.getStylesheets().add(cssResource.toExternalForm());
            }
            // Standardize styles
            dialogPane.getStyleClass().add("dialog-pane");
            
            // Force dark background on all areas
            dialogPane.setStyle("-fx-background-color: #1e293b;");
            
            // Apply dark theme to content area
            javafx.scene.layout.Region content = (javafx.scene.layout.Region) dialogPane.getContent();
            if (content != null) {
                content.setStyle("-fx-background-color: transparent;");
            }
            
            // Style all child nodes
            javafx.application.Platform.runLater(() -> {
                dialogPane.lookupAll(".label").forEach(node -> {
                    node.setStyle("-fx-text-fill: white;");
                });
                
                // Fix GridPane backgrounds
                dialogPane.lookupAll(".grid-pane").forEach(node -> {
                    node.setStyle("-fx-background-color: transparent;");
                });
                
                // Fix VBox/HBox backgrounds
                dialogPane.lookupAll(".vbox, .hbox").forEach(node -> {
                    node.setStyle("-fx-background-color: transparent;");
                });
            });
        } catch (Exception e) {
            System.err.println("Warning: Failed to apply dialog styles: " + e.getMessage());
        }
    }

    public static void addFormRow(javafx.scene.layout.GridPane grid, String labelText, javafx.scene.Node field,
            int row) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(labelText);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;"); // Force white and bold for visibility
        label.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE); // Prevent truncation
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        javafx.scene.layout.GridPane.setValignment(label, javafx.geometry.VPos.CENTER);
        javafx.scene.layout.GridPane.setValignment(field, javafx.geometry.VPos.CENTER);
    }

    public static void showSuccess(String title, String content) {
        showDialog(javafx.scene.control.Alert.AlertType.INFORMATION, title, content);
    }

    public static void showInfo(String title, String content) {
        showDialog(javafx.scene.control.Alert.AlertType.INFORMATION, title, content);
    }

    public static void showError(String title, String content) {
        showDialog(javafx.scene.control.Alert.AlertType.ERROR, title, content);
    }

    private static void showDialog(javafx.scene.control.Alert.AlertType type, String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleDialog(alert);
        alert.showAndWait();
    }
}
