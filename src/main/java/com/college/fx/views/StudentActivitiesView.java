package com.college.fx.views;

import com.college.utils.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

/**
 * Student Activities Dashboard View
 * Central hub for Events and Clubs
 */
public class StudentActivitiesView {
    private VBox root;
    private Runnable navigateToEvents;
    private Runnable navigateToClubs;
    private Runnable navigateToEventManagement;
    private Runnable navigateToClubManagement;

    public StudentActivitiesView(Runnable navigateToEvents, Runnable navigateToClubs,
            Runnable navigateToEventManagement, Runnable navigateToClubManagement) {
        this.navigateToEvents = navigateToEvents;
        this.navigateToClubs = navigateToClubs;
        this.navigateToEventManagement = navigateToEventManagement;
        this.navigateToClubManagement = navigateToClubManagement;
        createView();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Header
        Label header = new Label("Student Activities");
        header.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitle = new Label("Explore campus events and join student clubs");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

        VBox headerBox = new VBox(5, header, subtitle);

        // Tiles Grid
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 0, 0, 0));

        SessionManager session = SessionManager.getInstance();

        int col = 0;
        int row = 0;

        // Events tile (for students)
        if (session.hasPermission("VIEW_EVENTS")) {
            VBox eventsTile = createTile("Events", "Browse and register for campus events",
                    "#3b82f6", navigateToEvents);
            grid.add(eventsTile, col++, row);
        }

        // Clubs tile (for students)
        if (session.hasPermission("JOIN_CLUBS")) {
            VBox clubsTile = createTile("Clubs", "Explore and join student organizations",
                    "#8b5cf6", navigateToClubs);
            grid.add(clubsTile, col++, row);
        }

        // Move to next row if needed
        if (col >= 2) {
            col = 0;
            row++;
        }

        // Event Management tile (for admin/faculty)
        if (session.hasPermission("MANAGE_EVENTS")) {
            VBox eventMgmtTile = createTile("Event Management", "Create and manage campus events",
                    "#0891b2", navigateToEventManagement);
            grid.add(eventMgmtTile, col++, row);
        }

        // Club Management tile (for admin/faculty)
        if (session.hasPermission("MANAGE_CLUBS")) {
            VBox clubMgmtTile = createTile("Club Management", "Manage student clubs and memberships",
                    "#7c3aed", navigateToClubManagement);
            grid.add(clubMgmtTile, col++, row);
        }

        root.getChildren().addAll(headerBox, grid);
    }

    private VBox createTile(String title, String description, String color, Runnable action) {
        VBox tile = new VBox(15);
        tile.setPrefWidth(280);
        tile.setPrefHeight(180);
        tile.setPadding(new Insets(25));
        tile.setAlignment(Pos.TOP_LEFT);
        tile.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 12; " +
                "-fx-cursor: hand;");

        // Icon/Color bar
        Region colorBar = new Region();
        colorBar.setPrefHeight(4);
        colorBar.setPrefWidth(60);
        colorBar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        descLabel.setWrapText(true);

        // Button
        Button openBtn = new Button("Open →");
        openBtn.setStyle("-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 8 16 8 16; " +
                "-fx-cursor: hand;");
        openBtn.setOnAction(e -> action.run());

        // Hover effect
        tile.setOnMouseEntered(e -> {
            tile.setStyle("-fx-background-color: " + color + "10; " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: " + color + "; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12; " +
                    "-fx-cursor: hand;");
        });

        tile.setOnMouseExited(e -> {
            tile.setStyle("-fx-background-color: white; " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 12; " +
                    "-fx-cursor: hand;");
        });

        tile.setOnMouseClicked(e -> action.run());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        tile.getChildren().addAll(colorBar, titleLabel, descLabel, spacer, openBtn);

        return tile;
    }

    public VBox getView() {
        return root;
    }
}
