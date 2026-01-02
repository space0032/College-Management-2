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

import com.college.dao.BookIssueDAO;
import com.college.dao.StudentDAO;
import com.college.models.BookIssue;
import com.college.models.Student;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.util.StringConverter;
import java.util.List;

/**
 * JavaFX Library Management View
 */
public class LibraryManagementView {

    private VBox root;
    private TableView<Book> tableView;
    private ObservableList<Book> bookData;
    private LibraryDAO libraryDAO;
    private BookIssueDAO bookIssueDAO;
    private StudentDAO studentDAO;
    private String role;
    private TextField searchField;

    public LibraryManagementView(String role, int userId) {
        this.role = role;
        this.libraryDAO = new LibraryDAO();
        this.bookIssueDAO = new BookIssueDAO();
        this.studentDAO = new StudentDAO();
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
            addBtn.setOnAction(e -> showAddBookDialog());

            Button editBtn = createButton("Edit Book", "#6366f1");
            editBtn.setOnAction(e -> editBook());

            Button issueBtn = createButton("Issue Book", "#3b82f6");
            issueBtn.setOnAction(e -> showIssueBookDialog());

            Button returnBtn = createButton("Return Book", "#f59e0b");
            returnBtn.setOnAction(e -> showReturnBookDialog());

            section.getChildren().addAll(addBtn, editBtn, issueBtn, returnBtn);
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

    private void editBook() {
        Book selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to edit.");
            return;
        }
        showEditBookDialog(selected);
    }

