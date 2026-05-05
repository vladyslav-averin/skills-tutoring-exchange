package view;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewmodel.LoginViewModel;

public class LoginViewController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private ComboBox<String> roleComboBox;

    private LoginViewModel viewModel;

    // Called automatically after FXML is loaded
    public void init(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        
        // Initialize ComboBox for registration role
        roleComboBox.getItems().addAll("Student", "Administrator");
        roleComboBox.getSelectionModel().selectFirst();

        // Bind UI fields directly to ViewModel properties (Bidirectional for input fields)
        usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        
        // Unidirectional bind for the error label
        errorLabel.textProperty().bind(viewModel.errorProperty());
    }

    @FXML
    public void onLoginButton() {
        viewModel.login();
    }

    @FXML
    public void onRegisterButton() {
        String selectedRole = roleComboBox.getValue();
        viewModel.register(selectedRole);
    }
}
