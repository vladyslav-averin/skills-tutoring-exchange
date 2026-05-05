package model;

import java.io.Serializable;

public class Notification implements Serializable {
    private String title;
    private String messageInformation;

    public Notification(String title, String messageInformation) {
        this.title = title;
        this.messageInformation = messageInformation;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessageInformation() {
        return messageInformation;
    }

    public void setMessageInformation(String messageInformation) {
        this.messageInformation = messageInformation;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "title='" + title + '\'' +
                ", messageInformation='" + messageInformation + '\'' +
                '}';
    }
}
