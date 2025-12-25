package org.example.project.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.project.ProjectApplication;
import org.example.project.dao.*;
import org.example.project.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainView extends BorderPane {

    // =============================
    // ========= THEME COLORS ======
    // =============================
    private static final String MAIN_BG = "#f5c5cd";
    private static final String ACCENT  = "#a53d38";
    private static final String ACCENT_HOVER = "#8f332f";
    private static final String CARD_BG = "rgba(255,255,255,0.65)";
    private static final String CARD_BORDER = "rgba(165,61,56,0.18)";
    private static final String TEXT_DARK = "#2b2b2b";
    private static final String MUTED = "rgba(43,43,43,0.70)";

    private final boolean canEdit;

    public MainView(User user) {
        this.canEdit = user.getRole().equalsIgnoreCase("admin");

        setBackground(new Background(new BackgroundFill(Color.web(MAIN_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        // ---------- TOP BAR ----------
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(14));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-border-color: %s;
            -fx-border-width: 0 0 1 0;
        """.formatted(CARD_BORDER));

        Label title = new Label("📚 Library Management System");
        title.setFont(Font.font("Poppins", 18));
        title.setTextFill(Color.web(TEXT_DARK));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label roleLabel = new Label("Logged in as: " + user.getUsername() + " (" + user.getRole() + ")");
        roleLabel.setTextFill(Color.web(MUTED));
        roleLabel.setFont(Font.font("Poppins", 13));

        Button logout = primaryBtn("Logout");
        logout.setOnAction(e -> ProjectApplication.showLoginScreen());

        topBar.getChildren().addAll(title, spacer, roleLabel, logout);
        setTop(topBar);

        // ---------- TABS ----------
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        styleTabPane(tabs);

        tabs.getTabs().addAll(
                borrowersTab(),
                booksTab(),
                publishersTab(),
                authorsTab(),
                loansTab(),
                salesTab(),
                reportsTab(),
                aboutTab()
        );

        StackPane centerHolder = new StackPane(tabs);
        centerHolder.setPadding(new Insets(14));
        setCenter(centerHolder);
    }

    // =========================================================
    // ===================== BORROWERS TAB =====================
    // =========================================================
    private Tab borrowersTab() {
        TableView<Borrower> table = new TableView<>();
        styleTable(table);

        TableColumn<Borrower, Integer> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<Borrower, String> name = new TableColumn<>("Full Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFirstName() + " " + c.getValue().getLastName()));

        TableColumn<Borrower, String> contact = new TableColumn<>("Contact");
        contact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContact()));

        table.getColumns().addAll(id, name, contact);

        try { table.getItems().setAll(BorrowerDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search borrowers (id, name, contact)...", text -> {
            try {
                List<Borrower> all = BorrowerDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(all);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Borrower> filtered = all.stream().filter(b -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(b.getId()).append(' ');
                    if (b.getFirstName() != null) sb.append(b.getFirstName()).append(' ');
                    if (b.getLastName() != null) sb.append(b.getLastName()).append(' ');
                    if (b.getContact() != null) sb.append(b.getContact()).append(' ');

                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "ID", "Name", "Contact");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Borrower> allBorrowers = BorrowerDAO.findAll();
                switch (field) {
                    case "ID" -> allBorrowers.forEach(b -> valueCombo.getItems().add(String.valueOf(b.getId())));
                    case "Name" -> allBorrowers.forEach(b -> valueCombo.getItems().add(b.getFirstName() + " " + b.getLastName()));
                    case "Contact" -> allBorrowers.forEach(b -> valueCombo.getItems().add(b.getContact()));
                    case "All" -> table.getItems().setAll(allBorrowers);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(BorrowerDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Borrower> allBorrowers = BorrowerDAO.findAll();
                List<Borrower> filtered = allBorrowers.stream().filter(b -> switch (fieldCombo.getValue()) {
                    case "ID" -> String.valueOf(b.getId()).equals(selectedValue);
                    case "Name" -> (b.getFirstName() + " " + b.getLastName()).equals(selectedValue);
                    case "Contact" -> b.getContact().equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button edit = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, edit, del);

        add.setOnAction(e -> Dialogs.showAddBorrowerForm(table));
        edit.setOnAction(e -> Dialogs.showUpdateBorrowerForm(table));
        del.setOnAction(e -> Dialogs.showDeleteBorrowerDialog(table));

        HBox actions = new HBox(10, add, edit, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Borrowers", search, filterBox, table, actions);
        return new Tab("Borrowers", box);
    }

    // =========================================================
    // ======================= BOOKS TAB =======================
    // =========================================================
    private Tab booksTab() {
        TableView<Book> table = new TableView<>();
        styleTable(table);

        TableColumn<Book, Integer> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getBookId()).asObject());

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Book, String> cat = new TableColumn<>("Category");
        cat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));

        TableColumn<Book, Boolean> avail = new TableColumn<>("Available");
        avail.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isAvailable()));

        TableColumn<Book, BigDecimal> price = new TableColumn<>("Price");
        price.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getOriginalPrice()));

        table.getColumns().addAll(id, titleCol, cat, avail, price);

        try { table.getItems().setAll(BookDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search books (title, id, category)...", text -> {
            try {
                List<Book> allBooks = BookDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(allBooks);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Book> filtered = allBooks.stream().filter(b -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(b.getBookId()).append(' ');
                    if (b.getTitle() != null) sb.append(b.getTitle()).append(' ');
                    if (b.getCategory() != null) sb.append(b.getCategory()).append(' ');
                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "ID", "Title", "Category");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Book> allBooks = BookDAO.findAll();
                switch (field) {
                    case "ID" -> allBooks.forEach(b -> valueCombo.getItems().add(String.valueOf(b.getBookId())));
                    case "Title" -> allBooks.forEach(b -> valueCombo.getItems().add(b.getTitle()));
                    case "Category" -> allBooks.stream().map(Book::getCategory).distinct().forEach(valueCombo.getItems()::add);
                    case "All" -> table.getItems().setAll(allBooks);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(BookDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Book> allBooks = BookDAO.findAll();
                List<Book> filtered = allBooks.stream().filter(b -> switch (fieldCombo.getValue()) {
                    case "ID" -> String.valueOf(b.getBookId()).equals(selectedValue);
                    case "Title" -> b.getTitle().equals(selectedValue);
                    case "Category" -> b.getCategory().equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button upd = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, upd, del);

        add.setOnAction(e -> Dialogs.showAddBookDialog(table));
        upd.setOnAction(e -> Dialogs.showUpdateBookDialog(table));
        del.setOnAction(e -> Dialogs.showDeleteBookDialog(table));

        HBox actions = new HBox(10, add, upd, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Books", search, filterBox, table, actions);
        return new Tab("Books", box);
    }

    // =========================================================
    // ==================== PUBLISHERS TAB =====================
    // =========================================================
    private Tab publishersTab() {
        TableView<Publisher> table = new TableView<>();
        styleTable(table);

        TableColumn<Publisher, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Publisher, String> city = new TableColumn<>("City");
        city.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCity()));

        table.getColumns().addAll(name, city);

        try { table.getItems().setAll(PublisherDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search publishers (name, city)...", text -> {
            try {
                List<Publisher> all = PublisherDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(all);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Publisher> filtered = all.stream().filter(p -> {
                    StringBuilder sb = new StringBuilder();
                    if (p.getName() != null) sb.append(p.getName()).append(' ');
                    if (p.getCity() != null) sb.append(p.getCity()).append(' ');
                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "Name", "City");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Publisher> allPublishers = PublisherDAO.findAll();
                switch (field) {
                    case "Name" -> allPublishers.forEach(p -> valueCombo.getItems().add(p.getName()));
                    case "City" -> allPublishers.stream().map(Publisher::getCity).distinct().forEach(valueCombo.getItems()::add);
                    case "All" -> table.getItems().setAll(allPublishers);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(PublisherDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Publisher> allPublishers = PublisherDAO.findAll();
                List<Publisher> filtered = allPublishers.stream().filter(p -> switch (fieldCombo.getValue()) {
                    case "Name" -> p.getName().equals(selectedValue);
                    case "City" -> p.getCity().equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button upd = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, upd, del);

        add.setOnAction(e -> Dialogs.showAddPublisherForm(table));
        upd.setOnAction(e -> Dialogs.showUpdatePublisherForm(table));
        del.setOnAction(e -> Dialogs.showDeletePublisherDialog(table));

        HBox actions = new HBox(10, add, upd, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Publishers", search, filterBox, table, actions);
        return new Tab("Publishers", box);
    }

    // =========================================================
    // ====================== AUTHORS TAB ======================
    // =========================================================
    private Tab authorsTab() {
        TableView<Author> table = new TableView<>();
        styleTable(table);

        TableColumn<Author, Integer> id = new TableColumn<>("ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getAuthorId()).asObject());

        TableColumn<Author, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getFirstName() + " " + c.getValue().getLastName()));

        TableColumn<Author, String> country = new TableColumn<>("Country");
        country.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCountry()));

        TableColumn<Author, String> bio = new TableColumn<>("Bio");
        bio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBio()));

        table.getColumns().addAll(id, name, country, bio);

        try { table.getItems().setAll(AuthorDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search authors (id, name, country, bio)...", text -> {
            try {
                List<Author> all = AuthorDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(all);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Author> filtered = all.stream().filter(a -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(a.getAuthorId()).append(' ');
                    if (a.getFirstName() != null) sb.append(a.getFirstName()).append(' ');
                    if (a.getLastName() != null) sb.append(a.getLastName()).append(' ');
                    if (a.getCountry() != null) sb.append(a.getCountry()).append(' ');
                    if (a.getBio() != null) sb.append(a.getBio()).append(' ');
                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "ID", "Name", "Country");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Author> allAuthors = AuthorDAO.findAll();
                switch (field) {
                    case "ID" -> allAuthors.forEach(a -> valueCombo.getItems().add(String.valueOf(a.getAuthorId())));
                    case "Name" -> allAuthors.forEach(a -> valueCombo.getItems().add(a.getFirstName() + " " + a.getLastName()));
                    case "Country" -> allAuthors.stream().map(Author::getCountry).distinct().forEach(valueCombo.getItems()::add);
                    case "All" -> table.getItems().setAll(allAuthors);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(AuthorDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Author> allAuthors = AuthorDAO.findAll();
                List<Author> filtered = allAuthors.stream().filter(a -> switch (fieldCombo.getValue()) {
                    case "ID" -> String.valueOf(a.getAuthorId()).equals(selectedValue);
                    case "Name" -> (a.getFirstName() + " " + a.getLastName()).equals(selectedValue);
                    case "Country" -> a.getCountry().equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button upd = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, upd, del);

        add.setOnAction(e -> Dialogs.showAddAuthorForm(table));
        upd.setOnAction(e -> Dialogs.showUpdateAuthorForm(table));
        del.setOnAction(e -> Dialogs.showDeleteAuthorDialog(table));

        HBox actions = new HBox(10, add, upd, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Authors", search, filterBox, table, actions);
        return new Tab("Authors", box);
    }

    // =========================================================
    // ======================= LOANS TAB =======================
    // =========================================================
    private Tab loansTab() {
        TableView<Loan> table = new TableView<>();
        styleTable(table);

        TableColumn<Loan, Integer> id = new TableColumn<>("Loan ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<Loan, String> book = new TableColumn<>("Book ID");
        book.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getBookId())));

        TableColumn<Loan, String> borrower = new TableColumn<>("Borrower ID");
        borrower.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getBorrowerId())));

        TableColumn<Loan, String> loanDate = new TableColumn<>("Loan Date");
        loanDate.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getLoanDate())));

        TableColumn<Loan, String> dueDate = new TableColumn<>("Due Date");
        dueDate.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getDueDate())));

        TableColumn<Loan, String> returnDate = new TableColumn<>("Return Date");
        returnDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getReturnDate() != null ? String.valueOf(c.getValue().getReturnDate()) : ""));

        table.getColumns().addAll(id, book, borrower, loanDate, dueDate, returnDate);

        try { table.getItems().setAll(LoanDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search loans (ids, dates)...", text -> {
            try {
                List<Loan> all = LoanDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(all);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Loan> filtered = all.stream().filter(l -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(l.getId()).append(' ');
                    sb.append(l.getBookId()).append(' ');
                    sb.append(l.getBorrowerId()).append(' ');
                    if (l.getLoanDate() != null) sb.append(l.getLoanDate()).append(' ');
                    if (l.getDueDate() != null) sb.append(l.getDueDate()).append(' ');
                    if (l.getReturnDate() != null) sb.append(l.getReturnDate()).append(' ');
                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "Loan ID", "Book ID", "Borrower ID");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Loan> allLoans = LoanDAO.findAll();
                switch (field) {
                    case "Loan ID" -> allLoans.forEach(l -> valueCombo.getItems().add(String.valueOf(l.getId())));
                    case "Book ID" -> allLoans.forEach(l -> valueCombo.getItems().add(String.valueOf(l.getBookId())));
                    case "Borrower ID" -> allLoans.forEach(l -> valueCombo.getItems().add(String.valueOf(l.getBorrowerId())));
                    case "All" -> table.getItems().setAll(allLoans);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(LoanDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Loan> allLoans = LoanDAO.findAll();
                List<Loan> filtered = allLoans.stream().filter(l -> switch (fieldCombo.getValue()) {
                    case "Loan ID" -> String.valueOf(l.getId()).equals(selectedValue);
                    case "Book ID" -> String.valueOf(l.getBookId()).equals(selectedValue);
                    case "Borrower ID" -> String.valueOf(l.getBorrowerId()).equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button upd = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, upd, del);

        add.setOnAction(e -> Dialogs.showAddLoanForm(table));
        upd.setOnAction(e -> Dialogs.showUpdateLoanForm(table));
        del.setOnAction(e -> Dialogs.showDeleteLoanDialog(table));

        HBox actions = new HBox(10, add, upd, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Loans", search, filterBox, table, actions);
        return new Tab("Loans", box);
    }

    // =========================================================
    // ======================= SALES TAB =======================
    // =========================================================
    private Tab salesTab() {
        TableView<Sale> table = new TableView<>();
        styleTable(table);

        TableColumn<Sale, Integer> id = new TableColumn<>("Sale ID");
        id.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getSaleId()).asObject());

        TableColumn<Sale, String> book = new TableColumn<>("Book ID");
        book.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getBookId())));

        TableColumn<Sale, String> borrower = new TableColumn<>("Borrower ID");
        borrower.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getBorrowerId())));

        TableColumn<Sale, String> price = new TableColumn<>("Price");
        price.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSalePrice())));

        TableColumn<Sale, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSaleDate() != null ? c.getValue().getSaleDate().toString() : ""
        ));

        table.getColumns().addAll(id, book, borrower, price, date);

        try { table.getItems().setAll(SaleDAO.findAll()); } catch (Exception ignored) {}

        TextField search = searchBar("Search sales (id, book, borrower, price, date)...", text -> {
            try {
                List<Sale> all = SaleDAO.findAll();
                String q = text.toLowerCase();
                if (q.isEmpty()) {
                    table.getItems().setAll(all);
                    return;
                }
                String[] tokens = q.split("\\s+");

                List<Sale> filtered = all.stream().filter(s -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(s.getSaleId()).append(' ');
                    sb.append(s.getBookId()).append(' ');
                    sb.append(s.getBorrowerId()).append(' ');
                    sb.append(s.getSalePrice()).append(' ');
                    if (s.getSaleDate() != null) sb.append(s.getSaleDate()).append(' ');
                    String haystack = sb.toString().toLowerCase();
                    for (String token : tokens) if (!haystack.contains(token)) return false;
                    return true;
                }).toList();

                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = softLabel("Filter by:");

        ComboBox<String> fieldCombo = themedCombo();
        fieldCombo.getItems().addAll("All", "Sale ID", "Book ID", "Borrower ID");
        fieldCombo.setValue("All");

        ComboBox<String> valueCombo = themedCombo();
        valueCombo.setPromptText("Select value");

        fieldCombo.setOnAction(e -> {
            valueCombo.getItems().clear();
            String field = fieldCombo.getValue();
            try {
                List<Sale> allSales = SaleDAO.findAll();
                switch (field) {
                    case "Sale ID" -> allSales.forEach(s -> valueCombo.getItems().add(String.valueOf(s.getSaleId())));
                    case "Book ID" -> allSales.forEach(s -> valueCombo.getItems().add(String.valueOf(s.getBookId())));
                    case "Borrower ID" -> allSales.forEach(s -> valueCombo.getItems().add(String.valueOf(s.getBorrowerId())));
                    case "All" -> table.getItems().setAll(allSales);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        valueCombo.setOnAction(e -> {
            String selectedValue = valueCombo.getValue();
            if (selectedValue == null || fieldCombo.getValue().equals("All")) {
                try { table.getItems().setAll(SaleDAO.findAll()); } catch (Exception ignored) {}
                return;
            }
            try {
                List<Sale> allSales = SaleDAO.findAll();
                List<Sale> filtered = allSales.stream().filter(s -> switch (fieldCombo.getValue()) {
                    case "Sale ID" -> String.valueOf(s.getSaleId()).equals(selectedValue);
                    case "Book ID" -> String.valueOf(s.getBookId()).equals(selectedValue);
                    case "Borrower ID" -> String.valueOf(s.getBorrowerId()).equals(selectedValue);
                    default -> true;
                }).toList();
                table.getItems().setAll(filtered);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button add = accentBtn("➕ Add");
        Button upd = accentBtn("✏️ Update");
        Button del = accentBtn("🗑 Delete");
        disableForNonAdmin(add, upd, del);

        add.setOnAction(e -> Dialogs.showAddSaleForm(table));
        upd.setOnAction(e -> Dialogs.showUpdateSaleForm(table));
        del.setOnAction(e -> Dialogs.showDeleteSaleDialog(table));

        HBox actions = new HBox(10, add, upd, del);
        actions.setAlignment(Pos.CENTER_LEFT);

        filterBox.getChildren().addAll(filterLabel, fieldCombo, valueCombo);

        VBox box = contentCard("Sales", search, filterBox, table, actions);
        return new Tab("Sales", box);
    }

    // =========================================================
    // ====================== REPORTS TAB ======================
    // =========================================================
    private Tab reportsTab() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));

        // ---------- CENTER CARD ----------
        Label reportTitle = new Label("Reports");
        reportTitle.setFont(Font.font("Poppins", 18));
        reportTitle.setTextFill(Color.web(TEXT_DARK));

        Label reportHint = new Label("Select a report on the left, then optionally filter the results.");
        reportHint.setTextFill(Color.web(MUTED));

        TextField reportFilter = searchBar("Filter in current report...", t -> {});
        TableView<Map<String, Object>> table = new TableView<>();
        applyPrettyReportTable(table);
        table.widthProperty().addListener((obs, oldW, newW) -> autosizeReportColumns(table));

        Label emptyState = new Label("Select a report to display data.");
        emptyState.setTextFill(Color.web(MUTED));
        emptyState.setStyle("-fx-padding: 20; -fx-font-family: 'Poppins'; -fx-font-weight: 700;");

        StackPane tableHolder = new StackPane(table, emptyState);
        VBox.setVgrow(tableHolder, Priority.ALWAYS);

        Button exportBtn = ghostBtn("⬇ Export CSV");
        Button clearBtn = ghostBtn("✖ Clear");
        HBox actions = new HBox(10, exportBtn, clearBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(10, reportTitle, reportHint, reportFilter, actions, tableHolder);
        card.setPadding(new Insets(16));
        card.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: %s;
        """.formatted(CARD_BG, CARD_BORDER));

        root.setCenter(card);

        // ---------- CURRENT DATA ----------
        final List<Map<String, Object>>[] currentData = new List[1];

        Consumer<List<Map<String, Object>>> showData = data -> {
            table.getColumns().clear();
            table.getItems().clear();

            if (data == null || data.isEmpty()) {
                currentData[0] = null;
                emptyState.setVisible(true);
                return;
            }

            emptyState.setVisible(false);
            currentData[0] = data;

            Map<String, Object> sample = data.get(0);
            for (String colName : sample.keySet()) {
                TableColumn<Map<String, Object>, String> col = new TableColumn<>(colName);
                col.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().get(colName))));
                col.setStyle("-fx-alignment: CENTER-LEFT;");
                table.getColumns().add(col);
            }

            table.getItems().addAll(data);
            autosizeReportColumns(table);
        };

        reportFilter.textProperty().addListener((o, a, n) -> {
            if (currentData[0] == null) return;

            String q = n.trim().toLowerCase();
            if (q.isEmpty()) {
                table.getItems().setAll(currentData[0]);
                return;
            }
            String[] tokens = q.split("\\s+");

            List<Map<String, Object>> filtered = currentData[0].stream()
                    .filter(row -> {
                        StringBuilder sb = new StringBuilder();
                        for (Object value : row.values()) if (value != null) sb.append(value).append(" ");
                        String rowText = sb.toString().toLowerCase();
                        for (String token : tokens) if (!rowText.contains(token)) return false;
                        return true;
                    }).toList();

            table.getItems().setAll(filtered);
        });

        exportBtn.setOnAction(e -> exportCurrentTableToCSV(table));
        clearBtn.setOnAction(e -> {
            table.getColumns().clear();
            table.getItems().clear();
            currentData[0] = null;
            reportFilter.clear();
            emptyState.setVisible(true);
        });

        // ---------- LEFT SIDEBAR ----------
        Label sideTitle = new Label("📈 Reports");
        sideTitle.setTextFill(Color.web(TEXT_DARK));
        sideTitle.setFont(Font.font("Poppins", 16));

        Label sideSmall = new Label("Tables & Charts");
        sideSmall.setTextFill(Color.web(MUTED));

        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(14));
        sidebar.setPrefWidth(300);
        sidebar.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: %s;
        """.formatted(CARD_BG, CARD_BORDER));

        Button totalValueBtn    = reportBtn("Total value of all books");
        Button byAuthorBtn      = reportBtn("Books by author");
        Button byBorrowerBtn    = reportBtn("Books by borrower");
        Button loansBtn         = reportBtn("Current loans & due dates");
        Button byCountryBtn     = reportBtn("Books published by country");
        Button neverBorrowedBtn = reportBtn("Borrowers never borrowed/bought");
        Button multiAuthorsBtn  = reportBtn("Books with multiple authors");
        Button soldBooksBtn     = reportBtn("Sold books with prices");
        Button availableBtn     = reportBtn("Books currently available");
        Button loanHistoryBtn   = reportBtn("Loan history by borrower");
        Button betweenDatesBtn  = reportBtn("Books borrowed between dates");

        Separator sep = new Separator();

        Button catChartBtn   = reportBtn("📊 Books per category (Bar)");
        Button availPieBtn   = reportBtn("🟠 Availability overview (Pie)");
        Button loansLineBtn  = reportBtn("📈 Loans per month (Line)");
        Button salesBarBtn   = reportBtn("💰 Sales revenue per month (Bar)");
        Button topBorBtn     = reportBtn("🏆 Top borrowers (Bar)");

        VBox btns = new VBox(8,
                totalValueBtn, byAuthorBtn, byBorrowerBtn, loansBtn, byCountryBtn,
                neverBorrowedBtn, multiAuthorsBtn, soldBooksBtn, availableBtn,
                loanHistoryBtn, betweenDatesBtn,
                sep,
                catChartBtn, availPieBtn, loansLineBtn, salesBarBtn, topBorBtn
        );

        ScrollPane scroll = new ScrollPane(btns);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        sidebar.getChildren().addAll(sideTitle, sideSmall, scroll);
        root.setLeft(sidebar);
        BorderPane.setMargin(sidebar, new Insets(0, 12, 0, 0));

        // ---------- REPORT ACTIONS ----------
        totalValueBtn.setOnAction(e -> { try { showData.accept(ReportDAO.totalValueOfAllBooks()); } catch (Exception ex) { ex.printStackTrace(); } });

        byAuthorBtn.setOnAction(e -> {
            Dialog<Integer> dialog = new Dialog<>();
            dialog.setTitle("Choose Author");
            dialog.setHeaderText("Select an author:");

            ComboBox<Author> combo = new ComboBox<>();
            try { combo.getItems().setAll(AuthorDAO.findAll()); } catch (Exception ignored) {}
            combo.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Author a, boolean empty) {
                    super.updateItem(a, empty);
                    setText(empty || a == null ? null : a.getFirstName() + " " + a.getLastName());
                }
            });
            combo.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Author a, boolean empty) {
                    super.updateItem(a, empty);
                    setText(empty || a == null ? null : a.getFirstName() + " " + a.getLastName());
                }
            });

            dialog.getDialogPane().setContent(combo);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Dialogs.showInfo("", ""); // does nothing visual if you remove it; keep your dialogs theme class separate if you want

            dialog.setResultConverter(bt -> bt == ButtonType.OK && combo.getValue() != null ? combo.getValue().getAuthorId() : null);

            dialog.showAndWait().ifPresent(id -> {
                try { showData.accept(ReportDAO.booksByAuthor(id)); } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        byBorrowerBtn.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Choose Borrower");
            dlg.setHeaderText("Enter borrower ID:");
            dlg.showAndWait().ifPresent(id -> {
                try { showData.accept(ReportDAO.booksByBorrower(Integer.parseInt(id))); } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        loansBtn.setOnAction(e -> { try { showData.accept(ReportDAO.currentLoans()); } catch (Exception ex) { ex.printStackTrace(); } });

        byCountryBtn.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Publisher Country");
            dlg.setHeaderText("Enter publisher country:");
            dlg.showAndWait().ifPresent(country -> {
                try { showData.accept(ReportDAO.booksByPublisherCountry(country)); } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        neverBorrowedBtn.setOnAction(e -> { try { showData.accept(ReportDAO.borrowersNeverBorrowed()); } catch (Exception ex) { ex.printStackTrace(); } });

        multiAuthorsBtn.setOnAction(e -> {
            try {
                List<Map<String, Object>> data = ReportDAO.booksWithMultipleAuthors();
                if (data.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "No books have more than one author.").showAndWait();
                    clearBtn.fire();
                } else showData.accept(data);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        soldBooksBtn.setOnAction(e -> { try { showData.accept(ReportDAO.soldBooksWithPrices()); } catch (Exception ex) { ex.printStackTrace(); } });
        availableBtn.setOnAction(e -> { try { showData.accept(ReportDAO.booksCurrentlyAvailable()); } catch (Exception ex) { ex.printStackTrace(); } });

        loanHistoryBtn.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setTitle("Loan History");
            dlg.setHeaderText("Enter borrower ID:");
            dlg.showAndWait().ifPresent(id -> {
                try { showData.accept(ReportDAO.loanHistoryForBorrower(Integer.parseInt(id))); } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        betweenDatesBtn.setOnAction(e -> {
            Dialog<List<String>> dlg = new Dialog<>();
            dlg.setTitle("Borrowed Between Dates");
            dlg.setHeaderText("Choose start and end dates:");
            DatePicker from = new DatePicker();
            DatePicker to = new DatePicker();
            VBox v = new VBox(10, new Label("From:"), from, new Label("To:"), to);
            v.setPadding(new Insets(10));
            dlg.getDialogPane().setContent(v);
            dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dlg.setResultConverter(bt -> bt == ButtonType.OK && from.getValue() != null && to.getValue() != null
                    ? List.of(from.getValue().toString(), to.getValue().toString())
                    : null);

            dlg.showAndWait().ifPresent(dates -> {
                try { showData.accept(ReportDAO.booksBorrowedBetween(dates.get(0), dates.get(1))); } catch (Exception ex) { ex.printStackTrace(); }
            });
        });

        // ---------- CHART WIRING ----------
        catChartBtn.setOnAction(e -> { try { Charts.showCategoryChart(ReportDAO.booksPerCategory()); } catch (Exception ex) { ex.printStackTrace(); } });
        availPieBtn.setOnAction(e -> { try { Charts.showAvailabilityPie(ReportDAO.availabilitySummary()); } catch (Exception ex) { ex.printStackTrace(); } });
        loansLineBtn.setOnAction(e -> { try { Charts.showLoansPerMonthLine(ReportDAO.loansPerMonth()); } catch (Exception ex) { ex.printStackTrace(); } });
        salesBarBtn.setOnAction(e -> { try { Charts.showSalesRevenueBar(ReportDAO.salesRevenuePerMonth()); } catch (Exception ex) { ex.printStackTrace(); } });
        topBorBtn.setOnAction(e -> { try { Charts.showTopBorrowersBar(ReportDAO.topBorrowers(10)); } catch (Exception ex) { ex.printStackTrace(); } });

        return new Tab("Reports", root);
    }

    // =========================================================
    // ======================= ABOUT TAB =======================
    // =========================================================
    private Tab aboutTab() {
        Tab tab = new Tab("About");
        tab.setContent(new AboutView());
        return tab;
    }


    // =========================================================
    // ===================== HELPER METHODS ====================
    // =========================================================
    private VBox contentCard(String header, Node... nodes) {
        Label h = new Label(header);
        h.setFont(Font.font("Poppins", 16));
        h.setTextFill(Color.web(TEXT_DARK));

        VBox v = new VBox(10);
        v.setPadding(new Insets(16));
        v.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 18;
            -fx-border-radius: 18;
            -fx-border-color: %s;
        """.formatted(CARD_BG, CARD_BORDER));

        v.getChildren().add(h);
        v.getChildren().addAll(nodes);
        VBox.setVgrow(v, Priority.ALWAYS);
        return v;
    }

    private TextField searchBar(String placeholder, java.util.function.Consumer<String> onSearch) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setStyle("""
            -fx-background-color: rgba(255,255,255,0.92);
            -fx-text-fill: #2b2b2b;
            -fx-prompt-text-fill: rgba(165,61,56,0.55);
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-padding: 10 12;
            -fx-font-family: 'Poppins';
            -fx-font-size: 13px;
        """);
        tf.textProperty().addListener((o, a, n) -> onSearch.accept(n.trim()));
        return tf;
    }

    private void styleTable(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-control-inner-background: rgba(255,255,255,0.55);
            -fx-table-cell-border-color: transparent;
            -fx-border-color: rgba(165,61,56,0.18);
            -fx-border-radius: 14;
            -fx-background-radius: 14;
        """);
    }

    private ComboBox<String> themedCombo() {
        ComboBox<String> cb = new ComboBox<>();
        cb.setStyle("""
            -fx-background-color: rgba(255,255,255,0.92);
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-font-family: 'Poppins';
            -fx-padding: 2 6;
        """);
        return cb;
    }

    private Label softLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(MUTED));
        l.setFont(Font.font("Poppins", 13));
        return l;
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT));
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT_HOVER)));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 16;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT)));
        return b;
    }

    private Button accentBtn(String text) {
        Button b = new Button(text);
        b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.12);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 14;
            -fx-padding: 10 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT));
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.18);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.30);
            -fx-border-radius: 14;
            -fx-padding: 10 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT)));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.12);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 14;
            -fx-padding: 10 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT)));
        return b;
    }

    private void disableForNonAdmin(Button... buttons) {
        if (!canEdit) for (Button b : buttons) b.setDisable(true);
    }

    private void styleTabPane(TabPane tabs) {
        tabs.setStyle("""
            -fx-background-color: transparent;
            -fx-tab-min-width: 120;
            -fx-font-family: 'Poppins';
            -fx-font-size: 13;
        """);
    }

    // ---------- Reports sidebar buttons ----------
    private Button reportBtn(String text) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.92);
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """);
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: rgba(143,51,47,1.0);
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.92);
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
        """));
        return b;
    }

    private Button ghostBtn(String text) {
        Button b = new Button(text);
        b.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-radius: 14;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT));
        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: rgba(255,255,255,0.70);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-border-color: rgba(165,61,56,0.24);
            -fx-border-radius: 14;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT)));
        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-padding: 10 12;
            -fx-cursor: hand;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-radius: 14;
            -fx-font-family: 'Poppins';
        """.formatted(ACCENT)));
        return b;
    }

    private void applyPrettyReportTable(TableView<Map<String, Object>> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("""
            -fx-background-color: rgba(255,255,255,0.55);
            -fx-control-inner-background: rgba(255,255,255,0.55);
            -fx-table-cell-border-color: transparent;
            -fx-border-color: rgba(165,61,56,0.18);
            -fx-border-radius: 14;
            -fx-background-radius: 14;
        """);
    }

    @SuppressWarnings("unchecked")
    private void autosizeReportColumns(TableView<Map<String, Object>> table) {
        int colCount = table.getColumns().size();
        if (colCount == 0) return;

        double tableWidth = table.getWidth();
        if (tableWidth <= 0) tableWidth = 800;

        double padding = 20;
        double colWidth = (tableWidth - padding) / colCount;

        for (TableColumn<?, ?> c : table.getColumns()) {
            ((TableColumn<Map<String, Object>, ?>) c).setPrefWidth(colWidth);
        }
    }

    private void exportCurrentTableToCSV(TableView<Map<String, Object>> table) {
        if (table.getItems().isEmpty() || table.getColumns().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No data to export.").showAndWait();
            return;
        }

        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Report to CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fc.showSaveDialog(getScene().getWindow());
        if (file == null) return;

        try (java.io.PrintWriter out = new java.io.PrintWriter(file)) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                out.print(table.getColumns().get(i).getText());
                if (i < table.getColumns().size() - 1) out.print(",");
            }
            out.println();

            for (Map<String, Object> row : table.getItems()) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    String col = table.getColumns().get(i).getText();
                    Object v = row.get(col);
                    String s = v == null ? "" : String.valueOf(v).replace("\"", "\"\"");
                    out.print("\"" + s + "\"");
                    if (i < table.getColumns().size() - 1) out.print(",");
                }
                out.println();
            }

            new Alert(Alert.AlertType.INFORMATION, "Exported successfully!").showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).showAndWait();
        }
    }


}
