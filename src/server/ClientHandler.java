package server;

import dao.ChatDAO;
import dao.CourseDAO;
import dao.UserDAO;
import model.Administrator;
import model.Course;
import model.Message;
import model.Notification;
import model.User;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final String MAIN_ADMIN_NAME = "admin";
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private ChatDAO chatDAO;
    private User currentUser;

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
                    // Remember which logged-in user belongs to this socket.
                    // The server uses this to send private notifications only to the right client.
                    currentUser = loggedInUser;
                    return new Response(true, "Login successful", loggedInUser);
                } else {
                    return new Response(false, "Invalid credentials", null);
                }
            case "REGISTER":
                User newUser = (User) request.getPayload();
                // Admin accounts are created by the system, not by the register form.
                if (newUser instanceof Administrator) {
                    return new Response(false, "Failed to create account", null);
                }
                boolean created = userDAO.createUser(newUser);
                if (created) {
                    return new Response(true, "Account created successfully", newUser);
                } else {
                    return new Response(false, "Failed to create account", null);
                }
            case "GET_COURSES":
                return new Response(true, "Courses retrieved", courseDAO.getAllCourses());
            case "GET_USERS":
                if (currentUser instanceof Administrator) {
                    return new Response(true, "Users retrieved", userDAO.getAllUsers());
                } else {
                    return new Response(false, "Only administrators can view users", null);
                }
            case "UPDATE_USER_TAGS":
                User userWithNewTags = (User) request.getPayload();
                boolean tagsUpdated = userDAO.updateUserTags(userWithNewTags);
                if (tagsUpdated) {
                    return new Response(true, "User tags updated successfully", userWithNewTags);
                } else {
                    return new Response(false, "Failed to update user tags", null);
                }
            case "ADD_COURSE":
                Course newCourse = (Course) request.getPayload();
                boolean courseAdded = courseDAO.addCourse(newCourse);
                if (courseAdded) {
                    // Optionally notify all clients about the new course
                    return new Response(true, "Course added successfully", newCourse);
                } else {
                    return new Response(false, "Failed to add course", null);
                }
            case "DELETE_COURSE":
                Object[] deleteData = (Object[]) request.getPayload();
                User userDeletingCourse = (User) deleteData[0];
                Course courseToDelete = (Course) deleteData[1];
                boolean courseDeleted = courseDAO.deleteCourse(courseToDelete, userDeletingCourse);
                if (courseDeleted) {
                    return new Response(true, "Course deleted successfully", courseToDelete);
                } else {
                    return new Response(false, "Failed to delete course", null);
                }
            case "ADMIN_DELETE_COURSE":
                Course adminCourseToDelete = (Course) request.getPayload();
                if (!(currentUser instanceof Administrator)) {
                    return new Response(false, "Only administrators can delete any course", null);
                }
                boolean adminCourseDeleted = courseDAO.deleteCourseAsAdmin(adminCourseToDelete);
                if (adminCourseDeleted) {
                    return new Response(true, "Admin course deleted successfully", adminCourseToDelete);
                } else {
                    return new Response(false, "Failed to delete course as admin", null);
                }
            case "ADMIN_DELETE_USER":
                User userToDelete = (User) request.getPayload();
                if (!(currentUser instanceof Administrator)) {
                    return new Response(false, "Only administrators can delete users", null);
                }
                if (userToDelete == null
                        || userToDelete.getName().equals(currentUser.getName())
                        || MAIN_ADMIN_NAME.equals(userToDelete.getName())) {
                    return new Response(false, "Administrator cannot delete this account", null);
                }
                if (userDAO.isAdministrator(userToDelete.getName()) && !isMainAdmin()) {
                    return new Response(false, "Only main administrator can delete administrators", null);
                }
                boolean adminUserDeleted = userDAO.deleteUser(userToDelete.getName());
                if (adminUserDeleted) {
                    return new Response(true, "Admin user deleted successfully", userToDelete);
                } else {
                    return new Response(false, "Failed to delete user as admin", null);
                }
            case "ADMIN_PROMOTE_USER":
                User userToPromote = (User) request.getPayload();
                if (!isMainAdmin()) {
                    return new Response(false, "Only main administrator can change user roles", null);
                }
                if (userToPromote == null || userToPromote.getName().equals(currentUser.getName())) {
                    return new Response(false, "Failed to promote user to admin", null);
                }
                boolean userPromoted = userDAO.promoteStudentToAdmin(userToPromote.getName());
                if (userPromoted) {
                    return new Response(true, "User promoted to admin successfully", userToPromote);
                } else {
                    return new Response(false, "Failed to promote user to admin", null);
                }
            case "ADMIN_DEMOTE_USER":
                User userToDemote = (User) request.getPayload();
                if (!isMainAdmin()) {
                    return new Response(false, "Only main administrator can change user roles", null);
                }
                if (userToDemote == null
                        || userToDemote.getName().equals(currentUser.getName())
                        || MAIN_ADMIN_NAME.equals(userToDemote.getName())) {
                    return new Response(false, "Failed to demote user to student", null);
                }
                boolean userDemoted = userDAO.demoteAdminToStudent(userToDemote.getName());
                if (userDemoted) {
                    return new Response(true, "User demoted to student successfully", userToDemote);
                } else {
                    return new Response(false, "Failed to demote user to student", null);
                }
            case "UPDATE_COURSE":
                Object[] updateData = (Object[]) request.getPayload();
                User userUpdatingCourse = (User) updateData[0];
                Course courseToUpdate = (Course) updateData[1];
                boolean courseUpdated = courseDAO.updateCourse(courseToUpdate, userUpdatingCourse);
                if (courseUpdated) {
                    return new Response(true, "Course updated successfully", courseToUpdate);
                } else {
                    return new Response(false, "Failed to update course", null);
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
            case "GET_REGISTERED_COURSES":
                User userForRegistrations = (User) request.getPayload();
                return new Response(true, "Registered courses retrieved", courseDAO.getRegisteredCourses(userForRegistrations));
            case "CANCEL_REGISTRATION":
                Object[] cancelData = (Object[]) request.getPayload();
                User userCancelingRegistration = (User) cancelData[0];
                Course courseToCancel = (Course) cancelData[1];
                boolean registrationCanceled = courseDAO.cancelRegistration(courseToCancel, userCancelingRegistration);
                if (registrationCanceled) {
                    return new Response(true, "Registration canceled successfully", courseToCancel);
                } else {
                    return new Response(false, "Failed to cancel registration", null);
                }
            case "GET_DIRECT_CHAT_HISTORY":
                Object[] chatUsers = (Object[]) request.getPayload();
                User currentUser = (User) chatUsers[0];
                User chatPartner = (User) chatUsers[1];
                return new Response(true, "Direct chat history retrieved", chatDAO.getDirectChatHistory(currentUser, chatPartner));
            case "GET_CHAT_PARTNERS":
                User userForChatHistory = (User) request.getPayload();
                return new Response(true, "Chat partners retrieved", chatDAO.getChatPartners(userForChatHistory));
            case "SEND_DIRECT_MESSAGE":
                Message directMessage = (Message) request.getPayload();
                boolean directMessageSaved = chatDAO.saveMessage(directMessage);
                if (directMessageSaved) {
                    // Direct chats are private, so only the receiver should get the notification.
                    // The sender already gets the normal "Direct message sent" response below.
                    if (directMessage.getReceiver() != null) {
                        Notification notification = new Notification("New Message",
                                "New message from " + directMessage.getSender().getName() + ": " + directMessage.getText());
                        ServerMain.sendToUser(directMessage.getReceiver().getName(), notification);
                    }
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

    public String getCurrentUsername() {
        if (currentUser == null) {
            return null;
        }
        return currentUser.getName();
    }

    private boolean isMainAdmin() {
        return currentUser instanceof Administrator && MAIN_ADMIN_NAME.equals(currentUser.getName());
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
