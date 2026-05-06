package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Course;
import model.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class AdminDashboardViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<User> users;
    private ObservableList<Course> courses;
    private StringProperty welcomeMessage;
    private StringProperty statusMessage;

    public AdminDashboardViewModel(ClientModel model) {
        this.model = model;
        this.users = FXCollections.observableArrayList();
        this.courses = FXCollections.observableArrayList();
        this.welcomeMessage = new SimpleStringProperty("Admin Dashboard - " + model.getCurrentUser().getName());
        this.statusMessage = new SimpleStringProperty("");

        this.model.addListener("UsersRetrieved", this);
        this.model.addListener("CoursesRetrieved", this);
        this.model.addListener("AdminUserDeleted", this);
        this.model.addListener("AdminCourseDeleted", this);

        refreshData();
    }

    public void refreshData() {
        statusMessage.set("Loading admin data...");
        model.fetchUsers();
        model.fetchCourses();
    }

    public void deleteUser(User user) {
        if (user == null) {
            statusMessage.set("Please select a user to delete.");
            return;
        }
        if (user.getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You cannot delete your own admin account.");
            return;
        }

        statusMessage.set("Deleting user...");
        model.adminDeleteUser(user);
    }

    public void deleteCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to delete.");
            return;
        }

        statusMessage.set("Deleting course...");
        model.adminDeleteCourse(course);
    }

    public ObservableList<User> getUsers() {
        return users;
    }

    public ObservableList<Course> getCourses() {
        return courses;
    }

    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("UsersRetrieved".equals(evt.getPropertyName())) {
                List<User> receivedUsers = (List<User>) evt.getNewValue();
                users.clear();
                users.addAll(receivedUsers);
                statusMessage.set("Admin data loaded.");
            } else if ("CoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> receivedCourses = (List<Course>) evt.getNewValue();
                courses.clear();
                courses.addAll(receivedCourses);
                statusMessage.set("Admin data loaded.");
            } else if ("AdminUserDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("User deleted successfully.");
                    refreshData();
                } else {
                    statusMessage.set("Failed to delete user.");
                }
            } else if ("AdminCourseDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course deleted successfully.");
                    refreshData();
                } else {
                    statusMessage.set("Failed to delete course.");
                }
            }
        });
    }
}
