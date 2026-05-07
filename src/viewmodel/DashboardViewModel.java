package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Course;
import model.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardViewModel implements PropertyChangeListener {
    private ClientModel model;

    // Properties for UI binding
    private StringProperty welcomeMessage;
    private StringProperty userTags;
    private StringProperty searchText;
    private StringProperty newCourseName;
    private StringProperty newCourseInfo;
    private StringProperty newCourseTags;
    private StringProperty statusMessage;
    private StringProperty notificationsButtonText;
    private BooleanProperty canEditSelectedCourse;
    private BooleanProperty canDeleteSelectedCourse;
    private BooleanProperty canEnrollSelectedCourse;
    private ObservableList<Course> allCourses;
    private ObservableList<Course> courseList;
    private Set<Integer> registeredCourseIds;
    private Course selectedCourse;
    private Course courseForEnrollment;
    private Course lastEnrolledCourse;
    private Runnable onEnrollmentSuccess;

    public DashboardViewModel(ClientModel model) {
        this.model = model;
        this.welcomeMessage = new SimpleStringProperty("Welcome, " + model.getCurrentUser().getName());
        this.userTags = new SimpleStringProperty(model.getCurrentUser().getTags());
        this.searchText = new SimpleStringProperty("");
        this.newCourseName = new SimpleStringProperty("");
        this.newCourseInfo = new SimpleStringProperty("");
        this.newCourseTags = new SimpleStringProperty("");
        this.statusMessage = new SimpleStringProperty("");
        this.notificationsButtonText = new SimpleStringProperty("Notifications");
        this.canEditSelectedCourse = new SimpleBooleanProperty(false);
        this.canDeleteSelectedCourse = new SimpleBooleanProperty(false);
        this.canEnrollSelectedCourse = new SimpleBooleanProperty(false);
        this.allCourses = FXCollections.observableArrayList();
        this.courseList = FXCollections.observableArrayList();
        this.registeredCourseIds = new HashSet<>();

        // Search is local because the dashboard already has all loaded courses
        this.searchText.addListener((observable, oldValue, newValue) -> applyCourseFilter());

        this.model.addListener("CoursesRetrieved", this);
        this.model.addListener("CourseAdded", this);
        this.model.addListener("CourseDeleted", this);
        this.model.addListener("CourseUpdated", this);
        this.model.addListener("CourseEnrolled", this);
        this.model.addListener("RegisteredCoursesRetrieved", this);
        this.model.addListener("UserTagsUpdated", this);
        this.model.addListener("NewNotification", this);
        this.model.addListener("NotificationsRead", this);
        this.model.addListener("NotificationsCleared", this);
        
        // Fetch courses immediately when dashboard opens
        this.model.fetchCourses();
        this.model.fetchRegisteredCourses();
    }

    public void addCourse() {
        if (newCourseName.get().isEmpty() || newCourseInfo.get().isEmpty()) {
            statusMessage.set("Please fill in both course name and info");
            return;
        }
        
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can add courses as Tutors");
            return;
        }

        Student tutor = (Student) model.getCurrentUser();
        // Tags are optional, so students can still add a course without them
        Course newCourse = new Course(newCourseName.get(), newCourseInfo.get(), newCourseTags.get(), tutor);
        statusMessage.set("Adding course...");
        model.addCourse(newCourse);
    }

    public void deleteCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to delete");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can delete their own courses");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only delete courses you created");
            return;
        }

        statusMessage.set("Deleting course...");
        model.deleteCourse(course);
    }

    public void updateCourse(Course course, String newName, String newInformation, String newTags) {
        if (course == null) {
            statusMessage.set("Please select a course to edit");
            return;
        }
        if (newName.isEmpty() || newInformation.isEmpty()) {
            statusMessage.set("Please fill in both course name and info");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can edit their own courses");
            return;
        }
        if (course.getTutor() == null || !course.getTutor().getName().equals(model.getCurrentUser().getName())) {
            statusMessage.set("You can only edit courses you created");
            return;
        }

        // Keep the same course id so the database updates this exact course
        Course updatedCourse = new Course(newName, newInformation, newTags, course.getTutor());
        updatedCourse.setId(course.getId());
        statusMessage.set("Updating course...");
        model.updateCourse(updatedCourse);
    }

    public void saveUserTags() {
        statusMessage.set("Saving your tags...");
        model.updateCurrentUserTags(userTags.get());
    }
    
    public void refreshCourses() {
        statusMessage.set("Refreshing courses...");
        model.fetchCourses();
    }

    public StringProperty welcomeMessageProperty() { return welcomeMessage; }
    public StringProperty userTagsProperty() { return userTags; }
    public StringProperty searchTextProperty() { return searchText; }
    public StringProperty newCourseNameProperty() { return newCourseName; }
    public StringProperty newCourseInfoProperty() { return newCourseInfo; }
    public StringProperty newCourseTagsProperty() { return newCourseTags; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public StringProperty notificationsButtonTextProperty() { return notificationsButtonText; }
    public BooleanProperty canEditSelectedCourseProperty() { return canEditSelectedCourse; }
    public BooleanProperty canDeleteSelectedCourseProperty() { return canDeleteSelectedCourse; }
    public BooleanProperty canEnrollSelectedCourseProperty() { return canEnrollSelectedCourse; }
    public ObservableList<Course> getCourseList() { return courseList; }
    public ClientModel getModel() { return model; }
    public Course getLastEnrolledCourse() { return lastEnrolledCourse; }

    public void setOnEnrollmentSuccess(Runnable onEnrollmentSuccess) {
        this.onEnrollmentSuccess = onEnrollmentSuccess;
    }

    public void dispose() {
        model.removeListener("CoursesRetrieved", this);
        model.removeListener("CourseAdded", this);
        model.removeListener("CourseDeleted", this);
        model.removeListener("CourseUpdated", this);
        model.removeListener("CourseEnrolled", this);
        model.removeListener("RegisteredCoursesRetrieved", this);
        model.removeListener("UserTagsUpdated", this);
        model.removeListener("NewNotification", this);
        model.removeListener("NotificationsRead", this);
        model.removeListener("NotificationsCleared", this);
    }

    public void setSelectedCourse(Course selectedCourse) {
        this.selectedCourse = selectedCourse;
        updateButtonState();
    }

    public void enrollInCourse(Course course) {
        if (course == null) {
            statusMessage.set("Please select a course to enroll in");
            return;
        }
        if (!(model.getCurrentUser() instanceof Student)) {
            statusMessage.set("Only Students can enroll in courses");
            return;
        }
        statusMessage.set("Enrolling in " + course.getName() + "...");
        courseForEnrollment = course;
        model.enrollCourse(course);
    }

    private void applyCourseFilter() {
        List<Course> filteredCourses = new ArrayList<>();

        for (Course course : allCourses) {
            if (courseMatchesSearch(course)) {
                filteredCourses.add(course);
            }
        }

        // Matching courses still stay at the top after search is applied
        showCoursesWithMatchesFirst(filteredCourses);

        if (allCourses.isEmpty()) {
            statusMessage.set("No courses available");
        } else if (filteredCourses.isEmpty()) {
            statusMessage.set("No courses found");
        } else {
            statusMessage.set("Courses updated");
        }
    }

    private boolean courseMatchesSearch(Course course) {
        String search = searchText.get();
        if (search == null || search.trim().isEmpty()) {
            return true;
        }

        String lowerSearch = search.toLowerCase().trim();
        String courseName = course.getName();
        String courseInfo = course.getInformation();
        String courseTags = course.getTags();
        String tutorName = "";

        if (course.getTutor() != null) {
            tutorName = course.getTutor().getName();
        }

        if (courseName == null) courseName = "";
        if (courseInfo == null) courseInfo = "";
        if (courseTags == null) courseTags = "";
        if (tutorName == null) tutorName = "";

        // The search checks the course fields that students can see in the list
        String courseText = courseName + " " + courseInfo + " " + courseTags + " " + tutorName;
        return courseText.toLowerCase().contains(lowerSearch);
    }

    private void showCoursesWithMatchesFirst(List<Course> courses) {
        List<Course> matchingCourses = new ArrayList<>();
        List<Course> otherCourses = new ArrayList<>();

        for (Course course : courses) {
            boolean matches = courseMatchesUserTags(course);
            course.setMatchesUserTags(matches);

            if (matches) {
                matchingCourses.add(course);
            } else {
                otherCourses.add(course);
            }
        }

        courseList.clear();
        courseList.addAll(matchingCourses);
        courseList.addAll(otherCourses);
    }

    private boolean courseMatchesUserTags(Course course) {
        String studentTags = model.getCurrentUser().getTags();
        String courseTags = course.getTags();

        if (studentTags == null || studentTags.isEmpty() || courseTags == null || courseTags.isEmpty()) {
            return false;
        }

        String[] studentTagList = studentTags.toLowerCase().split(",");
        String[] courseTagList = courseTags.toLowerCase().split(",");

        // A course matches when at least one student tag is found in the course tags
        for (String studentTag : studentTagList) {
            String cleanStudentTag = studentTag.trim();

            for (String courseTag : courseTagList) {
                String cleanCourseTag = courseTag.trim();
                if (!cleanStudentTag.isEmpty() && cleanStudentTag.equals(cleanCourseTag)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            if ("CoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> courses = (List<Course>) evt.getNewValue();
                allCourses.clear();
                allCourses.addAll(courses);
                applyCourseFilter();
            } else if ("CourseAdded".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course added successfully!");
                    newCourseName.set("");
                    newCourseInfo.set("");
                    newCourseTags.set("");
                    model.fetchCourses(); // Refresh list automatically
                } else {
                    statusMessage.set("Failed to add course");
                }
            } else if ("CourseDeleted".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course deleted successfully!");
                    model.fetchCourses();
                } else {
                    statusMessage.set("Failed to delete course");
                }
            } else if ("CourseUpdated".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Course updated successfully!");
                    model.fetchCourses();
                } else {
                    statusMessage.set("Failed to update course");
                }
            } else if ("CourseEnrolled".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Successfully enrolled in the course!");
                    lastEnrolledCourse = courseForEnrollment;
                    if (lastEnrolledCourse != null) {
                        registeredCourseIds.add(lastEnrolledCourse.getId());
                    }
                    courseForEnrollment = null;
                    updateButtonState();
                    if (onEnrollmentSuccess != null) {
                        onEnrollmentSuccess.run();
                    }
                } else {
                    statusMessage.set("Failed to enroll. Maybe already enrolled?");
                    courseForEnrollment = null;
                    updateButtonState();
                }
            } else if ("RegisteredCoursesRetrieved".equals(evt.getPropertyName())) {
                List<Course> courses = (List<Course>) evt.getNewValue();
                registeredCourseIds.clear();
                for (Course course : courses) {
                    registeredCourseIds.add(course.getId());
                }
                updateButtonState();
            } else if ("UserTagsUpdated".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    statusMessage.set("Your tags were saved");
                    model.fetchCourses();
                } else {
                    statusMessage.set("Failed to save your tags");
                }
            } else if ("NewNotification".equals(evt.getPropertyName())) {
                model.Notification notif = (model.Notification) evt.getNewValue();
                // The newest notification is still shown here for quick feedback.
                statusMessage.set("Notification - " + notif.getTitle() + ": " + notif.getMessageInformation());
                updateNotificationsButtonText();
            } else if ("NotificationsRead".equals(evt.getPropertyName())) {
                updateNotificationsButtonText();
            } else if ("NotificationsCleared".equals(evt.getPropertyName())) {
                statusMessage.set("Notifications cleared");
                updateNotificationsButtonText();
            }
        });
    }

    private void updateNotificationsButtonText() {
        int count = model.getUnreadNotificationCount();
        if (count == 0) {
            notificationsButtonText.set("Notifications");
        } else {
            notificationsButtonText.set("Notifications (" + count + ")");
        }
    }

    private void updateButtonState() {
        boolean ownsSelectedCourse = currentUserOwnsCourse(selectedCourse);
        boolean canEnroll = selectedCourse != null
                && !ownsSelectedCourse
                && !registeredCourseIds.contains(selectedCourse.getId())
                && model.getCurrentUser() instanceof Student;

        // These properties only control the buttons. Server-side checks still protect the data.
        canEditSelectedCourse.set(ownsSelectedCourse);
        canDeleteSelectedCourse.set(ownsSelectedCourse);
        canEnrollSelectedCourse.set(canEnroll);
    }

    private boolean currentUserOwnsCourse(Course course) {
        if (course == null || course.getTutor() == null || model.getCurrentUser() == null) {
            return false;
        }
        return course.getTutor().getName().equals(model.getCurrentUser().getName());
    }
}
