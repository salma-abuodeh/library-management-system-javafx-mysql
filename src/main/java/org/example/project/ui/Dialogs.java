package org.example.project.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.example.project.dao.*;
import org.example.project.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Dialogs {

    // =============================
    // ========= THEME COLORS ======
    // =============================
    private static final String MAIN_BG = "#f5c5cd";
    private static final String CARD_BG = "rgba(255,255,255,0.72)";
    private static final String ACCENT  = "#a53d38";
    private static final String ACCENT_HOVER = "#8f332f";

    // =============================
    // ======= ALERT HELPERS =======
    // =============================
    public static void showInfo(String title, String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        applyAlertTheme(a);
        a.showAndWait();
    }

    public static void showError(String title, String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        applyAlertTheme(a);
        a.showAndWait();
    }

    // =============================
    // ========= BOOK DIALOGS ======
    // =============================
    public static void showAddBookDialog(TableView<Book> table) {
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add Book");
        dialog.setHeaderText("Fill in book details:");

        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField titleField = new TextField();
        TextField publisherField = new TextField();
        TextField categoryField = new TextField();
        TextField typeField = new TextField();
        TextField priceField = new TextField();
        CheckBox availableBox = new CheckBox("Available");
        availableBox.setSelected(true);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Title:"), 0, 0);        grid.add(titleField, 1, 0);
        grid.add(new Label("Publisher ID:"), 0, 1); grid.add(publisherField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);     grid.add(categoryField, 1, 2);
        grid.add(new Label("Book Type:"), 0, 3);    grid.add(typeField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);        grid.add(priceField, 1, 4);
        grid.add(availableBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        applyDialogTheme(dialog);

        dialog.setResultConverter(button -> {
            if (button == addButton) {
                try {
                    String title = titleField.getText().trim();
                    String category = categoryField.getText().trim();
                    String type = typeField.getText().trim();
                    String priceText = priceField.getText().trim();
                    String publisherText = publisherField.getText().trim();

                    if (title.isEmpty() || category.isEmpty() || type.isEmpty() || priceText.isEmpty()) {
                        showError("Validation error", "Please fill all required fields: Title, Category, Type, Price.");
                        return null;
                    }

                    Integer publisherId = null;
                    if (!publisherText.isEmpty()) {
                        try { publisherId = Integer.parseInt(publisherText); }
                        catch (NumberFormatException ex) {
                            showError("Validation error", "Publisher ID must be a valid number.");
                            return null;
                        }
                    }

                    BigDecimal price;
                    try { price = new BigDecimal(priceText); }
                    catch (NumberFormatException ex) {
                        showError("Validation error", "Price must be a valid number (e.g. 19.99).");
                        return null;
                    }

                    boolean available = availableBox.isSelected();

                    int id = BookDAO.insert(title, publisherId, category, type, price, available);
                    showInfo("Added", "Book ID " + id + " created.");
                    return BookDAO.findAll().stream().filter(b -> b.getBookId() == id).findFirst().orElse(null);

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(book -> {
            if (book != null) {
                try { table.getItems().setAll(BookDAO.findAll()); } catch (Exception ignored) {}
            }
        });
    }

    public static void showUpdateBookDialog(TableView<Book> table) {
        Book b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showInfo("Info", "Select a book first."); return; }

        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Update Book");
        dialog.setHeaderText("Edit book details:");

        ButtonType updateButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CANCEL);

        TextField titleField = new TextField(b.getTitle());
        TextField publisherField = new TextField(b.getPublisherId() == null ? "" : b.getPublisherId().toString());
        TextField categoryField = new TextField(b.getCategory());
        TextField typeField = new TextField(b.getBookType());
        TextField priceField = new TextField(b.getOriginalPrice().toString());
        CheckBox availableBox = new CheckBox("Available");
        availableBox.setSelected(b.isAvailable());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Title:"), 0, 0);        grid.add(titleField, 1, 0);
        grid.add(new Label("Publisher ID:"), 0, 1); grid.add(publisherField, 1, 1);
        grid.add(new Label("Category:"), 0, 2);     grid.add(categoryField, 1, 2);
        grid.add(new Label("Book Type:"), 0, 3);    grid.add(typeField, 1, 3);
        grid.add(new Label("Price:"), 0, 4);        grid.add(priceField, 1, 4);
        grid.add(availableBox, 1, 5);

        dialog.getDialogPane().setContent(grid);
        applyDialogTheme(dialog);

        dialog.setResultConverter(button -> {
            if (button == updateButton) {
                try {
                    String title = titleField.getText().trim();
                    String category = categoryField.getText().trim();
                    String type = typeField.getText().trim();
                    String priceText = priceField.getText().trim();
                    String publisherText = publisherField.getText().trim();

                    if (title.isEmpty() || category.isEmpty() || type.isEmpty() || priceText.isEmpty()) {
                        showError("Validation error", "Please fill all required fields: Title, Category, Type, Price.");
                        return null;
                    }

                    Integer publisherId = null;
                    if (!publisherText.isEmpty()) {
                        try { publisherId = Integer.parseInt(publisherText); }
                        catch (NumberFormatException ex) {
                            showError("Validation error", "Publisher ID must be a valid number.");
                            return null;
                        }
                    }

                    BigDecimal price;
                    try { price = new BigDecimal(priceText); }
                    catch (NumberFormatException ex) {
                        showError("Validation error", "Price must be a valid number.");
                        return null;
                    }

                    boolean available = availableBox.isSelected();

                    BookDAO.update(b.getBookId(), title, publisherId, category, type, price, available);
                    showInfo("Updated", "Book updated successfully.");
                    return new Book(b.getBookId(), title, publisherId, category, type, price, available);

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedBook -> {
            if (updatedBook != null) {
                try { table.getItems().setAll(BookDAO.findAll()); } catch (Exception ignored) {}
            }
        });
    }

    public static void showDeleteBookDialog(TableView<Book> table) {
        Book b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showInfo("Info", "Select a book first."); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + b.getTitle() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.setHeaderText(null);
        applyAlertTheme(c);

        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    BookDAO.delete(b.getBookId());
                    table.getItems().remove(b);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // =============================
    // ======= BORROWER DIALOGS ====
    // =============================
    public static void showAddBorrowerForm(TableView<Borrower> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Borrower");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField fn = new TextField(); fn.setPromptText("First Name");
        TextField ln = new TextField(); ln.setPromptText("Last Name");
        ComboBox<Integer> type = new ComboBox<>(); type.getItems().addAll(1,2,3); type.setValue(1);
        TextField contact = new TextField(); contact.setPromptText("Email");

        VBox form = new VBox(10,
                new Label("First Name:"), fn,
                new Label("Last Name:"), ln,
                new Label("Type:"), type,
                new Label("Email:"), contact
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                String firstName  = fn.getText().trim();
                String lastName   = ln.getText().trim();
                String contactVal = contact.getText().trim();

                if (firstName.isEmpty() || lastName.isEmpty() || contactVal.isEmpty()) {
                    showError("Validation error", "Please fill all fields (first name, last name, email).");
                    return null;
                }
                if (!isValidEmail(contactVal)) {
                    showError("Validation error", "Please enter a valid email address (e.g. user@example.com).");
                    return null;
                }

                try {
                    BorrowerDAO.insert(firstName, lastName, type.getValue(), contactVal);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(BorrowerDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showUpdateBorrowerForm(TableView<Borrower> table) {
        Borrower b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showInfo("Info", "Select a borrower first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Borrower");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        TextField fn = new TextField(b.getFirstName());
        TextField ln = new TextField(b.getLastName());
        ComboBox<Integer> type = new ComboBox<>(); type.getItems().addAll(1,2,3); type.setValue(b.getTypeId());
        TextField contact = new TextField(b.getContact());
        contact.setPromptText("Email");

        VBox form = new VBox(10,
                new Label("First Name:"), fn,
                new Label("Last Name:"), ln,
                new Label("Type:"), type,
                new Label("Email:"), contact
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                String firstName  = fn.getText().trim();
                String lastName   = ln.getText().trim();
                String contactVal = contact.getText().trim();

                if (firstName.isEmpty() || lastName.isEmpty() || contactVal.isEmpty()) {
                    showError("Validation error", "Please fill all fields (first name, last name, email).");
                    return null;
                }
                if (!isValidEmail(contactVal)) {
                    showError("Validation error", "Please enter a valid email address (e.g. user@example.com).");
                    return null;
                }

                try {
                    BorrowerDAO.update(b.getId(), firstName, lastName, type.getValue(), contactVal);
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(BorrowerDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showDeleteBorrowerDialog(TableView<Borrower> table) {
        Borrower b = table.getSelectionModel().getSelectedItem();
        if (b == null) { showInfo("Info", "Select a borrower first."); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + b.getFirstName() + " " + b.getLastName() + "?", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.setHeaderText(null);
        applyAlertTheme(c);

        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    BorrowerDAO.delete(b.getId());
                    table.getItems().remove(b);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // =============================
    // ======= PUBLISHER DIALOGS ===
    // =============================
    public static void showAddPublisherForm(TableView<Publisher> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Publisher");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField name = new TextField();
        TextField city = new TextField();
        TextField country = new TextField();
        TextField contact = new TextField();

        VBox form = new VBox(10,
                new Label("Publisher Name:"), name,
                new Label("City:"), city,
                new Label("Country:"), country,
                new Label("Contact:"), contact
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                String nameVal    = name.getText().trim();
                String cityVal    = city.getText().trim();
                String countryVal = country.getText().trim();
                String contactVal = contact.getText().trim();

                if (nameVal.isEmpty() || cityVal.isEmpty() || countryVal.isEmpty() || contactVal.isEmpty()) {
                    showError("Validation error", "Please fill all fields (name, city, country, contact).");
                    return null;
                }

                if (!isAlphaString(nameVal)) {
                    showError("Validation error", "Publisher name must contain only letters and spaces.");
                    return null;
                }
                if (!isAlphaString(cityVal)) {
                    showError("Validation error", "City must contain only letters and spaces.");
                    return null;
                }
                if (!isAlphaString(countryVal)) {
                    showError("Validation error", "Country must contain only letters and spaces.");
                    return null;
                }

                try { PublisherDAO.insert(nameVal, cityVal, countryVal, contactVal); }
                catch (Exception e) { e.printStackTrace(); showError("Error", e.getMessage()); }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(PublisherDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showUpdatePublisherForm(TableView<Publisher> table) {
        Publisher p = table.getSelectionModel().getSelectedItem();
        if (p == null) { showInfo("Info", "Select a publisher first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Publisher");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        TextField name = new TextField(p.getName());
        TextField city = new TextField(p.getCity());
        TextField country = new TextField(p.getCountry());
        TextField contact = new TextField(p.getContact());

        VBox form = new VBox(10,
                new Label("Publisher Name:"), name,
                new Label("City:"), city,
                new Label("Country:"), country,
                new Label("Contact:"), contact
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                String nameVal    = name.getText().trim();
                String cityVal    = city.getText().trim();
                String countryVal = country.getText().trim();
                String contactVal = contact.getText().trim();

                if (nameVal.isEmpty() || cityVal.isEmpty() || countryVal.isEmpty() || contactVal.isEmpty()) {
                    showError("Validation error", "Please fill all fields (name, city, country, contact).");
                    return null;
                }

                if (!isAlphaString(nameVal) || !isAlphaString(cityVal) || !isAlphaString(countryVal)) {
                    showError("Validation error", "Name/City/Country must contain only letters and spaces.");
                    return null;
                }

                try { PublisherDAO.update(p.getId(), nameVal, cityVal, countryVal, contactVal); }
                catch (Exception e) { e.printStackTrace(); showError("Error", e.getMessage()); }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(PublisherDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showDeletePublisherDialog(TableView<Publisher> table) {
        Publisher p = table.getSelectionModel().getSelectedItem();
        if (p == null) { showInfo("Info", "Select a publisher first."); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + p.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.setHeaderText(null);
        applyAlertTheme(c);

        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    PublisherDAO.delete(p.getId());
                    table.getItems().remove(p);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // =============================
    // ======= SALE DIALOGS ========
    // =============================
    public static void showAddSaleForm(TableView<Sale> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Sale");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField bookIdField = new TextField(); bookIdField.setPromptText("Book ID");
        TextField borrowerIdField = new TextField(); borrowerIdField.setPromptText("Borrower ID");
        TextField priceField = new TextField(); priceField.setPromptText("Sale Price");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        VBox form = new VBox(10,
                new Label("Book ID:"), bookIdField,
                new Label("Borrower ID:"), borrowerIdField,
                new Label("Price:"), priceField,
                new Label("Sale Date:"), datePicker
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                try {
                    String bookIdText = bookIdField.getText().trim();
                    String borrowerIdText = borrowerIdField.getText().trim();
                    String priceText = priceField.getText().trim();
                    LocalDate date = datePicker.getValue();

                    if (bookIdText.isEmpty() || borrowerIdText.isEmpty() || priceText.isEmpty() || date == null) {
                        showError("Validation error", "All fields are required.");
                        return null;
                    }

                    int bookId = Integer.parseInt(bookIdText);
                    int borrowerId = Integer.parseInt(borrowerIdText);
                    double price = Double.parseDouble(priceText);

                    SaleDAO.insert(bookId, borrowerId, price, date);

                } catch (NumberFormatException nfe) {
                    showError("Validation error", "Book ID / Borrower ID / Price must be valid numbers.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(SaleDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showUpdateSaleForm(TableView<Sale> table) {
        Sale s = table.getSelectionModel().getSelectedItem();
        if (s == null) { showInfo("Info", "Select a sale first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Sale");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        TextField bookIdField = new TextField(String.valueOf(s.getBookId()));
        TextField borrowerIdField = new TextField(String.valueOf(s.getBorrowerId()));
        TextField priceField = new TextField(String.valueOf(s.getSalePrice()));
        DatePicker datePicker = new DatePicker(s.getSaleDate());

        VBox form = new VBox(10,
                new Label("Book ID:"), bookIdField,
                new Label("Borrower ID:"), borrowerIdField,
                new Label("Price:"), priceField,
                new Label("Sale Date:"), datePicker
        );
        form.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(form);
        applyDialogTheme(dialog);

        dialog.setResultConverter(btn -> {
            if (btn == updateBtn) {
                try {
                    int bookId = Integer.parseInt(bookIdField.getText().trim());
                    int borrowerId = Integer.parseInt(borrowerIdField.getText().trim());
                    double price = Double.parseDouble(priceField.getText().trim());
                    LocalDate date = datePicker.getValue();

                    if (date == null) {
                        showError("Validation error", "Sale date is required.");
                        return null;
                    }

                    SaleDAO.update(s.getSaleId(), bookId, borrowerId, price, date);

                } catch (NumberFormatException nfe) {
                    showError("Validation error", "Book ID / Borrower ID / Price must be valid numbers.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(SaleDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showDeleteSaleDialog(TableView<Sale> table) {
        Sale s = table.getSelectionModel().getSelectedItem();
        if (s == null) { showInfo("Info", "Select a sale first."); return; }

        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete Sale ID " + s.getSaleId() + "?", ButtonType.OK, ButtonType.CANCEL);
        c.setTitle("Confirm Delete");
        c.setHeaderText(null);
        applyAlertTheme(c);

        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    SaleDAO.delete(s.getSaleId());
                    table.getItems().remove(s);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Error", ex.getMessage());
                }
            }
        });
    }

    // =============================
    // ======= AUTHOR DIALOGS ======
    // =============================
    public static void showAddAuthorForm(TableView<Author> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Author");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField firstName = new TextField(); firstName.setPromptText("First Name");
        TextField lastName = new TextField();  lastName.setPromptText("Last Name");
        TextField country = new TextField();   country.setPromptText("Country");
        TextField bio = new TextField();       bio.setPromptText("Bio");

        VBox v = new VBox(10,
                new Label("First Name:"), firstName,
                new Label("Last Name:"), lastName,
                new Label("Country:"), country,
                new Label("Bio:"), bio
        );
        v.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(v);
        applyDialogTheme(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == addBtn) {
                String fn = firstName.getText().trim();
                String ln = lastName.getText().trim();
                if (fn.isEmpty() || ln.isEmpty()) {
                    showError("Validation error", "First name and last name are required.");
                    return null;
                }
                try {
                    AuthorDAO.insert(fn, ln, country.getText().trim(), bio.getText().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(AuthorDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showUpdateAuthorForm(TableView<Author> table) {
        Author selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Info", "Select an author first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Author");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        TextField firstName = new TextField(selected.getFirstName());
        TextField lastName = new TextField(selected.getLastName());
        TextField country = new TextField(selected.getCountry());
        TextField bio = new TextField(selected.getBio());

        VBox v = new VBox(10,
                new Label("First Name:"), firstName,
                new Label("Last Name:"), lastName,
                new Label("Country:"), country,
                new Label("Bio:"), bio
        );
        v.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(v);
        applyDialogTheme(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == updateBtn) {
                String fn = firstName.getText().trim();
                String ln = lastName.getText().trim();
                if (fn.isEmpty() || ln.isEmpty()) {
                    showError("Validation error", "First name and last name are required.");
                    return null;
                }
                try {
                    AuthorDAO.update(selected.getAuthorId(), fn, ln, country.getText().trim(), bio.getText().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(AuthorDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showDeleteAuthorDialog(TableView<Author> table) {
        Author selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Info", "Select an author first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + selected.getFirstName() + " " + selected.getLastName() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        applyAlertTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    AuthorDAO.delete(selected.getAuthorId());
                    table.getItems().setAll(AuthorDAO.findAll());
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
        });
    }

    // =============================
    // ========= LOAN DIALOGS ======
    // =============================
    public static void showAddLoanForm(TableView<Loan> table) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Add Loan");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField bookIdField = new TextField(); bookIdField.setPromptText("Book ID");
        TextField borrowerIdField = new TextField(); borrowerIdField.setPromptText("Borrower ID");

        DatePicker loanDatePicker = new DatePicker(LocalDate.now());
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusWeeks(2));

        VBox v = new VBox(10,
                new Label("Book ID:"), bookIdField,
                new Label("Borrower ID:"), borrowerIdField,
                new Label("Loan Date:"), loanDatePicker,
                new Label("Due Date:"), dueDatePicker
        );
        v.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(v);
        applyDialogTheme(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == addBtn) {
                try {
                    String bookIdText = bookIdField.getText().trim();
                    String borrowerIdText = borrowerIdField.getText().trim();
                    LocalDate loanDate = loanDatePicker.getValue();
                    LocalDate dueDate = dueDatePicker.getValue();

                    if (bookIdText.isEmpty() || borrowerIdText.isEmpty() || loanDate == null || dueDate == null) {
                        showError("Validation error", "All fields are required.");
                        return null;
                    }

                    int bookId = Integer.parseInt(bookIdText);
                    int borrowerId = Integer.parseInt(borrowerIdText);

                    if (dueDate.isBefore(loanDate)) {
                        showError("Validation error", "Due date cannot be before loan date.");
                        return null;
                    }

                    LoanDAO.insert(borrowerId, bookId, loanDate, dueDate);

                } catch (NumberFormatException nfe) {
                    showError("Validation error", "Book ID and Borrower ID must be valid numbers.");
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(LoanDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showUpdateLoanForm(TableView<Loan> table) {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Info", "Select a loan first."); return; }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Update Loan");

        ButtonType updateBtn = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateBtn, ButtonType.CANCEL);

        DatePicker dueDatePicker = new DatePicker(selected.getDueDate());

        VBox v = new VBox(10, new Label("Due Date:"), dueDatePicker);
        v.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(v);
        applyDialogTheme(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == updateBtn) {
                try {
                    LocalDate newDueDate = dueDatePicker.getValue();
                    if (newDueDate == null) {
                        showError("Validation error", "Due date is required.");
                        return null;
                    }
                    if (selected.getLoanDate() != null && newDueDate.isBefore(selected.getLoanDate())) {
                        showError("Validation error", "Due date cannot be before loan date.");
                        return null;
                    }

                    LoanDAO.update(selected.getId(), selected.getBorrowerId(),
                            selected.getBookId(), selected.getLoanDate(), newDueDate);

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
        try { table.getItems().setAll(LoanDAO.findAll()); } catch (Exception ignored) {}
    }

    public static void showDeleteLoanDialog(TableView<Loan> table) {
        Loan selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showInfo("Info", "Select a loan first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete loan ID " + selected.getId() + "?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        applyAlertTheme(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    LoanDAO.delete(selected.getId());
                    table.getItems().setAll(LoanDAO.findAll());
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", e.getMessage());
                }
            }
        });
    }

    // =============================
    // ========= VALIDATION ========
    // =============================
    private static boolean isAlphaString(String value) {
        if (value == null) return false;
        value = value.trim();
        return value.matches("^[\\p{L} .'-]+$");
    }

    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        email = email.trim();
        if (email.isEmpty()) return false;
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // =============================
    // ========= STYLING ===========
    // =============================
    private static void applyDialogTheme(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();

        pane.setStyle("""
            -fx-background-color: %s;
            -fx-font-family: 'Poppins';
        """.formatted(MAIN_BG));

        Node content = pane.getContent();
        if (content != null) content.setStyle("""
            -fx-background-color: %s;
            -fx-background-radius: 16;
            -fx-padding: 14;
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-radius: 16;
        """.formatted(CARD_BG));

        if (content != null) {
            content.lookupAll(".text-field").forEach(n -> n.setStyle(inputStyle()));
            content.lookupAll(".password-field").forEach(n -> n.setStyle(inputStyle()));
            content.lookupAll(".date-picker").forEach(n -> n.setStyle(inputStyle()));
            content.lookupAll(".combo-box").forEach(n -> n.setStyle(inputStyle()));
            content.lookupAll(".label").forEach(n -> n.setStyle(labelStyle()));
            content.lookupAll(".check-box").forEach(n -> n.setStyle(labelStyle()));
        }

        pane.getButtonTypes().forEach(bt -> {
            Node btn = pane.lookupButton(bt);
            if (btn instanceof Button b) {
                if (bt.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    stylePrimaryButton(b);
                } else {
                    styleSecondaryButton(b);
                }
            }
        });
    }

    private static void applyAlertTheme(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle("""
            -fx-background-color: %s;
            -fx-font-family: 'Poppins';
        """.formatted(MAIN_BG));

        pane.lookupAll(".label").forEach(n -> n.setStyle(labelStyle()));
        pane.lookupAll(".content").forEach(n -> n.setStyle(labelStyle()));

        pane.getButtonTypes().forEach(bt -> {
            Node btn = pane.lookupButton(bt);
            if (btn instanceof Button b) stylePrimaryButton(b);
        });
    }

    private static String inputStyle() {
        return """
            -fx-background-radius: 12;
            -fx-background-color: rgba(255,255,255,0.95);
            -fx-border-color: rgba(165,61,56,0.20);
            -fx-border-radius: 12;
            -fx-prompt-text-fill: rgba(165,61,56,0.55);
            -fx-text-fill: #2b2b2b;
            -fx-font-family: 'Poppins';
            -fx-font-size: 13px;
            -fx-padding: 10 12;
        """;
    }

    private static String labelStyle() {
        return """
            -fx-text-fill: %s;
            -fx-font-family: 'Poppins';
            -fx-font-weight: 600;
        """.formatted(ACCENT);
    }

    private static void stylePrimaryButton(Button b) {
        b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT));

        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT_HOVER)));

        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT)));
    }

    private static void styleSecondaryButton(Button b) {
        b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.10);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT));

        b.setOnMouseEntered(e -> b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.16);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.30);
            -fx-border-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT)));

        b.setOnMouseExited(e -> b.setStyle("""
            -fx-background-color: rgba(165,61,56,0.10);
            -fx-text-fill: %s;
            -fx-font-weight: 900;
            -fx-background-radius: 14;
            -fx-border-color: rgba(165,61,56,0.25);
            -fx-border-radius: 14;
            -fx-cursor: hand;
            -fx-font-family: 'Poppins';
            -fx-padding: 10 16;
        """.formatted(ACCENT)));
    }
}
