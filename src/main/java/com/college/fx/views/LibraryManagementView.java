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
import com.college.dao.BookRequestDAO;
import com.college.dao.StudentDAO;
import com.college.models.BookIssue;
import com.college.models.BookRequest;
import com.college.models.Student;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import java.util.stream.Collectors;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.util.StringConverter;
import java.util.List;

/**
 * JavaFX Library Management View
 */
public class LibraryManagementView {

    private VBox root;

    // Catalog Tab
    private TableView<Book> catalogTable;
    private ObservableList<Book> bookData;

    // Issued Books Tab (for Students)
    private TableView<BookIssue> issuedTable;
    private ObservableList<BookIssue> issuedData;

    // Requests Tab (for Students)
    private TableView<BookRequest> requestTable;
    private ObservableList<BookRequest> requestData;

    // Review Requests Tab (for Librarian/Admin)
    private TableView<BookRequest> reviewTable;
    private ObservableList<BookRequest> reviewData;

    private LibraryDAO libraryDAO;
    private BookIssueDAO bookIssueDAO;
    private BookRequestDAO bookRequestDAO;
    private StudentDAO studentDAO;

    private String role;
    private int userId;
    private TextField searchField;

    public LibraryManagementView(String role, int userId) {
        this.role = role;
        this.userId = userId;
        this.libraryDAO = new LibraryDAO();
        this.bookIssueDAO = new BookIssueDAO();
        this.bookRequestDAO = new BookRequestDAO();
        this.studentDAO = new StudentDAO();

        this.bookData = FXCollections.observableArrayList();
        this.issuedData = FXCollections.observableArrayList();
        this.requestData = FXCollections.observableArrayList();
        this.reviewData = FXCollections.observableArrayList();

        createView();
        refreshData();
    }

    private void createView() {
        root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("glass-pane");
        root.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());

        Label title = new Label(role.equals("STUDENT") ? "Library" : "Library Management");
        title.getStyleClass().add("section-title");
        title.setPadding(new Insets(0, 0, 10, 0));

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("floating");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // 1. Catalog Tab (All Books)
        Tab catalogTab = new Tab("Book Catalog");
        catalogTab.setContent(createCatalogTab());

        tabPane.getTabs().add(catalogTab);

        if ("STUDENT".equals(role)) {
            // 2. My Issued Books Tab
            Tab issuedTab = new Tab("My Issued Books");
            issuedTab.setContent(createIssuedBooksTab());
            tabPane.getTabs().add(issuedTab);

            // 3. My Requests Tab
            Tab requestsTab = new Tab("My Requests");
            requestsTab.setContent(createRequestsTab());
            tabPane.getTabs().add(requestsTab);
        }

        if (SessionManager.getInstance().hasPermission("MANAGE_LIBRARY")) {
            // 4. Review Requests Tab (Librarian)
            Tab reviewTab = new Tab("Review Requests");
            reviewTab.setContent(createReviewRequestsTab());
            tabPane.getTabs().add(reviewTab);
        }

