package org.example.project.dao;

import org.example.project.db.DatabaseConnection;
import org.example.project.model.User;
import org.example.project.security.Passwords;
import org.example.project.util.ValidationUtil;

import java.sql.*;

public class UserDAO {

    public static boolean signup(String username, String email, String plainPassword, String role) throws SQLException {
        ValidationUtil.requireNonBlank(username, "Username");
        ValidationUtil.requireValidEmail(email, "email");
        ValidationUtil.requireNonBlank(plainPassword, "Password");
        ValidationUtil.requireNonBlank(role, "Role");

        String salt = Passwords.randomSaltHex();
        String hash = Passwords.hashWithSalt(plainPassword, salt);

        String sql = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setString(2, email.trim());
            ps.setString(3, hash);
            ps.setString(4, salt);
            ps.setString(5, role.toLowerCase());

            return ps.executeUpdate() == 1;
        }
    }

    public static User login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    // Get stored salt and hash
                    String salt = rs.getString("salt");
                    String storedHash = rs.getString("password_hash");

                    // Calculate hash from entered plain password + stored salt
                    String calculatedHash = Passwords.hashWithSalt(plainPassword, salt);

                    if (calculatedHash.equals(storedHash)) {
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("email"),
                                rs.getString("role")
                        );
                    }
                }
            }
        }
        return null;
    }


    public static boolean exists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
