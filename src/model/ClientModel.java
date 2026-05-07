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

    public void registerStudent(String username, String password) {
        User newUser = new Student(username, password);
        networkClient.sendRequest(new Request("REGISTER", newUser));
    }

    public void fetchCourses() {
        networkClient.sendRequest(new Request("GET_COURSES", null));
    }

    public void fetchUsers() {
        networkClient.sendRequest(new Request("GET_USERS", null));
    }

    public void updateCurrentUserTags(String tags) {
        currentUser.setTags(tags);
        networkClient.sendRequest(new Request("UPDATE_USER_TAGS", currentUser));
    }

    public void addCourse(model.Course course) {
        networkClient.sendRequest(new Request("ADD_COURSE", course));
    }

    public void deleteCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("DELETE_COURSE", payload));
    }

    public void adminDeleteCourse(model.Course course) {
        networkClient.sendRequest(new Request("ADMIN_DELETE_COURSE", course));
    }

    public void adminDeleteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_DELETE_USER", user));
    }

    public void adminPromoteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_PROMOTE_USER", user));
    }

    public void adminDemoteUser(User user) {
        networkClient.sendRequest(new Request("ADMIN_DEMOTE_USER", user));
    }

    public void updateCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("UPDATE_COURSE", payload));
    }

    public void enrollCourse(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("ENROLL_COURSE", payload));
    }

    public void fetchRegisteredCourses() {
        networkClient.sendRequest(new Request("GET_REGISTERED_COURSES", currentUser));
    }

    public void cancelRegistration(model.Course course) {
        Object[] payload = {currentUser, course};
        networkClient.sendRequest(new Request("CANCEL_REGISTRATION", payload));
    }

    public void fetchDirectChatHistory(User chatPartner) {
        Object[] payload = {currentUser, chatPartner};
        networkClient.sendRequest(new Request("GET_DIRECT_CHAT_HISTORY", payload));
    }

    public void fetchChatPartners() {
        networkClient.sendRequest(new Request("GET_CHAT_PARTNERS", currentUser));
    }

    public void sendDirectMessage(User receiver, String text) {
        model.Message msg = new model.Message(currentUser, receiver, text);
        networkClient.sendRequest(new Request("SEND_DIRECT_MESSAGE", msg));
    }

    public void logout() {
        currentUser = null;
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
