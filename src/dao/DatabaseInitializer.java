package dao;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Cannot initialize database: Connection is null.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            
            // 1. Create Users Table
            // Storing both Students and Administrators. We use 'user_type' to distinguish them.
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_type VARCHAR(20) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL" +
                    ")";
            stmt.execute(createUsersTable);
            System.out.println("Table 'users' verified/created.");

            // 2. Create Courses Table
            // 1-to-many relationship: A tutor (student) provides many courses
            String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "information TEXT, " +
                    "tutor_id INT, " +
                    "FOREIGN KEY (tutor_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createCoursesTable);
            System.out.println("Table 'courses' verified/created.");

            // 3. Create Enrollments Table (Many-to-Many relationship between Student and Course)
            String createEnrollmentsTable = "CREATE TABLE IF NOT EXISTS enrollments (" +
                    "student_id INT, " +
                    "course_id INT, " +
                    "PRIMARY KEY (student_id, course_id), " +
                    "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createEnrollmentsTable);
            System.out.println("Table 'enrollments' verified/created.");

            // 4. Create Messages Table
            String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id SERIAL PRIMARY KEY, " +
                    "sender_id INT, " +
                    "receiver_id INT, " +
                    "text TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(createMessagesTable);
            System.out.println("Table 'messages' verified/created.");

            String addReceiverColumn = "ALTER TABLE messages " +
                    "ADD COLUMN IF NOT EXISTS receiver_id INT REFERENCES users(id) ON DELETE CASCADE";
            stmt.execute(addReceiverColumn);

            System.out.println("Database initialization completed successfully.");

        } catch (Exception e) {
            System.err.println("Error initializing database tables.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Run this main method to create/verify tables
        initializeDatabase();
    }
}
