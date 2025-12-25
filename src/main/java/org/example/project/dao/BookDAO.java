package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Book;
import org.example.project.util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    public static List<Book> findAll() throws SQLException {
        String sql = "SELECT book_id, title, publisher_id, category, book_type, original_price, available FROM book";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Book> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }
    // BookDAO
    public static List<Book> searchFiltered(String title, String category, String type) throws SQLException {
        String sql = """
        SELECT * FROM book
        WHERE (? = '' OR title LIKE ?)
          AND (? = '' OR category LIKE ?)
          AND (? = '' OR book_type LIKE ?)
    """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, title); ps.setString(2, "%" + title + "%");
            ps.setString(3, category); ps.setString(4, "%" + category + "%");
            ps.setString(5, type); ps.setString(6, "%" + type + "%");

            List<Book> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }


    public static List<Book> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM book WHERE title LIKE ? OR category LIKE ? OR book_type LIKE ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public static int insert(String title, Integer publisherId, String category, String bookType,
                             BigDecimal price, boolean available) throws SQLException {

        ValidationUtil.requireNonBlank(title, "Title");
        ValidationUtil.requireNonBlank(category, "Category");
        ValidationUtil.requireNonBlank(bookType, "Book type");
        ValidationUtil.requireMaxLength(title, 200, "Title");
        ValidationUtil.requireMaxLength(category, 100, "Category");
        ValidationUtil.requireMaxLength(bookType, 50, "Book type");
        ValidationUtil.requireNonNegative(price, "Original price");
        if (publisherId != null) {
            ValidationUtil.requirePositive(publisherId, "Publisher ID");
        }

        String sql = "INSERT INTO book (title, publisher_id, category, book_type, original_price, available) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title.trim());
            if (publisherId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, publisherId);
            ps.setString(3, category.trim());
            ps.setString(4, bookType.trim());
            ps.setBigDecimal(5, price);
            ps.setBoolean(6, available);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int bookId, String title, Integer publisherId, String category,
                                 String bookType, BigDecimal price, boolean available) throws SQLException {

        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requireNonBlank(title, "Title");
        ValidationUtil.requireNonBlank(category, "Category");
        ValidationUtil.requireNonBlank(bookType, "Book type");
        ValidationUtil.requireMaxLength(title, 200, "Title");
        ValidationUtil.requireMaxLength(category, 100, "Category");
        ValidationUtil.requireMaxLength(bookType, 50, "Book type");
        ValidationUtil.requireNonNegative(price, "Original price");
        if (publisherId != null) {
            ValidationUtil.requirePositive(publisherId, "Publisher ID");
        }

        String sql = "UPDATE book SET title=?, publisher_id=?, category=?, book_type=?, original_price=?, available=? WHERE book_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, title.trim());
            if (publisherId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, publisherId);
            ps.setString(3, category.trim());
            ps.setString(4, bookType.trim());
            ps.setBigDecimal(5, price);
            ps.setBoolean(6, available);
            ps.setInt(7, bookId);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updatePrice(int bookId, BigDecimal newPrice) throws SQLException {
        ValidationUtil.requirePositive(bookId, "Book ID");
        ValidationUtil.requireNonNegative(newPrice, "Price");

        String sql = "UPDATE book SET original_price=? WHERE book_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, newPrice);
            ps.setInt(2, bookId);
            return ps.executeUpdate() == 1;
        }
    }


    public static boolean delete(int bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE book_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1;
        }
    }

    private static Book map(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                (Integer) rs.getObject("publisher_id"),
                rs.getString("category"),
                rs.getString("book_type"),
                rs.getBigDecimal("original_price"),
                rs.getBoolean("available")
        );
    }


}