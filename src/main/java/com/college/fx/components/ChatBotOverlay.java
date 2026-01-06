package com.college.fx.components;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatBotOverlay extends VBox {

    private boolean isExpanded = false;
    private VBox chatWindow;
    private Button fabButton;
    private VBox messagesBox;
    private TextField inputField;
    private ScrollPane scrollPane;

    private com.college.services.GeminiService geminiService;

    public ChatBotOverlay() {
        this.geminiService = new com.college.services.GeminiService();
        setAlignment(Pos.BOTTOM_RIGHT);
        setPickOnBounds(false); // Allow clicks to pass through transparent areas
        setPadding(new Insets(20));
        setSpacing(15);
        createView();
    }

    private void createView() {
        // Chat Window (Hidden by default)
        chatWindow = createChatWindow();
        chatWindow.setVisible(false);
        chatWindow.setManaged(false);

        // Floating Action Button (FAB)
        fabButton = new Button("AI Help");
        fabButton.setPrefSize(60, 60);
        fabButton.setStyle(
                "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        fabButton.setOnAction(e -> toggleChat());

        // Add to VBox
        getChildren().addAll(chatWindow, fabButton);
    }

    private VBox createChatWindow() {
        VBox window = new VBox();
        window.setPrefSize(350, 450);
        window.setMaxSize(350, 450);
        window.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-border-color: #e5e7eb; -fx-border-radius: 12; -fx-border-width: 1;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #4f46e5; -fx-background-radius: 12 12 0 0;");

        Label title = new Label("Gemini AI Assistant");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("×");
        closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> toggleChat());

        header.getChildren().addAll(title, spacer, closeBtn);

        // Messages Area
        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(15));
        messagesBox.setStyle("-fx-background-color: #f9fafb;");

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background: #f9fafb; -fx-border-color: transparent;");

        // Input Area
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        inputArea.setAlignment(Pos.CENTER_LEFT);
        inputArea.setStyle(
                "-fx-background-color: white; -fx-background-radius: 0 0 12 12; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        inputField = new TextField();
        inputField.setPromptText("Type a message...");
        inputField.setStyle("-fx-background-radius: 20; -fx-padding: 8 15;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button sendBtn = new Button("➤");
        sendBtn.setStyle(
                "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand;");
        sendBtn.setOnAction(e -> sendMessage());

        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                sendMessage();
        });

        inputArea.getChildren().addAll(inputField, sendBtn);

        window.getChildren().addAll(header, scrollPane, inputArea);

        // Initial welcome message
        addMessage("Hello! I am your AI assistant. How can I help you today?", false);

        return window;
    }

    private void toggleChat() {
        isExpanded = !isExpanded;
        chatWindow.setVisible(isExpanded);
        chatWindow.setManaged(isExpanded);

        // Basic animation
        if (isExpanded) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), chatWindow);
            tt.setFromY(20);
            tt.setToY(0);
            tt.play();
            Platform.runLater(() -> inputField.requestFocus());
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty())
            return;

        addMessage(text, true);
        inputField.clear();

        // Simulate thinking or call service
        addTypingIndicator();

        // Call Gemini Service
        new Thread(() -> {
            String response = geminiService.sendMessage(text);

            Platform.runLater(() -> {
                removeTypingIndicator();
                addMessage(response, false);
            });
        }).start();
    }

    private void addMessage(String text, boolean isUser) {
        HBox msgContainer = new HBox();
        msgContainer.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        TextFlow msgFlow = new TextFlow();
        Text textNode = new Text(text);
        textNode.setFill(isUser ? Color.WHITE : Color.web("#1f2937"));
        textNode.setFont(Font.font("Segoe UI", 13));
        msgFlow.getChildren().add(textNode);

        msgFlow.setMaxWidth(220); // Max bubble width
        msgFlow.setPadding(new Insets(10, 15, 10, 15));
        msgFlow.setStyle(isUser
                ? "-fx-background-color: #4f46e5; -fx-background-radius: 15 15 0 15;"
                : "-fx-background-color: #e5e7eb; -fx-background-radius: 15 15 15 0;");

        // Timestamp
        Label timestamp = new Label(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timestamp.setFont(Font.font("Segoe UI", 9));
        timestamp.setTextFill(Color.GRAY);
        timestamp.setPadding(new Insets(0, 0, 5, 5));

        VBox bubbles = new VBox(2);
        bubbles.setAlignment(isUser ? Pos.BOTTOM_RIGHT : Pos.BOTTOM_LEFT);
        bubbles.getChildren().addAll(msgFlow, timestamp);

        msgContainer.getChildren().add(bubbles);

        messagesBox.getChildren().add(msgContainer);
        scrollToBottom();
    }

    private HBox typingIndicator;

    private void addTypingIndicator() {
        typingIndicator = new HBox(5);
        typingIndicator.setAlignment(Pos.CENTER_LEFT);
        typingIndicator.setPadding(new Insets(10));
        typingIndicator.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 15 15 15 0;");
        typingIndicator.setMaxWidth(60);

        Circle d1 = new Circle(3, Color.GRAY);
        Circle d2 = new Circle(3, Color.GRAY);
        Circle d3 = new Circle(3, Color.GRAY);

        // Simple animation could go here

        typingIndicator.getChildren().addAll(d1, d2, d3);

        messagesBox.getChildren().add(typingIndicator);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        if (typingIndicator != null) {
            messagesBox.getChildren().remove(typingIndicator);
            typingIndicator = null;
        }
    }

    private void scrollToBottom() {
        // Auto-scroll
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }

    // Method to inject service later
    // public void setGeminiService(...)
}
