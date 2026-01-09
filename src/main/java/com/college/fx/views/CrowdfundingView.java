package com.college.fx.views;

import com.college.dao.CommunityDAO;
import com.college.models.Campaign;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class CrowdfundingView {

    private final CommunityDAO communityDAO = new CommunityDAO();
    private VBox cardsContainer;

    public VBox getView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(25));
        view.getStyleClass().add("glass-pane");
        view.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Campus Crowdfunding");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button startCampaignBtn = new Button("Start Campaign");
        startCampaignBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        startCampaignBtn.setOnAction(e -> showCreateCampaignDialog());

        header.getChildren().addAll(title, spacer, startCampaignBtn);

        // Cards Container
        cardsContainer = new VBox(15);
        ScrollPane scroll = new ScrollPane(cardsContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        refreshCampaigns();

        view.getChildren().addAll(header, scroll);
        return view;
    }

    private void refreshCampaigns() {
        cardsContainer.getChildren().clear();
        List<Campaign> campaigns = communityDAO.getAllCampaigns();

        if (campaigns.isEmpty()) {
            Label placeholder = new Label("No active campaigns. Be the first to start one!");
            placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            cardsContainer.getChildren().add(placeholder);
        } else {
            for (Campaign c : campaigns) {
                cardsContainer.getChildren().add(createCampaignCard(c));
            }
        }
    }

    private VBox createCampaignCard(Campaign c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10;");

        Label title = new Label(c.getTitle());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Label desc = new Label(c.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #cbd5e1;");

        // Progress
        double progress = c.getRaisedAmount() / c.getGoalAmount();
        ProgressBar pBar = new ProgressBar(progress);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setStyle("-fx-accent: #3b82f6;");

        HBox stats = new HBox(10);
        Label raised = new Label(String.format("Raised: $%.2f", c.getRaisedAmount()));
        raised.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");

        Label goal = new Label(String.format("Goal: $%.2f", c.getGoalAmount()));
        goal.setStyle("-fx-text-fill: #94a3b8;");

        stats.getChildren().addAll(raised, new Label("/"), goal);

        Button donateBtn = new Button("Donate");
        donateBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;");
        donateBtn.setOnAction(e -> showDonateDialog(c));

        card.getChildren().addAll(title, desc, pBar, stats, donateBtn);
        return card;
    }

    private void showCreateCampaignDialog() {
        Dialog<Campaign> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Start Campaign");
        dialog.getDialogPane().setMinWidth(500); // Fix truncation
        dialog.setHeaderText("Create a new fundraising campaign");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(100);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField titleField = new TextField();
        titleField.setPromptText("Campaign Title");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");

        TextField goalField = new TextField();
        goalField.setPromptText("Goal Amount");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Goal:"), 0, 1);
        grid.add(goalField, 1, 1);
        grid.add(new Label("Story:"), 0, 2);
        grid.add(descArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                try {
                    Campaign c = new Campaign();
                    c.setTitle(titleField.getText());
                    c.setDescription(descArea.getText());
                    c.setGoalAmount(Double.parseDouble(goalField.getText()));
                    c.setCreatedBy(SessionManager.getInstance().getUserId());
                    c.setStatus("ACTIVE");
                    return c;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (communityDAO.createCampaign(c)) {
                refreshCampaigns();
            } else {
                showAlert("Error", "Failed to create campaign.");
            }
        });
    }

    private void showDonateDialog(Campaign c) {
        TextInputDialog dialog = new TextInputDialog("10");
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Donate");
        dialog.setHeaderText("Donate to " + c.getTitle());
        dialog.setContentText("Enter amount:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (communityDAO.donateToCampaign(c.getId(), amount)) {
                    refreshCampaigns();
                    showAlert("Thank You", "Donation successful!");
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid amount.");
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogUtils.styleDialog(alert);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
