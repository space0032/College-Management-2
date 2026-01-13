package com.college.fx.views;

import com.college.dao.SystemSettingsDAO;
import com.college.services.DropboxStorageService;
import com.college.utils.DialogUtils;
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

public class CollegeSettingsView extends VBox {

    private final SystemSettingsDAO systemSettingsDAO = new SystemSettingsDAO();
    private final DropboxStorageService storageService = new DropboxStorageService();
    private TextField collegeNameField;
    private ImageView logoPreview;
    private File selectedLogoFile;

    public CollegeSettingsView() {
        setSpacing(30);
        setPadding(new Insets(30));
        // Ensure dark background for contrast with white text
        setStyle("-fx-background-color: #0f172a;");
        getStyleClass().add("content-view");

        createView();
        loadSettings();
    }

    public VBox getView() {
        return this;
    }

    private void createView() {
        // Header
        Label title = new Label("College Settings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        Label subtitle = new Label("Manage your institution's branding and check system configurations.");
        subtitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web("#94a3b8"));

        VBox headerBox = new VBox(5, title, subtitle);

        // --- Card: Branding Settings ---
        VBox brandingCard = createCard("Branding Configuration");

        // 1. College Name
        Label nameLabel = new Label("Institution Name");
        nameLabel.setTextFill(Color.web("#e2e8f0"));
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        collegeNameField = new TextField();
        collegeNameField.setPromptText("Enter the name of your college or university");
        styleTextField(collegeNameField);

        VBox nameBox = new VBox(8, nameLabel, collegeNameField);

        // 2. College Logo
        Label logoLabel = new Label("Institution Logo");
        logoLabel.setTextFill(Color.web("#e2e8f0"));
        logoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        HBox logoContainer = new HBox(20);
        logoContainer.setAlignment(Pos.CENTER_LEFT);

        // Logo Preview Container
        StackPane logoWrapper = new StackPane();
        logoWrapper.setPrefSize(120, 120);
        logoWrapper.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 10; -fx-border-color: #334155; -fx-border-radius: 10; -fx-border-width: 1;");

        logoPreview = new ImageView();
        logoPreview.setFitHeight(80);
        logoPreview.setFitWidth(80);
        logoPreview.setPreserveRatio(true);
        logoWrapper.getChildren().add(logoPreview);

        // Upload Button
        Button uploadButton = new Button("Upload New Logo");
        styleSecondaryButton(uploadButton);
        uploadButton.setOnAction(e -> handleLogoUpload());

        Label uploadHint = new Label("Recommended: Square PNG/JPG, at least 200x200px.");
        uploadHint.setTextFill(Color.web("#64748b"));
        uploadHint.setFont(Font.font("Segoe UI", 12));

        VBox uploadBox = new VBox(10, uploadButton, uploadHint);
        uploadBox.setAlignment(Pos.CENTER_LEFT);

        logoContainer.getChildren().addAll(logoWrapper, uploadBox);

        VBox logoBox = new VBox(8, logoLabel, logoContainer);

        brandingCard.getChildren().addAll(nameBox, new Separator(), logoBox);

        // --- Actions ---
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        Button saveButton = new Button("Save Changes");
        stylePrimaryButton(saveButton);
        saveButton.setOnAction(e -> saveSettings());

        actionBox.getChildren().add(saveButton);

        getChildren().addAll(headerBox, brandingCard, actionBox);
    }

    private VBox createCard(String titleText) {
        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Label cardTitle = new Label(titleText);
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        cardTitle.setTextFill(Color.WHITE);

        card.getChildren().add(cardTitle);
        return card;
    }

    private void styleTextField(TextField tf) {
        tf.setStyle(
                "-fx-background-color: #334155; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10; -fx-prompt-text-fill: #94a3b8;");
        tf.setFont(Font.font("Segoe UI", 14));
        tf.setMaxWidth(600);
    }

    private void stylePrimaryButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 24; -fx-cursor: hand;");
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 24; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 10 24; -fx-cursor: hand;"));
    }

    private void styleSecondaryButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-text-fill: white; -fx-padding: 8 16; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1); -fx-border-color: white; -fx-border-radius: 6; -fx-text-fill: white; -fx-padding: 8 16; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-radius: 6; -fx-text-fill: white; -fx-padding: 8 16; -fx-cursor: hand;"));
    }

    private void handleLogoUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select College Logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            selectedLogoFile = file;
            Image image = new Image(file.toURI().toString());
            logoPreview.setImage(image);
        }
    }

    private void loadSettings() {
        String name = systemSettingsDAO.getSetting("COLLEGE_NAME");
        if (name != null) {
            collegeNameField.setText(name);
        }

        String logoPath = systemSettingsDAO.getSetting("COLLEGE_LOGO_PATH");
        if (logoPath != null && !logoPath.isEmpty()) {
            // Load asynchronously
            new Thread(() -> {
                String url = storageService.getTemporaryLink(logoPath);
                if (url != null) {
                    javafx.application.Platform.runLater(() -> {
                        logoPreview.setImage(new Image(url, true));
                    });
                }
            }).start();
        }
    }

    private void saveSettings() {
        String newName = collegeNameField.getText();
        if (newName != null && !newName.isEmpty()) {
            systemSettingsDAO.updateSetting("COLLEGE_NAME", newName);
        }

        if (selectedLogoFile != null) {
            // Upload to Dropbox
            String fileName = "college_logo_" + System.currentTimeMillis() + ".png";
            new Thread(() -> {
                String result = storageService.saveImage(selectedLogoFile, fileName);
                if (result != null) {
                    javafx.application.Platform.runLater(() -> {
                        systemSettingsDAO.updateSetting("COLLEGE_LOGO_PATH", result);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Settings saved successfully!");
                        DialogUtils.styleDialog(alert);
                        alert.showAndWait();
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to upload logo.");
                        DialogUtils.styleDialog(alert);
                        alert.showAndWait();
                    });
                }
            }).start();
        } else {
            // Just name saved
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Settings saved successfully!");
            DialogUtils.styleDialog(alert);
            alert.showAndWait();
        }
    }
}
