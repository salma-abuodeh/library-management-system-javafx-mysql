package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Publisher;
import org.example.project.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublisherDAO {

    // 🔹 Get all publishers
    public static List<Publisher> findAll() throws SQLException {
        String sql = "SELECT publisher_id, name, city, country, contact_info FROM publisher";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Publisher> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }


    public static int insert(String name, String city, String country, String contact) throws SQLException {
        ValidationUtil.requireNonBlank(name, "Publisher name");
        ValidationUtil.requireNonBlank(country, "Country");
        ValidationUtil.requireMaxLength(name, 200, "Publisher name");
        ValidationUtil.requireMaxLength(city, 100, "City");
        ValidationUtil.requireMaxLength(country, 100, "Country");
        ValidationUtil.requireMaxLength(contact, 255, "Contact info");

        String sql = "INSERT INTO publisher (name, city, country, contact_info) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.setString(2, city != null ? city.trim() : null);
            ps.setString(3, country.trim());
            ps.setString(4, contact != null ? contact.trim() : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int id, String name, String city, String country, String contact) throws SQLException {
        ValidationUtil.requirePositive(id, "Publisher ID");
        ValidationUtil.requireNonBlank(name, "Publisher name");
        ValidationUtil.requireNonBlank(country, "Country");
        ValidationUtil.requireMaxLength(name, 200, "Publisher name");
        ValidationUtil.requireMaxLength(city, 100, "City");
        ValidationUtil.requireMaxLength(country, 100, "Country");
        ValidationUtil.requireMaxLength(contact, 255, "Contact info");

        String sql = "UPDATE publisher SET name=?, city=?, country=?, contact_info=? WHERE publisher_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name.trim());
            ps.setString(2, city != null ? city.trim() : null);
            ps.setString(3, country.trim());
            ps.setString(4, contact != null ? contact.trim() : null);
            ps.setInt(5, id);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Update publisher city
    public static boolean updateCity(int id, String newCity) throws SQLException {
        String sql = "UPDATE publisher SET city = ? WHERE publisher_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newCity);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Delete publisher
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM publisher WHERE publisher_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // 🔹 Search publishers by keyword
    public static List<Publisher> search(String keyword) throws SQLException {
        String sql = """
            SELECT publisher_id, name, city, country, contact_info
            FROM publisher
            WHERE name LIKE ?
               OR city LIKE ?
               OR country LIKE ?
               OR contact_info LIKE ?
            """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                List<Publisher> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    // 🔹 Map row to Publisher object
    private static Publisher map(ResultSet rs) throws SQLException {
        return new Publisher(
                rs.getInt("publisher_id"),
                rs.getString("name"),
                rs.getString("city"),
                rs.getString("country"),
                rs.getString("contact_info")
        );
    }

}
