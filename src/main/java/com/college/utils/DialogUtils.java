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
        } catch (Exception e) {
            System.err.println("Warning: Failed to apply dialog styles: " + e.getMessage());
        }
    }

    public static void addFormRow(javafx.scene.layout.GridPane grid, String labelText, javafx.scene.Node field,
            int row) {
        javafx.scene.control.Label label = new javafx.scene.control.Label(labelText);
        label.setStyle("-fx-text-fill: #e2e8f0;"); // Default text color for dark theme
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        javafx.scene.layout.GridPane.setValignment(label, javafx.geometry.VPos.CENTER);
        javafx.scene.layout.GridPane.setValignment(field, javafx.geometry.VPos.CENTER);
    }
}
