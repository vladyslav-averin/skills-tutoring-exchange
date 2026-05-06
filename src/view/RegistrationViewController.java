package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.Course;
import viewmodel.RegistrationViewModel;

public class RegistrationViewController {

    @FXML private ListView<Course> registeredCourseListView;
    @FXML private Label statusLabel;

    private RegistrationViewModel viewModel;

    public void init(RegistrationViewModel viewModel) {
        this.viewModel = viewModel;

        registeredCourseListView.setItems(viewModel.getRegisteredCourses());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
    }

    @FXML
    public void onCancelRegistrationButton() {
        Course selectedCourse = registeredCourseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.cancelRegistration(null);
            return;
        }

        // The student should confirm before the registration is removed.
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Cancel");
        confirmation.setHeaderText("Cancel this registration?");
        confirmation.setContentText(getCourseName(selectedCourse));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.cancelRegistration(selectedCourse);
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshRegisteredCourses();
    }

    private String getCourseName(Course course) {
        if (course == null || course.getName() == null) {
            return "";
        }
        return course.getName();
    }
}
