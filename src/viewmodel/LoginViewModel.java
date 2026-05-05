package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.ClientModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginViewModel implements PropertyChangeListener {
    private ClientModel model;

    // Data binding properties for the view
    private StringProperty usernameProperty;
    private StringProperty passwordProperty;
    private StringProperty errorProperty;
    private Runnable onLoginSuccess;

    public LoginViewModel(ClientModel model) {
        this.model = model;
        this.usernameProperty = new SimpleStringProperty("");
        this.passwordProperty = new SimpleStringProperty("");
        this.errorProperty = new SimpleStringProperty("");

        // Listen for responses from the server via the Model
        this.model.addListener("LoginResult", this);
        this.model.addListener("RegisterResult", this);
    }

    public void login() {
        if (usernameProperty.get().isEmpty() || passwordProperty.get().isEmpty()) {
            errorProperty.set("Please enter both username and password.");
            return;
        }
        errorProperty.set("Connecting...");
        model.login(usernameProperty.get(), passwordProperty.get());
    }

    public void register(String userType) {
        if (usernameProperty.get().isEmpty() || passwordProperty.get().isEmpty()) {
            errorProperty.set("Please enter both username and password to register.");
            return;
        }
        errorProperty.set("Registering...");
        model.register(userType, usernameProperty.get(), passwordProperty.get());
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public StringProperty usernameProperty() { return usernameProperty; }
    public StringProperty passwordProperty() { return passwordProperty; }
    public StringProperty errorProperty() { return errorProperty; }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // This is called when the Model fires an event (which happens when NetworkClient receives a Response)
        // Since this might be called from a background thread, JavaFX UI updates should ideally be wrapped in Platform.runLater()
        // We will handle that in the Controller or here. For now, we update the property.
        
        javafx.application.Platform.runLater(() -> {
            if ("LoginResult".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    errorProperty.set("Login Successful! Welcome " + model.getCurrentUser().getName());
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } else {
                    errorProperty.set("Invalid credentials. Try again.");
                }
            } else if ("RegisterResult".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    errorProperty.set("Registration Successful! You can now login.");
                } else {
                    errorProperty.set("Registration Failed.");
                }
            }
        });
    }
}
