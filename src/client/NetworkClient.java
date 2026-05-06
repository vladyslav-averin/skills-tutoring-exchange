package client;

import model.ClientModel;
import model.User;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientModel model;

    public NetworkClient(ClientModel model) {
        this.model = model;
    }

    public void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connected to Server at " + SERVER_HOST + ":" + SERVER_PORT);
            startListening();
        } catch (IOException e) {
            System.err.println("Could not connect to the server.");
            e.printStackTrace();
        }
    }

    public void sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                Object incoming;
                while ((incoming = in.readObject()) != null) {
                    if (incoming instanceof Response) {
                        Response response = (Response) incoming;
                        
                        // Handle specific responses based on message
                        if ("Login successful".equals(response.getMessage())) {
                            model.setCurrentUser((User) response.getPayload());
                            model.fireEvent("LoginResult", null, "SUCCESS");
                        } else if ("Invalid credentials".equals(response.getMessage())) {
                            model.fireEvent("LoginResult", null, "FAILED");
                        } else if ("Account created successfully".equals(response.getMessage())) {
                            model.fireEvent("RegisterResult", null, "SUCCESS");
                        } else if ("Failed to create account".equals(response.getMessage())) {
                            model.fireEvent("RegisterResult", null, "FAILED");
                        } else if ("Courses retrieved".equals(response.getMessage())) {
                            model.fireEvent("CoursesRetrieved", null, response.getPayload());
                        } else if ("User tags updated successfully".equals(response.getMessage())) {
                            model.setCurrentUser((User) response.getPayload());
                            model.fireEvent("UserTagsUpdated", null, "SUCCESS");
                        } else if ("Failed to update user tags".equals(response.getMessage())) {
                            model.fireEvent("UserTagsUpdated", null, "FAILED");
                        } else if ("Course added successfully".equals(response.getMessage())) {
                            model.fireEvent("CourseAdded", null, "SUCCESS");
                        } else if ("Failed to add course".equals(response.getMessage())) {
                            model.fireEvent("CourseAdded", null, "FAILED");
                        } else if ("Course deleted successfully".equals(response.getMessage())) {
                            model.fireEvent("CourseDeleted", null, "SUCCESS");
                        } else if ("Failed to delete course".equals(response.getMessage())) {
                            model.fireEvent("CourseDeleted", null, "FAILED");
                        } else if ("Course updated successfully".equals(response.getMessage())) {
                            model.fireEvent("CourseUpdated", null, "SUCCESS");
                        } else if ("Failed to update course".equals(response.getMessage())) {
                            model.fireEvent("CourseUpdated", null, "FAILED");
                        } else if ("Successfully enrolled in course".equals(response.getMessage())) {
                            model.fireEvent("CourseEnrolled", null, "SUCCESS");
                        } else if ("Failed to enroll or already enrolled".equals(response.getMessage())) {
                            model.fireEvent("CourseEnrolled", null, "FAILED");
                        } else if ("Registered courses retrieved".equals(response.getMessage())) {
                            model.fireEvent("RegisteredCoursesRetrieved", null, response.getPayload());
                        } else if ("Registration canceled successfully".equals(response.getMessage())) {
                            model.fireEvent("RegistrationCanceled", null, "SUCCESS");
                        } else if ("Failed to cancel registration".equals(response.getMessage())) {
                            model.fireEvent("RegistrationCanceled", null, "FAILED");
                        } else if ("Direct chat history retrieved".equals(response.getMessage())) {
                            model.fireEvent("DirectChatHistoryRetrieved", null, response.getPayload());
                        } else if ("Chat partners retrieved".equals(response.getMessage())) {
                            model.fireEvent("ChatPartnersRetrieved", null, response.getPayload());
                        } else if ("Direct message sent".equals(response.getMessage())) {
                            model.fireEvent("DirectMessageSent", null, "SUCCESS");
                        } else if ("Failed to send direct message".equals(response.getMessage())) {
                            model.fireEvent("DirectMessageSent", null, "FAILED");
                        }
                        
                    } else if (incoming instanceof model.Notification) {
                        model.Notification notif = (model.Notification) incoming;
                        model.fireEvent("NewNotification", null, notif);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Disconnected from server.");
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
