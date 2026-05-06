package view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import viewmodel.ChatViewModel;
import viewmodel.DashboardViewModel;
import java.io.IOException;

public class MainDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<model.Course> courseListView;
    @FXML private TextField courseNameField;
    @FXML private TextField courseInfoField;
    @FXML private Label statusLabel;

    private DashboardViewModel viewModel;

    public void init(DashboardViewModel viewModel) {
        this.viewModel = viewModel;

        // Bindings
        welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());
        courseNameField.textProperty().bindBidirectional(viewModel.newCourseNameProperty());
        courseInfoField.textProperty().bindBidirectional(viewModel.newCourseInfoProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        
        courseListView.setItems(viewModel.getCourseList());
    }

    @FXML
    public void onAddCourseButton() {
        viewModel.addCourse();
    }

    @FXML
    public void onEnrollButton() {
        model.Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.enrollInCourse(null);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Registration");
        confirmation.setHeaderText("Register for this course?");
        confirmation.setContentText(selectedCourse.toString());

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.enrollInCourse(selectedCourse);
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshCourses();
    }

    @FXML
    public void onOpenChatButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatView.fxml"));
            Parent root = loader.load();
            
            ChatViewModel chatViewModel = new ChatViewModel(viewModel.getModel());
            ChatViewController controller = loader.getController();
            controller.init(chatViewModel);
            
            Stage chatStage = new Stage();
            chatStage.setTitle("Global Chat Room");
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
