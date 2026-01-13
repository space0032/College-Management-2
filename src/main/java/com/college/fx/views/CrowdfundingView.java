package com.college.fx.views;

import com.college.dao.CommunityDAO;
import com.college.models.Campaign;
import com.college.utils.DialogUtils;
import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

public class CrowdfundingView {

    private final CommunityDAO communityDAO = new CommunityDAO();
    private BorderPane mainLayout;
    private StackPane centerStack;

    // Rocket Icon SVG
    private static final String ROCKET_ICON = "M13.145 2.147a2.5 2.5 0 00-2.29 0C8.583 3.195 6.78 5.767 6.136 8.56c-.504 2.18-.323 4.417.48 6.438.318.799.034 1.761-.634 2.14-.658.373-1.096 1.055-1.127 1.815-.027.64.293 1.258.824 1.587 1.95.897 4.135 1.344 6.321 1.344s4.372-.447 6.321-1.344c.53-.33.85-.947.824-1.587-.03-.76-.469-1.442-1.127-1.815-.668-.379-.952-1.34-.634-2.14.803-2.022.984-4.258.48-6.438-.644-2.793-2.447-5.365-4.719-6.413z M12 5.5a1.5 1.5 0 110 3 1.5 1.5 0 010-3z";

    public BorderPane getView() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("glass-pane");
        mainLayout.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Campus Crowdfunding");
        title.getStyleClass().add("crowd-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button startCampaignBtn = new Button("+ Start Campaign");
        startCampaignBtn.getStyleClass().add("crowd-start-btn");
        startCampaignBtn.setOnAction(e -> showCreateCampaignDialog());

        // RESTRICTION: Hide button if user is STUDENT
        com.college.models.Role role = SessionManager.getInstance().getUserRole();
        if (role != null && "STUDENT".equalsIgnoreCase(role.getCode())) {
            startCampaignBtn.setVisible(false);
            startCampaignBtn.setManaged(false);
        }

        header.getChildren().addAll(title, spacer, startCampaignBtn);
        mainLayout.setTop(header);

        // Center Stack (Empty State vs List)
        centerStack = new StackPane();
        centerStack.setAlignment(Pos.CENTER);
        VBox.setVgrow(centerStack, Priority.ALWAYS);

        refreshCampaigns();

        mainLayout.setCenter(centerStack);
        return mainLayout;
    }

    // Fix for the method name typo above if any

    private void refreshCampaigns() {
        if (centerStack == null)
            return;
        centerStack.getChildren().clear();

        List<Campaign> campaigns = communityDAO.getAllCampaigns();

        if (campaigns.isEmpty()) {
            centerStack.getChildren().add(createEmptyState());
        } else {
            ScrollPane scroll = new ScrollPane(createCardsContainer(campaigns));
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            centerStack.getChildren().add(scroll);
        }
    }

    private VBox createEmptyState() {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(ROCKET_ICON);
        icon.setScaleX(5);
        icon.setScaleY(5);
        icon.getStyleClass().add("crowd-empty-icon");

        StackPane iconPane = new StackPane(icon);
        iconPane.setPadding(new Insets(40));

        Label mainText = new Label("No active campaigns yet.");
        mainText.getStyleClass().add("crowd-empty-text");

        Label subText = new Label("Be the first to start one!");
        subText.getStyleClass().add("crowd-empty-subtext");

        box.getChildren().addAll(iconPane, mainText, subText);
        return box;
    }

    private VBox createCardsContainer(List<Campaign> campaigns) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(10));
        for (Campaign c : campaigns) {
            container.getChildren().add(createCampaignCard(c));
        }
        return container;
    }

    private VBox createCampaignCard(Campaign c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.6); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10;");

        Label title = new Label(c.getTitle());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Label desc = new Label(c.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #cbd5e1;");

        double progress = c.getRaisedAmount() / c.getGoalAmount();
        ProgressBar pBar = new ProgressBar(progress);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setStyle("-fx-accent: #22c55e;"); // Green progress

        HBox stats = new HBox(10);
        Label raised = new Label(String.format("Raised: $%.2f", c.getRaisedAmount()));
        raised.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");

        Label goal = new Label(String.format("Goal: $%.2f", c.getGoalAmount()));
        goal.setStyle("-fx-text-fill: #94a3b8;");

        stats.getChildren().addAll(raised, new Label("/"), goal);

        Button donateBtn = new Button("Donate");
        donateBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold;");
        donateBtn.setOnAction(e -> showDonateDialog(c));

        card.getChildren().addAll(title, desc, pBar, stats, donateBtn);
        return card;
    }

    private void showCreateCampaignDialog() {
        Dialog<Campaign> dialog = new Dialog<>();
        DialogUtils.styleDialog(dialog);
        dialog.setTitle("Start Campaign");
        dialog.setHeaderText("Create a new fundraising campaign");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleF = new TextField();
        titleF.setPromptText("Campaign Title");
        TextField goalF = new TextField();
        goalF.setPromptText("Goal Amount");
        TextArea descF = new TextArea();
        descF.setPromptText("Description");
        descF.setStyle("-fx-text-fill: white; -fx-control-inner-background: #1e293b;");

        DialogUtils.addFormRow(grid, "Title:", titleF, 0);
        DialogUtils.addFormRow(grid, "Goal:", goalF, 1);
        DialogUtils.addFormRow(grid, "Story:", descF, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == createBtn) {
                try {
                    Campaign c = new Campaign();
                    c.setTitle(titleF.getText());
                    c.setDescription(descF.getText());
                    c.setGoalAmount(Double.parseDouble(goalF.getText()));
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
                // Trigger Announcement
                com.college.models.Announcement ann = new com.college.models.Announcement();
                ann.setTitle("New Campaign: " + c.getTitle());
                ann.setContent("A new crowdfunding campaign has started: " + c.getTitle() + ". Support your peers!");
                ann.setTargetAudience("ALL");
                ann.setPriority("NORMAL");
                ann.setCreatedBy(SessionManager.getInstance().getUserId());
                ann.setActive(true);
                new com.college.dao.AnnouncementDAO().addAnnouncement(ann);

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
