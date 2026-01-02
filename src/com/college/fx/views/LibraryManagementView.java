package com.college.fx.views;

import com.college.dao.LibraryDAO;
import com.college.models.Book;
import com.college.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * JavaFX Library Management View
 */
public class LibraryManagementView {

    private VBox root;
    private TableView<Book> tableView;
    private ObservableList<Book> bookData;
    private LibraryDAO libraryDAO;
    private String role;
    private TextField searchField;

    public LibraryManagementView(String role, int userId) {
        this.role = role;
        this.libraryDAO = new LibraryDAO();
        this.bookData = FXCollections.observableArrayList();
        createView();
        loadBooks();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f8fafc;");

        HBox header = createHeader();
        VBox tableSection = createTableSection();
        VBox.setVgrow(tableSection, Priority.ALWAYS);
        HBox buttonSection = createButtonSection();

        root.getChildren().addAll(header, tableSection, buttonSection);
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15));
        header.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );

        Label title = new Label(role.equals("STUDENT") ? "Library" : "Library Management");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#0f172a"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Search books...");
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0;");

        Button searchBtn = createButton("Search", "#14b8a6");
        searchBtn.setOnAction(e -> searchBooks());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadBooks());

        header.getChildren().addAll(title, spacer, searchField, searchBtn, refreshBtn);
        return header;
    }

    @SuppressWarnings("unchecked")
    private VBox createTableSection() {
        VBox section = new VBox();
        section.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 12;"
        );
        section.setPadding(new Insets(15));

        tableView = new TableView<>();
        tableView.setItems(bookData);

        TableColumn<Book, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(250);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        authorCol.setPrefWidth(180);

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn()));
        isbnCol.setPrefWidth(130);

        TableColumn<Book, String> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        qtyCol.setPrefWidth(80);

        TableColumn<Book, String> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getAvailable())));
        availCol.setPrefWidth(80);

        tableView.getColumns().addAll(idCol, titleCol, authorCol, isbnCol, qtyCol, availCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        section.getChildren().add(tableView);
        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        section.setPadding(new Insets(10));

        SessionManager session = SessionManager.getInstance();

        if (session.hasPermission("MANAGE_LIBRARY")) {
            Button addBtn = createButton("Add Book", "#22c55e");
            addBtn.setOnAction(e -> showAlert("Add Book", "Add book dialog would open here."));

            Button issueBtn = createButton("Issue Book", "#3b82f6");
            issueBtn.setOnAction(e -> showAlert("Issue Book", "Issue book dialog would open here."));

            Button returnBtn = createButton("Return Book", "#f59e0b");
            returnBtn.setOnAction(e -> showAlert("Return Book", "Return book dialog would open here."));

            section.getChildren().addAll(addBtn, issueBtn, returnBtn);
        } else if (role.equals("STUDENT")) {
            Button requestBtn = createButton("Request Book", "#14b8a6");
            requestBtn.setOnAction(e -> requestBook());
            section.getChildren().add(requestBtn);
        }

        Button exportBtn = createButton("Export", "#64748b");
        section.getChildren().add(exportBtn);

        return section;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(140);
        btn.setPrefHeight(40);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }

    private void loadBooks() {
        bookData.clear();
        List<Book> books = libraryDAO.getAllBooks();
        bookData.addAll(books);
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadBooks();
            return;
        }
        bookData.clear();
        List<Book> books = libraryDAO.getAllBooks();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword) || 
                b.getAuthor().toLowerCase().contains(keyword)) {
                bookData.add(b);
            }
        }
    }

    private void requestBook() {
        Book selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to request.");
            return;
        }
        if (selected.getAvailable() <= 0) {
            showAlert("Error", "This book is not available.");
            return;
        }
        showAlert("Request Book", "Book request submitted for: " + selected.getTitle());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return root;
    }
}
