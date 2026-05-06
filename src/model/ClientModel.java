package model;

import client.NetworkClient;
import network.Request;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class ClientModel {
    private NetworkClient networkClient;
    private User currentUser;
    private PropertyChangeSupport support;

    public ClientModel() {
        this.support = new PropertyChangeSupport(this);
        this.networkClient = new NetworkClient(this);
    }

    public void start() {
        networkClient.connect();
    }

    public void login(String username, String password) {
        String[] credentials = {username, password};
        networkClient.sendRequest(new Request("LOGIN", credentials));
    }

    public void register(String userType, String username, String password) {
        User newUser;
        if ("Administrator".equals(userType)) {
            newUser = new Administrator(username, password);
        } else {
            newUser = new Student(username, password);
        }
        networkClient.sendRequest(new Request("REGISTER", newUser));
    }

    public void fetchCourses() {
        networkClient.sendRequest(new Request("GET_COURSES", null));
    }

    public void addCourse(model.Course course) {
        networkClient.sendRequest(new Request("ADD_COURSE", course));
    }

    public void deleteCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("DELETE_COURSE", payload));
    }

    public void updateCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("UPDATE_COURSE", payload));
    }

    public void enrollCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("ENROLL_COURSE", payload));
    }

    public void fetchChatHistory() {
        networkClient.sendRequest(new Request("GET_CHAT_HISTORY", null));
    }

    public void fetchDirectChatHistory(User chatPartner) {
        Object[] payload = {currentUser, chatPartner};
        networkClient.sendRequest(new Request("GET_DIRECT_CHAT_HISTORY", payload));
    }

    public void fetchChatPartners() {
        networkClient.sendRequest(new Request("GET_CHAT_PARTNERS", currentUser));
    }

    public void sendMessage(String text) {
        model.Message msg = new model.Message(currentUser, text);
        networkClient.sendRequest(new Request("SEND_MESSAGE", msg));
    }

    public void sendDirectMessage(User receiver, String text) {
        model.Message msg = new model.Message(currentUser, receiver, text);
        networkClient.sendRequest(new Request("SEND_DIRECT_MESSAGE", msg));
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void addListener(String eventName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(eventName, listener);
    }

    public void removeListener(String eventName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(eventName, listener);
    }
    
    // Called by NetworkClient when a response arrives
    public void fireEvent(String eventName, Object oldValue, Object newValue) {
        support.firePropertyChange(eventName, oldValue, newValue);
    }
}
