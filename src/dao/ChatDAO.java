package dao;

import model.Message;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private int getUserIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, text, timestamp) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int senderId = getUserIdByName(message.getSender().getName());
            if (senderId == -1) return false;

            pstmt.setInt(1, senderId);
            pstmt.setString(2, message.getText());
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getTimeStamp()));
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error saving message.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getChatHistory() {
        List<Message> history = new ArrayList<>();
        String sql = "SELECT m.text, m.timestamp, u.name AS sender_name " +
                     "FROM messages m JOIN users u ON m.sender_id = u.id " +
                     "ORDER BY m.timestamp ASC";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student dummySender = new Student(rs.getString("sender_name"), "");
                Message msg = new Message(dummySender, rs.getString("text"));
                msg.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());
                history.add(msg);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching chat history.");
            e.printStackTrace();
        }
        return history;
    }
}
