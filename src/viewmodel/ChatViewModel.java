package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.Message;
import model.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ChatViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<Message> messageList;
    private StringProperty messageInput;
    private StringProperty chatStatus;
    private StringProperty chatTitle;
    private User chatPartner;

    public ChatViewModel(ClientModel model, User chatPartner, String chatTitle) {
        this.model = model;
        this.chatPartner = chatPartner;
        this.messageList = FXCollections.observableArrayList();
        this.messageInput = new SimpleStringProperty("");
        this.chatTitle = new SimpleStringProperty(chatTitle);
        this.chatStatus = new SimpleStringProperty("Connected to " + chatTitle + ".");

        this.model.addListener("DirectChatHistoryRetrieved", this);
        this.model.addListener("DirectMessageSent", this);
        this.model.addListener("NewNotification", this); // To receive real-time messages

        fetchHistory();
    }

    public void sendMessage() {
        if (messageInput.get().isEmpty()) {
            return;
        }
        if (chatPartner == null) {
            chatStatus.set("No chat partner selected.");
            return;
        }
        model.sendDirectMessage(chatPartner, messageInput.get());
        messageInput.set("");
    }

    private void fetchHistory() {
        if (chatPartner != null) {
            model.fetchDirectChatHistory(chatPartner);
        }
    }

    public ObservableList<Message> getMessageList() { return messageList; }
    public StringProperty messageInputProperty() { return messageInput; }
    public StringProperty chatStatusProperty() { return chatStatus; }
    public StringProperty chatTitleProperty() { return chatTitle; }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        javafx.application.Platform.runLater(() -> {
            if ("DirectChatHistoryRetrieved".equals(evt.getPropertyName())) {
                if (chatPartner != null) {
                    List<Message> history = (List<Message>) evt.getNewValue();
                    messageList.clear();
                    messageList.addAll(history);
                    chatStatus.set("Direct chat history loaded.");
                }
            } else if ("DirectMessageSent".equals(evt.getPropertyName())) {
                if (chatPartner != null) {
                    if ("SUCCESS".equals(evt.getNewValue())) {
                        fetchHistory();
                    } else {
                        chatStatus.set("Failed to send message.");
                    }
                }
            } else if ("NewNotification".equals(evt.getPropertyName())) {
                // The server sends this only to the receiver of a direct message.
                // We reload this chat so the new message appears without reopening the window.
                model.Notification notif = (model.Notification) evt.getNewValue();
                if (notif.getTitle().equals("New Message")) {
                    fetchHistory();
                } else {
                    fetchHistory();
                }
            }
        });
    }
}
