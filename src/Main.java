import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ClientModel;
import view.LoginViewController;
import view.MainDashboardController;
import viewmodel.DashboardViewModel;
import viewmodel.LoginViewModel;

import java.io.IOException;

public class Main extends Application {

    private ClientModel clientModel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Initialize Model and start network connection
        clientModel = new ClientModel();
        clientModel.start(); // This attempts to connect to the Server (make sure ServerMain is running!)

        // 2. Initialize ViewModel
        LoginViewModel loginViewModel = new LoginViewModel(clientModel);

        // 3. Load View (FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
        Parent root = loader.load();

        // 4. Inject ViewModel into Controller
        LoginViewController controller = loader.getController();
        controller.init(loginViewModel);

        // Define Scene transition on successful login
        loginViewModel.setOnLoginSuccess(() -> {
            try {
                FXMLLoader dashLoader = new FXMLLoader(getClass().getResource("/view/MainDashboard.fxml"));
                Parent dashRoot = dashLoader.load();

                DashboardViewModel dashViewModel = new DashboardViewModel(clientModel);
                MainDashboardController dashController = dashLoader.getController();
                dashController.init(dashViewModel);

                Scene dashScene = new Scene(dashRoot, 600, 500);
                primaryStage.setScene(dashScene);
                primaryStage.setTitle("Skills & Tutoring Exchange - Dashboard");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 5. Setup Scene and Stage
        Scene scene = new Scene(root, 400, 500);
        primaryStage.setTitle("Skills & Tutoring Exchange");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up network resources when window is closed
        System.out.println("Application closing, disconnecting...");
        // clientModel.stop(); // If implemented
    }

    public static void main(String[] args) {
        launch(args);
    }
}
