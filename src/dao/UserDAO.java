package dao;

import model.Administrator;
import model.Student;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Method to create/register a new user
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (user_type, name, password) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Determine user type
            String userType = user instanceof Administrator ? "Administrator" : "Student";
            
            pstmt.setString(1, userType);
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error inserting user.");
            e.printStackTrace();
            return false;
        }
    }

    // Method to authenticate a user
    public User authenticateUser(String name, String password) {
        String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String userType = rs.getString("user_type");
                    if ("Administrator".equals(userType)) {
                        return new Administrator(rs.getString("name"), rs.getString("password"));
                    } else {
                        return new Student(rs.getString("name"), rs.getString("password"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user.");
            e.printStackTrace();
        }
        return null; // Return null if authentication fails
    }

    // Method to delete a user by name (useful for Admin)
    public boolean deleteUser(String name) {
        String sql = "DELETE FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user.");
            e.printStackTrace();
            return false;
        }
    }
}
