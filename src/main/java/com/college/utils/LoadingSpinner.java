package com.college.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * Loading spinner utility for visual feedback during slow operations
 */
public class LoadingSpinner {

    private Stage stage;
    private ProgressIndicator spinner;
    private Label messageLabel;

    public LoadingSpinner(String message) {
        Platform.runLater(() -> {
            stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);

            spinner = new ProgressIndicator();
            spinner.setPrefSize(60, 60);

            messageLabel = new Label(message);
            messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b;");

            VBox vbox = new VBox(15);
            vbox.setAlignment(Pos.CENTER);
            vbox.setStyle(
                    "-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
            vbox.getChildren().addAll(spinner, messageLabel);

            Scene scene = new Scene(vbox);
            stage.setScene(scene);
        });
    }

    public void show() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.show();
            }
        });
    }

    public void updateMessage(String message) {
        Platform.runLater(() -> {
            if (messageLabel != null) {
                messageLabel.setText(message);
            }
        });
    }

    public void close() {
        Platform.runLater(() -> {
            if (stage != null) {
                stage.close();
            }
        });
    }
}
