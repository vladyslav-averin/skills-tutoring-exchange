package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Message;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ChatViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<Message> messageList;
    private StringProperty messageInput;
    private StringProperty chatStatus;

    public ChatViewModel(ClientModel model) {
        this.model = model;
        this.messageList = FXCollections.observableArrayList();
        this.messageInput = new SimpleStringProperty("");
        this.chatStatus = new SimpleStringProperty("Connected to chat.");

        this.model.addListener("ChatHistoryRetrieved", this);
        this.model.addListener("NewNotification", this); // To receive real-time messages

        this.model.fetchChatHistory();
    }

    public void sendMessage() {
        if (messageInput.get().isEmpty()) {
            return;
        }
        model.sendMessage(messageInput.get());
        messageInput.set("");
    }

    public ObservableList<Message> getMessageList() { return messageList; }
    public StringProperty messageInputProperty() { return messageInput; }
    public StringProperty chatStatusProperty() { return chatStatus; }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            if ("ChatHistoryRetrieved".equals(evt.getPropertyName())) {
                List<Message> history = (List<Message>) evt.getNewValue();
                messageList.clear();
                messageList.addAll(history);
                chatStatus.set("Chat history loaded.");
            } else if ("NewNotification".equals(evt.getPropertyName())) {
                // When someone sends a message, Server broadcasts a Notification via Observer pattern.
                // We should technically fetch the new message or the notification could contain the message.
                // For simplicity, whenever there's a notification, we just re-fetch the history so it updates!
                model.Notification notif = (model.Notification) evt.getNewValue();
                if (notif.getTitle().equals("New Message")) {
                    model.fetchChatHistory();
                } else {
                    // It's a general notification, just re-fetch chat anyway to be safe
                    model.fetchChatHistory();
                }
            }
        });
    }
}
