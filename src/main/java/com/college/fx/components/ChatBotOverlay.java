package com.college.fx.components;

import com.college.services.GeminiService;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.function.Supplier;

public class ChatBotOverlay extends VBox {

    private boolean isExpanded = false;
    private VBox chatWindow;
    private Button fabButton;
    private VBox messagesBox;
    private TextField inputField;
    private ScrollPane scrollPane;

    private GeminiService geminiService;
    private Supplier<String> contextProvider;
    private HBox typingIndicator;

    public ChatBotOverlay() {
        this.geminiService = new GeminiService();
        setAlignment(Pos.BOTTOM_RIGHT);
        setPickOnBounds(false); // Allow clicks to pass through transparent areas
        setPadding(new Insets(20));
        setSpacing(15);
        createView();
    }

    public void setContextProvider(Supplier<String> provider) {
        this.contextProvider = provider;
    }

    private void createView() {
        // Chat Window (Hidden by default)
        chatWindow = createChatWindow();
        chatWindow.setVisible(false);
        chatWindow.setManaged(false);

        // Floating Action Button (FAB)
        fabButton = new Button();
        fabButton.setPrefSize(60, 60);

        // Default Style
        String fabStyle = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4); -fx-cursor: hand;";

        // Try to load custom logo
        try {
            java.net.URL imgUrl = getClass().getResource("/com/college/images/bot_logo.png");
            if (imgUrl != null) {
                ImageView logoView = new ImageView(new Image(imgUrl.toExternalForm()));
                logoView.setFitWidth(60);
                logoView.setFitHeight(60);
                logoView.setPreserveRatio(true);

                // Circular clip
                Circle clip = new Circle(30, 30, 30);
                logoView.setClip(clip);

                fabButton.setGraphic(logoView);
                fabButton.setPadding(Insets.EMPTY);
                fabButton.setStyle(
                        "-fx-background-color: transparent; -fx-background-radius: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4); -fx-cursor: hand;");
            } else {
                fabButton.setText("AI Help");
                fabButton.setStyle(fabStyle);
            }
        } catch (Exception e) {
            fabButton.setText("AI Help");
            fabButton.setStyle(fabStyle);
        }

        fabButton.setOnAction(e -> toggleChat());

        // Add to VBox
        getChildren().addAll(chatWindow, fabButton);
    }

    private VBox createChatWindow() {
        VBox window = new VBox();
        window.setPrefSize(350, 450);
        window.setMaxSize(350, 450);
        window.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 5); -fx-border-color: #334155; -fx-border-radius: 12; -fx-border-width: 1;");

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
        messagesBox.setStyle("-fx-background-color: #0f172a;");

        scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.setStyle("-fx-background: #0f172a; -fx-border-color: transparent;");

        // Input Area
        HBox inputArea = new HBox(10);
        inputArea.setPadding(new Insets(15));
        inputArea.setAlignment(Pos.CENTER_LEFT);
        inputArea.setStyle(
                "-fx-background-color: #1e293b; -fx-background-radius: 0 0 12 12; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");

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
        addMessage("Hello! I am your AI assistant. I can see what's on your screen. How can I help?", false);

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

        addTypingIndicator();

        // Get Context
        String context = "No specific context available.";
        if (contextProvider != null) {
            String ctx = contextProvider.get();
            if (ctx != null && !ctx.isEmpty()) {
                context = ctx;
            }
        }
        final String currentContext = context;

        // Call Gemini Service
        new Thread(() -> {
            String response = geminiService.sendMessage(text, currentContext);

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
        textNode.setFill(Color.WHITE);
        textNode.setFont(Font.font("Segoe UI", 13));
        msgFlow.getChildren().add(textNode);

        msgFlow.setMaxWidth(220); // Max bubble width
        msgFlow.setPadding(new Insets(10, 15, 10, 15));
        msgFlow.setStyle(isUser
                ? "-fx-background-color: #4f46e5; -fx-background-radius: 15 15 0 15;"
                : "-fx-background-color: #334155; -fx-background-radius: 15 15 15 0;");

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

    private void addTypingIndicator() {
        typingIndicator = new HBox(5);
        typingIndicator.setAlignment(Pos.CENTER_LEFT);
        typingIndicator.setPadding(new Insets(10));
        typingIndicator.setStyle("-fx-background-color: #334155; -fx-background-radius: 15 15 15 0;");
        typingIndicator.setMaxWidth(60);

        Circle d1 = new Circle(3, Color.GRAY);
        Circle d2 = new Circle(3, Color.GRAY);
        Circle d3 = new Circle(3, Color.GRAY);

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
        Platform.runLater(() -> {
            scrollPane.setVvalue(1.0);
        });
    }
}