        root.getChildren().addAll(title, tabPane);
    }

    private void refreshData() {
        loadBooks();
        if ("STUDENT".equals(role)) {
            loadIssuedBooks();
            loadRequests();
        }
        if (SessionManager.getInstance().hasPermission("MANAGE_LIBRARY")) {
            loadPendingRequests();
        }
    }

    // --- Catalog Tab ---

    private VBox createCatalogTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        // Search Bar
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by Title or Author...");
        searchField.setPrefWidth(300);

        Button searchBtn = createButton("Search", "#14b8a6");
        searchBtn.setOnAction(e -> searchBooks());

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadBooks());

        header.getChildren().addAll(searchField, searchBtn, refreshBtn);

        // Table
        catalogTable = new TableView<>();
        catalogTable.setItems(bookData);

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

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAvailable() > 0 ? "Available (" + data.getValue().getAvailable() + ")"
                        : "Out of Stock"));
        statusCol.setPrefWidth(120);

        catalogTable.getColumns().addAll(idCol, titleCol, authorCol, isbnCol, statusCol);
        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

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
            buttonBox.getChildren().addAll(addBtn, editBtn, issueBtn, returnBtn);
        } else if ("STUDENT".equals(role)) {
            Button requestBtn = createButton("Request Book", "#14b8a6");
            requestBtn.setOnAction(e -> requestBook());
            buttonBox.getChildren().add(requestBtn);
        }

        content.getChildren().addAll(header, catalogTable, buttonBox);
        return content;
    }

    // --- My Issued Books Tab ---

    private VBox createIssuedBooksTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        issuedTable = new TableView<>();
        issuedTable.setItems(issuedData);

        TableColumn<BookIssue, String> bTitleCol = new TableColumn<>("Book Title");
        bTitleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        bTitleCol.setPrefWidth(250);

        TableColumn<BookIssue, String> issueDateCol = new TableColumn<>("Issue Date");
        issueDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIssueDate().toString()));

        TableColumn<BookIssue, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().toString()));

        TableColumn<BookIssue, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        // Fine column
        TableColumn<BookIssue, String> fineCol = new TableColumn<>("Current Fine");
        fineCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("Rs. %.2f", data.getValue().calculateFine(5.0))));

        issuedTable.getColumns().addAll(bTitleCol, issueDateCol, dueDateCol, statusCol, fineCol);
        VBox.setVgrow(issuedTable, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadIssuedBooks());

        totalFineLabel = new Label("Total Pending Fine: Rs. 0.00");
        totalFineLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        totalFineLabel.setTextFill(Color.RED);

        HBox bottomBox = new HBox(10, refreshBtn, totalFineLabel);
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(issuedTable, bottomBox);
        return content;
    }

    private Label totalFineLabel;

    // --- My Requests Tab ---

    private VBox createRequestsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        requestTable = new TableView<>();
        requestTable.setItems(requestData);

        TableColumn<BookRequest, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        titleCol.setPrefWidth(250);

        TableColumn<BookRequest, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestDate().toString()));

        TableColumn<BookRequest, String> msgCol = new TableColumn<>("Remarks");
        msgCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRemarks()));

        TableColumn<BookRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<BookRequest, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equals(item))
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    else if ("REJECTED".equals(item))
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                }
            }
        });

        requestTable.getColumns().addAll(titleCol, dateCol, msgCol, statusCol);
        VBox.setVgrow(requestTable, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadRequests());

        refreshBtn.setOnAction(e -> loadRequests());

        content.getChildren().addAll(requestTable, refreshBtn);
        return content;
    }

    // --- Review Requests Tab (Librarian) ---

    private VBox createReviewRequestsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(10));

        reviewTable = new TableView<>();
        reviewTable.setItems(reviewData);

        TableColumn<BookRequest, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        idCol.setPrefWidth(50);

        TableColumn<BookRequest, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<BookRequest, String> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBookTitle()));
        bookCol.setPrefWidth(200);

        TableColumn<BookRequest, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequestDate().toString()));

        TableColumn<BookRequest, String> remarksCol = new TableColumn<>("Remarks");
        remarksCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRemarks()));

        TableColumn<BookRequest, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            {
                approveBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                approveBtn.setOnAction(event -> handleApprove(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(event -> handleReject(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox pane = new HBox(10, approveBtn, rejectBtn);
                    pane.setAlignment(Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });

        reviewTable.getColumns().addAll(idCol, studentCol, bookCol, dateCol, remarksCol, actionCol);
        VBox.setVgrow(reviewTable, Priority.ALWAYS);

        Button refreshBtn = createButton("Refresh", "#3b82f6");
        refreshBtn.setOnAction(e -> loadPendingRequests());

        content.getChildren().addAll(reviewTable, refreshBtn);
        return content;
    }

    private void handleApprove(BookRequest req) {
        if (bookRequestDAO.approveRequest(req.getId(), userId)) {
            showAlert("Success", "Request Approved! Book Issued.");
            loadPendingRequests();
            loadBooks(); // refresh availability
        } else {
            showAlert("Error", "Failed to approve. Book might be out of stock.");
        }
    }

    private void handleReject(BookRequest req) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Request");
        dialog.setHeaderText("Rejecting Request for: " + req.getBookTitle());
        dialog.setContentText("Reason for Rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            if (bookRequestDAO.rejectRequest(req.getId(), userId, reason)) {
                showAlert("Success", "Request Rejected.");
                loadPendingRequests();
            } else {
                showAlert("Error", "Failed to reject request.");
            }
        });
    }

    // --- Data Loaders ---

    private void loadBooks() {
        bookData.clear();
        bookData.addAll(libraryDAO.getAllBooks());
    }

    private void loadIssuedBooks() {
        if (!"STUDENT".equals(role))
            return;
        Student s = studentDAO.getStudentByUserId(userId);
        if (s != null) {
            bookIssueDAO.updateFinesForStudent(s.getId()); // Update fines first
            issuedData.clear();
            issuedData.addAll(bookIssueDAO.getIssuedBooksByStudent(s.getId()));

            double totalFine = bookIssueDAO.getPendingFines(s.getId());
            if (totalFineLabel != null) {
                totalFineLabel.setText(String.format("Total Pending Fine: Rs. %.2f", totalFine));
            }
        }
    }

    private void loadRequests() {
        if (!"STUDENT".equals(role))
            return;
        Student s = studentDAO.getStudentByUserId(userId);
        if (s != null) {
            requestData.clear();
            requestData.addAll(bookRequestDAO.getRequestsByStudent(s.getId()));
        }
    }

    private void loadPendingRequests() {
        if (!SessionManager.getInstance().hasPermission("MANAGE_LIBRARY"))
            return;
        reviewData.clear();
        reviewData.addAll(bookRequestDAO.getPendingRequests());
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

    // --- Actions ---

    private void requestBook() {
        Book selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to request.");
            return;
        }
        if (selected.getAvailable() <= 0) {
            showAlert("Error", "This book is currently out of stock.");
            return;
        }

        Student s = studentDAO.getStudentByUserId(userId);
        if (s == null) {
            showAlert("Error", "Student profile not found.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Book");
        dialog.setHeaderText("Requesting: " + selected.getTitle());
        dialog.setContentText("Remarks (Optional):");

        dialog.showAndWait().ifPresent(remarks -> {
            BookRequest req = new BookRequest();
            req.setStudentId(s.getId());
            req.setBookId(selected.getId());
            req.setLoanPeriodDays(14); // Default 14 days
            req.setRemarks(remarks);

            if (bookRequestDAO.createRequest(req)) {
                showAlert("Success", "Request submitted successfully!");
                loadRequests();
            } else {
                showAlert("Error", "Failed to submit request.");
            }
        });
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

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2);
        grid.add(isbnField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtySpinner, 1, 3);

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
        dialog.showAndWait().ifPresent(b -> {
            loadBooks();
            showAlert("Success", "Book added!");
        });
    }

    private void editBook() {
        Book selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to edit.");
            return;
        }

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Edit Book");
        dialog.setHeaderText("Edit Book: " + selected.getTitle());
        ButtonType saveBtn = new ButtonType("Update", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(selected.getTitle());
        TextField authorField = new TextField(selected.getAuthor());
        TextField isbnField = new TextField(selected.getIsbn());
        Spinner<Integer> qtySpinner = new Spinner<>(1, 100, selected.getQuantity());

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2);
        grid.add(isbnField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtySpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                int oldQty = selected.getQuantity();
                int newQty = qtySpinner.getValue();
                int diff = newQty - oldQty;

                selected.setTitle(titleField.getText());
                selected.setAuthor(authorField.getText());
                selected.setIsbn(isbnField.getText());
                selected.setQuantity(newQty);
                selected.setAvailable(selected.getAvailable() + diff);

                libraryDAO.updateBook(selected);
                return selected;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(b -> {
            loadBooks();
            showAlert("Success", "Book updated!");
        });
    }

    private void showIssueBookDialog() {
        Dialog<BookIssue> dialog = new Dialog<>();
        dialog.setTitle("Issue Book");
        dialog.setHeaderText("Issue Book to Student");
        ButtonType issueBtn = new ButtonType("Issue", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(issueBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
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
        List<Book> avlBooks = libraryDAO.getAllBooks().stream().filter(b -> b.getAvailable() > 0)
                .collect(Collectors.toList());
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

        grid.add(new Label("Filter Student:"), 0, 0);
        grid.add(studentSearch, 1, 0);
        grid.add(new Label("Select Student:"), 0, 1);
        grid.add(studentCombo, 1, 1);
        grid.add(new Label("Filter Book:"), 0, 2);
        grid.add(bookSearch, 1, 2);
        grid.add(new Label("Select Book:"), 0, 3);
        grid.add(bookCombo, 1, 3);
        grid.add(new Label("Due Date:"), 0, 4);
        grid.add(dueDate, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == issueBtn && studentCombo.getValue() != null && bookCombo.getValue() != null) {
                BookIssue issue = new BookIssue();
                issue.setStudentId(studentCombo.getValue().getId());
                issue.setBookId(bookCombo.getValue().getId());
                issue.setIssueDate(new Date());
                issue.setDueDate(Date.from(dueDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                issue.setIssuedBy(1); // Default admin ID
                bookIssueDAO.issueBook(issue);
                return issue;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(i -> {
            loadBooks();
            showAlert("Success", "Book Issued!");
        });
    }

    private void showReturnBookDialog() {
        Dialog<BookIssue> dialog = new Dialog<>();
        dialog.setTitle("Return Book");
        dialog.setHeaderText("Select Book to Return");
        ButtonType returnBtn = new ButtonType("Return", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(returnBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField searchIssue = new TextField();
        searchIssue.setPromptText("Search Book/Student");

        ComboBox<BookIssue> issueCombo = new ComboBox<>();
        issueCombo.setPrefWidth(300);
        List<BookIssue> allIssues = bookIssueDAO.getAllIssuedBooks();
        issueCombo.getItems().addAll(allIssues);

        issueCombo.setConverter(new StringConverter<BookIssue>() {
            public String toString(BookIssue object) {
                if (object == null)
                    return "";
                return object.getBookTitle() + " (" + object.getStudentName() + ")";
            }

            public BookIssue fromString(String string) {
                return null;
            }
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
            if (bi != null)
                fineField.setText(String.valueOf(bi.calculateFine(5.0)));
        });

        grid.add(new Label("Filter:"), 0, 0);
        grid.add(searchIssue, 1, 0);
        grid.add(new Label("Issued Book:"), 0, 1);
        grid.add(issueCombo, 1, 1);
        grid.add(new Label("Fine:"), 0, 2);
        grid.add(fineField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == returnBtn && issueCombo.getValue() != null) {
                bookIssueDAO.returnBook(issueCombo.getValue().getId(), 1);
                return issueCombo.getValue();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(i -> {
            loadBooks();
            BookIssue processed = issueCombo.getValue();
            double fine = processed.calculateFine(5.0);
            if (fine > 0) {
                showAlert("Success", "Book Returned!\nPlease collect Fine: Rs. " + String.format("%.2f", fine));
            } else {
                showAlert("Success", "Book Returned!");
            }
        });
    }

    // --- Helpers ---

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(120);
        btn.setPrefHeight(35);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        return btn;
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
