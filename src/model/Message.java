package model;

import java.time.LocalDateTime;
import java.io.Serializable;

public class Message implements Serializable {
    private User sender;
    private LocalDateTime timeStamp;
    private String text;

    public Message(User sender, String text) {
        this.sender = sender;
        this.text = text;
        this.timeStamp = LocalDateTime.now();
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "[" + timeStamp.toLocalTime() + "] " + sender.getName() + ": " + text;
    }
}
