package com.college;

import atlantafx.base.theme.PrimerLight;
import com.college.fx.views.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Main Application Entry Point
 * Uses AtlantaFX for modern UI styling
 */
public class MainFX extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Apply AtlantaFX theme
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        
        // Create and show login screen
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1000, 650);
        
        stage.setTitle("College Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
