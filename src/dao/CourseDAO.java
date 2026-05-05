package dao;

import model.Course;
import model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

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

    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (name, information, tutor_id) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int tutorId = getUserIdByName(course.getTutor().getName());
            if (tutorId == -1) return false;

            pstmt.setString(1, course.getName());
            pstmt.setString(2, course.getInformation());
            pstmt.setInt(3, tutorId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding course.");
            e.printStackTrace();
            return false;
        }
    }

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.information, u.name AS tutor_name " +
                     "FROM courses c JOIN users u ON c.tutor_id = u.id";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student dummyTutor = new Student(rs.getString("tutor_name"), "");
                Course course = new Course(rs.getString("name"), rs.getString("information"), dummyTutor);
                courses.add(course);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching courses.");
            e.printStackTrace();
        }
        return courses;
    }

    public boolean enrollStudent(Student student, Course course) {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int studentId = getUserIdByName(student.getName());
            int courseId = getCourseId(course.getName(), course.getTutor().getName());
            
            if (studentId == -1 || courseId == -1) return false;

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Error enrolling student.");
            e.printStackTrace();
            return false;
        }
    }

    private int getCourseId(String courseName, String tutorName) throws SQLException {
        String sql = "SELECT c.id FROM courses c JOIN users u ON c.tutor_id = u.id " +
                     "WHERE c.name = ? AND u.name = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseName);
            pstmt.setString(2, tutorName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
}
