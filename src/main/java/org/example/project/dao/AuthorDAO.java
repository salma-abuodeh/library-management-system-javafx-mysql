package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Author;
import org.example.project.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {

    // 🔹 Get all authors
    public static List<Author> findAll() throws SQLException {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT author_id, first_name, last_name, country, bio FROM author";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // 🔹 Search authors by name, country, or bio
    public static List<Author> search(String keyword) throws SQLException {
        String sql = """
            SELECT author_id, first_name, last_name, country, bio
            FROM author
            WHERE first_name LIKE ?
               OR last_name LIKE ?
               OR country LIKE ?
               OR bio LIKE ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, pattern);

            List<Author> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

    // 🔹 Insert a new author
    public static int insert(String firstName, String lastName, String country, String bio) throws SQLException {
        ValidationUtil.requireNonBlank(firstName, "First name");
        ValidationUtil.requireNonBlank(lastName, "Last name");
        ValidationUtil.requireMaxLength(firstName, 100, "First name");
        ValidationUtil.requireMaxLength(lastName, 100, "Last name");
        ValidationUtil.requireMaxLength(country, 100, "Country");
        ValidationUtil.requireMaxLength(bio, 1000, "Bio");

        String sql = "INSERT INTO author (first_name, last_name, country, bio) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setString(3, country != null ? country.trim() : null);
            ps.setString(4, bio != null ? bio.trim() : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int id, String firstName, String lastName, String country, String bio) throws SQLException {
        ValidationUtil.requirePositive(id, "Author ID");
        ValidationUtil.requireNonBlank(firstName, "First name");
        ValidationUtil.requireNonBlank(lastName, "Last name");
        ValidationUtil.requireMaxLength(firstName, 100, "First name");
        ValidationUtil.requireMaxLength(lastName, 100, "Last name");
        ValidationUtil.requireMaxLength(country, 100, "Country");
        ValidationUtil.requireMaxLength(bio, 1000, "Bio");

        String sql = "UPDATE author SET first_name = ?, last_name = ?, country = ?, bio = ? WHERE author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setString(3, country != null ? country.trim() : null);
            ps.setString(4, bio != null ? bio.trim() : null);
            ps.setInt(5, id);
            return ps.executeUpdate() == 1;
        }
    }
    // 🔹 Delete an author
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM author WHERE author_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Helper: Map result set to Author object
    private static Author map(ResultSet rs) throws SQLException {
        return new Author(
                rs.getInt("author_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("country"),
                rs.getString("bio")
        );
    }

    public static List<Author> searchFiltered(String name, String country, String bio) throws SQLException {
        String sql = """
        SELECT author_id, first_name, last_name, country, bio
        FROM author
        WHERE (? = '' OR CONCAT(first_name, ' ', last_name) LIKE ?)
          AND (? = '' OR country LIKE ?)
          AND (? = '' OR bio LIKE ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, "%" + name + "%");
            ps.setString(3, country);
            ps.setString(4, "%" + country + "%");
            ps.setString(5, bio);
            ps.setString(6, "%" + bio + "%");

            List<Author> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

}
