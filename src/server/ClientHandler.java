package server;

import dao.ChatDAO;
import dao.CourseDAO;
import dao.UserDAO;
import model.Course;
import model.Message;
import model.User;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private ChatDAO chatDAO;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.chatDAO = new ChatDAO();
        try {
            // Output stream must be initialized first to flush the header
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object inputObject;
            // Listen continuously for incoming requests from this specific client
            while ((inputObject = in.readObject()) != null) {
                if (inputObject instanceof Request) {
                    Request request = (Request) inputObject;
                    Response response = handleRequest(request);
                    sendDataToClient(response);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            closeConnections();
            ServerMain.removeClient(this);
        }
    }

    // Process the Request and use DAOs to interact with the DB
    private Response handleRequest(Request request) {
        switch (request.getType()) {
            case "LOGIN":
                // Expecting a User object or String array with [name, password]
                String[] credentials = (String[]) request.getPayload();
                User loggedInUser = userDAO.authenticateUser(credentials[0], credentials[1]);
                if (loggedInUser != null) {
                    return new Response(true, "Login successful", loggedInUser);
                } else {
                    return new Response(false, "Invalid credentials", null);
                }
            case "REGISTER":
                User newUser = (User) request.getPayload();
                boolean created = userDAO.createUser(newUser);
                if (created) {
                    return new Response(true, "Account created successfully", newUser);
                } else {
                    return new Response(false, "Failed to create account", null);
                }
            case "GET_COURSES":
                return new Response(true, "Courses retrieved", courseDAO.getAllCourses());
            case "ADD_COURSE":
                Course newCourse = (Course) request.getPayload();
                boolean courseAdded = courseDAO.addCourse(newCourse);
                if (courseAdded) {
                    // Optionally notify all clients about the new course
                    return new Response(true, "Course added successfully", newCourse);
                } else {
                    return new Response(false, "Failed to add course", null);
                }
            case "ENROLL_COURSE":
                // Payload contains array [Student, Course]
                Object[] enrollData = (Object[]) request.getPayload();
                model.Student studentToEnroll = (model.Student) enrollData[0];
                Course courseToEnroll = (Course) enrollData[1];
                boolean enrolled = courseDAO.enrollStudent(studentToEnroll, courseToEnroll);
                if (enrolled) {
                    return new Response(true, "Successfully enrolled in course", courseToEnroll);
                } else {
                    return new Response(false, "Failed to enroll or already enrolled", null);
                }
            case "GET_CHAT_HISTORY":
                return new Response(true, "Chat history retrieved", chatDAO.getChatHistory());
            case "GET_DIRECT_CHAT_HISTORY":
                Object[] chatUsers = (Object[]) request.getPayload();
                User currentUser = (User) chatUsers[0];
                User chatPartner = (User) chatUsers[1];
                return new Response(true, "Direct chat history retrieved", chatDAO.getDirectChatHistory(currentUser, chatPartner));
            case "GET_CHAT_PARTNERS":
                User userForChatHistory = (User) request.getPayload();
                return new Response(true, "Chat partners retrieved", chatDAO.getChatPartners(userForChatHistory));
            case "SEND_MESSAGE":
                Message message = (Message) request.getPayload();
                boolean messageSaved = chatDAO.saveMessage(message);
                if (messageSaved) {
                    // Add message to global chat to trigger the Observer Pattern Notification broadcast
                    ServerMain.getGlobalChat().addMessage(message);
                    return new Response(true, "Message sent", null);
                } else {
                    return new Response(false, "Failed to send message", null);
                }
            case "SEND_DIRECT_MESSAGE":
                Message directMessage = (Message) request.getPayload();
                boolean directMessageSaved = chatDAO.saveMessage(directMessage);
                if (directMessageSaved) {
                    ServerMain.getGlobalChat().addMessage(directMessage);
                    return new Response(true, "Direct message sent", null);
                } else {
                    return new Response(false, "Failed to send direct message", null);
                }
            default:
                return new Response(false, "Unknown request type", null);
        }
    }

    // Method to push data back to the client
    public void sendDataToClient(Object data) {
        try {
            out.writeObject(data);
            out.flush();
            out.reset(); // Clear the object cache to send fresh data next time
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
