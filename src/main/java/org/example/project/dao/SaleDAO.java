package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Sale;
import org.example.project.util.ValidationUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // 🔹 Get all sales
    public static List<Sale> findAll() throws SQLException {
        String sql = "SELECT sale_id, book_id, borrower_id, sale_price, sale_date FROM sale";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Sale> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    // 🔹 Insert a new sale

    public static int insert(int bookId, int borrowerId, double salePrice, LocalDate saleDate) throws SQLException {
        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requirePositive(borrowerId, "Borrower ID");
        ValidationUtil.requirePositive(salePrice, "Sale price");
        ValidationUtil.requireNotFuture(saleDate, "Sale date");

        String sql = "INSERT INTO sale (book_id, borrower_id, sale_price, sale_date) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ps.setDouble(3, salePrice);
            ps.setDate(4, Date.valueOf(saleDate));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int saleId, int bookId, int borrowerId, double salePrice, LocalDate saleDate) throws SQLException {
        ValidationUtil.requirePositive(saleId, "Sale ID");
        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requirePositive(borrowerId, "Borrower ID");
        ValidationUtil.requirePositive(salePrice, "Sale price");
        ValidationUtil.requireNotFuture(saleDate, "Sale date");

        String sql = "UPDATE sale SET book_id=?, borrower_id=?, sale_price=?, sale_date=? WHERE sale_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ps.setDouble(3, salePrice);
            ps.setDate(4, Date.valueOf(saleDate));
            ps.setInt(5, saleId);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updatePrice(int id, double newPrice) throws SQLException {
        ValidationUtil.requirePositive(id, "Sale ID");
        ValidationUtil.requirePositive(newPrice, "Sale price");

        String sql = "UPDATE sale SET sale_price = ? WHERE sale_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDouble(1, newPrice);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Delete a sale
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM sale WHERE sale_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }


    // 🔹 Search sales by any field
    public static List<Sale> search(String keyword) throws SQLException {
        String sql = """
        SELECT s.sale_id, s.book_id, s.borrower_id, s.sale_price, s.sale_date
        FROM sale s
        JOIN book bk ON s.book_id = bk.book_id
        JOIN author a ON bk.author_id = a.author_id
        JOIN borrower b ON s.borrower_id = b.borrower_id
        WHERE CAST(s.sale_id AS CHAR) LIKE ?
           OR CAST(s.book_id AS CHAR) LIKE ?
           OR CAST(s.borrower_id AS CHAR) LIKE ?
           OR CAST(s.sale_price AS CHAR) LIKE ?
           OR s.sale_date LIKE ?
           OR bk.title LIKE ?
           OR a.name LIKE ?
           OR b.name LIKE ?
        """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            for (int i = 1; i <= 8; i++) ps.setString(i, pattern);

            List<Sale> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

    // 🔹 Map row to Sale object
    private static Sale map(ResultSet rs) throws SQLException {
        return new Sale(
                rs.getInt("sale_id"),
                rs.getInt("book_id"),
                rs.getInt("borrower_id"),
                rs.getDate("sale_date").toLocalDate(), // SQL Date → LocalDate
                rs.getDouble("sale_price")              // BigDecimal → double
        );
    }


}
