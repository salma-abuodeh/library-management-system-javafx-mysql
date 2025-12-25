package org.example.project.dao;

import org.example.project.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    // Utility - run SELECT and return dynamic rows
    private static List<Map<String, Object>> run(String sql, Object... args) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);

            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int n = md.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= n; i++) row.put(md.getColumnLabel(i), rs.getObject(i));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    // 1. Total value of all books
    public static List<Map<String,Object>> totalValueOfAllBooks() throws SQLException {
        return run("SELECT SUM(original_price) AS total_value FROM book");
    }

    // 2. Books written by a selected author
    public static List<Map<String,Object>> booksByAuthor(int authorId) throws SQLException {
        String sql = """
            SELECT b.book_id, b.title
            FROM book b JOIN bookauthor ba ON b.book_id = ba.book_id
            WHERE ba.author_id = ?
            """;
        return run(sql, authorId);
    }

// 3. Books bought by a specific borrower
    public static List<Map<String, Object>> booksByBorrower(int borrowerId) throws SQLException {
        String sql = """
        SELECT
            b.title        AS title,
            s.sale_price   AS price,
            s.sale_date    AS date
        FROM sale s
        JOIN book b ON b.book_id = s.book_id
        WHERE s.borrower_id = ?
        ORDER BY s.sale_date DESC
        """;

        return run(sql, borrowerId);
    }



    // 4. Current loans and due dates
    public static List<Map<String,Object>> currentLoans() throws SQLException {
        return run("SELECT loan_id, borrower_id, book_id, due_date FROM loan WHERE return_date IS NULL");
    }

    // 5. Books published in a selected country
    public static List<Map<String,Object>> booksByPublisherCountry(String country) throws SQLException {
        String sql = """
            SELECT b.book_id, b.title, p.country
            FROM book b JOIN publisher p ON b.publisher_id = p.publisher_id
            WHERE p.country = ?
            """;
        return run(sql, country);
    }

    // 6. Borrowers who never borrowed OR bought a book
    public static List<Map<String,Object>> borrowersNeverBorrowed() throws SQLException {
        String sql = """
            SELECT bo.borrower_id, bo.first_name, bo.last_name
            FROM borrower bo
            LEFT JOIN loan l ON bo.borrower_id = l.borrower_id
            LEFT JOIN sale s ON bo.borrower_id = s.borrower_id
            WHERE l.borrower_id IS NULL AND s.borrower_id IS NULL
            """;
        return run(sql);
    }

    // 7. Books with more than one author
    public static List<Map<String,Object>> booksWithMultipleAuthors() throws SQLException {
        String sql = """
            SELECT b.book_id, b.title, COUNT(ba.author_id) AS authors_count
            FROM book b JOIN bookauthor ba ON b.book_id = ba.book_id
            GROUP BY b.book_id, b.title
            HAVING COUNT(ba.author_id) > 1
            """;
        return run(sql);
    }

    // 8. Books that were sold and their prices
    public static List<Map<String,Object>> soldBooksWithPrices() throws SQLException {
        String sql = """
            SELECT b.book_id, b.title, s.sale_price, s.sale_date
            FROM sale s JOIN book b ON s.book_id = b.book_id
            """;
        return run(sql);
    }

    // 9. Books currently available for borrowing
    public static List<Map<String,Object>> booksCurrentlyAvailable() throws SQLException {
        return run("SELECT book_id, title FROM book WHERE available = TRUE");
    }

    // 10. Loan history for a selected borrower
    public static List<Map<String,Object>> loanHistoryForBorrower(int borrowerId) throws SQLException {
        String sql = """
            SELECT loan_id, book_id, loan_date, due_date, return_date
            FROM loan
            WHERE borrower_id = ?
            ORDER BY loan_date DESC
            """;
        return run(sql, borrowerId);
    }

    // 11. Books borrowed within a date range
    public static List<Map<String,Object>> booksBorrowedBetween(String from, String to) throws SQLException {
        String sql = """
            SELECT loan_id, book_id, borrower_id, loan_date
            FROM loan
            WHERE loan_date BETWEEN ? AND ?
            """;
        return run(sql, from, to);
    }

    // 12. Books per category (bar chart)
    public static List<Map<String,Object>> booksPerCategory() throws SQLException {
        return run("SELECT category, COUNT(*) AS cnt FROM book GROUP BY category ORDER BY cnt DESC");
    }

    // 13. Availability summary (pie chart)
    public static List<Map<String,Object>> availabilitySummary() throws SQLException {
        String sql = """
            SELECT
              SUM(CASE WHEN available = TRUE THEN 1 ELSE 0 END) AS available_cnt,
              SUM(CASE WHEN available = FALSE THEN 1 ELSE 0 END) AS borrowed_cnt
            FROM book
            """;
        return run(sql);
    }

    // 14. Loans per month (line chart) - MySQL/MariaDB
    public static List<Map<String,Object>> loansPerMonth() throws SQLException {
        String sql = """
            SELECT DATE_FORMAT(loan_date, '%Y-%m') AS month_label, COUNT(*) AS cnt
            FROM loan
            GROUP BY DATE_FORMAT(loan_date, '%Y-%m')
            ORDER BY month_label
            """;
        return run(sql);
    }

    // 15. Sales revenue per month (bar chart) - MySQL/MariaDB
    public static List<Map<String,Object>> salesRevenuePerMonth() throws SQLException {
        String sql = """
            SELECT DATE_FORMAT(sale_date, '%Y-%m') AS month_label, SUM(sale_price) AS revenue
            FROM sale
            GROUP BY DATE_FORMAT(sale_date, '%Y-%m')
            ORDER BY month_label
            """;
        return run(sql);
    }

    // 16. Top borrowers by loans (bar chart)
    public static List<Map<String,Object>> topBorrowers(int limit) throws SQLException {
        String sql = """
            SELECT CONCAT(b.first_name, ' ', b.last_name) AS borrower, COUNT(*) AS cnt
            FROM loan l
            JOIN borrower b ON b.borrower_id = l.borrower_id
            GROUP BY borrower
            ORDER BY cnt DESC
            LIMIT ?
            """;
        return run(sql, limit);
    }
}
