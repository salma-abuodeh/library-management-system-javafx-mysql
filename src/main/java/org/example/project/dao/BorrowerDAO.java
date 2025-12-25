package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.Borrower;
import org.example.project.util.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    // ---- READ ALL ----
    public static List<Borrower> findAll() throws SQLException {
        String sql = "SELECT borrower_id, first_name, last_name, type_id, contact_info FROM borrower";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Borrower> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;
        }
    }

    // ---- SEARCH ----
    public static List<Borrower> search(String keyword) throws SQLException {
        String sql = """
                SELECT borrower_id, first_name, last_name, type_id, contact_info
                FROM borrower
                WHERE first_name LIKE ? OR last_name LIKE ? OR contact_info LIKE ?
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);

            try (ResultSet rs = ps.executeQuery()) {
                List<Borrower> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    // BorrowerDAO
    public static List<Borrower> searchFiltered(String name, String contact) throws SQLException {
        String sql = """
        SELECT borrower_id, first_name, last_name, type_id, contact_info
        FROM borrower
        WHERE (? = '' OR CONCAT(first_name, ' ', last_name) LIKE ?)
          AND (? = '' OR contact_info LIKE ?)
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, "%" + name + "%");
            ps.setString(3, contact);
            ps.setString(4, "%" + contact + "%");

            List<Borrower> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }


    public static int insert(String firstName, String lastName, int typeId, String contact) throws SQLException {
        ValidationUtil.requireNonBlank(firstName, "First name");
        ValidationUtil.requireNonBlank(lastName, "Last name");
        ValidationUtil.requirePositive(typeId, "Borrower type");
        ValidationUtil.requireMaxLength(firstName, 100, "First name");
        ValidationUtil.requireMaxLength(lastName, 100, "Last name");
        ValidationUtil.requireMaxLength(contact, 255, "Contact info");

        String sql = "INSERT INTO borrower (first_name, last_name, type_id, contact_info) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setInt(3, typeId);
            ps.setString(4, contact != null ? contact.trim() : null);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    public static boolean update(int id, String firstName, String lastName, int typeId, String contact) throws SQLException {
        ValidationUtil.requirePositive(id, "Borrower ID");
        ValidationUtil.requireNonBlank(firstName, "First name");
        ValidationUtil.requireNonBlank(lastName, "Last name");
        ValidationUtil.requirePositive(typeId, "Borrower type");
        ValidationUtil.requireMaxLength(firstName, 100, "First name");
        ValidationUtil.requireMaxLength(lastName, 100, "Last name");
        ValidationUtil.requireMaxLength(contact, 255, "Contact info");

        String sql = "UPDATE borrower SET first_name=?, last_name=?, type_id=?, contact_info=? WHERE borrower_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setInt(3, typeId);
            ps.setString(4, contact != null ? contact.trim() : null);
            ps.setInt(5, id);
            return ps.executeUpdate() == 1;
        }
    }

    public static boolean updateContact(int id, String newContact) throws SQLException {
        ValidationUtil.requirePositive(id, "Borrower ID");
        ValidationUtil.requireMaxLength(newContact, 255, "Contact info");

        String sql = "UPDATE borrower SET contact_info=? WHERE borrower_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, newContact != null ? newContact.trim() : null);
            ps.setInt(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ---- DELETE ----
    public static boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM borrower WHERE borrower_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    // ---- HELPER ----
    private static Borrower map(ResultSet rs) throws SQLException {
        return new Borrower(
                rs.getInt("borrower_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getInt("type_id"),
                rs.getString("contact_info")
        );
    }

}
