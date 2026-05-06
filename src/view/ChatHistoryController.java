package view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import model.User;
import viewmodel.ChatHistoryViewModel;
import viewmodel.ChatViewModel;

import java.io.IOException;

public class ChatHistoryController {

    @FXML private ListView<User> chatPartnerListView;
    @FXML private Label statusLabel;

    private ChatHistoryViewModel viewModel;

    public void init(ChatHistoryViewModel viewModel) {
        this.viewModel = viewModel;

        chatPartnerListView.setItems(viewModel.getChatPartners());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        chatPartnerListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSelectedChat();
            }
        });
    }

    @FXML
    public void onOpenChatButton() {
        openSelectedChat();
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshChatPartners();
    }

    private void openSelectedChat() {
        User selectedUser = chatPartnerListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            viewModel.setStatusMessage("Please select a chat.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChatView.fxml"));
            Parent root = loader.load();

            ChatViewModel chatViewModel = new ChatViewModel(viewModel.getModel(), selectedUser, "Chat with " + selectedUser.getName());
            ChatViewController controller = loader.getController();
            controller.init(chatViewModel);

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat with " + selectedUser.getName());
            chatStage.setScene(new Scene(root));
            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
