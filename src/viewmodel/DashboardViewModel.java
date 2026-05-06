package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Course;
import model.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class DashboardViewModel implements PropertyChangeListener {
    private ClientModel model;

    // Properties for UI binding
    private StringProperty welcomeMessage;
    private StringProperty newCourseName;
    private StringProperty newCourseInfo;
    private StringProperty statusMessage;
    private ObservableList<Course> courseList;
    private Course courseForEnrollment;
    private Course lastEnrolledCourse;
    private Runnable onEnrollmentSuccess;

    public DashboardViewModel(ClientModel model) {
        this.model = model;
        this.welcomeMessage = new SimpleStringProperty("Welcome, " + model.getCurrentUser().getName());
        this.newCourseName = new SimpleStringProperty("");
        this.newCourseInfo = new SimpleStringProperty("");
        this.statusMessage = new SimpleStringProperty("");
        this.courseList = FXCollections.observableArrayList();

        this.model.addListener("CoursesRetrieved", this);
        this.model.addListener("CourseAdded", this);
        this.model.addListener("CourseDeleted", this);
        this.model.addListener("CourseUpdated", this);
        this.model.addListener("CourseEnrolled", this);
        this.model.addListener("NewNotification", this);
        
        // Fetch courses immediately when dashboard opens
        this.model.fetchCourses();
    }

    public void addCourse() {
        if (newCourseName.get().isEmpty() || newCourseInfo.get().isEmpty()) {
            statusMessage.set("Please fill in both course name and info.");
            return;
        }
        
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can add courses as Tutors.");
            return;
        }

        Student tutor = (Student) model.getCurrentUser();
        Course newCourse = new Course(newCourseName.get(), newCourseInfo.get(), tutor);
        statusMessage.set("Adding course...");
        model.addCourse(newCourse);
    }

    public void deleteCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to delete.");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can delete their own courses.");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only delete courses you created.");
            return;
        }

        statusMessage.set("Deleting course...");
        model.deleteCourse(course);
    }

    public void updateCourse(Course course, String newName, String newInformation) {
        if (course == null) {
            statusMessage.set("Please select a course to edit.");
            return;
        }
        if (newName.isEmpty() || newInformation.isEmpty()) {
            statusMessage.set("Please fill in both course name and info.");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can edit their own courses.");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only edit courses you created.");
            return;
        }

        Course updatedCourse = new Course(newName, newInformation, course.getTutor());
        updatedCourse.setId(course.getId());
        statusMessage.set("Updating course...");
        model.updateCourse(updatedCourse);
    }
    
    public void refreshCourses() {
        statusMessage.set("Refreshing courses...");
        model.fetchCourses();
    }

    public StringProperty welcomeMessageProperty() { return welcomeMessage; }
    public StringProperty newCourseNameProperty() { return newCourseName; }
    public StringProperty newCourseInfoProperty() { return newCourseInfo; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public ObservableList<Course> getCourseList() { return courseList; }
    public ClientModel getModel() { return model; }
    public Course getLastEnrolledCourse() { return lastEnrolledCourse; }

    public void setOnEnrollmentSuccess(Runnable onEnrollmentSuccess) {
        this.onEnrollmentSuccess = onEnrollmentSuccess;
    }

    public void enrollInCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to enroll in.");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can enroll in courses.");
            return;
        }
        statusMessage.set("Enrolling in " + course.getName() + "...");
        courseForEnrollment = course;
        model.enrollCourse(course);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            if ("CoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> courses = (List<Course>) evt.getNewValue();
                courseList.clear();
                courseList.addAll(courses);
                statusMessage.set("Courses updated.");
            } else if ("CourseAdded".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course added successfully!");
                    newCourseName.set("");
                    newCourseInfo.set("");
                    model.fetchCourses(); // Refresh list automatically
                } else {
                    statusMessage.set("Failed to add course.");
                }
            } else if ("CourseDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course deleted successfully!");
                    model.fetchCourses();
                } else {
                    statusMessage.set("Failed to delete course.");
                }
            } else if ("CourseUpdated".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course updated successfully!");
                    model.fetchCourses();
                } else {
                    statusMessage.set("Failed to update course.");
                }
            } else if ("CourseEnrolled".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Successfully enrolled in the course!");
                    lastEnrolledCourse = courseForEnrollment;
                    courseForEnrollment = null;
                    if (onEnrollmentSuccess != null) {
                        onEnrollmentSuccess.run();
                    }
                } else {
                    statusMessage.set("Failed to enroll. Maybe already enrolled?");
                    courseForEnrollment = null;
                }
            } else if ("NewNotification".equals(evt.getPropertyName())) {
                model.Notification notif = (model.Notification) evt.getNewValue();
                // We could pop up an alert or add to a notification list
                statusMessage.set("🔔 " + notif.getTitle() + ": " + notif.getMessageInformation());
            }
        });
    }
}
