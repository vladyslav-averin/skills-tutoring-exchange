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

    public ChatViewModel(ClientModel model) {
        this(model, "Global Chat Room");
    }

    public ChatViewModel(ClientModel model, String chatTitle) {
        this(model, null, chatTitle);
    }

    public ChatViewModel(ClientModel model, User chatPartner, String chatTitle) {
        this.model = model;
        this.chatPartner = chatPartner;
        this.messageList = FXCollections.observableArrayList();
        this.messageInput = new SimpleStringProperty("");
        this.chatTitle = new SimpleStringProperty(chatTitle);
        this.chatStatus = new SimpleStringProperty("Connected to " + chatTitle + ".");

        this.model.addListener("ChatHistoryRetrieved", this);
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
            model.sendMessage(messageInput.get());
        } else {
            model.sendDirectMessage(chatPartner, messageInput.get());
        }
        messageInput.set("");
    }

    private void fetchHistory() {
        if (chatPartner == null) {
            model.fetchChatHistory();
        } else {
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
            if ("ChatHistoryRetrieved".equals(evt.getPropertyName())) {
                if (chatPartner == null) {
                    List<Message> history = (List<Message>) evt.getNewValue();
                    messageList.clear();
                    messageList.addAll(history);
                    chatStatus.set("Chat history loaded.");
                }
            } else if ("DirectChatHistoryRetrieved".equals(evt.getPropertyName())) {
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
                // When someone sends a message, Server broadcasts a Notification via Observer pattern.
                // We should technically fetch the new message or the notification could contain the message.
                // For simplicity, whenever there's a notification, we just re-fetch the history so it updates!
                model.Notification notif = (model.Notification) evt.getNewValue();
                if (notif.getTitle().equals("New Message")) {
                    fetchHistory();
                } else {
                    // It's a general notification, just re-fetch chat anyway to be safe
                    fetchHistory();
                }
            }
        });
    }
}
