package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import viewmodel.ChatHistoryViewModel;
import viewmodel.ChatViewModel;
import viewmodel.DashboardViewModel;
import viewmodel.RegistrationViewModel;
import java.io.IOException;
import java.util.Optional;

public class MainDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ListView<model.Course> courseListView;
    @FXML private TextField userTagsField;
    @FXML private TextField searchField;
    @FXML private TextField courseNameField;
    @FXML private TextArea courseInfoField;
    @FXML private TextField courseTagsField;
    @FXML private Button deleteCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button enrollCourseButton;
    @FXML private Label statusLabel;

    private DashboardViewModel viewModel;

    public void init(DashboardViewModel viewModel) {
        this.viewModel = viewModel;

        // Bindings
        welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());
        userTagsField.textProperty().bindBidirectional(viewModel.userTagsProperty());
        searchField.textProperty().bindBidirectional(viewModel.searchTextProperty());
        courseNameField.textProperty().bindBidirectional(viewModel.newCourseNameProperty());
        courseInfoField.textProperty().bindBidirectional(viewModel.newCourseInfoProperty());
        courseTagsField.textProperty().bindBidirectional(viewModel.newCourseTagsProperty());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        
        courseListView.setItems(viewModel.getCourseList());
        courseListView.setCellFactory(listView -> createCourseCell());
        courseListView.getSelectionModel().selectedItemProperty().addListener((observable, oldCourse, newCourse) -> {
            viewModel.setSelectedCourse(newCourse);
        });
        deleteCourseButton.disableProperty().bind(viewModel.canDeleteSelectedCourseProperty().not());
        editCourseButton.disableProperty().bind(viewModel.canEditSelectedCourseProperty().not());
        enrollCourseButton.disableProperty().bind(viewModel.canEnrollSelectedCourseProperty().not());
        viewModel.setOnEnrollmentSuccess(() -> openTutorChat(viewModel.getLastEnrolledCourse()));
    }

    @FXML
    public void onAddCourseButton() {
        viewModel.addCourse();
    }

    @FXML
    public void onSaveTagsButton() {
        viewModel.saveUserTags();
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
        confirmation.setContentText(getCourseName(selectedCourse));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.enrollInCourse(selectedCourse);
    }

    @FXML
    public void onDeleteCourseButton() {
        model.Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.deleteCourse(null);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete this course?");
        confirmation.setContentText(getCourseName(selectedCourse));

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        viewModel.deleteCourse(selectedCourse);
    }

    @FXML
    public void onEditCourseButton() {
        model.Course selectedCourse = courseListView.getSelectionModel().getSelectedItem();
        if (selectedCourse == null) {
            viewModel.updateCourse(null, "", "", "");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit your course");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefWidth(460);

        TextField nameField = new TextField(selectedCourse.getName());
        TextField tagsField = new TextField(selectedCourse.getTags());
        TextArea infoField = new TextArea(selectedCourse.getInformation());
        infoField.setWrapText(true);
        infoField.setPrefRowCount(4);

        VBox content = new VBox(10);
        // Tags stay in the same edit dialog as the other course details
        content.getChildren().addAll(new Label("Course name"), nameField, new Label("Course tags"), tagsField,
                new Label("Course info"), infoField);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            viewModel.updateCourse(selectedCourse, nameField.getText(), infoField.getText(), tagsField.getText());
        }
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshCourses();
    }

    @FXML
    public void onOpenChatButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatHistoryView.fxml"));
            Parent root = loader.load();
            
            ChatHistoryViewModel chatHistoryViewModel = new ChatHistoryViewModel(viewModel.getModel());
            ChatHistoryController controller = loader.getController();
            controller.init(chatHistoryViewModel);
            
            Stage chatHistoryStage = new Stage();
            chatHistoryStage.setTitle("Chat History");
            chatHistoryStage.setScene(new Scene(root));
            chatHistoryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onOpenRegistrationsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RegistrationView.fxml"));
            Parent root = loader.load();

            RegistrationViewModel registrationViewModel = new RegistrationViewModel(viewModel.getModel());
            RegistrationViewController controller = loader.getController();
            controller.init(registrationViewModel);

            Stage registrationStage = new Stage();
            registrationStage.setTitle("My Registrations");
            registrationStage.setScene(new Scene(root));
            registrationStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openTutorChat(model.Course course) {
        if (course == null || course.getTutor() == null) {
            return;
        }

        String tutorName = course.getTutor().getName();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatView.fxml"));
            Parent root = loader.load();

            ChatViewModel chatViewModel = new ChatViewModel(viewModel.getModel(), course.getTutor(), "Chat with " + tutorName);
            ChatViewController controller = loader.getController();
            controller.init(chatViewModel);

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat with " + tutorName);
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ListCell<model.Course> createCourseCell() {
        return new ListCell<>() {
            private final VBox content = new VBox(4);
            private final HBox header = new HBox(8);
            private final Label nameLabel = new Label();
            private final Label matchLabel = new Label("Match");
            private final Label tutorLabel = new Label();
            private final Label infoLabel = new Label();
            private final Label tagsLabel = new Label();

            {
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                matchLabel.setStyle("-fx-background-color: #dff0d8; -fx-text-fill: #2e7d32; -fx-padding: 2 8 2 8; -fx-background-radius: 4;");
                tutorLabel.setStyle("-fx-text-fill: #5d6d7e;");
                infoLabel.setStyle("-fx-text-fill: #2f2f2f;");
                tagsLabel.setStyle("-fx-text-fill: #7f8c8d;");

                nameLabel.setWrapText(true);
                infoLabel.setWrapText(true);
                tagsLabel.setWrapText(true);

                // Keep text inside the list instead of forcing a horizontal scrollbar.
                content.maxWidthProperty().bind(courseListView.widthProperty().subtract(24));
                header.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                nameLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(140));
                infoLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));
                tagsLabel.maxWidthProperty().bind(courseListView.widthProperty().subtract(40));

                header.getChildren().addAll(nameLabel, spacer, matchLabel);
                content.getChildren().addAll(header, tutorLabel, infoLabel, tagsLabel);
                content.setPadding(new Insets(6, 8, 6, 8));
            }

            @Override
            protected void updateItem(model.Course course, boolean empty) {
                super.updateItem(course, empty);

                if (empty || course == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                nameLabel.setText(safeText(course.getName()));
                tutorLabel.setText("Tutor: " + getTutorName(course));
                infoLabel.setText(safeText(course.getInformation()));

                String tags = course.getTags();
                if (tags == null || tags.isEmpty()) {
                    tagsLabel.setText("");
                    tagsLabel.setVisible(false);
                    tagsLabel.setManaged(false);
                } else {
                    tagsLabel.setText("Tags: " + tags);
                    tagsLabel.setVisible(true);
                    tagsLabel.setManaged(true);
                }

                matchLabel.setVisible(course.matchesUserTags());
                matchLabel.setManaged(course.matchesUserTags());

                setText(null);
                setGraphic(content);
            }
        };
    }

    private String getTutorName(model.Course course) {
        if (course.getTutor() == null || course.getTutor().getName() == null) {
            return "Unknown";
        }
        return course.getTutor().getName();
    }

    private String getCourseName(model.Course course) {
        if (course == null || course.getName() == null) {
            return "";
        }
        return course.getName();
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }
}
