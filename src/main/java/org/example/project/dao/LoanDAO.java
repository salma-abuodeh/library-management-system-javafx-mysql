package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Loan;
import org.example.project.util.ValidationUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    // 🔹 Get all loans
    public static List<Loan> findAll() throws SQLException {
        String sql = "SELECT loan_id, borrower_id, book_id, loan_date, due_date, return_date FROM loan";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Loan> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    // 🔹 Insert new loan
    public static int insert(int borrowerId, int bookId, LocalDate loanDate, LocalDate dueDate) throws SQLException {
        ValidationUtil.requirePositive(borrowerId, "Borrower ID");
        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requireOrder(loanDate, dueDate, "Loan date", "Due date");

        String sql = "INSERT INTO loan (borrower_id, book_id, loan_date, due_date) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, borrowerId);
            ps.setInt(2, bookId);
            ps.setDate(3, Date.valueOf(loanDate));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int loanId, int borrowerId, int bookId, LocalDate loanDate, LocalDate dueDate) throws SQLException {
        ValidationUtil.requirePositive(loanId, "Loan ID");
        ValidationUtil.requirePositive(borrowerId, "Borrower ID");
        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requireOrder(loanDate, dueDate, "Loan date", "Due date");

        String sql = "UPDATE loan SET borrower_id=?, book_id=?, loan_date=?, due_date=? WHERE loan_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, borrowerId);
            ps.setInt(2, bookId);
            ps.setDate(3, Date.valueOf(loanDate));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.setInt(5, loanId);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updateReturnDate(int loanId, LocalDate returnDate) throws SQLException {
        ValidationUtil.requirePositive(loanId, "Loan ID");
        ValidationUtil.requireNotFuture(returnDate, "Return date");

        String sql = "UPDATE loan SET return_date=? WHERE loan_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(returnDate));
            ps.setInt(2, loanId);
            return ps.executeUpdate() == 1;
        }
    }


    // 🔹 Delete loan
    public static boolean delete(int loanId) throws SQLException {
        String sql = "DELETE FROM loan WHERE loan_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, loanId);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Search loans
    public static List<Loan> search(String keyword) throws SQLException {
        String sql = """
        SELECT l.loan_id, l.borrower_id, l.book_id, l.loan_date, l.due_date, l.return_date
        FROM loan l
        JOIN borrower b ON l.borrower_id = b.borrower_id
        JOIN book bk ON l.book_id = bk.book_id
        JOIN author a ON bk.author_id = a.author_id
        WHERE CAST(l.loan_id AS CHAR) LIKE ?
           OR CAST(l.borrower_id AS CHAR) LIKE ?
           OR CAST(l.book_id AS CHAR) LIKE ?
           OR l.loan_date LIKE ?
           OR l.due_date LIKE ?
           OR (l.return_date IS NOT NULL AND l.return_date LIKE ?)
           OR b.name LIKE ?
           OR bk.title LIKE ?
           OR a.name LIKE ?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            for (int i = 1; i <= 9; i++) ps.setString(i, pattern);

            List<Loan> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }


    // 🔹 Helper: map ResultSet to Loan
    private static Loan map(ResultSet rs) throws SQLException {
        LocalDate loanDate = rs.getDate("loan_date") != null ? rs.getDate("loan_date").toLocalDate() : null;
        LocalDate dueDate = rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null;
        LocalDate returnDate = rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null;

        return new Loan(
                rs.getInt("loan_id"),
                rs.getInt("borrower_id"),
                rs.getInt("book_id"),
                loanDate,
                dueDate,
                returnDate
        );
    }
}