    private void showEditBookDialog(Book book) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit Book Details");
        ButtonType saveBtn = new ButtonType("Update", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(book.getTitle());
        TextField authorField = new TextField(book.getAuthor());
        TextField isbnField = new TextField(book.getIsbn());
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, book.getQuantity());

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1); grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2); grid.add(isbnField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3); grid.add(qtySpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                // Adjust available if quantity changed?
                // For simplicity, we just update total quantity.
                // Or calculating diff.
                int oldQty = book.getQuantity();
                int newQty = qtySpinner.getValue();
                int diff = newQty - oldQty;
                
                book.setTitle(titleField.getText());
                book.setAuthor(authorField.getText());
                book.setIsbn(isbnField.getText());
                book.setQuantity(newQty);
                book.setAvailable(book.getAvailable() + diff); // Adjust available logic
                
                libraryDAO.updateBook(book);
                return book;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(b -> { loadBooks(); showAlert("Success", "Book updated!"); });
    }

    private void showAddBookDialog() {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book");
        dialog.setHeaderText("Add New Book to Library");
        ButtonType saveBtn = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField isbnField = new TextField();
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, 1);

        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1); grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2); grid.add(isbnField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3); grid.add(qtySpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                Book b = new Book();
                b.setTitle(titleField.getText());
                b.setAuthor(authorField.getText());
                b.setIsbn(isbnField.getText());
                b.setQuantity(qtySpinner.getValue());
                b.setAvailable(qtySpinner.getValue());
                libraryDAO.addBook(b);
                return b;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(b -> { loadBooks(); showAlert("Success", "Book added!"); });
    }

    private void showIssueBookDialog() {
        Dialog<BookIssue> dialog = new Dialog<>();
        dialog.setTitle("Issue Book");
        dialog.setHeaderText("Issue Book to Student");
        ButtonType issueBtn = new ButtonType("Issue", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(issueBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField studentSearch = new TextField();
        studentSearch.setPromptText("Search Student Name/ID");
        ComboBox<Student> studentCombo = new ComboBox<>();
        studentCombo.setPrefWidth(250);
        List<Student> allStudents = studentDAO.getAllStudents();
        studentCombo.getItems().addAll(allStudents);
        
        studentSearch.textProperty().addListener((o, old, newVal) -> {
            String lower = newVal.toLowerCase();
            List<Student> filtered = allStudents.stream()
                .filter(s -> s.getName().toLowerCase().contains(lower) || String.valueOf(s.getId()).contains(lower))
                .collect(Collectors.toList());
            studentCombo.getItems().setAll(filtered);
            studentCombo.show();
        });

        TextField bookSearch = new TextField();
        bookSearch.setPromptText("Search Book Title");
        ComboBox<Book> bookCombo = new ComboBox<>();
        bookCombo.setPrefWidth(250);
        List<Book> avlBooks = libraryDAO.getAllBooks().stream().filter(b -> b.getAvailable() > 0).collect(Collectors.toList());
        bookCombo.getItems().addAll(avlBooks);
        
        bookSearch.textProperty().addListener((o, old, newVal) -> {
            String lower = newVal.toLowerCase();
            List<Book> filtered = avlBooks.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lower))
                .collect(Collectors.toList());
            bookCombo.getItems().setAll(filtered);
            bookCombo.show();
        });

        DatePicker dueDate = new DatePicker(LocalDate.now().plusDays(14));

        grid.add(new Label("Filter Student:"), 0, 0); grid.add(studentSearch, 1, 0);
        grid.add(new Label("Select Student:"), 0, 1); grid.add(studentCombo, 1, 1);
        grid.add(new Label("Filter Book:"), 0, 2); grid.add(bookSearch, 1, 2);
        grid.add(new Label("Select Book:"), 0, 3); grid.add(bookCombo, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4); grid.add(dueDate, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == issueBtn && studentCombo.getValue() != null && bookCombo.getValue() != null) {
                 BookIssue issue = new BookIssue();
                 issue.setStudentId(studentCombo.getValue().getId());
                 issue.setBookId(bookCombo.getValue().getId());
                 issue.setIssueDate(new Date());
                 issue.setDueDate(Date.from(dueDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                 issue.setIssuedBy(1); 
                 bookIssueDAO.issueBook(issue);
                 return issue;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(i -> { loadBooks(); showAlert("Success", "Book Issued!"); });
    }

    private void showReturnBookDialog() {
         Dialog<BookIssue> dialog = new Dialog<>();
         dialog.setTitle("Return Book");
         dialog.setHeaderText("Select Book to Return");
         ButtonType returnBtn = new ButtonType("Return", ButtonData.OK_DONE);
         dialog.getDialogPane().getButtonTypes().addAll(returnBtn, ButtonType.CANCEL);
         
         GridPane grid = new GridPane();
         grid.setHgap(10); grid.setVgap(10);
         grid.setPadding(new Insets(20, 10, 10, 10));
         
         TextField searchIssue = new TextField();
         searchIssue.setPromptText("Search Book/Student");
         
         ComboBox<BookIssue> issueCombo = new ComboBox<>();
         issueCombo.setPrefWidth(300);
         List<BookIssue> allIssues = bookIssueDAO.getAllIssuedBooks();
         issueCombo.getItems().addAll(allIssues);
         
         issueCombo.setConverter(new StringConverter<BookIssue>() {
             public String toString(BookIssue object) {
                 if(object == null) return "";
                 return object.getBookTitle() + " (" + object.getStudentName() + ")";
             }
             public BookIssue fromString(String string) { return null; }
         });
         
         searchIssue.textProperty().addListener((o, old, newVal) -> {
            String lower = newVal.toLowerCase();
            List<BookIssue> filtered = allIssues.stream()
                .filter(i -> (i.getBookTitle() != null && i.getBookTitle().toLowerCase().contains(lower)) || 
                             (i.getStudentName() != null && i.getStudentName().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
            issueCombo.getItems().setAll(filtered);
            issueCombo.show();
         });
         
         TextField fineField = new TextField("0.0");
         fineField.setEditable(false);
         
         issueCombo.setOnAction(e -> {
             BookIssue bi = issueCombo.getValue();
             if (bi != null) fineField.setText(String.valueOf(bi.calculateFine(5.0)));
         });
         
         grid.add(new Label("Filter:"), 0, 0); grid.add(searchIssue, 1, 0);
         grid.add(new Label("Issued Book:"), 0, 1); grid.add(issueCombo, 1, 1);
         grid.add(new Label("Fine:"), 0, 2); grid.add(fineField, 1, 2);
         
         dialog.getDialogPane().setContent(grid);
         
         dialog.setResultConverter(btn -> {
             if (btn == returnBtn && issueCombo.getValue() != null) {
                 bookIssueDAO.returnBook(issueCombo.getValue().getId(), 1); 
                 return issueCombo.getValue();
             }
             return null;
         });
         dialog.showAndWait().ifPresent(i -> { loadBooks(); showAlert("Success", "Book Returned!"); });
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
